package achievements;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.dao.CharacterDAO;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.listener.game.OnCharacterDeleteListener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class AchievementCounter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AchievementCounter.class);

    static {
        CharacterDAO.getInstance().getCharacterDeleteListenerList().add(new AchievementCounterOnCharacterDeleteListener());
    }

    private final int _objid;
    private final int _achId;

    public AchievementCounter(final int objid, final int achId) {
        _objid = objid;
        _achId = achId;
    }

    public static AchievementCounter makeDBStorableCounter(final int objid, final int achId) {
        return new AchievementCounterDb(objid, achId);
    }

    public int getObjid() {
        return _objid;
    }

    public int getAchId() {
        return _achId;
    }

    public abstract int getVal();

    public abstract void setVal(final int p0);

    public boolean isStorable() {
        return false;
    }

    public abstract void store();

    public int incrementAndGetValue() {
        setVal(getVal() + 1);
        return getVal();
    }

    private static class AchievementCounterOnCharacterDeleteListener implements OnCharacterDeleteListener {
        @Override
        public void onCharacterDelate(final int charObjId) {
            Connection con = null;
            PreparedStatement pstmt = null;
            try {
                con = DatabaseFactory.getInstance().getConnection();
                pstmt = con.prepareStatement("DELETE FROM `ex_achievements` WHERE `objId` = ?");
                pstmt.setInt(1, charObjId);
                pstmt.executeUpdate();
            } catch (SQLException se) {
                LOGGER.error("Can't delete counter for " + charObjId);
            } finally {
                DbUtils.closeQuietly(con, pstmt);
            }
        }
    }

    private static final class AchievementCounterDb extends AchievementCounter {
        private volatile Integer _val;

        public AchievementCounterDb(final int objid, final int achId) {
            super(objid, achId);
            _val = null;
        }

        private int getVal0() {
            Connection con = null;
            PreparedStatement pstmt = null;
            ResultSet rset = null;
            try {
                con = DatabaseFactory.getInstance().getConnection();
                pstmt = con.prepareStatement("SELECT `value` AS `value` FROM `ex_achievements` WHERE  `objId` = ? AND `achId` = ?");
                pstmt.setInt(1, getObjid());
                pstmt.setInt(2, getAchId());
                rset = pstmt.executeQuery();
                if (rset.next()) {
                    return rset.getInt("value");
                }
            } catch (SQLException se) {
                LOGGER.error("Can't load counter for " + getObjid() + "(" + getAchId() + ")", se);
            } finally {
                DbUtils.closeQuietly(con, pstmt, rset);
            }
            return 0;
        }

        @Override
        public int getVal() {
            if (_val == null) {
                _val = getVal0();
            }
            return _val;
        }

        @Override
        public void setVal(final int val) {
            _val = val;
        }

        @Override
        public boolean isStorable() {
            return true;
        }

        @Override
        public void store() {
            Connection con = null;
            PreparedStatement pstmt = null;
            try {
                con = DatabaseFactory.getInstance().getConnection();
                pstmt = con.prepareStatement("REPLACE INTO `ex_achievements` (`objId`, `achId`, `value`) VALUES (?, ?, ?)");
                pstmt.setInt(1, getObjid());
                pstmt.setInt(2, getAchId());
                pstmt.setInt(3, getVal());
                pstmt.executeUpdate();
            } catch (SQLException se) {
                LOGGER.error("Can't store counter for " + getObjid() + "(" + getAchId() + ")");
            } finally {
                DbUtils.closeQuietly(con, pstmt);
            }
        }
    }
}
