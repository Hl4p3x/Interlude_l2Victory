package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ShowXMasSeal extends L2GameServerPacket {
    private final int _item;

    public ShowXMasSeal(final int item) {
        _item = item;
    }

    @Override
    protected void writeImpl() {
        writeC(0xf2);
        writeD(_item);
    }
}
