package ru.j2dev.gameserver.network.lineage2.clientpackets;

import org.apache.commons.lang3.ArrayUtils;
import ru.j2dev.commons.math.SafeMath;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SellRefundList;
import ru.j2dev.gameserver.utils.Log;
import ru.j2dev.gameserver.utils.Log.ItemLog;

public class RequestSellItem extends L2GameClientPacket {
    private int _listId;
    private int _count;
    private int[] _items;
    private long[] _itemQ;

    @Override
    protected void readImpl() {
        _listId = readD();
        _count = readD();
        if (_count * 12 > _buf.remaining() || _count > 32767 || _count < 1) {
            _count = 0;
            return;
        }
        _items = new int[_count];
        _itemQ = new long[_count];
        for (int i = 0; i < _count; ++i) {
            _items[i] = readD();
            readD();
            _itemQ[i] = readD();
            if (_itemQ[i] < 1L || ArrayUtils.indexOf(_items, _items[i]) < i) {
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
        if (!activeChar.isGM() && (!isValidMerchant || !activeChar.isInActingRange(merchant))) {
            activeChar.sendActionFailed();
            return;
        }
        activeChar.getInventory().writeLock();
        try {
            for (int i = 0; i < _count; ++i) {
                final int objectId = _items[i];
                final long count = _itemQ[i];
                final ItemInstance item = activeChar.getInventory().getItemByObjectId(objectId);
                if (item != null && item.getCount() >= count) {
                    if (item.canBeSold(activeChar)) {
                        final long price = SafeMath.mulAndCheck(Math.max(1L, item.getReferencePrice() / Config.ALT_SHOP_REFUND_SELL_DIVISOR), count);
                        final ItemInstance refund = activeChar.getInventory().removeItemByObjectId(objectId, count);
                        Log.LogItem(activeChar, ItemLog.RefundSell, refund);
                        activeChar.addAdena(price);
                        activeChar.getRefund().addItem(refund);
                    }
                }
            }
        } catch (ArithmeticException ae) {
            sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
            return;
        } finally {
            activeChar.getInventory().writeUnlock();
        }
        activeChar.sendPacket(new SellRefundList(activeChar, true));
        activeChar.sendChanges();
    }
}
