package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExShowRefineryInterface extends L2GameServerPacket {
    public static final L2GameServerPacket STATIC = new ExShowRefineryInterface();

    @Override
    protected final void writeImpl() {
        writeEx(0x50);
    }
}
