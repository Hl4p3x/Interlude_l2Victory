package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.stats.Env;

public class EffectInterrupt extends Effect {
    public EffectInterrupt(final Env env, final EffectTemplate template) {
        super(env, template);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!getEffected().isRaid()) {
            getEffected().abortCast(false, true);
        }
    }

    @Override
    public boolean onActionTime() {
        return false;
    }
}
