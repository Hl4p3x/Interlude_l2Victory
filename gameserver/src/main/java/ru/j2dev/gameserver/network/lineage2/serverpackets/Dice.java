package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class Dice extends L2GameServerPacket {
    private final int _playerId;
    private final int _itemId;
    private final int _number;
    private final int _x;
    private final int _y;
    private final int _z;

    public Dice(final int playerId, final int itemId, final int number, final int x, final int y, final int z) {
        _playerId = playerId;
        _itemId = itemId;
        _number = number;
        _x = x;
        _y = y;
        _z = z;
    }

    @Override
    protected final void writeImpl() {
        writeC(0xd4);
        writeD(_playerId);
        writeD(_itemId);
        writeD(_number);
        writeD(_x);
        writeD(_y);
        writeD(_z);
    }
}
