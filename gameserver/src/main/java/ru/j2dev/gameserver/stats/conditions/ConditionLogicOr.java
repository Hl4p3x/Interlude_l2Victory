package ru.j2dev.gameserver.stats.conditions;

import ru.j2dev.gameserver.stats.Env;

public class ConditionLogicOr extends Condition {
    private static final Condition[] emptyConditions = Condition.EMPTY_ARRAY;

    public Condition[] _conditions;

    public ConditionLogicOr() {
        _conditions = emptyConditions;
    }

    public void add(final Condition condition) {
        if (condition == null) {
            return;
        }
        final int len = _conditions.length;
        final Condition[] tmp = new Condition[len + 1];
        System.arraycopy(_conditions, 0, tmp, 0, len);
        tmp[len] = condition;
        _conditions = tmp;
    }

    @Override
    protected boolean testImpl(final Env env) {
        for (final Condition c : _conditions) {
            if (c.test(env)) {
                return true;
            }
        }
        return false;
    }
}
