package ru.j2dev.gameserver.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.model.entity.residence.Castle;
import ru.j2dev.gameserver.model.entity.residence.ClanHall;
import ru.j2dev.gameserver.model.entity.residence.Residence;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.tables.ClanTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ClanDataDAO {
    private static final String SELECT_CASTLE_OWNER = "SELECT clan_id FROM clan_data WHERE hasCastle = ? LIMIT 1";
    private static final String SELECT_CLANHALL_OWNER = "SELECT clan_id FROM clan_data WHERE hasHideout = ? LIMIT 1";
    private static final Logger LOGGER = LoggerFactory.getLogger(ClanDataDAO.class);


    public static ClanDataDAO getInstance() {
        return LazyHolder.INSTANCE;
    }


    public Clan getOwner(final Castle c) {
        return getOwner(c, SELECT_CASTLE_OWNER);
    }


    public Clan getOwner(final ClanHall c) {
        return getOwner(c, SELECT_CLANHALL_OWNER);
    }

    private Clan getOwner(final Residence residence, final String sql) {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(sql);
            statement.setInt(1, residence.getId());
            rset = statement.executeQuery();
            if (rset.next()) {
                return ClanTable.getInstance().getClan(rset.getInt("clan_id"));
            }
        } catch (Exception e) {
            LOGGER.error("ClanDataDAO.getOwner(Residence, String)", e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
        return null;
    }

    private static class LazyHolder {
        private static final ClanDataDAO INSTANCE = new ClanDataDAO();
    }
}
