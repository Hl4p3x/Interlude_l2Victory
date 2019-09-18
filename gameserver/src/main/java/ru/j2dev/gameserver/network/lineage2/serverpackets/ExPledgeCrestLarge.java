package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExPledgeCrestLarge extends L2GameServerPacket {
    private final int _crestId;
    private final byte[] _data;

    public ExPledgeCrestLarge(final int crestId, final byte[] data) {
        _crestId = crestId;
        _data = data;
    }

    @Override
    protected final void writeImpl() {
        writeEx(0x28);
        writeD(0);
        writeD(_crestId);
        writeD(_data.length);
        writeB(_data);
    }
}
