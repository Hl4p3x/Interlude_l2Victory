package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class RequestNetPing extends L2GameServerPacket {
    private final int timestamp;

    public RequestNetPing(final int timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    protected void writeImpl() {
        writeC(0xd3);
        writeD(timestamp);
    }
}
