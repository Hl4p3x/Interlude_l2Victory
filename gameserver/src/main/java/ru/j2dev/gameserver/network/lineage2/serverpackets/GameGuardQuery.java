package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class GameGuardQuery extends L2GameServerPacket {
    @Override
    protected final void writeImpl() {
        writeC(0xf9);
        writeD(0);
        writeD(0);
        writeD(0);
        writeD(0);
    }
}
