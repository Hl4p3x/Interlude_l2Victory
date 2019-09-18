package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.utils.Location;

public class StopMoveToLocationInVehicle extends L2GameServerPacket {
    private final int _boatObjectId;
    private final int _playerObjectId;
    private final int _heading;
    private final Location _loc;

    public StopMoveToLocationInVehicle(final Player player) {
        _boatObjectId = player.getBoat().getObjectId();
        _playerObjectId = player.getObjectId();
        _loc = player.getInBoatPosition();
        _heading = player.getHeading();
    }

    @Override
    protected final void writeImpl() {
        writeC(0x72);
        writeD(_playerObjectId);
        writeD(_boatObjectId);
        writeD(_loc.x);
        writeD(_loc.y);
        writeD(_loc.z);
        writeD(_heading);
    }
}
