package ru.j2dev.gameserver.model.entity.events.actions;

import ru.j2dev.gameserver.model.entity.events.EventAction;
import ru.j2dev.gameserver.model.entity.events.GlobalEvent;

public class StartStopAction implements EventAction {
    public static final String EVENT = "event";
    private final String _name;
    private final boolean _start;

    public StartStopAction(final String name, final boolean start) {
        _name = name;
        _start = start;
    }

    @Override
    public void call(final GlobalEvent event) {
        event.action(_name, _start);
    }
}
