package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.templates.StatsSet;

import java.util.List;

public class Disablers extends Skill {
    private final boolean _skillInterrupt;

    public Disablers(final StatsSet set) {
        super(set);
        _skillInterrupt = set.getBool("skillInterrupt", false);
    }

    @Override
    public void useSkill(final Creature activeChar, final List<Creature> targets) {
        for (final Creature target : targets) {
            if (target != null) {
                final boolean reflected = target.checkReflectSkill(activeChar, this);
                final Creature realTarget = reflected ? activeChar : target;
                if (_skillInterrupt) {
                    if (realTarget.getCastingSkill() != null && !realTarget.getCastingSkill().isMagic() && !realTarget.isRaid()) {
                        realTarget.abortCast(false, true);
                    }
                    if (!realTarget.isRaid()) {
                        realTarget.abortAttack(true, true);
                    }
                }
                getEffects(activeChar, target, getActivateRate() > 0, false, reflected);
            }
        }
        if (isSSPossible()) {
            activeChar.unChargeShots(isMagic());
        }
    }
}
