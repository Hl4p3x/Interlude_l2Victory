package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.entity.boat.Boat;
import ru.j2dev.gameserver.utils.Location;

public class VehicleCheckLocation extends L2GameServerPacket {
    private final int _boatObjectId;
    private final Location _loc;

    public VehicleCheckLocation(final Boat instance) {
        _boatObjectId = instance.getObjectId();
        _loc = instance.getLoc();
    }

    @Override
    protected final void writeImpl() {
        writeC(0x5b);
        writeD(_boatObjectId);
        writeD(_loc.x);
        writeD(_loc.y);
        writeD(_loc.z);
        writeD(_loc.h);
    }
}
