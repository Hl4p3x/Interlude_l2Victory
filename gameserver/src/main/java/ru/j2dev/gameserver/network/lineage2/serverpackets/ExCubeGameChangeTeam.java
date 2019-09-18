package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;

public class ExCubeGameChangeTeam extends L2GameServerPacket {
    private final int _objectId;
    private final boolean _fromRedTeam;

    public ExCubeGameChangeTeam(final Player player, final boolean fromRedTeam) {
        _objectId = player.getObjectId();
        _fromRedTeam = fromRedTeam;
    }

    @Override
    protected void writeImpl() {
        writeEx(0x97);
        writeD(5);
        writeD(_objectId);
        writeD(_fromRedTeam ? 1 : 0);
        writeD(_fromRedTeam ? 0 : 1);
    }
}
