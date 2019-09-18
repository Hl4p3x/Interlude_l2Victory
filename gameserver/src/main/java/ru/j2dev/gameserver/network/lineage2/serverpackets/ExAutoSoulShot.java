package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExAutoSoulShot extends L2GameServerPacket {
    private final int _itemId;
    private final boolean _type;

    public ExAutoSoulShot(final int itemId, final boolean type) {
        _itemId = itemId;
        _type = type;
    }

    @Override
    protected final void writeImpl() {
        writeEx(0x12);
        writeD(_itemId);
        writeD(_type ? 1 : 0);
    }
}
