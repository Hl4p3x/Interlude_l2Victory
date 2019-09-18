package ru.j2dev.gameserver.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.collections.MultiValueSet;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.utils.Strings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CharacterVariablesDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(CharacterVariablesDAO.class);
    private static final String GET_VAR = "SELECT value FROM character_variables WHERE obj_id=? AND type=? AND name=?";
    private static final String SET_VAR = "REPLACE INTO character_variables (obj_id, type, name, value, expire_time) VALUES (?,?,?,?,?)";
    private static final String DELETE_VAR = "DELETE FROM character_variables WHERE obj_id=? AND type=? AND name=? LIMIT 1";
    private static final String DELETE_VARS = "DELETE FROM character_variables WHERE obj_id=?";
    private static final String LOAD_VARS = "SELECT name,value FROM character_variables WHERE obj_id=?";


    public static CharacterVariablesDAO getInstance() {
        return LazyHolder.INSTANCE;
    }


    public String getVar(final int objectId, final String name) {
        return getVar(objectId, name, "user-var");
    }


    public String getVar(final int objectId, final String name, final String type) {
        String value = null;
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(GET_VAR);
            statement.setInt(1, objectId);
            statement.setString(2, type);
            statement.setString(3, name);
            rs = statement.executeQuery();
            if (rs.next()) {
                value = Strings.stripSlashes(rs.getString("value"));
            }
        } catch (Exception e) {
            LOGGER.error("CharacterVariablesDAO.getVar(int,String,String): " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement, rs);
        }
        return value;
    }

    public void setVar(final int objectId, final String name, final String value, final long expiration) {
        setVar(objectId, name, "user-var", value, expiration);
    }

    public void setVar(final int objectId, final String name, final String type, final String value, final long expiration) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(SET_VAR);
            statement.setInt(1, objectId);
            statement.setString(2, type);
            statement.setString(3, name);
            statement.setString(4, value);
            statement.setLong(5, expiration);
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("CharacterVariablesDAO.setVar(int,String,String,String,long): " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void deleteVar(final int objectId, final String name) {
        deleteVar(objectId, name, "user-var");
    }

    public void deleteVar(final int objectId, final String name, final String type) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(DELETE_VAR);
            statement.setInt(1, objectId);
            statement.setString(2, type);
            statement.setString(3, name);
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("CharacterVariablesDAO.deleteVar(int,String,String): " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void deleteVars(final int objectId) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(DELETE_VARS);
            statement.setInt(1, objectId);
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("CharacterVariablesDAO.deleteVar(int): " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    protected void deleteVars0(final Connection con, final int objectId) {
        PreparedStatement statement = null;
        try {
            statement = con.prepareStatement(DELETE_VARS);
            statement.setInt(1, objectId);
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("CharacterVariablesDAO.deleteVar(int): " + e, e);
        } finally {
            DbUtils.closeQuietly(statement);
        }
    }

    public void loadVariables(final int objectId, final MultiValueSet<String> vars) {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(LOAD_VARS);
            statement.setInt(1, objectId);
            rs = statement.executeQuery();
            while (rs.next()) {
                final String name = rs.getString(1);
                final String value = Strings.stripSlashes(rs.getString(2));
                vars.put(name, value);
            }
        } catch (Exception e) {
            LOGGER.error("CharacterVariablesDAO.loadVariables(int,MultiValueSet<String>): " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement, rs);
        }
    }

    private static class LazyHolder {
        private static final CharacterVariablesDAO INSTANCE = new CharacterVariablesDAO();
    }
}
