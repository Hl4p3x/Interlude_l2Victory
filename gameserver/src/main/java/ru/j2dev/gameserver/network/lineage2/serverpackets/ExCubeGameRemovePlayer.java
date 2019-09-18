package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;

public class ExCubeGameRemovePlayer extends L2GameServerPacket {
    private final int _objectId;
    private final boolean _isRedTeam;

    public ExCubeGameRemovePlayer(final Player player, final boolean isRedTeam) {
        _objectId = player.getObjectId();
        _isRedTeam = isRedTeam;
    }

    @Override
    protected void writeImpl() {
        writeEx(0x97);
        writeD(2);
        writeD(-1);
        writeD(_isRedTeam ? 1 : 0);
        writeD(_objectId);
    }
}
