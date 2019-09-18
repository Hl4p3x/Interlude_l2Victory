package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.stats.Env;

public final class EffectHealBlock extends Effect {
    public EffectHealBlock(final Env env, final EffectTemplate template) {
        super(env, template);
    }

    @Override
    public boolean checkCondition() {
        return !_effected.isHealBlocked() && super.checkCondition();
    }

    @Override
    public void onStart() {
        super.onStart();
        _effected.startHealBlocked();
    }

    @Override
    public void onExit() {
        super.onExit();
        _effected.stopHealBlocked();
    }

    @Override
    public boolean onActionTime() {
        return false;
    }
}
