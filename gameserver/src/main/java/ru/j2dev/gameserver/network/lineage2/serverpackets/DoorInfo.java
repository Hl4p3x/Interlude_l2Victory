package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.DoorInstance;

public class DoorInfo extends L2GameServerPacket {
    private final boolean _isAttakable;
    private final int _id;
    private final int obj_id;
    private final int unk1;
    private final int _isOpened;
    private final int _curHP;
    private final int _maxHp;
    private final int _isVis;
    private final int _damage;

    public DoorInfo(final DoorInstance door, final Player player) {
        _id = door.getDoorId();
        obj_id = door.getObjectId();
        unk1 = 1;
        _isOpened = (door.isOpen() ? 0 : 1);
        _curHP = (int) door.getCurrentHp();
        _maxHp = door.getMaxHp();
        _isVis = (door.isHPVisible() ? 1 : 0);
        _damage = door.getDamage();
        _isAttakable = door.isAutoAttackable(player);
    }

    @Override
    protected final void writeImpl() {
        writeC(0x4c);
        writeD(obj_id);
        writeD(_id);
        writeD(_isAttakable);
        writeD(unk1);
        writeD(_isOpened);
        writeD(_curHP);
        writeD(_maxHp);
        writeD(_isVis);
        writeD(_damage);
    }
}
