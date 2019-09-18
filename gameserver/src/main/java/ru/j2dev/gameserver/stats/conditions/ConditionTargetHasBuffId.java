package ru.j2dev.gameserver.stats.conditions;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.stats.Env;

import java.util.List;

public final class ConditionTargetHasBuffId extends Condition {
    private final int _id;
    private final int _level;

    public ConditionTargetHasBuffId(final int id, final int level) {
        _id = id;
        _level = level;
    }

    @Override
    protected boolean testImpl(final Env env) {
        final Creature target = env.target;
        if (target == null) {
            return false;
        }
        if (_level == -1) {
            return target.getEffectList().getEffectsBySkillId(_id) != null;
        }
        final List<Effect> el = target.getEffectList().getEffectsBySkillId(_id);
        if (el == null) {
            return false;
        }
        for (final Effect effect : el) {
            if (effect != null && effect.getSkill().getLevel() >= _level) {
                return true;
            }
        }
        return false;
    }
}
