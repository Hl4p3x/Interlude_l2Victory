package ru.j2dev.gameserver.stats.conditions;

import ru.j2dev.gameserver.stats.Env;

public class ConditionPlayerMinLevel extends Condition {
    private final int _level;

    public ConditionPlayerMinLevel(final int level) {
        _level = level;
    }

    @Override
    protected boolean testImpl(final Env env) {
        return env.character.getLevel() >= _level;
    }
}
