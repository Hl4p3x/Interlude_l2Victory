package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.gameserver.ai.CtrlIntention;
import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.stats.Env;

public class EffectAggression extends Effect {
    public EffectAggression(final Env env, final EffectTemplate template) {
        super(env, template);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (_effected.isPlayer() && _effected != _effector) {
            _effected.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, _effector);
        }
    }

    @Override
    public boolean onActionTime() {
        return false;
    }
}
