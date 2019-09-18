package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;

import java.util.List;

public class ExCubeGameTeamList extends L2GameServerPacket {
    final List<Player> _bluePlayers;
    final List<Player> _redPlayers;
    final int _roomNumber;

    public ExCubeGameTeamList(final List<Player> redPlayers, final List<Player> bluePlayers, final int roomNumber) {
        _redPlayers = redPlayers;
        _bluePlayers = bluePlayers;
        _roomNumber = roomNumber - 1;
    }

    @Override
    protected void writeImpl() {
        writeEx(151);
        writeD(0);
        writeD(_roomNumber);
        writeD(-1);
        writeD(_bluePlayers.size());
        _bluePlayers.forEach(player -> {
            writeD(player.getObjectId());
            writeS(player.getName());
        });
        writeD(_redPlayers.size());
        _redPlayers.forEach(player -> {
            writeD(player.getObjectId());
            writeS(player.getName());
        });
    }
}
