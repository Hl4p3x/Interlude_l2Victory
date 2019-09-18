package ru.j2dev.gameserver.model.items;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance.ItemLocation;

public class PcWarehouse extends Warehouse {
    public PcWarehouse(final Player owner) {
        super(owner.getObjectId());
    }

    public PcWarehouse(final int ownerId) {
        super(ownerId);
    }

    @Override
    public ItemLocation getItemLocation() {
        return ItemLocation.WAREHOUSE;
    }
}
