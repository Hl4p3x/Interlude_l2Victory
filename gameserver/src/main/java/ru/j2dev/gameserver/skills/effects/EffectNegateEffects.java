package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.stats.Env;

public class EffectNegateEffects extends Effect {
    public EffectNegateEffects(final Env env, final EffectTemplate template) {
        super(env, template);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onExit() {
        super.onExit();
    }

    @Override
    public boolean onActionTime() {
        for (final Effect e : _effected.getEffectList().getAllEffects()) {
            if (((!"none".equals(e.getStackType()) && (e.getStackType().equals(getStackType()) || e.getStackType().equals(getStackType2()))) || (!"none".equals(e.getStackType2()) && (e.getStackType2().equals(getStackType()) || e.getStackType2().equals(getStackType2())))) && e.getStackOrder() <= getStackOrder()) {
                e.exit();
            }
        }
        return false;
    }
}
