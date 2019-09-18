package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.utils.Location;

public class TeleportToLocation extends L2GameServerPacket {
    private final int _targetId;
    private final Location _loc;

    public TeleportToLocation(final GameObject cha, final Location loc) {
        _targetId = cha.getObjectId();
        _loc = loc;
    }

    public TeleportToLocation(final GameObject cha, final int x, final int y, final int z) {
        _targetId = cha.getObjectId();
        _loc = new Location(x, y, z, cha.getHeading());
    }

    @Override
    protected final void writeImpl() {
        writeC(0x28);
        writeD(_targetId);
        writeD(_loc.x);
        writeD(_loc.y);
        writeD(_loc.z + Config.CLIENT_Z_SHIFT);
    }
}
