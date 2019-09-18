package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.base.Element;
import ru.j2dev.gameserver.model.items.ItemInstance;

import java.util.ArrayList;
import java.util.List;

public class ExShowBaseAttributeCancelWindow extends L2GameServerPacket {
    private final List<ItemInstance> _items;

    public ExShowBaseAttributeCancelWindow(final Player activeChar) {
        _items = new ArrayList<>();
        activeChar.getInventory().getItems().stream()
                .filter(item -> item.getAttributeElement() != Element.NONE && item.canBeEnchanted(true))
                .filter(item -> getAttributeRemovePrice(item) != 0L).forEach(_items::add);
    }

    public static long getAttributeRemovePrice(final ItemInstance item) {
        switch (item.getCrystalType()) {
            case S: {
                return (item.getTemplate().getType2() == 0) ? 50000L : 40000L;
            }
            default: {
                return 0L;
            }
        }
    }

    @Override
    protected final void writeImpl() {
        writeEx(0x74);
        writeD(_items.size());
        _items.forEach(item -> {
            writeD(item.getObjectId());
            writeQ(getAttributeRemovePrice(item));
        });
    }
}
