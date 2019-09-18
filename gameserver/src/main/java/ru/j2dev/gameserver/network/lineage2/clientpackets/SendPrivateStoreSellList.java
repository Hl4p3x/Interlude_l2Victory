package ru.j2dev.gameserver.network.lineage2.clientpackets;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.math.SafeMath;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.items.TradeItem;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.utils.Log;
import ru.j2dev.gameserver.utils.Log.ItemLog;
import ru.j2dev.gameserver.utils.TradeHelper;

import java.util.ArrayList;
import java.util.List;

public class SendPrivateStoreSellList extends L2GameClientPacket {
    private static final Logger LOGGER = LoggerFactory.getLogger(SendPrivateStoreSellList.class);

    private int _buyerId;
    private int _count;
    private int[] _items;
    private long[] _itemQ;
    private long[] _itemP;

    @Override
    protected void readImpl() {
        _buyerId = readD();
        _count = readD();
        if (_count * 20 > _buf.remaining() || _count > 32767 || _count < 1) {
            _count = 0;
            return;
        }
        _items = new int[_count];
        _itemQ = new long[_count];
        _itemP = new long[_count];
        for (int i = 0; i < _count; ++i) {
            _items[i] = readD();
            readD();
            readH();
            readH();
            _itemQ[i] = readD();
            _itemP[i] = readD();
            if (_itemQ[i] < 1L || _itemP[i] < 1L || ArrayUtils.indexOf(_items, _items[i]) < i) {
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
        if (seller.isActionsDisabled()) {
            seller.sendActionFailed();
            return;
        }
        if (seller.isInStoreMode()) {
            seller.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
            return;
        }
        if (seller.isInTrade()) {
            seller.sendActionFailed();
            return;
        }
        if (seller.isFishing()) {
            seller.sendPacket(Msg.YOU_CANNOT_DO_ANYTHING_ELSE_WHILE_FISHING);
            return;
        }
        if (!seller.getPlayerAccess().UseTrade) {
            seller.sendPacket(Msg.THIS_ACCOUNT_CANOT_USE_PRIVATE_STORES);
            return;
        }
        final Player buyer = (Player) seller.getVisibleObject(_buyerId);
        if (buyer == null || buyer.getPrivateStoreType() != 3 || !seller.isInActingRange(buyer)) {
            seller.sendPacket(Msg.THE_ATTEMPT_TO_SELL_HAS_FAILED);
            seller.sendActionFailed();
            return;
        }
        final List<TradeItem> buyList = buyer.getBuyList();
        if (buyList.isEmpty()) {
            seller.sendPacket(Msg.THE_ATTEMPT_TO_SELL_HAS_FAILED);
            seller.sendActionFailed();
            return;
        }
        final List<TradeItem> sellList = new ArrayList<>();
        long totalCost = 0L;
        int slots = 0;
        long weight = 0L;
        buyer.getInventory().writeLock();
        seller.getInventory().writeLock();
        try {
            Label_0469:
            {
                for (int i = 0; i < _count; ++i) {
                    final int objectId = _items[i];
                    final long count = _itemQ[i];
                    final long price = _itemP[i];
                    final ItemInstance item = seller.getInventory().getItemByObjectId(objectId);
                    if (item == null || item.getCount() < count) {
                        break;
                    }
                    if (!item.canBeTraded(seller)) {
                        break;
                    }
                    TradeItem si;
                    for (final TradeItem bi : buyList) {
                        if (bi.getItemId() == item.getItemId() && bi.getOwnersPrice() == price) {
                            if (count > bi.getCount()) {
                                break Label_0469;
                            }
                            totalCost = SafeMath.addAndCheck(totalCost, SafeMath.mulAndCheck(count, price));
                            weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(count, item.getTemplate().getWeight()));
                            if (!item.isStackable() || buyer.getInventory().getItemByItemId(item.getItemId()) == null) {
                                ++slots;
                            }
                            si = new TradeItem();
                            si.setObjectId(objectId);
                            si.setItemId(item.getItemId());
                            si.setCount(count);
                            si.setOwnersPrice(price);
                            sellList.add(si);
                            break;
                        }
                    }
                }
            }
        } catch (ArithmeticException ae) {
            sellList.clear();
            sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
            try {
                if (sellList.size() != _count) {
                    seller.sendPacket(Msg.THE_ATTEMPT_TO_SELL_HAS_FAILED);
                    seller.sendActionFailed();
                    return;
                }
                if (!buyer.getInventory().validateWeight(weight)) {
                    buyer.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
                    seller.sendPacket(Msg.THE_ATTEMPT_TO_SELL_HAS_FAILED);
                    seller.sendActionFailed();
                    return;
                }
                if (!buyer.getInventory().validateCapacity(slots)) {
                    buyer.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
                    seller.sendPacket(Msg.THE_ATTEMPT_TO_SELL_HAS_FAILED);
                    seller.sendActionFailed();
                    return;
                }
                if (!buyer.reduceAdena(totalCost)) {
                    buyer.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
                    seller.sendPacket(Msg.THE_ATTEMPT_TO_SELL_HAS_FAILED);
                    seller.sendActionFailed();
                    return;
                }
                sellList.forEach(si2 -> {
                    final ItemInstance item2 = seller.getInventory().removeItemByObjectId(si2.getObjectId(), si2.getCount());
                    for (final TradeItem bi2 : buyList) {
                        if (bi2.getItemId() == si2.getItemId() && bi2.getOwnersPrice() == si2.getOwnersPrice()) {
                            bi2.setCount(bi2.getCount() - si2.getCount());
                            if (bi2.getCount() < 1L) {
                                buyList.remove(bi2);
                                break;
                            }
                            break;
                        }
                    }
                    Log.LogItem(seller, ItemLog.PrivateStoreSell, item2);
                    Log.LogItem(buyer, ItemLog.PrivateStoreBuy, item2);
                    buyer.getInventory().addItem(item2);
                    TradeHelper.purchaseItem(buyer, seller, si2);
                });
                final long tax = TradeHelper.getTax(seller, totalCost);
                if (tax > 0L) {
                    totalCost -= tax;
                    seller.sendMessage(new CustomMessage("trade.HavePaidTax", seller).addNumber(tax));
                }
                seller.addAdena(totalCost);
                buyer.saveTradeList();
            } finally {
                seller.getInventory().writeUnlock();
                buyer.getInventory().writeUnlock();
            }
            return;
        } finally {
            try {
                if (sellList.size() != _count) {
                    seller.sendPacket(Msg.THE_ATTEMPT_TO_SELL_HAS_FAILED);
                    seller.sendActionFailed();
                    return;
                }
                if (!buyer.getInventory().validateWeight(weight)) {
                    buyer.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
                    seller.sendPacket(Msg.THE_ATTEMPT_TO_SELL_HAS_FAILED);
                    seller.sendActionFailed();
                    return;
                }
                if (!buyer.getInventory().validateCapacity(slots)) {
                    buyer.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
                    seller.sendPacket(Msg.THE_ATTEMPT_TO_SELL_HAS_FAILED);
                    seller.sendActionFailed();
                    return;
                }
                if (!buyer.reduceAdena(totalCost)) {
                    buyer.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
                    seller.sendPacket(Msg.THE_ATTEMPT_TO_SELL_HAS_FAILED);
                    seller.sendActionFailed();
                    return;
                }
                sellList.forEach(si3 -> {
                    final ItemInstance item3 = seller.getInventory().removeItemByObjectId(si3.getObjectId(), si3.getCount());
                    for (final TradeItem bi3 : buyList) {
                        if (bi3.getItemId() == si3.getItemId() && bi3.getOwnersPrice() == si3.getOwnersPrice()) {
                            bi3.setCount(bi3.getCount() - si3.getCount());
                            if (bi3.getCount() < 1L) {
                                buyList.remove(bi3);
                                break;
                            }
                            break;
                        }
                    }
                    Log.LogItem(seller, ItemLog.PrivateStoreSell, item3);
                    Log.LogItem(buyer, ItemLog.PrivateStoreBuy, item3);
                    buyer.getInventory().addItem(item3);
                    TradeHelper.purchaseItem(buyer, seller, si3);
                });
                final long tax2 = TradeHelper.getTax(seller, totalCost);
                if (tax2 > 0L) {
                    totalCost -= tax2;
                    seller.sendMessage(new CustomMessage("trade.HavePaidTax", seller).addNumber(tax2));
                }
                seller.addAdena(totalCost);
                buyer.saveTradeList();
            } finally {
                seller.getInventory().writeUnlock();
                buyer.getInventory().writeUnlock();
            }
        }
        if (buyList.isEmpty()) {
            TradeHelper.cancelStore(buyer);
        }
        seller.sendChanges();
        buyer.sendChanges();
        seller.sendActionFailed();
    }
}
