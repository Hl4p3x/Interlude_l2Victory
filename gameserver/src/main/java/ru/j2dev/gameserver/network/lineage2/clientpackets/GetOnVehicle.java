package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.data.BoatHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.boat.Boat;
import ru.j2dev.gameserver.utils.Location;

public class GetOnVehicle extends L2GameClientPacket {
    private final Location _loc;
    private int _objectId;

    public GetOnVehicle() {
        _loc = new Location();
    }

    @Override
    protected void readImpl() {
        _objectId = readD();
        _loc.x = readD();
        _loc.y = readD();
        _loc.z = readD();
    }

    @Override
    protected void runImpl() {
        final Player player = getClient().getActiveChar();
        if (player == null) {
            return;
        }
        final Boat boat = BoatHolder.getInstance().getBoat(_objectId);
        if (boat == null) {
            return;
        }
        player._stablePoint = boat.getCurrentWay().getReturnLoc();
        boat.addPlayer(player, _loc);
    }
}
