package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.data.BoatHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.boat.Boat;
import ru.j2dev.gameserver.utils.Location;

public class GetOffVehicle extends L2GameClientPacket {
    private final Location _location;
    private int _objectId;

    public GetOffVehicle() {
        _location = new Location();
    }

    @Override
    protected void readImpl() {
        _objectId = readD();
        _location.x = readD();
        _location.y = readD();
        _location.z = readD();
    }

    @Override
    protected void runImpl() {
        final Player player = getClient().getActiveChar();
        if (player == null) {
            return;
        }
        final Boat boat = BoatHolder.getInstance().getBoat(_objectId);
        if (boat == null || boat.isMoving()) {
            player.sendActionFailed();
            return;
        }
        boat.oustPlayer(player, _location, false);
    }
}
