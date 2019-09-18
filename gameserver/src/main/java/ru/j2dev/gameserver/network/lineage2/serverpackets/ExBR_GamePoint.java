package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;

public class ExBR_GamePoint extends L2GameServerPacket {
    private final int _objectId;
    private final long _points;

    public ExBR_GamePoint(final Player player) {
        _objectId = player.getObjectId();
        _points = player.getPremiumPoints();
    }

    @Override
    protected void writeImpl() {
        writeEx(0xd5);
        writeD(_objectId);
        writeQ(_points);
        writeD(0);
    }
}
