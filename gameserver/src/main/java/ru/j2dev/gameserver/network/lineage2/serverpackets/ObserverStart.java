package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.utils.Location;

public class ObserverStart extends L2GameServerPacket {
    private final Location _loc;

    public ObserverStart(final Location loc) {
        _loc = loc;
    }

    @Override
    protected final void writeImpl() {
        writeC(0xdf);
        writeD(_loc.x);
        writeD(_loc.y);
        writeD(_loc.z);
        writeC(0);
        writeC(192);
        writeC(0);
    }
}
