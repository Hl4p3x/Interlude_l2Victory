package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.stats.Env;

public final class EffectSalvation extends Effect {
    public EffectSalvation(final Env env, final EffectTemplate template) {
        super(env, template);
    }

    @Override
    public boolean checkCondition() {
        return getEffected().isPlayer() && super.checkCondition();
    }

    @Override
    public void onStart() {
        getEffected().setIsSalvation(true);
    }

    @Override
    public void onExit() {
        super.onExit();
        getEffected().setIsSalvation(false);
    }

    @Override
    public boolean onActionTime() {
        return false;
    }
}
