package ru.j2dev.gameserver.stats.conditions;

import ru.j2dev.gameserver.model.Zone.ZoneType;
import ru.j2dev.gameserver.stats.Env;

public class ConditionZoneType extends Condition {
    private final ZoneType _zoneType;

    public ConditionZoneType(final String zoneType) {
        _zoneType = ZoneType.valueOf(zoneType);
    }

    @Override
    protected boolean testImpl(final Env env) {
        return env.character.isPlayer() && env.character.isInZone(_zoneType);
    }
}
