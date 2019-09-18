package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExMPCCClose extends L2GameServerPacket {
    public static final L2GameServerPacket STATIC = new ExMPCCClose();

    @Override
    protected void writeImpl() {
        writeEx(0x26);
    }
}
