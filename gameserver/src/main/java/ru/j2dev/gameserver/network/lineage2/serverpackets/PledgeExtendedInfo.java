package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class PledgeExtendedInfo extends L2GameServerPacket {
    @Override
    protected final void writeImpl() {
        writeC(0x8a);
    }
}
