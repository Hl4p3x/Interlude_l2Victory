package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.stats.Env;

public final class EffectStun extends Effect {
    public EffectStun(final Env env, final EffectTemplate template) {
        super(env, template);
    }

    @Override
    public boolean checkCondition() {
        return Rnd.chance(_template.chance(100));
    }

    @Override
    public void onStart() {
        super.onStart();
        _effected.startStunning();
        _effected.abortAttack(true, true);
        _effected.abortCast(true, true);
        _effected.stopMove();
    }

    @Override
    public void onExit() {
        super.onExit();
        _effected.stopStunning();
    }

    @Override
    public boolean onActionTime() {
        return false;
    }
}
