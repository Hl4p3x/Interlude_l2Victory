package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class CharacterDeleteSuccess extends L2GameServerPacket {
    @Override
    protected final void writeImpl() {
        writeC(0x23);
    }
}
