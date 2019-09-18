package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.stats.Stats;
import ru.j2dev.gameserver.templates.StatsSet;

import java.util.List;

public class ManaHeal extends Skill {
    private final boolean _ignoreMpEff;

    public ManaHeal(final StatsSet set) {
        super(set);
        _ignoreMpEff = set.getBool("ignoreMpEff", false);
    }

    @Override
    public void useSkill(final Creature activeChar, final List<Creature> targets) {
        double mp = _power;
        final int sps = isSSPossible() ? activeChar.getChargedSpiritShot() : 0;
        if (sps > 0 && Config.MANAHEAL_SPS_BONUS) {
            mp *= ((sps == 2) ? 1.5 : 1.3);
        }
        for (final Creature target : targets) {
            if (target.isHealBlocked()) {
                continue;
            }
            double newMp = (activeChar == target) ? mp : Math.min(mp * 1.7, mp * (_ignoreMpEff ? 100.0 : target.calcStat(Stats.MANAHEAL_EFFECTIVNESS, 100.0, activeChar, this)) / 100.0);
            if (getMagicLevel() > 0 && activeChar != target) {
                final int diff = target.getLevel() - getMagicLevel();
                if (diff > 5) {
                    if (diff < 20) {
                        newMp = newMp / 100.0 * (100 - diff * 5);
                    } else {
                        newMp = 0.0;
                    }
                }
            }
            if (newMp == 0.0) {
                activeChar.sendPacket(new SystemMessage(1597).addSkillName(_id, getDisplayLevel()));
                getEffects(activeChar, target, getActivateRate() > 0, false);
            } else {
                final double addToMp = Math.max(0.0, Math.min(newMp, target.calcStat(Stats.MP_LIMIT, null, null) * target.getMaxMp() / 100.0 - target.getCurrentMp()));
                if (addToMp > 0.0) {
                    target.setCurrentMp(addToMp + target.getCurrentMp());
                }
                if (target.isPlayer()) {
                    if (activeChar != target) {
                        target.sendPacket(new SystemMessage(1069).addString(activeChar.getName()).addNumber(Math.round(addToMp)));
                    } else {
                        activeChar.sendPacket(new SystemMessage(1068).addNumber(Math.round(addToMp)));
                    }
                }
                getEffects(activeChar, target, getActivateRate() > 0, false);
            }
        }
        if (isSSPossible()) {
            activeChar.unChargeShots(isMagic());
        }
    }
}
