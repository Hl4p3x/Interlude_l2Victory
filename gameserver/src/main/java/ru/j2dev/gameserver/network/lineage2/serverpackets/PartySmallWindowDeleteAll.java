package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class PartySmallWindowDeleteAll extends L2GameServerPacket {
    public static final L2GameServerPacket STATIC = new PartySmallWindowDeleteAll();

    @Override
    protected final void writeImpl() {
        writeC(0x50);
    }
}
