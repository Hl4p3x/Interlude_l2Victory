package ru.j2dev.gameserver.stats.conditions;

import ru.j2dev.gameserver.stats.Env;

public class ConditionFirstEffectSuccess extends Condition {
    final boolean _param;

    public ConditionFirstEffectSuccess(final boolean param) {
        _param = param;
    }

    @Override
    protected boolean testImpl(final Env env) {
        return _param == (env.value == 2.147483647E9);
    }
}
