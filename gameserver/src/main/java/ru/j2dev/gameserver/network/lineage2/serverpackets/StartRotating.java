package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Creature;

public class StartRotating extends L2GameServerPacket {
    private final int _charId;
    private final int _degree;
    private final int _side;
    private final int _speed;

    public StartRotating(final Creature cha, final int degree, final int side, final int speed) {
        _charId = cha.getObjectId();
        _degree = degree;
        _side = side;
        _speed = speed;
    }

    @Override
    protected final void writeImpl() {
        writeC(0x62);
        writeD(_charId);
        writeD(_degree);
        writeD(_side);
        writeD(_speed);
    }
}
