package ru.j2dev.gameserver.model.entity.events.actions;

import ru.j2dev.gameserver.model.entity.events.EventAction;
import ru.j2dev.gameserver.model.entity.events.GlobalEvent;

public class AnnounceAction implements EventAction {
    private final int _id;

    public AnnounceAction(final int id) {
        _id = id;
    }

    @Override
    public void call(final GlobalEvent event) {
        event.announce(_id);
    }
}
