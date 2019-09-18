package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExCubeGameChangePoints extends L2GameServerPacket {
    final int _timeLeft;
    final int _bluePoints;
    final int _redPoints;

    public ExCubeGameChangePoints(final int timeLeft, final int bluePoints, final int redPoints) {
        _timeLeft = timeLeft;
        _bluePoints = bluePoints;
        _redPoints = redPoints;
    }

    @Override
    protected void writeImpl() {
        writeEx(0x98);
        writeD(2);
        writeD(_timeLeft);
        writeD(_bluePoints);
        writeD(_redPoints);
    }
}
