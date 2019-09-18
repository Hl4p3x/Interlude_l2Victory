package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.utils.Location;

public class StopMove extends L2GameServerPacket {
    private final int _objectId;
    private final int _x;
    private final int _y;
    private final int _z;
    private final int _h;

    public StopMove(final Creature cha) {
        _objectId = cha.getObjectId();
        _x = cha.getX();
        _y = cha.getY();
        _z = cha.getZ();
        _h = cha.getHeading();
    }

    public StopMove(final int obj_id, final Location loc) {
        _objectId = obj_id;
        _x = loc.x;
        _y = loc.y;
        _z = loc.z;
        _h = loc.h;
    }

    @Override
    protected final void writeImpl() {
        writeC(0x47);
        writeD(_objectId);
        writeD(_x);
        writeD(_y);
        writeD(_z);
        writeD(_h);
    }
}
