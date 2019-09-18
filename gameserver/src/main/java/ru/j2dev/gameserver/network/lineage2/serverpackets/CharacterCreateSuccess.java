package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class CharacterCreateSuccess extends L2GameServerPacket {
    public static final L2GameServerPacket STATIC = new CharacterCreateSuccess();

    @Override
    protected final void writeImpl() {
        writeC(0x19);
        writeD(0x1);
    }
}
