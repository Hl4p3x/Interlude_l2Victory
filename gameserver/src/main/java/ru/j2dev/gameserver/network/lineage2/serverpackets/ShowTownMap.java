package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ShowTownMap extends L2GameServerPacket {
    final String _texture;
    final int _x;
    final int _y;

    public ShowTownMap(final String texture, final int x, final int y) {
        _texture = texture;
        _x = x;
        _y = y;
    }

    @Override
    protected final void writeImpl() {
        writeC(0xde);
        writeS(_texture);
        writeD(_x);
        writeD(_y);
    }
}
