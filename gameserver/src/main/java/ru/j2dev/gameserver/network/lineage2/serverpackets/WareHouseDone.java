package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class WareHouseDone extends L2GameServerPacket {
    @Override
    protected void writeImpl() {
        writeC(0x43);
        writeD(0);
    }
}
