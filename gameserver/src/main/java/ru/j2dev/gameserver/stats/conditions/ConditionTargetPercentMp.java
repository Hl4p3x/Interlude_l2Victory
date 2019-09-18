package ru.j2dev.gameserver.stats.conditions;

import ru.j2dev.gameserver.stats.Env;

public class ConditionTargetPercentMp extends Condition {
    private final double _mp;

    public ConditionTargetPercentMp(final int mp) {
        _mp = mp / 100.0;
    }

    @Override
    protected boolean testImpl(final Env env) {
        return env.target != null && env.target.getCurrentMpRatio() <= _mp;
    }
}
