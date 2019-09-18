package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExPlayAnimation extends L2GameServerPacket {
    @Override
    protected void writeImpl() {
        writeEx(0x59);
    }
}
