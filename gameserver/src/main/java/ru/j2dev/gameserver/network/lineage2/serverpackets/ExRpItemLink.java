package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.items.ItemInfo;

public class ExRpItemLink extends L2GameServerPacket {
    private final ItemInfo _item;

    public ExRpItemLink(final ItemInfo item) {
        _item = item;
    }

    @Override
    protected final void writeImpl() {
        writeEx(0x6c);
        writeItemInfo(_item);
    }
}
