package ru.j2dev.gameserver.taskmanager.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.skills.AbnormalEffect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RestoreOfflineTraders extends RunnableImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestoreOfflineTraders.class);

    @Override
    public void runImpl() {
        int count = 0;
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            if (Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK > 0L) {
                final int expireTimeSecs = (int) (System.currentTimeMillis() / 1000L - Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK);
                statement = con.prepareStatement("DELETE FROM character_variables WHERE name = 'offline' AND value < ?");
                statement.setLong(1, expireTimeSecs);
                statement.executeUpdate();
                DbUtils.close(statement);
            }
            statement = con.prepareStatement("DELETE FROM character_variables WHERE name = 'offline' AND obj_id IN (SELECT obj_id FROM characters WHERE accessLevel < 0)");
            statement.executeUpdate();
            DbUtils.close(statement);
            statement = con.prepareStatement("SELECT obj_id, value FROM character_variables WHERE name = 'offline'");
            rset = statement.executeQuery();
            while (rset.next()) {
                final int objectId = rset.getInt("obj_id");
                final int expireTimeSecs2 = rset.getInt("value");
                final Player p = Player.restore(objectId);
                if (p == null) {
                    continue;
                }
                if (p.isDead()) {
                    p.kick();
                } else {
                    if (Config.SERVICES_OFFLINE_TRADE_NAME_COLOR_CHANGE) {
                        p.setNameColor(Config.SERVICES_OFFLINE_TRADE_NAME_COLOR);
                    }
                    if (Config.SERVICES_OFFLINE_TRADE_ABNORMAL != AbnormalEffect.NULL) {
                        p.startAbnormalEffect(Config.SERVICES_OFFLINE_TRADE_ABNORMAL);
                    }
                    p.setOfflineMode(true);
                    p.setIsOnline(true);
                    p.spawnMe();
                    if (p.getClan() != null && p.getClan().getAnyMember(p.getObjectId()) != null) {
                        p.getClan().getAnyMember(p.getObjectId()).setPlayerInstance(p, false);
                    }
                    if (Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK > 0L) {
                        p.startKickTask((Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK + expireTimeSecs2 - System.currentTimeMillis() / 1000L) * 1000L);
                    }
                    if (Config.SERVICES_TRADE_ONLY_FAR) {
                        for (final Player player : World.getAroundPlayers(p, Config.SERVICES_TRADE_RADIUS, 200)) {
                            if (player.isInStoreMode()) {
                                if (player.isInOfflineMode()) {
                                    player.setOfflineMode(false);
                                    player.kick();
                                    LOGGER.warn("Offline trader: " + player + " kicked.");
                                } else {
                                    player.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
                                    player.standUp();
                                    player.broadcastCharInfo();
                                }
                            }
                        }
                    }
                    ++count;
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error while restoring offline traders!", e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
        LOGGER.info("Restored " + count + " offline traders");
    }
}
