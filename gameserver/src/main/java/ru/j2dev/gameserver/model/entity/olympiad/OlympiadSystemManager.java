package ru.j2dev.gameserver.model.entity.olympiad;

import com.stringer.annotations.HideAccess;
import com.stringer.annotations.StringEncryption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.Announcements;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.Config.OlySeasonTimeCalcMode;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.manager.ServerVariables;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@HideAccess
@StringEncryption
public class OlympiadSystemManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(OlympiadSystemManager.class);
    private static final SimpleDateFormat _dtformat = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
    private static final String VAR_SEASON_ID = "oly_season_id";
    private static final String VAR_SEASON_CALC = "oly_season_calc";
    private static final String SQL_LOAD_SEASON_TIME = "SELECT `season_id`,`season_start_time`,`season_end_time`,`nominate_start`,`b_idx`,`b_s0`,`b_s1`,`b_s2`,`b_s3`,`c_idx`,`c_s0`,`c_e0`,`c_s1`,`c_e1`,`c_s2`,`c_e2`,`c_s3`,`c_e3`,`c_s4`,`c_e4`,`c_s5`,`c_e5`,`c_s6`,`c_e6`,`c_s7`,`c_e7`,`c_s8`,`c_e8`,`c_s9`,`c_e9`,`c_s10`,`c_e10`,`c_s11`,`c_e11`,`c_s12`,`c_e12`,`c_s13`,`c_e13`,`c_s14`,`c_e14`,`c_s15`,`c_e15`,`c_s16`,`c_e16`,`c_s17`,`c_e17`,`c_s18`,`c_e18`,`c_s19`,`c_e19`,`c_s20`,`c_e20`,`c_s21`,`c_e21`,`c_s22`,`c_e22`,`c_s23`,`c_e23`,`c_s24`,`c_e24`,`c_s25`,`c_e25`,`c_s26`,`c_e26`,`c_s27`,`c_e27`,`c_s28`,`c_e28`,`c_s29`,`c_e29`,`c_s30`,`c_e30` FROM `oly_season` WHERE `season_id` = ?";
    private static final String SQL_SAVE_SEASON_TIME = "REPLACE INTO `oly_season`(`season_id`,`season_start_time`,`season_end_time`,`nominate_start`,`b_idx`,`b_s0`,`b_s1`,`b_s2`,`b_s3`,`c_idx`,`c_s0`,`c_e0`,`c_s1`,`c_e1`,`c_s2`,`c_e2`,`c_s3`,`c_e3`,`c_s4`,`c_e4`,`c_s5`,`c_e5`,`c_s6`,`c_e6`,`c_s7`,`c_e7`,`c_s8`,`c_e8`,`c_s9`,`c_e9`,`c_s10`,`c_e10`,`c_s11`,`c_e11`,`c_s12`,`c_e12`,`c_s13`,`c_e13`,`c_s14`,`c_e14`,`c_s15`,`c_e15`,`c_s16`,`c_e16`,`c_s17`,`c_e17`,`c_s18`,`c_e18`,`c_s19`,`c_e19`,`c_s20`,`c_e20`,`c_s21`,`c_e21`,`c_s22`,`c_e22`,`c_s23`,`c_e23`,`c_s24`,`c_e24`,`c_s25`,`c_e25`,`c_s26`,`c_e26`,`c_s27`,`c_e27`,`c_s28`,`c_e28`,`c_s29`,`c_e29`,`c_s30`,`c_e30`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String PART_CNT_VAR = "@OlyPartCnt";
    private static final String OLY_HERO_SEASON_VAR = "oly_chero_season";
    private final ScheduledFuture<?>[][] _comps_start_tasks;
    private final ScheduledFuture<?>[] _bonus_tasks;
    private final long[] _bonus_time = new long[4];
    private final long[][] _comps_time = new long[31][];
    private int _season_id;
    private boolean _season_calculation;
    private boolean _is_comp_active;
    private ScheduledFuture<?> _season_start_task;
    private ScheduledFuture<?> _season_end_task;
    private ScheduledFuture<?> _nominate_task;
    private long _season_start_time;
    private long _season_end_time;
    private long _nominate_time;
    private int _bonus_idx;
    private int _comp_idx;
    private int _part_count;
    private int _active_comp_idx = -1;

    private OlympiadSystemManager() {
        _comps_start_tasks = (ScheduledFuture<?>[][]) new ScheduledFuture[_comps_time.length][];
        _bonus_tasks = (ScheduledFuture<?>[]) new ScheduledFuture[_bonus_time.length];
        _dtformat.setTimeZone(TimeZone.getDefault());
        load();
        schedule();
    }

    public static OlympiadSystemManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    private static String ScheduledFutureTime(final ScheduledFuture<?> future) {
        return UnixTimeStampToString(System.currentTimeMillis() / 1000L + future.getDelay(TimeUnit.SECONDS));
    }

    private static String UnixTimeStampToString(final long dt) {
        return _dtformat.format(new Date((dt + 1L) * 1000L));
    }

    public synchronized void load() {
        _season_id = ServerVariables.getInt(VAR_SEASON_ID, 0);
        _season_calculation = ServerVariables.getBool(VAR_SEASON_CALC, false);
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rset = null;
        try {
            conn = DatabaseFactory.getInstance().getConnection();
            pstmt = conn.prepareStatement(SQL_LOAD_SEASON_TIME);
            pstmt.setInt(1, _season_id);
            rset = pstmt.executeQuery();
            if (rset.next()) {
                _season_start_time = rset.getLong("season_start_time");
                _season_end_time = rset.getLong("season_end_time");
                _nominate_time = rset.getLong("nominate_start");
                _bonus_idx = rset.getInt("b_idx");
                _bonus_time[0] = rset.getLong("b_s0");
                _bonus_time[1] = rset.getLong("b_s1");
                _bonus_time[2] = rset.getLong("b_s2");
                _bonus_time[3] = rset.getLong("b_s3");
                _comp_idx = rset.getInt("c_idx");
                for (int i = 0; i < _comps_time.length; ++i) {
                    final long[] comp_time = {rset.getLong("c_s" + i), rset.getLong("c_e" + i)};
                    _comps_time[i] = comp_time;
                }
            } else {
                LOGGER.info("OlympiadSystemManager: Generating a new season " + _season_id);
                calcNewSeason();
                save();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(conn, pstmt, rset);
        }
        _part_count = ServerVariables.getInt(PART_CNT_VAR, 0);
    }

    public synchronized void save() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseFactory.getInstance().getConnection();
            pstmt = conn.prepareStatement(SQL_SAVE_SEASON_TIME);
            pstmt.setInt(1, _season_id);
            pstmt.setLong(2, _season_start_time);
            pstmt.setLong(3, _season_end_time);
            pstmt.setLong(4, _nominate_time);
            pstmt.setInt(5, _bonus_idx);
            for (int i = 0; i < _bonus_time.length; ++i) {
                pstmt.setLong(6 + i, _bonus_time[i]);
            }
            pstmt.setInt(10, _comp_idx);
            for (int j = 0; j < _comps_time.length; ++j) {
                pstmt.setLong(11 + j * 2, _comps_time[j][0]);
                pstmt.setLong(12 + j * 2, _comps_time[j][1]);
            }
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(conn, pstmt);
        }
        ServerVariables.set("oly_season_id", _season_id);
        ServerVariables.set("oly_season_calc", _season_calculation);
        ServerVariables.set("@OlyPartCnt", _part_count);
    }

    private void schedule() {
        final long now = System.currentTimeMillis() / 1000L;
        final int curr_season = _season_id;
        _season_start_task = ThreadPoolManager.getInstance().schedule(new SeasonStartTask(curr_season), Math.max(60L, _season_start_time - now) * 1000L);
        LOGGER.info("OlympiadSystemManager: Season " + curr_season + " start schedule at " + ScheduledFutureTime(_season_start_task));
        _season_end_task = ThreadPoolManager.getInstance().schedule(new SeasonEndTask(curr_season), Math.max(240L, _season_end_time - now) * 1000L);
        LOGGER.info("OlympiadSystemManager: Season " + curr_season + " end schedule at " + ScheduledFutureTime(_season_end_task));
        _nominate_task = ThreadPoolManager.getInstance().schedule(new NominationTask(curr_season), Math.max(600L, _nominate_time - now) * 1000L);
        LOGGER.info("OlympiadSystemManager: Season " + curr_season + " nomination schedule at " + ScheduledFutureTime(_nominate_task));
        StringBuilder sb = new StringBuilder();
        IntStream.range(_comp_idx, _comps_time.length).filter(i -> _comps_time[i] != null).filter(i -> _comps_time[i][0] >= now || _comps_time[i][1] >= now).forEach(i -> {
            if (i != _comp_idx) {
                sb.append(';');
            }
            _comps_start_tasks[i] = (ScheduledFuture<?>[]) new ScheduledFuture[]{ThreadPoolManager.getInstance().schedule(new CompetitionStartTask(curr_season, i), Math.max(60L, _comps_time[i][0] - now) * 1000L), ThreadPoolManager.getInstance().schedule(new CompetitionEndTask(curr_season, i), Math.max(60L, _comps_time[i][1] - now) * 1000L)};
            sb.append(ScheduledFutureTime(_comps_start_tasks[i][0])).append("-").append(ScheduledFutureTime(_comps_start_tasks[i][1]));
        });
        //LOGGER.info("OlympiadManager: Season " + curr_season + " competitions schedule at [" + sb + "]");
        sb.delete(0, sb.length());
        IntStream.range(_bonus_idx, _bonus_time.length).filter(j -> _bonus_time[j] > now).forEach(j -> {
            if (j != _bonus_idx) {
                sb.append(';');
            }
            _bonus_tasks[j] = ThreadPoolManager.getInstance().schedule(new BonusTask(curr_season, j), Math.max((3 + j - _bonus_idx) * 60, _bonus_time[j] - now) * 1000L);
            sb.append(ScheduledFutureTime(_bonus_tasks[j]));
        });
        //LOGGER.info("OlympiadManager: Season " + curr_season + " bonuses schedule at [" + sb + "]");
    }

    private synchronized void SeasonStart(final int season_id) {
        try {
            _season_calculation = false;
            Announcements.getInstance().announceToAll(new SystemMessage(1639).addNumber(season_id));
            LOGGER.info("OlympiadSystemManager: Season " + season_id + " started.");
        } catch (Exception ex) {
            LOGGER.warn("OlympiadSystemManager: Exception while starting of " + season_id + " season", ex);
        }
    }

    private synchronized void SeasonEnd(final int season_id) {
        try {
            if (!_season_calculation) {
                _season_calculation = true;
                if (ServerVariables.getInt(OLY_HERO_SEASON_VAR, -1) != season_id) {
                    LOGGER.info("OlympiadSystemManager: calculation heroes for " + season_id + " season");
                    HeroManager.getInstance().ComputeNewHeroNobleses();
                    ServerVariables.set(OLY_HERO_SEASON_VAR, season_id);
                }
                save();
                Announcements.getInstance().announceToAll(new SystemMessage(1640).addNumber(season_id));
            } else {
                LOGGER.warn("OlympiadSystemManager: Unexpected season calculated. Canceling computation.");
            }
            LOGGER.info("OlympiadSystemManager: Season " + season_id + " ended.");
        } catch (Exception ex) {
            LOGGER.warn("OlympiadSystemManager: Exception while ending of " + season_id + " season", ex);
        }
    }

    private synchronized void Nomination(final int season_id) {
        try {
            if (_season_calculation) {
                _season_calculation = false;
                save();
            } else {
                LOGGER.warn("OlympiadSystemManager: Season not calculated. Run calculation manualy.");
            }
            LOGGER.info("OlympiadSystemManager: Season " + season_id + " nomination started.");
            ThreadPoolManager.getInstance().execute(new NewSeasonCalcTask(season_id + 1));
        } catch (Exception ex) {
            LOGGER.warn("OlympiadSystemManager: Exception while nominating in " + season_id + " season", ex);
        }
    }

    private synchronized void CompetitionStart(final int season_id, final int comp_id) {
        try {
            if (!_is_comp_active) {
                _is_comp_active = true;
                OlympiadStadiumManager.getInstance().AllocateStadiums();
                OlympiadPlayersManager.getInstance().AllocatePools();
                OlympiadGameManager.getInstance();
                OlympiadGameManager.getInstance().scheduleStartTask();
                Announcements.getInstance().announceToAll(Msg.THE_OLYMPIAD_GAME_HAS_STARTED);
                LOGGER.info("OlympiadSystemManager: Season " + season_id + " comp " + comp_id + " started.");
                _active_comp_idx = comp_id;
            } else {
                LOGGER.warn("OlympiadSystemManager: Can't start new competitions. Old comps in progress.");
            }
        } catch (Exception ex) {
            LOGGER.warn("OlympiadSystemManager: Exception while start comp " + comp_id + " in " + season_id + " season", ex);
        }
    }

    private synchronized void CompetitionEnd(final int season_id, final int comp_id) {
        try {
            if (_is_comp_active) {
                OlympiadGameManager.getInstance().cancelStartTask();
                _is_comp_active = false;
                OlympiadStadiumManager.getInstance().FreeStadiums();
                OlympiadPlayersManager.getInstance().FreePools();
                _active_comp_idx = -1;
                ++_comp_idx;
                Announcements.getInstance().announceToAll(new SystemMessage(1919));
                Announcements.getInstance().announceToAll(Msg.THE_OLYMPIAD_GAME_HAS_ENDED);
                LOGGER.info("OlympiadSystemManager: Season " + season_id + " comp " + comp_id + " ended.");
                save();
            } else {
                LOGGER.warn("OlympiadSystemManager: Can't stop competitions. Competitions not in progress.");
            }
        } catch (Exception ex) {
            LOGGER.warn("OlympiadSystemManager: Exception while end comp " + comp_id + " in " + season_id + " season", ex);
        }
    }

    private synchronized void Bonus(final int season_id, final int bonus_id) {
        try {
            NoblessManager.getInstance().AddWeaklyBonus();
            ++_bonus_idx;
            LOGGER.info("OlympiadSystemManager: Season " + season_id + " bonus " + bonus_id + " applied.");
            save();
        } catch (Exception ex) {
            LOGGER.warn("OlympiadSystemManager: Exception while bonus " + bonus_id + " in " + season_id + " season", ex);
        }
    }

    private synchronized void NewSeasonCalc(final int season_id) {
        try {
            save();
            _season_id = season_id;
            if (Config.OLY_RECALC_NEW_SEASON) {
                calcNewSeason();
                save();
            } else {
                ServerVariables.set("oly_season_id", _season_id);
                load();
            }
            schedule();
        } catch (Exception ex) {
            LOGGER.warn("OlympiadSystemManager: Exception while calculating new " + season_id + " season", ex);
        }
    }

    public boolean isCompetitionsActive() {
        return _is_comp_active;
    }

    public boolean isRegAllowed() {
        if (_is_comp_active && _active_comp_idx >= 0) {
            return System.currentTimeMillis() < (_comps_time[_active_comp_idx][1] - 300L) * 1000L;
        }
        return _is_comp_active;
    }

    public boolean isCalculationPeriod() {
        return _season_calculation;
    }

    public int getCurrentSeason() {
        return _season_id;
    }

    public int getCurrentPeriod() {
        return _bonus_idx;
    }

    public void shutdown() {
        if (_is_comp_active) {
            OlympiadGameManager.getInstance().cancelStartTask();
            OlympiadStadiumManager.getInstance().FreeStadiums();
            OlympiadPlayersManager.getInstance().FreePools();
        }
    }

    public void announceCompetition(final OlympiadGameType type, final int stad_id) {
        for (NpcInstance npc : GameObjectsStorage.getNpcs(npcInstance -> npcInstance.getNpcId() == 31688)) {
            final int npcString;
            switch (type) {
                case TEAM_CLASS_FREE:
                    npcString = 1300132;
                    break;
                case CLASS_INDIVIDUAL:
                    npcString = 1300167;
                    break;
                case CLASS_FREE:
                    npcString = 1300166;
                    break;
                default:
                    continue;
            }
            if (Config.OLY_MANAGER_TRADE_CHAT) {
                npc.MakeFString(npcString, ChatType.TRADE, String.valueOf(stad_id + 1));
            } else {
                npc.MakeFString(npcString, ChatType.SHOUT, String.valueOf(stad_id + 1));
            }
        }
    }

    public int getPartCount() {
        return _part_count;
    }

    public int getCurrentPartCount() {
        return OlympiadGameManager.getInstance().getCompetitions().size();
    }

    public void incPartCount() {
        ++_part_count;
    }

    private synchronized void calcNewSeason() {
        final Calendar base = Calendar.getInstance();
        if (Config.OLY_SEASON_TIME_CALC_MODE == OlySeasonTimeCalcMode.NORMAL) {
            base.set(Calendar.DATE, 1);
        } else {
            base.set(Calendar.DATE, base.get(Calendar.DATE));
        }
        base.set(Calendar.HOUR_OF_DAY, 0);
        base.set(Calendar.MINUTE, 0);
        base.set(Calendar.SECOND, 0);
        base.set(Calendar.MILLISECOND, 0);
        long base_mills = base.getTimeInMillis();
        _season_start_time = getDateSeconds(base_mills, Config.OLY_SEASON_START_TIME);
        _season_end_time = getDateSeconds(base_mills, Config.OLY_SEASON_END_TIME);
        _nominate_time = getDateSeconds(base_mills, Config.OLY_NOMINATE_TIME);
        base_mills = _season_start_time * 1000L;
        final Calendar c_bonus = Calendar.getInstance();
        c_bonus.setTimeInMillis(base_mills);
        IntStream.range(0, _bonus_time.length).forEach(i -> _bonus_time[i] = getDateSeconds(c_bonus, Config.OLY_BONUS_TIME));
        _bonus_idx = 0;
        final Calendar c_comp_start = Calendar.getInstance();
        c_comp_start.setTimeInMillis(base_mills);
        IntStream.range(0, _comps_time.length).forEach(j -> {
            (_comps_time[j] = new long[2])[0] = getDateSeconds(c_comp_start, Config.OLY_COMPETITION_START_TIME);
            _comps_time[j][1] = getDateSeconds(c_comp_start, Config.OLY_COMPETITION_END_TIME);
            if (_comps_time[j][0] > _season_end_time) {
                _comps_time[j][0] = -1L;
                _comps_time[j][1] = -1L;
            }
            if (_comps_time[j][1] >= _season_end_time - 300L) {
                final long[] array = _comps_time[j];
                final int n = 1;
                array[n] -= 300L;
            }
        });
        _comp_idx = 0;
    }

    private long getDateSeconds(final long mills, final String rule) {
        final Calendar c = Calendar.getInstance();
        c.setTime(new Date(mills));
        return getDateSeconds(c, rule);
    }

    private long getDateSeconds(final Calendar c, final String rule) {
        final String[] parts = rule.split("\\s+");
        if (parts.length == 2) {
            final String datepartsstr = parts[0];
            final String[] dateparts = datepartsstr.split(":");
            if (dateparts.length == 2) {
                if (dateparts[0].startsWith("+")) {
                    c.add(Calendar.MONTH, Integer.parseInt(dateparts[0].substring(1)));
                } else {
                    c.set(Calendar.MONTH, Integer.parseInt(dateparts[0]) - 1);
                }
            }
            final String datemodstr = dateparts[dateparts.length - 1];
            if (datemodstr.startsWith("+")) {
                c.add(Calendar.DATE, Integer.parseInt(datemodstr.substring(1)));
            } else {
                c.set(Calendar.DATE, Integer.parseInt(datemodstr));
            }
        }
        final String[] timeparts = parts[parts.length - 1].split(":");
        if (timeparts[0].startsWith("+")) {
            c.add(Calendar.HOUR_OF_DAY, Integer.parseInt(timeparts[0].substring(1)));
        } else {
            c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeparts[0]));
        }
        if (timeparts[1].startsWith("+")) {
            c.add(Calendar.MINUTE, Integer.parseInt(timeparts[1].substring(1)));
        } else {
            c.set(Calendar.MINUTE, Integer.parseInt(timeparts[1]));
        }
        return c.getTimeInMillis() / 1000L;
    }

    public long getSeasonStartTime() {
        return _season_start_time;
    }

    public long getSeasonEndTime() {
        return _season_end_time;
    }

    private static class LazyHolder {
        private static final OlympiadSystemManager INSTANCE = new OlympiadSystemManager();
    }

    private class SeasonStartTask implements Runnable {
        private final int season_id;

        public SeasonStartTask(final int season_id_) {
            season_id = season_id_;
        }

        @Override
        public void run() {
            getInstance().SeasonStart(season_id);
        }
    }

    private class SeasonEndTask implements Runnable {
        private final int season_id;

        public SeasonEndTask(final int season_id_) {
            season_id = season_id_;
        }

        @Override
        public void run() {
            getInstance().SeasonEnd(season_id);
        }
    }

    private class NominationTask implements Runnable {
        private final int season_id;

        public NominationTask(final int season_id_) {
            season_id = season_id_;
        }

        @Override
        public void run() {
            getInstance().Nomination(season_id);
        }
    }

    private class CompetitionStartTask implements Runnable {
        private final int season_id;
        private final int comp_id;

        public CompetitionStartTask(final int season_id_, final int comp_id_) {
            season_id = season_id_;
            comp_id = comp_id_;
        }

        @Override
        public void run() {
            getInstance().CompetitionStart(season_id, comp_id);
        }
    }

    private class CompetitionEndTask implements Runnable {
        private final int season_id;
        private final int comp_id;

        public CompetitionEndTask(final int season_id_, final int comp_id_) {
            season_id = season_id_;
            comp_id = comp_id_;
        }

        @Override
        public void run() {
            getInstance().CompetitionEnd(season_id, comp_id);
        }
    }

    private class BonusTask implements Runnable {
        private final int season_id;
        private final int bonus_id;

        public BonusTask(final int season_id_, final int bonus_id_) {
            season_id = season_id_;
            bonus_id = bonus_id_;
        }

        @Override
        public void run() {
            getInstance().Bonus(season_id, bonus_id);
        }
    }

    private class NewSeasonCalcTask implements Runnable {
        private final int season_id;

        public NewSeasonCalcTask(final int season_id_) {
            season_id = season_id_;
        }

        @Override
        public void run() {
            getInstance().NewSeasonCalc(season_id);
        }
    }
}
