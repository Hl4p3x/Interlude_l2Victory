package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.stats.Env;

public final class EffectImmobilize extends Effect {
    public EffectImmobilize(final Env env, final EffectTemplate template) {
        super(env, template);
    }

    @Override
    public void onStart() {
        super.onStart();
        _effected.startImmobilized();
        _effected.stopMove();
    }

    @Override
    public void onExit() {
        super.onExit();
        _effected.stopImmobilized();
    }

    @Override
    public boolean onActionTime() {
        return false;
    }
}
