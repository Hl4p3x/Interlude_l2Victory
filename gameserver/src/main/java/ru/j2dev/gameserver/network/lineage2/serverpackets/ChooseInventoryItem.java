package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ChooseInventoryItem extends L2GameServerPacket {
    private final int ItemID;

    public ChooseInventoryItem(final int id) {
        ItemID = id;
    }

    @Override
    protected final void writeImpl() {
        writeC(0x6f);
        writeD(ItemID);
    }
}
