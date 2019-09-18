package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.model.Summon;
import ru.j2dev.gameserver.stats.Env;
import ru.j2dev.gameserver.stats.Stats;
import ru.j2dev.gameserver.stats.funcs.Func;
import ru.j2dev.gameserver.stats.funcs.FuncTemplate;

public class EffectServitorShare extends Effect {
    public EffectServitorShare(final Env env, final EffectTemplate template) {
        super(env, template);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (_effector.isPlayer() && !_effector.isAlikeDead()) {
            final Summon target = _effector.getPet();
            if (target != null && !target.isAlikeDead()) {
                target.addStatFuncs(getShareFuncs());
            }
        }
    }

    @Override
    protected void onExit() {
        if (_effector.isPlayer() && !_effector.isAlikeDead()) {
            final Summon target = _effector.getPet();
            if (target != null && !target.isAlikeDead()) {
                target.removeStatsOwner(this);
            }
        }
        super.onExit();
    }

    @Override
    public Func[] getStatFuncs() {
        return Func.EMPTY_FUNC_ARRAY;
    }

    private Func[] getShareFuncs() {
        final FuncTemplate[] funcTemplates = getTemplate().getAttachedFuncs();
        final Func[] funcs = new Func[funcTemplates.length];
        for (int i = 0; i < funcs.length; ++i) {
            funcs[i] = new FuncShare(funcTemplates[i].getStat(), funcTemplates[i].getOrder(), this, funcTemplates[i].getValue());
        }
        return funcs;
    }

    @Override
    protected boolean onActionTime() {
        return false;
    }

    public class FuncShare extends Func {
        public FuncShare(final Stats stat, final int order, final Object owner, final double value) {
            super(stat, order, owner, value);
        }

        @Override
        public void calc(final Env env) {
            env.value += env.character.getPlayer().calcStat(getStat(), getStat().getInit()) * getValue();
        }
    }
}
