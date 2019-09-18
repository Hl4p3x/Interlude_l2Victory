package ru.j2dev.gameserver.model.entity.events.actions;

import ru.j2dev.gameserver.model.entity.events.EventAction;
import ru.j2dev.gameserver.model.entity.events.GlobalEvent;

public class ActiveDeactiveAction implements EventAction {
    private final boolean _active;
    private final String _name;

    public ActiveDeactiveAction(final boolean active, final String name) {
        _active = active;
        _name = name;
    }

    @Override
    public void call(final GlobalEvent event) {
        event.zoneAction(_name, _active);
    }
}
