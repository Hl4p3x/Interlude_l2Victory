package services;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.dao.CharacterDAO;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.events.impl.SiegeEvent;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.Log;
import ru.j2dev.gameserver.utils.Util;

public class Rename extends Functions {
    public void rename_page() {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (!Config.SERVICES_CHANGE_NICK_ENABLED) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
            return;
        }
        final NpcHtmlMessage msg = new NpcHtmlMessage(5).setFile("scripts/services/rename_char.htm");
        msg.replace("%item_id%", String.valueOf(Config.SERVICES_CHANGE_NICK_ITEM));
        msg.replace("%item_count%", String.valueOf(Config.SERVICES_CHANGE_NICK_PRICE));
        player.sendPacket(msg);
    }

    public void rename(final String[] arg) {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (!Config.SERVICES_CHANGE_NICK_ENABLED) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
            return;
        }
        if (arg == null || arg.length < 1) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/rename_char_err01.htm"));
            return;
        }
        if (player.isHero() || player.isClanLeader()) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/rename_char_err03.htm"));
            return;
        }
        if (player.getEvent(SiegeEvent.class) != null) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/rename_char_err03.htm"));
            return;
        }
        final String name = arg[0];
        if (Util.isMatchingRegexp(name, Config.CNAME_FORBIDDEN_PATTERN) || CharacterDAO.getInstance().getObjectIdByName(name) > 0) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/rename_char_err02.htm"));
            return;
        }
        if (!Util.isMatchingRegexp(name, Config.CUSTOM_CNAME_TEMPLATE)) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/rename_char_err01.htm"));
            return;
        }
        if (ItemFunctions.getItemCount(player, Config.SERVICES_CHANGE_NICK_ITEM) < Config.SERVICES_CHANGE_NICK_PRICE) {
            if (Config.SERVICES_CHANGE_NICK_ITEM == 57) {
                player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
            } else {
                player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
            }
            return;
        }
        ItemFunctions.removeItem(player, Config.SERVICES_CHANGE_NICK_ITEM, (long) Config.SERVICES_CHANGE_NICK_PRICE, true);
        final String oldName = player.getName();
        player.reName(name, true);
        player.broadcastCharInfo();
        Log.add("Character " + oldName + " renamed to " + name, "renames");
        player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/rename_char_msg01.htm").replace("%old_name%", oldName).replace("%new_name%", name));
    }
}
