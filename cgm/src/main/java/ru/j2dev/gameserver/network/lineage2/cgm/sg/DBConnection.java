package ru.j2dev.gameserver.network.lineage2.cgm.sg;

import ru.akumu.smartguard.core.wrappers.db.IConnection;
import ru.j2dev.commons.dbutils.DbUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBConnection extends IConnection {
    private Connection _conn;

    public DBConnection(final Connection conn) {
        _conn = conn;
    }

    @Override
    public PreparedStatement prepareStatement(final String pstmt) throws SQLException {
        return _conn.prepareStatement(pstmt);
    }

    @Override
    public void close() {
        DbUtils.closeQuietly(_conn);
    }
}