package ru.j2dev.gameserver.stats.conditions;

import ru.j2dev.gameserver.stats.Env;
import ru.j2dev.gameserver.utils.PositionUtils;
import ru.j2dev.gameserver.utils.PositionUtils.TargetDirection;

public class ConditionTargetDirection extends Condition {
    private final TargetDirection _dir;

    public ConditionTargetDirection(final TargetDirection direction) {
        _dir = direction;
    }

    @Override
    protected boolean testImpl(final Env env) {
        return PositionUtils.getDirectionTo(env.target, env.character) == _dir;
    }
}
