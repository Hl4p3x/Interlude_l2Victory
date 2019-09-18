package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;

public class DeleteObject extends L2GameServerPacket {
    private final int _objectId;

    public DeleteObject(final GameObject obj) {
        _objectId = obj.getObjectId();
    }

    @Override
    protected final void writeImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null || activeChar.getObjectId() == _objectId) {
            return;
        }
        writeC(0x12);
        writeD(_objectId);
        writeD(1);
    }

    @Override
    public String getType() {
        return super.getType() + " " + GameObjectsStorage.findObject(_objectId) + " (" + _objectId + ")";
    }
}
