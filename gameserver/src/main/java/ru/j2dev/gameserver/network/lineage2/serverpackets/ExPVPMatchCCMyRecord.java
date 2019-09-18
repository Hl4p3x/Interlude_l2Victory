package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExPVPMatchCCMyRecord extends L2GameServerPacket {
    private final int _points;

    public ExPVPMatchCCMyRecord(final int points) {
        _points = points;
    }

    @Override
    public void writeImpl() {
        writeEx(0x8a);
        writeD(_points);
    }
}
