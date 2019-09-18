package ru.j2dev.gameserver.model.entity.SevenSignsFestival;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Party;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.base.Experience;
import ru.j2dev.gameserver.model.entity.SevenSigns;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PledgeShowInfoUpdate;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.tables.ClanTable;
import ru.j2dev.gameserver.templates.StatsSet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

public class SevenSignsFestival {
    public static final int FESTIVAL_MANAGER_START = 120000;
    public static final int FESTIVAL_LENGTH = 1080000;
    public static final int FESTIVAL_CYCLE_LENGTH = 2280000;
    public static final int FESTIVAL_SIGNUP_TIME = 1200000;
    public static final int FESTIVAL_FIRST_SPAWN = 120000;
    public static final int FESTIVAL_FIRST_SWARM = 300000;
    public static final int FESTIVAL_SECOND_SPAWN = 540000;
    public static final int FESTIVAL_SECOND_SWARM = 720000;
    public static final int FESTIVAL_CHEST_SPAWN = 900000;
    public static final int FESTIVAL_COUNT = 5;
    public static final int FESTIVAL_LEVEL_MAX_31 = 0;
    public static final int FESTIVAL_LEVEL_MAX_42 = 1;
    public static final int FESTIVAL_LEVEL_MAX_53 = 2;
    public static final int FESTIVAL_LEVEL_MAX_64 = 3;
    public static final int FESTIVAL_LEVEL_MAX_NONE = 4;
    public static final int[] FESTIVAL_LEVEL_SCORES = {60, 70, 100, 120, 150};
    public static final int FESTIVAL_BLOOD_OFFERING = 5901;
    public static final int FESTIVAL_OFFERING_VALUE = 1;
    private static final Logger LOGGER = LoggerFactory.getLogger(SevenSignsFestival.class);
    private static final SevenSigns _signsInstance = SevenSigns.getInstance();
    private static SevenSignsFestival _instance;
    private static boolean _festivalInitialized;
    private static long[] _accumulatedBonuses;
    private static Map<Integer, Long> _dawnFestivalScores;
    private static Map<Integer, Long> _duskFestivalScores;

    private final Map<Integer, Map<Integer, StatsSet>> _festivalData;

    public SevenSignsFestival() {
        _accumulatedBonuses = new long[5];
        _dawnFestivalScores = new ConcurrentHashMap<>();
        _duskFestivalScores = new ConcurrentHashMap<>();
        _festivalData = new ConcurrentHashMap<>();
        restoreFestivalData();
    }

    public static SevenSignsFestival getInstance() {
        if (_instance == null) {
            _instance = new SevenSignsFestival();
        }
        return _instance;
    }

    public static String getFestivalName(final int festivalID) {
        switch (festivalID) {
            case 0: {
                return "31";
            }
            case 1: {
                return "42";
            }
            case 2: {
                return "53";
            }
            case 3: {
                return "64";
            }
            default: {
                return "No Level Limit";
            }
        }
    }

    public static int getMaxLevelForFestival(final int festivalId) {
        switch (festivalId) {
            case 0: {
                return 31;
            }
            case 1: {
                return 42;
            }
            case 2: {
                return 53;
            }
            case 3: {
                return 64;
            }
            default: {
                return Experience.getMaxLevel();
            }
        }
    }

    public static int getStoneCount(final int festivalId, final int stoneId) {
        switch (festivalId) {
            case 0: {
                if (stoneId == 6360) {
                    return 900;
                }
                if (stoneId == 6361) {
                    return 520;
                }
                return 270;
            }
            case 1: {
                if (stoneId == 6360) {
                    return 1500;
                }
                if (stoneId == 6361) {
                    return 900;
                }
                return 450;
            }
            case 2: {
                if (stoneId == 6360) {
                    return 3000;
                }
                if (stoneId == 6361) {
                    return 1500;
                }
                return 900;
            }
            case 3: {
                if (stoneId == 6360) {
                    return 1500;
                }
                if (stoneId == 6361) {
                    return 2700;
                }
                return 1350;
            }
            case 4: {
                if (stoneId == 6360) {
                    return 6000;
                }
                if (stoneId == 6361) {
                    return 3600;
                }
                return 1800;
            }
            default: {
                return 0;
            }
        }
    }

    public static String implodeString(final List<?> strArray) {
        final StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < strArray.size()) {
            final Object o = strArray.get(i);
            if (o instanceof Player) {
                sb.append(((Player) o).getName());
            } else {
                sb.append(o);
            }
            if (++i < strArray.size()) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    private void restoreFestivalData() {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT festivalId, cabal, cycle, date, score, members, names FROM seven_signs_festival");
            rset = statement.executeQuery();
            while (rset.next()) {
                final int cycle = _signsInstance.getCurrentCycle();
                int festivalId = rset.getInt("festivalId");
                final int cabal = SevenSigns.getCabalNumber(rset.getString("cabal"));
                final StatsSet festivalDat = new StatsSet();
                festivalDat.set("festivalId", festivalId);
                festivalDat.set("cabal", cabal);
                festivalDat.set("cycle", cycle);
                festivalDat.set("date", rset.getString("date"));
                festivalDat.set("score", rset.getInt("score"));
                festivalDat.set("members", rset.getString("members"));
                festivalDat.set("names", rset.getString("names"));
                if (cabal == 2) {
                    festivalId += 5;
                }
                Map<Integer, StatsSet> tempData = _festivalData.get(cycle);
                if (tempData == null) {
                    tempData = new TreeMap<>();
                }
                tempData.put(festivalId, festivalDat);
                _festivalData.put(cycle, tempData);
            }
            DbUtils.close(statement, rset);
            final StringBuilder query = new StringBuilder("SELECT festival_cycle, ");
            IntStream.range(0, 4).forEach(i -> query.append("accumulated_bonus").append(String.valueOf(i)).append(", "));
            query.append("accumulated_bonus").append(4).append(" ");
            query.append("FROM seven_signs_status");
            statement = con.prepareStatement(query.toString());
            rset = statement.executeQuery();
            while (rset.next()) {
                for (int i = 0; i < 5; ++i) {
                    _accumulatedBonuses[i] = rset.getInt("accumulated_bonus" + String.valueOf(i));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("SevenSignsFestival: Failed to load configuration: " + e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
    }

    public synchronized void saveFestivalData(final boolean updateSettings) {
        Connection con = null;
        PreparedStatement statement = null;
        PreparedStatement statement2 = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("UPDATE seven_signs_festival SET date=?, score=?, members=?, names=? WHERE cycle=? AND cabal=? AND festivalId=?");
            statement2 = con.prepareStatement("INSERT INTO seven_signs_festival (festivalId, cabal, cycle, date, score, members, names) VALUES (?,?,?,?,?,?,?)");
            for (final Map<Integer, StatsSet> currCycleData : _festivalData.values()) {
                for (final StatsSet festivalDat : currCycleData.values()) {
                    final int festivalCycle = festivalDat.getInteger("cycle");
                    final int festivalId = festivalDat.getInteger("festivalId");
                    final String cabal = SevenSigns.getCabalShortName(festivalDat.getInteger("cabal"));
                    statement.setLong(1, Long.parseLong(festivalDat.getString("date")));
                    statement.setInt(2, festivalDat.getInteger("score"));
                    statement.setString(3, festivalDat.getString("members"));
                    statement.setString(4, festivalDat.getString("names", ""));
                    statement.setInt(5, festivalCycle);
                    statement.setString(6, cabal);
                    statement.setInt(7, festivalId);
                    final boolean update = statement.executeUpdate() > 0;
                    if (update) {
                        continue;
                    }
                    statement2.setInt(1, festivalId);
                    statement2.setString(2, cabal);
                    statement2.setInt(3, festivalCycle);
                    statement2.setLong(4, Long.parseLong(festivalDat.getString("date")));
                    statement2.setInt(5, festivalDat.getInteger("score"));
                    statement2.setString(6, festivalDat.getString("members"));
                    statement2.setString(7, festivalDat.getString("names", ""));
                    statement2.execute();
                }
            }
        } catch (Exception e) {
            LOGGER.error("SevenSignsFestival: Failed to save configuration!", e);
        } finally {
            DbUtils.closeQuietly(statement2);
            DbUtils.closeQuietly(con, statement);
        }
        if (updateSettings) {
            _signsInstance.saveSevenSignsData(0, true);
        }
    }

    public void rewardHighestRanked() {
        IntStream.range(0, 5).
                mapToObj(this::getOverallHighestScoreData).
                filter(Objects::nonNull).
                map(overallData -> overallData.getString("members").split(",")).
                forEach(partyMembers -> Arrays.stream(partyMembers).
                        forEach(this::addReputationPointsForPartyMemberClan));
    }

    private void addReputationPointsForPartyMemberClan(final String playerId) {
        final Player player = GameObjectsStorage.getPlayer(Integer.parseInt(playerId));
        if (player != null) {
            if (player.getClan() != null) {
                player.getClan().incReputation(100, true, "SevenSignsFestival");
                final SystemMessage sm = new SystemMessage(1775);
                sm.addName(player);
                sm.addNumber(100);
                player.getClan().broadcastToOnlineMembers(sm);
            }
        } else {
            Connection con = null;
            PreparedStatement statement = null;
            ResultSet rset = null;
            try {
                con = DatabaseFactory.getInstance().getConnection();
                statement = con.prepareStatement("SELECT char_name, clanid FROM characters WHERE obj_Id = ?");
                statement.setString(1, playerId);
                rset = statement.executeQuery();
                if (rset.next()) {
                    final int clanId = rset.getInt("clanid");
                    if (clanId > 0) {
                        final Clan clan = ClanTable.getInstance().getClan(clanId);
                        if (clan != null) {
                            clan.incReputation(100, true, "SevenSignsFestival");
                            clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
                            final SystemMessage sm2 = new SystemMessage(1775);
                            sm2.addString(rset.getString("char_name"));
                            sm2.addNumber(100);
                            clan.broadcastToOnlineMembers(sm2);
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("could not get clan name of " + playerId + ": " + e);
                LOGGER.error("", e);
            } finally {
                DbUtils.closeQuietly(con, statement, rset);
            }
        }
    }

    public void resetFestivalData(final boolean updateSettings) {
        IntStream.range(0, 5).forEach(i -> _accumulatedBonuses[i] = 0L);
        _dawnFestivalScores.clear();
        _duskFestivalScores.clear();
        final Map<Integer, StatsSet> newData = new TreeMap<>();
        for (int j = 0; j < 10; ++j) {
            int festivalId;
            if ((festivalId = j) >= 5) {
                festivalId -= 5;
            }
            final StatsSet tempStats = new StatsSet();
            tempStats.set("festivalId", festivalId);
            tempStats.set("cycle", _signsInstance.getCurrentCycle());
            tempStats.set("date", "0");
            tempStats.set("score", 0);
            tempStats.set("members", "");
            if (j >= 5) {
                tempStats.set("cabal", 2);
            } else {
                tempStats.set("cabal", 1);
            }
            newData.put(j, tempStats);
        }
        _festivalData.put(_signsInstance.getCurrentCycle(), newData);
        saveFestivalData(updateSettings);
        for (final Player onlinePlayer : GameObjectsStorage.getAllPlayers()) {
            Functions.removeItem(onlinePlayer, 5901, Functions.getItemCount(onlinePlayer, 5901));
        }
        LOGGER.info("SevenSignsFestival: Reinitialized engine for next competition period.");
    }

    public boolean isFestivalInitialized() {
        return _festivalInitialized;
    }

    public static void setFestivalInitialized(final boolean isInitialized) {
        _festivalInitialized = isInitialized;
    }

    public String getTimeToNextFestivalStr() {
        if (_signsInstance.isSealValidationPeriod()) {
            return "<font color=\"FF0000\">This is the Seal Validation period. Festivals will resume next week.</font>";
        }
        return "<font color=\"FF0000\">The next festival is ready to start.</font>";
    }

    public long getHighestScore(final int oracle, final int festivalId) {
        return getHighestScoreData(oracle, festivalId).getLong("score");
    }

    public StatsSet getHighestScoreData(final int oracle, final int festivalId) {
        int offsetId = festivalId;
        if (oracle == 2) {
            offsetId += 5;
        }
        StatsSet currData = null;
        try {
            currData = _festivalData.get(_signsInstance.getCurrentCycle()).get(offsetId);
        } catch (Exception e) {
            LOGGER.info("SSF: Error while getting scores");
            LOGGER.info("oracle=" + oracle + " festivalId=" + festivalId + " offsetId" + offsetId + " _signsCycle" + _signsInstance.getCurrentCycle());
            LOGGER.info("_festivalData=" + _festivalData);
            LOGGER.error("", e);
        }
        if (currData == null) {
            currData = new StatsSet();
            currData.set("score", 0);
            currData.set("members", "");
            LOGGER.warn("SevenSignsFestival: Data missing for " + SevenSigns.getCabalName(oracle) + ", FestivalID = " + festivalId + " (Current Cycle " + _signsInstance.getCurrentCycle() + ")");
        }
        return currData;
    }

    public StatsSet getOverallHighestScoreData(final int festivalId) {
        StatsSet result = null;
        int highestScore = 0;
        for (final Map<Integer, StatsSet> currCycleData : _festivalData.values()) {
            for (final StatsSet currFestData : currCycleData.values()) {
                final int currFestID = currFestData.getInteger("festivalId");
                final int festivalScore = currFestData.getInteger("score");
                if (currFestID != festivalId) {
                    continue;
                }
                if (festivalScore <= highestScore) {
                    continue;
                }
                highestScore = festivalScore;
                result = currFestData;
            }
        }
        return result;
    }

    public boolean setFinalScore(final Party party, final int oracle, final int festivalId, final long offeringScore) {
        final List<Integer> partyMemberIds = party.getPartyMembersObjIds();
        final List<Player> partyMembers = party.getPartyMembers();
        final long currDawnHighScore = getHighestScore(2, festivalId);
        final long currDuskHighScore = getHighestScore(1, festivalId);
        long thisCabalHighScore;
        long otherCabalHighScore;
        if (oracle == 2) {
            thisCabalHighScore = currDawnHighScore;
            otherCabalHighScore = currDuskHighScore;
            _dawnFestivalScores.put(festivalId, offeringScore);
        } else {
            thisCabalHighScore = currDuskHighScore;
            otherCabalHighScore = currDawnHighScore;
            _duskFestivalScores.put(festivalId, offeringScore);
        }
        final StatsSet currFestData = getHighestScoreData(oracle, festivalId);
        if (offeringScore > thisCabalHighScore) {
            currFestData.set("date", String.valueOf(System.currentTimeMillis()));
            currFestData.set("score", offeringScore);
            currFestData.set("members", implodeString(partyMemberIds));
            currFestData.set("names", implodeString(partyMembers));
            if (offeringScore > otherCabalHighScore) {
                _signsInstance.updateFestivalScore();
            }
            saveFestivalData(true);
            return true;
        }
        return false;
    }

    public long getAccumulatedBonus(final int festivalId) {
        return _accumulatedBonuses[festivalId];
    }

    public void addAccumulatedBonus(final int festivalId, final int stoneType, final long stoneAmount) {
        int eachStoneBonus = 0;
        switch (stoneType) {
            case 6360: {
                eachStoneBonus = 3;
                break;
            }
            case 6361: {
                eachStoneBonus = 5;
                break;
            }
            case 6362: {
                eachStoneBonus = 10;
                break;
            }
        }
        final long[] accumulatedBonuses = _accumulatedBonuses;
        accumulatedBonuses[festivalId] += stoneAmount * eachStoneBonus;
    }

    public void distribAccumulatedBonus() {
        final long[][] result = new long[5][];
        long draw_count = 0L;
        long draw_score = 0L;
        for (int i = 0; i < 5; ++i) {
            final long dawnHigh = getHighestScore(2, i);
            final long duskHigh = getHighestScore(1, i);
            if (dawnHigh > duskHigh) {
                result[i] = new long[]{2L, dawnHigh};
            } else if (duskHigh > dawnHigh) {
                result[i] = new long[]{1L, duskHigh};
            } else {
                result[i] = new long[]{0L, dawnHigh};
                ++draw_count;
                draw_score += _accumulatedBonuses[i];
            }
        }
        for (int i = 0; i < 5; ++i) {
            if (result[i][0] != 0L) {
                final StatsSet high = getHighestScoreData((int) result[i][0], i);
                final String membersString = high.getString("members");
                final long add = (draw_count > 0L) ? (draw_score / draw_count) : 0L;
                final String[] members = membersString.split(",");
                final long count = (_accumulatedBonuses[i] + add) / members.length;
                for (final String pIdStr : members) {
                    SevenSigns.getInstance().addPlayerStoneContrib(Integer.parseInt(pIdStr), 0L, 0L, count / 10L);
                }
            }
        }
    }
}
