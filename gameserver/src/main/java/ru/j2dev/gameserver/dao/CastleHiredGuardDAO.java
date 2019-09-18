package ru.j2dev.gameserver.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.model.entity.residence.Castle;
import ru.j2dev.gameserver.model.entity.residence.Residence;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CastleHiredGuardDAO {
    private static final String SELECT_SQL_QUERY = "SELECT * FROM castle_hired_guards WHERE residence_id=?";
    private static final String INSERT_SQL_QUERY = "INSERT INTO castle_hired_guards(residence_id, item_id, x, y, z) VALUES (?, ?, ?, ?, ?)";
    private static final String DELETE_SQL_QUERY = "DELETE FROM castle_hired_guards WHERE residence_id=?";
    private static final String DELETE_SQL_QUERY2 = "DELETE FROM castle_hired_guards WHERE residence_id=? AND item_id=? AND x=? AND y=? AND z=?";
    private static final Logger LOGGER = LoggerFactory.getLogger(CastleHiredGuardDAO.class);


    public static CastleHiredGuardDAO getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void load(final Castle r) {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(SELECT_SQL_QUERY);
            statement.setInt(1, r.getId());
            rset = statement.executeQuery();
            while (rset.next()) {
                final int itemId = rset.getInt("item_id");
                final Location loc = new Location(rset.getInt("x"), rset.getInt("y"), rset.getInt("z"));
                final ItemInstance item = ItemFunctions.createItem(itemId);
                item.spawnMe(loc);
                r.getSpawnMerchantTickets().add(item);
            }
        } catch (Exception e) {
            LOGGER.error("CastleHiredGuardDAO:load(Castle): " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
    }

    public void insert(final Residence residence, final int itemId, final Location loc) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(INSERT_SQL_QUERY);
            statement.setInt(1, residence.getId());
            statement.setInt(2, itemId);
            statement.setInt(3, loc.x);
            statement.setInt(4, loc.y);
            statement.setInt(5, loc.z);
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("CastleHiredGuardDAO:insert(Residence, int, Location): " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void delete(final Residence residence, final ItemInstance item) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(DELETE_SQL_QUERY2);
            statement.setInt(1, residence.getId());
            statement.setInt(2, item.getItemId());
            statement.setInt(3, item.getLoc().x);
            statement.setInt(4, item.getLoc().y);
            statement.setInt(5, item.getLoc().z);
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("CastleHiredGuardDAO:delete(Residence): " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void delete(final Residence residence) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(DELETE_SQL_QUERY);
            statement.setInt(1, residence.getId());
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("CastleHiredGuardDAO:delete(Residence): " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    private static class LazyHolder {
        private static final CastleHiredGuardDAO INSTANCE = new CastleHiredGuardDAO();
    }
}
