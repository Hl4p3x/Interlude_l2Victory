package ru.j2dev.gameserver.network.lineage2.clientpackets;

import org.apache.commons.lang3.ArrayUtils;
import ru.j2dev.commons.math.SafeMath;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.items.PcInventory;
import ru.j2dev.gameserver.model.items.Warehouse;
import ru.j2dev.gameserver.model.items.Warehouse.WarehouseType;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.utils.Log;
import ru.j2dev.gameserver.utils.Log.ItemLog;

public class SendWareHouseDepositList extends L2GameClientPacket {
    private static final long _WAREHOUSE_FEE = 30L;
    private int _count;
    private int[] _items;
    private long[] _itemQ;

    @Override
    protected void readImpl() {
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
            if (_itemQ[i] < 1L || ArrayUtils.indexOf(_items, _items[i]) < i) {
                _count = 0;
                return;
            }
        }
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null || _count == 0) {
            return;
        }
        if (!activeChar.getPlayerAccess().UseWarehouse) {
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
        final NpcInstance whkeeper = activeChar.getLastNpc();
        if (whkeeper == null || !whkeeper.isInActingRange(activeChar)) {
            activeChar.sendPacket(Msg.WAREHOUSE_IS_TOO_FAR);
            return;
        }
        final PcInventory inventory = activeChar.getInventory();
        final boolean privatewh = activeChar.getUsingWarehouseType() != WarehouseType.CLAN;
        Warehouse warehouse;
        if (privatewh) {
            warehouse = activeChar.getWarehouse();
        } else {
            warehouse = activeChar.getClan().getWarehouse();
        }
        inventory.writeLock();
        warehouse.writeLock();
        try {
            int slotsleft;
            long adenaDeposit = 0L;
            if (privatewh) {
                slotsleft = activeChar.getWarehouseLimit() - warehouse.getSize();
            } else {
                slotsleft = activeChar.getClan().getWhBonus() + Config.WAREHOUSE_SLOTS_CLAN - warehouse.getSize();
            }
            int items = 0;
            for (int i = 0; i < _count; ++i) {
                final ItemInstance item = inventory.getItemByObjectId(_items[i]);
                if (item == null || item.getCount() < _itemQ[i] || !item.canBeStored(activeChar, privatewh)) {
                    _items[i] = 0;
                    _itemQ[i] = 0L;
                } else {
                    if (!item.isStackable() || warehouse.getItemByItemId(item.getItemId()) == null) {
                        if (slotsleft <= 0) {
                            _items[i] = 0;
                            _itemQ[i] = 0L;
                            continue;
                        }
                        --slotsleft;
                    }
                    if (item.getItemId() == 57) {
                        adenaDeposit = _itemQ[i];
                    }
                    ++items;
                }
            }
            if (slotsleft <= 0) {
                activeChar.sendPacket(Msg.YOUR_WAREHOUSE_IS_FULL);
            }
            if (items == 0) {
                activeChar.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
                return;
            }
            final long fee = SafeMath.mulAndCheck(items, 30L);
            if (fee + adenaDeposit > activeChar.getAdena()) {
                activeChar.sendPacket(Msg.YOU_LACK_THE_FUNDS_NEEDED_TO_PAY_FOR_THIS_TRANSACTION);
                return;
            }
            if (!activeChar.reduceAdena(fee, true)) {
                sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
                return;
            }
            for (int j = 0; j < _count; ++j) {
                if (_items[j] != 0) {
                    final ItemInstance item2 = inventory.removeItemByObjectId(_items[j], _itemQ[j]);
                    Log.LogItem(activeChar, privatewh ? ItemLog.WarehouseDeposit : ItemLog.ClanWarehouseDeposit, item2);
                    warehouse.addItem(item2);
                }
            }
        } catch (ArithmeticException ae) {
            sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
            return;
        } finally {
            warehouse.writeUnlock();
            inventory.writeUnlock();
        }
        activeChar.sendChanges();
        activeChar.sendPacket(Msg.THE_TRANSACTION_IS_COMPLETE);
    }
}
