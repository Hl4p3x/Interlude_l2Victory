package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.stats.Env;

public final class EffectPetrification extends Effect {
    public EffectPetrification(final Env env, final EffectTemplate template) {
        super(env, template);
    }

    @Override
    public boolean checkCondition() {
        return !_effected.isParalyzeImmune() && super.checkCondition();
    }

    @Override
    public void onStart() {
        super.onStart();
        _effected.startParalyzed();
        _effected.startDebuffImmunity();
        _effected.startBuffImmunity();
        _effected.startDamageBlocked();
        _effected.abortAttack(true, true);
        _effected.abortCast(true, true);
    }

    @Override
    public void onExit() {
        super.onExit();
        _effected.stopParalyzed();
        _effected.stopDebuffImmunity();
        _effected.stopBuffImmunity();
        _effected.stopDamageBlocked();
    }

    @Override
    public boolean onActionTime() {
        return false;
    }
}
