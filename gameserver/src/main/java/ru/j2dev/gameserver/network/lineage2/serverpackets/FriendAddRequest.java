package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class FriendAddRequest extends L2GameServerPacket {
    private final String _requestorName;

    public FriendAddRequest(final String requestorName) {
        _requestorName = requestorName;
    }

    @Override
    protected final void writeImpl() {
        writeC(0x7d);
        writeS(_requestorName);
        writeD(0);
    }
}
