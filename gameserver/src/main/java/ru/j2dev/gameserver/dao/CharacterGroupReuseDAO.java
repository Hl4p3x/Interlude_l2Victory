package ru.j2dev.gameserver.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.commons.dbutils.SqlBatch;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.skills.TimeStamp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Map;

public class CharacterGroupReuseDAO {
    private static final String DELETE_SQL_QUERY = "DELETE FROM character_group_reuse WHERE object_id=?";
    private static final String SELECT_SQL_QUERY = "SELECT * FROM character_group_reuse WHERE object_id=?";
    private static final String INSERT_SQL_QUERY = "REPLACE INTO `character_group_reuse` (`object_id`,`reuse_group`,`item_id`,`end_time`,`reuse`) VALUES";
    private static final Logger LOGGER = LoggerFactory.getLogger(CharacterGroupReuseDAO.class);


    public static CharacterGroupReuseDAO getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void select(final Player player) {
        final long curTime = System.currentTimeMillis();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(SELECT_SQL_QUERY);
            statement.setInt(1, player.getObjectId());
            rset = statement.executeQuery();
            while (rset.next()) {
                final int group = rset.getInt("reuse_group");
                final int item_id = rset.getInt("item_id");
                final long endTime = rset.getLong("end_time");
                final long reuse = rset.getLong("reuse");
                if (endTime - curTime > 500L) {
                    final TimeStamp stamp = new TimeStamp(item_id, endTime, reuse);
                    player.addSharedGroupReuse(group, stamp);
                }
            }
            DbUtils.close(statement);
            statement = con.prepareStatement(DELETE_SQL_QUERY);
            statement.setInt(1, player.getObjectId());
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("CharacterGroupReuseDAO.select(L2Player):", e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
    }

    public void insert(final Player player) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(DELETE_SQL_QUERY);
            statement.setInt(1, player.getObjectId());
            statement.execute();
            final Collection<Map.Entry<Integer, TimeStamp>> reuses = player.getSharedGroupReuses();
            if (reuses.isEmpty()) {
                return;
            }
            final SqlBatch b = new SqlBatch(INSERT_SQL_QUERY);
            synchronized (reuses) {
                reuses.forEach(entry -> {
                    final int group = entry.getKey();
                    final TimeStamp timeStamp = entry.getValue();
                    if (timeStamp.hasNotPassed()) {
                        String sb = "(" + player.getObjectId() + "," +
                                group + "," +
                                timeStamp.getId() + "," +
                                timeStamp.getEndTime() + "," +
                                timeStamp.getReuseBasic() + ")";
                        b.write(sb);
                    }
                });
            }
            if (!b.isEmpty()) {
                statement.executeUpdate(b.close());
            }
        } catch (Exception e) {
            LOGGER.error("CharacterGroupReuseDAO.insert(L2Player):", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    private static class LazyHolder {
        private static final CharacterGroupReuseDAO INSTANCE = new CharacterGroupReuseDAO();
    }
}
