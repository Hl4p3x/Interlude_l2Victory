package services.community;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.HtmCache;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.handler.bbs.CommunityBoardManager;
import ru.j2dev.gameserver.handler.bbs.ICommunityBoardHandler;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ShowBoard;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.StringTokenizer;

public class ManageFavorites implements OnInitScriptListener, ICommunityBoardHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ManageFavorites.class);

    @Override
    public void onInit() {
        if (Config.COMMUNITYBOARD_ENABLED) {
            ManageFavorites.LOGGER.info("CommunityBoard: Manage Favorites service loaded.");
            CommunityBoardManager.getInstance().registerHandler(this);
        }
    }

    @Override
    public String[] getBypassCommands() {
        return new String[]{"_bbsgetfav", "_bbsaddfav_List", "_bbsdelfav_"};
    }

    @Override
    public void onBypassCommand(final Player player, final String bypass) {
        final StringTokenizer st = new StringTokenizer(bypass, "_");
        final String cmd = st.nextToken();
        switch (cmd) {
            case "bbsgetfav":
                Connection con = null;
                PreparedStatement statement = null;
                ResultSet rset = null;
                final StringBuilder fl = new StringBuilder("");
                try {
                    con = DatabaseFactory.getInstance().getConnection();
                    statement = con.prepareStatement("SELECT * FROM `bbs_favorites` WHERE `object_id` = ? ORDER BY `add_date` DESC");
                    statement.setInt(1, player.getObjectId());
                    rset = statement.executeQuery();
                    final String tpl = HtmCache.getInstance().getNotNull("scripts/services/community/bbs_favoritetpl.htm", player);
                    while (rset.next()) {
                        String fav = tpl.replace("%fav_title%", rset.getString("fav_title"));
                        fav = fav.replace("%fav_bypass%", rset.getString("fav_bypass"));
                        fav = fav.replace("%add_date%", String.format("%1$te.%1$tm.%1$tY %1$tH:%1tM", new Date(rset.getInt("add_date") * 1000L)));
                        fav = fav.replace("%fav_id%", String.valueOf(rset.getInt("fav_id")));
                        fl.append(fav);
                    }
                } catch (Exception ex) {
                } finally {
                    DbUtils.closeQuietly(con, statement, rset);
                }
                String html = HtmCache.getInstance().getNotNull("scripts/services/community/bbs_getfavorite.htm", player);
                html = html.replace("%FAV_LIST%", fl.toString());
                ShowBoard.separateAndSend(html, player);
                break;
            case "bbsaddfav":
                final String fav2 = player.getSessionVar("add_fav");
                player.setSessionVar("add_fav", null);
                if (fav2 != null) {
                    final String[] favs = fav2.split("&");
                    if (favs.length > 1) {
                        Connection con2 = null;
                        PreparedStatement statement2 = null;
                        try {
                            con2 = DatabaseFactory.getInstance().getConnection();
                            statement2 = con2.prepareStatement("REPLACE INTO `bbs_favorites`(`object_id`, `fav_bypass`, `fav_title`, `add_date`) VALUES(?, ?, ?, ?)");
                            statement2.setInt(1, player.getObjectId());
                            statement2.setString(2, favs[0]);
                            statement2.setString(3, favs[1]);
                            statement2.setInt(4, (int) (System.currentTimeMillis() / 1000L));
                            statement2.execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            DbUtils.closeQuietly(con2, statement2);
                        }
                    }
                }
                onBypassCommand(player, "_bbsgetfav");
                break;
            case "bbsdelfav":
                final int fav_id = Integer.parseInt(st.nextToken());
                Connection con3 = null;
                PreparedStatement statement3 = null;
                try {
                    con3 = DatabaseFactory.getInstance().getConnection();
                    statement3 = con3.prepareStatement("DELETE FROM `bbs_favorites` WHERE `fav_id` = ? and `object_id` = ?");
                    statement3.setInt(1, fav_id);
                    statement3.setInt(2, player.getObjectId());
                    statement3.execute();
                } catch (Exception ex2) {
                } finally {
                    DbUtils.closeQuietly(con3, statement3);
                }
                onBypassCommand(player, "_bbsgetfav");
                break;
        }
    }

    @Override
    public void onWriteCommand(final Player player, final String bypass, final String arg1, final String arg2, final String arg3, final String arg4, final String arg5) {
    }
}
