package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExClosePartyRoom extends L2GameServerPacket {
    public static L2GameServerPacket STATIC = new ExClosePartyRoom();

    @Override
    protected void writeImpl() {
        writeEx(0x9);
    }
}
