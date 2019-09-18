package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInfo;
import ru.j2dev.gameserver.model.items.ItemInstance;

import java.util.ArrayList;
import java.util.List;

public class ExReplyPostItemList extends L2GameServerPacket {
    private final List<ItemInfo> _itemsList;

    public ExReplyPostItemList(final Player activeChar) {
        _itemsList = new ArrayList<>();
        final List<ItemInstance> items = activeChar.getInventory().getItems();
        items.stream().filter(item -> item.canBeTraded(activeChar)).map(ItemInfo::new).forEach(_itemsList::add);
    }

    @Override
    protected void writeImpl() {
        writeEx(0xb2);
        writeD(_itemsList.size());
        _itemsList.forEach(this::writeItemInfo);
    }
}
