package ru.j2dev.commons.dbutils;

import com.stringer.annotations.HideAccess;
import com.stringer.annotations.StringEncryption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.file.SQLFilter;

import java.io.File;
import java.nio.file.Path;
import java.sql.*;
import java.util.Objects;
import java.util.Scanner;

/**
 * Created by JunkyFunky
 * on 26.10.2016.
 * group j2dev
 */
@HideAccess
@StringEncryption
public class SqlInstaller {
    private static final Logger LOGGER = LoggerFactory.getLogger(SqlInstaller.class);

    public static void checkDatabase(Connection connection, Path fileDir) throws SQLException {
        final File dir = fileDir.toFile();
        final DatabaseMetaData meta = connection.getMetaData();
        ResultSet resultSet = null;
        try {
            for (final File f : Objects.requireNonNull(dir.listFiles(new SQLFilter()))) {
                resultSet = meta.getTables(null, null, f.getName().replace(".sql", ""), null);
                if (resultSet.next()) {
                    continue;
                }
                execSqlFile(connection, f);
            }
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.close(connection, resultSet);
        }
    }

    public static void execSqlFile(final Connection connection, final File file) throws SQLException {
        Statement statement = null;
        try {
            String string;
            statement = connection.createStatement();
            final Scanner scanner = new Scanner(file);
            StringBuilder stringBuilder = new StringBuilder();
            while (scanner.hasNextLine()) {
                string = scanner.nextLine();
                if (string.startsWith("--")) {
                    continue;
                }
                if (string.contains("--")) {
                    string = string.split("--")[0];
                }
                if (!(string = string.trim()).isEmpty()) {
                    stringBuilder.append(string).append('\n');
                }
                if (!string.endsWith(";")) {
                    continue;
                }
                statement.execute(stringBuilder.toString());
                stringBuilder = new StringBuilder();
            }
            scanner.close();
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.close(statement);
        }
        LOGGER.info("Install {}", file.getName());
    }

    public void execSqlFiles(final Connection connection, final File file) throws SQLException {
        for (final File file2 : Objects.requireNonNull(file.listFiles(new SQLFilter()))) {
            if (file2.isDirectory()) {
                continue;
            }
            execSqlFile(connection, file2);
            LOGGER.info("Install {}", file2.getName());
        }
    }
}
