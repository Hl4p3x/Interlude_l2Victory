package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.boat.Boat;
import ru.j2dev.gameserver.utils.Location;

public class CannotMoveAnymoreInVehicle extends L2GameClientPacket {
    private final Location _loc;
    private int _boatid;

    public CannotMoveAnymoreInVehicle() {
        _loc = new Location();
    }

    @Override
    protected void readImpl() {
        _boatid = readD();
        _loc.x = readD();
        _loc.y = readD();
        _loc.z = readD();
        _loc.h = readD();
    }

    @Override
    protected void runImpl() {
        final Player player = getClient().getActiveChar();
        if (player == null) {
            return;
        }
        final Boat boat = player.getBoat();
        if (boat != null && boat.getObjectId() == _boatid) {
            player.setInBoatPosition(_loc);
            player.setHeading(_loc.h);
            player.broadcastPacket(boat.inStopMovePacket(player));
        }
    }
}
