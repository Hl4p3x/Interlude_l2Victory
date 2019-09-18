package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExCubeGameChangeTimeToStart extends L2GameServerPacket {
    final int _seconds;

    public ExCubeGameChangeTimeToStart(final int seconds) {
        _seconds = seconds;
    }

    @Override
    protected void writeImpl() {
        writeEx(0x97);
        writeD(3);
        writeD(_seconds);
    }
}
