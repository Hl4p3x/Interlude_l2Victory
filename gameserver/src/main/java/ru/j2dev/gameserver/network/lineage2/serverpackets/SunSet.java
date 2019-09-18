package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class SunSet extends L2GameServerPacket {
    @Override
    protected final void writeImpl() {
        writeC(0x1d);
    }
}
