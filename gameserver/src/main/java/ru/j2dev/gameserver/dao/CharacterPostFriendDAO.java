package ru.j2dev.gameserver.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.model.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CharacterPostFriendDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(CharacterPostFriendDAO.class);
    private static final String SELECT_SQL_QUERY = "SELECT pf.post_friend, c.char_name FROM character_post_friends pf LEFT JOIN characters c ON pf.post_friend = c.obj_Id WHERE pf.object_id = ?";
    private static final String INSERT_SQL_QUERY = "INSERT INTO character_post_friends(object_id, post_friend) VALUES (?,?)";
    private static final String DELETE_SQL_QUERY = "DELETE FROM character_post_friends WHERE object_id=? AND post_friend=?";


    public static CharacterPostFriendDAO getInstance() {
        return LazyHolder.INSTANCE;
    }


    public ConcurrentMap<Integer, String> select(final Player player) {
        final ConcurrentMap<Integer, String> set = new ConcurrentHashMap<>();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(SELECT_SQL_QUERY);
            statement.setInt(1, player.getObjectId());
            rset = statement.executeQuery();
            while (rset.next()) {
                final String name = rset.getString(2);
                if (name != null) {
                    set.put(rset.getInt(1), rset.getString(2));
                }
            }
        } catch (Exception e) {
            LOGGER.error("CharacterPostFriendDAO.load(Player): " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
        return set;
    }

    public void insert(final Player player, final int val) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(INSERT_SQL_QUERY);
            statement.setInt(1, player.getObjectId());
            statement.setInt(2, val);
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("CharacterPostFriendDAO.insert(Player, int): " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void delete(final Player player, final int val) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(DELETE_SQL_QUERY);
            statement.setInt(1, player.getObjectId());
            statement.setInt(2, val);
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("CharacterPostFriendDAO.delete(Player, int): " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    private static class LazyHolder {
        private static final CharacterPostFriendDAO INSTANCE = new CharacterPostFriendDAO();
    }
}
