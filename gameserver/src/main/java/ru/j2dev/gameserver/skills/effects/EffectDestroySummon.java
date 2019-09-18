package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.model.Summon;
import ru.j2dev.gameserver.stats.Env;

public final class EffectDestroySummon extends Effect {
    public EffectDestroySummon(final Env env, final EffectTemplate template) {
        super(env, template);
    }

    @Override
    public boolean checkCondition() {
        return _effected.isSummon() && super.checkCondition();
    }

    @Override
    public void onStart() {
        super.onStart();
        ((Summon) _effected).unSummon();
    }

    @Override
    public boolean onActionTime() {
        return false;
    }
}
