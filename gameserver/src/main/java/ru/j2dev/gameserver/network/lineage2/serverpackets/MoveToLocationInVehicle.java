package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.boat.Boat;
import ru.j2dev.gameserver.utils.Location;

public class MoveToLocationInVehicle extends L2GameServerPacket {
    private final int _playerObjectId;
    private final int _boatObjectId;
    private final Location _origin;
    private final Location _destination;

    public MoveToLocationInVehicle(final Player cha, final Boat boat, final Location origin, final Location destination) {
        _playerObjectId = cha.getObjectId();
        _boatObjectId = boat.getObjectId();
        _origin = origin;
        _destination = destination;
    }

    @Override
    protected final void writeImpl() {
        writeC(0x71);
        writeD(_playerObjectId);
        writeD(_boatObjectId);
        writeD(_destination.x);
        writeD(_destination.y);
        writeD(_destination.z);
        writeD(_origin.x);
        writeD(_origin.y);
        writeD(_origin.z);
    }
}
