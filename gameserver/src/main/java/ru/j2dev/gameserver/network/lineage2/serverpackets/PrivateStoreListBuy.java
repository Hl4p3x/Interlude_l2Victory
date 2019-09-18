package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.items.TradeItem;

import java.util.ArrayList;
import java.util.List;

public class PrivateStoreListBuy extends L2GameServerPacket {
    private final int _buyerId;
    private final long _adena;
    private final List<TradeItem> _sellList;

    public PrivateStoreListBuy(final Player seller, final Player buyer) {
        _adena = seller.getAdena();
        _buyerId = buyer.getObjectId();
        _sellList = new ArrayList<>();
        final List<TradeItem> buyList = buyer.getBuyList();
        final List<ItemInstance> items = seller.getInventory().getItems();
        for (final TradeItem bi : buyList) {
            TradeItem si = null;
            for (final ItemInstance item : items) {
                if (item.getItemId() == bi.getItemId() && item.canBeTraded(seller)) {
                    si = new TradeItem(item);
                    _sellList.add(si);
                    si.setOwnersPrice(bi.getOwnersPrice());
                    si.setCount(bi.getCount());
                    si.setCurrentValue(Math.min(bi.getCount(), item.getCount()));
                }
            }
            if (si == null) {
                si = new TradeItem();
                si.setItemId(bi.getItemId());
                si.setOwnersPrice(bi.getOwnersPrice());
                si.setCount(bi.getCount());
                si.setCurrentValue(0L);
                _sellList.add(si);
            }
        }
    }

    @Override
    protected final void writeImpl() {
        writeC(0xb8);
        writeD(_buyerId);
        writeD((int) _adena);
        writeD(_sellList.size());
        _sellList.forEach(si -> {
            writeD(si.getObjectId());
            writeD(si.getItemId());
            writeH(si.getEnchantLevel());
            writeD((int) si.getCurrentValue());
            writeD((int) si.getStorePrice());
            writeH(0);
            writeD(si.getBodyPart());
            writeH(si.getItem().getType2ForPackets());
            writeD((int) si.getOwnersPrice());
            writeD((int) si.getCount());
        });
    }
}
