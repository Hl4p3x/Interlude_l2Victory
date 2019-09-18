package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.utils.Location;

public class RequestDropItem extends L2GameClientPacket {
    private int _objectId;
    private long _count;
    private Location _loc;

    @Override
    protected void readImpl() {
        _objectId = readD();
        _count = readD();
        _loc = new Location(readD(), readD(), readD());
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        if (_count < 1L || _loc.isNull()) {
            activeChar.sendActionFailed();
            return;
        }
        if (activeChar.isActionsDisabled()) {
            activeChar.sendActionFailed();
            return;
        }
        if (!Config.ALLOW_DISCARDITEM || activeChar.getPlayerAccess().BlockInventory) {
            activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestDropItem.Disallowed", activeChar));
            return;
        }
        if (activeChar.isInStoreMode()) {
            activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
            return;
        }
        if (activeChar.isSitting() || activeChar.isDropDisabled()) {
            activeChar.sendActionFailed();
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
        if (activeChar.isActionBlocked("drop_item")) {
            activeChar.sendPacket(Msg.YOU_CANNOT_DISCARD_THOSE_ITEMS_HERE);
            return;
        }
        if (!activeChar.isInRangeSq(_loc, 22500L) || Math.abs(_loc.z - activeChar.getZ()) > 50) {
            activeChar.sendPacket(Msg.THAT_IS_TOO_FAR_FROM_YOU_TO_DISCARD);
            return;
        }
        final ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
        if (item == null) {
            activeChar.sendActionFailed();
            return;
        }
        if(activeChar.getInventory().itemIsLocked(item)) {
            activeChar.sendMessage(activeChar.isLangRus() ? "Вы не можете использовать это предмет" : "Cannot use this item.");
            return;
        }
        if (!item.canBeDropped(activeChar, false)) {
            activeChar.sendPacket(Msg.THAT_ITEM_CANNOT_BE_DISCARDED);
            return;
        }
        item.getTemplate().getHandler().dropItem(activeChar, item, _count, _loc);
    }
}
