package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.model.Player;

public class SendAppearing extends L2GameClientPacket {
    @Override
    protected void readImpl() {
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        if (activeChar.isLogoutStarted()) {
            activeChar.sendActionFailed();
            return;
        }
        if (activeChar.getObserverMode() == 1) {
            activeChar.appearObserverMode();
            return;
        }
        if (activeChar.getObserverMode() == 2) {
            activeChar.returnFromObserverMode();
            return;
        }
        if (!activeChar.isTeleporting()) {
            activeChar.sendActionFailed();
            return;
        }
        activeChar.onTeleported();
    }
}
