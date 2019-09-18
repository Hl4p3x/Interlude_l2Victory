package ru.j2dev.gameserver.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dao.JdbcEntityState;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.model.entity.residence.Castle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CastleDAO {
    private static final String SELECT_SQL_QUERY = "SELECT tax_percent, treasury, reward_count, siege_date, last_siege_date, own_date FROM castle WHERE id=? LIMIT 1";
    private static final String UPDATE_SQL_QUERY = "UPDATE castle SET tax_percent=?, treasury=?, reward_count=?, siege_date=?, last_siege_date=?, own_date=? WHERE id=?";
    private static final Logger LOGGER = LoggerFactory.getLogger(CastleDAO.class);


    public static CastleDAO getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void select(final Castle castle) {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(SELECT_SQL_QUERY);
            statement.setInt(1, castle.getId());
            rset = statement.executeQuery();
            if (rset.next()) {
                castle.setTaxPercent(rset.getInt("tax_percent"));
                castle.setTreasury(rset.getLong("treasury"));
                castle.setRewardCount(rset.getInt("reward_count"));
                castle.getSiegeDate().setTimeInMillis(rset.getLong("siege_date"));
                castle.getLastSiegeDate().setTimeInMillis(rset.getLong("last_siege_date"));
                castle.getOwnDate().setTimeInMillis(rset.getLong("own_date"));
            }
        } catch (Exception e) {
            LOGGER.error("CastleDAO.select(Castle):" + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
    }

    public void update(final Castle residence) {
        if (!residence.getJdbcState().isUpdatable()) {
            return;
        }
        update0(residence);
        residence.setJdbcState(JdbcEntityState.STORED);
    }

    private void update0(final Castle castle) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(UPDATE_SQL_QUERY);
            statement.setInt(1, castle.getTaxPercent0());
            statement.setLong(2, castle.getTreasury());
            statement.setInt(3, castle.getRewardCount());
            statement.setLong(4, castle.getSiegeDate().getTimeInMillis());
            statement.setLong(5, castle.getLastSiegeDate().getTimeInMillis());
            statement.setLong(6, castle.getOwnDate().getTimeInMillis());
            statement.setInt(7, castle.getId());
            statement.execute();
        } catch (Exception e) {
            LOGGER.warn("CastleDAO#update0(Castle): " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    private static class LazyHolder {
        private static final CastleDAO INSTANCE = new CastleDAO();
    }
}
