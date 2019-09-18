package ru.j2dev.gameserver.stats.conditions;

import ru.j2dev.gameserver.stats.Env;

public class ConditionUsingSkill extends Condition {
    private final int _id;

    public ConditionUsingSkill(final int id) {
        _id = id;
    }

    @Override
    protected boolean testImpl(final Env env) {
        return env.skill != null && env.skill.getId() == _id;
    }
}
