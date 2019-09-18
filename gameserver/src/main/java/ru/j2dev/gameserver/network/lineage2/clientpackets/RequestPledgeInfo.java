package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PledgeInfo;
import ru.j2dev.gameserver.tables.ClanTable;

public class RequestPledgeInfo extends L2GameClientPacket {
    private int _clanId;

    @Override
    protected void readImpl() {
        _clanId = readD();
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        if (_clanId < 10000000) {
            activeChar.sendActionFailed();
            return;
        }
        final Clan clan = ClanTable.getInstance().getClan(_clanId);
        if (clan == null) {
            activeChar.sendActionFailed();
            return;
        }
        activeChar.sendPacket(new PledgeInfo(clan));
    }
}
