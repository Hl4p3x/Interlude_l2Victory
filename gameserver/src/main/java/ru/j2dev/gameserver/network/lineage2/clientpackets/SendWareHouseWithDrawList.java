package ru.j2dev.gameserver.network.lineage2.clientpackets;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

public class SendWareHouseWithDrawList extends L2GameClientPacket {
    private static final Logger LOGGER = LoggerFactory.getLogger(SendWareHouseWithDrawList.class);

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
        Warehouse warehouse;
        ItemLog logType;
        switch (activeChar.getUsingWarehouseType()) {
            case PRIVATE:
                warehouse = activeChar.getWarehouse();
                logType = ItemLog.WarehouseWithdraw;
                break;
            case CLAN:
                logType = ItemLog.ClanWarehouseWithdraw;
                boolean canWithdrawCWH = false;
                if (activeChar.getClan() != null && (activeChar.getClanPrivileges() & 0x8) == 0x8 && (Config.ALT_ALLOW_OTHERS_WITHDRAW_FROM_CLAN_WAREHOUSE || activeChar.isClanLeader() || activeChar.getVarB("canWhWithdraw"))) {
                    canWithdrawCWH = true;
                }
                if (!canWithdrawCWH) {
                    return;
                }
                warehouse = activeChar.getClan().getWarehouse();
                break;
            default:
                if (activeChar.getUsingWarehouseType() != WarehouseType.FREIGHT) {
                    LOGGER.warn("Error retrieving a warehouse object for char " + activeChar.getName() + " - using warehouse type: " + activeChar.getUsingWarehouseType());
                    return;
                }
                warehouse = activeChar.getFreight();
                logType = ItemLog.FreightWithdraw;
                break;
        }
        final PcInventory inventory = activeChar.getInventory();
        inventory.writeLock();
        warehouse.writeLock();
        try {
            long weight = 0L;
            int slots = 0;
            for (int i = 0; i < _count; ++i) {
                final ItemInstance item = warehouse.getItemByObjectId(_items[i]);
                if (item == null || item.getCount() < _itemQ[i]) {
                    activeChar.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
                    return;
                }
                weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(item.getTemplate().getWeight(), _itemQ[i]));
                if (!item.isStackable() || inventory.getItemByItemId(item.getItemId()) == null) {
                    ++slots;
                }
            }
            if (!activeChar.getInventory().validateCapacity(slots)) {
                activeChar.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
                return;
            }
            if (!activeChar.getInventory().validateWeight(weight)) {
                activeChar.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
                return;
            }
            for (int i = 0; i < _count; ++i) {
                final ItemInstance item = warehouse.removeItemByObjectId(_items[i], _itemQ[i]);
                Log.LogItem(activeChar, logType, item);
                activeChar.getInventory().addItem(item);
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
