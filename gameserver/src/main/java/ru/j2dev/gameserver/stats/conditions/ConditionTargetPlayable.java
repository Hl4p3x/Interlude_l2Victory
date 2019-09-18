package ru.j2dev.gameserver.stats.conditions;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.stats.Env;

public class ConditionTargetPlayable extends Condition {
    private final boolean _flag;

    public ConditionTargetPlayable(final boolean flag) {
        _flag = flag;
    }

    @Override
    protected boolean testImpl(final Env env) {
        final Creature target = env.target;
        return target != null && target.isPlayable() == _flag;
    }
}
