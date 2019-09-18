package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;

public class ExCubeGameExtendedChangePoints extends L2GameServerPacket {
    private final int _timeLeft;
    private final int _bluePoints;
    private final int _redPoints;
    private final boolean _isRedTeam;
    private final int _objectId;
    private final int _playerPoints;

    public ExCubeGameExtendedChangePoints(final int timeLeft, final int bluePoints, final int redPoints, final boolean isRedTeam, final Player player, final int playerPoints) {
        _timeLeft = timeLeft;
        _bluePoints = bluePoints;
        _redPoints = redPoints;
        _isRedTeam = isRedTeam;
        _objectId = player.getObjectId();
        _playerPoints = playerPoints;
    }

    @Override
    protected void writeImpl() {
        writeEx(0x98);
        writeD(0);
        writeD(_timeLeft);
        writeD(_bluePoints);
        writeD(_redPoints);
        writeD(_isRedTeam ? 1 : 0);
        writeD(_objectId);
        writeD(_playerPoints);
    }
}
