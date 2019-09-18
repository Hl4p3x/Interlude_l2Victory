package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.model.Player;

public class RequestShortCutDel extends L2GameClientPacket {
    private int _slot;
    private int _page;

    @Override
    protected void readImpl() {
        final int id = readD();
        _slot = id % 12;
        _page = id / 12;
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        activeChar.deleteShortCut(_slot, _page);
    }
}
