package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExMailArrived extends L2GameServerPacket {
    public static final L2GameServerPacket STATIC = new ExMailArrived();

    @Override
    protected final void writeImpl() {
        writeEx(0x2d);
    }
}
