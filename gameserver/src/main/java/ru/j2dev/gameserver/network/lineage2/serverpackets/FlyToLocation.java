package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.utils.Location;

public class FlyToLocation extends L2GameServerPacket {
    private final FlyType _type;
    private final int _chaObjId;
    private final Location _loc;
    private final Location _destLoc;

    public FlyToLocation(final Creature cha, final Location destLoc, final FlyType type) {
        _destLoc = destLoc;
        _type = type;
        _chaObjId = cha.getObjectId();
        _loc = cha.getLoc();
    }

    @Override
    protected void writeImpl() {
        writeC(0xd4);
        writeD(_chaObjId);
        writeD(_destLoc.x);
        writeD(_destLoc.y);
        writeD(_destLoc.z);
        writeD(_loc.x);
        writeD(_loc.y);
        writeD(_loc.z);
        writeD(_type.ordinal());
    }

    public enum FlyType {
        THROW_UP,
        THROW_HORIZONTAL,
        DUMMY,
        CHARGE,
        NONE
    }
}
