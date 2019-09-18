package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.handler.items.IItemHandler;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExAutoSoulShot;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.utils.Log;

public class RequestAutoSoulShot extends L2GameClientPacket {

    private int _itemId;
    private boolean _type;

    @Override
    protected void readImpl() {
        _itemId = readD();
        _type = (readD() == 1);
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        if (activeChar.getPrivateStoreType() != Player.STORE_PRIVATE_NONE || activeChar.isDead()) {
            return;
        }
        final ItemInstance item = activeChar.getInventory().getItemByItemId(_itemId);
        if (item == null) {
            return;
        }
        if (!item.getTemplate().isShotItem()) {
            return;
        }
        if(activeChar.getInventory().itemIsLocked(item)) {
            activeChar.sendMessage(activeChar.isLangRus() ? "Вы не можете использовать это предмет" : "Cannot use this item.");
            return;
        }
        if (!item.getTemplate().testCondition(activeChar, item, false)) {
            final String msg = "Player " + activeChar.getName() + " trying illegal item use, ban this player!";
            Log.add(msg, "illegal-actions");
            LOGGER.warn(msg);
            return;
        }
        if (_type) {
            activeChar.addAutoSoulShot(_itemId);
            activeChar.sendPacket(new ExAutoSoulShot(_itemId, true));
            activeChar.sendPacket(new SystemMessage(1433).addString(item.getName()));
            final IItemHandler handler = item.getTemplate().getHandler();
            handler.useItem(activeChar, item, false);
            return;
        }
        activeChar.removeAutoSoulShot(_itemId);
        activeChar.sendPacket(new ExAutoSoulShot(_itemId, false));
        activeChar.sendPacket(new SystemMessage(1434).addString(item.getName()));
    }
}
