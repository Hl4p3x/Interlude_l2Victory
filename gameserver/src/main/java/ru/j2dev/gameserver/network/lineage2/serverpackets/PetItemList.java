package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.instances.PetInstance;
import ru.j2dev.gameserver.model.items.ItemInstance;

import java.util.List;

public class PetItemList extends L2GameServerPacket {
    private final List<ItemInstance> items;

    public PetItemList(final PetInstance cha) {
        items = cha.getInventory().getItems();
    }

    @Override
    protected final void writeImpl() {
        writeC(0xb2);
        writeH(items.size());
        items.forEach(item -> {
            writeH(item.getTemplate().getType1());
            writeD(item.getObjectId());
            writeD(item.getItemId());
            writeD((int) item.getCount());
            writeH(item.getTemplate().getType2ForPackets());
            writeH(item.getBlessed());
            writeH(item.isEquipped() ? 1 : 0);
            writeD(item.getTemplate().getBodyPart());
            writeH(item.getEnchantLevel());
            writeH(item.getDamaged());
        });
    }
}
