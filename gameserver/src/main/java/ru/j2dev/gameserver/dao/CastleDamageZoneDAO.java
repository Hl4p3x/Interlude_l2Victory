package ru.j2dev.gameserver.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.model.entity.residence.Residence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CastleDamageZoneDAO {
    private static final String SELECT_SQL_QUERY = "SELECT zone FROM castle_damage_zones WHERE residence_id=?";
    private static final String INSERT_SQL_QUERY = "INSERT INTO castle_damage_zones (residence_id, zone) VALUES (?,?)";
    private static final String DELETE_SQL_QUERY = "DELETE FROM castle_damage_zones WHERE residence_id=?";
    private static final Logger LOGGER = LoggerFactory.getLogger(CastleDoorUpgradeDAO.class);


    public static CastleDamageZoneDAO getInstance() {
        return LazyHolder.INSTANCE;
    }


    public List<String> load(final Residence r) {
        List<String> set = Collections.emptyList();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(SELECT_SQL_QUERY);
            statement.setInt(1, r.getId());
            rset = statement.executeQuery();
            set = new ArrayList<>();
            while (rset.next()) {
                set.add(rset.getString("zone"));
            }
        } catch (Exception e) {
            LOGGER.error("CastleDamageZoneDAO:load(Residence): " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
        return set;
    }

    public void insert(final Residence residence, final String name) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(INSERT_SQL_QUERY);
            statement.setInt(1, residence.getId());
            statement.setString(2, name);
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("CastleDamageZoneDAO:insert(Residence, String): " + e, e);
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
            LOGGER.error("CastleDamageZoneDAO:delete(Residence): " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    private static class LazyHolder {
        private static final CastleDamageZoneDAO INSTANCE = new CastleDamageZoneDAO();
    }
}
