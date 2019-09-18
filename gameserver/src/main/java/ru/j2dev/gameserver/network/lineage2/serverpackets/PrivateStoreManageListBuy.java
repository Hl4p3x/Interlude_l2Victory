package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.items.TradeItem;
import ru.j2dev.gameserver.model.items.Warehouse.ItemClassComparator;

import java.util.ArrayList;
import java.util.List;

public class PrivateStoreManageListBuy extends L2GameServerPacket {
    private final int _buyerId;
    private final long _adena;
    private final List<TradeItem> _buyList0;
    private final List<TradeItem> _buyList;

    public PrivateStoreManageListBuy(final Player buyer) {
        _buyerId = buyer.getObjectId();
        _adena = buyer.getAdena();
        _buyList0 = buyer.getBuyList();
        _buyList = new ArrayList<>();
        final List<ItemInstance> items = buyer.getInventory().getItems();
        items.sort(ItemClassComparator.getInstance());
        for (final ItemInstance item : items) {
            if (item.canBeTraded(buyer) && item.getItemId() != 57) {
                final TradeItem bi;
                _buyList.add(bi = new TradeItem(item));
                bi.setObjectId(0);
            }
        }
    }

    @Override
    protected final void writeImpl() {
        writeC(0xb7);
        writeD(_buyerId);
        writeD((int) _adena);
        writeD(_buyList.size());
        _buyList.forEach(bi -> {
            writeD(bi.getItemId());
            writeH(bi.getEnchantLevel());
            writeD((int) bi.getCount());
            writeD((int) bi.getStorePrice());
            writeH(0);
            writeD(bi.getItem().getBodyPart());
            writeH(bi.getItem().getType2ForPackets());
        });
        writeD(_buyList0.size());
        _buyList0.forEach(bi -> {
            writeD(bi.getItemId());
            writeH(bi.getEnchantLevel());
            writeD((int) bi.getCount());
            writeD((int) bi.getStorePrice());
            writeH(0);
            writeD(bi.getItem().getBodyPart());
            writeH(bi.getItem().getType2ForPackets());
            writeD((int) bi.getOwnersPrice());
            writeD((int) bi.getStorePrice());
        });
    }
}
