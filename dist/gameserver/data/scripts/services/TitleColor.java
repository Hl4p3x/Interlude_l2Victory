package services;

import org.apache.commons.lang3.StringUtils;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.utils.ItemFunctions;

public class TitleColor extends Functions {
    public void list() {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (!Config.SERVICES_CHANGE_TITLE_COLOR_ENABLED) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
            return;
        }
        if (StringUtils.isEmpty(player.getTitle())) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_title_empty.htm"));
            return;
        }
        final NpcHtmlMessage msg = new NpcHtmlMessage(5).setFile("scripts/services/change_title_color.htm");
        msg.replace("%item_id%", String.valueOf(Config.SERVICES_CHANGE_TITLE_COLOR_ITEM));
        msg.replace("%item_count%", String.valueOf(Config.SERVICES_CHANGE_TITLE_COLOR_PRICE));
        final StringBuilder sb = new StringBuilder();
        for (final String color : Config.SERVICES_CHANGE_TITLE_COLOR_LIST) {
            sb.append("<br><a action=\"bypass -h scripts_services.TitleColor:change ").append(color).append("\"><font color=\"").append(color.substring(4, 6)).append(color.substring(2, 4)).append(color.substring(0, 2)).append("\">").append(player.getTitle()).append("</font></a>");
        }
        msg.replace("%list%", sb.toString());
        player.sendPacket(msg);
    }

    public void change(final String[] param) {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (param == null || param.length < 1) {
            return;
        }
        if (!Config.SERVICES_CHANGE_TITLE_COLOR_ENABLED) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
            return;
        }
        if (StringUtils.isEmpty(player.getTitle())) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_title_empty.htm"));
            return;
        }
        if ("FFFF77".equalsIgnoreCase(param[0])) {
            player.setTitleColor(Integer.decode("0xFFFF77"));
            player.broadcastUserInfo(true);
            return;
        }
        if (ItemFunctions.removeItem(player, Config.SERVICES_CHANGE_TITLE_COLOR_ITEM, (long) Config.SERVICES_CHANGE_TITLE_COLOR_PRICE, true) >= Config.SERVICES_CHANGE_TITLE_COLOR_PRICE) {
            player.setTitleColor(Integer.decode("0x" + param[0]));
            player.broadcastUserInfo(true);
        } else if (Config.SERVICES_CHANGE_TITLE_COLOR_ITEM == 57) {
            player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
        } else {
            player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
        }
    }
}
