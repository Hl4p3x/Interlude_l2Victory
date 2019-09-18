package ru.j2dev.gameserver.stats.conditions;

import ru.j2dev.gameserver.model.instances.DoorInstance;
import ru.j2dev.gameserver.stats.Env;

public class ConditionTargetCastleDoor extends Condition {
    private final boolean _isCastleDoor;

    public ConditionTargetCastleDoor(final boolean isCastleDoor) {
        _isCastleDoor = isCastleDoor;
    }

    @Override
    protected boolean testImpl(final Env env) {
        return env.target instanceof DoorInstance == _isCastleDoor;
    }
}
