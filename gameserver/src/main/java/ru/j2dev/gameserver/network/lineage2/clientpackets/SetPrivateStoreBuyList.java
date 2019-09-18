package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.commons.math.SafeMath;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.TradeItem;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PrivateStoreManageListBuy;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PrivateStoreMsgBuy;
import ru.j2dev.gameserver.templates.item.ItemTemplate;
import ru.j2dev.gameserver.utils.TradeHelper;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SetPrivateStoreBuyList extends L2GameClientPacket {
    private int _count;
    private int[] _items;
    private long[] _itemQ;
    private long[] _itemP;

    @Override
    protected void readImpl() {
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
            readH();
            readH();
            _itemQ[i] = readD();
            _itemP[i] = readD();
            if (_itemQ[i] < 1L || _itemP[i] < 1L) {
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
        if (!TradeHelper.checksIfCanOpenStore(buyer, 3)) {
            buyer.sendActionFailed();
            return;
        }
        final List<TradeItem> buyList = new CopyOnWriteArrayList<>();
        long totalCost = 0L;
        try {
            Label_0298:
            for (int i = 0; i < _count; ++i) {
                final int itemId = _items[i];
                final long count = _itemQ[i];
                final long price = _itemP[i];
                final ItemTemplate item = ItemTemplateHolder.getInstance().getTemplate(itemId);
                if (item != null) {
                    if (itemId != 57) {
                        if (item.getReferencePrice() / 2 > price) {
                            buyer.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.SetPrivateStoreBuyList.TooLowPrice", buyer).addItemName(item).addNumber(item.getReferencePrice() / 2));
                        } else {
                            if (item.isStackable()) {
                                for (final TradeItem bi : buyList) {
                                    if (bi.getItemId() == itemId) {
                                        bi.setOwnersPrice(price);
                                        bi.setCount(bi.getCount() + count);
                                        totalCost = SafeMath.addAndCheck(totalCost, SafeMath.mulAndCheck(count, price));
                                        continue Label_0298;
                                    }
                                }
                            }
                            final TradeItem bi2 = new TradeItem();
                            bi2.setItemId(itemId);
                            bi2.setCount(count);
                            bi2.setOwnersPrice(price);
                            totalCost = SafeMath.addAndCheck(totalCost, SafeMath.mulAndCheck(count, price));
                            buyList.add(bi2);
                        }
                    }
                }
            }
        } catch (ArithmeticException ae) {
            sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
            return;
        }
        if (buyList.size() > buyer.getTradeLimit()) {
            buyer.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
            buyer.sendPacket(new PrivateStoreManageListBuy(buyer));
            return;
        }
        if (totalCost > buyer.getAdena()) {
            buyer.sendPacket(Msg.THE_PURCHASE_PRICE_IS_HIGHER_THAN_THE_AMOUNT_OF_MONEY_THAT_YOU_HAVE_AND_SO_YOU_CANNOT_OPEN_A_PERSONAL_STORE);
            buyer.sendPacket(new PrivateStoreManageListBuy(buyer));
            return;
        }
        if (!buyList.isEmpty()) {
            buyer.setBuyList(buyList);
            buyer.saveTradeList();
            buyer.setPrivateStoreType(Player.STORE_PRIVATE_BUY);
            buyer.broadcastPacket(new PrivateStoreMsgBuy(buyer));
            buyer.sitDown(null);
            buyer.broadcastCharInfo();
        }
        buyer.sendActionFailed();
    }
}
