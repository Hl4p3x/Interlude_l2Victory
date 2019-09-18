package ru.j2dev.gameserver.templates.mapregion;

import ru.j2dev.gameserver.model.Territory;
import ru.j2dev.gameserver.model.base.Race;

import java.util.Map;

public class RestartArea implements RegionData {
    private final Territory _territory;
    private final Map<Race, RestartPoint> _restarts;

    public RestartArea(final Territory territory, final Map<Race, RestartPoint> restarts) {
        _territory = territory;
        _restarts = restarts;
    }

    @Override
    public Territory getTerritory() {
        return _territory;
    }

    public Map<Race, RestartPoint> getRestartPoint() {
        return _restarts;
    }
}
