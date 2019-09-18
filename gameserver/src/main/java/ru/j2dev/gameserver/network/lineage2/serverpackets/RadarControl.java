package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.utils.Location;

public class RadarControl extends L2GameServerPacket {
    private final int _x;
    private final int _y;
    private final int _z;
    private final int _type;
    private final int _showRadar;

    public RadarControl(final int showRadar, final int type, final Location loc) {
        this(showRadar, type, loc.x, loc.y, loc.z);
    }

    public RadarControl(final int showRadar, final int type, final int x, final int y, final int z) {
        _showRadar = showRadar;
        _type = type;
        _x = x;
        _y = y;
        _z = z;
    }

    @Override
    protected final void writeImpl() {
        writeC(0xeb);
        writeD(_showRadar);
        writeD(_type);
        writeD(_x);
        writeD(_y);
        writeD(_z);
    }
}
