package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;

import java.util.List;

public class GMViewItemList extends L2GameServerPacket {
    private final int _size;
    private final List<ItemInstance> _items;
    private final int _limit;
    private final String _name;

    public GMViewItemList(final Player cha, final List<ItemInstance> items, final int size) {
        _size = size;
        _items = items;
        _name = cha.getName();
        _limit = cha.getInventoryLimit();
    }

    @Override
    protected final void writeImpl() {
        writeC(0x94);
        writeS(_name);
        writeD(_limit);
        writeH(1);
        writeH(_items.size());
        _items.forEach(temp -> {
            writeH(temp.getTemplate().getType1());
            writeD(temp.getObjectId());
            writeD(temp.getItemId());
            writeD((int) temp.getCount());
            writeH(temp.getTemplate().getType2ForPackets());
            writeH(temp.getBlessed());
            writeH(temp.isEquipped() ? 1 : 0);
            writeD(temp.getBodyPart());
            writeH(temp.getEnchantLevel());
            writeH(temp.getTemplate().getType2());
            writeH(temp.getVariationStat1());
            writeH(temp.getVariationStat2());
            writeD(temp.getDuration());
        });
    }
}
