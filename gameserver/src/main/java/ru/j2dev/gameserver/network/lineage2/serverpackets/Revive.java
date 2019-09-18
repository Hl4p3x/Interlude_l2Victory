package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.GameObject;

public class Revive extends L2GameServerPacket {
    private final int _objectId;

    public Revive(final GameObject obj) {
        _objectId = obj.getObjectId();
    }

    @Override
    protected final void writeImpl() {
        writeC(0x7);
        writeD(_objectId);
    }
}
