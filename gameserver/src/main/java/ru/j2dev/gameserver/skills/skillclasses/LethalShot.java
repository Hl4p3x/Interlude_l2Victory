package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.stats.Formulas;
import ru.j2dev.gameserver.stats.Formulas.AttackInfo;
import ru.j2dev.gameserver.templates.StatsSet;

import java.util.List;

public class LethalShot extends Skill {
    public LethalShot(final StatsSet set) {
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
                final boolean reflected = target.checkReflectSkill(activeChar, this);
                final Creature realTarget = reflected ? activeChar : target;
                if (getPower() > 0.0) {
                    final AttackInfo info = Formulas.calcPhysDam(activeChar, realTarget, this, false, false, ss, false);
                    if (info.lethal_dmg > 0.0) {
                        realTarget.reduceCurrentHp(info.lethal_dmg, activeChar, this, true, true, false, false, false, false, false);
                    }
                    realTarget.reduceCurrentHp(info.damage, activeChar, this, true, true, false, true, false, false, true);
                    if (!reflected) {
                        realTarget.doCounterAttack(this, activeChar, false);
                    }
                }
                getEffects(activeChar, target, getActivateRate() > 0, false, reflected);
            }
        }
    }
}
