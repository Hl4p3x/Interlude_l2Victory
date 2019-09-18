package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExRotation extends L2GameServerPacket {
    private final int _charObjId;
    private final int _degree;

    public ExRotation(final int charId, final int degree) {
        _charObjId = charId;
        _degree = degree;
    }

    @Override
    protected void writeImpl() {
        writeEx(0xc1);
        writeD(_charObjId);
        writeD(_degree);
    }
}
