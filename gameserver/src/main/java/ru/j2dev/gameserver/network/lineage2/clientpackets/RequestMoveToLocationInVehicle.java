package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.data.BoatHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.boat.Boat;
import ru.j2dev.gameserver.utils.Location;

public class RequestMoveToLocationInVehicle extends L2GameClientPacket {
    private final Location _pos;
    private final Location _originPos;
    private int _boatObjectId;

    public RequestMoveToLocationInVehicle() {
        _pos = new Location();
        _originPos = new Location();
    }

    @Override
    protected void readImpl() {
        _boatObjectId = readD();
        _pos.x = readD();
        _pos.y = readD();
        _pos.z = readD();
        _originPos.x = readD();
        _originPos.y = readD();
        _originPos.z = readD();
    }

    @Override
    protected void runImpl() {
        final Player player = getClient().getActiveChar();
        if (player == null) {
            return;
        }
        final Boat boat = BoatHolder.getInstance().getBoat(_boatObjectId);
        if (boat == null) {
            player.sendActionFailed();
            return;
        }
        boat.moveInBoat(player, _originPos, _pos);
    }
}
