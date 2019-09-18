package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExShowVariationCancelWindow extends L2GameServerPacket {
    public static final L2GameServerPacket STATIC = new ExShowVariationCancelWindow();

    @Override
    protected final void writeImpl() {
        writeEx(0x51);
    }
}
