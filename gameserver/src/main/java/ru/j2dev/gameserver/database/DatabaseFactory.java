package ru.j2dev.gameserver.database;

import ru.j2dev.commons.db.AbstractDataBaseFactory;

public class DatabaseFactory extends AbstractDataBaseFactory {

    public static DatabaseFactory getInstance() {
        return DataSourceHolder.INSTANCE;
    }

    @Override
    public String getConfigFile() {
        return "config/database.ini";
    }

    private static class DataSourceHolder {
        private static final DatabaseFactory INSTANCE = new DatabaseFactory();
    }
}