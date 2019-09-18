package services;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.scripts.Functions;

public class ClanReputationSell extends Functions {
    public void clan_reputation_page() {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (!Config.SERVICES_CLAN_REPUTATION_ENABLE) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
            return;
        }
        if (!player.isInPeaceZone()) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_peace_zone.htm"));
            return;
        }
        final Clan clan = player.getClan();
        if (clan == null) {
            player.sendMessage("Get clan first.");
            return;
        }
        final NpcHtmlMessage msg = new NpcHtmlMessage(5).setFile("scripts/services/clan_reputation_sell.htm");
        msg.replace("%item_id%", String.valueOf(Config.SERVICES_CLAN_REPUTATION_ITEM_ID));
        msg.replace("%item_count%", String.valueOf(Config.SERVICES_CLAN_REPUTATION_ITEM_COUNT));
        msg.replace("%reputation_amount%", String.valueOf(Config.SERVICES_CLAN_REPUTATION_AMOUNT));
        player.sendPacket(msg);
    }

    public void clan_reputation_up() {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (!Config.SERVICES_CLAN_REPUTATION_ENABLE) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
            return;
        }
        if (!player.isInPeaceZone()) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_peace_zone.htm"));
            return;
        }
        final Clan clan = player.getClan();
        if (clan == null) {
            player.sendMessage("Get clan first.");
            return;
        }
        if (getItemCount(player, Config.SERVICES_CLAN_REPUTATION_ITEM_ID) < Config.SERVICES_CLAN_REPUTATION_ITEM_COUNT) {
            player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
            return;
        }
        removeItem(player, Config.SERVICES_CLAN_REPUTATION_ITEM_ID, (long) Config.SERVICES_CLAN_REPUTATION_ITEM_COUNT);
        clan.incReputation(Config.SERVICES_CLAN_REPUTATION_AMOUNT, true, "ClanReputationServicesAdd");
        player.sendPacket(new SystemMessage(1777).addNumber(Config.SERVICES_CLAN_REPUTATION_AMOUNT));
    }
}
