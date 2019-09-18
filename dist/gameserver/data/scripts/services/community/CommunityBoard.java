package services.community;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.HtmCache;
import ru.j2dev.gameserver.data.xml.holder.MultiSellHolder;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.handler.bbs.CommunityBoardManager;
import ru.j2dev.gameserver.handler.bbs.ICommunityBoardHandler;
import ru.j2dev.gameserver.handler.bypass.BypassHolder;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ShowBoard;
import ru.j2dev.gameserver.scripts.Scripts;
import ru.j2dev.gameserver.tables.ClanTable;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.StringTokenizer;

public class CommunityBoard implements OnInitScriptListener, ICommunityBoardHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommunityBoard.class);

    @Override
    public void onInit() {
        if (Config.COMMUNITYBOARD_ENABLED) {
            CommunityBoard.LOGGER.info("CommunityBoard: service loaded.");
            CommunityBoardManager.getInstance().registerHandler(this);
        }
    }

    @Override
    public String[] getBypassCommands() {
        return new String[]{"_bbshome", "_bbslink", "_bbsmultisell", "_bbspage", "_bbsclose", "_bbsscripts"};
    }

    @Override
    public void onBypassCommand(final Player player, final String bypass) {
        final StringTokenizer st = new StringTokenizer(bypass, "_");
        final String cmd = st.nextToken();
        String html = "";
        if ("bbshome".equals(cmd)) {
            final StringTokenizer p = new StringTokenizer(Config.BBS_DEFAULT, "_");
            final String dafault = p.nextToken();
            if (!dafault.equals(cmd)) {
                onBypassCommand(player, Config.BBS_DEFAULT);
                return;
            }
            html = HtmCache.getInstance().getNotNull("scripts/services/community/bbs_top.htm", player);
            int favCount = 0;
            Connection con = null;
            PreparedStatement statement = null;
            ResultSet rset = null;
            try {
                con = DatabaseFactory.getInstance().getConnection();
                statement = con.prepareStatement("SELECT count(*) as cnt FROM `bbs_favorites` WHERE `object_id` = ?");
                statement.setInt(1, player.getObjectId());
                rset = statement.executeQuery();
                if (rset.next()) {
                    favCount = rset.getInt("cnt");
                }
            } catch (Exception ignored) {
            } finally {
                DbUtils.closeQuietly(con, statement, rset);
            }
            html = html.replace("<?fav_count?>", String.valueOf(favCount));
            html = html.replace("<?clan_count?>", String.valueOf(ClanTable.getInstance().getClans().length));
            html = html.replace("<?market_count?>", String.valueOf(CommunityBoardManager.getInstance().getIntProperty("col_count")));
        } else if ("bbslink".equals(cmd)) {
            html = HtmCache.getInstance().getNotNull("scripts/services/community/bbs_homepage.htm", player);
        } else if ("_bbsclose".equals(cmd)) {
            player.sendPacket(ShowBoard.CLOSE);
        } else if (bypass.startsWith("_bbspage")) {
            final String[] b = bypass.split(":");
            final String page = b[1];
            html = HtmCache.getInstance().getNotNull("scripts/services/community/pages/" + page + ".htm", player);
        } else if (bypass.startsWith("_bbshtmbypass")) {
            StringTokenizer st2 = new StringTokenizer(bypass, ";");
            String command = st2.nextToken().substring(14);
            String pBypass = st2.hasMoreTokens() ? st2.nextToken() : null;
            if (pBypass != null) {
                onBypassCommand(player, pBypass);
            }

            String word = command.split("\\s+")[0];

            Pair<Object, Method> b = BypassHolder.getInstance().getBypass(word);
            if (b != null) {
                try {
                    b.getValue().invoke(b.getKey(), player, null, command.substring(word.length()).trim().split("\\s+"));
                } catch (Exception e) {
                    LOGGER.info("Exception: " + e, e);
                }
            }
            return;
        } else {
            if (bypass.startsWith("_bbsmultisell")) {
                final StringTokenizer st2 = new StringTokenizer(bypass, ";");
                final String[] mBypass = st2.nextToken().split(":");
                final String pBypass = st2.hasMoreTokens() ? st2.nextToken() : null;
                if (pBypass != null) {
                    final ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(pBypass);
                    if (handler != null) {
                        handler.onBypassCommand(player, pBypass);
                    }
                }
                final int listId = Integer.parseInt(mBypass[1]);
                MultiSellHolder.getInstance().SeparateAndSend(listId, player, 0.0);
                return;
            }
            if (bypass.startsWith("_bbsscripts")) {
                final StringTokenizer st2 = new StringTokenizer(bypass, ";");
                final String sBypass = st2.nextToken().substring(12);
                final String pBypass = st2.hasMoreTokens() ? st2.nextToken() : null;
                if (pBypass != null) {
                    final ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(pBypass);
                    if (handler != null) {
                        handler.onBypassCommand(player, pBypass);
                    }
                }
                final String[] word = sBypass.split("\\s+");
                final String[] args = sBypass.substring(word[0].length()).trim().split("\\s+");
                final String[] path = word[0].split(":");
                if (path.length != 2) {
                    return;
                }
                final String s = path[0];
                final String s2 = path[1];
                Object[] array2 = new Object[0];
                if (word.length == 1) {
                } else {
                    array2 = new Object[]{args};
                }
                Scripts.getInstance().callScripts(player, s, s2, array2);
                return;
            }
        }
        ShowBoard.separateAndSend(html, player);
    }

    @Override
    public void onWriteCommand(final Player player, final String bypass, final String arg1, final String arg2, final String arg3, final String arg4, final String arg5) {
    }
}
