package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExAskModifyPartyLooting extends L2GameServerPacket {
    private final String _requestor;
    private final int _mode;

    public ExAskModifyPartyLooting(final String name, final int mode) {
        _requestor = name;
        _mode = mode;
    }

    @Override
    protected void writeImpl() {
        writeEx(0xbf);
        writeS(_requestor);
        writeD(_mode);
    }
}
