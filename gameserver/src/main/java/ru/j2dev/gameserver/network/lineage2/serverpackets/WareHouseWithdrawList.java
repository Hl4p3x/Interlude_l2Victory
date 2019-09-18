package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInfo;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.items.Warehouse.ItemClassComparator;
import ru.j2dev.gameserver.model.items.Warehouse.WarehouseType;
import ru.j2dev.gameserver.templates.item.ItemTemplate.ItemClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WareHouseWithdrawList extends L2GameServerPacket {
    private final long _adena;
    private final int _type;
    private List<ItemInfo> _itemList;

    public WareHouseWithdrawList(final Player player, final WarehouseType type, final ItemClass clss) {
        _itemList = new ArrayList<>();
        _adena = player.getAdena();
        _type = type.ordinal();
        List<ItemInstance> items;
        switch (type) {
            case PRIVATE: {
                items = player.getWarehouse().getItems(clss);
                break;
            }
            case FREIGHT: {
                items = player.getFreight().getItems(clss);
                break;
            }
            case CLAN:
            case CASTLE: {
                items = player.getClan().getWarehouse().getItems(clss);
                break;
            }
            default: {
                _itemList = Collections.emptyList();
                return;
            }
        }
        _itemList = new ArrayList<>(items.size());
        items.sort(ItemClassComparator.getInstance());
        items.forEach(item -> _itemList.add(new ItemInfo(item)));
    }

    @Override
    protected final void writeImpl() {
        writeC(0x42);
        writeH(_type);
        writeD((int) _adena);
        writeWatehouseItemInfo(_itemList);
    }
}
