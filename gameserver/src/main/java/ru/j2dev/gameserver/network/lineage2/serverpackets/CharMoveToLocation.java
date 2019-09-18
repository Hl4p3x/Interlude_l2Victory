package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.Log;

public class CharMoveToLocation extends L2GameServerPacket {
    private final int _objectId;
    private final Location _current;
    private int _client_z_shift;
    private Location _destination;

    public CharMoveToLocation(final Creature cha) {
        this(cha, cha.getLoc(), cha.getDestination());
    }

    public CharMoveToLocation(final Creature cha, final Location from, final Location to) {
        _objectId = cha.getObjectId();
        _current = from;
        _destination = to;
        if (!cha.isFlying()) {
            _client_z_shift = Config.CLIENT_Z_SHIFT;
        }
        if (cha.isInWater()) {
            _client_z_shift += Config.CLIENT_Z_SHIFT;
        }
        if (_destination == null) {
            Log.debug("CharMoveToLocation: desc is null, but moving. L2Character: " + cha.getObjectId() + ":" + cha.getName() + "; Loc: " + _current);
            _destination = _current;
        }
    }

    public CharMoveToLocation(final int objectId, final Location from, final Location to) {
        _objectId = objectId;
        _current = from;
        _destination = to;
    }

    @Override
    protected final void writeImpl() {
        writeC(0x1);
        writeD(_objectId);
        writeD(_destination.x);
        writeD(_destination.y);
        writeD(_destination.z + _client_z_shift);
        writeD(_current.x);
        writeD(_current.y);
        writeD(_current.z + _client_z_shift);
    }
}
