package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.Player;

public class RequestOlympiadObserverEnd extends L2GameClientPacket {
    @Override
    protected void readImpl() {
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        if (Config.OLY_ENABLED && activeChar.isOlyObserver()) {
            activeChar.leaveOlympiadObserverMode();
        }
    }
}
