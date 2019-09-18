package ru.j2dev.gameserver.model.entity.olympiad;

import com.stringer.annotations.HideAccess;
import com.stringer.annotations.StringEncryption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.base.ClassId;
import ru.j2dev.gameserver.utils.Log;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@HideAccess
@StringEncryption
public class NoblessManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoblessManager.class);
    private static final String GET_ALL_NOBLES = "SELECT `char_id`,`class_id`,`char_name`,`points_current`,`points_past`,`points_pre_past`,`class_free_cnt`,`class_based_cnt`,`team_cnt`,`comp_win`,`comp_loose`,`comp_done` FROM `oly_nobles`";
    private static final String SAVE_NOBLE = "REPLACE INTO `oly_nobles`(`char_id`,`class_id`,`char_name`,`points_current`,`points_past`,`points_pre_past`,`class_free_cnt`,`class_based_cnt`,`team_cnt`,`comp_win`,`comp_loose`,`comp_done`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String REMOVE_NOBLE = "DELETE FROM `oly_nobles` WHERE `char_id` = ?";
    private static final NobleRecordCmp NRCmp = new NobleRecordCmp();

    private final List<NobleRecord> _nobleses;

    private NoblessManager() {
        _nobleses = new CopyOnWriteArrayList<>();
        LoadNobleses();
        ComputeRanks();
    }

    public static NoblessManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    public boolean isNobles(final int object_id) {
        return null != getNobleRecord(object_id);
    }

    public boolean isNobles(final Player player) {
        return getNobleRecord(player.getObjectId()) != null;
    }

    public NobleRecord getNobleRecord(final int object_id) {
        return _nobleses.stream().filter(nr -> nr != null && nr.char_id == object_id).findFirst().orElse(null);
    }

    public void renameNoble(final int object_id, final String newname) {
        _nobleses.stream().filter(nr -> nr != null && nr.char_id == object_id).forEach(nr -> {
            nr.char_name = newname;
            SaveNobleRecord(nr);
        });
    }

    protected Collection<NobleRecord> getNoblesRecords() {
        return _nobleses;
    }

    public int getPointsOf(final Player player) {
        return getPointsOf(player.getObjectId());
    }

    public int getPointsOf(final int object_id) {
        return _nobleses.stream().filter(nr -> nr != null && nr.char_id == object_id).findFirst().map(nr -> nr.points_current).orElse(-1);
    }

    public void setPointsOf(final int object_id, final int points) {
        _nobleses.stream().filter(nr -> nr != null && nr.char_id == object_id).forEach(nr -> {
            nr.points_current = points;
            SaveNobleRecord(nr);
        });
    }

    public synchronized void removeNoble(final Player noble) {
        NobleRecord nobleRecord = null;
        for (final NobleRecord nobleRecord2 : _nobleses) {
            if (nobleRecord2.char_id == noble.getObjectId()) {
                nobleRecord = nobleRecord2;
            }
        }
        if (nobleRecord == null) {
            return;
        }
        Connection conn = null;
        PreparedStatement pstmt = null;
        final ResultSet rset = null;
        try {
            conn = DatabaseFactory.getInstance().getConnection();
            pstmt = conn.prepareStatement(REMOVE_NOBLE);
            pstmt.setInt(1, nobleRecord.char_id);
            pstmt.executeUpdate();
            _nobleses.remove(nobleRecord);
        } catch (SQLException se) {
            LOGGER.warn("NoblessManager: Can't remove nobleses ", se);
        } finally {
            DbUtils.closeQuietly(conn, pstmt, rset);
        }
    }

    public synchronized void addNoble(final Player noble) {
        synchronized (_nobleses) {
            NobleRecord nr = null;
            for (final NobleRecord nr2 : _nobleses) {
                if (nr2.char_id == noble.getObjectId()) {
                    nr = nr2;
                }
            }
            _nobleses.remove(nr);
            if (nr == null) {
                int classId = noble.getBaseClassId();
                if (classId < 88) {
                    for (final ClassId id : ClassId.values()) {
                        if (id.level() == 3 && id.getParent(0).getId() == classId) {
                            classId = id.getId();
                            break;
                        }
                    }
                }
                nr = new NobleRecord(noble.getObjectId(), classId, noble.getName(), Config.OLY_SEASON_START_POINTS, 0, 0, 0, 0, 0, 0, 0, 0);
            }
            if (!noble.getName().equals(nr.char_name)) {
                nr.char_name = noble.getName();
            }
            if (noble.getBaseClassId() != nr.class_id) {
                LOGGER.warn("NoblessManager: " + noble.getName() + " got base class " + noble.getBaseClassId() + " but " + nr.class_id + " class nobless");
            }
            _nobleses.add(nr);
            setNobleRecord(nr);
        }
    }

    private void setNobleRecord(final NobleRecord noble) {
        SaveNobleRecord(noble);
    }

    public void LoadNobleses() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;
        try {
            conn = DatabaseFactory.getInstance().getConnection();
            stmt = conn.createStatement();
            rset = stmt.executeQuery(GET_ALL_NOBLES);
            while (rset.next()) {
                final NobleRecord nr = new NobleRecord(rset.getInt("char_id"), rset.getInt("class_id"), rset.getString("char_name"), rset.getInt("points_current"), rset.getInt("points_past"), rset.getInt("points_pre_past"), rset.getInt("class_free_cnt"), rset.getInt("class_based_cnt"), rset.getInt("team_cnt"), rset.getInt("comp_win"), rset.getInt("comp_loose"), rset.getInt("comp_done"));
                _nobleses.add(nr);
            }
        } catch (SQLException se) {
            LOGGER.warn("NoblessManager: Can't load nobleses ", se);
        } finally {
            DbUtils.closeQuietly(conn, stmt, rset);
        }
        LOGGER.info("NoblessManager: loaded " + _nobleses.size() + " nobleses.");
    }

    public void SaveNobleses() {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            pstmt = con.prepareStatement(SAVE_NOBLE);
            for (final NobleRecord noble : _nobleses) {
                pstmt.setInt(1, noble.char_id);
                pstmt.setInt(2, noble.class_id);
                pstmt.setString(3, noble.char_name);
                pstmt.setInt(4, noble.points_current);
                pstmt.setInt(5, noble.points_past);
                pstmt.setInt(6, noble.points_pre_past);
                pstmt.setInt(7, noble.class_free_cnt);
                pstmt.setInt(8, noble.class_based_cnt);
                pstmt.setInt(9, noble.team_cnt);
                pstmt.setInt(10, noble.comp_win);
                pstmt.setInt(11, noble.comp_loose);
                pstmt.setInt(12, noble.comp_done);
                pstmt.executeUpdate();
            }
        } catch (Exception e) {
            LOGGER.warn("NoblessManager: can't save nobleses : ", e);
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(con, pstmt);
        }
    }

    public void SaveNobleRecord(final NobleRecord noble) {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            pstmt = con.prepareStatement(SAVE_NOBLE);
            pstmt.setInt(1, noble.char_id);
            pstmt.setInt(2, noble.class_id);
            pstmt.setString(3, noble.char_name);
            pstmt.setInt(4, noble.points_current);
            pstmt.setInt(5, noble.points_past);
            pstmt.setInt(6, noble.points_pre_past);
            pstmt.setInt(7, noble.class_free_cnt);
            pstmt.setInt(8, noble.class_based_cnt);
            pstmt.setInt(9, noble.team_cnt);
            pstmt.setInt(10, noble.comp_win);
            pstmt.setInt(11, noble.comp_loose);
            pstmt.setInt(12, noble.comp_done);
            pstmt.executeUpdate();
        } catch (Exception e) {
            LOGGER.warn("NoblessManager: can't save noble " + noble.char_name, e);
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(con, pstmt);
        }
    }

    public void TransactNewSeason() {
        LOGGER.info("NoblessManager: Cleanuping last period.");
        _nobleses.forEach(nr -> {
            Log.add(String.format("NoblesController: %s(%d) new season clean. points_current=%d|points_past=%d|points_pre_past=%d|comp_done=%d", nr.char_name, nr.char_id, nr.points_current, nr.points_past, nr.points_pre_past, nr.comp_done), "olympiad");
            if (nr.comp_done >= Config.OLY_MIN_NOBLE_COMPS) {
                nr.points_past = nr.points_current;
                nr.points_pre_past = nr.points_current;
            } else {
                nr.points_past = 0;
                nr.points_pre_past = 0;
            }
            nr.comp_done = 0;
            nr.comp_win = 0;
            nr.comp_loose = 0;
            nr.class_based_cnt = 0;
            nr.class_free_cnt = 0;
            nr.team_cnt = 0;
            nr.points_current = Config.OLY_DEFAULT_POINTS;
        });
        SaveNobleses();
    }

    public void AddWeaklyBonus() {
        _nobleses.forEach(nobleRecord -> {
            nobleRecord.points_current += Config.OLY_WBONUS_POINTS;
            nobleRecord.class_based_cnt = 0;
            nobleRecord.class_free_cnt = 0;
            nobleRecord.team_cnt = 0;
        });
        SaveNobleses();
    }

    public String[] getClassLeaders(final int cid) {
        final ArrayList<String> result = new ArrayList<>();
        final NobleRecord[] leader = _nobleses.stream().filter(nr -> nr.class_id == cid && nr.points_pre_past > 0).sorted(NRCmp).toArray(NobleRecord[]::new);
        for (int i = 0; i < leader.length && i < 15; ++i) {
            if (leader[i] != null) {
                result.add(leader[i].char_name);
            }
        }
        return result.toArray(new String[0]);
    }

    public int getPlayerClassRank(final int cid, final int playerId) {
        final ArrayList<NobleRecord> tmp = _nobleses.stream().filter(nr -> nr.class_id == cid).sorted(NRCmp).collect(Collectors.toCollection(ArrayList::new));
        return IntStream.range(0, tmp.size()).filter(i -> tmp.get(i) != null && tmp.get(i).char_id == playerId).findFirst().orElse(-1);
    }

    public synchronized void ComputeRanks() {
        LOGGER.info("NoblessManager: Computing ranks.");
        final NobleRecord[] rank_nobleses = _nobleses.toArray(new NobleRecord[0]);
        Arrays.sort(rank_nobleses, NRCmp);
        int rank0 = (int) Math.round(rank_nobleses.length * 0.01);
        int rank2 = (int) Math.round(rank_nobleses.length * 0.1);
        int rank3 = (int) Math.round(rank_nobleses.length * 0.25);
        int rank4 = (int) Math.round(rank_nobleses.length * 0.5);
        if (rank0 == 0) {
            rank0 = 1;
            ++rank2;
            ++rank3;
            ++rank4;
        }
        int i;
        for (i = 0; i <= rank0 && i < rank_nobleses.length; ++i) {
            rank_nobleses[i].rank = 0;
        }
        while (i <= rank2 && i < rank_nobleses.length) {
            rank_nobleses[i].rank = 1;
            ++i;
        }
        while (i <= rank3 && i < rank_nobleses.length) {
            rank_nobleses[i].rank = 2;
            ++i;
        }
        while (i <= rank4 && i < rank_nobleses.length) {
            rank_nobleses[i].rank = 3;
            ++i;
        }
        while (i < rank_nobleses.length) {
            rank_nobleses[i].rank = 4;
            ++i;
        }
    }

    public synchronized int getNoblessePasses(final Player player) {
        final int coid = player.getObjectId();
        final NobleRecord nr = getInstance().getNobleRecord(coid);
        if (nr == null) {
            return 0;
        }
        if (nr.points_past == 0) {
            return 0;
        }
        int points = 0;
        if (nr.rank >= 0 && nr.rank < Config.OLY_POINTS_SETTLEMENT.length) {
            points = Config.OLY_POINTS_SETTLEMENT[nr.rank];
        }
        if (HeroManager.getInstance().isCurrentHero(coid) || HeroManager.getInstance().isInactiveHero(coid)) {
            points += Config.OLY_HERO_POINT_BONUS;
        }
        nr.points_past = 0;
        getInstance().SaveNobleRecord(nr);
        return points * Config.OLY_ITEMS_SETTLEMENT_PER_POINT;
    }

    private static class LazyHolder {
        private static final NoblessManager INSTANCE = new NoblessManager();
    }

    private static class NobleRecordCmp implements Comparator<NobleRecord> {
        @Override
        public int compare(final NobleRecord o1, final NobleRecord o2) {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return 1;
            }
            if (o2 == null) {
                return -1;
            }
            return o2.points_pre_past - o1.points_pre_past;
        }
    }

    public class NobleRecord {
        public final int char_id;
        public final int class_id;
        public String char_name;
        public int points_current;
        public int points_past;
        public int points_pre_past;
        public int class_free_cnt;
        public int class_based_cnt;
        public int team_cnt;
        public int comp_win;
        public int comp_loose;
        public int comp_done;
        public int rank;

        private NobleRecord(final int _char_id, final int _class_id, final String _char_name, final int _points_current, final int _points_past, final int _points_pre_past, final int _class_free_cnt, final int _class_based_cnt, final int _team_cnt, final int _comp_win, final int _comp_loose, final int _comp_done) {
            char_id = _char_id;
            class_id = _class_id;
            char_name = _char_name;
            points_current = _points_current;
            points_past = _points_past;
            points_pre_past = _points_pre_past;
            class_free_cnt = _class_free_cnt;
            class_based_cnt = _class_based_cnt;
            team_cnt = _team_cnt;
            comp_win = _comp_win;
            comp_loose = _comp_loose;
            comp_done = _comp_done;
            rank = 0;
        }
    }
}
