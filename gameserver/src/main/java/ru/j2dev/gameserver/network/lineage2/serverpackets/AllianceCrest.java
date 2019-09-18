package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class AllianceCrest extends L2GameServerPacket {
    private final int _crestId;
    private final byte[] _data;

    public AllianceCrest(final int crestId, final byte[] data) {
        _crestId = crestId;
        _data = data;
    }

    @Override
    protected final void writeImpl() {
        writeC(0xae);
        writeD(_crestId);
        writeD(_data.length);
        writeB(_data);
    }
}
