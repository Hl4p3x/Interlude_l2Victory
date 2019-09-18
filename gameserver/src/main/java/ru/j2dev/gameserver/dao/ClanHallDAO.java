package ru.j2dev.gameserver.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dao.JdbcEntityState;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.model.entity.residence.ClanHall;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ClanHallDAO {
    private static final String SELECT_SQL_QUERY = "SELECT siege_date, own_date, last_siege_date, auction_desc, auction_length, auction_min_bid, cycle, paid_cycle FROM clanhall WHERE id = ?";
    private static final String UPDATE_SQL_QUERY = "UPDATE clanhall SET siege_date=?, last_siege_date=?, own_date=?, auction_desc=?, auction_length=?, auction_min_bid=?, cycle=?, paid_cycle=? WHERE id=?";
    private static final Logger LOGGER = LoggerFactory.getLogger(ClanHallDAO.class);


    public static ClanHallDAO getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void select(final ClanHall clanHall) {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(SELECT_SQL_QUERY);
            statement.setInt(1, clanHall.getId());
            rset = statement.executeQuery();
            if (rset.next()) {
                clanHall.getSiegeDate().setTimeInMillis(rset.getLong("siege_date"));
                clanHall.getLastSiegeDate().setTimeInMillis(rset.getLong("last_siege_date"));
                clanHall.getOwnDate().setTimeInMillis(rset.getLong("own_date"));
                clanHall.setAuctionLength(rset.getInt("auction_length"));
                clanHall.setAuctionMinBid(rset.getLong("auction_min_bid"));
                clanHall.setAuctionDescription(rset.getString("auction_desc"));
                clanHall.setCycle(rset.getInt("cycle"));
                clanHall.setPaidCycle(rset.getInt("paid_cycle"));
            }
        } catch (Exception e) {
            LOGGER.error("ClanHallDAO.select(ClanHall):" + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
    }

    public void update(final ClanHall c) {
        if (!c.getJdbcState().isUpdatable()) {
            return;
        }
        c.setJdbcState(JdbcEntityState.STORED);
        update0(c);
    }

    private void update0(final ClanHall c) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(UPDATE_SQL_QUERY);
            statement.setLong(1, c.getSiegeDate().getTimeInMillis());
            statement.setLong(2, c.getLastSiegeDate().getTimeInMillis());
            statement.setLong(3, c.getOwnDate().getTimeInMillis());
            statement.setString(4, c.getAuctionDescription());
            statement.setInt(5, c.getAuctionLength());
            statement.setLong(6, c.getAuctionMinBid());
            statement.setInt(7, c.getCycle());
            statement.setInt(8, c.getPaidCycle());
            statement.setInt(9, c.getId());
            statement.execute();
        } catch (Exception e) {
            LOGGER.warn("ClanHallDAO#update0(ClanHall): " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    private static class LazyHolder {
        private static final ClanHallDAO INSTANCE = new ClanHallDAO();
    }
}
