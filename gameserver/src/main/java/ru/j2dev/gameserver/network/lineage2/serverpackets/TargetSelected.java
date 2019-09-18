package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.utils.Location;

public class TargetSelected extends L2GameServerPacket {
    private final int _objectId;
    private final int _targetId;
    private final Location _loc;

    public TargetSelected(final int objectId, final int targetId, final Location loc) {
        _objectId = objectId;
        _targetId = targetId;
        _loc = loc;
    }

    @Override
    protected final void writeImpl() {
        writeC(0x29);
        writeD(_objectId);
        writeD(_targetId);
        writeD(_loc.x);
        writeD(_loc.y);
        writeD(_loc.z);
    }
}
