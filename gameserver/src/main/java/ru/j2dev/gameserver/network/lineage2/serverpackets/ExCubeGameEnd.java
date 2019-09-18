package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExCubeGameEnd extends L2GameServerPacket {
    final boolean _isRedTeamWin;

    public ExCubeGameEnd(final boolean isRedTeamWin) {
        _isRedTeamWin = isRedTeamWin;
    }

    @Override
    protected void writeImpl() {
        writeEx(0x98);
        writeD(1);
        writeD(_isRedTeamWin ? 1 : 0);
    }
}
