package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.stats.Env;

public class EffectMPDamPercent extends Effect {
    public EffectMPDamPercent(final Env env, final EffectTemplate template) {
        super(env, template);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (_effected.isDead()) {
            return;
        }
        double newMp = (100.0 - calc()) * _effected.getMaxMp() / 100.0;
        newMp = Math.min(_effected.getCurrentMp(), Math.max(0.0, newMp));
        _effected.setCurrentMp(newMp);
    }

    @Override
    public boolean onActionTime() {
        return false;
    }
}
