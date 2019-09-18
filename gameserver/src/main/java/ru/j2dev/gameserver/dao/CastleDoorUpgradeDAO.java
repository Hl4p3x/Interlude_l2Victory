package ru.j2dev.gameserver.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.database.DatabaseFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CastleDoorUpgradeDAO {
    private static final String SELECT_SQL_QUERY = "SELECT hp FROM castle_door_upgrade WHERE door_id=?";
    private static final String REPLACE_SQL_QUERY = "REPLACE INTO castle_door_upgrade (door_id, hp) VALUES (?,?)";
    private static final String DELETE_SQL_QUERY = "DELETE FROM castle_door_upgrade WHERE door_id=?";
    private static final Logger LOGGER = LoggerFactory.getLogger(CastleDoorUpgradeDAO.class);


    public static CastleDoorUpgradeDAO getInstance() {
        return LazyHolder.INSTANCE;
    }

    public int load(final int doorId) {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(SELECT_SQL_QUERY);
            statement.setInt(1, doorId);
            rset = statement.executeQuery();
            if (rset.next()) {
                return rset.getInt("hp");
            }
        } catch (Exception e) {
            LOGGER.error("CastleDoorUpgradeDAO:load(int): " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
        return 0;
    }

    public void insert(final int uId, final int val) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(REPLACE_SQL_QUERY);
            statement.setInt(1, uId);
            statement.setInt(2, val);
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("CastleDoorUpgradeDAO:insert(int, int): " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void delete(final int uId) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(DELETE_SQL_QUERY);
            statement.setInt(1, uId);
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("CastleDoorUpgradeDAO:delete(int): " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    private static class LazyHolder {
        private static final CastleDoorUpgradeDAO INSTANCE = new CastleDoorUpgradeDAO();
    }
}
