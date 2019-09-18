package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.data.xml.holder.ResidenceHolder;
import ru.j2dev.gameserver.model.entity.events.impl.ClanHallAuctionEvent;
import ru.j2dev.gameserver.model.entity.events.impl.ClanHallMiniGameEvent;
import ru.j2dev.gameserver.model.entity.residence.ClanHall;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.tables.ClanTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExShowAgitInfo extends L2GameServerPacket {
    private List<AgitInfo> _clanHalls;

    public ExShowAgitInfo() {
        _clanHalls = Collections.emptyList();
        final List<ClanHall> chs = ResidenceHolder.getInstance().getResidenceList(ClanHall.class);
        _clanHalls = new ArrayList<>(chs.size());
        for (final ClanHall clanHall : chs) {
            final int ch_id = clanHall.getId();
            int getType;
            if (clanHall.getSiegeEvent().getClass() == ClanHallAuctionEvent.class) {
                getType = 0;
            } else if (clanHall.getSiegeEvent().getClass() == ClanHallMiniGameEvent.class) {
                getType = 2;
            } else {
                getType = 1;
            }
            final Clan clan = ClanTable.getInstance().getClan(clanHall.getOwnerId());
            final String clan_name = (clanHall.getOwnerId() == 0 || clan == null) ? "" : clan.getName();
            final String leader_name = (clanHall.getOwnerId() == 0 || clan == null) ? "" : clan.getLeaderName();
            _clanHalls.add(new AgitInfo(clan_name, leader_name, ch_id, getType));
        }
    }

    @Override
    protected final void writeImpl() {
        writeEx(0x16);
        writeD(_clanHalls.size());
        _clanHalls.forEach(info -> {
            writeD(info.ch_id);
            writeS(info.clan_name);
            writeS(info.leader_name);
            writeD(info.getType);
        });
    }

    static class AgitInfo {
        public final String clan_name;
        public final String leader_name;
        public final int ch_id;
        public final int getType;

        public AgitInfo(final String clan_name, final String leader_name, final int ch_id, final int lease) {
            this.clan_name = clan_name;
            this.leader_name = leader_name;
            this.ch_id = ch_id;
            getType = lease;
        }
    }
}
