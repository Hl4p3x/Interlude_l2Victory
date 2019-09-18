package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.entity.boat.Boat;
import ru.j2dev.gameserver.utils.Location;

public class VehicleDeparture extends L2GameServerPacket {
    private final int _moveSpeed;
    private final int _rotationSpeed;
    private final int _boatObjId;
    private Location _loc;

    public VehicleDeparture(final Boat boat) {
        _boatObjId = boat.getObjectId();
        _moveSpeed = boat.getMoveSpeed();
        _rotationSpeed = boat.getRotationSpeed();
        _loc = boat.getDestination();
        if (_loc == null) {
            _loc = boat.getReturnLoc();
        }
    }

    public VehicleDeparture(final Boat boat, final Location dest) {
        _boatObjId = boat.getObjectId();
        _moveSpeed = boat.getMoveSpeed();
        _rotationSpeed = boat.getRotationSpeed();
        _loc = dest;
    }

    @Override
    protected final void writeImpl() {
        writeC(0x5a);
        writeD(_boatObjId);
        writeD(_moveSpeed);
        writeD(_rotationSpeed);
        writeD(_loc.x);
        writeD(_loc.y);
        writeD(_loc.z);
    }
}
