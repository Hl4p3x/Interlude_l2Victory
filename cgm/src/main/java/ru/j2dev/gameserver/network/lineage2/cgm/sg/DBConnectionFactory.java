package ru.j2dev.gameserver.network.lineage2.cgm.sg;

import ru.akumu.smartguard.core.wrappers.db.IConnection;
import ru.akumu.smartguard.core.wrappers.db.IConnectionFactory;
import ru.j2dev.gameserver.database.DatabaseFactory;

import java.sql.SQLException;

public class DBConnectionFactory extends IConnectionFactory {
    private static final DBConnectionFactory INSTANCE = new DBConnectionFactory();

    public static DBConnectionFactory getInstance() {
        return DBConnectionFactory.INSTANCE;
    }

    @Override
    public IConnection getConnection() throws SQLException {
        return new DBConnection(DatabaseFactory.getInstance().getConnection());
    }
}