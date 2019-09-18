package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.dao.SummonEffectsDAO;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.idfactory.IdFactory;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.base.Experience;
import ru.j2dev.gameserver.model.entity.events.impl.SiegeEvent;
import ru.j2dev.gameserver.model.instances.MerchantInstance;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.instances.SummonInstance;
import ru.j2dev.gameserver.model.instances.TrapInstance;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.stats.Stats;
import ru.j2dev.gameserver.stats.funcs.FuncAdd;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.taskmanager.tasks.objecttasks.DeleteTask;
import ru.j2dev.gameserver.templates.StatsSet;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.NpcUtils;

import java.util.List;

public class Summon extends Skill {
    private final SummonType _summonType;
    private final double _expPenalty;
    private final int _itemConsumeIdInTime;
    private final int _itemConsumeCountInTime;
    private final int _itemConsumeDelay;
    private final int _lifeTime;
    private final int _minRadius;

    public Summon(final StatsSet set) {
        super(set);
        _summonType = Enum.valueOf(SummonType.class, set.getString("summonType", "PET").toUpperCase());
        _expPenalty = set.getDouble("expPenalty", 0.0);
        _itemConsumeIdInTime = set.getInteger("itemConsumeIdInTime", 0);
        _itemConsumeCountInTime = set.getInteger("itemConsumeCountInTime", 0);
        _itemConsumeDelay = set.getInteger("itemConsumeDelay", 240) * 1000;
        _lifeTime = set.getInteger("lifeTime", 1200) * 1000;
        _minRadius = set.getInteger("minRadius", 0);
    }

    @Override
    public boolean checkCondition(final Creature activeChar, final Creature target, final boolean forceUse, final boolean dontMove, final boolean first) {
        final Player player = activeChar.getPlayer();
        if (player == null) {
            return false;
        }
        switch (_summonType) {
            case TRAP: {
                if (player.isInZonePeace()) {
                    activeChar.sendPacket(Msg.YOU_MAY_NOT_ATTACK_IN_A_PEACEFUL_ZONE);
                    return false;
                }
                break;
            }
            case PET:
            case SIEGE_SUMMON: {
                if (player.isProcessingRequest()) {
                    player.sendPacket(Msg.PETS_AND_SERVITORS_ARE_NOT_AVAILABLE_AT_THIS_TIME);
                    return false;
                }
                if (player.getPet() != null || player.isMounted()) {
                    player.sendPacket(Msg.YOU_ALREADY_HAVE_A_PET);
                    return false;
                }
                break;
            }
            case MERCHANT: {
                if (player.isProcessingRequest()) {
                    player.sendPacket(Msg.PETS_AND_SERVITORS_ARE_NOT_AVAILABLE_AT_THIS_TIME);
                    return false;
                }
                break;
            }
            case AGATHION: {
                if (player.getAgathionId() > 0 && _npcId != 0) {
                    player.sendPacket(SystemMsg.AN_AGATHION_HAS_ALREADY_BEEN_SUMMONED);
                    return false;
                }
            }
            case NPC: {
                if (_minRadius > 0) {
                    for (final NpcInstance npc : World.getAroundNpc(player, _minRadius, 200)) {
                        if (npc != null && npc.getNpcId() == getNpcId()) {
                            player.sendPacket(new SystemMessage(SystemMsg.SINCE_S1_ALREADY_EXISTS_NEARBY_YOU_CANNOT_SUMMON_IT_AGAIN).addName(npc));
                            return false;
                        }
                    }
                    break;
                }
                break;
            }
        }
        return super.checkCondition(activeChar, target, forceUse, dontMove, first);
    }

    @Override
    public void useSkill(final Creature caster, final List<Creature> targets) {
        final Player activeChar = caster.getPlayer();
        switch (_summonType) {
            case AGATHION: {
                activeChar.setAgathion(getNpcId());
                break;
            }
            case TRAP: {
                final Skill trapSkill = getFirstAddedSkill();
                if (activeChar.getTrapsCount() >= 5) {
                    activeChar.destroyFirstTrap();
                }
                final TrapInstance trap = new TrapInstance(IdFactory.getInstance().getNextId(), NpcTemplateHolder.getInstance().getTemplate(getNpcId()), activeChar, trapSkill);
                activeChar.addTrap(trap);
                trap.spawnMe();
                break;
            }
            case PET:
            case SIEGE_SUMMON: {
                Location loc = null;
                if (_targetType == SkillTargetType.TARGET_CORPSE) {
                    for (final Creature target : targets) {
                        if (target != null && target.isDead()) {
                            activeChar.getAI().setAttackTarget(null);
                            loc = target.getLoc();
                            if (target.isNpc()) {
                                ((NpcInstance) target).endDecayTask();
                            } else {
                                if (!target.isSummon()) {
                                    return;
                                }
                                ((SummonInstance) target).endDecayTask();
                            }
                        }
                    }
                }
                if (activeChar.getPet() != null || activeChar.isMounted()) {
                    return;
                }
                final NpcTemplate summonTemplate = NpcTemplateHolder.getInstance().getTemplate(getNpcId());
                final SummonInstance summon = new SummonInstance(IdFactory.getInstance().getNextId(), summonTemplate, activeChar, _lifeTime, _itemConsumeIdInTime, _itemConsumeCountInTime, _itemConsumeDelay, this);
                activeChar.setPet(summon);
                summon.setExpPenalty(_expPenalty);
                summon.setExp(Experience.LEVEL[Math.min(summon.getLevel(), Experience.LEVEL.length - 1)]);
                summon.setHeading(activeChar.getHeading());
                summon.setReflection(activeChar.getReflection());
                summon.spawnMe((loc == null) ? Location.findAroundPosition(activeChar, 50, 70) : loc);
                summon.setRunning();
                summon.setFollowMode(true);
                if (summon.getSkillLevel(4140) > 0) {
                    summon.altUseSkill(SkillTable.getInstance().getInfo(4140, summon.getSkillLevel(4140)), activeChar);
                }
                if ("Shadow".equalsIgnoreCase(summon.getName())) {
                    summon.addStatFunc(new FuncAdd(Stats.ABSORB_DAMAGE_PERCENT, 64, this, 15.0));
                }
                SummonEffectsDAO.getInstance().restoreEffects(summon);
                if (activeChar.isOlyParticipant()) {
                    summon.getEffectList().stopAllEffects();
                }
                summon.setCurrentHpMp(summon.getMaxHp(), summon.getMaxMp(), false);
                if (_summonType == SummonType.SIEGE_SUMMON) {
                    final SiegeEvent siegeEvent = activeChar.getEvent(SiegeEvent.class);
                    siegeEvent.addSiegeSummon(summon);
                    break;
                }
                break;
            }
            case MERCHANT: {
                if (activeChar.getPet() != null || activeChar.isMounted()) {
                    return;
                }
                final NpcTemplate merchantTemplate = NpcTemplateHolder.getInstance().getTemplate(getNpcId());
                final MerchantInstance merchant = new MerchantInstance(IdFactory.getInstance().getNextId(), merchantTemplate);
                merchant.setCurrentHp(merchant.getMaxHp(), false);
                merchant.setCurrentMp(merchant.getMaxMp());
                merchant.setHeading(activeChar.getHeading());
                merchant.setReflection(activeChar.getReflection());
                merchant.spawnMe(activeChar.getLoc());
                ThreadPoolManager.getInstance().schedule(new DeleteTask(merchant), _lifeTime);
                break;
            }
            case NPC: {
                NpcUtils.spawnSingle(getNpcId(), activeChar.getLoc(), activeChar.getReflection(), _lifeTime, activeChar.getName(), null, 0, 0, 0);
                break;
            }
        }
        if (isSSPossible()) {
            caster.unChargeShots(isMagic());
        }
    }

    @Override
    public boolean isOffensive() {
        return _targetType == SkillTargetType.TARGET_CORPSE;
    }

    private enum SummonType {
        PET,
        SIEGE_SUMMON,
        AGATHION,
        TRAP,
        MERCHANT,
        NPC
    }
}
