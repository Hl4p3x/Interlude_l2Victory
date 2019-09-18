package ru.j2dev.gameserver.stats.conditions;

import ru.j2dev.gameserver.stats.Env;

public final class ConditionHasSkill extends Condition {
    private final Integer _id;
    private final int _level;

    public ConditionHasSkill(final Integer id, final int level) {
        _id = id;
        _level = level;
    }

    @Override
    protected boolean testImpl(final Env env) {
        return env.skill != null && env.character.getSkillLevel(_id) >= _level;
    }
}
