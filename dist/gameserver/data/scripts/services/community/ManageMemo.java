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
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

public class ManageMemo implements OnInitScriptListener, ICommunityBoardHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ManageMemo.class);
    private static final int MEMO_PER_PAGE = 12;

    private static String getMemoList(final Player player, final int page, final int count) {
        final StringBuilder memoList = new StringBuilder("");
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            if (count > 0) {
                final int start = (page - 1) * 12;
                final int end = page * 12;
                con = DatabaseFactory.getInstance().getConnection();
                statement = con.prepareStatement("SELECT memo_id,title,post_date FROM `bbs_memo` WHERE `account_name` = ? ORDER BY post_date DESC LIMIT " + start + "," + end);
                statement.setString(1, player.getAccountName());
                rset = statement.executeQuery();
                final String tpl = HtmCache.getInstance().getNotNull("scripts/services/community/bbs_memo_post.htm", player);
                while (rset.next()) {
                    String post = tpl;
                    post = post.replace("%memo_id%", String.valueOf(rset.getInt("memo_id")));
                    post = post.replace("%memo_title%", rset.getString("title"));
                    post = post.replace("%page%", String.valueOf(page));
                    post = post.replace("%memo_date%", String.format("%1$te-%1$tm-%1$tY", new Date(rset.getInt("post_date") * 1000L)));
                    memoList.append(post);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
        return memoList.toString();
    }

    private static int getMemoCount(final Player player) {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        int count = 0;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT count(*) as cnt FROM bbs_memo WHERE `account_name` = ?");
            statement.setString(1, player.getAccountName());
            rset = statement.executeQuery();
            if (rset.next()) {
                count = rset.getInt("cnt");
            }
        } catch (Exception ignored) {
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
        return count;
    }

    @Override
    public void onInit() {
        if (Config.COMMUNITYBOARD_ENABLED) {
            ManageMemo.LOGGER.info("CommunityBoard: Manage Memo service loaded.");
            CommunityBoardManager.getInstance().registerHandler(this);
        }
    }

    @Override
    public String[] getBypassCommands() {
        return new String[]{"_bbsmemo", "_mmread_", "_mmlist_", "_mmcrea", "_mmwrite", "_mmmodi_", "_mmdele"};
    }

    @Override
    public void onBypassCommand(final Player player, final String bypass) {
        final StringTokenizer st = new StringTokenizer(bypass, "_");
        final String cmd = st.nextToken();
        player.setSessionVar("add_fav", null);
        String html = HtmCache.getInstance().getNotNull("scripts/services/community/bbs_memo_list.htm", player);
        if ("bbsmemo".equals(cmd) || "_mmlist_1".equals(bypass)) {
            final int count = getMemoCount(player);
            html = html.replace("%memo_list%", getMemoList(player, 1, count));
            html = html.replace("%prev_page%", "");
            html = html.replace("%page%", "1");
            StringBuilder pages = new StringBuilder("<td>1</td>\n\n");
            if (count > 12) {
                int pgs = count / 12;
                if (count % 12 != 0) {
                    ++pgs;
                }
                html = html.replace("%next_page%", "bypass _mmlist_2");
                for (int i = 2; i <= pgs; ++i) {
                    pages.append("<td><a action=\"bypass _mmlist_").append(i).append("\"> ").append(i).append(" </a></td>\n\n");
                }
            } else {
                html = html.replace("%next_page%", "");
            }
            html = html.replace("%pages%", pages.toString());
        } else if ("mmlist".equals(cmd)) {
            final int currPage = Integer.parseInt(st.nextToken());
            final int count2 = getMemoCount(player);
            html = html.replace("%memo_list%", getMemoList(player, currPage, count2));
            html = html.replace("%prev_page%", "bypass _mmlist_" + (currPage - 1));
            html = html.replace("%page%", String.valueOf(currPage));
            StringBuilder pages2 = new StringBuilder();
            int pgs2 = count2 / 12;
            if (count2 % 12 != 0) {
                ++pgs2;
            }
            if (count2 > currPage * 12) {
                html = html.replace("%next_page%", "bypass _mmlist_" + (currPage + 1));
            } else {
                html = html.replace("%next_page%", "");
            }
            for (int j = 1; j <= pgs2; ++j) {
                if (j == currPage) {
                    pages2.append("<td>").append(j).append("</td>\n\n");
                } else {
                    pages2.append("<td height=15><a action=\"bypass _mmlist_").append(j).append("\"> ").append(j).append(" </a></td>\n\n");
                }
            }
            html = html.replace("%pages%", pages2.toString());
        } else if ("mmcrea".equals(cmd)) {
            if (getMemoCount(player) >= 100) {
                player.sendPacket(new SystemMessage(1206));
                onBypassCommand(player, "_mmlist_1");
                return;
            }
            final String page = st.nextToken();
            html = HtmCache.getInstance().getNotNull("scripts/services/community/bbs_memo_edit.htm", player);
            html = html.replace("%page%", page);
            html = html.replace("%memo_id%", "0");
            html = html.replace("%TREE%", "&nbsp;>&nbsp;\u0421\u043e\u0437\u0434\u0430\u043d\u0438\u0435 \u0437\u0430\u043f\u0438\u0441\u043a\u0438");
            player.sendPacket(new ShowBoard(html, "1001", player));
            final List<String> args = new ArrayList<>();
            args.add("0");
            args.add("0");
            args.add("0");
            args.add("0");
            args.add("0");
            args.add("0");
            args.add("");
            args.add("0");
            args.add("");
            args.add("0");
            args.add("");
            args.add("");
            args.add("");
            args.add("1970-01-01 00:00:00 ");
            args.add("1970-01-01 00:00:00 ");
            args.add("0");
            args.add("0");
            args.add("");
            player.sendPacket(new ShowBoard(args));
            return;
        } else {
            if ("mmread".equals(cmd)) {
                final int memoId = Integer.parseInt(st.nextToken());
                final String page2 = st.nextToken();
                Connection con = null;
                PreparedStatement statement = null;
                ResultSet rset = null;
                try {
                    con = DatabaseFactory.getInstance().getConnection();
                    statement = con.prepareStatement("SELECT * FROM `bbs_memo` WHERE `account_name` = ? and memo_id = ?");
                    statement.setString(1, player.getAccountName());
                    statement.setInt(2, memoId);
                    rset = statement.executeQuery();
                    if (rset.next()) {
                        String post = HtmCache.getInstance().getNotNull("scripts/services/community/bbs_memo_read.htm", player);
                        post = post.replace("%title%", rset.getString("title"));
                        post = post.replace("%char_name%", rset.getString("char_name"));
                        post = post.replace("%post_date%", String.format("%1$tY-%1$tm-%1$te %1$tH:%1tM:%1$tS", new Date(rset.getInt("post_date") * 1000L)));
                        post = post.replace("%memo%", rset.getString("memo").replace("\n", "<br1>"));
                        post = post.replace("%page%", page2);
                        post = post.replace("%memo_id%", String.valueOf(memoId));
                        ShowBoard.separateAndSend(post, player);
                        return;
                    }
                } catch (Exception ignored) {
                } finally {
                    DbUtils.closeQuietly(con, statement, rset);
                }
                onBypassCommand(player, "_bbsmemo");
                return;
            }
            if ("mmdele".equals(cmd)) {
                final int memoId = Integer.parseInt(st.nextToken());
                Connection con2 = null;
                PreparedStatement statement2 = null;
                try {
                    con2 = DatabaseFactory.getInstance().getConnection();
                    statement2 = con2.prepareStatement("DELETE FROM `bbs_memo` WHERE `account_name` = ? and memo_id = ?");
                    statement2.setString(1, player.getAccountName());
                    statement2.setInt(2, memoId);
                    statement2.execute();
                } catch (Exception ignored) {
                } finally {
                    DbUtils.closeQuietly(con2, statement2);
                }
                onBypassCommand(player, "_mmlist_1");
                return;
            }
            if ("mmmodi".equals(cmd)) {
                final int memoId = Integer.parseInt(st.nextToken());
                final String page2 = st.nextToken();
                Connection con = null;
                PreparedStatement statement = null;
                ResultSet rset = null;
                try {
                    con = DatabaseFactory.getInstance().getConnection();
                    statement = con.prepareStatement("SELECT * FROM `bbs_memo` WHERE `account_name` = ? and memo_id = ?");
                    statement.setString(1, player.getAccountName());
                    statement.setInt(2, memoId);
                    rset = statement.executeQuery();
                    if (rset.next()) {
                        html = HtmCache.getInstance().getNotNull("scripts/services/community/bbs_memo_edit.htm", player);
                        html = html.replace("%page%", page2);
                        html = html.replace("%memo_id%", String.valueOf(memoId));
                        html = html.replace("%TREE%", "&nbsp;>&nbsp;<a action=\"bypass _mmread_" + memoId + "_" + page2 + "\">\u0417\u0430\u043f\u0438\u0441\u043a\u0430: " + rset.getString("title") + "</a>&nbsp;>&nbsp;\u0420\u0435\u0434\u0430\u043a\u0442\u0438\u0440\u043e\u0432\u0430\u043d\u0438\u0435");
                        player.sendPacket(new ShowBoard(html, "1001", player));
                        final List<String> args2 = new ArrayList<>();
                        args2.add("0");
                        args2.add("0");
                        args2.add(String.valueOf(memoId));
                        args2.add("0");
                        args2.add("0");
                        args2.add("0");
                        args2.add(player.getName());
                        args2.add("0");
                        args2.add(player.getAccountName());
                        args2.add("0");
                        args2.add(rset.getString("title"));
                        args2.add(rset.getString("title"));
                        args2.add(rset.getString("memo"));
                        args2.add(String.format("%1$tY-%1$tm-%1$te %1$tH:%1tM:%1$tS", new Date(rset.getInt("post_date") * 1000L)));
                        args2.add(String.format("%1$tY-%1$tm-%1$te %1$tH:%1tM:%1$tS", new Date(rset.getInt("post_date") * 1000L)));
                        args2.add("0");
                        args2.add("0");
                        args2.add("");
                        player.sendPacket(new ShowBoard(args2));
                        return;
                    }
                } catch (Exception ignored) {
                } finally {
                    DbUtils.closeQuietly(con, statement, rset);
                }
                onBypassCommand(player, "_mmlist_" + page2);
                return;
            }
        }
        ShowBoard.separateAndSend(html, player);
    }

    @Override
    public void onWriteCommand(final Player player, final String bypass, final String arg1, final String arg2, final String arg3, final String arg4, final String arg5) {
        final StringTokenizer st = new StringTokenizer(bypass, "_");
        final String cmd = st.nextToken();
        if ("mmwrite".equals(cmd)) {
            if (getMemoCount(player) >= 100) {
                player.sendPacket(new SystemMessage(1206));
                onBypassCommand(player, "_mmlist_1");
                return;
            }
            if (arg3 != null && !arg3.isEmpty() && arg4 != null && !arg4.isEmpty()) {
                String title = arg3.replace("<", "");
                title = title.replace(">", "");
                title = title.replace("&", "");
                title = title.replace("$", "");
                if (title.length() > 128) {
                    title = title.substring(0, 128);
                }
                String memo = arg4.replace("<", "");
                memo = memo.replace(">", "");
                memo = memo.replace("&", "");
                memo = memo.replace("$", "");
                if (memo.length() > 1000) {
                    memo = memo.substring(0, 1000);
                }
                int memoId = 0;
                if (arg2 != null && !arg2.isEmpty()) {
                    memoId = Integer.parseInt(arg2);
                }
                if (title.length() > 0 && memo.length() > 0) {
                    Connection con = null;
                    PreparedStatement stmt = null;
                    try {
                        con = DatabaseFactory.getInstance().getConnection();
                        if (memoId > 0) {
                            stmt = con.prepareStatement("UPDATE bbs_memo SET title = ?, memo = ? WHERE memo_id = ? AND account_name = ?");
                            stmt.setString(1, title);
                            stmt.setString(2, memo);
                            stmt.setInt(3, memoId);
                            stmt.setString(4, player.getAccountName());
                            stmt.execute();
                        } else {
                            stmt = con.prepareStatement("INSERT INTO bbs_memo(account_name, char_name, ip, title, memo, post_date) VALUES(?, ?, ?, ?, ?, ?)");
                            stmt.setString(1, player.getAccountName());
                            stmt.setString(2, player.getName());
                            stmt.setString(3, player.getNetConnection().getIpAddr());
                            stmt.setString(4, title);
                            stmt.setString(5, memo);
                            stmt.setInt(6, (int) (System.currentTimeMillis() / 1000L));
                            stmt.execute();
                        }
                    } catch (Exception ignored) {
                    } finally {
                        DbUtils.closeQuietly(con, stmt);
                    }
                }
            }
        }
        onBypassCommand(player, "_bbsmemo");
    }
}
