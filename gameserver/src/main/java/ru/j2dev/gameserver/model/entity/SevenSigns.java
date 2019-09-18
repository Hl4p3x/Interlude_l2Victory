package ru.j2dev.gameserver.model.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.commons.listener.ListenerList;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.GameServer;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.listener.GameListener;
import ru.j2dev.gameserver.listener.game.OnSSPeriodListener;
import ru.j2dev.gameserver.listener.game.OnStartListener;
import ru.j2dev.gameserver.manager.RaidBossSpawnManager;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SSQInfo;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.templates.StatsSet;
import ru.j2dev.gameserver.utils.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.IntStream;

public class SevenSigns {
    public static final String SEVEN_SIGNS_HTML_PATH = "seven_signs/";
    public static final int CABAL_NULL = 0;
    public static final int CABAL_DUSK = 1;
    public static final int CABAL_DAWN = 2;
    public static final int SEAL_NULL = 0;
    public static final int SEAL_AVARICE = 1;
    public static final int SEAL_GNOSIS = 2;
    public static final int SEAL_STRIFE = 3;
    public static final int PERIOD_COMP_RECRUITING = 0;
    public static final int PERIOD_COMPETITION = 1;
    public static final int PERIOD_COMP_RESULTS = 2;
    public static final int PERIOD_SEAL_VALIDATION = 3;
    public static final int PERIOD_START_HOUR = 18;
    public static final int PERIOD_START_MINS = 0;
    public static final int PERIOD_START_DAY = 2;
    public static final int PERIOD_MINOR_LENGTH = 900000;
    public static final int PERIOD_MAJOR_LENGTH = 603900000;
    public static final int ANCIENT_ADENA_ID = 5575;
    public static final int RECORD_SEVEN_SIGNS_ID = 5707;
    public static final int CERTIFICATE_OF_APPROVAL_ID = 6388;
    public static final int RECORD_SEVEN_SIGNS_COST = 500;
    public static final int ADENA_JOIN_DAWN_COST = 50000;
    public static final Set<Integer> ORATOR_NPC_IDS = new HashSet<>(Arrays.asList(31093, 31172, 31174, 31176, 31178, 31180, 31182, 31184, 31186, 31188, 31190, 31192, 31194, 31196, 31198, 31200, 31231, 31232, 31233, 31234, 31235, 31236, 31237, 31238, 31239, 31240, 31241, 31242, 31243, 31244, 31245, 31246, 31713, 31714, 31715, 31716, 31717, 31718, 31719, 31720));
    public static final Set<Integer> PREACHER_NPC_IDS = new HashSet<>(Arrays.asList(31094, 31173, 31175, 31177, 31179, 31181, 31183, 31185, 31187, 31189, 31191, 31193, 31195, 31197, 31199, 31201, 31247, 31248, 31249, 31250, 31251, 31252, 31253, 31254, 31721, 31722, 31723, 31724, 31725, 31726, 31727, 31728, 32003, 32004, 32005, 32006));
    public static final int SEAL_STONE_BLUE_ID = 6360;
    public static final int SEAL_STONE_GREEN_ID = 6361;
    public static final int SEAL_STONE_RED_ID = 6362;
    public static final int SEAL_STONE_BLUE_VALUE = 3;
    public static final int SEAL_STONE_GREEN_VALUE = 5;
    public static final int SEAL_STONE_RED_VALUE = 10;
    public static final int BLUE_CONTRIB_POINTS = 3;
    public static final int GREEN_CONTRIB_POINTS = 5;
    public static final int RED_CONTRIB_POINTS = 10;
    public static final long MAXIMUM_PLAYER_CONTRIB = Math.round(1000000.0 * Config.RATE_DROP_ITEMS);
    private static final Logger LOGGER = LoggerFactory.getLogger(SevenSigns.class);
    private static SevenSigns _instance;

    private final Calendar _calendar = Calendar.getInstance();
    private final Map<Integer, StatsSet> _signsPlayerData = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> _signsSealOwners = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> _signsDuskSealTotals = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> _signsDawnSealTotals = new ConcurrentHashMap<>();
    private final SSListenerList _listenerList = new SSListenerList();
    protected int _activePeriod;
    protected int _currentCycle;
    protected long _dawnStoneScore;
    protected long _duskStoneScore;
    protected long _dawnFestivalScore;
    protected long _duskFestivalScore;
    protected int _compWinner;
    protected int _previousWinner;
    private ScheduledFuture<?> _periodChange;

    public SevenSigns() {
        GameServer.getInstance().addListener(new OnStartListenerImpl());
        try {
            restoreSevenSignsData();
        } catch (Exception e) {
            LOGGER.error("SevenSigns: Failed to load configuration: " + e);
            LOGGER.error("", e);
        }
        LOGGER.info("SevenSigns: Currently in the " + getCurrentPeriodName() + " period!");
        initializeSeals();
        if (isSealValidationPeriod()) {
            if (getCabalHighestScore() == 0) {
                LOGGER.info("SevenSigns: The Competition last week ended with a tie.");
            } else {
                LOGGER.info("SevenSigns: The " + getCabalName(getCabalHighestScore()) + " were victorious last week.");
            }
        } else if (getCabalHighestScore() == 0) {
            LOGGER.info("SevenSigns: The Competition this week, if the trend continue, will end with a tie.");
        } else {
            LOGGER.info("SevenSigns: The " + getCabalName(getCabalHighestScore()) + " are in the lead this week.");
        }
        int numMins = 0;
        int numHours = 0;
        int numDays = 0;
        setCalendarForNextPeriodChange();
        long milliToChange = getMilliToPeriodChange();
        if (milliToChange < 10L) {
            milliToChange = 10L;
        }
        _periodChange = ThreadPoolManager.getInstance().schedule(new SevenSignsPeriodChange(), milliToChange);
        final double numSecs = milliToChange / 1000L % 60L;
        double countDown = (milliToChange / 1000L - numSecs) / 60.0;
        numMins = (int) Math.floor(countDown % 60.0);
        countDown = (countDown - numMins) / 60.0;
        numHours = (int) Math.floor(countDown % 24.0);
        numDays = (int) Math.floor((countDown - numHours) / 24.0);
        LOGGER.info("SevenSigns: Next period begins in " + numDays + " days, " + numHours + " hours and " + numMins + " mins.");
        if (Config.SS_ANNOUNCE_PERIOD > 0) {
            ThreadPoolManager.getInstance().schedule(new SevenSignsAnnounce(), Config.SS_ANNOUNCE_PERIOD * 1000L * 60L);
        }
    }

    public static SevenSigns getInstance() {
        if (_instance == null) {
            _instance = new SevenSigns();
        }
        return _instance;
    }

    public static long calcContributionScore(final long blueCount, final long greenCount, final long redCount) {
        long contrib = blueCount * BLUE_CONTRIB_POINTS;
        contrib += greenCount * GREEN_CONTRIB_POINTS;
        contrib += redCount * RED_CONTRIB_POINTS;

        return contrib;
    }

    public static long calcAncientAdenaReward(final long blueCount, final long greenCount, final long redCount) {
        long reward = blueCount * SEAL_STONE_BLUE_VALUE;
        reward += greenCount * SEAL_STONE_GREEN_VALUE;
        reward += redCount * SEAL_STONE_RED_VALUE;

        return reward;
    }

    public static int getCabalNumber(final String cabal) {
        if ("dawn".equalsIgnoreCase(cabal)) {
            return CABAL_DAWN;
        } else if ("dusk".equalsIgnoreCase(cabal)) {
            return CABAL_DUSK;
        } else {
            return CABAL_NULL;
        }
    }

    public static String getCabalShortName(final int cabal) {
        switch (cabal) {
            case CABAL_DAWN:
                return "dawn";
            case CABAL_DUSK:
                return "dusk";
        }
        return "No Cabal";
    }

    public static String getCabalName(final int cabal) {
        switch (cabal) {
            case CABAL_DAWN:
                return "Lords of Dawn";
            case CABAL_DUSK:
                return "Revolutionaries of Dusk";
        }
        return "No Cabal";
    }

    public static String getSealName(final int seal, final boolean shortName) {
        String sealName = !shortName ? "Seal of " : "";

        switch (seal) {
            case SEAL_AVARICE:
                sealName += "Avarice";
                break;
            case SEAL_GNOSIS:
                sealName += "Gnosis";
                break;
            case SEAL_STRIFE:
                sealName += "Strife";
                break;
        }
        return sealName;
    }

    public static String capitalizeWords(final String str) {
        final char[] charArray = str.toCharArray();
        final StringBuilder buf = new StringBuilder();
        charArray[0] = Character.toUpperCase(charArray[0]);
        IntStream.range(0, charArray.length).forEach(i -> {
            if (Character.isWhitespace(charArray[i]) && i != charArray.length - 1) {
                charArray[i + 1] = Character.toUpperCase(charArray[i + 1]);
            }
            buf.append(Character.toString(charArray[i]));
        });
        return buf.toString();
    }

    private static void processStatement(final PreparedStatement statement, final StatsSet sevenDat) throws SQLException {
        statement.setString(1, getCabalShortName(sevenDat.getInteger("cabal")));
        statement.setInt(2, sevenDat.getInteger("seal"));
        statement.setInt(3, sevenDat.getInteger("dawn_red_stones"));
        statement.setInt(4, sevenDat.getInteger("dawn_green_stones"));
        statement.setInt(5, sevenDat.getInteger("dawn_blue_stones"));
        statement.setInt(6, sevenDat.getInteger("dawn_ancient_adena_amount"));
        statement.setInt(7, sevenDat.getInteger("dawn_contribution_score"));
        statement.setInt(8, sevenDat.getInteger("dusk_red_stones"));
        statement.setInt(9, sevenDat.getInteger("dusk_green_stones"));
        statement.setInt(10, sevenDat.getInteger("dusk_blue_stones"));
        statement.setInt(11, sevenDat.getInteger("dusk_ancient_adena_amount"));
        statement.setInt(12, sevenDat.getInteger("dusk_contribution_score"));
        statement.setInt(13, sevenDat.getInteger("char_obj_id"));
        statement.executeUpdate();
    }

    public final int getCurrentCycle() {
        return _currentCycle;
    }

    public final int getCurrentPeriod() {
        return _activePeriod;
    }

    private int getDaysToPeriodChange() {
        final int numDays = _calendar.get(Calendar.DAY_OF_WEEK) - 2;
        if (numDays < 0) {
            return 0 - numDays;
        }
        return 7 - numDays;
    }

    public final long getMilliToPeriodChange() {
        return _calendar.getTimeInMillis() - System.currentTimeMillis();
    }

    protected void setCalendarForNextPeriodChange() {
        switch (getCurrentPeriod()) {
            case PERIOD_SEAL_VALIDATION:
            case PERIOD_COMPETITION: {
                int daysToChange = getDaysToPeriodChange();
                if (daysToChange == 7) {
                    if (_calendar.get(Calendar.HOUR_OF_DAY) < 18) {
                        daysToChange = 0;
                    } else if (_calendar.get(Calendar.HOUR_OF_DAY) == 18 && _calendar.get(Calendar.MINUTE) < 0) {
                        daysToChange = 0;
                    }
                }
                if (daysToChange > 0) {
                    _calendar.add(Calendar.DATE, daysToChange);
                }
                _calendar.set(Calendar.HOUR_OF_DAY, 18);
                _calendar.set(Calendar.MINUTE, 0);
                break;
            }
            case PERIOD_COMP_RECRUITING:
            case PERIOD_COMP_RESULTS: {
                _calendar.add(Calendar.MILLISECOND, 900000);
                break;
            }
        }
    }

    public final String getCurrentPeriodName() {
        String periodName = null;

        switch (_activePeriod) {
            case PERIOD_COMP_RECRUITING:
                periodName = "Quest Event Initialization";
                break;
            case PERIOD_COMPETITION:
                periodName = "Competition (Quest Event)";
                break;
            case PERIOD_COMP_RESULTS:
                periodName = "Quest Event Results";
                break;
            case PERIOD_SEAL_VALIDATION:
                periodName = "Seal Validation";
                break;
        }
        return periodName;
    }

    public final boolean isSealValidationPeriod() {
        return _activePeriod == 3;
    }

    public final boolean isCompResultsPeriod() {
        return _activePeriod == 2;
    }

    public final long getCurrentScore(final int cabal) {
        final double totalStoneScore = _dawnStoneScore + _duskStoneScore;

        switch (cabal) {
            case CABAL_NULL:
                return 0;
            case CABAL_DAWN:
                return Math.round(_dawnStoneScore / (totalStoneScore == 0 ? 1 : totalStoneScore) * 500) + _dawnFestivalScore;
            case CABAL_DUSK:
                return Math.round(_duskStoneScore / (totalStoneScore == 0 ? 1 : totalStoneScore) * 500) + _duskFestivalScore;
        }
        return 0;
    }

    public final long getCurrentStoneScore(final int cabal) {
        switch (cabal) {
            case CABAL_NULL:
                return 0;
            case CABAL_DAWN:
                return _dawnStoneScore;
            case CABAL_DUSK:
                return _duskStoneScore;
        }
        return 0;
    }

    public final long getCurrentFestivalScore(final int cabal) {
        switch (cabal) {
            case CABAL_NULL:
                return 0;
            case CABAL_DAWN:
                return _dawnFestivalScore;
            case CABAL_DUSK:
                return _duskFestivalScore;
        }
        return 0;
    }

    public final int getCabalHighestScore() {
        final long diff = getCurrentScore(CABAL_DUSK) - getCurrentScore(CABAL_DAWN);
        if (diff == 0) {
            return CABAL_NULL;
        } else if (diff > 0) {
            return CABAL_DUSK;
        }

        return CABAL_DAWN;
    }

    public final int getSealOwner(final int seal) {
        if (_signsSealOwners == null || !_signsSealOwners.containsKey(seal)) {
            return CABAL_NULL;
        }
        return _signsSealOwners.get(seal);
    }

    public final int getSealProportion(final int seal, final int cabal) {
        switch (cabal) {
            case CABAL_NULL:
                return 0;
            case CABAL_DUSK:
                return _signsDuskSealTotals.get(seal);
            default:
                return _signsDawnSealTotals.get(seal);
        }
    }

    public final int getTotalMembers(final int cabal) {
        return (int) _signsPlayerData.values().stream().filter(sevenDat -> sevenDat.getInteger("cabal") == cabal).count();
    }

    public final StatsSet getPlayerStatsSet(final Player player) {
        if (!hasRegisteredBefore(player.getObjectId())) {
            return null;
        }
        return _signsPlayerData.get(player.getObjectId());
    }

    public long getPlayerStoneContrib(final Player player) {
        if (!hasRegisteredBefore(player.getObjectId())) {
            return 0L;
        }
        long stoneCount = 0L;
        final StatsSet currPlayer = _signsPlayerData.get(player.getObjectId());
        if (getPlayerCabal(player) == 2) {
            stoneCount += currPlayer.getLong("dawn_red_stones");
            stoneCount += currPlayer.getLong("dawn_green_stones");
            stoneCount += currPlayer.getLong("dawn_blue_stones");
        } else {
            stoneCount += currPlayer.getLong("dusk_red_stones");
            stoneCount += currPlayer.getLong("dusk_green_stones");
            stoneCount += currPlayer.getLong("dusk_blue_stones");
        }
        return stoneCount;
    }

    public long getPlayerContribScore(final Player player) {
        if (!hasRegisteredBefore(player.getObjectId())) {
            return 0L;
        }
        final StatsSet currPlayer = _signsPlayerData.get(player.getObjectId());
        if (getPlayerCabal(player) == 2) {
            return currPlayer.getInteger("dawn_contribution_score");
        }
        return currPlayer.getInteger("dusk_contribution_score");
    }

    public long getPlayerAdenaCollect(final Player player) {
        if (!hasRegisteredBefore(player.getObjectId())) {
            return 0L;
        }
        return _signsPlayerData.get(player.getObjectId()).getLong((getPlayerCabal(player) == 2) ? "dawn_ancient_adena_amount" : "dusk_ancient_adena_amount");
    }

    public int getPlayerSeal(final Player player) {
        if (!hasRegisteredBefore(player.getObjectId())) {
            return SEAL_NULL;
        }
        return _signsPlayerData.get(player.getObjectId()).getInteger("seal");
    }

    public int getPlayerCabal(final Player player) {
        if (!hasRegisteredBefore(player.getObjectId())) {
            return CABAL_NULL;
        }
        return _signsPlayerData.get(player.getObjectId()).getInteger("cabal");
    }

    protected void restoreSevenSignsData() {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT char_obj_id, cabal, seal, dawn_red_stones, dawn_green_stones, dawn_blue_stones, dawn_ancient_adena_amount, dawn_contribution_score, dusk_red_stones, dusk_green_stones, dusk_blue_stones, dusk_ancient_adena_amount, dusk_contribution_score FROM seven_signs");
            rset = statement.executeQuery();
            while (rset.next()) {
                final int charObjId = rset.getInt("char_obj_id");
                final StatsSet sevenDat = new StatsSet();
                sevenDat.set("char_obj_id", charObjId);
                sevenDat.set("cabal", getCabalNumber(rset.getString("cabal")));
                sevenDat.set("seal", rset.getInt("seal"));
                sevenDat.set("dawn_red_stones", rset.getInt("dawn_red_stones"));
                sevenDat.set("dawn_green_stones", rset.getInt("dawn_green_stones"));
                sevenDat.set("dawn_blue_stones", rset.getInt("dawn_blue_stones"));
                sevenDat.set("dawn_ancient_adena_amount", rset.getInt("dawn_ancient_adena_amount"));
                sevenDat.set("dawn_contribution_score", rset.getInt("dawn_contribution_score"));
                sevenDat.set("dusk_red_stones", rset.getInt("dusk_red_stones"));
                sevenDat.set("dusk_green_stones", rset.getInt("dusk_green_stones"));
                sevenDat.set("dusk_blue_stones", rset.getInt("dusk_blue_stones"));
                sevenDat.set("dusk_ancient_adena_amount", rset.getInt("dusk_ancient_adena_amount"));
                sevenDat.set("dusk_contribution_score", rset.getInt("dusk_contribution_score"));
                _signsPlayerData.put(charObjId, sevenDat);
            }
            DbUtils.close(statement, rset);
            statement = con.prepareStatement("SELECT * FROM seven_signs_status");
            rset = statement.executeQuery();
            while (rset.next()) {
                _currentCycle = rset.getInt("current_cycle");
                _activePeriod = rset.getInt("active_period");
                _previousWinner = rset.getInt("previous_winner");
                _dawnStoneScore = rset.getLong("dawn_stone_score");
                _dawnFestivalScore = rset.getLong("dawn_festival_score");
                _duskStoneScore = rset.getLong("dusk_stone_score");
                _duskFestivalScore = rset.getLong("dusk_festival_score");
                _signsSealOwners.put(1, rset.getInt("avarice_owner"));
                _signsSealOwners.put(2, rset.getInt("gnosis_owner"));
                _signsSealOwners.put(3, rset.getInt("strife_owner"));
                _signsDawnSealTotals.put(1, rset.getInt("avarice_dawn_score"));
                _signsDawnSealTotals.put(2, rset.getInt("gnosis_dawn_score"));
                _signsDawnSealTotals.put(3, rset.getInt("strife_dawn_score"));
                _signsDuskSealTotals.put(1, rset.getInt("avarice_dusk_score"));
                _signsDuskSealTotals.put(2, rset.getInt("gnosis_dusk_score"));
                _signsDuskSealTotals.put(3, rset.getInt("strife_dusk_score"));
            }
            DbUtils.close(statement, rset);
            statement = con.prepareStatement("UPDATE seven_signs_status SET date=?");
            statement.setInt(1, Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
            statement.execute();
        } catch (SQLException e) {
            LOGGER.error("Unable to load Seven Signs Data: " + e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
    }

    public synchronized void saveSevenSignsData(final int playerId, final boolean updateSettings) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("UPDATE seven_signs SET cabal=?, seal=?, dawn_red_stones=?, dawn_green_stones=?, dawn_blue_stones=?, dawn_ancient_adena_amount=?, dawn_contribution_score=?, dusk_red_stones=?, dusk_green_stones=?, dusk_blue_stones=?, dusk_ancient_adena_amount=?, dusk_contribution_score=? WHERE char_obj_id=?");
            if (playerId > 0) {
                processStatement(statement, _signsPlayerData.get(playerId));
            } else {
                for (final StatsSet sevenDat : _signsPlayerData.values()) {
                    processStatement(statement, sevenDat);
                }
            }
            DbUtils.close(statement);
            if (updateSettings) {
                final StringBuilder buf = new StringBuilder();
                buf.append("UPDATE seven_signs_status SET current_cycle=?, active_period=?, previous_winner=?, dawn_stone_score=?, dawn_festival_score=?, dusk_stone_score=?, dusk_festival_score=?, avarice_owner=?, gnosis_owner=?, strife_owner=?, avarice_dawn_score=?, gnosis_dawn_score=?, strife_dawn_score=?, avarice_dusk_score=?, gnosis_dusk_score=?, strife_dusk_score=?, festival_cycle=?, ");
                for (int i = 0; i < 5; ++i) {
                    buf.append("accumulated_bonus").append(String.valueOf(i)).append("=?, ");
                }
                buf.append("date=?");
                statement = con.prepareStatement(buf.toString());
                statement.setInt(1, _currentCycle);
                statement.setInt(2, _activePeriod);
                statement.setInt(3, _previousWinner);
                statement.setLong(4, _dawnStoneScore);
                statement.setLong(5, _dawnFestivalScore);
                statement.setLong(6, _duskStoneScore);
                statement.setLong(7, _duskFestivalScore);
                statement.setInt(8, _signsSealOwners.get(1));
                statement.setInt(9, _signsSealOwners.get(2));
                statement.setInt(10, _signsSealOwners.get(3));
                statement.setInt(11, _signsDawnSealTotals.get(1));
                statement.setInt(12, _signsDawnSealTotals.get(2));
                statement.setInt(13, _signsDawnSealTotals.get(3));
                statement.setInt(14, _signsDuskSealTotals.get(1));
                statement.setInt(15, _signsDuskSealTotals.get(2));
                statement.setInt(16, _signsDuskSealTotals.get(3));
                statement.setInt(17, getCurrentCycle());
                for (int i = 0; i < 5; ++i) {
                    statement.setLong(18 + i, SevenSignsFestival.getInstance().getAccumulatedBonus(i));
                }
                statement.setInt(23, Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            LOGGER.error("Unable to save Seven Signs data: " + e);
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    protected void resetPlayerData() {
        _signsPlayerData.values().forEach(sevenDat -> {
            final int charObjId = sevenDat.getInteger("char_obj_id");
            if (sevenDat.getInteger("cabal") == getCabalHighestScore()) {
                switch (getCabalHighestScore()) {
                    case 2: {
                        sevenDat.set("dawn_red_stones", 0);
                        sevenDat.set("dawn_green_stones", 0);
                        sevenDat.set("dawn_blue_stones", 0);
                        sevenDat.set("dawn_contribution_score", 0);
                        break;
                    }
                    case 1: {
                        sevenDat.set("dusk_red_stones", 0);
                        sevenDat.set("dusk_green_stones", 0);
                        sevenDat.set("dusk_blue_stones", 0);
                        sevenDat.set("dusk_contribution_score", 0);
                        break;
                    }
                }
            } else if (sevenDat.getInteger("cabal") == 2 || sevenDat.getInteger("cabal") == 0) {
                sevenDat.set("dusk_red_stones", 0);
                sevenDat.set("dusk_green_stones", 0);
                sevenDat.set("dusk_blue_stones", 0);
                sevenDat.set("dusk_contribution_score", 0);
            } else if (sevenDat.getInteger("cabal") == 1 || sevenDat.getInteger("cabal") == 0) {
                sevenDat.set("dawn_red_stones", 0);
                sevenDat.set("dawn_green_stones", 0);
                sevenDat.set("dawn_blue_stones", 0);
                sevenDat.set("dawn_contribution_score", 0);
            }
            sevenDat.set("cabal", 0);
            sevenDat.set("seal", 0);
            _signsPlayerData.put(charObjId, sevenDat);
        });
    }

    private boolean hasRegisteredBefore(final int charObjId) {
        return _signsPlayerData.containsKey(charObjId);
    }

    public int setPlayerInfo(final int charObjId, final int chosenCabal, final int chosenSeal) {
        StatsSet currPlayer;
        if (hasRegisteredBefore(charObjId)) {
            currPlayer = _signsPlayerData.get(charObjId);
            currPlayer.set("cabal", chosenCabal);
            currPlayer.set("seal", chosenSeal);
            _signsPlayerData.put(charObjId, currPlayer);
        } else {
            currPlayer = new StatsSet();
            currPlayer.set("char_obj_id", charObjId);
            currPlayer.set("cabal", chosenCabal);
            currPlayer.set("seal", chosenSeal);
            currPlayer.set("dawn_red_stones", 0);
            currPlayer.set("dawn_green_stones", 0);
            currPlayer.set("dawn_blue_stones", 0);
            currPlayer.set("dawn_ancient_adena_amount", 0);
            currPlayer.set("dawn_contribution_score", 0);
            currPlayer.set("dusk_red_stones", 0);
            currPlayer.set("dusk_green_stones", 0);
            currPlayer.set("dusk_blue_stones", 0);
            currPlayer.set("dusk_ancient_adena_amount", 0);
            currPlayer.set("dusk_contribution_score", 0);
            _signsPlayerData.put(charObjId, currPlayer);
            Connection con = null;
            PreparedStatement statement = null;
            try {
                con = DatabaseFactory.getInstance().getConnection();
                statement = con.prepareStatement("INSERT INTO seven_signs (char_obj_id, cabal, seal) VALUES (?,?,?)");
                statement.setInt(1, charObjId);
                statement.setString(2, getCabalShortName(chosenCabal));
                statement.setInt(3, chosenSeal);
                statement.execute();
            } catch (SQLException e) {
                LOGGER.error("SevenSigns: Failed to save data: " + e);
            } finally {
                DbUtils.closeQuietly(con, statement);
            }
        }
        long contribScore;
        switch (chosenCabal) {
            case CABAL_DAWN: {
                contribScore = calcContributionScore(currPlayer.getInteger("dawn_blue_stones"), currPlayer.getInteger("dawn_green_stones"), currPlayer.getInteger("dawn_red_stones"));
                _dawnStoneScore += contribScore;
                break;
            }
            case CABAL_DUSK: {
                contribScore = calcContributionScore(currPlayer.getInteger("dusk_blue_stones"), currPlayer.getInteger("dusk_green_stones"), currPlayer.getInteger("dusk_red_stones"));
                _duskStoneScore += contribScore;
                break;
            }
        }
        if (currPlayer.getInteger("cabal") == CABAL_DAWN) {
            _signsDawnSealTotals.put(chosenSeal, _signsDawnSealTotals.get(chosenSeal) + 1);
        } else {
            _signsDuskSealTotals.put(chosenSeal, _signsDuskSealTotals.get(chosenSeal) + 1);
        }
        saveSevenSignsData(charObjId, true);
        return chosenCabal;
    }

    public int getAncientAdenaReward(final Player player, final boolean removeReward) {
        final int charObjId = player.getObjectId();
        final StatsSet currPlayer = _signsPlayerData.get(charObjId);
        int rewardAmount = 0;
        if (currPlayer.getInteger("cabal") == 2) {
            rewardAmount = currPlayer.getInteger("dawn_ancient_adena_amount");
            currPlayer.set("dawn_ancient_adena_amount", 0);
        } else {
            rewardAmount = currPlayer.getInteger("dusk_ancient_adena_amount");
            currPlayer.set("dusk_ancient_adena_amount", 0);
        }
        if (removeReward) {
            _signsPlayerData.put(charObjId, currPlayer);
            saveSevenSignsData(charObjId, false);
        }
        return rewardAmount;
    }

    public long addPlayerStoneContrib(final Player player, final long blueCount, final long greenCount, final long redCount) {
        return addPlayerStoneContrib(player.getObjectId(), blueCount, greenCount, redCount);
    }

    public long addPlayerStoneContrib(final int charObjId, final long blueCount, final long greenCount, final long redCount) {
        final StatsSet currPlayer = _signsPlayerData.get(charObjId);
        final long contribScore = calcContributionScore(blueCount, greenCount, redCount);
        long totalAncientAdena = 0L;
        long totalContribScore = 0L;
        if (currPlayer.getInteger("cabal") == 2) {
            totalAncientAdena = currPlayer.getInteger("dawn_ancient_adena_amount") + calcAncientAdenaReward(blueCount, greenCount, redCount);
            totalContribScore = currPlayer.getInteger("dawn_contribution_score") + contribScore;
            if (totalContribScore > MAXIMUM_PLAYER_CONTRIB) {
                return -1L;
            }
            currPlayer.set("dawn_red_stones", currPlayer.getInteger("dawn_red_stones") + redCount);
            currPlayer.set("dawn_green_stones", currPlayer.getInteger("dawn_green_stones") + greenCount);
            currPlayer.set("dawn_blue_stones", currPlayer.getInteger("dawn_blue_stones") + blueCount);
            currPlayer.set("dawn_ancient_adena_amount", totalAncientAdena);
            currPlayer.set("dawn_contribution_score", totalContribScore);
            _signsPlayerData.put(charObjId, currPlayer);
            _dawnStoneScore += contribScore;
        } else {
            totalAncientAdena = currPlayer.getInteger("dusk_ancient_adena_amount") + calcAncientAdenaReward(blueCount, greenCount, redCount);
            totalContribScore = currPlayer.getInteger("dusk_contribution_score") + contribScore;
            if (totalContribScore > MAXIMUM_PLAYER_CONTRIB) {
                return -1L;
            }
            currPlayer.set("dusk_red_stones", currPlayer.getInteger("dusk_red_stones") + redCount);
            currPlayer.set("dusk_green_stones", currPlayer.getInteger("dusk_green_stones") + greenCount);
            currPlayer.set("dusk_blue_stones", currPlayer.getInteger("dusk_blue_stones") + blueCount);
            currPlayer.set("dusk_ancient_adena_amount", totalAncientAdena);
            currPlayer.set("dusk_contribution_score", totalContribScore);
            _signsPlayerData.put(charObjId, currPlayer);
            _duskStoneScore += contribScore;
        }
        saveSevenSignsData(charObjId, true);
        return contribScore;
    }

    public synchronized void updateFestivalScore() {
        _duskFestivalScore = 0L;
        _dawnFestivalScore = 0L;
        for (int i = 0; i < 5; ++i) {
            final long dusk = SevenSignsFestival.getInstance().getHighestScore(1, i);
            final long dawn = SevenSignsFestival.getInstance().getHighestScore(2, i);
            if (dusk > dawn) {
                _duskFestivalScore += SevenSignsFestival.FESTIVAL_LEVEL_SCORES[i];
            } else if (dusk < dawn) {
                _dawnFestivalScore += SevenSignsFestival.FESTIVAL_LEVEL_SCORES[i];
            }
        }
    }

    /**
     * Send info on the current Seven Signs period to the specified player.
     *
     * @param player
     */
    public void sendCurrentPeriodMsg(final Player player) {
        switch (_activePeriod) {
            case PERIOD_COMP_RECRUITING:
                player.sendPacket(Msg.SEVEN_SIGNS_PREPARATIONS_HAVE_BEGUN_FOR_THE_NEXT_QUEST_EVENT);
                return;
            case PERIOD_COMPETITION:
                player.sendPacket(Msg.SEVEN_SIGNS_THE_QUEST_EVENT_PERIOD_HAS_BEGUN_SPEAK_WITH_A_PRIEST_OF_DAWN_OR_DUSK_PRIESTESS_IF_YOU_WISH_TO_PARTICIPATE_IN_THE_EVENT);
                return;
            case PERIOD_COMP_RESULTS:
                player.sendPacket(Msg.SEVEN_SIGNS_QUEST_EVENT_HAS_ENDED_RESULTS_ARE_BEING_TALLIED);
                return;
            case PERIOD_SEAL_VALIDATION:
                player.sendPacket(Msg.SEVEN_SIGNS_THIS_IS_THE_SEAL_VALIDATION_PERIOD_A_NEW_QUEST_EVENT_PERIOD_BEGINS_NEXT_MONDAY);
        }
    }

    public void sendMessageToAll(final int sysMsgId) {
        final SystemMessage sm = new SystemMessage(sysMsgId);
        GameObjectsStorage.getPlayers().forEach(player -> player.sendPacket(sm));
    }

    protected void initializeSeals() {
        _signsSealOwners.forEach((key, value) -> {
            final int sealOwner = value;
            if (sealOwner != 0) {
                if (isSealValidationPeriod()) {
                    LOGGER.info("SevenSigns: The " + getCabalName(sealOwner) + " have won the " + getSealName(key, false) + ".");
                } else {
                    LOGGER.info("SevenSigns: The " + getSealName(key, false) + " is currently owned by " + getCabalName(sealOwner) + ".");
                }
            } else {
                LOGGER.info("SevenSigns: The " + getSealName(key, false) + " remains unclaimed.");
            }
        });
    }

    protected void resetSeals() {
        _signsDawnSealTotals.put(1, 0);
        _signsDawnSealTotals.put(2, 0);
        _signsDawnSealTotals.put(3, 0);
        _signsDuskSealTotals.put(1, 0);
        _signsDuskSealTotals.put(2, 0);
        _signsDuskSealTotals.put(3, 0);
    }

    protected void calcNewSealOwners() {
        _signsDawnSealTotals.forEach((key, value) -> {
            final int prevSealOwner = value;
            int newSealOwner = 0;
            final int dawnProportion = getSealProportion(key, 2);
            final int totalDawnMembers = (getTotalMembers(2) == 0) ? 1 : getTotalMembers(2);
            final int duskProportion = getSealProportion(key, 1);
            final int totalDuskMembers = (getTotalMembers(1) == 0) ? 1 : getTotalMembers(1);
            switch (prevSealOwner) {
                case CABAL_NULL:
                    switch (getCabalHighestScore()) {
                        case CABAL_NULL:
                            if (dawnProportion >= Math.round(0.35 * totalDawnMembers) && dawnProportion > duskProportion) {
                                newSealOwner = CABAL_DAWN;
                            } else if (duskProportion >= Math.round(0.35 * totalDuskMembers) && duskProportion > dawnProportion) {
                                newSealOwner = CABAL_DUSK;
                            } else {
                                newSealOwner = prevSealOwner;
                            }
                            break;
                        case CABAL_DAWN:
                            if (dawnProportion >= Math.round(0.35 * totalDawnMembers)) {
                                newSealOwner = CABAL_DAWN;
                            } else if (duskProportion >= Math.round(0.35 * totalDuskMembers)) {
                                newSealOwner = CABAL_DUSK;
                            } else {
                                newSealOwner = prevSealOwner;
                            }
                            break;
                        case CABAL_DUSK:
                            if (duskProportion >= Math.round(0.35 * totalDuskMembers)) {
                                newSealOwner = CABAL_DUSK;
                            } else if (dawnProportion >= Math.round(0.35 * totalDawnMembers)) {
                                newSealOwner = CABAL_DAWN;
                            } else {
                                newSealOwner = prevSealOwner;
                            }
                            break;
                    }
                    break;
                case CABAL_DAWN:
                    switch (getCabalHighestScore()) {
                        case CABAL_NULL:
                            if (dawnProportion >= Math.round(0.10 * totalDawnMembers)) {
                                newSealOwner = prevSealOwner;
                            } else if (duskProportion >= Math.round(0.35 * totalDuskMembers)) {
                                newSealOwner = CABAL_DUSK;
                            } else {
                                newSealOwner = CABAL_NULL;
                            }
                            break;
                        case CABAL_DAWN:
                            if (dawnProportion >= Math.round(0.10 * totalDawnMembers)) {
                                newSealOwner = prevSealOwner;
                            } else if (duskProportion >= Math.round(0.35 * totalDuskMembers)) {
                                newSealOwner = CABAL_DUSK;
                            } else {
                                newSealOwner = CABAL_NULL;
                            }
                            break;
                        case CABAL_DUSK:
                            if (duskProportion >= Math.round(0.10 * totalDuskMembers)) {
                                newSealOwner = CABAL_DUSK;
                            } else if (dawnProportion >= Math.round(0.35 * totalDawnMembers)) {
                                newSealOwner = prevSealOwner;
                            } else {
                                newSealOwner = CABAL_NULL;
                            }
                            break;
                    }
                    break;
                case CABAL_DUSK:
                    switch (getCabalHighestScore()) {
                        case CABAL_NULL:
                            if (duskProportion >= Math.round(0.10 * totalDuskMembers)) {
                                newSealOwner = prevSealOwner;
                            } else if (dawnProportion >= Math.round(0.35 * totalDawnMembers)) {
                                newSealOwner = CABAL_DAWN;
                            } else {
                                newSealOwner = CABAL_NULL;
                            }
                            break;
                        case CABAL_DAWN:
                            if (dawnProportion >= Math.round(0.35 * totalDawnMembers)) {
                                newSealOwner = CABAL_DAWN;
                            } else if (duskProportion >= Math.round(0.10 * totalDuskMembers)) {
                                newSealOwner = prevSealOwner;
                            } else {
                                newSealOwner = CABAL_NULL;
                            }
                            break;
                        case CABAL_DUSK:
                            if (duskProportion >= Math.round(0.10 * totalDuskMembers)) {
                                newSealOwner = prevSealOwner;
                            } else if (dawnProportion >= Math.round(0.35 * totalDawnMembers)) {
                                newSealOwner = CABAL_DAWN;
                            } else {
                                newSealOwner = CABAL_NULL;
                            }
                            break;
                    }
                    break;
            }
            _signsSealOwners.put(key, newSealOwner);

            // Alert all online players to new seal status.
            switch (key) {
                case SEAL_AVARICE:
                    if (newSealOwner == CABAL_DAWN) {
                        sendMessageToAll(SystemMessage.SEVEN_SIGNS_THE_LORDS_OF_DAWN_HAVE_OBTAINED_THE_SEAL_OF_AVARICE);
                    } else if (newSealOwner == CABAL_DUSK) {
                        sendMessageToAll(SystemMessage.SEVEN_SIGNS_THE_REVOLUTIONARIES_OF_DUSK_HAVE_OBTAINED_THE_SEAL_OF_AVARICE);
                    }
                    break;
                case SEAL_GNOSIS:
                    if (newSealOwner == CABAL_DAWN) {
                        sendMessageToAll(SystemMessage.SEVEN_SIGNS_THE_LORDS_OF_DAWN_HAVE_OBTAINED_THE_SEAL_OF_GNOSIS);
                    } else if (newSealOwner == CABAL_DUSK) {
                        sendMessageToAll(SystemMessage.SEVEN_SIGNS_THE_REVOLUTIONARIES_OF_DUSK_HAVE_OBTAINED_THE_SEAL_OF_GNOSIS);
                    }
                    break;
                case SEAL_STRIFE:
                    if (newSealOwner == CABAL_DAWN) {
                        sendMessageToAll(SystemMessage.SEVEN_SIGNS_THE_LORDS_OF_DAWN_HAVE_OBTAINED_THE_SEAL_OF_STRIFE);
                    } else if (newSealOwner == CABAL_DUSK) {
                        sendMessageToAll(SystemMessage.SEVEN_SIGNS_THE_REVOLUTIONARIES_OF_DUSK_HAVE_OBTAINED_THE_SEAL_OF_STRIFE);
                    }
                    break;
            }
        });
    }

    public int getPriestCabal(final int id) {
        switch (id) {
            case 31078:
            case 31079:
            case 31080:
            case 31081:
            case 31082:
            case 31083:
            case 31084:
            case 31168:
            case 31692:
            case 31694:
            case 31997: {
                return 2;
            }
            case 31085:
            case 31086:
            case 31087:
            case 31088:
            case 31089:
            case 31090:
            case 31091:
            case 31169:
            case 31693:
            case 31695:
            case 31998: {
                return 1;
            }
            default: {
                return 0;
            }
        }
    }

    public void changePeriod() {
        _periodChange = ThreadPoolManager.getInstance().schedule(new SevenSignsPeriodChange(), 10L);
    }

    public void changePeriod(final int period) {
        changePeriod(period, 1);
    }

    public void changePeriod(final int period, final int seconds) {
        _activePeriod = period - 1;
        if (_activePeriod < 0) {
            _activePeriod += 4;
        }
        _periodChange = ThreadPoolManager.getInstance().schedule(new SevenSignsPeriodChange(), seconds * 1000L);
    }

    public void setTimeToNextPeriodChange(final int time) {
        _calendar.setTimeInMillis(System.currentTimeMillis() + time * 1000L * 60L);
        if (_periodChange != null) {
            _periodChange.cancel(false);
        }
        _periodChange = ThreadPoolManager.getInstance().schedule(new SevenSignsPeriodChange(), getMilliToPeriodChange());
    }

    public SSListenerList getListenerEngine() {
        return _listenerList;
    }

    public <T extends GameListener> boolean addListener(final T listener) {
        return _listenerList.add(listener);
    }

    public <T extends GameListener> boolean removeListener(final T listener) {
        return _listenerList.remove(listener);
    }

    private class OnStartListenerImpl implements OnStartListener {
        @Override
        public void onStart() {
            getListenerEngine().onPeriodChange();
        }
    }

    protected class SSListenerList extends ListenerList<GameServer> {
        public void onPeriodChange() {
            if (getInstance().getCurrentPeriod() == 3) {
                getInstance().getCabalHighestScore();
            }
            getListeners().stream().filter(listener -> listener instanceof OnSSPeriodListener).forEach(listener -> ((OnSSPeriodListener) listener).onPeriodChange(getInstance().getCurrentPeriod()));
        }
    }

    public class SevenSignsAnnounce extends RunnableImpl {
        @Override
        public void runImpl() {
            if (Config.SEND_SSQ_WELCOME_MESSAGE) {
                GameObjectsStorage.getPlayers().forEach(SevenSigns.this::sendCurrentPeriodMsg);
                ThreadPoolManager.getInstance().schedule(new SevenSignsAnnounce(), Config.SS_ANNOUNCE_PERIOD * 1000L * 60L);
            }
        }
    }

    public class SevenSignsPeriodChange extends RunnableImpl {
        @Override
        public void runImpl() {
            LOGGER.info("SevenSignsPeriodChange: old=" + _activePeriod);
            final int periodEnded = _activePeriod;
            final SevenSigns this$0 = SevenSigns.this;
            ++this$0._activePeriod;
            switch (periodEnded) {
                case 0: {
                    sendMessageToAll(1210);
                    RaidBossSpawnManager.getInstance().distributeRewards();
                    break;
                }
                case 1: {
                    sendMessageToAll(1211);
                    final int compWinner = getCabalHighestScore();
                    calcNewSealOwners();
                    if (compWinner == 1) {
                        sendMessageToAll(1240);
                    } else {
                        sendMessageToAll(1241);
                    }
                    _previousWinner = compWinner;
                    break;
                }
                case 2: {
                    SevenSignsFestival.getInstance().distribAccumulatedBonus();
                    SevenSignsFestival.getInstance().rewardHighestRanked();
                    initializeSeals();
                    RaidBossSpawnManager.getInstance().distributeRewards();
                    sendMessageToAll(1218);
                    LOGGER.info("SevenSigns: The " + getCabalName(_previousWinner) + " have won the competition with " + getCurrentScore(_previousWinner) + " points!");
                    break;
                }
                case 3: {
                    _activePeriod = 0;
                    sendMessageToAll(1219);
                    resetPlayerData();
                    resetSeals();
                    _dawnStoneScore = 0L;
                    _duskStoneScore = 0L;
                    _dawnFestivalScore = 0L;
                    _duskFestivalScore = 0L;
                    final SevenSigns this$2 = SevenSigns.this;
                    ++this$2._currentCycle;
                    SevenSignsFestival.getInstance().resetFestivalData(false);
                    break;
                }
            }
            saveSevenSignsData(0, true);
            LOGGER.info("SevenSignsPeriodChange: new=" + _activePeriod);
            try {
                LOGGER.info("SevenSigns: Change Catacomb spawn...");
                getListenerEngine().onPeriodChange();
                final SSQInfo ss = new SSQInfo();
                GameObjectsStorage.getPlayers().forEach(player -> player.sendPacket(ss));
                LOGGER.info("SevenSigns: Spawning NPCs...");
                LOGGER.info("SevenSigns: The " + getCurrentPeriodName() + " period has begun!");
                LOGGER.info("SevenSigns: Calculating next period change time...");
                setCalendarForNextPeriodChange();
                LOGGER.info("SevenSignsPeriodChange: time to next change=" + Util.formatTime((int) (getMilliToPeriodChange() / 1000L)));
                final SevenSignsPeriodChange sspc = new SevenSignsPeriodChange();
                _periodChange = ThreadPoolManager.getInstance().schedule(sspc, getMilliToPeriodChange());
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        }
    }
}
