package ru.j2dev.gameserver.network.lineage2.serverpackets;

@Deprecated
public class TradePressOwnOk extends L2GameServerPacket {
    @Override
    protected final void writeImpl() {
        writeC(0x22);
        writeD(0);
    }
}
