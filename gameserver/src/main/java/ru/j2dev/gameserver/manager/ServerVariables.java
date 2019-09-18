package ru.j2dev.gameserver.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.templates.StatsSet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ServerVariables {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerVariables.class);
    private static StatsSet server_vars;

    private static StatsSet getVars() {
        if (server_vars == null) {
            server_vars = new StatsSet();
            LoadFromDB();
        }
        return server_vars;
    }

    private static void LoadFromDB() {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT * FROM server_variables");
            rs = statement.executeQuery();
            while (rs.next()) {
                server_vars.set(rs.getString("name"), rs.getString("value"));
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement, rs);
        }
    }

    private static void SaveToDB(final String name) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            final String value = getVars().getString(name, "");
            if (value.isEmpty()) {
                statement = con.prepareStatement("DELETE FROM server_variables WHERE name = ?");
                statement.setString(1, name);
                statement.execute();
            } else {
                statement = con.prepareStatement("REPLACE INTO server_variables (name, value) VALUES (?,?)");
                statement.setString(1, name);
                statement.setString(2, value);
                statement.execute();
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public static boolean getBool(final String name) {
        return getVars().getBool(name);
    }

    public static boolean getBool(final String name, final boolean defult) {
        return getVars().getBool(name, defult);
    }

    public static int getInt(final String name) {
        return getVars().getInteger(name);
    }

    public static int getInt(final String name, final int defult) {
        return getVars().getInteger(name, defult);
    }

    public static long getLong(final String name) {
        return getVars().getLong(name);
    }

    public static long getLong(final String name, final long defult) {
        return getVars().getLong(name, defult);
    }

    public static double getFloat(final String name) {
        return getVars().getDouble(name);
    }

    public static double getFloat(final String name, final double defult) {
        return getVars().getDouble(name, defult);
    }

    public static String getString(final String name) {
        return getVars().getString(name);
    }

    public static String getString(final String name, final String defult) {
        return getVars().getString(name, defult);
    }

    public static void set(final String name, final boolean value) {
        getVars().set(name, value);
        SaveToDB(name);
    }

    public static void set(final String name, final int value) {
        getVars().set(name, value);
        SaveToDB(name);
    }

    public static void set(final String name, final long value) {
        getVars().set(name, value);
        SaveToDB(name);
    }

    public static void set(final String name, final double value) {
        getVars().set(name, value);
        SaveToDB(name);
    }

    public static void set(final String name, final String value) {
        getVars().set(name, value);
        SaveToDB(name);
    }

    public static void unset(final String name) {
        getVars().unset(name);
        SaveToDB(name);
    }
}
