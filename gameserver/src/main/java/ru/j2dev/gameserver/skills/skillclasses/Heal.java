package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.instances.residences.SiegeFlagInstance;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.stats.Formulas;
import ru.j2dev.gameserver.stats.Stats;
import ru.j2dev.gameserver.templates.StatsSet;

import java.util.List;

public class Heal extends Skill {
    private final boolean _ignoreHpEff;
    private final boolean _staticPower;

    public Heal(final StatsSet set) {
        super(set);
        _ignoreHpEff = set.getBool("ignoreHpEff", false);
        _staticPower = set.getBool("staticPower", isHandler());
    }

    @Override
    public boolean checkCondition(final Creature activeChar, final Creature target, final boolean forceUse, final boolean dontMove, final boolean first) {
        return target != null && !target.isDoor() && !(target instanceof SiegeFlagInstance) && super.checkCondition(activeChar, target, forceUse, dontMove, first);
    }

    @Override
    public void useSkill(final Creature activeChar, final List<Creature> targets) {
        double hp = getPower();
        if (!_staticPower) {
            final int mAtk = activeChar.getMAtk(null, this);
            int mAtkMod = 1;
            int staticBonus = 0;
            if (isSSPossible()) {
                switch (activeChar.getChargedSpiritShot()) {
                    case 2: {
                        mAtkMod = 4;
                        staticBonus = getStaticBonus(mAtk);
                        break;
                    }
                    case 1: {
                        mAtkMod = 2;
                        staticBonus = getStaticBonus(mAtk) / 2;
                        break;
                    }
                }
            }
            hp += Math.sqrt(mAtkMod * mAtk) + staticBonus;
            if (Config.HEAL_CRIT_POSSIBLE && Formulas.calcMCrit(activeChar, null, 4.5)) {
                hp *= 3.0;
            }
        }
        for (final Creature target : targets) {
            if (target != null) {
                if (target.isHealBlocked()) {
                    continue;
                }
                if (target != activeChar) {
                    if (target.isPlayer() && target.isCursedWeaponEquipped()) {
                        continue;
                    }
                    if (activeChar.isPlayer() && activeChar.isCursedWeaponEquipped()) {
                        continue;
                    }
                }
                double addToHp;
                if (_staticPower) {
                    addToHp = _power;
                } else {
                    addToHp = hp;
                    if (!isHandler()) {
                        addToHp += activeChar.calcStat(Stats.HEAL_POWER, activeChar, this);
                        addToHp *= (_ignoreHpEff ? 100.0 : target.calcStat(Stats.HEAL_EFFECTIVNESS, 100.0, activeChar, this)) / 100.0;
                    }
                }
                addToHp = Math.max(0.0, Math.min(addToHp, target.calcStat(Stats.HP_LIMIT, null, null) * target.getMaxHp() / 100.0 - target.getCurrentHp()));
                if (addToHp > 0.0) {
                    target.setCurrentHp(addToHp + target.getCurrentHp(), false);
                }
                if (target.isPlayer()) {
                    if (getId() == 4051) {
                        target.sendPacket(Msg.REJUVENATING_HP);
                    } else if (activeChar == target) {
                        activeChar.sendPacket(new SystemMessage(SystemMsg.S1_HP_HAS_BEEN_RESTORED).addNumber(Math.round(addToHp)));
                    } else {
                        target.sendPacket(new SystemMessage(SystemMsg.S2_HP_HAS_BEEN_RESTORED_BY_C1).addName(activeChar).addNumber(Math.round(addToHp)));
                    }
                }
                getEffects(activeChar, target, getActivateRate() > 0, false);
            }
        }
        if (isSSPossible() && isMagic()) {
            activeChar.unChargeShots(isMagic());
        }
    }

    private int getStaticBonus(int mAtk) {
        final double power = getPower();
        final double bottom = getPower() / 4.0;
        if (mAtk < bottom) {
            return 0;
        }
        final double top = getPower() / 3.1;
        if (mAtk > getPower()) {
            return (int) top;
        }
        mAtk -= (int) bottom;
        return (int) (top * (mAtk / (power - bottom)));
    }
}
