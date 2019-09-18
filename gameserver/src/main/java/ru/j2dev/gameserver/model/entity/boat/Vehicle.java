package ru.j2dev.gameserver.model.entity.boat;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.serverpackets.*;
import ru.j2dev.gameserver.templates.CharTemplate;
import ru.j2dev.gameserver.utils.Location;

public class Vehicle extends Boat {
    public Vehicle(final int objectId, final CharTemplate template) {
        super(objectId, template);
    }

    @Override
    public L2GameServerPacket startPacket() {
        return new VehicleStart(this);
    }

    @Override
    public L2GameServerPacket validateLocationPacket(final Player player) {
        return new ValidateLocationInVehicle(player);
    }

    @Override
    public L2GameServerPacket checkLocationPacket() {
        return new VehicleCheckLocation(this);
    }

    @Override
    public L2GameServerPacket infoPacket() {
        return new VehicleInfo(this);
    }

    @Override
    public L2GameServerPacket movePacket() {
        return new VehicleDeparture(this, getDestination());
    }

    @Override
    public L2GameServerPacket inMovePacket(final Player player, final Location src, final Location desc) {
        return new MoveToLocationInVehicle(player, this, src, desc);
    }

    @Override
    public L2GameServerPacket stopMovePacket() {
        return new StopMove(this);
    }

    @Override
    public L2GameServerPacket inStopMovePacket(final Player player) {
        return new StopMoveToLocationInVehicle(player);
    }

    @Override
    public L2GameServerPacket getOnPacket(final Player player, final Location location) {
        return new GetOnVehicle(player, this, location);
    }

    @Override
    public L2GameServerPacket getOffPacket(final Player player, final Location location) {
        return new GetOffVehicle(player, this, location);
    }

    @Override
    public void oustPlayers() {
    }

    @Override
    public boolean isVehicle() {
        return true;
    }
}
