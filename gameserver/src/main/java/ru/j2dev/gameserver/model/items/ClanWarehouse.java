package ru.j2dev.gameserver.model.items;

import ru.j2dev.gameserver.model.items.ItemInstance.ItemLocation;
import ru.j2dev.gameserver.model.pledge.Clan;

public final class ClanWarehouse extends Warehouse {
    public ClanWarehouse(final Clan clan) {
        super(clan.getClanId());
    }

    @Override
    public ItemLocation getItemLocation() {
        return ItemLocation.CLANWH;
    }
}
