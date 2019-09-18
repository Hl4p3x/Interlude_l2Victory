package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ShowRadar extends L2GameServerPacket {
    @Override
    protected final void writeImpl() {
        writeC(0xaa);
    }
}
