package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.items.LockType;

import java.util.List;
import java.util.stream.IntStream;

public class ItemList extends L2GameServerPacket {
    private final int _size;
    private final List<ItemInstance> _items;
    private final boolean _showWindow;
    private final LockType _lockType;
    private final int[] _lockItems;

    public ItemList(final int size, final List<ItemInstance> items, final boolean showWindow, final LockType lockType, final int[] lockItems) {
        _size = size;
        _items = items;
        _showWindow = showWindow;
        _lockType = lockType;
        _lockItems = lockItems;
    }

    @Override
    protected final void writeImpl() {
        writeC(0x1b);
        writeH(_showWindow ? 1 : 0);
        writeH(_size);
        _items.forEach(this::writeItemInfo);
        writeH(_lockItems.length);
        if (_lockItems.length > 0) {
            writeC(_lockType.ordinal());
            IntStream.of(_lockItems).forEach(this::writeD);
        }
    }
}
