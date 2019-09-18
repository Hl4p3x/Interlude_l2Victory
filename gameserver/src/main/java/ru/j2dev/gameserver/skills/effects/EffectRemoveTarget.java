package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ai.CtrlIntention;
import ru.j2dev.gameserver.ai.DefaultAI;
import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.stats.Env;

public final class EffectRemoveTarget extends Effect {
    private final boolean _doStopTarget;

    public EffectRemoveTarget(final Env env, final EffectTemplate template) {
        super(env, template);
        _doStopTarget = template.getParam().getBool("doStopTarget", false);
    }

    @Override
    public boolean checkCondition() {
        return Rnd.chance(_template.chance(100));
    }

    @Override
    public void onStart() {
        if (getEffected().getAI() instanceof DefaultAI) {
            ((DefaultAI) getEffected().getAI()).setGlobalAggro(System.currentTimeMillis() + 3000L);
        }
        getEffected().setTarget(null);
        if (_doStopTarget) {
            getEffected().stopMove();
        }
        getEffected().abortAttack(true, true);
        getEffected().abortCast(true, true);
        getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, getEffector());
    }

    @Override
    public boolean isHidden() {
        return true;
    }

    @Override
    public boolean onActionTime() {
        return false;
    }
}
