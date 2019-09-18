package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.pledge.Alliance;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.network.lineage2.serverpackets.L2GameServerPacket;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.tables.ClanTable;

import java.util.ArrayList;
import java.util.List;

public class RequestAllyInfo extends L2GameClientPacket {
    @Override
    protected void readImpl() {
    }

    @Override
    protected void runImpl() {
        final Player player = getClient().getActiveChar();
        if (player == null) {
            return;
        }
        final Alliance ally = player.getAlliance();
        if (ally == null) {
            return;
        }
        int clancount;
        final Clan leaderclan = player.getAlliance().getLeader();
        clancount = ClanTable.getInstance().getAlliance(leaderclan.getAllyId()).getMembers().length;
        final int[] online = new int[clancount + 1];
        final int[] count = new int[clancount + 1];
        final Clan[] clans = player.getAlliance().getMembers();
        for (int i = 0; i < clancount; ++i) {
            online[i + 1] = clans[i].getOnlineMembers(0).size();
            count[i + 1] = clans[i].getAllSize();
            final int n = 0;
            online[n] += online[i + 1];
            final int n2 = 0;
            count[n2] += count[i + 1];
        }
        final List<L2GameServerPacket> packets = new ArrayList<>(7 + 5 * clancount);
        packets.add(Msg._ALLIANCE_INFORMATION_);
        packets.add(new SystemMessage(492).addString(player.getClan().getAlliance().getAllyName()));
        packets.add(new SystemMessage(493).addNumber(online[0]).addNumber(count[0]));
        packets.add(new SystemMessage(494).addString(leaderclan.getName()).addString(leaderclan.getLeaderName()));
        packets.add(new SystemMessage(495).addNumber(clancount));
        packets.add(Msg._CLAN_INFORMATION_);
        for (int j = 0; j < clancount; ++j) {
            packets.add(new SystemMessage(497).addString(clans[j].getName()));
            packets.add(new SystemMessage(498).addString(clans[j].getLeaderName()));
            packets.add(new SystemMessage(499).addNumber(clans[j].getLevel()));
            packets.add(new SystemMessage(493).addNumber(online[j + 1]).addNumber(count[j + 1]));
            packets.add(Msg.__DASHES__);
        }
        packets.add(Msg.__EQUALS__);
        player.sendPacket(packets);
    }
}
