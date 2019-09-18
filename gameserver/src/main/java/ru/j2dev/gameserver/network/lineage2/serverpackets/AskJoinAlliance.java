package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class AskJoinAlliance extends L2GameServerPacket {
    private final String _requestorName;
    private final String _requestorAllyName;
    private final int _requestorId;

    public AskJoinAlliance(final int requestorId, final String requestorName, final String requestorAllyName) {
        _requestorName = requestorName;
        _requestorAllyName = requestorAllyName;
        _requestorId = requestorId;
    }

    @Override
    protected final void writeImpl() {
        writeC(0xa8);
        writeD(_requestorId);
        writeS(_requestorName);
        writeS("");
        writeS(_requestorAllyName);
    }
}
