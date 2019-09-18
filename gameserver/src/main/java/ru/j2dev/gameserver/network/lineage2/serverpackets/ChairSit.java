package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.StaticObjectInstance;

public class ChairSit extends L2GameServerPacket {
    private final int _objectId;
    private final int _staticObjectId;

    public ChairSit(final Player player, final StaticObjectInstance throne) {
        _objectId = player.getObjectId();
        _staticObjectId = throne.getUId();
    }

    @Override
    protected final void writeImpl() {
        writeC(0xe1);
        writeD(_objectId);
        writeD(_staticObjectId);
    }
}
