package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.stats.Env;

public class EffectHPDamPercent extends Effect {
    public EffectHPDamPercent(final Env env, final EffectTemplate template) {
        super(env, template);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (_effected.isDead()) {
            return;
        }
        double newHp = (100.0 - calc()) * _effected.getMaxHp() / 100.0;
        newHp = Math.min(_effected.getCurrentHp(), Math.max(0.0, newHp));
        _effected.setCurrentHp(newHp, false);
    }

    @Override
    public boolean onActionTime() {
        return false;
    }
}
