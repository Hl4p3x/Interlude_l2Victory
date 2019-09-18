package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExCubeGameRequestReady extends L2GameServerPacket {
    @Override
    protected void writeImpl() {
        writeEx(0x97);
        writeD(4);
    }
}
