package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;

public class ExCubeGameAddPlayer extends L2GameServerPacket {
    final boolean _isRedTeam;
    private final int _objectId;
    private final String _name;

    public ExCubeGameAddPlayer(final Player player, final boolean isRedTeam) {
        _objectId = player.getObjectId();
        _name = player.getName();
        _isRedTeam = isRedTeam;
    }

    @Override
    protected void writeImpl() {
        writeEx(0x97);
        writeD(1);
        writeD(-1);
        writeD(_isRedTeam ? 1 : 0);
        writeD(_objectId);
        writeS(_name);
    }
}
