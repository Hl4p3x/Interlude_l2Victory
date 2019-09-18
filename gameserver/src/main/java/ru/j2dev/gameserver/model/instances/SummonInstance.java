package ru.j2dev.gameserver.model.instances;

import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.cache.HtmCache;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.Summon;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SetSummonRemainTime;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.templates.item.WeaponTemplate.WeaponType;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

import java.util.concurrent.Future;

public class SummonInstance extends Summon {
    public final int CYCLE = 5000;
    private final int _summonSkillId;
    private final int _itemConsumeIdInTime;
    private final int _itemConsumeCountInTime;
    private final int _itemConsumeDelay;
    private final int _maxLifetime;
    private double _expPenalty;
    private Future<?> _disappearTask;
    private int _consumeCountdown;
    private int _lifetimeCountdown;

    public SummonInstance(final int objectId, final NpcTemplate template, final Player owner, final int lifetime, final int consumeid, final int consumecount, final int consumedelay, final Skill skill) {
        super(objectId, template, owner);
        _expPenalty = 0.0;
        setName(template.name);
        _maxLifetime = lifetime;
        _lifetimeCountdown = lifetime;
        _itemConsumeIdInTime = consumeid;
        _itemConsumeCountInTime = consumecount;
        _itemConsumeDelay = consumedelay;
        _consumeCountdown = consumedelay;
        _summonSkillId = skill.getDisplayId();
        _disappearTask = ThreadPoolManager.getInstance().schedule(new Lifetime(), CYCLE);
    }

    @SuppressWarnings("unchecked")
    @Override
    public HardReference<SummonInstance> getRef() {
        return (HardReference<SummonInstance>) super.getRef();
    }

    @Override
    public final int getLevel() {
        return (getTemplate() != null) ? getTemplate().level : 0;
    }

    @Override
    public int getSummonType() {
        return 1;
    }

    @Override
    public int getCurrentFed() {
        return _lifetimeCountdown;
    }

    @Override
    public int getMaxFed() {
        return _maxLifetime;
    }

    @Override
    public double getExpPenalty() {
        return _expPenalty;
    }

    public void setExpPenalty(final double expPenalty) {
        _expPenalty = expPenalty;
    }

    @Override
    protected void onDeath(final Creature killer) {
        super.onDeath(killer);
        saveEffects();
        if (_disappearTask != null) {
            _disappearTask.cancel(false);
            _disappearTask = null;
        }
    }

    public int getItemConsumeIdInTime() {
        return _itemConsumeIdInTime;
    }

    public int getItemConsumeCountInTime() {
        return _itemConsumeCountInTime;
    }

    public int getItemConsumeDelay() {
        return _itemConsumeDelay;
    }

    protected synchronized void stopDisappear() {
        if (_disappearTask != null) {
            _disappearTask.cancel(false);
            _disappearTask = null;
        }
    }

    @Override
    public void unSummon() {
        stopDisappear();
        super.unSummon();
    }

    @Override
    public void displayGiveDamageMessage(final Creature target, final int damage, final boolean crit, final boolean miss, final boolean shld, final boolean magic) {
        final Player owner = getPlayer();
        if (owner == null) {
            return;
        }
        if (crit) {
            owner.sendPacket(SystemMsg.SUMMONED_MONSTERS_CRITICAL_HIT);
        }
        if (miss) {
            owner.sendPacket(new SystemMessage(43));
        } else if (!target.isInvul()) {
            owner.sendPacket(new SystemMessage(35).addNumber(damage));
        }
    }

    @Override
    public void displayReceiveDamageMessage(final Creature attacker, final int damage) {
        final Player owner = getPlayer();
        owner.sendPacket(new SystemMessage(36).addName(attacker).addNumber((long) damage));
    }

    @Override
    public int getEffectIdentifier() {
        return _summonSkillId;
    }

    @Override
    public boolean isSummon() {
        return true;
    }

    @Override
    public void onAction(final Player player, final boolean shift) {
        super.onAction(player, shift);
        if (shift) {
            if (!player.getPlayerAccess().CanViewChar) {
                return;
            }
            String dialog = HtmCache.getInstance().getNotNull("scripts/actions/admin.L2SummonInstance.onActionShift.htm", player);
            dialog = dialog.replaceFirst("%name%", String.valueOf(getName()));
            dialog = dialog.replaceFirst("%level%", String.valueOf(getLevel()));
            dialog = dialog.replaceFirst("%class%", String.valueOf(getClass().getSimpleName().replaceFirst("L2", "").replaceFirst("Instance", "")));
            dialog = dialog.replaceFirst("%xyz%", getLoc().x + " " + getLoc().y + " " + getLoc().z);
            dialog = dialog.replaceFirst("%heading%", String.valueOf(getLoc().h));
            dialog = dialog.replaceFirst("%owner%", String.valueOf(getPlayer().getName()));
            dialog = dialog.replaceFirst("%ownerId%", String.valueOf(getPlayer().getObjectId()));
            dialog = dialog.replaceFirst("%npcId%", String.valueOf(getNpcId()));
            dialog = dialog.replaceFirst("%expPenalty%", String.valueOf(getExpPenalty()));
            dialog = dialog.replaceFirst("%maxHp%", String.valueOf(getMaxHp()));
            dialog = dialog.replaceFirst("%maxMp%", String.valueOf(getMaxMp()));
            dialog = dialog.replaceFirst("%currHp%", String.valueOf((int) getCurrentHp()));
            dialog = dialog.replaceFirst("%currMp%", String.valueOf((int) getCurrentMp()));
            dialog = dialog.replaceFirst("%pDef%", String.valueOf(getPDef(null)));
            dialog = dialog.replaceFirst("%mDef%", String.valueOf(getMDef(null, null)));
            dialog = dialog.replaceFirst("%pAtk%", String.valueOf(getPAtk(null)));
            dialog = dialog.replaceFirst("%mAtk%", String.valueOf(getMAtk(null, null)));
            dialog = dialog.replaceFirst("%accuracy%", String.valueOf(getAccuracy()));
            dialog = dialog.replaceFirst("%evasionRate%", String.valueOf(getEvasionRate(null)));
            dialog = dialog.replaceFirst("%crt%", String.valueOf(getCriticalHit(null, null)));
            dialog = dialog.replaceFirst("%runSpeed%", String.valueOf(getRunSpeed()));
            dialog = dialog.replaceFirst("%walkSpeed%", String.valueOf(getWalkSpeed()));
            dialog = dialog.replaceFirst("%pAtkSpd%", String.valueOf(getPAtkSpd()));
            dialog = dialog.replaceFirst("%mAtkSpd%", String.valueOf(getMAtkSpd()));
            dialog = dialog.replaceFirst("%dist%", String.valueOf((int) getRealDistance(player)));
            dialog = dialog.replaceFirst("%STR%", String.valueOf(getSTR()));
            dialog = dialog.replaceFirst("%DEX%", String.valueOf(getDEX()));
            dialog = dialog.replaceFirst("%CON%", String.valueOf(getCON()));
            dialog = dialog.replaceFirst("%INT%", String.valueOf(getINT()));
            dialog = dialog.replaceFirst("%WIT%", String.valueOf(getWIT()));
            dialog = dialog.replaceFirst("%MEN%", String.valueOf(getMEN()));
            final NpcHtmlMessage msg = new NpcHtmlMessage(5);
            msg.setHtml(dialog);
            player.sendPacket(msg);
        }
    }

    @Override
    public long getWearedMask() {
        return WeaponType.SWORD.mask();
    }

    class Lifetime extends RunnableImpl {
        @Override
        public void runImpl() {
            final Player owner = getPlayer();
            if (owner == null) {
                _disappearTask = null;
                unSummon();
                return;
            }
            final int usedtime = isInCombat() ? 5000 : 1250;
            _lifetimeCountdown -= usedtime;
            if (_lifetimeCountdown <= 0) {
                owner.sendPacket(Msg.SERVITOR_DISAPPEASR_BECAUSE_THE_SUMMONING_TIME_IS_OVER);
                _disappearTask = null;
                unSummon();
                return;
            }
            _consumeCountdown -= usedtime;
            if (_itemConsumeIdInTime > 0 && _itemConsumeCountInTime > 0 && _consumeCountdown <= 0) {
                if (owner.getInventory().destroyItemByItemId(getItemConsumeIdInTime(), getItemConsumeCountInTime())) {
                    _consumeCountdown = _itemConsumeDelay;
                    owner.sendPacket(new SystemMessage(1029).addItemName(getItemConsumeIdInTime()));
                } else {
                    owner.sendPacket(Msg.SINCE_YOU_DO_NOT_HAVE_ENOUGH_ITEMS_TO_MAINTAIN_THE_SERVITORS_STAY_THE_SERVITOR_WILL_DISAPPEAR);
                    unSummon();
                }
            }
            owner.sendPacket(new SetSummonRemainTime(SummonInstance.this));
            _disappearTask = ThreadPoolManager.getInstance().schedule(this, 5000L);
        }
    }
}
