package ru.j2dev.authserver.accounts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.authserver.database.DatabaseFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.commons.net.utils.NetList;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;

public class Account {
    private static final Logger LOGGER = LoggerFactory.getLogger(Account.class);
    private static final String SQLP_ACCOUNT_LOAD = "{CALL `lip_AccountLoad`(?)}";
    private static final String SQLP_ACCOUNT_CREATE = "{CALL `lip_AccountCreate`(?, ?)}";
    private static final String SQLP_ACCOUNT_UPDATE = "{CALL `lip_AccountUpdate`(?, ?, ?, ?, ?, ?, ?)}";

    private final String login;
    private final NetList allowedIpList;
    private String passwordHash;
    private int accessLevel;
    private int banExpire;
    private String lastIP;
    private int lastAccess;
    private int lastServer;
    private String email;

    public Account(final String login) {
        allowedIpList = new NetList();
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String val) {
        email = val;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(final String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isAllowedIP(final String ip) {
        return allowedIpList.isEmpty() || allowedIpList.isInRange(ip);
    }

    public int getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(final int accessLevel) {
        this.accessLevel = accessLevel;
    }

    public int getBanExpire() {
        return banExpire;
    }

    public void setBanExpire(final int banExpire) {
        this.banExpire = banExpire;
    }

    public String getLastIP() {
        return lastIP;
    }

    public void setLastIP(final String lastIP) {
        this.lastIP = lastIP;
    }

    public int getLastAccess() {
        return lastAccess;
    }

    public void setLastAccess(final int lastAccess) {
        this.lastAccess = lastAccess;
    }

    public int getLastServer() {
        return lastServer;
    }

    public void setLastServer(final int lastServer) {
        this.lastServer = lastServer;
    }

    @Override
    public String toString() {
        return login;
    }

    public void restore() {
        Connection con = null;
        CallableStatement cstmt = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            cstmt = con.prepareCall(SQLP_ACCOUNT_LOAD);
            cstmt.setString(1, login);
            rset = cstmt.executeQuery();
            if (rset.next()) {
                setPasswordHash(rset.getString("password").trim());
                setAccessLevel(rset.getInt("accessLevel"));
                setLastServer(rset.getInt("lastServerId"));
                setLastIP(rset.getString("lastIP"));
                setLastAccess(rset.getInt("lastactive"));
                setEmail(rset.getString("email"));
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, cstmt, rset);
        }
    }

    public void save() {
        Connection con = null;
        CallableStatement cstmt = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            cstmt = con.prepareCall(SQLP_ACCOUNT_CREATE);
            cstmt.setString(1, getLogin());
            cstmt.setString(2, getPasswordHash());
            cstmt.execute();
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, cstmt);
        }
    }

    public void update() {
        Connection con = null;
        CallableStatement cstmt = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            cstmt = con.prepareCall(SQLP_ACCOUNT_UPDATE);
            cstmt.setString(1, getLogin());
            cstmt.setString(2, getPasswordHash());
            cstmt.setInt(3, getAccessLevel());
            cstmt.setInt(4, getLastServer());
            cstmt.setString(5, getLastIP());
            cstmt.setInt(6, getLastAccess());
            cstmt.setString(7, getEmail());
            cstmt.execute();
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, cstmt);
        }
    }
}
