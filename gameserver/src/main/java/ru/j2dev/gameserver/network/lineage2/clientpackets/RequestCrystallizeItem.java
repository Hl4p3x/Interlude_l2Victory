package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.Log;
import ru.j2dev.gameserver.utils.Log.ItemLog;

public class RequestCrystallizeItem extends L2GameClientPacket {
    private int _objectId;
    private long unk;

    @Override
    protected void readImpl() {
        _objectId = readD();
        unk = readD();
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
        final ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
        if (item == null) {
            activeChar.sendActionFailed();
            return;
        }
        if (item.isHeroWeapon()) {
            activeChar.sendPacket(Msg.HERO_WEAPONS_CANNOT_BE_DESTROYED);
            return;
        }
        if (!item.canBeCrystallized(activeChar)) {
            activeChar.sendActionFailed();
            return;
        }
        if(activeChar.getInventory().itemIsLocked(item)) {
            activeChar.sendMessage(activeChar.isLangRus() ? "Вы не можете использовать это предмет" : "Cannot use this item.");
            return;
        }
        if (activeChar.isInStoreMode()) {
            activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
            return;
        }
        if (activeChar.isFishing()) {
            activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
            return;
        }
        if (activeChar.isInTrade()) {
            activeChar.sendActionFailed();
            return;
        }
        final int crystalAmount = item.getTemplate().getCrystalCount();
        final int crystalId = item.getTemplate().getCrystalType().cry;
        final int level = activeChar.getSkillLevel(248);
        if (level < 1 || crystalId - 1458 + 1 > level) {
            activeChar.sendPacket(Msg.CANNOT_CRYSTALLIZE_CRYSTALLIZATION_SKILL_LEVEL_TOO_LOW);
            activeChar.sendActionFailed();
            return;
        }
        Log.LogItem(activeChar, ItemLog.Crystalize, item);
        if (!activeChar.getInventory().destroyItemByObjectId(_objectId, 1L)) {
            activeChar.sendActionFailed();
            return;
        }
        activeChar.sendPacket(Msg.THE_ITEM_HAS_BEEN_SUCCESSFULLY_CRYSTALLIZED);
        ItemFunctions.addItem(activeChar, crystalId, crystalAmount, true);
        activeChar.sendChanges();
    }
}
