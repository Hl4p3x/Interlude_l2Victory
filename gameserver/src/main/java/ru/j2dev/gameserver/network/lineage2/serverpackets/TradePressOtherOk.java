package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class TradePressOtherOk extends L2GameServerPacket {
    public static final L2GameServerPacket STATIC = new TradePressOtherOk();

    @Override
    protected final void writeImpl() {
        writeC(0x7c);
    }
}
