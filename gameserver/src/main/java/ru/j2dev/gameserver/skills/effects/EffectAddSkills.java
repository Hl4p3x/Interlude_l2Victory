package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.model.Skill.AddedSkill;
import ru.j2dev.gameserver.stats.Env;

public class EffectAddSkills extends Effect {
    public EffectAddSkills(final Env env, final EffectTemplate template) {
        super(env, template);
    }

    @Override
    public void onStart() {
        super.onStart();
        for (final AddedSkill as : getSkill().getAddedSkills()) {
            getEffected().addSkill(as.getSkill());
        }
    }

    @Override
    public void onExit() {
        super.onExit();
        for (final AddedSkill as : getSkill().getAddedSkills()) {
            getEffected().removeSkill(as.getSkill());
        }
    }

    @Override
    public boolean onActionTime() {
        return false;
    }
}
