package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.PetInstance;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.items.PcInventory;
import ru.j2dev.gameserver.model.items.PetInventory;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.utils.Log;
import ru.j2dev.gameserver.utils.Log.ItemLog;

public class RequestGetItemFromPet extends L2GameClientPacket {
    private int _objectId;
    private long _amount;
    private int _unknown;

    @Override
    protected void readImpl() {
        _objectId = readD();
        _amount = readD();
        _unknown = readD();
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null || _amount < 1L) {
            return;
        }
        final PetInstance pet = (PetInstance) activeChar.getPet();
        if (pet == null) {
            activeChar.sendActionFailed();
            return;
        }
        if (activeChar.isOutOfControl()) {
            activeChar.sendActionFailed();
            return;
        }
        if (activeChar.isInStoreMode()) {
            activeChar.sendPacket(SystemMsg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
            return;
        }
        if (activeChar.isInTrade() || activeChar.isProcessingRequest()) {
            activeChar.sendActionFailed();
            return;
        }
        if (activeChar.isFishing()) {
            activeChar.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING_);
            return;
        }
        final PetInventory petInventory = pet.getInventory();
        final PcInventory playerInventory = activeChar.getInventory();
        petInventory.writeLock();
        playerInventory.writeLock();
        try {
            ItemInstance item = petInventory.getItemByObjectId(_objectId);
            if (item == null || item.getCount() < _amount || item.isEquipped()) {
                activeChar.sendActionFailed();
                return;
            }
            int slots = 0;
            final long weight = item.getTemplate().getWeight() * _amount;
            if (!item.getTemplate().isStackable() || activeChar.getInventory().getItemByItemId(item.getItemId()) == null) {
                slots = 1;
            }
            if (!activeChar.getInventory().validateWeight(weight)) {
                activeChar.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
                return;
            }
            if (!activeChar.getInventory().validateCapacity(slots)) {
                activeChar.sendPacket(SystemMsg.YOUR_INVENTORY_IS_FULL);
                return;
            }
            item = petInventory.removeItemByObjectId(_objectId, _amount);
            Log.LogItem(activeChar, ItemLog.FromPet, item);
            playerInventory.addItem(item);
            pet.sendChanges();
            activeChar.sendChanges();
        } finally {
            playerInventory.writeUnlock();
            petInventory.writeUnlock();
        }
    }
}
