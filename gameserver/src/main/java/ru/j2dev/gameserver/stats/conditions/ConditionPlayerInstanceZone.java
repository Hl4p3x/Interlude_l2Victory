package ru.j2dev.gameserver.stats.conditions;

import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.stats.Env;

public class ConditionPlayerInstanceZone extends Condition {
    private final int _id;

    public ConditionPlayerInstanceZone(final int id) {
        _id = id;
    }

    @Override
    protected boolean testImpl(final Env env) {
        final Reflection ref = env.character.getReflection();
        return ref.getInstancedZoneId() == _id;
    }
}
