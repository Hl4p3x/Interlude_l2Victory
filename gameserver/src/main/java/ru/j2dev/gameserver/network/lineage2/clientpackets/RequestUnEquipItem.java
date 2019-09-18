package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;

@Deprecated
public class RequestUnEquipItem extends L2GameClientPacket {
    private int _slot;

    @Override
    protected void readImpl() {
        _slot = readD();
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
        if (activeChar.isFishing()) {
            activeChar.sendPacket(Msg.YOU_CANNOT_DO_ANYTHING_ELSE_WHILE_FISHING);
            return;
        }
        if ((_slot == 128 || _slot == 256 || _slot == 16384) && (activeChar.isCursedWeaponEquipped() || activeChar.getActiveWeaponFlagAttachment() != null)) {
            return;
        }
        if (_slot == 128) {
            final ItemInstance weapon = activeChar.getActiveWeaponInstance();
            if (weapon == null) {
                return;
            }
            activeChar.abortAttack(true, true);
            activeChar.abortCast(true, true);
            activeChar.sendDisarmMessage(weapon);
        }
        final ItemInstance slotItem = activeChar.getInventory().getPaperdollItem(_slot);
        if(activeChar.getInventory().itemIsLocked(slotItem)) {
            activeChar.sendMessage(activeChar.isLangRus() ? "Вы не можете использовать это предмет" : "Cannot use this item.");
        }
        activeChar.getInventory().unEquipItemInBodySlot(_slot);
    }
}
