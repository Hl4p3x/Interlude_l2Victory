package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExRaidReserveResult extends L2GameServerPacket {
    @Override
    protected void writeImpl() {
        writeEx(0xb6);
    }
}
