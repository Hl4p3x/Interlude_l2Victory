package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.templates.StatsSet;

import java.util.List;

public class EffectsFromSkills extends Skill {
    public EffectsFromSkills(final StatsSet set) {
        super(set);
    }

    @Override
    public void useSkill(final Creature activeChar, final List<Creature> targets) {
        for (final Creature target : targets) {
            if (target != null) {
                for (final AddedSkill as : getAddedSkills()) {
                    as.getSkill().getEffects(activeChar, target, false, false);
                }
            }
        }
    }
}
