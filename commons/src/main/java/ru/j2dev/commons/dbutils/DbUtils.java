package ru.j2dev.commons.dbutils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DbUtils {

    public static void close(final Connection conn) throws SQLException {
        if (conn != null) {
            conn.close();
        }
    }

    public static void close(final ResultSet rs) throws SQLException {
        if (rs != null) {
            rs.close();
        }
    }

    public static void close(final Statement stmt) throws SQLException {
        if (stmt != null) {
            stmt.clearBatch();
            stmt.close();
        }
    }

    public static void close(final Statement stmt, final ResultSet rs) throws SQLException {
        close(stmt);
        close(rs);
    }

    public static void close(final Connection connection, final ResultSet rs) throws SQLException {
        close(connection);
        close(rs);
    }

    public static void closeQuietly(final Connection conn) {
        try {
            close(conn);
        } catch (SQLException ignored) {
        }
    }

    public static void closeQuietly(final Connection conn, final Statement stmt) {
        try {
            closeQuietly(stmt);
        } finally {
            closeQuietly(conn);
        }
    }

    public static void closeQuietly(final Statement stmt, final ResultSet rs) {
        try {
            closeQuietly(stmt);
        } finally {
            closeQuietly(rs);
        }
    }

    public static void closeQuietly(final Connection conn, final Statement stmt, final ResultSet rs) {
        try {
            closeQuietly(rs);
        } finally {
            try {
                closeQuietly(stmt);
            } finally {
                closeQuietly(conn);
            }
        }
    }

    public static void closeQuietly(final ResultSet rs) {
        try {
            close(rs);
        } catch (SQLException ignored) {
        }
    }

    public static void closeQuietly(final Statement stmt) {
        try {
            close(stmt);
        } catch (SQLException ignored) {
        }
    }

}
