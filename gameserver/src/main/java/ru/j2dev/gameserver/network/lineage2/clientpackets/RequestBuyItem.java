package ru.j2dev.gameserver.network.lineage2.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.math.SafeMath;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.BuyListHolder;
import ru.j2dev.gameserver.data.xml.holder.BuyListHolder.NpcTradeList;
import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.residence.Castle;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.items.TradeItem;

import java.util.ArrayList;
import java.util.List;

public class RequestBuyItem extends L2GameClientPacket {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestBuyItem.class);

    private int _listId;
    private int _count;
    private int[] _items;
    private long[] _itemQ;

    @Override
    protected void readImpl() {
        _listId = readD();
        _count = readD();
        if (_count * 8 > _buf.remaining() || _count > 32767 || _count < 1) {
            _count = 0;
            return;
        }
        _items = new int[_count];
        _itemQ = new long[_count];
        for (int i = 0; i < _count; ++i) {
            _items[i] = readD();
            _itemQ[i] = readD();
            if (_itemQ[i] < 1L) {
                _count = 0;
                break;
            }
        }
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null || _count == 0) {
            return;
        }
        if (activeChar.getBuyListId() != _listId) {
            activeChar.sendActionFailed();
            return;
        }
        if (activeChar.isActionsDisabled()) {
            activeChar.sendActionFailed();
            return;
        }
        if (activeChar.isInStoreMode()) {
            activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
            return;
        }
        if (activeChar.isInTrade()) {
            activeChar.sendActionFailed();
            return;
        }
        if (activeChar.isFishing()) {
            activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
            return;
        }
        if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && activeChar.getKarma() > 0 && !activeChar.isGM()) {
            activeChar.sendActionFailed();
            return;
        }
        final NpcInstance merchant = activeChar.getLastNpc();
        final boolean isValidMerchant = merchant != null && merchant.isMerchantNpc();
        if (!activeChar.isGM() && (merchant == null || !isValidMerchant || !merchant.isInActingRange(activeChar))) {
            activeChar.sendActionFailed();
            return;
        }
        final NpcTradeList list = BuyListHolder.getInstance().getBuyList(_listId);
        if (list == null) {
            activeChar.sendActionFailed();
            return;
        }
        int slots = 0;
        long weight = 0L;
        long totalPrice = 0L;
        long tax;
        double taxRate = 0.0;
        Castle castle = null;
        if (merchant != null) {
            castle = merchant.getCastle(activeChar);
            if (castle != null) {
                taxRate = castle.getTaxRate();
            }
        }
        final List<TradeItem> buyList = new ArrayList<>(_count);
        final List<TradeItem> tradeList = list.getItems();
        try {
            loop:
            for (int i = 0; i < _count; i++) {
                int itemId = _items[i];
                long count = _itemQ[i];
                long price = 0;

                for (TradeItem ti : tradeList) {
                    if (ti.getItemId() == itemId) {
                        if (ti.isCountLimited() && ti.getCurrentValue() < count) {
                            continue loop;
                        }
                        price = ti.getOwnersPrice();
                    }
                }

                if (price == 0 && (!activeChar.isGM() || !activeChar.getPlayerAccess().UseGMShop)) {
                    //TODO audit
                    activeChar.sendActionFailed();
                    return;
                }

                totalPrice = SafeMath.addAndCheck(totalPrice, SafeMath.mulAndCheck(count, price));

                TradeItem ti = new TradeItem();
                ti.setItemId(itemId);
                ti.setCount(count);
                ti.setOwnersPrice(price);

                weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(count, ti.getItem().getWeight()));
                if (!ti.getItem().isStackable() || activeChar.getInventory().getItemByItemId(itemId) == null) {
                    slots++;
                }

                buyList.add(ti);
            }
            tax = (long) (totalPrice * taxRate);
            totalPrice = SafeMath.addAndCheck(totalPrice, tax);
            if (!activeChar.getInventory().validateWeight(weight)) {
                sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
                return;
            }
            if (!activeChar.getInventory().validateCapacity(slots)) {
                sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
                return;
            }
            if (!activeChar.reduceAdena(totalPrice)) {
                activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
                return;
            }
            buyList.forEach(ti3 -> activeChar.getInventory().addItem(ti3.getItemId(), ti3.getCount()));
            list.updateItems(buyList);
            if (castle != null && tax > 0L && castle.getOwnerId() > 0 && activeChar.getReflection() == ReflectionManager.DEFAULT) {
                castle.addToTreasury(tax, true, false);
            }
        } catch (ArithmeticException ae) {
            sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
            return;
        }
        activeChar.sendChanges();
    }
}
