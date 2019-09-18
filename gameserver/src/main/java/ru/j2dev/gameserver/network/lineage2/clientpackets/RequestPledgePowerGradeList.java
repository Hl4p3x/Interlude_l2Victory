package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.model.pledge.RankPrivs;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PledgePowerGradeList;

import java.util.Collection;

public class RequestPledgePowerGradeList extends L2GameClientPacket {
    @Override
    protected void readImpl() {
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        final Clan clan = activeChar.getClan();
        if (clan != null) {
            final Collection<RankPrivs> privs = clan.getAllRankPrivs();
            activeChar.sendPacket(new PledgePowerGradeList(privs));
        }
    }
}
