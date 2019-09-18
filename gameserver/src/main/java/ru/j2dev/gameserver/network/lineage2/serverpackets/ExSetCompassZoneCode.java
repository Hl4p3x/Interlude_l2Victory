package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;

public class ExSetCompassZoneCode extends L2GameServerPacket {
    public static final int ZONE_ALTERED = 8;
    public static final int ZONE_ALTERED2 = 9;
    public static final int ZONE_REMINDER = 10;
    public static final int ZONE_SIEGE = 11;
    public static final int ZONE_PEACE = 12;
    public static final int ZONE_SSQ = 13;
    public static final int ZONE_PVP = 14;
    public static final int ZONE_GENERAL_FIELD = 15;
    public static final int ZONE_PVP_FLAG = 16384;
    public static final int ZONE_ALTERED_FLAG = 256;
    public static final int ZONE_SIEGE_FLAG = 2048;
    public static final int ZONE_PEACE_FLAG = 4096;
    public static final int ZONE_SSQ_FLAG = 8192;
    private final int _zone;

    public ExSetCompassZoneCode(final Player player) {
        this(player.getZoneMask());
    }

    public ExSetCompassZoneCode(final int zoneMask) {
        if ((zoneMask & 0x100) == 0x100) {
            _zone = ZONE_ALTERED;
        } else if ((zoneMask & 0x800) == 0x800) {
            _zone = ZONE_SIEGE;
        } else if ((zoneMask & 0x4000) == 0x4000) {
            _zone = ZONE_PVP;
        } else if ((zoneMask & 0x1000) == 0x1000) {
            _zone = ZONE_PEACE;
        } else if ((zoneMask & 0x2000) == 0x2000) {
            _zone = ZONE_SSQ;
        } else {
            _zone = ZONE_GENERAL_FIELD;
        }
    }

    @Override
    protected final void writeImpl() {
        writeEx(0x32);
        writeD(_zone);
    }
}
