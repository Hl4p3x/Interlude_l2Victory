package ru.j2dev.gameserver.taskmanager.actionrunner.tasks;

import org.apache.log4j.Logger;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.database.mysql;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.utils.Strings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class DeleteExpiredVarsTask extends AutomaticTask {
    private static final Logger LOGGER = Logger.getLogger(DeleteExpiredVarsTask.class);

    @Override
    public void doTask() {
        final long t = System.currentTimeMillis();
        Connection con = null;
        PreparedStatement query = null;
        final Map<Integer, String> varMap = new HashMap<>();
        ResultSet rs = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            query = con.prepareStatement("SELECT obj_id, name FROM character_variables WHERE expire_time > 0 AND expire_time < ?");
            query.setLong(1, System.currentTimeMillis());
            rs = query.executeQuery();
            while (rs.next()) {
                final String name = rs.getString("name");
                final String obj_id = Strings.stripSlashes(rs.getString("obj_id"));
                varMap.put(Integer.parseInt(obj_id), name);
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, query, rs);
        }
        if (!varMap.isEmpty()) {
            for (final Entry<Integer, String> entry : varMap.entrySet()) {
                final Player player = GameObjectsStorage.getPlayer(entry.getKey());
                if (player != null && player.isOnline()) {
                    player.unsetVar(entry.getValue());
                } else {
                    mysql.set("DELETE FROM `character_variables` WHERE `obj_id`=? AND `type`='user-var' AND `name`=? LIMIT 1", entry.getKey(), entry.getValue());
                }
            }
        }
    }

    @Override
    public long reCalcTime(final boolean start) {
        return System.currentTimeMillis() + 600000L;
    }
}
