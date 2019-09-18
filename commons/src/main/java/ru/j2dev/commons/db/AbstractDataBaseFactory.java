package ru.j2dev.commons.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Created by JunkyFunky
 * on 17.11.2017 17:22
 * group j2dev
 */
public abstract class AbstractDataBaseFactory {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private HikariDataSource dataSource;
    private HikariConfig config;

    public void initPool(final String name) {
        try {
            config = new HikariConfig(getConfigFile());
            config.setPoolName(name+" db connection");
            config.setValidationTimeout(TimeUnit.SECONDS.toMillis(30));
            dataSource = new HikariDataSource(config);
        } catch (final RuntimeException e) {
            LOGGER.warn("Could not init database connection.", e);
        }
    }

    public void shutdown() {
        dataSource.close();
    }

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (final SQLException e) {
            LOGGER.warn("Can't get connection from database", e);
        }

        return null;
    }

    public Properties getDataSourceProperies() {
        return config.getDataSourceProperties();
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }

    protected abstract String getConfigFile();
}