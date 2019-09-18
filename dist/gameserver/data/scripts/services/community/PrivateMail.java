package services.community;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.HtmCache;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.handler.bbs.CommunityBoardManager;
import ru.j2dev.gameserver.handler.bbs.ICommunityBoardHandler;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExMailArrived;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ShowBoard;
import ru.j2dev.gameserver.scripts.Functions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

public class PrivateMail extends Functions implements OnInitScriptListener, ICommunityBoardHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrivateMail.class);
    private static final int MESSAGE_PER_PAGE = 10;

    public static void OnPlayerEnter(final Player player) {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT * FROM `bbs_mail` WHERE `box_type` = 0 and `read` = 0 and `to_object_id` = ?");
            statement.setInt(1, player.getObjectId());
            rset = statement.executeQuery();
            if (rset.next()) {
                player.sendPacket(Msg.YOUVE_GOT_MAIL);
                player.sendPacket(ExMailArrived.STATIC);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
    }

    private static List<MailData> getMailList(final Player player, final int type, final String search, final boolean byTitle) {
        final List<MailData> list = new ArrayList<>();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("DELETE FROM `bbs_mail` WHERE `to_object_id` = ? and `post_date` < ?");
            statement.setInt(1, player.getObjectId());
            statement.setInt(2, (int) (System.currentTimeMillis() / 1000L) - 7776000);
            statement.execute();
            statement.close();
            final String column_name = (type == 0) ? "from_name" : "to_name";
            statement = con.prepareStatement("SELECT * FROM `bbs_mail` WHERE `box_type` = ? and `to_object_id` = ? ORDER BY post_date DESC");
            statement.setInt(1, type);
            statement.setInt(2, player.getObjectId());
            rset = statement.executeQuery();
            while (rset.next()) {
                if (search.isEmpty()) {
                    list.add(new MailData(rset.getString(column_name), rset.getString("title"), rset.getInt("post_date"), rset.getInt("message_id")));
                } else if (byTitle && !search.isEmpty() && rset.getString("title").toLowerCase().contains(search.toLowerCase())) {
                    list.add(new MailData(rset.getString(column_name), rset.getString("title"), rset.getInt("post_date"), rset.getInt("message_id")));
                } else {
                    if (byTitle || search.isEmpty() || !rset.getString(column_name).toLowerCase().contains(search.toLowerCase())) {
                        continue;
                    }
                    list.add(new MailData(rset.getString(column_name), rset.getString("title"), rset.getInt("post_date"), rset.getInt("message_id")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
        return list;
    }

    @Override
    public void onInit() {
        if (Config.COMMUNITYBOARD_ENABLED) {
            PrivateMail.LOGGER.info("CommunityBoard: Private Mail service loaded.");
            CommunityBoardManager.getInstance().registerHandler(this);
        }
    }

    @Override
    public String[] getBypassCommands() {
        return new String[]{"_maillist_", "_mailsearch_", "_mailread_", "_maildelete_"};
    }

    @Override
    public void onBypassCommand(final Player player, final String bypass) {
        final StringTokenizer st = new StringTokenizer(bypass, "_");
        final String cmd = st.nextToken();
        player.setSessionVar("add_fav", null);
        switch (cmd) {
            case "maillist": {
                final int type = Integer.parseInt(st.nextToken());
                final int page = Integer.parseInt(st.nextToken());
                final int byTitle = Integer.parseInt(st.nextToken());
                final String search = st.hasMoreTokens() ? st.nextToken() : "";
                String html = HtmCache.getInstance().getNotNull("scripts/services/community/bbs_mail_list.htm", player);
                int inbox = 0;
                int send = 0;
                Connection con = null;
                PreparedStatement statement = null;
                ResultSet rset = null;
                try {
                    con = DatabaseFactory.getInstance().getConnection();
                    statement = con.prepareStatement("SELECT count(*) as cnt FROM `bbs_mail` WHERE `box_type` = 0 and `to_object_id` = ?");
                    statement.setInt(1, player.getObjectId());
                    rset = statement.executeQuery();
                    if (rset.next()) {
                        inbox = rset.getInt("cnt");
                    }
                    statement.close();
                    statement = con.prepareStatement("SELECT count(*) as cnt FROM `bbs_mail` WHERE `box_type` = 1 and `from_object_id` = ?");
                    statement.setInt(1, player.getObjectId());
                    rset = statement.executeQuery();
                    if (rset.next()) {
                        send = rset.getInt("cnt");
                    }
                } catch (Exception ex) {
                } finally {
                    DbUtils.closeQuietly(con, statement, rset);
                }
                List<MailData> mailList = null;
                switch (type) {
                    case 0: {
                        html = html.replace("%inbox_link%", "[&$917;]");
                        html = html.replace("%sentbox_link%", "<a action=\"bypass _maillist_1_1_0_\">[&$918;]</a>");
                        html = html.replace("%archive_link%", "<a action=\"bypass _maillist_2_1_0_\">[&$919;]</a>");
                        html = html.replace("%temp_archive_link%", "<a action=\"bypass _maillist_3_1_0_\">[&$920;]</a>");
                        html = html.replace("%TREE%", "&$917;");
                        html = html.replace("%writer_header%", "&$911;");
                        mailList = getMailList(player, type, search, byTitle == 1);
                        break;
                    }
                    case 1: {
                        html = html.replace("%inbox_link%", "<a action=\"bypass _maillist_0_1_0_\">[&$917;]</a>");
                        html = html.replace("%sentbox_link%", "[&$918;]");
                        html = html.replace("%archive_link%", "<a action=\"bypass _maillist_2_1_0_\">[&$919;]</a>");
                        html = html.replace("%temp_archive_link%", "<a action=\"bypass _maillist_3_1_0_\">[&$920;]</a>");
                        html = html.replace("%TREE%", "&$918;");
                        html = html.replace("%writer_header%", "&$909;");
                        mailList = getMailList(player, type, search, byTitle == 1);
                        break;
                    }
                    case 2: {
                        html = html.replace("%inbox_link%", "<a action=\"bypass _maillist_0_1_0_\">[&$917;]</a>");
                        html = html.replace("%sentbox_link%", "<a action=\"bypass _maillist_1_1_0_\">[&$918;]</a>");
                        html = html.replace("%archive_link%", "[&$919;]");
                        html = html.replace("%temp_archive_link%", "<a action=\"bypass _maillist_3_1_0_\">[&$920;]</a>");
                        html = html.replace("%TREE%", "&$919;");
                        html = html.replace("%writer_header%", "&$911;");
                        break;
                    }
                    case 3: {
                        html = html.replace("%inbox_link%", "<a action=\"bypass _maillist_0_1_0_\">[&$917;]</a>");
                        html = html.replace("%sentbox_link%", "<a action=\"bypass _maillist_1_1_0_\">[&$918;]</a>");
                        html = html.replace("%archive_link%", "<a action=\"bypass _maillist_2_1_0_\">[&$919;]</a>");
                        html = html.replace("%temp_archive_link%", "[&$920;]");
                        html = html.replace("%TREE%", "&$920;");
                        html = html.replace("%writer_header%", "&$909;");
                        break;
                    }
                }
                if (mailList != null) {
                    final int start = (page - 1) * 10;
                    final int end = Math.min(page * 10, mailList.size());
                    if (page == 1) {
                        html = html.replace("%ACTION_GO_LEFT%", "");
                        html = html.replace("%GO_LIST%", "");
                        html = html.replace("%NPAGE%", "1");
                    } else {
                        html = html.replace("%ACTION_GO_LEFT%", "bypass _maillist_" + type + "_" + (page - 1) + "_" + byTitle + "_" + search);
                        html = html.replace("%NPAGE%", String.valueOf(page));
                        final StringBuilder goList = new StringBuilder("");
                        for (int i = (page > 10) ? (page - 10) : 1; i < page; ++i) {
                            goList.append("<td><a action=\"bypass _maillist_").append(type).append("_").append(i).append("_").append(byTitle).append("_").append(search).append("\"> ").append(i).append(" </a> </td>\n\n");
                        }
                        html = html.replace("%GO_LIST%", goList.toString());
                    }
                    int pages = Math.max(mailList.size() / 10, 1);
                    if (mailList.size() > pages * 10) {
                        ++pages;
                    }
                    if (pages > page) {
                        html = html.replace("%ACTION_GO_RIGHT%", "bypass _maillist_" + type + "_" + (page + 1) + "_" + byTitle + "_" + search);
                        final int ep = Math.min(page + 10, pages);
                        final StringBuilder goList2 = new StringBuilder("");
                        for (int j = page + 1; j <= ep; ++j) {
                            goList2.append("<td><a action=\"bypass _maillist_").append(type).append("_").append(j).append("_").append(byTitle).append("_").append(search).append("\"> ").append(j).append(" </a> </td>\n\n");
                        }
                        html = html.replace("%GO_LIST2%", goList2.toString());
                    } else {
                        html = html.replace("%ACTION_GO_RIGHT%", "");
                        html = html.replace("%GO_LIST2%", "");
                    }
                    final StringBuilder ml = new StringBuilder("");
                    final String tpl = HtmCache.getInstance().getNotNull("scripts/services/community/bbs_mailtpl.htm", player);
                    for (int j = start; j < end; ++j) {
                        final MailData md = mailList.get(j);
                        String mailtpl = tpl;
                        mailtpl = mailtpl.replace("%action%", "bypass _mailread_" + md.messageId + "_" + type + "_" + page + "_" + byTitle + "_" + search);
                        mailtpl = mailtpl.replace("%writer%", md.author);
                        mailtpl = mailtpl.replace("%title%", md.title);
                        mailtpl = mailtpl.replace("%post_date%", md.postDate);
                        ml.append(mailtpl);
                    }
                    html = html.replace("%MAIL_LIST%", ml.toString());
                } else {
                    html = html.replace("%ACTION_GO_LEFT%", "");
                    html = html.replace("%GO_LIST%", "");
                    html = html.replace("%NPAGE%", "1");
                    html = html.replace("%GO_LIST2%", "");
                    html = html.replace("%ACTION_GO_RIGHT%", "");
                    html = html.replace("%MAIL_LIST%", "");
                }
                html = html.replace("%mailbox_type%", String.valueOf(type));
                html = html.replace("%incomming_mail_no%", String.valueOf(inbox));
                html = html.replace("%sent_mail_no%", String.valueOf(send));
                html = html.replace("%archived_mail_no%", "0");
                html = html.replace("%temp_mail_no%", "0");
                ShowBoard.separateAndSend(html, player);
                break;
            }
            case "mailread":
                final int messageId = Integer.parseInt(st.nextToken());
                final int type2 = Integer.parseInt(st.nextToken());
                final int page2 = Integer.parseInt(st.nextToken());
                final int byTitle2 = Integer.parseInt(st.nextToken());
                final String search2 = st.hasMoreTokens() ? st.nextToken() : "";
                Connection con2 = null;
                PreparedStatement statement2 = null;
                ResultSet rset2 = null;
                try {
                    con2 = DatabaseFactory.getInstance().getConnection();
                    statement2 = con2.prepareStatement("SELECT * FROM `bbs_mail` WHERE `message_id` = ? and `box_type` = ? and `to_object_id` = ?");
                    statement2.setInt(1, messageId);
                    statement2.setInt(2, type2);
                    statement2.setInt(3, player.getObjectId());
                    rset2 = statement2.executeQuery();
                    if (rset2.next()) {
                        String html2 = HtmCache.getInstance().getNotNull("scripts/services/community/bbs_mail_read.htm", player);
                        switch (type2) {
                            case 0: {
                                html2 = html2.replace("%TREE%", "<a action=\"bypass _maillist_0_1_0_\">&$917;</a>");
                                break;
                            }
                            case 1: {
                                html2 = html2.replace("%TREE%", "<a action=\"bypass _maillist_1_1_0__\">&$918;</a>");
                                break;
                            }
                            case 2: {
                                html2 = html2.replace("%TREE%", "<a action=\"bypass _maillist_2_1_0__\">&$919;</a>");
                                break;
                            }
                            case 3: {
                                html2 = html2.replace("%TREE%", "<a action=\"bypass _maillist_3_1_0__\">&$920;</a>");
                                break;
                            }
                        }
                        html2 = html2.replace("%writer%", rset2.getString("from_name"));
                        html2 = html2.replace("%post_date%", String.format("%1$te-%1$tm-%1$tY", new Date(rset2.getInt("post_date") * 1000L)));
                        html2 = html2.replace("%del_date%", String.format("%1$te-%1$tm-%1$tY", new Date((rset2.getInt("post_date") + 7776000) * 1000L)));
                        html2 = html2.replace("%char_name%", rset2.getString("to_name"));
                        html2 = html2.replace("%title%", rset2.getString("title"));
                        html2 = html2.replace("%CONTENT%", rset2.getString("message").replace("\n", "<br1>"));
                        html2 = html2.replace("%GOTO_LIST_LINK%", "bypass _maillist_" + type2 + "_" + page2 + "_" + byTitle2 + "_" + search2);
                        html2 = html2.replace("%message_id%", String.valueOf(messageId));
                        html2 = html2.replace("%mailbox_type%", String.valueOf(type2));
                        player.setSessionVar("add_fav", bypass + "&" + rset2.getString("title"));
                        statement2.close();
                        statement2 = con2.prepareStatement("UPDATE `bbs_mail` SET `read` = `read` + 1 WHERE message_id = ?");
                        statement2.setInt(1, messageId);
                        statement2.execute();
                        ShowBoard.separateAndSend(html2, player);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    DbUtils.closeQuietly(con2, statement2, rset2);
                }
                onBypassCommand(player, "_maillist_" + type2 + "_" + page2 + "_" + byTitle2 + "_" + search2);
                break;
            case "maildelete": {
                final int type = Integer.parseInt(st.nextToken());
                final int messageId2 = Integer.parseInt(st.nextToken());
                Connection con3 = null;
                PreparedStatement statement3 = null;
                try {
                    con3 = DatabaseFactory.getInstance().getConnection();
                    statement3 = con3.prepareStatement("DELETE FROM `bbs_mail` WHERE `box_type` = ? and `message_id` = ? and `to_object_id` = ?");
                    statement3.setInt(1, type);
                    statement3.setInt(2, messageId2);
                    statement3.setInt(3, player.getObjectId());
                    statement3.execute();
                } catch (Exception ex2) {
                } finally {
                    DbUtils.closeQuietly(con3, statement3);
                }
                onBypassCommand(player, "_maillist_" + type + "_1_0_");
                break;
            }
        }
    }

    @Override
    public void onWriteCommand(final Player player, final String bypass, final String arg1, final String arg2, final String arg3, final String arg4, final String arg5) {
        final StringTokenizer st = new StringTokenizer(bypass, "_");
        final String cmd = st.nextToken();
        if ("mailsearch".equals(cmd)) {
            onBypassCommand(player, "_maillist_" + st.nextToken() + "_1_" + ("Title".equals(arg3) ? "1_" : "0_") + ((arg5 != null) ? arg5 : ""));
        }
    }

    private static class MailData {
        public String author;
        public String title;
        public String postDate;
        public int messageId;

        public MailData(final String _author, final String _title, final int _postDate, final int _messageId) {
            author = _author;
            title = _title;
            postDate = String.format("%1$te-%1$tm-%1$tY", new Date(_postDate * 1000L));
            messageId = _messageId;
        }
    }
}
