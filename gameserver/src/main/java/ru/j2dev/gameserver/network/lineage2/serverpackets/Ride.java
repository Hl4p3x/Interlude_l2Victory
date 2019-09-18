package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.utils.Location;

public class Ride extends L2GameServerPacket {
    private final int _mountType;
    private final int _id;
    private final int _rideClassID;
    private final Location _loc;

    public Ride(final Player cha) {
        _id = cha.getObjectId();
        _mountType = cha.getMountType();
        _rideClassID = cha.getMountNpcId() + 1000000;
        _loc = cha.getLoc();
    }

    @Override
    protected final void writeImpl() {
        writeC(0x86);
        writeD(_id);
        writeD((_mountType != 0) ? 1 : 0);
        writeD(_mountType);
        writeD(_rideClassID);
        writeD(_loc.x);
        writeD(_loc.y);
        writeD(_loc.z);
    }
}
