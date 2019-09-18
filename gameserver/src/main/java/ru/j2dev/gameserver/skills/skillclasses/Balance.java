package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.stats.Stats;
import ru.j2dev.gameserver.templates.StatsSet;

import java.util.List;

public class Balance extends Skill {
    public Balance(final StatsSet set) {
        super(set);
    }

    @Override
    public void useSkill(final Creature activeChar, final List<Creature> targets) {
        double summaryCurrentHp = 0.0;
        int summaryMaximumHp = 0;
        for (final Creature target : targets) {
            if (target != null) {
                if (target.isAlikeDead()) {
                    continue;
                }
                summaryCurrentHp += target.getCurrentHp();
                summaryMaximumHp += target.getMaxHp();
            }
        }
        final double percent = summaryCurrentHp / summaryMaximumHp;
        for (final Creature target2 : targets) {
            if (target2 != null) {
                if (target2.isAlikeDead()) {
                    continue;
                }
                final double hp = target2.getMaxHp() * percent;
                if (hp > target2.getCurrentHp()) {
                    final double limit = target2.calcStat(Stats.HP_LIMIT, null, null) * target2.getMaxHp() / 100.0;
                    if (target2.getCurrentHp() < limit) {
                        target2.setCurrentHp(Math.min(hp, limit), false);
                    }
                } else {
                    target2.setCurrentHp(Math.max(1.01, hp), false);
                }
                getEffects(activeChar, target2, getActivateRate() > 0, false);
            }
        }
        if (isSSPossible()) {
            activeChar.unChargeShots(isMagic());
        }
    }
}
