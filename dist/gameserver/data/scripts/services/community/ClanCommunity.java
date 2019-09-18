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
import ru.j2dev.gameserver.listener.actor.player.OnPlayerEnterListener;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.actor.listener.CharListenerList;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.model.pledge.UnitMember;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExMailArrived;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ShowBoard;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.tables.ClanTable;
import ru.j2dev.gameserver.utils.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class ClanCommunity extends Functions implements OnInitScriptListener, ICommunityBoardHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClanCommunity.class);
    private static final int CLANS_PER_PAGE = 10;

    private final Listener _listener;

    public ClanCommunity() {
        _listener = new Listener();
    }

    private static List<Clan> getClanList(final String search, final boolean byCL) {
        ArrayList<Clan> clanList = new ArrayList<>();
        final Clan[] clans = ClanTable.getInstance().getClans();
        Arrays.sort(clans, new ClansComparator<Object>());
        for (final Clan clan : clans) {
            if (clan.getLevel() > 1) {
                clanList.add(clan);
            }
        }
        if (!search.isEmpty()) {
            final ArrayList<Clan> searchList = new ArrayList<>();
            for (final Clan clan2 : clanList) {
                if (byCL && clan2.getLeaderName().toLowerCase().contains(search.toLowerCase())) {
                    searchList.add(clan2);
                } else {
                    if (byCL || !clan2.getName().toLowerCase().contains(search.toLowerCase())) {
                        continue;
                    }
                    searchList.add(clan2);
                }
            }
            clanList = searchList;
        }
        return clanList;
    }

    @Override
    public void onInit() {
        CharListenerList.addGlobal(_listener);
        if (Config.COMMUNITYBOARD_ENABLED) {
            ClanCommunity.LOGGER.info("CommunityBoard: Clan Community service loaded.");
            CommunityBoardManager.getInstance().registerHandler(this);
        }
    }

    @Override
    public String[] getBypassCommands() {
        return new String[]{"_bbsclan", "_clbbsclan_", "_clbbslist_", "_clsearch", "_clbbsadmi", "_mailwritepledgeform", "_announcepledgewriteform", "_announcepledgeswitchshowflag", "_announcepledgewrite", "_clwriteintro", "_clwritemail"};
    }

    @Override
    public void onBypassCommand(final Player player, final String bypass) {
        final StringTokenizer st = new StringTokenizer(bypass, "_");
        final String cmd = st.nextToken();
        player.setSessionVar("add_fav", null);
        switch (cmd) {
            case "bbsclan": {
                final Clan clan = player.getClan();
                if (clan != null && clan.getLevel() > 1) {
                    onBypassCommand(player, "_clbbsclan_" + player.getClanId());
                    return;
                }
                onBypassCommand(player, "_clbbslist_1_0_");
                break;
            }
            case "clbbslist":
                final int page = Integer.parseInt(st.nextToken());
                final int byCL = Integer.parseInt(st.nextToken());
                final String search = st.hasMoreTokens() ? st.nextToken() : "";
                final HashMap<Integer, String> tpls = Util.parseTemplate(HtmCache.getInstance().getNotNull("scripts/services/community/bbs_clanlist.htm", player));
                String html = tpls.get(0);
                final Clan playerClan = player.getClan();
                if (playerClan != null) {
                    String my_clan = tpls.get(1);
                    my_clan = my_clan.replace("%PLEDGE_ID%", String.valueOf(playerClan.getClanId()));
                    my_clan = my_clan.replace("%MY_PLEDGE_NAME%", (playerClan.getLevel() > 1) ? playerClan.getName() : "");
                    html = html.replace("<?my_clan_link?>", my_clan);
                } else {
                    html = html.replace("<?my_clan_link?>", "");
                }
                final List<Clan> clanList = getClanList(search, byCL == 1);
                final int start = (page - 1) * 10;
                final int end = Math.min(page * 10, clanList.size());
                if (page == 1) {
                    html = html.replace("%ACTION_GO_LEFT%", "");
                    html = html.replace("%GO_LIST%", "");
                    html = html.replace("%NPAGE%", "1");
                } else {
                    html = html.replace("%ACTION_GO_LEFT%", "bypass _clbbslist_" + (page - 1) + "_" + byCL + "_" + search);
                    html = html.replace("%NPAGE%", String.valueOf(page));
                    final StringBuilder goList = new StringBuilder("");
                    for (int i = (page > 10) ? (page - 10) : 1; i < page; ++i) {
                        goList.append("<td><a action=\"bypass _clbbslist_").append(i).append("_").append(byCL).append("_").append(search).append("\"> ").append(i).append(" </a> </td>\n\n");
                    }
                    html = html.replace("%GO_LIST%", goList.toString());
                }
                int pages = Math.max(clanList.size() / 10, 1);
                if (clanList.size() > pages * 10) {
                    ++pages;
                }
                if (pages > page) {
                    html = html.replace("%ACTION_GO_RIGHT%", "bypass _clbbslist_" + (page + 1) + "_" + byCL + "_" + search);
                    final int ep = Math.min(page + 10, pages);
                    final StringBuilder goList2 = new StringBuilder("");
                    for (int j = page + 1; j <= ep; ++j) {
                        goList2.append("<td><a action=\"bypass _clbbslist_").append(j).append("_").append(byCL).append("_").append(search).append("\"> ").append(j).append(" </a> </td>\n\n");
                    }
                    html = html.replace("%GO_LIST2%", goList2.toString());
                } else {
                    html = html.replace("%ACTION_GO_RIGHT%", "");
                    html = html.replace("%GO_LIST2%", "");
                }
                final StringBuilder cl = new StringBuilder("");
                final String tpl = HtmCache.getInstance().getNotNull("scripts/services/community/bbs_clantpl.htm", player);
                for (int j = start; j < end; ++j) {
                    final Clan clan2 = clanList.get(j);
                    String clantpl = tpl;
                    clantpl = clantpl.replace("%action_clanhome%", "bypass _clbbsclan_" + clan2.getClanId());
                    clantpl = clantpl.replace("%clan_name%", clan2.getName());
                    clantpl = clantpl.replace("%clan_owner%", clan2.getLeaderName());
                    clantpl = clantpl.replace("%skill_level%", String.valueOf(clan2.getLevel()));
                    clantpl = clantpl.replace("%member_count%", String.valueOf(clan2.getAllSize()));
                    cl.append(clantpl);
                }
                html = html.replace("%CLAN_LIST%", cl.toString());
                ShowBoard.separateAndSend(html, player);
                break;
            case "clbbsclan": {
                final int clanId = Integer.parseInt(st.nextToken());
                if (clanId == 0) {
                    player.sendPacket(new SystemMessage(238));
                    onBypassCommand(player, "_clbbslist_1_0");
                    return;
                }
                final Clan clan3 = ClanTable.getInstance().getClan(clanId);
                if (clan3 == null) {
                    onBypassCommand(player, "_clbbslist_1_0");
                    return;
                }
                if (clan3.getLevel() < 2) {
                    player.sendPacket(new SystemMessage(1050));
                    onBypassCommand(player, "_clbbslist_1_0");
                    return;
                }
                Connection con = null;
                PreparedStatement statement = null;
                ResultSet rset = null;
                String intro = "";
                try {
                    con = DatabaseFactory.getInstance().getConnection();
                    statement = con.prepareStatement("SELECT * FROM `bbs_clannotice` WHERE `clan_id` = ? and type = 2");
                    statement.setInt(1, clanId);
                    rset = statement.executeQuery();
                    if (rset.next()) {
                        intro = rset.getString("notice");
                    }
                } catch (Exception ex) {
                } finally {
                    DbUtils.closeQuietly(con, statement, rset);
                }
                final HashMap<Integer, String> tpls2 = Util.parseTemplate(HtmCache.getInstance().getNotNull("scripts/services/community/bbs_clan.htm", player));
                String html2 = tpls2.get(0);
                html2 = html2.replace("%PLEDGE_ID%", String.valueOf(clanId));
                html2 = html2.replace("%ACTION_ANN%", "");
                html2 = html2.replace("%ACTION_FREE%", "");
                if (player.getClanId() == clanId && player.isClanLeader()) {
                    html2 = html2.replace("<?menu?>", tpls2.get(1));
                } else {
                    html2 = html2.replace("<?menu?>", "");
                }
                html2 = html2.replace("%CLAN_INTRO%", intro.replace("\n", "<br1>"));
                html2 = html2.replace("%CLAN_NAME%", clan3.getName());
                html2 = html2.replace("%SKILL_LEVEL%", String.valueOf(clan3.getLevel()));
                html2 = html2.replace("%CLAN_MEMBERS%", String.valueOf(clan3.getAllSize()));
                html2 = html2.replace("%OWNER_NAME%", clan3.getLeaderName());
                html2 = html2.replace("%ALLIANCE_NAME%", (clan3.getAlliance() != null) ? clan3.getAlliance().getAllyName() : "");
                html2 = html2.replace("%ANN_LIST%", "");
                html2 = html2.replace("%THREAD_LIST%", "");
                ShowBoard.separateAndSend(html2, player);
                break;
            }
            case "clbbsadmi": {
                final Clan clan = player.getClan();
                if (clan == null || clan.getLevel() < 2 || !player.isClanLeader()) {
                    onBypassCommand(player, "_clbbsclan_" + player.getClanId());
                    return;
                }
                String html3 = HtmCache.getInstance().getNotNull("scripts/services/community/bbs_clanadmin.htm", player);
                html3 = html3.replace("%PLEDGE_ID%", String.valueOf(clan.getClanId()));
                html3 = html3.replace("%ACTION_ANN%", "");
                html3 = html3.replace("%ACTION_FREE%", "");
                html3 = html3.replace("%CLAN_NAME%", clan.getName());
                html3 = html3.replace("%per_list%", "");
                Connection con = null;
                PreparedStatement statement = null;
                ResultSet rset = null;
                String intro = "";
                try {
                    con = DatabaseFactory.getInstance().getConnection();
                    statement = con.prepareStatement("SELECT * FROM `bbs_clannotice` WHERE `clan_id` = ? and type = 2");
                    statement.setInt(1, clan.getClanId());
                    rset = statement.executeQuery();
                    if (rset.next()) {
                        intro = rset.getString("notice");
                    }
                } catch (Exception ex2) {
                } finally {
                    DbUtils.closeQuietly(con, statement, rset);
                }
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
                args.add(intro);
                args.add("");
                args.add("");
                args.add("0");
                args.add("0");
                args.add("");
                player.sendPacket(new ShowBoard(html3, "1001", player));
                player.sendPacket(new ShowBoard(args));
                break;
            }
            case "mailwritepledgeform": {
                final Clan clan = player.getClan();
                if (clan == null || clan.getLevel() < 2 || !player.isClanLeader()) {
                    onBypassCommand(player, "_clbbsclan_" + player.getClanId());
                    return;
                }
                String html3 = HtmCache.getInstance().getNotNull("scripts/services/community/bbs_pledge_mail_write.htm", player);
                html3 = html3.replace("%PLEDGE_ID%", String.valueOf(clan.getClanId()));
                html3 = html3.replace("%pledge_id%", String.valueOf(clan.getClanId()));
                html3 = html3.replace("%pledge_name%", clan.getName());
                ShowBoard.separateAndSend(html3, player);
                break;
            }
            case "announcepledgewriteform": {
                final Clan clan = player.getClan();
                if (clan == null || clan.getLevel() < 2 || !player.isClanLeader()) {
                    onBypassCommand(player, "_clbbsclan_" + player.getClanId());
                    return;
                }
                final HashMap<Integer, String> tpls3 = Util.parseTemplate(HtmCache.getInstance().getNotNull("scripts/services/community/bbs_clanannounce.htm", player));
                String html4 = tpls3.get(0);
                html4 = html4.replace("%PLEDGE_ID%", String.valueOf(clan.getClanId()));
                html4 = html4.replace("%ACTION_ANN%", "");
                html4 = html4.replace("%ACTION_FREE%", "");
                Connection con2 = null;
                PreparedStatement statement2 = null;
                ResultSet rset2 = null;
                String notice = "";
                int type = 0;
                try {
                    con2 = DatabaseFactory.getInstance().getConnection();
                    statement2 = con2.prepareStatement("SELECT * FROM `bbs_clannotice` WHERE `clan_id` = ? and type != 2");
                    statement2.setInt(1, clan.getClanId());
                    rset2 = statement2.executeQuery();
                    if (rset2.next()) {
                        notice = rset2.getString("notice");
                        type = rset2.getInt("type");
                    }
                } catch (Exception ex3) {
                } finally {
                    DbUtils.closeQuietly(con2, statement2, rset2);
                }
                if (type == 0) {
                    html4 = html4.replace("<?usage?>", tpls3.get(1));
                } else {
                    html4 = html4.replace("<?usage?>", tpls3.get(2));
                }
                html4 = html4.replace("%flag%", String.valueOf(type));
                final List<String> args2 = new ArrayList<>();
                args2.add("0");
                args2.add("0");
                args2.add("0");
                args2.add("0");
                args2.add("0");
                args2.add("0");
                args2.add("");
                args2.add("0");
                args2.add("");
                args2.add("0");
                args2.add("");
                args2.add("");
                args2.add(notice);
                args2.add("");
                args2.add("");
                args2.add("0");
                args2.add("0");
                args2.add("");
                player.sendPacket(new ShowBoard(html4, "1001", player));
                player.sendPacket(new ShowBoard(args2));
                break;
            }
            case "announcepledgeswitchshowflag": {
                final Clan clan = player.getClan();
                if (clan == null || clan.getLevel() < 2 || !player.isClanLeader()) {
                    onBypassCommand(player, "_clbbsclan_" + player.getClanId());
                    return;
                }
                final int type2 = Integer.parseInt(st.nextToken());
                Connection con = null;
                PreparedStatement statement = null;
                try {
                    con = DatabaseFactory.getInstance().getConnection();
                    statement = con.prepareStatement("UPDATE `bbs_clannotice` SET type = ? WHERE `clan_id` = ? and type = ?");
                    statement.setInt(1, type2);
                    statement.setInt(2, clan.getClanId());
                    statement.setInt(3, (type2 != 1) ? 1 : 0);
                    statement.execute();
                } catch (Exception ex4) {
                } finally {
                    DbUtils.closeQuietly(con, statement);
                }
                clan.setNotice((type2 == 0) ? "" : null);
                onBypassCommand(player, "_announcepledgewriteform");
                break;
            }
        }
    }

    @Override
    public void onWriteCommand(final Player player, final String bypass, final String arg1, final String arg2, String arg3, final String arg4, String arg5) {
        final StringTokenizer st = new StringTokenizer(bypass, "_");
        final String cmd = st.nextToken();
        switch (cmd) {
            case "clsearch":
                if (arg3 == null) {
                    arg3 = "";
                }
                onBypassCommand(player, "_clbbslist_1_" + ("Ruler".equals(arg4) ? "1" : "0") + "_" + arg3);
                break;
            case "clwriteintro": {
                final Clan clan = player.getClan();
                if (clan == null || clan.getLevel() < 2 || !player.isClanLeader() || arg3 == null || arg3.isEmpty()) {
                    onBypassCommand(player, "_clbbsclan_" + player.getClanId());
                    return;
                }
                arg3 = arg3.replace("<", "");
                arg3 = arg3.replace(">", "");
                arg3 = arg3.replace("&", "");
                arg3 = arg3.replace("$", "");
                arg3 = arg3.replace("\"", "&quot;");
                if (arg3.length() > 3000) {
                    arg3 = arg3.substring(0, 3000);
                }
                Connection con = null;
                PreparedStatement statement = null;
                try {
                    con = DatabaseFactory.getInstance().getConnection();
                    statement = con.prepareStatement("REPLACE INTO `bbs_clannotice`(clan_id, type, notice) VALUES(?, ?, ?)");
                    statement.setInt(1, clan.getClanId());
                    statement.setInt(2, 2);
                    statement.setString(3, arg3);
                    statement.execute();
                } catch (Exception ex) {
                } finally {
                    DbUtils.closeQuietly(con, statement);
                }
                onBypassCommand(player, "_clbbsclan_" + player.getClanId());
                break;
            }
            case "clwritemail": {
                final Clan clan = player.getClan();
                if (clan == null || clan.getLevel() < 2 || !player.isClanLeader()) {
                    onBypassCommand(player, "_clbbsclan_" + player.getClanId());
                    return;
                }
                if (arg3 == null || arg4 == null) {
                    player.sendPacket(Msg.THE_MESSAGE_WAS_NOT_SENT);
                    onBypassCommand(player, "_clbbsclan_" + player.getClanId());
                    return;
                }
                arg3 = arg3.replace("<", "");
                arg3 = arg3.replace(">", "");
                arg3 = arg3.replace("&", "");
                arg3 = arg3.replace("$", "");
                arg3 = arg3.replace("\"", "&quot;");
                arg5 = arg5.replace("<", "");
                arg5 = arg5.replace(">", "");
                arg5 = arg5.replace("&", "");
                arg5 = arg5.replace("$", "");
                arg5 = arg5.replace("\"", "&quot;");
                if (arg3.isEmpty() || arg4.isEmpty()) {
                    player.sendPacket(Msg.THE_MESSAGE_WAS_NOT_SENT);
                    onBypassCommand(player, "_clbbsclan_" + player.getClanId());
                    return;
                }
                if (arg3.length() > 128) {
                    arg3 = arg3.substring(0, 128);
                }
                if (arg4.length() > 3000) {
                    arg5 = arg5.substring(0, 3000);
                }
                Connection con = null;
                PreparedStatement statement = null;
                try {
                    con = DatabaseFactory.getInstance().getConnection();
                    statement = con.prepareStatement("INSERT INTO `bbs_mail`(to_name, to_object_id, from_name, from_object_id, title, message, post_date, box_type) VALUES(?, ?, ?, ?, ?, ?, ?, 0)");
                    for (final UnitMember clm : clan) {
                        statement.setString(1, clan.getName());
                        statement.setInt(2, clm.getObjectId());
                        statement.setString(3, player.getName());
                        statement.setInt(4, player.getObjectId());
                        statement.setString(5, arg3);
                        statement.setString(6, arg5);
                        statement.setInt(7, (int) (System.currentTimeMillis() / 1000L));
                        statement.execute();
                    }
                    statement.close();
                    statement = con.prepareStatement("INSERT INTO `bbs_mail`(to_name, to_object_id, from_name, from_object_id, title, message, post_date, box_type) VALUES(?, ?, ?, ?, ?, ?, ?, 1)");
                    statement.setString(1, clan.getName());
                    statement.setInt(2, player.getObjectId());
                    statement.setString(3, player.getName());
                    statement.setInt(4, player.getObjectId());
                    statement.setString(5, arg3);
                    statement.setString(6, arg5);
                    statement.setInt(7, (int) (System.currentTimeMillis() / 1000L));
                    statement.execute();
                } catch (Exception e2) {
                    player.sendPacket(Msg.THE_MESSAGE_WAS_NOT_SENT);
                    onBypassCommand(player, "_clbbsclan_" + player.getClanId());
                    return;
                } finally {
                    DbUtils.closeQuietly(con, statement);
                }
                player.sendPacket(Msg.YOUVE_SENT_MAIL);
                for (final Player member : clan.getOnlineMembers(0)) {
                    member.sendPacket(Msg.YOUVE_GOT_MAIL);
                    member.sendPacket(ExMailArrived.STATIC);
                }
                onBypassCommand(player, "_clbbsclan_" + player.getClanId());
                break;
            }
            case "announcepledgewrite": {
                final Clan clan = player.getClan();
                if (clan == null || clan.getLevel() < 2 || !player.isClanLeader()) {
                    onBypassCommand(player, "_clbbsclan_" + player.getClanId());
                    return;
                }
                if (arg3 == null || arg3.isEmpty()) {
                    onBypassCommand(player, "_announcepledgewriteform");
                    return;
                }
                arg3 = arg3.replace("<", "");
                arg3 = arg3.replace(">", "");
                arg3 = arg3.replace("&", "");
                arg3 = arg3.replace("$", "");
                arg3 = arg3.replace("\"", "&quot;");
                if (arg3.isEmpty()) {
                    onBypassCommand(player, "_announcepledgewriteform");
                    return;
                }
                if (arg3.length() > 3000) {
                    arg3 = arg3.substring(0, 3000);
                }
                final int type = Integer.parseInt(st.nextToken());
                Connection con2 = null;
                PreparedStatement statement2 = null;
                try {
                    con2 = DatabaseFactory.getInstance().getConnection();
                    statement2 = con2.prepareStatement("REPLACE INTO `bbs_clannotice`(clan_id, type, notice) VALUES(?, ?, ?)");
                    statement2.setInt(1, clan.getClanId());
                    statement2.setInt(2, type);
                    statement2.setString(3, arg3);
                    statement2.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                    onBypassCommand(player, "_announcepledgewriteform");
                    return;
                } finally {
                    DbUtils.closeQuietly(con2, statement2);
                }
                if (type == 1) {
                    clan.setNotice(arg3.replace("\n", "<br1>"));
                } else {
                    clan.setNotice("");
                }
                player.sendPacket(Msg.NOTICE_HAS_BEEN_SAVED);
                onBypassCommand(player, "_announcepledgewriteform");
                break;
            }
        }
    }

    private static class ClansComparator<T> implements Comparator<T> {
        @Override
        public int compare(final Object o1, final Object o2) {
            if (o1 instanceof Clan && o2 instanceof Clan) {
                final Clan p1 = (Clan) o1;
                final Clan p2 = (Clan) o2;
                return p1.getName().compareTo(p2.getName());
            }
            return 0;
        }
    }

    private class Listener implements OnPlayerEnterListener {
        @Override
        public void onPlayerEnter(final Player player) {
            final Clan clan = player.getClan();
            if (clan == null || clan.getLevel() < 2) {
                return;
            }
            if (clan.getNotice() == null) {
                Connection con = null;
                PreparedStatement statement = null;
                ResultSet rset = null;
                String notice = "";
                int type = 0;
                try {
                    con = DatabaseFactory.getInstance().getConnection();
                    statement = con.prepareStatement("SELECT * FROM `bbs_clannotice` WHERE `clan_id` = ? and type != 2");
                    statement.setInt(1, clan.getClanId());
                    rset = statement.executeQuery();
                    if (rset.next()) {
                        notice = rset.getString("notice");
                        type = rset.getInt("type");
                    }
                } catch (Exception ignored) {
                } finally {
                    DbUtils.closeQuietly(con, statement, rset);
                }
                clan.setNotice((type == 1) ? notice.replace("\n", "<br1>\n") : "");
            }
            if (!clan.getNotice().isEmpty()) {
                String html = HtmCache.getInstance().getNotNull("scripts/services/community/clan_popup.htm", player);
                html = html.replace("%pledge_name%", clan.getName());
                html = html.replace("%content%", clan.getNotice());
                player.sendPacket(new NpcHtmlMessage(0).setHtml(html));
            }
        }
    }
}
