package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.commons.math.SafeMath;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Request;
import ru.j2dev.gameserver.model.Request.L2RequestType;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.items.TradeItem;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SendTradeDone;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.TradePressOtherOk;
import ru.j2dev.gameserver.utils.Log;
import ru.j2dev.gameserver.utils.Log.ItemLog;

import java.util.List;

public class TradeDone extends L2GameClientPacket {
    private int _response;

    @Override
    protected void readImpl() {
        _response = readD();
    }

    @Override
    protected void runImpl() {
        final Player parthner1 = getClient().getActiveChar();
        if (parthner1 == null) {
            return;
        }
        final Request request = parthner1.getRequest();
        if (request == null || !request.isTypeOf(L2RequestType.TRADE)) {
            parthner1.sendActionFailed();
            return;
        }
        if (!request.isInProgress()) {
            request.cancel();
            parthner1.sendPacket(SendTradeDone.FAIL);
            parthner1.sendActionFailed();
            return;
        }
        if (parthner1.isOutOfControl()) {
            request.cancel();
            parthner1.sendPacket(SendTradeDone.FAIL);
            parthner1.sendActionFailed();
            return;
        }
        final Player parthner2 = request.getOtherPlayer(parthner1);
        if (parthner2 == null) {
            request.cancel();
            parthner1.sendPacket(SendTradeDone.FAIL);
            parthner1.sendPacket(Msg.THAT_PLAYER_IS_NOT_ONLINE);
            parthner1.sendActionFailed();
            return;
        }
        if (parthner2.getRequest() != request) {
            request.cancel();
            parthner1.sendPacket(SendTradeDone.FAIL);
            parthner1.sendActionFailed();
            return;
        }
        if (_response == 0) {
            request.cancel();
            parthner1.sendPacket(SendTradeDone.FAIL);
            parthner2.sendPacket(SendTradeDone.FAIL, new SystemMessage(124).addString(parthner1.getName()));
            return;
        }
        if (!parthner2.isInActingRange(parthner1)) {
            parthner1.sendPacket(Msg.YOUR_TARGET_IS_OUT_OF_RANGE);
            return;
        }
        request.confirm(parthner1);
        parthner2.sendPacket(new SystemMessage(121).addString(parthner1.getName()), TradePressOtherOk.STATIC);
        if (!request.isConfirmed(parthner2)) {
            parthner1.sendActionFailed();
            return;
        }
        final List<TradeItem> tradeList1 = parthner1.getTradeList();
        final List<TradeItem> tradeList2 = parthner2.getTradeList();
        int slots;
        long weight;
        boolean success = false;
        parthner1.getInventory().writeLock();
        parthner2.getInventory().writeLock();
        try {
            slots = 0;
            weight = 0L;
            for (final TradeItem ti : tradeList1) {
                final ItemInstance item = parthner1.getInventory().getItemByObjectId(ti.getObjectId());
                if (item == null || item.getCount() < ti.getCount() || !item.canBeTraded(parthner1)) {
                    return;
                }
                weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(ti.getCount(), ti.getItem().getWeight()));
                if (ti.getItem().isStackable() && parthner2.getInventory().getItemByItemId(ti.getItemId()) != null) {
                    continue;
                }
                ++slots;
            }
            if (!parthner2.getInventory().validateWeight(weight)) {
                parthner2.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
                return;
            }
            if (!parthner2.getInventory().validateCapacity(slots)) {
                parthner2.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
                return;
            }
            slots = 0;
            weight = 0L;
            for (final TradeItem ti : tradeList2) {
                final ItemInstance item = parthner2.getInventory().getItemByObjectId(ti.getObjectId());
                if (item == null || item.getCount() < ti.getCount() || !item.canBeTraded(parthner2)) {
                    return;
                }
                weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(ti.getCount(), ti.getItem().getWeight()));
                if (ti.getItem().isStackable() && parthner1.getInventory().getItemByItemId(ti.getItemId()) != null) {
                    continue;
                }
                ++slots;
            }
            if (!parthner1.getInventory().validateWeight(weight)) {
                parthner1.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
                return;
            }
            if (!parthner1.getInventory().validateCapacity(slots)) {
                parthner1.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
                return;
            }
            for (final TradeItem ti : tradeList1) {
                final ItemInstance item = parthner1.getInventory().removeItemByObjectId(ti.getObjectId(), ti.getCount());
                Log.LogItem(parthner1, ItemLog.TradeSell, item);
                Log.LogItem(parthner2, ItemLog.TradeBuy, item);
                parthner2.getInventory().addItem(item);
            }
            for (final TradeItem ti : tradeList2) {
                final ItemInstance item = parthner2.getInventory().removeItemByObjectId(ti.getObjectId(), ti.getCount());
                Log.LogItem(parthner2, ItemLog.TradeSell, item);
                Log.LogItem(parthner1, ItemLog.TradeBuy, item);
                parthner1.getInventory().addItem(item);
            }
            parthner1.sendPacket(Msg.YOUR_TRADE_IS_SUCCESSFUL);
            parthner2.sendPacket(Msg.YOUR_TRADE_IS_SUCCESSFUL);
            success = true;
        } finally {
            parthner2.getInventory().writeUnlock();
            parthner1.getInventory().writeUnlock();
            request.done();
            parthner1.sendPacket(success ? SendTradeDone.SUCCESS : SendTradeDone.FAIL);
            parthner2.sendPacket(success ? SendTradeDone.SUCCESS : SendTradeDone.FAIL);
        }
    }
}
