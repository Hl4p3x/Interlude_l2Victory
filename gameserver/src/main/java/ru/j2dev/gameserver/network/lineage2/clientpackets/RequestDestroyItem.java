package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;
import ru.j2dev.gameserver.tables.PetDataTable;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.Log;
import ru.j2dev.gameserver.utils.Log.ItemLog;

public class RequestDestroyItem extends L2GameClientPacket {
    private int _objectId;
    private long _count;

    @Override
    protected void readImpl() {
        _objectId = readD();
        _count = readD();
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
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
        long count = _count;
        final ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
        if (item == null) {
            activeChar.sendActionFailed();
            return;
        }
        if (count < 1L) {
            activeChar.sendPacket(Msg.YOU_CANNOT_DESTROY_IT_BECAUSE_THE_NUMBER_IS_INCORRECT);
            return;
        }
        if(activeChar.getInventory().itemIsLocked(item)) {
            activeChar.sendMessage(activeChar.isLangRus() ? "Вы не можете использовать это предмет" : "Cannot use this item.");
            return;
        }
        if (!activeChar.isGM() && item.isHeroWeapon()) {
            activeChar.sendPacket(Msg.HERO_WEAPONS_CANNOT_BE_DESTROYED);
            return;
        }
        if (activeChar.getPet() != null && activeChar.getPet().getControlItemObjId() == item.getObjectId()) {
            activeChar.sendPacket(Msg.THE_PET_HAS_BEEN_SUMMONED_AND_CANNOT_BE_DELETED);
            return;
        }
        if (!activeChar.isGM() && !item.canBeDestroyed(activeChar)) {
            activeChar.sendPacket(Msg.THIS_ITEM_CANNOT_BE_DISCARDED);
            return;
        }
        if (_count > item.getCount()) {
            count = item.getCount();
        }
        boolean crystallize = item.canBeCrystallized(activeChar);
        final int crystalId = item.getTemplate().getCrystalType().cry;
        final int crystalAmount = item.getTemplate().getCrystalCount();
        if (crystallize) {
            if (Config.DWARF_AUTOMATICALLY_CRYSTALLIZE_ON_ITEM_DELETE) {
                final int level = activeChar.getSkillLevel(248);
                if (level < 1 || crystalId - 1458 + 1 > level) {
                    crystallize = false;
                }
            } else {
                crystallize = false;
            }
        }
        Log.LogItem(activeChar, ItemLog.Delete, item, count);
        if (!activeChar.getInventory().destroyItemByObjectId(_objectId, count)) {
            activeChar.sendActionFailed();
            return;
        }
        if (PetDataTable.isPetControlItem(item)) {
            PetDataTable.deletePet(item, activeChar);
        }
        if (crystallize) {
            activeChar.sendPacket(Msg.THE_ITEM_HAS_BEEN_SUCCESSFULLY_CRYSTALLIZED);
            ItemFunctions.addItem(activeChar, crystalId, crystalAmount, true);
        } else {
            activeChar.sendPacket(SystemMessage2.removeItems(item.getItemId(), count));
        }
        activeChar.sendChanges();
    }
}
