package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.utils.Location;

public class ObserverEnd extends L2GameServerPacket {
    private final Location _loc;

    public ObserverEnd(final Location loc) {
        _loc = loc;
    }

    @Override
    protected final void writeImpl() {
        writeC(0xe0);
        writeD(_loc.x);
        writeD(_loc.y);
        writeD(_loc.z);
    }
}
