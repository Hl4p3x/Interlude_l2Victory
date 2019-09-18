package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExSetPartyLooting extends L2GameServerPacket {
    private final int _result;
    private final int _mode;

    public ExSetPartyLooting(final int result, final int mode) {
        _result = result;
        _mode = mode;
    }

    @Override
    protected void writeImpl() {
        writeEx(0xc0);
        writeD(_result);
        writeD(_mode);
    }
}
