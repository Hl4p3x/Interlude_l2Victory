package ru.j2dev.gameserver.model.items;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance.ItemLocation;

public class PcFreight extends Warehouse {
    public PcFreight(final Player player) {
        super(player.getObjectId());
    }

    public PcFreight(final int objectId) {
        super(objectId);
    }

    @Override
    public ItemLocation getItemLocation() {
        return ItemLocation.FREIGHT;
    }
}
