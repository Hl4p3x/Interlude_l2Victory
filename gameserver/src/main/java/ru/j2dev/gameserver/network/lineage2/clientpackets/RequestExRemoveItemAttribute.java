package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.base.Element;
import ru.j2dev.gameserver.model.items.ItemAttributes;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.items.PcInventory;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ActionFail;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExBaseAttributeCancelResult;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowBaseAttributeCancelWindow;
import ru.j2dev.gameserver.network.lineage2.serverpackets.InventoryUpdate;

public class RequestExRemoveItemAttribute extends L2GameClientPacket {
    private int _objectId;
    private int _attributeId;

    @Override
    protected void readImpl() {
        _objectId = readD();
        _attributeId = readD();
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        if (activeChar.isActionsDisabled() || activeChar.isInStoreMode() || activeChar.isInTrade()) {
            activeChar.sendActionFailed();
            return;
        }
        final PcInventory inventory = activeChar.getInventory();
        final ItemInstance itemToUnnchant = inventory.getItemByObjectId(_objectId);
        if (itemToUnnchant == null) {
            activeChar.sendActionFailed();
            return;
        }
        final ItemAttributes set = itemToUnnchant.getAttributes();
        final Element element = Element.getElementById(_attributeId);
        if (element == Element.NONE || set.getValue(element) <= 0) {
            activeChar.sendPacket(new ExBaseAttributeCancelResult(false, itemToUnnchant, element), ActionFail.STATIC);
            return;
        }
        if (!activeChar.reduceAdena(ExShowBaseAttributeCancelWindow.getAttributeRemovePrice(itemToUnnchant), true)) {
            activeChar.sendPacket(new ExBaseAttributeCancelResult(false, itemToUnnchant, element), SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA, ActionFail.STATIC);
            return;
        }
        boolean equipped;
        if (equipped = itemToUnnchant.isEquipped()) {
            activeChar.getInventory().unEquipItem(itemToUnnchant);
        }
        itemToUnnchant.setAttributeElement(element, 0);
        if (equipped) {
            activeChar.getInventory().equipItem(itemToUnnchant);
        }
        activeChar.sendPacket(new InventoryUpdate().addModifiedItem(itemToUnnchant));
        activeChar.sendPacket(new ExBaseAttributeCancelResult(true, itemToUnnchant, element));
        activeChar.updateStats();
    }
}
