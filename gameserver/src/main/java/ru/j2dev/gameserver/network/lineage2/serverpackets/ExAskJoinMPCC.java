package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExAskJoinMPCC extends L2GameServerPacket {
    private final String _requestorName;

    public ExAskJoinMPCC(final String requestorName) {
        _requestorName = requestorName;
    }

    @Override
    protected void writeImpl() {
        writeEx(0x27);
        writeS(_requestorName);
    }
}
