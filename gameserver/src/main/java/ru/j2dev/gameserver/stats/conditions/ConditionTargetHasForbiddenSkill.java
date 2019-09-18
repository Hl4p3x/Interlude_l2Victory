package ru.j2dev.gameserver.stats.conditions;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.stats.Env;

public final class ConditionTargetHasForbiddenSkill extends Condition {
    private final int _skillId;

    public ConditionTargetHasForbiddenSkill(final int skillId) {
        _skillId = skillId;
    }

    @Override
    protected boolean testImpl(final Env env) {
        final Creature target = env.target;
        return target.isPlayable() && target.getSkillLevel(_skillId) <= 0;
    }
}
