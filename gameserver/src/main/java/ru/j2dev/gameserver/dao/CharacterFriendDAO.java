package ru.j2dev.gameserver.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.actor.instances.player.Friend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class CharacterFriendDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(CharacterFriendDAO.class);
    private static final String SELECT_F_FRIEND = "SELECT f.friend_id, c.char_name, s.class_id, s.level FROM character_friends f LEFT JOIN characters c ON f.friend_id = c.obj_Id LEFT JOIN character_subclasses s ON ( f.friend_id = s.char_obj_id AND s.active =1 ) WHERE f.char_id = ?";
    private static final String INSERT_INTO_CHARACTER_FRIENDS = "INSERT INTO character_friends (char_id,friend_id) VALUES(?,?)";
    private static final String DELETE_FROM_CHARACTER_FRIENDS = "DELETE FROM character_friends WHERE (char_id=? AND friend_id=?) OR (char_id=? AND friend_id=?)";


    public static CharacterFriendDAO getInstance() {
        return LazyHolder.INSTANCE;
    }


    public Map<Integer, Friend> select(final Player owner) {
        final Map<Integer, Friend> map = new HashMap<>();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(SELECT_F_FRIEND);
            statement.setInt(1, owner.getObjectId());
            rset = statement.executeQuery();
            while (rset.next()) {
                final int objectId = rset.getInt("f.friend_id");
                final String name = rset.getString("c.char_name");
                final int classId = rset.getInt("s.class_id");
                final int level = rset.getInt("s.level");
                map.put(objectId, new Friend(objectId, name, level, classId));
            }
        } catch (Exception e) {
            LOGGER.error("CharacterFriendDAO.load(L2Player): " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
        return map;
    }

    public void insert(final Player owner, final Player friend) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(INSERT_INTO_CHARACTER_FRIENDS);
            statement.setInt(1, owner.getObjectId());
            statement.setInt(2, friend.getObjectId());
            statement.execute();
        } catch (Exception e) {
            LOGGER.warn(owner.getFriendList() + " could not add friend objectid: " + friend.getObjectId(), e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void delete(final Player owner, final int friend) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(DELETE_FROM_CHARACTER_FRIENDS);
            statement.setInt(1, owner.getObjectId());
            statement.setInt(2, friend);
            statement.setInt(3, friend);
            statement.setInt(4, owner.getObjectId());
            statement.execute();
        } catch (Exception e) {
            LOGGER.warn("FriendList: could not delete friend objectId: " + friend + " ownerId: " + owner.getObjectId(), e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    private static class LazyHolder {
        private static final CharacterFriendDAO INSTANCE = new CharacterFriendDAO();
    }
}
