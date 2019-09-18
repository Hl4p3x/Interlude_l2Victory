package ru.j2dev.gameserver.model;

import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.commons.util.concurrent.atomic.AtomicState;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ai.CtrlEvent;
import ru.j2dev.gameserver.ai.CtrlIntention;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.geodata.GeoEngine;
import ru.j2dev.gameserver.model.AggroList.AggroInfo;
import ru.j2dev.gameserver.model.Skill.SkillTargetType;
import ru.j2dev.gameserver.model.Skill.SkillType;
import ru.j2dev.gameserver.model.Zone.ZoneType;
import ru.j2dev.gameserver.model.base.TeamType;
import ru.j2dev.gameserver.model.entity.events.GlobalEvent;
import ru.j2dev.gameserver.model.entity.events.impl.DuelEvent;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.instances.StaticObjectInstance;
import ru.j2dev.gameserver.model.items.Inventory;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExServerPrimitive;
import ru.j2dev.gameserver.network.lineage2.serverpackets.Revive;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.skills.EffectType;
import ru.j2dev.gameserver.stats.Stats;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.templates.CharTemplate;
import ru.j2dev.gameserver.templates.item.EtcItemTemplate;
import ru.j2dev.gameserver.templates.item.WeaponTemplate;
import ru.j2dev.gameserver.templates.item.WeaponTemplate.WeaponType;
import ru.j2dev.gameserver.utils.Location;

import java.awt.*;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class Playable extends Creature {
    protected final ReadWriteLock questLock;
    protected final Lock questRead;
    protected final Lock questWrite;
    private final AtomicState _isSilentMoving;
    private boolean _isPendingRevive;
    private long _nonAggroTime;
    private int pvpEventMode;

    public Playable(final int objectId, final CharTemplate template) {
        super(objectId, template);
        _isSilentMoving = new AtomicState();
        questLock = new ReentrantReadWriteLock();
        questRead = questLock.readLock();
        questWrite = questLock.writeLock();
        _nonAggroTime = 0L;
    }

    @SuppressWarnings("unchecked")
    @Override
    public HardReference<? extends Playable> getRef() {
        return (HardReference<? extends Playable>) super.getRef();
    }

    public abstract Inventory getInventory();

    public abstract long getWearedMask();

    @Override
    public boolean checkPvP(final Creature target, final Skill skill) {
        final Player player = getPlayer();
        if (isDead() || target == null || player == null || target == this || target == player || target == player.getPet() || player.getKarma() > 0) {
            return false;
        }
        if (skill != null) {
            if (skill.altUse()) {
                return false;
            }
            if (skill.getTargetType() == SkillTargetType.TARGET_FEEDABLE_BEAST) {
                return false;
            }
            if (skill.getTargetType() == SkillTargetType.TARGET_UNLOCKABLE) {
                return false;
            }
            if (skill.getTargetType() == SkillTargetType.TARGET_CHEST) {
                return false;
            }
        }
        final DuelEvent duelEvent = getEvent(DuelEvent.class);
        if (duelEvent != null && duelEvent == target.getEvent(DuelEvent.class)) {
            return false;
        }
        if (isInZonePeace() && target.isInZonePeace()) {
            return false;
        }
        if (isInZoneBattle() && target.isInZoneBattle()) {
            return false;
        }
        if (isInZone(ZoneType.SIEGE) && target.isInZone(ZoneType.SIEGE)) {
            return false;
        }
        if (isInZone(ZoneType.fun) && target.isInZone(ZoneType.fun)) {
            return false;
        }
        if (skill == null || skill.isOffensive()) {
            if (target.getKarma() > 0) {
                return false;
            }
            return target.isPlayable();
        } else return target.getPvpFlag() > 0 || target.getKarma() > 0 || target.isMonster();
    }

    public boolean checkTarget(final Creature target) {
        final Player player = getPlayer();
        if (player == null) {
            return false;
        }
        if (target == null || target.isDead()) {
            player.sendPacket(Msg.INVALID_TARGET);
            return false;
        }
        if (!isInRange(target, 2000L)) {
            player.sendPacket(Msg.YOUR_TARGET_IS_OUT_OF_RANGE);
            return false;
        }
        if (target.isDoor() && !target.isAttackable(this)) {
            player.sendPacket(Msg.INVALID_TARGET);
            return false;
        }
        if (target.paralizeOnAttack(this)) {
            if (Config.PARALIZE_ON_RAID_DIFF) {
                paralizeMe(target);
            }
            return false;
        }
        if (target.isInvisible() || getReflection() != target.getReflection() || !GeoEngine.canSeeTarget(this, target, false)) {
            player.sendPacket(SystemMsg.CANNOT_SEE_TARGET);
            return false;
        }
        if (player.isInZone(ZoneType.epic) != target.isInZone(ZoneType.epic)) {
            player.sendPacket(Msg.INVALID_TARGET);
            return false;
        }
        if (target.isPlayable()) {
            if (isInZoneBattle() != target.isInZoneBattle()) {
                player.sendPacket(Msg.INVALID_TARGET);
                return false;
            }
            if (isInZonePeace() || target.isInZonePeace()) {
                player.sendPacket(Msg.YOU_MAY_NOT_ATTACK_THIS_TARGET_IN_A_PEACEFUL_ZONE);
                return false;
            }
            if (player.isOlyParticipant() && !player.isOlyCompetitionStarted()) {
                return false;
            }
            if (target.isPlayer()) {
                final Player pcAttacker = target.getPlayer();
                if (player.isOlyParticipant()) {
                    if (pcAttacker.isOlyParticipant() && player.getOlyParticipant().getCompetition() != pcAttacker.getOlyParticipant().getCompetition()) {
                        return false;
                    }
                    if (player.isOlyCompetitionStarted() && player.getOlyParticipant() == pcAttacker.getOlyParticipant()) {
                        return false;
                    }
                    return !player.isLooseOlyCompetition();
                }
            }
        }
        return true;
    }

    @Override
    public void setXYZ(final int x, final int y, final int z, final boolean MoveTask) {
        super.setXYZ(x, y, z, MoveTask);
        if (MoveTask && isPlayable()) {
            final Player player = getPlayer();
            final int dbgMove = player.getVarInt("debugMove", 0);
            if (dbgMove > 0) {
                final Location loc = getLoc();
                final ExServerPrimitive tracePkt = new ExServerPrimitive(loc.toXYZString(), loc.getX(), loc.getY(), (int) (loc.getZ() + getColHeight() + 16.0));
                if (moveAction != null) {
                    final Color[] ccs = {Color.CYAN, Color.BLUE, Color.GREEN, Color.ORANGE, Color.MAGENTA, Color.RED, Color.YELLOW, Color.RED};
                    final Color c = ccs[System.identityHashCode(moveAction) % ccs.length];
                    tracePkt.addPoint(String.format("%s|%08x", loc.toXYZString(), moveAction.hashCode()), c, true, loc.getX(), loc.getY(), loc.getZ());
                } else {
                    tracePkt.addPoint(loc.toXYZString(), 0xffffff, true, loc.getX(), loc.getY(), loc.getZ());
                }
                player.sendPacket(tracePkt);
                if (dbgMove > 1) {
                    player.broadcastPacketToOthers(tracePkt);
                }
            }
        }
    }

    @Override
    public void doAttack(final Creature target) {
        final Player player = getPlayer();
        if (player == null) {
            return;
        }
        if (isAMuted() || isAttackingNow()) {
            player.sendActionFailed();
            return;
        }
        if (player.isInObserverMode()) {
            player.sendMessage(new CustomMessage("l2p.gameserver.model.L2Playable.OutOfControl.ObserverNoAttack", player));
            return;
        }
        if (!checkTarget(target)) {
            getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
            player.sendActionFailed();
            return;
        }
        final DuelEvent duelEvent = getEvent(DuelEvent.class);
        if (duelEvent != null && target.getEvent(DuelEvent.class) != duelEvent) {
            duelEvent.abortDuel(getPlayer());
        }
        final WeaponTemplate weaponItem = getActiveWeaponItem();
        if (weaponItem != null && weaponItem.getItemType() == WeaponType.BOW) {
            double bowMpConsume = weaponItem.getMpConsume();
            if (bowMpConsume > 0.0) {
                final double chance = calcStat(Stats.MP_USE_BOW_CHANCE, 0.0, target, null);
                if (chance > 0.0 && Rnd.chance(chance)) {
                    bowMpConsume = calcStat(Stats.MP_USE_BOW, bowMpConsume, target, null);
                }
                if (_currentMp < bowMpConsume) {
                    getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
                    player.sendPacket(Msg.NOT_ENOUGH_MP);
                    player.sendActionFailed();
                    return;
                }
                reduceCurrentMp(bowMpConsume, null);
            }
            if (!player.checkAndEquipArrows()) {
                getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
                player.sendPacket(Msg.YOU_HAVE_RUN_OUT_OF_ARROWS);
                player.sendActionFailed();
                return;
            }
        }
        super.doAttack(target);
    }

    public void doPurePk(final Player killer) {
        final int pkCountMulti = Math.max(killer.getPkKills() / 2, 1);
        killer.increaseKarma(Config.KARMA_MIN_KARMA * pkCountMulti);
    }

    @Override
    public void doCast(final Skill skill, final Creature target, final boolean forceUse) {
        if (skill == null) {
            return;
        }
        final DuelEvent duelEvent = getEvent(DuelEvent.class);
        if (duelEvent != null && target.getEvent(DuelEvent.class) != duelEvent) {
            duelEvent.abortDuel(getPlayer());
        }
        if (isInPeaceZone() && (skill.getTargetType() == SkillTargetType.TARGET_AREA || skill.getTargetType() == SkillTargetType.TARGET_AURA || skill.getTargetType() == SkillTargetType.TARGET_MULTIFACE || skill.getTargetType() == SkillTargetType.TARGET_MULTIFACE_AURA)) {
            getPlayer().sendPacket(Msg.YOU_MAY_NOT_ATTACK_IN_A_PEACEFUL_ZONE);
            return;
        }
        if (skill.getSkillType() == SkillType.DEBUFF && skill.isMagic() && target.isNpc() && target.isInvul() && !target.isMonster()) {
            getPlayer().sendPacket(Msg.INVALID_TARGET);
            return;
        }
        super.doCast(skill, target, forceUse);
    }

    @Override
    public void reduceCurrentHp(final double damage, final Creature attacker, final Skill skill, final boolean awake, final boolean standUp, final boolean directHp, final boolean canReflect, final boolean transferDamage, final boolean isDot, final boolean sendMessage) {
        if (attacker == null || isDead() || (attacker.isDead() && !isDot)) {
            return;
        }
        if (isDamageBlocked() && transferDamage) {
            return;
        }
        if (isDamageBlocked() && attacker != this) {
            if (sendMessage) {
                attacker.sendPacket(Msg.THE_ATTACK_HAS_BEEN_BLOCKED);
            }
            return;
        }
        if (attacker != this && attacker.isPlayable()) {
            final Player player = getPlayer();
            final Player pcAttacker = attacker.getPlayer();
            if (pcAttacker != player && player.isOlyParticipant() && !player.isOlyCompetitionStarted()) {
                if (sendMessage) {
                    pcAttacker.sendPacket(Msg.INVALID_TARGET);
                }
                return;
            }
            if (isInZoneBattle() != attacker.isInZoneBattle()) {
                if (sendMessage) {
                    attacker.getPlayer().sendPacket(Msg.INVALID_TARGET);
                }
                return;
            }
            final DuelEvent duelEvent = getEvent(DuelEvent.class);
            if (duelEvent != null && attacker.getEvent(DuelEvent.class) != duelEvent) {
                duelEvent.abortDuel(player);
            }
        }
        super.reduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, canReflect, transferDamage, isDot, sendMessage);
    }

    @Override
    public int getPAtkSpd() {
        return Math.max((int) calcStat(Stats.POWER_ATTACK_SPEED, calcStat(Stats.ATK_BASE, _template.getBasePAtkSpd(), null, null), null, null), 1);
    }

    @Override
    public int getPAtk(final Creature target) {
        final double init = (getActiveWeaponInstance() == null) ? _template.getBasePAtk() : 0.0;
        return (int) calcStat(Stats.POWER_ATTACK, init, target, null);
    }

    @Override
    public int getMAtk(final Creature target, final Skill skill) {
        if (skill != null && skill.getMatak() > 0) {
            return skill.getMatak();
        }
        final double init = (getActiveWeaponInstance() == null) ? _template.getBaseMAtk() : 0.0;
        return (int) calcStat(Stats.MAGIC_ATTACK, init, target, skill);
    }

    @Override
    public boolean isAttackable(final Creature attacker) {
        return isCtrlAttackable(attacker, true, false);
    }

    @Override
    public boolean isAutoAttackable(final Creature attacker) {
        return isCtrlAttackable(attacker, false, false);
    }

    public boolean isCtrlAttackable(final Creature attacker, final boolean force, final boolean witchCtrl) {
        final Player player = getPlayer();
        if (attacker == null || player == null || attacker == this || (attacker == player && !force) || isAlikeDead() || attacker.isAlikeDead()) {
            return false;
        }
        if (isInvisible() || getReflection() != attacker.getReflection()) {
            return false;
        }
        if (isInBoat()) {
            return false;
        }
        if (attacker == getPet()) {
            return false;
        }
        for (final GlobalEvent e : getEvents()) {
            if (e.checkForAttack(attacker, this, null, force) != null) {
                return false;
            }
        }
        for (final GlobalEvent e : player.getEvents()) {
            if (e.canAttack(this, attacker, null, force)) {
                return true;
            }
        }
        final Player pcAttacker = attacker.getPlayer();
        if (pcAttacker == null || pcAttacker == player) {
            return true;
        }
        if (pcAttacker.isInBoat()) {
            return false;
        }
        if ((pcAttacker.isCursedWeaponEquipped() && player.getLevel() < 21) || (player.isCursedWeaponEquipped() && pcAttacker.getLevel() < 21)) {
            return false;
        }
        if (player.isInZone(ZoneType.epic) != pcAttacker.isInZone(ZoneType.epic)) {
            return false;

        }
        if (player.getPvpEventMode() > 0 || pcAttacker.getPvpEventMode() > 0) {
            if (player.getPvpEventMode() != pcAttacker.getPvpEventMode()) {
                return false;
            }

            if (player.getPvpEventMode() == 2 && player.getTeam() == pcAttacker.getTeam()) {
                return false;
            }
        }
        if (player.isOlyParticipant()) {
            if (pcAttacker.isOlyParticipant() && player.getOlyParticipant().getCompetition() != pcAttacker.getOlyParticipant().getCompetition()) {
                return false;
            }
            if (player.isOlyCompetitionStarted() && player.getOlyParticipant() == pcAttacker.getOlyParticipant()) {
                return false;
            }
            if (player.isLooseOlyCompetition()) {
                return false;
            }
            if (player.getClan() != null && player.getClan() == pcAttacker.getClan()) {
                return true;
            }
        }
        if (player.getTeam() != TeamType.NONE && player.getTeam() == pcAttacker.getTeam()) {
            return false;
        }
        if (isInZonePeace()) {
            return false;
        }
        if (!force && player.getParty() != null && player.getParty() == pcAttacker.getParty()) {
            return false;
        }
        if (isInZoneBattle()) {
            return true;
        }
        if (!force) {
            if (player.getClan() != null && player.getClan() == pcAttacker.getClan()) {
                return false;
            }
            if (Config.ALLY_ALLOW_BUFF_DEBUFFS && player.getAlliance() != null && player.getAlliance() == pcAttacker.getAlliance()) {
                return false;
            }
        }
        return isInZone(ZoneType.SIEGE) || isInZone(ZoneType.fun) || pcAttacker.atMutualWarWith(player) || (player.getKarma() > 0 || player.getPvpFlag() != 0) || (witchCtrl && player.getPvpFlag() > 0) || force;
    }

    @Override
    public int getKarma() {
        final Player player = getPlayer();
        return (player == null) ? 0 : player.getKarma();
    }

    @Override
    public void callSkill(final Skill skill, final List<Creature> targets, final boolean useActionSkills) {
        final Player player = getPlayer();
        if (player == null) {
            return;
        }
        if (useActionSkills && !skill.altUse() && skill.getSkillType() != SkillType.BEAST_FEED) {
            for (final Creature target : targets) {
                if (target.isNpc()) {
                    if (skill.isOffensive()) {
                        if (target.paralizeOnAttack(player)) {
                            if (Config.PARALIZE_ON_RAID_DIFF) {
                                paralizeMe(target);
                            }
                            return;
                        }
                        if (!skill.isAI()) {
                            final int damage = (skill.getEffectPoint() != 0) ? skill.getEffectPoint() : 1;
                            target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this, damage);
                        }
                    }
                    target.getAI().notifyEvent(CtrlEvent.EVT_SEE_SPELL, skill, this);
                } else if (target.isPlayable() && target != getPet() && ((!isSummon() && !isPet()) || target != player)) {
                    final int aggro = (skill.getEffectPoint() != 0) ? skill.getEffectPoint() : Math.max(1, (int) skill.getPower());
                    final List<NpcInstance> npcs = World.getAroundNpc(target);
                    for (final NpcInstance npc : npcs) {
                        if (!npc.isDead()) {
                            if (!npc.isInRangeZ(this, 2000L)) {
                                continue;
                            }
                            npc.getAI().notifyEvent(CtrlEvent.EVT_SEE_SPELL, skill, this);
                            final AggroInfo ai = npc.getAggroList().get(target);
                            if (ai == null) {
                                continue;
                            }
                            if (!skill.isHandler() && npc.paralizeOnAttack(player)) {
                                if (Config.PARALIZE_ON_RAID_DIFF) {
                                    paralizeMe(npc);
                                }
                                return;
                            }
                            if (ai.hate < 100) {
                                continue;
                            }
                            if (!GeoEngine.canSeeTarget(npc, target, false)) {
                                continue;
                            }
                            npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, this, (ai.damage == 0) ? (aggro / 2) : aggro);
                        }
                    }
                }
                if (checkPvP(target, skill)) {
                    startPvPFlag(target);
                }
            }
        }
        super.callSkill(skill, targets, useActionSkills);
    }

    public void broadcastPickUpMsg(final ItemInstance item) {
        final Player player = getPlayer();
        if (item == null || player == null || player.isInvisible()) {
            return;
        }
        if (item.isEquipable() && !(item.getTemplate() instanceof EtcItemTemplate)) {
            SystemMessage msg;
            final String player_name = player.getName();
            if (item.getEnchantLevel() > 0) {
                final int msg_id = isPlayer() ? 1534 : 1536;
                msg = new SystemMessage(msg_id).addString(player_name).addNumber(item.getEnchantLevel()).addItemName(item.getItemId());
            } else {
                final int msg_id = isPlayer() ? 1533 : 1536;
                msg = new SystemMessage(msg_id).addString(player_name).addItemName(item.getItemId());
            }
            player.broadcastPacket(msg);
        }
    }

    public void paralizeMe(final Creature effector) {
        final Skill revengeSkill = SkillTable.getInstance().getInfo(4515, 1);
        revengeSkill.getEffects(effector, this, false, false);
    }

    public boolean isPendingRevive() {
        return _isPendingRevive;
    }

    public final void setPendingRevive(final boolean value) {
        _isPendingRevive = value;
    }

    public void doRevive() {
        if (!isTeleporting()) {
            setPendingRevive(false);
            setNonAggroTime(System.currentTimeMillis() + Config.NONAGGRO_TIME_ONTELEPORT);
            if (isSalvation()) {
                for (final Effect e : getEffectList().getAllEffects()) {
                    if (e.getEffectType() == EffectType.Salvation) {
                        e.exit();
                        break;
                    }
                }
                setCurrentHp(getMaxHp(), true);
                setCurrentMp(getMaxMp());
                setCurrentCp(getMaxCp());
            } else {
                if (Config.RESPAWN_RESTORE_HP >= 0.0) {
                    setCurrentHp(getMaxHp() * Config.RESPAWN_RESTORE_HP, true);
                }
                if (Config.RESPAWN_RESTORE_MP >= 0.0) {
                    setCurrentMp(getMaxMp() * Config.RESPAWN_RESTORE_MP);
                }
                if (isPlayer() && Config.RESPAWN_RESTORE_CP >= 0.0) {
                    setCurrentCp(getMaxCp() * Config.RESPAWN_RESTORE_CP, true);
                }
            }
            broadcastPacket(new Revive(this));
        } else {
            setPendingRevive(true);
        }
    }

    public abstract void doPickupItem(final GameObject p0);

    public void sitDown(final StaticObjectInstance throne) {
    }

    public void standUp() {
    }

    public long getNonAggroTime() {
        return _nonAggroTime;
    }

    public void setNonAggroTime(final long time) {
        _nonAggroTime = time;
    }

    public boolean startSilentMoving() {
        return _isSilentMoving.getAndSet(true);
    }

    public boolean stopSilentMoving() {
        return _isSilentMoving.setAndGet(false);
    }

    public boolean isSilentMoving() {
        return _isSilentMoving.get();
    }

    public boolean isInCombatZone() {
        return isInZoneBattle();
    }

    public boolean isInPeaceZone() {
        return isInZonePeace();
    }

    @Override
    public boolean isInZoneBattle() {
        return super.isInZoneBattle();
    }

    public boolean isOnSiegeField() {
        return isInZone(ZoneType.SIEGE);
    }

    public boolean isInSSQZone() {
        return isInZone(ZoneType.ssq_zone);
    }

    public boolean isInDangerArea() {
        return isInZone(ZoneType.damage) || isInZone(ZoneType.swamp) || isInZone(ZoneType.poison) || isInZone(ZoneType.instant_skill);
    }

    public int getPvpEventMode() {
        return pvpEventMode;
    }

    public void setPvpEventMode(final int mode) {
        pvpEventMode = mode;
    }

    public int getMaxLoad() {
        return 0;
    }

    public int getInventoryLimit() {
        return 0;
    }

    @Override
    public boolean isPlayable() {
        return true;
    }
}
