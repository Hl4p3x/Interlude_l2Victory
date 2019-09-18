package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.items.ItemInstance;

import java.util.Arrays;

public class ExBR_AgathionEnergyInfo extends L2GameServerPacket {
    private final int _size;
    private ItemInstance[] _itemList;

    public ExBR_AgathionEnergyInfo(final int size, final ItemInstance... item) {
        _itemList = null;
        _itemList = item;
        _size = size;
    }

    @Override
    protected void writeImpl() {
        writeEx(0xde);
        writeD(_size);
        Arrays.stream(_itemList).filter(item -> item.getTemplate().getAgathionEnergy() != 0).forEach(item -> {
            writeD(item.getObjectId());
            writeD(item.getItemId());
            writeD(2097152);
            writeD(item.getAgathionEnergy());
            writeD(item.getTemplate().getAgathionEnergy());
        });
    }
}
