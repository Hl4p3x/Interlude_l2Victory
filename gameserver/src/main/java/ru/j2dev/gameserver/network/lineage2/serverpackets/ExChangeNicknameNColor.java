package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExChangeNicknameNColor extends L2GameServerPacket {
    private final int _itemObjId;

    public ExChangeNicknameNColor(final int itemObjId) {
        _itemObjId = itemObjId;
    }

    @Override
    protected void writeImpl() {
        writeEx(0x83);
        writeD(_itemObjId);
    }
}
