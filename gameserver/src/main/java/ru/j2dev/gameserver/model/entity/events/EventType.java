package ru.j2dev.gameserver.model.entity.events;

public enum EventType {
    MAIN_EVENT,
    SIEGE_EVENT,
    PVP_EVENT,
    BOAT_EVENT,
    FUN_EVENT;

    private final int _step;

    EventType() {
        _step = ordinal() * 1000;
    }

    public int step() {
        return _step;
    }
}
