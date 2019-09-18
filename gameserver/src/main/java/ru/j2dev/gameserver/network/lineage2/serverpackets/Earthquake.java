package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.utils.Location;

public class Earthquake extends L2GameServerPacket {
    private final Location _loc;
    private final int _intensity;
    private final int _duration;

    public Earthquake(final Location loc, final int intensity, final int duration) {
        _loc = loc;
        _intensity = intensity;
        _duration = duration;
    }

    @Override
    protected final void writeImpl() {
        writeC(0xc4);
        writeD(_loc.x);
        writeD(_loc.y);
        writeD(_loc.z);
        writeD(_intensity);
        writeD(_duration);
        writeD(0);
    }
}
