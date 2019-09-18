package ru.j2dev.gameserver.model.entity.events.actions;

import ru.j2dev.gameserver.model.entity.events.EventAction;
import ru.j2dev.gameserver.model.entity.events.GlobalEvent;

public class SpawnDespawnAction implements EventAction {
    private final boolean _spawn;
    private final String _name;

    public SpawnDespawnAction(final String name, final boolean spawn) {
        _spawn = spawn;
        _name = name;
    }

    @Override
    public void call(final GlobalEvent event) {
        event.spawnAction(_name, _spawn);
    }
}
