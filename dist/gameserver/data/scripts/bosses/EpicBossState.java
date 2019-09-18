package bosses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.database.DatabaseFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;

public class EpicBossState {
    private static final Logger LOGGER = LoggerFactory.getLogger(EpicBossState.class);

    private int _bossId;
    private long _respawnDate;
    private State _state;

    EpicBossState(final int bossId) {
        this(bossId, true);
    }

    private EpicBossState(final int bossId, final boolean isDoLoad) {
        _bossId = bossId;
        if (isDoLoad) {
            load();
        }
    }

    public int getBossId() {
        return _bossId;
    }

    public void setBossId(final int newId) {
        _bossId = newId;
    }

    public State getState() {
        return _state;
    }

    public void setState(final State newState) {
        _state = newState;
    }

    public long getRespawnDate() {
        return _respawnDate;
    }

    public void setRespawnDate(final long interval) {
        _respawnDate = interval + System.currentTimeMillis();
    }

    public void load() {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT * FROM epic_boss_spawn WHERE bossId = ? LIMIT 1");
            statement.setInt(1, _bossId);
            rset = statement.executeQuery();
            if (rset.next()) {
                _respawnDate = rset.getLong("respawnDate") * 1000L;
                if (_respawnDate - System.currentTimeMillis() <= 0L) {
                    _state = State.NOTSPAWN;
                } else {
                    final int tempState = rset.getInt("state");
                    if (tempState == State.NOTSPAWN.ordinal()) {
                        _state = State.NOTSPAWN;
                    } else if (tempState == State.INTERVAL.ordinal()) {
                        _state = State.INTERVAL;
                    } else if (tempState == State.ALIVE.ordinal()) {
                        _state = State.ALIVE;
                    } else if (tempState == State.DEAD.ordinal()) {
                        _state = State.DEAD;
                    } else {
                        _state = State.NOTSPAWN;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
    }

    public void save() {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("REPLACE INTO epic_boss_spawn (bossId,respawnDate,state) VALUES(?,?,?)");
            statement.setInt(1, _bossId);
            statement.setInt(2, (int) (_respawnDate / 1000L));
            statement.setInt(3, _state.ordinal());
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void update() {
        Connection con = null;
        Statement statement = null;
        if(_bossId > 0) {
            try {
                con = DatabaseFactory.getInstance().getConnection();
                statement = con.createStatement();
                statement.executeUpdate("UPDATE epic_boss_spawn SET respawnDate=" + _respawnDate / 1000L + ", state=" + _state.ordinal() + " WHERE bossId=" + _bossId);
                final Date dt = new Date(_respawnDate);
                LOGGER.info("update EpicBossState: ID:" + _bossId + ", RespawnDate:" + dt + ", State:" + _state);
            } catch (Exception e) {
                LOGGER.error("Exception on update EpicBossState: ID " + _bossId + ", RespawnDate:" + _respawnDate / 1000L + ", State:" + _state, e);
            } finally {
                DbUtils.closeQuietly(con, statement);
            }
        }
    }

    public void setNextRespawnDate(final long newRespawnDate) {
        _respawnDate = newRespawnDate;
    }

    public long getInterval() {
        final long interval = _respawnDate - System.currentTimeMillis();
        return (interval > 0L) ? interval : 0L;
    }

    public enum State {
        NOTSPAWN,
        ALIVE,
        DEAD,
        INTERVAL
    }
}
