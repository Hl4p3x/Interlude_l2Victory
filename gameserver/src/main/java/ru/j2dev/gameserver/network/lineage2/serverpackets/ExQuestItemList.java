package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.items.LockType;

import java.util.Arrays;

public class ExQuestItemList extends L2GameServerPacket {
    private final int _size;
    private final ItemInstance[] _items;
    private final LockType _lockType;
    private final int[] _lockItems;

    public ExQuestItemList(final int size, final ItemInstance[] t, final LockType lockType, final int[] lockItems) {
        _size = size;
        _items = t;
        _lockType = lockType;
        _lockItems = lockItems;
    }

    @Override
    protected void writeImpl() {
        writeEx(0x19);
        writeH(_size);
        Arrays.stream(_items).filter(temp -> temp.getTemplate().isQuest()).forEach(this::writeItemInfo);
        writeH(_lockItems.length);
        if (_lockItems.length > 0) {
            writeC(_lockType.ordinal());
            Arrays.stream(_lockItems).forEach(this::writeD);
        }
    }
}
