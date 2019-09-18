package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.templates.StatsSet;

import java.util.List;

public class Toggle extends Skill {
    public Toggle(final StatsSet set) {
        super(set);
    }

    @Override
    public void useSkill(final Creature activeChar, final List<Creature> targets) {
        if (activeChar.getEffectList().getEffectsBySkillId(_id) != null) {
            activeChar.getEffectList().stopEffect(_id);
            activeChar.sendActionFailed();
            return;
        }
        getEffects(activeChar, activeChar, getActivateRate() > 0, false);
    }
}
