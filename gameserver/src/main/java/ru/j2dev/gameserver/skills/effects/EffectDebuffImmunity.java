package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.stats.Env;

public final class EffectDebuffImmunity extends Effect {
    public EffectDebuffImmunity(final Env env, final EffectTemplate template) {
        super(env, template);
    }

    @Override
    public void onStart() {
        super.onStart();
        getEffected().startDebuffImmunity();
    }

    @Override
    public void onExit() {
        super.onExit();
        getEffected().stopDebuffImmunity();
    }

    @Override
    public boolean onActionTime() {
        return false;
    }
}
