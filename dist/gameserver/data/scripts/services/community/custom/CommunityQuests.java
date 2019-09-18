package services.community.custom;

import ru.j2dev.commons.lang.ArrayUtils;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.HtmCache;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.handler.bbs.CommunityBoardManager;
import ru.j2dev.gameserver.handler.bbs.ICommunityBoardHandler;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.base.ClassId;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.*;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.utils.HtmlUtils;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.Log;
import ru.j2dev.gameserver.utils.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

public class CommunityQuests implements OnInitScriptListener, ICommunityBoardHandler {
    private static final int[] class_levels = {20, 40, 76};


    public static String htmlButton(final String value, final int width, final int height, final String function, final Object... args) {
        String action = Arrays.stream(args).map(arg -> " " + arg).collect(Collectors.joining("", "bypass " + function, ""));
        return HtmlUtils.htmlButton(value, action, width, height);
    }

    public static String htmlButton(final String value, final int width, final String function, final Object... args) {
        return htmlButton(value, width, 22, function, args);
    }

    private static boolean checkHaveItem(final Player player, final int itemId, final long count) {
        if (Functions.getItemCount(player, itemId) < count) {
            if (itemId == 57) {
                player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
            } else {
                player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
            }
            return false;
        }
        return true;
    }

    private static ArrayList<ClassId> getAvailClasses(final ClassId playerClass) {
        return Arrays.stream(ClassId.values()).filter(_class -> _class.getLevel() == playerClass.getLevel() + 1 && _class.childOf(playerClass)).collect(Collectors.toCollection(ArrayList::new));
    }

    private static String tableOcupation(final Player player) {
        final ClassId playerClass = player.getClassId();
        final String playerClassName = getClassIdSysstring(player, playerClass.getId());
        final StringBuilder result = new StringBuilder();
        result.append("<center>");
        result.append(localize(player, 1)).append(": <font color=LEVEL>").append(playerClassName).append("</font>");
        result.append("</center>");
        if (playerClass.getLevel() == 4) {
            return result + "<br>";
        }
        final int need_level = class_levels[playerClass.getLevel() - 1];
        if (player.getLevel() < need_level) {
            return result + "<br1>" + localize(player, 2) + ": " + need_level + "<br>";
        }
        final ArrayList<ClassId> avail_classes = getAvailClasses(playerClass);
        if (avail_classes.size() == 0) {
            return result + "<br>";
        }
        result.append("<center><table>");
        result.append("<tr><td>");
        switch (playerClass.getLevel()) {
            case 1: {
                result.append(localize(player, 3, ACbConfigManager.FIRST_CLASS_ID, ACbConfigManager.FIRST_CLASS_PRICE));
                break;
            }
            case 2: {
                result.append(localize(player, 3, ACbConfigManager.SECOND_CLASS_ID, ACbConfigManager.SECOND_CLASS_PRICE));
                break;
            }
            case 3: {
                result.append(localize(player, 3, ACbConfigManager.THRID_CLASS_ID, ACbConfigManager.THRID_CLASS_PRICE));
                break;
            }
        }
        result.append("</td></tr>");
        avail_classes.forEach(_class -> {
            final String _className = getClassIdSysstring(player, _class.getId());
            result.append("<tr><td>");
            result.append("<center><button value=\"");
            result.append(_className);
            result.append("\" action=\"bypass _cbbsquestsocupation ");
            result.append(_class.getId());
            if (playerClass.getLevel() == 3) {
                result.append(" 0");
            }
            result.append("\" back=\"L2UI_CH3.bigbutton3_down\" fore=\"L2UI_CH3.bigbutton3\" width=134 height=22></center>");
            result.append("</td></tr>");
        });
        result.append("</table></center><br><br>");
        return result.toString();
    }

    public static String localize(final Player player, final int ID, final Object... args) {
        final boolean ru = player.isLangRus();
        switch (ID) {
            case 1: {
                return ru ? "Ваша текущая профессия" : "Your current occupation";
            }
            case 2: {
                return ru ? "Для получения следующей профессии вы должны достичь уровня" : "To get your's next occupation you should reach level";
            }
            case 3: {
                final int itemId = ((Number) args[0]).intValue();
                final String itemName = ItemTemplateHolder.getInstance().getTemplate(itemId).getName();
                final long itemCount = ((Number) args[1]).longValue();
                return ru ? ("Цена за профессию : " + Util.formatAdena(itemCount) + " " + itemName) : ("Class occupation price: " + Util.formatAdena(itemCount) + " " + itemName);
            }
            default: {
                return "Unknown localize String - " + ID;
            }
        }
    }

    private static String getClassIdSysstring(final Player player, final int classId) {
        final String className = HtmlUtils.makeClassNameFString(player, classId);
        return (className != null) ? className : "Unknown";
    }

    private static boolean reducePoints(final Player player, final int count) {
        if (player == null || player.getPcBangPoints() < count) {
            return false;
        }
        player.setPcBangPoints(player.getPcBangPoints() - count);
        player.sendPacket(new ExPCCafePointInfo(player, 0, 1, 2, 12));
        return true;
    }

    @Override
    public void onInit() {
        if (Config.COMMUNITYBOARD_ENABLED) {
            CommunityBoardManager.getInstance().registerHandler(this);
        }
    }

    @Override
    public String[] getBypassCommands() {
        return new String[]{"_cbbsquestsmain", "_cbbsquestsocupation"};
    }

    @Override
    public void onBypassCommand(final Player player, final String bypass) {
        if (!CommunityTools.checkConditions(player)) {
            String html = HtmCache.getInstance().getNotNull("scripts/services/community/pages/locked.htm", player);
            html = html.replace("%name%", player.getName());
            ShowBoard.separateAndSend(html, player);
            return;
        }
        String html = HtmCache.getInstance().getNotNull("scripts/services/community/pages/quests.htm", player);
        String content = "";
        if (bypass.startsWith("_cbbsquestsmain")) {
            content = html(player);
        } else {
            final StringTokenizer bf = new StringTokenizer(bypass, " ");
            bf.nextToken();
            String[] arg = new String[0];
            while (bf.hasMoreTokens()) {
                arg = ArrayUtils.add(arg, bf.nextToken());
            }
            if (bypass.startsWith("_cbbsquestsocupation")) {
                content = getOcupation(arg, player);
            }
        }
        html = html.replace("%content%", content);
        ShowBoard.separateAndSend(html, player);
    }

    @Override
    public void onWriteCommand(final Player player, final String bypass, final String arg1, final String arg2, final String arg3, final String arg4, final String arg5) {
    }

    private String html(final Player player) {
        String result = "";
        result += tableOcupation(player);
        return result;
    }

    private String getOcupation(final String[] var, final Player player) {
        final ClassId playerClass = player.getClassId();
        if (playerClass.getLevel() == 4) {
            return html(player);
        }
        final int need_level = class_levels[playerClass.getLevel() - 1];
        if (player.getLevel() < need_level) {
            return html(player);
        }
        final int RequestClass = Integer.parseInt(var[0]);
        ClassId RequestClassId = null;
        final ArrayList<ClassId> avail_classes = getAvailClasses(playerClass);
        for (final ClassId _class : avail_classes) {
            if (_class.getId() == RequestClass) {
                RequestClassId = _class;
                break;
            }
        }
        if (RequestClassId == null) {
            return html(player);
        }
        int need_item_id = 0;
        int need_item_count = 0;
        switch (playerClass.getLevel()) {
            case 1: {
                need_item_id = ACbConfigManager.FIRST_CLASS_ID;
                need_item_count = ACbConfigManager.FIRST_CLASS_PRICE;
                break;
            }
            case 2: {
                need_item_id = ACbConfigManager.SECOND_CLASS_ID;
                need_item_count = ACbConfigManager.SECOND_CLASS_PRICE;
                break;
            }
            case 3: {
                need_item_id = ACbConfigManager.THRID_CLASS_ID;
                need_item_count = ACbConfigManager.THRID_CLASS_PRICE;
                break;
            }
        }
        if (need_item_id == 0 || need_item_count == 0) {
            return html(player);
        }
        if (need_item_id == -300) {
            if (!reducePoints(player, need_item_count)) {
                return html(player);
            }
        } else {
            if (!checkHaveItem(player, need_item_id, need_item_count)) {
                return html(player);
            }
            Functions.removeItem(player, need_item_id, (long) need_item_count);
        }
        Log.add("QUEST\t\u0421\u043c\u0435\u043d\u0430 \u043f\u0440\u043e\u0444\u0435\u0441\u0438\u0438 " + playerClass.getId() + " -> " + RequestClassId.getId() + " \u0437\u0430 " + need_item_id + ":" + need_item_count, "service_quests", player);
        if (ACbConfigManager.COMMUNITY_CLASS_MASTERS_REWARD_ITEM.length >= playerClass.getLevel() && ACbConfigManager.COMMUNITY_CLASS_MASTERS_REWARD_ITEM[playerClass.getLevel() - 1] > 0 && ACbConfigManager.COMMUNITY_MASTERS_REWARD_AMOUNT.length >= playerClass.getLevel() && ACbConfigManager.COMMUNITY_MASTERS_REWARD_AMOUNT[playerClass.getLevel() - 1] > 0L) {
            ItemFunctions.addItem(player, ACbConfigManager.COMMUNITY_CLASS_MASTERS_REWARD_ITEM[playerClass.getLevel() - 1], (long) ACbConfigManager.COMMUNITY_MASTERS_REWARD_AMOUNT[playerClass.getLevel() - 1], true);
        }
        player.sendPacket(new SystemMessage(1308));
        player.setClassId(RequestClass, false, false);
        player.broadcastUserInfo(true);
        player.sendPacket(new SocialAction(player.getObjectId(), SocialAction.VICTORY));
        player.broadcastPacket(new MagicSkillUse(player, player, 4339, 1, 0, 0L));
        return html(player);
    }
}
