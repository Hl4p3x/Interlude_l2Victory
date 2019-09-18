package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.items.ItemInfo;
import ru.j2dev.gameserver.model.items.ItemInstance;

import java.util.ArrayList;
import java.util.List;

public class InventoryUpdate extends L2GameServerPacket {
    public static final int UNCHANGED = 0;
    public static final int ADDED = 1;
    public static final int MODIFIED = 2;
    public static final int REMOVED = 3;
    private final List<ItemInfo> _items;

    public InventoryUpdate() {
        _items = new ArrayList<>(1);
    }

    public InventoryUpdate addNewItem(final ItemInstance item) {
        addItem(item).setLastChange(ADDED);
        return this;
    }

    public InventoryUpdate addModifiedItem(final ItemInstance item) {
        addItem(item).setLastChange(MODIFIED);
        return this;
    }

    public InventoryUpdate addRemovedItem(final ItemInstance item) {
        addItem(item).setLastChange(REMOVED);
        return this;
    }

    private ItemInfo addItem(final ItemInstance item) {
        final ItemInfo info;
        _items.add(info = new ItemInfo(item));
        return info;
    }

    @Override
    protected final void writeImpl() {
        writeC(0x27);
        writeH(_items.size());
        _items.forEach(temp -> {
            writeH(temp.getLastChange());
            writeItemInfo(temp);
        });
    }
}
