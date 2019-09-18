package ru.j2dev.gameserver.network.lineage2.clientpackets;

import org.apache.commons.lang3.ArrayUtils;
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

public class SendPrivateStoreBuyList extends L2GameClientPacket {
    private int _sellerId;
    private int _count;
    private int[] _items;
    private long[] _itemQ;
    private long[] _itemP;

    @Override
    protected void readImpl() {
        _sellerId = readD();
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
            if (_itemQ[i] < 1L || _itemP[i] < 1L || ArrayUtils.indexOf(_items, _items[i]) < i) {
                _count = 0;
                break;
            }
        }
    }

    @Override
    protected void runImpl() {
        final Player buyer = getClient().getActiveChar();
        if (buyer == null || _count == 0) {
            return;
        }
        if (buyer.isActionsDisabled()) {
            buyer.sendActionFailed();
            return;
        }
        if (buyer.isInStoreMode()) {
            buyer.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
            return;
        }
        if (buyer.isInTrade()) {
            buyer.sendActionFailed();
            return;
        }
        if (buyer.isFishing()) {
            buyer.sendPacket(Msg.YOU_CANNOT_DO_ANYTHING_ELSE_WHILE_FISHING);
            return;
        }
        if (!buyer.getPlayerAccess().UseTrade) {
            buyer.sendPacket(Msg.THIS_ACCOUNT_CANOT_USE_PRIVATE_STORES);
            return;
        }
        final Player seller = (Player) buyer.getVisibleObject(_sellerId);
        if (seller == null || (seller.getPrivateStoreType() != 1 && seller.getPrivateStoreType() != 8) || !seller.isInActingRange(buyer)) {
            buyer.sendPacket(Msg.THE_ATTEMPT_TO_TRADE_HAS_FAILED);
            buyer.sendActionFailed();
            return;
        }
        final List<TradeItem> sellList = seller.getSellList();
        if (sellList.isEmpty()) {
            buyer.sendPacket(Msg.THE_ATTEMPT_TO_TRADE_HAS_FAILED);
            buyer.sendActionFailed();
            return;
        }
        final List<TradeItem> buyList = new ArrayList<>();
        long totalCost = 0L;
        int slots = 0;
        long weight = 0L;
        buyer.getInventory().writeLock();
        seller.getInventory().writeLock();
        try {
            Label_0475:
            {
                for (int i = 0; i < _count; ++i) {
                    final int objectId = _items[i];
                    final long count = _itemQ[i];
                    final long price = _itemP[i];
                    TradeItem bi;
                    for (final TradeItem si : sellList) {
                        if (si.getObjectId() == objectId && si.getOwnersPrice() == price) {
                            if (count > si.getCount()) {
                                break Label_0475;
                            }
                            final ItemInstance item = seller.getInventory().getItemByObjectId(objectId);
                            if (item == null || item.getCount() < count) {
                                break Label_0475;
                            }
                            if (!item.canBeTraded(seller)) {
                                break Label_0475;
                            }
                            totalCost = SafeMath.addAndCheck(totalCost, SafeMath.mulAndCheck(count, price));
                            weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(count, item.getTemplate().getWeight()));
                            if (!item.isStackable() || buyer.getInventory().getItemByItemId(item.getItemId()) == null) {
                                ++slots;
                            }
                            bi = new TradeItem();
                            bi.setObjectId(objectId);
                            bi.setItemId(item.getItemId());
                            bi.setCount(count);
                            bi.setOwnersPrice(price);
                            buyList.add(bi);
                            break;
                        }
                    }
                }
            }
        } catch (ArithmeticException ae) {
            buyList.clear();
            sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
            try {
                if (buyList.size() != _count || (seller.getPrivateStoreType() == 8 && buyList.size() != sellList.size())) {
                    buyer.sendPacket(Msg.THE_ATTEMPT_TO_TRADE_HAS_FAILED);
                    buyer.sendActionFailed();
                    return;
                }
                if (!buyer.getInventory().validateWeight(weight)) {
                    buyer.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
                    buyer.sendActionFailed();
                    return;
                }
                if (!buyer.getInventory().validateCapacity(slots)) {
                    buyer.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
                    buyer.sendActionFailed();
                    return;
                }
                if (!buyer.reduceAdena(totalCost)) {
                    buyer.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
                    buyer.sendActionFailed();
                    return;
                }
                buyList.forEach(bi2 -> {
                    final ItemInstance item2 = seller.getInventory().removeItemByObjectId(bi2.getObjectId(), bi2.getCount());
                    for (final TradeItem si2 : sellList) {
                        if (si2.getObjectId() == bi2.getObjectId()) {
                            si2.setCount(si2.getCount() - bi2.getCount());
                            if (si2.getCount() < 1L) {
                                sellList.remove(si2);
                                break;
                            }
                            break;
                        }
                    }
                    Log.LogItem(seller, ItemLog.PrivateStoreSell, item2);
                    Log.LogItem(buyer, ItemLog.PrivateStoreBuy, item2);
                    buyer.getInventory().addItem(item2);
                    TradeHelper.purchaseItem(buyer, seller, bi2);
                });
                final long tax = TradeHelper.getTax(seller, totalCost);
                if (tax > 0L) {
                    totalCost -= tax;
                    seller.sendMessage(new CustomMessage("trade.HavePaidTax", seller).addNumber(tax));
                }
                seller.addAdena(totalCost);
                seller.saveTradeList();
            } finally {
                seller.getInventory().writeUnlock();
                buyer.getInventory().writeUnlock();
            }
            return;
        } finally {
            try {
                if (buyList.size() != _count || (seller.getPrivateStoreType() == 8 && buyList.size() != sellList.size())) {
                    buyer.sendPacket(Msg.THE_ATTEMPT_TO_TRADE_HAS_FAILED);
                    buyer.sendActionFailed();
                    return;
                }
                if (!buyer.getInventory().validateWeight(weight)) {
                    buyer.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
                    buyer.sendActionFailed();
                    return;
                }
                if (!buyer.getInventory().validateCapacity(slots)) {
                    buyer.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
                    buyer.sendActionFailed();
                    return;
                }
                if (!buyer.reduceAdena(totalCost)) {
                    buyer.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
                    buyer.sendActionFailed();
                    return;
                }
                buyList.forEach(bi3 -> {
                    final ItemInstance item3 = seller.getInventory().removeItemByObjectId(bi3.getObjectId(), bi3.getCount());
                    for (final TradeItem si3 : sellList) {
                        if (si3.getObjectId() == bi3.getObjectId()) {
                            si3.setCount(si3.getCount() - bi3.getCount());
                            if (si3.getCount() < 1L) {
                                sellList.remove(si3);
                                break;
                            }
                            break;
                        }
                    }
                    Log.LogItem(seller, ItemLog.PrivateStoreSell, item3);
                    Log.LogItem(buyer, ItemLog.PrivateStoreBuy, item3);
                    buyer.getInventory().addItem(item3);
                    TradeHelper.purchaseItem(buyer, seller, bi3);
                });
                final long tax2 = TradeHelper.getTax(seller, totalCost);
                if (tax2 > 0L) {
                    totalCost -= tax2;
                    seller.sendMessage(new CustomMessage("trade.HavePaidTax", seller).addNumber(tax2));
                }
                seller.addAdena(totalCost);
                seller.saveTradeList();
            } finally {
                seller.getInventory().writeUnlock();
                buyer.getInventory().writeUnlock();
            }
        }
        if (sellList.isEmpty()) {
            TradeHelper.cancelStore(seller);
        }
        seller.sendChanges();
        buyer.sendChanges();
        buyer.sendActionFailed();
    }
}
