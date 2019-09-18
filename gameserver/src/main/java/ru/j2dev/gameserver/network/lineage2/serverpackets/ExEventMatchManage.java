package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExEventMatchManage extends L2GameServerPacket {
    @Override
    protected void writeImpl() {
        writeEx(0x30);
    }
}
