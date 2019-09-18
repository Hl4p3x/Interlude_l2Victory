package ru.j2dev.gameserver.stats.conditions;

import ru.j2dev.gameserver.stats.Env;

public class ConditionUsingBlowSkill extends Condition {
    private final boolean _flag;

    public ConditionUsingBlowSkill(final boolean flag) {
        _flag = flag;
    }

    @Override
    protected boolean testImpl(final Env env) {
        if (env.skill == null) {
            return !_flag;
        }
        return env.skill.isBlowSkill() == _flag;
    }
}
