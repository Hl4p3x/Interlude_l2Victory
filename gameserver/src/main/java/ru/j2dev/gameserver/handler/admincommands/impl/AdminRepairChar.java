package ru.j2dev.gameserver.handler.admincommands.impl;

import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.model.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AdminRepairChar implements IAdminCommandHandler {
    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        if (activeChar.getPlayerAccess() == null || !activeChar.getPlayerAccess().CanEditChar) {
            return false;
        }
        if (wordList.length != 2) {
            return false;
        }
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("UPDATE characters SET x=-84318, y=244579, z=-3730 WHERE char_name=?");
            statement.setString(1, wordList[1]);
            statement.execute();
            DbUtils.close(statement);
            statement = con.prepareStatement("SELECT obj_id FROM characters where char_name=?");
            statement.setString(1, wordList[1]);
            rset = statement.executeQuery();
            int objId = 0;
            if (rset.next()) {
                objId = rset.getInt(1);
            }
            DbUtils.close(statement, rset);
            if (objId == 0) {
                return false;
            }
            statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE object_id=?");
            statement.setInt(1, objId);
            statement.execute();
            DbUtils.close(statement);
            statement = con.prepareStatement("DELETE FROM character_variables WHERE obj_id=? AND `type`='user-var' AND `name`='reflection' LIMIT 1");
            statement.setInt(1, objId);
            statement.execute();
            DbUtils.close(statement);
        } catch (Exception ignored) {
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
        return true;
    }

    @Override
    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    private enum Commands {
        admin_restore,
        admin_repair
    }
}
