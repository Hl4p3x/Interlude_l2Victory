package services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.handler.voicecommands.IVoicedCommandHandler;
import ru.j2dev.gameserver.handler.voicecommands.VoicedCommandHandler;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.GameClient;
import ru.j2dev.gameserver.network.lineage2.GameClient.GameClientState;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.utils.ItemFunctions;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class L2TopZoneService implements OnInitScriptListener, IVoicedCommandHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(L2TopZoneService.class);
    private static final String[] COMMAND_LIST = {"l2topzone", "topzone"};
    private static final String URL_TEMPLATE = "http://l2topzone.com/api.php?API_KEY=%api_key%&SERVER_ID=%server_id%&IP=%player_key%";
    private static final L2TopZoneService INSTANCE = new L2TopZoneService();

    public static L2TopZoneService getInstance() {
        return INSTANCE;
    }

    private static String getPlayerKey(final Player player) {
        if (player == null) {
            return null;
        }
        final GameClient client = player.getNetConnection();
        if (client == null || !client.isConnected() || client.getState() != GameClientState.IN_GAME) {
            return null;
        }
        return client.getIpAddr();
    }

    private static long getLastStoredCheckDate(final String playerKey) {
        long result = 0L;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rset = null;
        try {
            conn = DatabaseFactory.getInstance().getConnection();
            pstmt = conn.prepareStatement("SELECT `last_check` AS `lastCheck` FROM `l2topzone_votes` WHERE `player_key` = ?");
            pstmt.setString(1, playerKey);
            rset = pstmt.executeQuery();
            if (rset.next()) {
                result = rset.getLong(1);
            }
        } catch (SQLException se) {
            LOGGER.error("L2TopZoneService: Cant get last stored vote check date", se);
        } finally {
            DbUtils.closeQuietly(conn, pstmt, rset);
        }
        return result;
    }

    private static void setLastStoredCheckDate(final String playerKey, final long lastCheck) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseFactory.getInstance().getConnection();
            pstmt = conn.prepareStatement("REPLACE INTO `l2topzone_votes`(`player_key`, `last_check`) VALUES (?, ?)");
            pstmt.setString(1, playerKey);
            pstmt.setLong(2, lastCheck);
            pstmt.executeUpdate();
        } catch (SQLException se) {
            LOGGER.error("L2TopZoneService: Cant set last stored vote check date", se);
        } finally {
            DbUtils.closeQuietly(conn, pstmt);
        }
    }

    private static boolean requestCheckVote(final String apiKey, final int serverId, final String playerKey) {
        try {
            final URL url = new URL("http://l2topzone.com/api.php?API_KEY=%api_key%&SERVER_ID=%server_id%&IP=%player_key%".replace("%api_key%", apiKey).replace("%server_id%", String.valueOf(serverId)).replace("%player_key%", playerKey));
            final URLConnection conn = url.openConnection();
            conn.addRequestProperty("Host", url.getHost());
            conn.addRequestProperty("Accept", "*/*");
            conn.addRequestProperty("Connection", "close");
            conn.addRequestProperty("User-Agent", "L2TopZone");
            conn.setConnectTimeout(5000);
            InputStream is = null;
            InputStreamReader isr = null;
            try {
                is = conn.getInputStream();
                final StringBuilder sb = new StringBuilder();
                final char[] buff = new char[64];
                isr = new InputStreamReader(is);
                int len;
                while ((len = isr.read(buff)) > 0 && sb.length() < 1024) {
                    sb.append(buff, 0, len);
                }
                return Boolean.parseBoolean(sb.toString());
            } finally {
                if (isr != null) {
                    try {
                        isr.close();
                    } catch (IOException ignored) {
                        ignored.printStackTrace();
                    }
                }
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ignored) {
                        ignored.printStackTrace();
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.warn("L2TopZoneService: Check request failed", ex);
            return false;
        }
    }

    private void processPlayerRewardRequest(final Player player) {
        final String playerKey = getPlayerKey(player);
        if (player == null) {
            return;
        }
        final long now = System.currentTimeMillis() / 1000L;
        final long lastStoredCheckDate = getLastStoredCheckDate(playerKey);
        final long checkRemainingTime = lastStoredCheckDate + Config.L2TOPZONE_VOTE_TIME_TO_LIVE - now;
        if (checkRemainingTime > 0L) {
            final int hours = (int) (checkRemainingTime / 3600L);
            final int minuter = (int) (checkRemainingTime % 3600L);
            if (hours > 0) {
                player.sendPacket(new SystemMessage(1813).addString("L2TopZone").addNumber(hours));
            } else {
                player.sendPacket(new SystemMessage(1814).addString("L2TopZone").addNumber(minuter));
            }
            return;
        }
        setLastStoredCheckDate(playerKey, now);
        try {
            if (requestCheckVote(Config.L2TOPZONE_API_KEY, Config.L2TOPZONE_SERVER_ID, playerKey)) {
                ItemFunctions.addItem(player, Config.L2TOPZONE_REWARD_ITEM_ID, (long) Config.L2TOPZONE_REWARD_ITEM_COUNT, true);
                player.sendMessage("L2TopZone: Thank you for your vote.");
            } else {
                player.sendMessage("L2TopZone: Vote first!");
            }
        } catch (Exception ex) {
            LOGGER.warn("L2TopZoneService: Cant process reward.");
            player.sendPacket(SystemMsg.SYSTEM_ERROR);
        }
    }

    @Override
    public boolean useVoicedCommand(final String command, final Player activeChar, final String target) {
        for (final String c : COMMAND_LIST) {
            if (c.equalsIgnoreCase(command)) {
                processPlayerRewardRequest(activeChar);
                return true;
            }
        }
        return false;
    }

    @Override
    public String[] getVoicedCommandList() {
        return COMMAND_LIST;
    }

    @Override
    public void onInit() {
        if (Config.L2TOPZONE_ENABLED) {
            VoicedCommandHandler.getInstance().registerVoicedCommandHandler(getInstance());
        }
    }
}
