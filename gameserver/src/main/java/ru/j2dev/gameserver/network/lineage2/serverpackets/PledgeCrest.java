package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class PledgeCrest extends L2GameServerPacket {
    private final int _crestId;
    private final int _crestSize;
    private final byte[] _data;

    public PledgeCrest(final int crestId, final byte[] data) {
        _crestId = crestId;
        _data = data;
        _crestSize = _data.length;
    }

    @Override
    protected final void writeImpl() {
        writeC(0x6c);
        writeD(_crestId);
        writeD(_crestSize);
        writeB(_data);
    }
}
