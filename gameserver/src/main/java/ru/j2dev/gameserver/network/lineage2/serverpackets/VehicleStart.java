package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.entity.boat.Boat;

public class VehicleStart extends L2GameServerPacket {
    private final int _objectId;
    private final int _state;

    public VehicleStart(final Boat boat) {
        _objectId = boat.getObjectId();
        _state = boat.getRunState();
    }

    @Override
    protected void writeImpl() {
        writeC(0xba);
        writeD(_objectId);
        writeD(_state);
    }
}
