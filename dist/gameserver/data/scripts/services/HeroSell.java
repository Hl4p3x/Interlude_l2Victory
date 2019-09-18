package services;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SocialAction;
import ru.j2dev.gameserver.scripts.Functions;

public class HeroSell extends Functions {
    private static boolean makeCustomHero(final Player player, final long customHeroDuration) {
        if (player.isHero() || customHeroDuration <= 0L) {
            return false;
        }
        player.setCustomHero(true, customHeroDuration, true);
        player.broadcastPacket(new SocialAction(player.getObjectId(), 16));
        player.broadcastUserInfo(true);
        return true;
    }

    public void hero_sell_page() {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (!Config.SERVICES_HERO_SELL_ENABLED) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
            return;
        }
        if (!player.isInPeaceZone()) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_peace_zone.htm"));
            return;
        }
        final NpcHtmlMessage msg = new NpcHtmlMessage(5).setFile("scripts/services/hero_sell.htm");
        msg.replace("%duration_time%", String.valueOf(Config.SERVICE_HERO_STATUS_DURATION / 86400L));
        msg.replace("%item_id%", String.valueOf(Config.SERVICES_HERO_SELLER_ITEM_ID));
        msg.replace("%item_count%", String.valueOf(Config.SERVICES_HERO_SELLER_ITEM_COUNT));
        player.sendPacket(msg);
    }

    public void hero_sell() {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (!Config.SERVICES_HERO_SELL_ENABLED) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
            return;
        }
        if (player.isHero()) {
            show("You are already Hero.", player);
            return;
        }
        if (!player.isNoble()) {
            show("You are not a Nobles, only Nobles can become a Hero.", player);
            return;
        }
        if (player.isSubClassActive()) {
            show("You can get Hero status only on main class.", player);
            return;
        }
        if (Functions.removeItem(player, Config.SERVICES_HERO_SELLER_ITEM_ID, Config.SERVICES_HERO_SELLER_ITEM_COUNT) < Config.SERVICES_HERO_SELLER_ITEM_COUNT) {
            player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
            return;
        }
        makeCustomHero(player, Config.SERVICE_HERO_STATUS_DURATION);
    }
}
