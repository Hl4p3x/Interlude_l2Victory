package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Creature;

public class FinishRotating extends L2GameServerPacket {
    private final int _charId;
    private final int _degree;
    private final int _speed;

    public FinishRotating(final Creature player, final int degree, final int speed) {
        _charId = player.getObjectId();
        _degree = degree;
        _speed = speed;
    }

    @Override
    protected final void writeImpl() {
        writeC(0x63);
        writeD(_charId);
        writeD(_degree);
        writeD(_speed);
        writeD(0);
    }
}
