package ru.j2dev.gameserver.network.lineage2.clientpackets;

import org.apache.commons.lang3.ArrayUtils;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.items.TradeItem;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PrivateStoreManageListSell;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PrivateStoreMsgSell;
import ru.j2dev.gameserver.utils.TradeHelper;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.IntStream;

public class SetPrivateStoreSellList extends L2GameClientPacket {
    private int _count;
    private boolean _package;
    private int[] _items;
    private long[] _itemQ;
    private long[] _itemP;

    @Override
    protected void readImpl() {
        _package = (readD() == 1);
        _count = readD();
        if (_count * 12 > _buf.remaining() || _count > 32767 || _count < 1) {
            _count = 0;
            return;
        }
        _items = new int[_count];
        _itemQ = new long[_count];
        _itemP = new long[_count];
        for (int i = 0; i < _count; ++i) {
            _items[i] = readD();
            _itemQ[i] = readD();
            _itemP[i] = readD();
            if (_itemQ[i] < 1L || _itemP[i] < 0L || ArrayUtils.indexOf(_items, _items[i]) < i) {
                _count = 0;
                break;
            }
        }
    }

    @Override
    protected void runImpl() {
        final Player seller = getClient().getActiveChar();
        if (seller == null || _count == 0) {
            return;
        }
        if (!TradeHelper.checksIfCanOpenStore(seller, _package ? 8 : 1)) {
            seller.sendActionFailed();
            return;
        }
        final List<TradeItem> sellList = new CopyOnWriteArrayList<>();
        seller.getInventory().writeLock();
        try {
            IntStream.range(0, _count).forEach(i -> {
                final int objectId = _items[i];
                final long count = _itemQ[i];
                final long price = _itemP[i];
                final ItemInstance item = seller.getInventory().getItemByObjectId(objectId);
                if (item != null && item.getCount() >= count && item.canBeTraded(seller)) {
                    if (item.getItemId() != 57) {
                        final TradeItem temp = new TradeItem(item);
                        temp.setCount(count);
                        temp.setOwnersPrice(price);
                        sellList.add(temp);
                    }
                }
            });
        } finally {
            seller.getInventory().writeUnlock();
        }
        if (sellList.size() > seller.getTradeLimit()) {
            seller.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
            seller.sendPacket(new PrivateStoreManageListSell(seller, _package));
            return;
        }
        if (!sellList.isEmpty()) {
            seller.setSellList(_package, sellList);
            seller.saveTradeList();
            seller.setPrivateStoreType(_package ? Player.STORE_PRIVATE_SELL_PACKAGE : Player.STORE_PRIVATE_SELL);
            seller.broadcastPacket(new PrivateStoreMsgSell(seller));
            seller.sitDown(null);
            seller.broadcastCharInfo();
        }
        seller.sendActionFailed();
    }
}
