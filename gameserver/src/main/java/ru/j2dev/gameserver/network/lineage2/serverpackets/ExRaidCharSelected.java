package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExRaidCharSelected extends L2GameServerPacket {
    @Override
    protected void writeImpl() {
        writeEx(0xb5);
    }
}
