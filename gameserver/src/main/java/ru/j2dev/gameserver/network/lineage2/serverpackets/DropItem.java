package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.utils.Location;

public class DropItem extends L2GameServerPacket {
    private final Location _loc;
    private final int _playerId;
    private final int item_obj_id;
    private final int item_id;
    private final int _stackable;
    private final long _count;

    public DropItem(final ItemInstance item, final int playerId) {
        _playerId = playerId;
        item_obj_id = item.getObjectId();
        item_id = item.getItemId();
        _loc = item.getLoc();
        _stackable = (item.isStackable() ? 1 : 0);
        _count = item.getCount();
    }

    public DropItem(final int dropperId, final int itemObjId, final int itemId, final Location loc, final boolean isStackable, final int count) {
        _playerId = dropperId;
        item_obj_id = itemObjId;
        item_id = itemId;
        _loc = loc.clone();
        _stackable = (isStackable ? 1 : 0);
        _count = count;
    }

    @Override
    protected final void writeImpl() {
        writeC(0xc);
        writeD(_playerId);
        writeD(item_obj_id);
        writeD(item_id);
        writeD(_loc.x);
        writeD(_loc.y);
        writeD(_loc.z + Config.CLIENT_Z_SHIFT);
        writeD(_stackable);
        writeD((int) _count);
        writeD(1);
    }
}
