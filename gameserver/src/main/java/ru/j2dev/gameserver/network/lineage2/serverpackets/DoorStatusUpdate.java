package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.DoorInstance;

public class DoorStatusUpdate extends L2GameServerPacket {
    private final int _doorObjId;
    private final boolean _isClosed;
    private final int _dmg;
    private final boolean _isAttakable;
    private final int _doorStaticId;
    private final int _curHp;
    private final int _maxHp;

    public DoorStatusUpdate(final DoorInstance door, final Player player) {
        _doorObjId = door.getObjectId();
        _doorStaticId = door.getDoorId();
        _isClosed = !door.isOpen();
        _isAttakable = door.isAutoAttackable(player);
        _curHp = (int) door.getCurrentHp();
        _maxHp = door.getMaxHp();
        _dmg = door.getDamage();
    }

    @Override
    protected void writeImpl() {
        writeC(0x4d);
        writeD(_doorObjId);
        writeD(_isClosed);
        writeD(_dmg);
        writeD(_isAttakable);
        writeD(_doorStaticId);
        writeD(_maxHp);
        writeD(_curHp);
    }
}
