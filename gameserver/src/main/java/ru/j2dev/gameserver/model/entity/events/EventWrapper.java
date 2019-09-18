package ru.j2dev.gameserver.model.entity.events;

import ru.j2dev.gameserver.taskmanager.actionrunner.ActionWrapper;

public class EventWrapper extends ActionWrapper {
    private final GlobalEvent _event;
    private final int _time;

    public EventWrapper(final String name, final GlobalEvent event, final int time) {
        super(name);
        _event = event;
        _time = time;
    }

    @Override
    public void runImpl0() {
        _event.timeActions(_time);
    }
}
