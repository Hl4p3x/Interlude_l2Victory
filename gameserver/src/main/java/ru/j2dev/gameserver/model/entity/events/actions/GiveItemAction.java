package ru.j2dev.gameserver.model.entity.events.actions;

import ru.j2dev.gameserver.model.entity.events.EventAction;
import ru.j2dev.gameserver.model.entity.events.GlobalEvent;

public class GiveItemAction implements EventAction {
    private final int _itemId;
    private final long _count;

    public GiveItemAction(final int itemId, final long count) {
        _itemId = itemId;
        _count = count;
    }

    @Override
    public void call(final GlobalEvent event) {
        event.itemObtainPlayers().forEach(player -> event.giveItem(player, _itemId, _count));
    }
}
