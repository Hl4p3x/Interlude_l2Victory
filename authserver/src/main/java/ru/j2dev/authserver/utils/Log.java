package ru.j2dev.authserver.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.authserver.Config;
import ru.j2dev.authserver.accounts.Account;
import ru.j2dev.authserver.database.DatabaseFactory;
import ru.j2dev.commons.dbutils.DbUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class Log {
    private static final Logger LOGGER = LoggerFactory.getLogger(Log.class);

    public static void LogAccount(final Account account) {
        if (!Config.LOGIN_LOG) {
            return;
        }
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("INSERT INTO account_log (time, login, ip) VALUES(?,?,?)");
            statement.setInt(1, account.getLastAccess());
            statement.setString(2, account.getLogin());
            statement.setString(3, account.getLastIP());
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }
}
