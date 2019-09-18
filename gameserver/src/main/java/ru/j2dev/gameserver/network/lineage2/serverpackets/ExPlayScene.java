package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExPlayScene extends L2GameServerPacket {
    public static final ExPlayScene STATIC = new ExPlayScene();

    @Override
    protected void writeImpl() {
        writeEx(0x5b);
        writeD(0);
    }
}
