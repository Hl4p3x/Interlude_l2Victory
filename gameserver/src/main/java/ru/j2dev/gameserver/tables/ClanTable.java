package ru.j2dev.gameserver.tables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.dao.CharacterVariablesDAO;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.idfactory.IdFactory;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.pledge.Alliance;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.model.pledge.SubUnit;
import ru.j2dev.gameserver.model.pledge.UnitMember;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PledgeShowMemberListDeleteAll;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;
import ru.j2dev.gameserver.utils.SiegeUtils;
import ru.j2dev.gameserver.utils.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClanTable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClanTable.class);
    private static final List<Skill> FULL_CLAN_SKILLS = Arrays.asList(SkillTable.getInstance().getInfo(370, 3), SkillTable.getInstance().getInfo(373, 3), SkillTable.getInstance().getInfo(379, 3), SkillTable.getInstance().getInfo(391, 1), SkillTable.getInstance().getInfo(371, 3), SkillTable.getInstance().getInfo(374, 3), SkillTable.getInstance().getInfo(376, 3), SkillTable.getInstance().getInfo(377, 3), SkillTable.getInstance().getInfo(383, 3), SkillTable.getInstance().getInfo(380, 3), SkillTable.getInstance().getInfo(382, 3), SkillTable.getInstance().getInfo(384, 3), SkillTable.getInstance().getInfo(385, 3), SkillTable.getInstance().getInfo(386, 3), SkillTable.getInstance().getInfo(387, 3), SkillTable.getInstance().getInfo(388, 3), SkillTable.getInstance().getInfo(390, 3), SkillTable.getInstance().getInfo(372, 3), SkillTable.getInstance().getInfo(375, 3), SkillTable.getInstance().getInfo(378, 3), SkillTable.getInstance().getInfo(381, 3), SkillTable.getInstance().getInfo(389, 3));
    private static ClanTable _instance;
    private final Map<Integer, Clan> _clans = new ConcurrentHashMap<>();
    private final Map<Integer, Alliance> _alliances = new ConcurrentHashMap<>();
    private Clan _npcClan;

    private ClanTable() {
        (_instance = this).restoreClans();
        restoreAllies();
        restoreWars();
    }

    public static ClanTable getInstance() {
        if (_instance == null) {
            new ClanTable();
        }
        return _instance;
    }

    public static void unload() {
        if (_instance != null) {
            try {
                _instance.finalize();
            } catch (Throwable ignored) {
            }
        }
    }

    public Clan[] getClans() {
        return _clans.values().toArray(new Clan[0]);
    }

    public Alliance[] getAlliances() {
        return _alliances.values().toArray(new Alliance[0]);
    }

    public Clan getClan(final int clanId) {
        if (clanId <= 0) {
            return null;
        }
        if (Config.ALT_NPC_CLAN == clanId) {
            return _npcClan;
        }
        return _clans.get(clanId);
    }

    public String getClanName(final int clanId) {
        final Clan c = getClan(clanId);
        return (c != null) ? c.getName() : "";
    }

    public Clan getClanByCharId(final int charId) {
        if (charId <= 0) {
            return null;
        }
        for (final Clan clan : getClans()) {
            if (clan != null && clan.isAnyMember(charId)) {
                return clan;
            }
        }
        return null;
    }

    public Alliance getAlliance(final int allyId) {
        if (allyId <= 0) {
            return null;
        }
        return _alliances.get(allyId);
    }

    public Alliance getAllianceByCharId(final int charId) {
        if (charId <= 0) {
            return null;
        }
        final Clan charClan = getClanByCharId(charId);
        return (charClan == null) ? null : charClan.getAlliance();
    }

    public Map.Entry<Clan, Alliance> getClanAndAllianceByCharId(final int charId) {
        final Player player = GameObjectsStorage.getPlayer(charId);
        final Clan charClan = (player != null) ? player.getClan() : getClanByCharId(charId);
        return new AbstractMap.SimpleEntry<>(charClan, (charClan == null) ? null : charClan.getAlliance());
    }

    public void restoreClans() {
        final List<Integer> clanIds = new ArrayList<>();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT clan_id FROM clan_data");
            result = statement.executeQuery();
            while (result.next()) {
                clanIds.add(result.getInt("clan_id"));
            }
        } catch (Exception e) {
            LOGGER.warn("Error while restoring clans!!! " + e);
        } finally {
            DbUtils.closeQuietly(con, statement, result);
        }
        for (final int clanId : clanIds) {
            final Clan clan = Clan.restore(clanId);
            if (clan == null) {
                LOGGER.warn("Error while restoring clanId: " + clanId);
            } else if (clan.getAllSize() <= 0) {
                LOGGER.warn("membersCount = 0 for clanId: " + clanId);
            } else if (clan.getLeader() == null) {
                LOGGER.warn("Not found leader for clanId: " + clanId);
            } else {
                _clans.put(clan.getClanId(), clan);
                if (Config.ALT_NPC_CLAN <= 0) {
                    continue;
                }
                _npcClan = _clans.get(Config.ALT_NPC_CLAN);
            }
        }
    }

    public void restoreAllies() {
        final List<Integer> allyIds = new ArrayList<>();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT ally_id FROM ally_data");
            result = statement.executeQuery();
            while (result.next()) {
                allyIds.add(result.getInt("ally_id"));
            }
        } catch (Exception e) {
            LOGGER.warn("Error while restoring allies!!! " + e);
        } finally {
            DbUtils.closeQuietly(con, statement, result);
        }
        for (final int allyId : allyIds) {
            final Alliance ally = new Alliance(allyId);
            if (ally.getMembersCount() <= 0) {
                LOGGER.warn("membersCount = 0 for allyId: " + allyId);
            } else if (ally.getLeader() == null) {
                LOGGER.warn("Not found leader for allyId: " + allyId);
            } else {
                _alliances.put(ally.getAllyId(), ally);
            }
        }
    }

    public Clan getClanByName(final String clanName) {
        if (!Util.isMatchingRegexp(clanName, Config.CLAN_NAME_TEMPLATE)) {
            return null;
        }
        for (final Clan clan : _clans.values()) {
            if (clan.getName().equalsIgnoreCase(clanName)) {
                return clan;
            }
        }
        return null;
    }

    public Alliance getAllyByName(final String allyName) {
        if (!Util.isMatchingRegexp(allyName, Config.ALLY_NAME_TEMPLATE)) {
            return null;
        }
        for (final Alliance ally : _alliances.values()) {
            if (ally.getAllyName().equalsIgnoreCase(allyName)) {
                return ally;
            }
        }
        return null;
    }

    public Clan createClan(final Player player, final String clanName) {
        if (getClanByName(clanName) == null) {
            final UnitMember leader = new UnitMember(player);
            leader.setLeaderOf(0);
            final Clan clan = new Clan(IdFactory.getInstance().getNextId());
            clan.setLevel(Config.CLAN_INIT_LEVEL);
            final SubUnit unit = new SubUnit(clan, 0, leader, clanName);
            unit.addUnitMember(leader);
            clan.addSubUnit(unit, false);
            clan.store();
            player.setPledgeType(0);
            player.setClan(clan);
            player.setPowerGrade(6);
            leader.setPlayerInstance(player, false);
            _clans.put(clan.getClanId(), clan);
            if (Config.CLAN_REPUTATION_BONUS_ON_CREATE > 0) {
                clan.incReputation(Config.CLAN_REPUTATION_BONUS_ON_CREATE, false, "ClanReputationOnCreateBonusAdd");
            }
            if (Config.FULL_CLAN_SKILLS_ON_CREATE) {
                FULL_CLAN_SKILLS.forEach(aNewClanSkill -> {
                    clan.addSkill(aNewClanSkill, true);
                    clan.broadcastToOnlineMembers(new SystemMessage2(SystemMsg.THE_CLAN_SKILL_S1_HAS_BEEN_ADDED).addSkillName(aNewClanSkill));
                });
            }
            return clan;
        }
        return null;
    }

    public void dissolveClan(final Clan clan) {
        final int leaderId = clan.getLeaderId();
        final Player leaderPlayer = (clan.getLeader() != null) ? clan.getLeader().getPlayer() : null;
        final long curtime = System.currentTimeMillis();
        if (leaderPlayer != null) {
            SiegeUtils.removeSiegeSkills(leaderPlayer);
        }
        clan.getOnlineMembers(0).forEach(clanMember -> {
            clanMember.setClan(null);
            clanMember.setTitle("");
            clanMember.sendPacket(PledgeShowMemberListDeleteAll.STATIC, Msg.YOU_HAVE_RECENTLY_BEEN_DISMISSED_FROM_A_CLAN_YOU_ARE_NOT_ALLOWED_TO_JOIN_ANOTHER_CLAN_FOR_24_HOURS);
            clanMember.broadcastCharInfo();
            clanMember.setLeaveClanTime(curtime);
        });
        clan.flush();
        deleteClanFromDb(clan.getClanId(), leaderId);
        _clans.remove(clan.getClanId());
        if (leaderPlayer != null) {
            leaderPlayer.sendPacket(Msg.CLAN_HAS_DISPERSED);
            leaderPlayer.setDeleteClanTime(curtime);
        }
    }

    public void deleteClanFromDb(final int clanId, final int leaderId) {
        final long curtime = System.currentTimeMillis();
        Connection con = null;
        PreparedStatement statement = null;
        boolean deleted = false;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("UPDATE characters SET clanid=0,title='',pledge_type=0,pledge_rank=0,lvl_joined_academy=0,apprentice=0,leaveclan=? WHERE clanid=?");
            statement.setLong(1, curtime / 1000L);
            statement.setInt(2, clanId);
            statement.execute();
            DbUtils.close(statement);
            statement = con.prepareStatement("UPDATE characters SET deleteclan=? WHERE obj_Id=?");
            statement.setLong(1, curtime / 1000L);
            statement.setInt(2, leaderId);
            statement.execute();
            DbUtils.close(statement);
            statement = con.prepareStatement("DELETE FROM clan_data WHERE clan_id=?");
            statement.setInt(1, clanId);
            statement.execute();
            DbUtils.close(statement);
            statement = con.prepareStatement("DELETE FROM clan_subpledges WHERE clan_id=?");
            statement.setInt(1, clanId);
            statement.execute();
            DbUtils.close(statement);
            statement = con.prepareStatement("DELETE FROM clan_privs WHERE clan_id=?");
            statement.setInt(1, clanId);
            statement.execute();
            DbUtils.close(statement);
            statement = con.prepareStatement("DELETE FROM siege_clans WHERE clan_id=?");
            statement.setInt(1, clanId);
            statement.execute();
            DbUtils.close(statement);
            statement = con.prepareStatement("DELETE FROM siege_players WHERE clan_id=?");
            statement.setInt(1, clanId);
            statement.execute();
            DbUtils.close(statement);
            statement = con.prepareStatement("DELETE FROM clan_skills WHERE clan_id=?");
            statement.setInt(1, clanId);
            statement.execute();
            deleted = true;
        } catch (Exception e) {
            LOGGER.warn("could not dissolve clan:" + e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
        if (deleted) {
            CharacterVariablesDAO.getInstance().deleteVars(clanId);
        }
    }

    public Alliance createAlliance(final Player player, final String allyName) {
        Alliance alliance = null;
        if (getAllyByName(allyName) == null) {
            final Clan leader = player.getClan();
            alliance = new Alliance(IdFactory.getInstance().getNextId(), allyName, leader);
            alliance.store();
            _alliances.put(alliance.getAllyId(), alliance);
            player.getClan().setAllyId(alliance.getAllyId());
            for (final Player temp : player.getClan().getOnlineMembers(0)) {
                temp.broadcastCharInfo();
            }
        }
        return alliance;
    }

    public void dissolveAlly(final Player player) {
        final int allyId = player.getAllyId();
        for (final Clan member : player.getAlliance().getMembers()) {
            member.setAllyId(0);
            member.broadcastClanStatus(false, true, false);
            member.broadcastToOnlineMembers(Msg.YOU_HAVE_WITHDRAWN_FROM_THE_ALLIANCE);
            member.setLeavedAlly();
        }
        deleteAllyFromDb(allyId);
        _alliances.remove(allyId);
        player.sendPacket(Msg.THE_ALLIANCE_HAS_BEEN_DISSOLVED);
        player.getClan().setDissolvedAlly();
    }

    public void deleteAllyFromDb(final int allyId) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("UPDATE clan_data SET ally_id=0 WHERE ally_id=?");
            statement.setInt(1, allyId);
            statement.execute();
            DbUtils.close(statement);
            statement = con.prepareStatement("DELETE FROM ally_data WHERE ally_id=?");
            statement.setInt(1, allyId);
            statement.execute();
        } catch (Exception e) {
            LOGGER.warn("could not dissolve clan:" + e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void startClanWar(final Clan clan1, final Clan clan2) {
        clan1.setEnemyClan(clan2);
        clan2.setAttackerClan(clan1);
        clan1.broadcastClanStatus(false, false, true);
        clan2.broadcastClanStatus(false, false, true);
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("REPLACE INTO clan_wars (clan1, clan2) VALUES(?,?)");
            statement.setInt(1, clan1.getClanId());
            statement.setInt(2, clan2.getClanId());
            statement.execute();
        } catch (Exception e) {
            LOGGER.warn("could not store clan war data:" + e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
        clan1.broadcastToOnlineMembers(new SystemMessage(1562).addString(clan2.getName()));
        clan2.broadcastToOnlineMembers(new SystemMessage(1561).addString(clan1.getName()));
    }

    public void stopClanWar(final Clan clan1, final Clan clan2) {
        clan1.deleteEnemyClan(clan2);
        clan2.deleteAttackerClan(clan1);
        clan1.broadcastClanStatus(false, false, true);
        clan2.broadcastClanStatus(false, false, true);
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("DELETE FROM clan_wars WHERE clan1=? AND clan2=?");
            statement.setInt(1, clan1.getClanId());
            statement.setInt(2, clan2.getClanId());
            statement.execute();
        } catch (Exception e) {
            LOGGER.warn("could not delete war data:" + e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
        clan1.broadcastToOnlineMembers(new SystemMessage(1567).addString(clan2.getName()));
        clan2.broadcastToOnlineMembers(new SystemMessage(1566).addString(clan1.getName()));
    }

    private void restoreWars() {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT clan1, clan2 FROM clan_wars");
            rset = statement.executeQuery();
            while (rset.next()) {
                final Clan clan1 = getClan(rset.getInt("clan1"));
                final Clan clan2 = getClan(rset.getInt("clan2"));
                if (clan1 != null && clan2 != null) {
                    clan1.setEnemyClan(clan2);
                    clan2.setAttackerClan(clan1);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("could not restore clan wars data:");
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
    }

    public void checkClans() {
        final long currentTime = System.currentTimeMillis();
        Arrays.stream(getClans()).filter(clan -> clan.getDisbandEndTime() > 0L && clan.getDisbandEndTime() < currentTime).forEach(this::dissolveClan);
    }

}
