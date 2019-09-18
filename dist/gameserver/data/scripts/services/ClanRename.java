package services;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.events.impl.SiegeEvent;
import ru.j2dev.gameserver.model.pledge.SubUnit;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.tables.ClanTable;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.Util;

public class ClanRename extends Functions {
    public void rename_clan_page() {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (!Config.SERVICES_CHANGE_CLAN_NAME_ENABLED) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
            return;
        }
        if (player.getClan() == null || !player.isClanLeader()) {
            player.sendPacket(new SystemMessage(SystemMsg.S1_IS_NOT_A_CLAN_LEADER).addName(player));
            return;
        }
        final NpcHtmlMessage msg = new NpcHtmlMessage(5).setFile("scripts/services/rename_clan.htm");
        msg.replace("%item_id%", String.valueOf(Config.SERVICES_CHANGE_CLAN_NAME_ITEM));
        msg.replace("%item_count%", String.valueOf(Config.SERVICES_CHANGE_CLAN_NAME_PRICE));
        player.sendPacket(msg);
    }

    public void rename_clan(final String[] arg) {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (arg == null || arg.length < 1) {
            return;
        }
        if (!Config.SERVICES_CHANGE_CLAN_NAME_ENABLED) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
            return;
        }
        if (player.getClan() == null || !player.isClanLeader()) {
            player.sendPacket(new SystemMessage(SystemMsg.S1_IS_NOT_A_CLAN_LEADER).addName(player));
            return;
        }
        if (ClanTable.getInstance().getClanByName(arg[0]) != null) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/rename_clan_err02.htm"));
            return;
        }
        if (!Util.isMatchingRegexp(arg[0], Config.CLAN_NAME_TEMPLATE)) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/rename_clan_err01.htm"));
            return;
        }
        if (player.getEvent(SiegeEvent.class) != null) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/rename_clan_err03.htm"));
            return;
        }
        if (ItemFunctions.getItemCount(player, Config.SERVICES_CHANGE_CLAN_NAME_ITEM) < Config.SERVICES_CHANGE_CLAN_NAME_PRICE) {
            if (Config.SERVICES_CHANGE_CLAN_NAME_ITEM == 57) {
                player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
            } else {
                player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
            }
            return;
        }
        ItemFunctions.removeItem(player, Config.SERVICES_CHANGE_CLAN_NAME_ITEM, (long) Config.SERVICES_CHANGE_CLAN_NAME_PRICE, true);
        final String name = arg[0];
        final SubUnit sub = player.getClan().getSubUnit(0);
        final String oldName = sub.getName();
        sub.setName(name, true);
        player.getClan().broadcastClanStatus(true, true, false);
        player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/rename_clan_msg01.htm").replace("%old_name%", oldName).replace("%new_name%", name));
    }
}
