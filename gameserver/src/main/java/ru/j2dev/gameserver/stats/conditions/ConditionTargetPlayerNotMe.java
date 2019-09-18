package ru.j2dev.gameserver.stats.conditions;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.stats.Env;

public class ConditionTargetPlayerNotMe extends Condition {
    private final boolean _flag;

    public ConditionTargetPlayerNotMe(final boolean flag) {
        _flag = flag;
    }

    @Override
    protected boolean testImpl(final Env env) {
        final Creature activeChar = env.character;
        final Creature target = env.target;
        return activeChar != null && activeChar != target == _flag;
    }
}
