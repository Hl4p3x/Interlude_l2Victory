package ru.j2dev.commons.dbutils;

import com.stringer.annotations.HideAccess;
import com.stringer.annotations.StringEncryption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.util.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Created by JunkyFunky
 * on 09.07.2018 10:44
 * group j2dev
 */
@HideAccess
@StringEncryption
public class SqlTableOptimizer {
    private static final Logger LOGGER = LoggerFactory.getLogger(SqlTableOptimizer.class);
    /**
     * Оптимизировать таблицу — значит, сжать ее, то есть физически освободить файл таблицы от удаленных данных.
     */
    public static void optimizeTables(final Connection con) {
        Statement st = null;
        ResultSet rs = null;
        try {
            st = con.createStatement();

            final ArrayList<String> tablesList = new ArrayList<>();
            // получаем список таблиц
            rs = st.executeQuery("SHOW FULL TABLES");
            while (rs.next()) {
                final String tableType = rs.getString(2/* "Table_type" */);
                if (tableType.equals("VIEW"))
                    continue;

                tablesList.add(rs.getString(1));
            }
            rs.close();

            final String all_tables = StringUtils.join(tablesList.toArray(new String[0]), ",");

            rs = st.executeQuery("CHECK TABLE " + all_tables);
            while (rs.next()) {
                final String table = rs.getString("Table");
                final String msgType = rs.getString("Msg_type");
                final String msgText = rs.getString("Msg_text");

                if (msgType.equals("status"))
                    if (msgText.equals("OK"))
                        continue;

                LOGGER.warn("SqlTable Optimizer: CHECK TABLE "+table+": "+msgType+" -> "+msgText);
            }
            rs.close();
            LOGGER.info("SqlTable Optimizer: Database tables have been checked.");

            rs = st.executeQuery("ANALYZE TABLE " + all_tables);
            while (rs.next()) {
                final String table = rs.getString("Table");
                final String msgType = rs.getString("Msg_type");
                final String msgText = rs.getString("Msg_text");

                if (msgType.equals("status"))
                    if (msgText.equals("OK") || msgText.equals("Table is already up to date"))
                        continue;

                if (msgType.equals("note"))
                    if (msgText.equals("The storage engine for the table doesn't support analyze"))
                        continue;

                LOGGER.warn("SqlTable Optimizer: ANALYZE TABLE "+table+": "+msgType+" -> "+msgText);
            }
            rs.close();
            LOGGER.info("SqlTable Optimizer: Database tables have been analyzed.");

            rs = st.executeQuery("OPTIMIZE TABLE " + all_tables);
            while (rs.next()) {
                final String table = rs.getString("Table");
                final String msgType = rs.getString("Msg_type");
                final String msgText = rs.getString("Msg_text");

                if (msgType.equals("status"))
                    if (msgText.equals("OK") || msgText.equals("Table is already up to date"))
                        continue;

                if (msgType.equals("note"))
                    if (msgText.equals("Table does not support optimize, doing recreate + analyze instead"))
                        continue;

                LOGGER.warn("SqlTable Optimizer: OPTIMIZE TABLE "+table+": "+msgType+" -> "+msgText);
            }
            LOGGER.info("SqlTable Optimizer: Database tables have been optimized.");
        } catch (final Exception e) {
            LOGGER.warn("SqlTable Optimizer: Cannot optimize database tables!", e);
        } finally {
            DbUtils.closeQuietly(con, st, rs);
        }
    }

    public static void repairTables(final Connection con) {
        Statement st = null;
        ResultSet rs = null;
        try {
            st = con.createStatement();

            final ArrayList<String> tablesList = new ArrayList<>();
            // получаем список таблиц
            rs = st.executeQuery("SHOW FULL TABLES");
            while (rs.next()) {
                final String tableType = rs.getString(2/* "Table_type" */);
                if (tableType.equals("VIEW"))
                    continue;

                tablesList.add(rs.getString(1));
            }
            rs.close();

            final String all_tables = StringUtils.join(tablesList.toArray(new String[0]), ",");

            rs = st.executeQuery("REPAIR TABLE " + all_tables + " EXTENDED");
            while (rs.next()) {
                final String table = rs.getString("Table");
                final String msgType = rs.getString("Msg_type");
                final String msgText = rs.getString("Msg_text");

                if (msgType.equals("status"))
                    if (msgText.equals("OK"))
                        continue;

                if (msgType.equals("note"))
                    if (msgText.equals("The storage engine for the table doesn't support repair"))
                        continue;

                LOGGER.warn("SqlTable Optimizer: REPAIR TABLE "+table+": "+msgType+" -> "+msgText);
            }
            LOGGER.info("SqlTable Optimizer: Database tables have been repaired.");
        } catch (final Exception e) {
            LOGGER.warn("SqlTable Optimizer: Cannot optimize database tables!", e);
        } finally {
            DbUtils.closeQuietly(con, st, rs);
        }
    }
}
