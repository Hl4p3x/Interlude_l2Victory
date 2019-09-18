package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.stats.Env;

public final class EffectBuffImmunity extends Effect {
    public EffectBuffImmunity(final Env env, final EffectTemplate template) {
        super(env, template);
    }

    @Override
    public void onStart() {
        super.onStart();
        getEffected().startBuffImmunity();
    }

    @Override
    public void onExit() {
        super.onExit();
        getEffected().stopBuffImmunity();
    }

    @Override
    public boolean onActionTime() {
        return !_effected.isDead() && getSkill().isToggle();
    }
}
