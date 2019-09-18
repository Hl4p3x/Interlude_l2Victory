package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.items.ItemInfo;
import ru.j2dev.gameserver.model.items.ItemInstance;

import java.util.ArrayList;
import java.util.List;

public class PetInventoryUpdate extends L2GameServerPacket {
    public static final int UNCHANGED = 0;
    public static final int ADDED = 1;
    public static final int MODIFIED = 2;
    public static final int REMOVED = 3;
    private final List<ItemInfo> _items;

    public PetInventoryUpdate() {
        _items = new ArrayList<>(1);
    }

    public PetInventoryUpdate addNewItem(final ItemInstance item) {
        addItem(item).setLastChange(ADDED);
        return this;
    }

    public PetInventoryUpdate addModifiedItem(final ItemInstance item) {
        addItem(item).setLastChange(MODIFIED);
        return this;
    }

    public PetInventoryUpdate addRemovedItem(final ItemInstance item) {
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
        writeC(0xb3);
        writeH(_items.size());
        _items.forEach(item -> {
            writeH(item.getLastChange());
            writeH(item.getType1());
            writeD(item.getObjectId());
            writeD(item.getItemId());
            writeD((int) item.getCount());
            writeH(item.getType2());
            writeH(item.getCustomType1());
            writeH(item.isEquipped() ? 1 : 0);
            writeD(item.getItem().getBodyPart());
            writeH(item.getEnchantLevel());
            writeH(item.getCustomType2());
        });
    }
}
