package services;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.olympiad.NoblessManager;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.scripts.Functions;

public class OlympiadPointsReset extends Functions {
    public void olympiad_point_sell_page() {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (!Config.SERVICES_OLY_POINTS_RESET) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
            return;
        }
        if (!player.isInPeaceZone()) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_peace_zone.htm"));
            return;
        }
        final NpcHtmlMessage msg = new NpcHtmlMessage(5).setFile("scripts/services/reset_olympiad_point_sell.htm");
        msg.replace("%item_id%", String.valueOf(Config.SERVICES_OLY_POINTS_RESET_ITEM_ID));
        msg.replace("%item_count%", String.valueOf(Config.SERVICES_OLY_POINTS_RESET_ITEM_AMOUNT));
        msg.replace("%reset_point_count%", String.valueOf(Config.SERVICES_OLY_POINTS_REWARD));
        player.sendPacket(msg);
    }

    public void olympiad_point_sell() {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (!Config.SERVICES_OLY_POINTS_RESET) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
            return;
        }
        if (!player.isNoble()) {
            show("You are mpt a noble", player);
            return;
        }
        if (player.isSubClassActive()) {
            show("You can reset points only on main class.", player);
            return;
        }
        if (NoblessManager.getInstance().getPointsOf(player.getObjectId()) >= Config.SERVICES_OLY_POINTS_THRESHOLD) {
            show("Your olympiad noble points count is to high.", player);
            return;
        }
        if (Functions.removeItem(player, Config.SERVICES_OLY_POINTS_RESET_ITEM_ID, (long) Config.SERVICES_OLY_POINTS_RESET_ITEM_AMOUNT) < Config.SERVICES_OLY_POINTS_RESET_ITEM_AMOUNT) {
            player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
            return;
        }
        NoblessManager.getInstance().setPointsOf(player.getObjectId(), Config.SERVICES_OLY_POINTS_REWARD);
        player.sendMessage("Olympiad points added.");
    }

    
}
