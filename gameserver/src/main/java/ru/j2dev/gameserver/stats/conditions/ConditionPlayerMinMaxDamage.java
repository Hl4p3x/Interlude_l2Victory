package ru.j2dev.gameserver.stats.conditions;

import ru.j2dev.gameserver.stats.Env;

public class ConditionPlayerMinMaxDamage extends Condition {
    private final double _min;
    private final double _max;

    public ConditionPlayerMinMaxDamage(final double min, final double max) {
        _min = min;
        _max = max;
    }

    @Override
    protected boolean testImpl(final Env env) {
        return (_min <= 0.0 || env.value >= _min) && (_max <= 0.0 || env.value <= _max);
    }
}
