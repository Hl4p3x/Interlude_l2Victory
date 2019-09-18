package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.model.Player;

public class RequestItemList extends L2GameClientPacket {
    @Override
    protected void readImpl() {
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        if (!activeChar.getPlayerAccess().UseInventory || activeChar.isBlocked()) {
            activeChar.sendActionFailed();
            return;
        }
        activeChar.sendItemList(true);
        activeChar.sendStatusUpdate(false, false, 14);
    }
}
