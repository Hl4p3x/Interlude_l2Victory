package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.items.ItemInstance;

public class SpawnItem extends L2GameServerPacket {
    private final int _objectId;
    private final int _itemId;
    private final int _x;
    private final int _y;
    private final int _z;
    private final int _stackable;
    private final long _count;

    public SpawnItem(final ItemInstance item) {
        _objectId = item.getObjectId();
        _itemId = item.getItemId();
        _x = item.getX();
        _y = item.getY();
        _z = item.getZ();
        _stackable = (item.isStackable() ? 1 : 0);
        _count = item.getCount();
    }

    @Override
    protected final void writeImpl() {
        writeC(0xb);
        writeD(_objectId);
        writeD(_itemId);
        writeD(_x);
        writeD(_y);
        writeD(_z + Config.CLIENT_Z_SHIFT);
        writeD(_stackable);
        writeD((int) _count);
        writeD(0);
    }
}
