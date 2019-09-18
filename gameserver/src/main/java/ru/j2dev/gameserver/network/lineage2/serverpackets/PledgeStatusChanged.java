package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.pledge.Clan;

public class PledgeStatusChanged extends L2GameServerPacket {
    private final int leader_id;
    private final int clan_id;
    private final int level;

    public PledgeStatusChanged(final Clan clan) {
        leader_id = clan.getLeaderId();
        clan_id = clan.getClanId();
        level = clan.getLevel();
    }

    @Override
    protected final void writeImpl() {
        writeC(0xcd);
        writeD(leader_id);
        writeD(clan_id);
        writeD(0);
        writeD(level);
        writeD(0);
        writeD(0);
        writeD(0);
    }
}
