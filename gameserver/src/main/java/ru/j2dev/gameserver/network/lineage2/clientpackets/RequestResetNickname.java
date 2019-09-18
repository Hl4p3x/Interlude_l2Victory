package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.model.Player;

public class RequestResetNickname extends L2GameClientPacket {
    @Override
    protected void readImpl() {
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        if (activeChar.getTitleColor() != 16777079) {
            activeChar.setTitleColor(16777079);
            activeChar.broadcastUserInfo(true);
        }
    }
}
