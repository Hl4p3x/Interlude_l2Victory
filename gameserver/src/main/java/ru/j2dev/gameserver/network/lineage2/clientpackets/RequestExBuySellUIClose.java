package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.model.Player;

public class RequestExBuySellUIClose extends L2GameClientPacket {
    @Override
    protected void runImpl() {
    }

    @Override
    protected void readImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        activeChar.setBuyListId(0);
        activeChar.sendItemList(true);
    }
}
