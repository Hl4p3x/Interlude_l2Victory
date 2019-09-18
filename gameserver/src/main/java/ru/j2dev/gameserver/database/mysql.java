package ru.j2dev.gameserver.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class mysql {
    private static final Logger LOGGER = LoggerFactory.getLogger(mysql.class);

    public static boolean setEx(DatabaseFactory db, final String query, final Object... vars) {
        Connection con = null;
        Statement statement = null;
        PreparedStatement pstatement = null;
        try {
            if (db == null) {
                db = DatabaseFactory.getInstance();
            }
            con = db.getConnection();
            if (vars.length == 0) {
                statement = con.createStatement();
                statement.executeUpdate(query);
            } else {
                pstatement = con.prepareStatement(query);
                setVars(pstatement, vars);
                pstatement.executeUpdate();
            }
        } catch (Exception e) {
            LOGGER.warn("Could not execute update '" + query + "': " + e);
            e.printStackTrace();
            return false;
        } finally {
            DbUtils.closeQuietly(con, (vars.length == 0) ? statement : pstatement);
        }
        return true;
    }

    public static void setVars(final PreparedStatement statement, final Object... vars) throws SQLException {
        for (int i = 0; i < vars.length; ++i) {
            if (vars[i] instanceof Number) {
                final Number n = (Number) vars[i];
                final long long_val = n.longValue();
                final double double_val = n.doubleValue();
                if (long_val == double_val) {
                    statement.setLong(i + 1, long_val);
                } else {
                    statement.setDouble(i + 1, double_val);
                }
            } else if (vars[i] instanceof String) {
                statement.setString(i + 1, (String) vars[i]);
            }
        }
    }

    public static boolean set(final String query, final Object... vars) {
        return setEx(null, query, vars);
    }

    public static boolean set(final String query) {
        return setEx(null, query);
    }

    public static Object get(final String query) {
        Object ret = null;
        Connection con = null;
        Statement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.createStatement();
            rset = statement.executeQuery(query + " LIMIT 1");
            final ResultSetMetaData md = rset.getMetaData();
            if (rset.next()) {
                if (md.getColumnCount() > 1) {
                    final Map<String, Object> tmp = new HashMap<>();
                    for (int i = md.getColumnCount(); i > 0; --i) {
                        tmp.put(md.getColumnName(i), rset.getObject(i));
                    }
                    ret = tmp;
                } else {
                    ret = rset.getObject(1);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Could not execute query '" + query + "': " + e);
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
        return ret;
    }

    public static List<Map<String, Object>> getAll(final String query) {
        final List<Map<String, Object>> ret = new ArrayList<>();
        Connection con = null;
        Statement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.createStatement();
            rset = statement.executeQuery(query);
            final ResultSetMetaData md = rset.getMetaData();
            while (rset.next()) {
                final Map<String, Object> tmp = new HashMap<>();
                for (int i = md.getColumnCount(); i > 0; --i) {
                    tmp.put(md.getColumnName(i), rset.getObject(i));
                }
                ret.add(tmp);
            }
        } catch (Exception e) {
            LOGGER.warn("Could not execute query '" + query + "': " + e);
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
        return ret;
    }

    public static List<Object> get_array(DatabaseFactory db, final String query) {
        final List<Object> ret = new ArrayList<>();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            if (db == null) {
                db = DatabaseFactory.getInstance();
            }
            con = db.getConnection();
            statement = con.prepareStatement(query);
            rset = statement.executeQuery();
            final ResultSetMetaData md = rset.getMetaData();
            while (rset.next()) {
                if (md.getColumnCount() > 1) {
                    final Map<String, Object> tmp = new HashMap<>();
                    for (int i = 0; i < md.getColumnCount(); ++i) {
                        tmp.put(md.getColumnName(i + 1), rset.getObject(i + 1));
                    }
                    ret.add(tmp);
                } else {
                    ret.add(rset.getObject(1));
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Could not execute query '" + query + "': " + e);
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
        return ret;
    }

    public static List<Object> get_array(final String query) {
        return get_array(null, query);
    }

    public static int simple_get_int(final String ret_field, final String table, final String where) {
        final String query = "SELECT " + ret_field + " FROM `" + table + "` WHERE " + where + " LIMIT 1;";
        int res = 0;
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(query);
            rset = statement.executeQuery();
            if (rset.next()) {
                res = rset.getInt(1);
            }
        } catch (Exception e) {
            LOGGER.warn("mSGI: Error in query '" + query + "':" + e);
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
        return res;
    }

    public static Integer[][] simple_get_int_array(DatabaseFactory db, final String[] ret_fields, final String table, final String where) {
        StringBuilder fields = null;
        for (final String field : ret_fields) {
            if (fields != null) {
                fields.append(",");
                fields.append("`").append(field).append("`");
            } else {
                fields = new StringBuilder("`" + field + "`");
            }
        }
        final String query = "SELECT " + fields + " FROM `" + table + "` WHERE " + where;
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        Integer[][] res = null;
        try {
            if (db == null) {
                db = DatabaseFactory.getInstance();
            }
            con = db.getConnection();
            statement = con.prepareStatement(query);
            rset = statement.executeQuery();
            final List<Integer[]> al = new ArrayList<>();
            int row = 0;
            while (rset.next()) {
                final Integer[] tmp = new Integer[ret_fields.length];
                for (int i = 0; i < ret_fields.length; ++i) {
                    tmp[i] = rset.getInt(i + 1);
                }
                al.add(row, tmp);
                ++row;
            }
            res = al.toArray(new Integer[row][ret_fields.length]);
        } catch (Exception e) {
            LOGGER.warn("mSGIA: Error in query '" + query + "':" + e);
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
        return res;
    }

    public static Integer[][] simple_get_int_array(final String[] ret_fields, final String table, final String where) {
        return simple_get_int_array(null, ret_fields, table, where);
    }
}
