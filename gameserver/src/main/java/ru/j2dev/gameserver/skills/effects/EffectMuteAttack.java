package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.stats.Env;

public class EffectMuteAttack extends Effect {
    public EffectMuteAttack(final Env env, final EffectTemplate template) {
        super(env, template);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!_effected.startAMuted()) {
            _effected.abortCast(true, true);
            _effected.abortAttack(true, true);
        }
    }

    @Override
    public void onExit() {
        super.onExit();
        _effected.stopAMuted();
    }

    @Override
    public boolean onActionTime() {
        return false;
    }
}
