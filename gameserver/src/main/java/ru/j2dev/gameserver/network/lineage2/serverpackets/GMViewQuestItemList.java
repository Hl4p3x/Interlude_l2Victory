package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;

import java.util.Arrays;

public class GMViewQuestItemList extends L2GameServerPacket {
    private final int _size;
    private final ItemInstance[] _items;
    private final int _limit;
    private final String _name;

    public GMViewQuestItemList(final Player player, final ItemInstance[] items, final int size) {
        _items = items;
        _size = size;
        _name = player.getName();
        _limit = Config.QUEST_INVENTORY_MAXIMUM;
    }

    @Override
    protected final void writeImpl() {
        writeC(0x93);
        writeS(_name);
        writeD(_limit);
        writeH(1);
        writeH(_size);
        Arrays.stream(_items).filter(temp -> temp.getTemplate().isQuest()).forEach(this::writeItemInfo);
    }
}
