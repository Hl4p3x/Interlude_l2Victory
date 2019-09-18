package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.model.pledge.UnitMember;

import java.util.ArrayList;
import java.util.List;

public class GMViewPledgeInfo extends L2GameServerPacket {
    private final String clan_name;
    private final String leader_name;
    private final String ally_name;
    private final int rank;
    private final int ally_id;
    private final int ally_crest_id;
    private final int hasCastle;
    private final int hasHideout;
    private final int atWar;
    private final List<PledgeMemberInfo> infos;
    private String char_name;
    private int clan_id;
    private int clan_crest_id;
    private int clan_level;
    private int rep;

    public GMViewPledgeInfo(final Player activeChar) {
        infos = new ArrayList<>();
        final Clan clan = activeChar.getClan();
        for (final UnitMember member : clan) {
            if (member == null) {
                continue;
            }
            char_name = member.getName();
            clan_level = member.getLevel();
            clan_id = member.getClassId();
            clan_crest_id = (member.isOnline() ? member.getObjectId() : 0);
            rep = ((member.getSponsor() != 0) ? 1 : 0);
            infos.add(new PledgeMemberInfo(char_name, clan_level, clan_id, clan_crest_id, member.getSex(), 1, rep));
        }
        char_name = activeChar.getName();
        clan_id = clan.getClanId();
        clan_name = clan.getName();
        leader_name = clan.getLeaderName();
        clan_crest_id = clan.getCrestId();
        clan_level = clan.getLevel();
        hasCastle = clan.getCastle();
        hasHideout = clan.getHasHideout();
        rank = clan.getRank();
        rep = clan.getReputationScore();
        ally_id = clan.getAllyId();
        if (clan.getAlliance() != null) {
            ally_name = clan.getAlliance().getAllyName();
            ally_crest_id = clan.getAlliance().getAllyCrestId();
        } else {
            ally_name = "";
            ally_crest_id = 0;
        }
        atWar = clan.isAtWar();
    }

    @Override
    protected final void writeImpl() {
        writeC(0x90);
        writeS(char_name);
        writeD(clan_id);
        writeD(0);
        writeS(clan_name);
        writeS(leader_name);
        writeD(clan_crest_id);
        writeD(clan_level);
        writeD(hasCastle);
        writeD(hasHideout);
        writeD(rank);
        writeD(rep);
        writeD(0);
        writeD(0);
        writeD(ally_id);
        writeS(ally_name);
        writeD(ally_crest_id);
        writeD(atWar);
        writeD(infos.size());
        infos.forEach(_info -> {
            writeS(_info._name);
            writeD(_info.level);
            writeD(_info.class_id);
            writeD(_info.sex);
            writeD(_info.race);
            writeD(_info.online);
            writeD(_info.sponsor);
        });
        infos.clear();
    }

    static class PledgeMemberInfo {
        public final String _name;
        public final int level;
        public final int class_id;
        public final int online;
        public final int sex;
        public final int race;
        public final int sponsor;

        public PledgeMemberInfo(final String __name, final int _level, final int _class_id, final int _online, final int _sex, final int _race, final int _sponsor) {
            _name = __name;
            level = _level;
            class_id = _class_id;
            online = _online;
            sex = _sex;
            race = _race;
            sponsor = _sponsor;
        }
    }
}
