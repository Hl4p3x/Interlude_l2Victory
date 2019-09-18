package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class LeaveWorld extends L2GameServerPacket {
    public static final L2GameServerPacket STATIC = new LeaveWorld();

    @Override
    protected final void writeImpl() {
        writeC(0x7e);
    }
}
