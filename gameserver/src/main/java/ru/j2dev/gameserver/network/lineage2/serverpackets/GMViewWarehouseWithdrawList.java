package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;

import java.util.List;

public class GMViewWarehouseWithdrawList extends L2GameServerPacket {
    private final List<ItemInstance> _items;
    private final String _charName;
    private final long _charAdena;

    public GMViewWarehouseWithdrawList(final Player cha) {
        _charName = cha.getName();
        _charAdena = cha.getAdena();
        _items = cha.getWarehouse().getItems();
    }

    @Override
    protected final void writeImpl() {
        writeC(0x95);
        writeS(_charName);
        writeD((int) _charAdena);
        writeH(_items.size());
        _items.forEach(temp -> {
            writeH(temp.getTemplate().getType1());
            writeD(temp.getObjectId());
            writeD(temp.getItemId());
            writeD((int) temp.getCount());
            writeH(temp.getTemplate().getType2ForPackets());
            writeH(temp.getBlessed());
            if (temp.getTemplate().getType1() < 4) {
                writeD(temp.getTemplate().getBodyPart());
                writeH(temp.getEnchantLevel());
                writeH(temp.getDamaged());
                writeH(0);
                writeD(temp.getVariationStat1());
                writeD(temp.getVariationStat2());
            }
            writeD(temp.getObjectId());
        });
    }
}
