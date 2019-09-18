package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.stats.Stats;
import ru.j2dev.gameserver.templates.StatsSet;

import java.util.List;

public class ManaHealPercent extends Skill {
    private final boolean _ignoreMpEff;

    public ManaHealPercent(final StatsSet set) {
        super(set);
        _ignoreMpEff = set.getBool("ignoreMpEff", true);
    }

    @Override
    public void useSkill(final Creature activeChar, final List<Creature> targets) {
        for (final Creature target : targets) {
            if (target != null) {
                if (target.isDead()) {
                    continue;
                }
                if (target.isHealBlocked()) {
                    continue;
                }
                getEffects(activeChar, target, getActivateRate() > 0, false);
                final double mp = _power * target.getMaxMp() / 100.0;
                final double newMp = mp * (_ignoreMpEff ? 100.0 : target.calcStat(Stats.MANAHEAL_EFFECTIVNESS, 100.0, activeChar, this)) / 100.0;
                final double addToMp = Math.max(0.0, Math.min(newMp, target.calcStat(Stats.MP_LIMIT, null, null) * target.getMaxMp() / 100.0 - target.getCurrentMp()));
                if (addToMp > 0.0) {
                    target.setCurrentMp(target.getCurrentMp() + addToMp);
                }
                if (!target.isPlayer()) {
                    continue;
                }
                if (activeChar != target) {
                    target.sendPacket(new SystemMessage(1069).addString(activeChar.getName()).addNumber(Math.round(addToMp)));
                } else {
                    activeChar.sendPacket(new SystemMessage(1068).addNumber(Math.round(addToMp)));
                }
            }
        }
        if (isSSPossible()) {
            activeChar.unChargeShots(isMagic());
        }
    }
}
