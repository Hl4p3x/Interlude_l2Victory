package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.utils.Location;

public class ExJumpToLocation extends L2GameServerPacket {
    private final int _objectId;
    private final Location _current;
    private final Location _destination;

    public ExJumpToLocation(final int objectId, final Location from, final Location to) {
        _objectId = objectId;
        _current = from;
        _destination = to;
    }

    @Override
    protected final void writeImpl() {
        writeEx(0x88);
        writeD(_objectId);
        writeD(_destination.x);
        writeD(_destination.y);
        writeD(_destination.z);
        writeD(_current.x);
        writeD(_current.y);
        writeD(_current.z);
    }
}
