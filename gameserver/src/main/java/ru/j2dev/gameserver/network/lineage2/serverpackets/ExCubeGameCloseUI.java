package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExCubeGameCloseUI extends L2GameServerPacket {
    int _seconds;

    @Override
    protected void writeImpl() {
        writeEx(0x97);
        writeD(-1);
    }
}
