package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.templates.StatsSet;

import java.util.List;

public class CPDam extends Skill {
    public CPDam(final StatsSet set) {
        super(set);
    }

    @Override
    public void useSkill(final Creature activeChar, final List<Creature> targets) {
        final boolean ss = activeChar.getChargedSoulShot() && isSSPossible();
        if (ss) {
            activeChar.unChargeShots(false);
        }
        for (final Creature target : targets) {
            if (target != null) {
                if (target.isDead()) {
                    continue;
                }
                target.doCounterAttack(this, activeChar, false);
                final boolean reflected = target.checkReflectSkill(activeChar, this);
                final Creature realTarget = reflected ? activeChar : target;
                if (realTarget.isCurrentCpZero()) {
                    continue;
                }
                double damage = _power * realTarget.getCurrentCp();
                if (damage < 1.0) {
                    damage = 1.0;
                }
                realTarget.reduceCurrentHp(damage, activeChar, this, true, true, false, true, false, false, true);
                getEffects(activeChar, target, getActivateRate() > 0, false, reflected);
            }
        }
    }
}
