package ru.j2dev.gameserver.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.model.entity.events.objects.SiegeClanObject;
import ru.j2dev.gameserver.model.entity.residence.Residence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SiegeClanDAO {
    private static final String SELECT_SQL_QUERY = "SELECT clan_id, param, date FROM siege_clans WHERE residence_id=? AND type=? ORDER BY date";
    private static final String INSERT_SQL_QUERY = "INSERT INTO siege_clans(residence_id, clan_id, param, type, date) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL_QUERY = "UPDATE siege_clans SET type=?, param=? WHERE residence_id=? AND clan_id=?";
    private static final String DELETE_SQL_QUERY = "DELETE FROM siege_clans WHERE residence_id=? AND clan_id=? AND type=?";
    private static final String DELETE_SQL_QUERY2 = "DELETE FROM siege_clans WHERE residence_id=?";
    private static final Logger LOGGER = LoggerFactory.getLogger(SiegeClanDAO.class);


    public static SiegeClanDAO getInstance() {
        return LazyHolder.INSTANCE;
    }


    public List<SiegeClanObject> load(final Residence residence, final String name) {
        List<SiegeClanObject> siegeClans = Collections.emptyList();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(SELECT_SQL_QUERY);
            statement.setInt(1, residence.getId());
            statement.setString(2, name);
            rset = statement.executeQuery();
            siegeClans = new ArrayList<>();
            while (rset.next()) {
                final int clanId = rset.getInt("clan_id");
                final long param = rset.getLong("param");
                final long date = rset.getLong("date");
                final SiegeClanObject object = residence.getSiegeEvent().newSiegeClan(name, clanId, param, date);
                if (object != null) {
                    siegeClans.add(object);
                } else {
                    LOGGER.info("SiegeClanDAO#load(Residence, String): null clan: " + clanId + "; residence: " + residence.getId());
                }
            }
        } catch (Exception e) {
            LOGGER.warn("SiegeClanDAO#load(Residence, String): " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
        return siegeClans;
    }

    public void insert(final Residence residence, final SiegeClanObject siegeClan) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(INSERT_SQL_QUERY);
            statement.setInt(1, residence.getId());
            statement.setInt(2, siegeClan.getObjectId());
            statement.setLong(3, siegeClan.getParam());
            statement.setString(4, siegeClan.getType());
            statement.setLong(5, siegeClan.getDate());
            statement.execute();
        } catch (Exception e) {
            LOGGER.warn("SiegeClanDAO#insert(Residence, SiegeClan): " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void delete(final Residence residence, final SiegeClanObject siegeClan) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(DELETE_SQL_QUERY);
            statement.setInt(1, residence.getId());
            statement.setInt(2, siegeClan.getObjectId());
            statement.setString(3, siegeClan.getType());
            statement.execute();
        } catch (Exception e) {
            LOGGER.warn("SiegeClanDAO#delete(Residence, SiegeClan): " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void delete(final Residence residence) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(DELETE_SQL_QUERY2);
            statement.setInt(1, residence.getId());
            statement.execute();
        } catch (Exception e) {
            LOGGER.warn("SiegeClanDAO#delete(Residence): " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void update(final Residence residence, final SiegeClanObject siegeClan) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(UPDATE_SQL_QUERY);
            statement.setString(1, siegeClan.getType());
            statement.setLong(2, siegeClan.getParam());
            statement.setInt(3, residence.getId());
            statement.setInt(4, siegeClan.getObjectId());
            statement.execute();
        } catch (Exception e) {
            LOGGER.warn("SiegeClanDAO#update(Residence, SiegeClan): " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    private static class LazyHolder {
        private static final SiegeClanDAO INSTANCE = new SiegeClanDAO();
    }
}
