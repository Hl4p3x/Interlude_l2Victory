package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.items.ItemInfo;

public class TradeOtherAdd extends L2GameServerPacket {
    private final ItemInfo _temp;
    private final long _amount;

    public TradeOtherAdd(final ItemInfo item, final long amount) {
        _temp = item;
        _amount = amount;
    }

    @Override
    protected final void writeImpl() {
        writeC(0x21);
        writeH(1);
        writeH(0);
        writeD(_temp.getObjectId());
        writeD(_temp.getItemId());
        writeD((int) _amount);
        writeH(_temp.getItem().getType2ForPackets());
        writeH(_temp.getCustomType1());
        writeD(_temp.getItem().getBodyPart());
        writeH(_temp.getEnchantLevel());
        writeH(0);
        writeH(0);
    }
}
