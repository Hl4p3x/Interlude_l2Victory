package ru.j2dev.gameserver.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.model.actor.instances.player.Bonus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountBonusDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountBonusDAO.class);
    private static final String SQL_LOAD_BONUS = "SELECT \n    `expireTime` AS `expireTime`,\n    `rateXp` AS `rateXp`,\n    `rateSp` AS `rateSp`,\n    `questRewardRate` AS `questRewardRate`,\n    `questDropRate` AS `questDropRate`,\n    `dropAdena` AS `dropAdena`,\n    `dropItems` AS `dropItems`,\n    `dropRaidItems` AS `dropRaidItems`,\n    `dropSpoil` AS `dropSpoil`,\n    `enchantItemBonus` AS `enchantItemBonus` \nFROM \n    `accounts_bonuses`\nWHERE \n    `account` = ?";
    private static final String SQL_STORE_BONUS = "REPLACE LOW_PRIORITY INTO `accounts_bonuses` (\n    `account`,\n    `expireTime`,\n    `rateXp`,\n    `rateSp`,\n    `questRewardRate`,\n    `questDropRate`,\n    `dropAdena`,\n    `dropItems`,\n    `dropRaidItems`,\n    `dropSpoil`,    `enchantItemBonus`  \n) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_DELETE_BONUS = "DELETE FROM `accounts_bonuses` WHERE `account` = ?";


    public static AccountBonusDAO getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void load(final String accountName, final Bonus bonus) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rset = null;
        try {
            conn = DatabaseFactory.getInstance().getConnection();
            pstmt = conn.prepareStatement(SQL_LOAD_BONUS);
            pstmt.setString(1, accountName);
            rset = pstmt.executeQuery();
            if (rset.next()) {
                bonus.setBonusExpire(rset.getLong("expireTime"));
                bonus.setRateXp(rset.getFloat("rateXp"));
                bonus.setRateSp(rset.getFloat("rateSp"));
                bonus.setQuestRewardRate(rset.getFloat("questRewardRate"));
                bonus.setQuestDropRate(rset.getFloat("questDropRate"));
                bonus.setDropAdena(rset.getFloat("dropAdena"));
                bonus.setDropItems(rset.getFloat("dropItems"));
                bonus.setDropRaidItems(rset.getFloat("dropRaidItems"));
                bonus.setDropSpoil(rset.getFloat("dropSpoil"));
                bonus.setEnchantItem(rset.getFloat("enchantItemBonus"));
            }
        } catch (SQLException se) {
            LOGGER.error("Can't load account bonus for account \"" + accountName + "\"", se);
        } finally {
            DbUtils.closeQuietly(conn, pstmt, rset);
        }
    }


    public Bonus load(final String accountName) {
        final Bonus bonus = new Bonus();
        load(accountName, bonus);
        return bonus;
    }

    public void store(final String accountName, final Bonus bonus) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseFactory.getInstance().getConnection();
            pstmt = conn.prepareStatement(SQL_STORE_BONUS);
            pstmt.setString(1, accountName);
            pstmt.setLong(2, bonus.getBonusExpire());
            pstmt.setFloat(3, bonus.getRateXp());
            pstmt.setFloat(4, bonus.getRateSp());
            pstmt.setFloat(5, bonus.getQuestRewardRate());
            pstmt.setFloat(6, bonus.getQuestDropRate());
            pstmt.setFloat(7, bonus.getDropAdena());
            pstmt.setFloat(8, bonus.getDropItems());
            pstmt.setFloat(9, bonus.getDropRaidItems());
            pstmt.setFloat(10, bonus.getDropSpoil());
            pstmt.setFloat(11, bonus.getEnchantItemMul());
            pstmt.executeUpdate();
        } catch (SQLException se) {
            LOGGER.error("Can't store account bonus for account \"" + accountName + "\"", se);
        } finally {
            DbUtils.closeQuietly(conn, pstmt);
        }
    }

    public void delete(final String accountName) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseFactory.getInstance().getConnection();
            pstmt = conn.prepareStatement(SQL_DELETE_BONUS);
            pstmt.setString(1, accountName);
            pstmt.executeUpdate();
        } catch (SQLException se) {
            LOGGER.error("Can't store account bonus for account \"" + accountName + "\"", se);
        } finally {
            DbUtils.closeQuietly(conn, pstmt);
        }
    }

    private static class LazyHolder {
        private static final AccountBonusDAO INSTANCE = new AccountBonusDAO();
    }
}
