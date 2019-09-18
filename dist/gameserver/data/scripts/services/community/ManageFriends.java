package services.community;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.HtmCache;
import ru.j2dev.gameserver.handler.bbs.CommunityBoardManager;
import ru.j2dev.gameserver.handler.bbs.ICommunityBoardHandler;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.actor.instances.player.Friend;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ShowBoard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

public class ManageFriends implements OnInitScriptListener, ICommunityBoardHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ManageFriends.class);

    private static String getFriendList(final Player player) {
        final StringBuilder friendList = new StringBuilder("");
        final Map<Integer, Friend> fl = player.getFriendList().getList();
        for (final Entry<Integer, Friend> entry : fl.entrySet()) {
            friendList.append("<a action=\"bypass _friendlist_1_").append(entry.getKey()).append("\">").append(entry.getValue().getName()).append("</a> (").append(entry.getValue().isOnline() ? "on" : "off").append(") &nbsp;");
        }
        return friendList.toString();
    }

    private static String getSelectedList(final Player player) {
        final String selected = player.getSessionVar("selFriends");
        if (selected == null) {
            return "";
        }
        final String[] sels = selected.split(";");
        final StringBuilder selectedList = new StringBuilder("");
        for (final String objectId : sels) {
            if (!objectId.isEmpty()) {
                selectedList.append("<a action=\"bypass _friendlist_2_").append(objectId).append("\">").append(player.getFriendList().getList().get(Integer.parseInt(objectId)).getName()).append("</a>;");
            }
        }
        return selectedList.toString();
    }

    private static String getBlockList(final Player player) {
        final StringBuilder blockList = new StringBuilder("");
        final Map<Integer, String> bl = player.getBlockListMap();
        for (final Integer objectId : bl.keySet()) {
            blockList.append(bl.get(objectId)).append("&nbsp; <a action=\"bypass _friendblockdelete_").append(objectId).append("\">Delete</a>&nbsp;&nbsp;");
        }
        return blockList.toString();
    }

    @Override
    public void onInit() {
        if (Config.COMMUNITYBOARD_ENABLED) {
            ManageFriends.LOGGER.info("CommunityBoard: Manage Friends service loaded.");
            CommunityBoardManager.getInstance().registerHandler(this);
        }
    }

    @Override
    public String[] getBypassCommands() {
        return new String[]{"_friendlist_", "_friendblocklist_", "_frienddelete_", "_frienddeleteallconfirm_", "_frienddeleteall_", "_friendblockdelete_", "_friendblockadd_", "_friendblockdeleteallconfirm_", "_friendblockdeleteall_"};
    }

    @Override
    public void onBypassCommand(final Player player, final String bypass) {
        final StringTokenizer st = new StringTokenizer(bypass, "_");
        final String cmd = st.nextToken();
        String html = HtmCache.getInstance().getNotNull(cmd.startsWith("friendbloc") ? "scripts/services/community/bbs_block_list.htm" : "scripts/services/community/bbs_friend_list.htm", player);
        player.setSessionVar("add_fav", null);
        switch (cmd) {
            case "friendlist":
                final String act = st.nextToken();
                html = html.replace("%friend_list%", getFriendList(player));
                switch (act) {
                    case "0":
                        if (player.getSessionVar("selFriends") != null) {
                            player.setSessionVar("selFriends", null);
                        }
                        html = html.replace("%selected_friend_list%", "");
                        html = html.replace("%delete_all_msg%", "");
                        break;
                    case "1": {
                        final String objId = st.nextToken();
                        String selected;
                        if ((selected = player.getSessionVar("selFriends")) == null) {
                            selected = objId + ";";
                        } else if (!selected.contains(objId)) {
                            selected = selected + objId + ";";
                        }
                        player.setSessionVar("selFriends", selected);
                        html = html.replace("%selected_friend_list%", getSelectedList(player));
                        html = html.replace("%delete_all_msg%", "");
                        break;
                    }
                    case "2": {
                        final String objId = st.nextToken();
                        String selected = player.getSessionVar("selFriends");
                        if (selected != null) {
                            selected = selected.replace(objId + ";", "");
                            player.setSessionVar("selFriends", selected);
                        }
                        html = html.replace("%selected_friend_list%", getSelectedList(player));
                        html = html.replace("%delete_all_msg%", "");
                        break;
                    }
                }
                break;
            case "frienddeleteallconfirm":
                html = html.replace("%friend_list%", getFriendList(player));
                html = html.replace("%selected_friend_list%", getSelectedList(player));
                html = html.replace("%delete_all_msg%", "<br>\nAre you sure you want to delete all friends from the friends list? <button value = \"OK\" action=\"bypass _frienddeleteall_\" back=\"l2ui_ct1.button.button_df_small_down\" width=70 height=25 fore=\"l2ui_ct1.button.button_df_small\">");
                break;
            case "frienddelete":
                final String selected2 = player.getSessionVar("selFriends");
                if (selected2 != null) {
                    for (final String objId2 : selected2.split(";")) {
                        if (!objId2.isEmpty()) {
                            player.getFriendList().removeFriend(player.getFriendList().getList().get(Integer.parseInt(objId2)).getName());
                        }
                    }
                }
                player.setSessionVar("selFriends", null);
                html = html.replace("%friend_list%", getFriendList(player));
                html = html.replace("%selected_friend_list%", "");
                html = html.replace("%delete_all_msg%", "");
                break;
            case "frienddeleteall":
                final List<Friend> friends = new ArrayList<>(1);
                friends.addAll(player.getFriendList().getList().values());
                for (final Friend friend : friends) {
                    player.getFriendList().removeFriend(friend.getName());
                }
                player.setSessionVar("selFriends", null);
                html = html.replace("%friend_list%", "");
                html = html.replace("%selected_friend_list%", "");
                html = html.replace("%delete_all_msg%", "");
                break;
            case "friendblocklist":
                html = html.replace("%block_list%", getBlockList(player));
                html = html.replace("%delete_all_msg%", "");
                break;
            case "friendblockdeleteallconfirm":
                html = html.replace("%block_list%", getBlockList(player));
                html = html.replace("%delete_all_msg%", "<br>\nDo you want to delete all characters from the block list? <button value = \"OK\" action=\"bypass _friendblockdeleteall_\" back=\"l2ui_ct1.button.button_df_small_down\" width=70 height=25 fore=\"l2ui_ct1.button.button_df_small\" >");
                break;
            case "friendblockdelete":
                final String objId3 = st.nextToken();
                if (objId3 != null && !objId3.isEmpty()) {
                    final int objectId = Integer.parseInt(objId3);
                    final String name = player.getBlockListMap().get(objectId);
                    if (name != null) {
                        player.removeFromBlockList(name);
                    }
                }
                html = html.replace("%block_list%", getBlockList(player));
                html = html.replace("%delete_all_msg%", "");
                break;
            case "friendblockdeleteall":
                final List<String> bl = new ArrayList<>(1);
                bl.addAll(player.getBlockList());
                for (final String name : bl) {
                    player.removeFromBlockList(name);
                }
                html = html.replace("%block_list%", "");
                html = html.replace("%delete_all_msg%", "");
                break;
        }
        ShowBoard.separateAndSend(html, player);
    }

    @Override
    public void onWriteCommand(final Player player, final String bypass, final String arg1, final String arg2, final String arg3, final String arg4, final String arg5) {
        String html = HtmCache.getInstance().getNotNull("scripts/services/community/bbs_block_list.htm", player);
        if ("_friendblockadd_".equals(bypass) && arg3 != null && !arg3.isEmpty()) {
            player.addToBlockList(arg3);
        }
        html = html.replace("%block_list%", getBlockList(player));
        html = html.replace("%delete_all_msg%", "");
        ShowBoard.separateAndSend(html, player);
    }
}
