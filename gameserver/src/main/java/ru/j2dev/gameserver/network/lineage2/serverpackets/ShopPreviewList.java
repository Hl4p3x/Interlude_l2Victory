package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.data.xml.holder.BuyListHolder.NpcTradeList;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInfo;
import ru.j2dev.gameserver.model.items.TradeItem;
import ru.j2dev.gameserver.templates.item.ItemTemplate;

import java.util.ArrayList;
import java.util.List;

public class ShopPreviewList extends L2GameServerPacket {
    private final int _listId;
    private final List<ItemInfo> _itemList;
    private final long _money;

    public ShopPreviewList(final NpcTradeList list, final Player player) {
        _listId = list.getListId();
        _money = player.getAdena();
        final List<TradeItem> tradeList = list.getItems();
        _itemList = new ArrayList<>(tradeList.size());
        list.getItems().stream().filter(item -> item.getItem().isEquipable()).forEach(_itemList::add);
    }

    public static int getWearPrice(final ItemTemplate item) {
        switch (item.getItemGrade()) {
            case D: {
                return 50;
            }
            case C: {
                return 100;
            }
            case B: {
                return 200;
            }
            case A: {
                return 500;
            }
            case S: {
                return 1000;
            }
            default: {
                return 10;
            }
        }
    }

    @Override
    protected final void writeImpl() {
        writeC(0xef);
        writeC(192);
        writeC(19);
        writeC(0);
        writeC(0);
        writeD((int) _money);
        writeD(_listId);
        writeH(_itemList.size());
        _itemList.stream().filter(item -> item.getItem().isEquipable()).forEach(item -> {
            writeD(item.getItemId());
            writeH(item.getItem().getType2ForPackets());
            writeH(item.getItem().isEquipable() ? item.getItem().getBodyPart() : 0);
            writeD(getWearPrice(item.getItem()));
        });
    }
}
