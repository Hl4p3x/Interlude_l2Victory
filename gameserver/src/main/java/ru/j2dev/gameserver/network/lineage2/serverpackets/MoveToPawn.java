package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.GameObject;

public class MoveToPawn extends L2GameServerPacket {
    private final int _chaId;
    private final int _targetId;
    private final int _minRange;
    private final int _x;
    private final int _y;
    private final int _z;

    public MoveToPawn(final Creature cha, final GameObject target, final int minRange) {
        _chaId = cha.getObjectId();
        _targetId = target.getObjectId();
        _minRange = minRange;
        _x = cha.getX();
        _y = cha.getY();
        _z = cha.getZ();
    }

    @Override
    protected final void writeImpl() {
        writeC(0x60);
        writeD(_chaId);
        writeD(_targetId);
        writeD(_minRange);
        writeD(_x);
        writeD(_y);
        writeD(_z);
    }
}
