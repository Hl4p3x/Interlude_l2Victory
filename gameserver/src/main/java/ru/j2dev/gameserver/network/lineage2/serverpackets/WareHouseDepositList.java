package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInfo;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.items.Warehouse.ItemClassComparator;
import ru.j2dev.gameserver.model.items.Warehouse.WarehouseType;

import java.util.ArrayList;
import java.util.List;

public class WareHouseDepositList extends L2GameServerPacket {
    private final int _whtype;
    private final long _adena;
    private final List<ItemInfo> _itemList;

    public WareHouseDepositList(final Player cha, final WarehouseType whtype) {
        _whtype = whtype.ordinal();
        _adena = cha.getAdena();
        final List<ItemInstance> items = cha.getInventory().getItems();
        items.sort(ItemClassComparator.getInstance());
        _itemList = new ArrayList<>(items.size());
        items.stream().filter(item -> item.canBeStored(cha, _whtype == 1)).map(ItemInfo::new).forEach(_itemList::add);
    }

    @Override
    protected final void writeImpl() {
        writeC(0x41);
        writeH(_whtype);
        writeD((int) _adena);
        writeWatehouseItemInfo(_itemList);
    }
}
