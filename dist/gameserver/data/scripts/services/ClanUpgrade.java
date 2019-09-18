package services;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.scripts.Functions;

public class ClanUpgrade extends Functions {
    public void clan_upgrade_page() {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (!Config.SERVICES_CLANLEVEL_SELL_ENABLED) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
            return;
        }
        if (!player.isInPeaceZone()) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_peace_zone.htm"));
            return;
        }
        final Clan clan = player.getClan();
        if (clan == null) {
            player.sendPacket(Msg.YOU_ARE_NOT_A_CLAN_MEMBER);
            return;
        }
        if (clan.getLeaderId() != player.getObjectId()) {
            player.sendPacket(Msg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
            return;
        }
        if (clan.getLevel() < 1 || clan.getLevel() >= Config.SERVICES_CLAN_MAX_SELL_LEVEL) {
            player.sendMessage("Clan level to high or to low.");
            return;
        }
        final NpcHtmlMessage msg = new NpcHtmlMessage(5).setFile("scripts/services/clan_upgrade.htm");
        msg.replace("%item_id%", String.valueOf(Config.SERVICES_CLANLEVEL_SELL_ITEM[clan.getLevel() - 1]));
        msg.replace("%item_count%", String.valueOf(Config.SERVICES_CLANLEVEL_SELL_PRICE[clan.getLevel() - 1]));
        msg.replace("%clan_level_next%", String.valueOf(clan.getLevel() + 1));
        player.sendPacket(msg);
    }

    public void clan_upgrade() {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (!Config.SERVICES_CLANLEVEL_SELL_ENABLED) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
            return;
        }
        /*if (!player.isInPeaceZone()) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_peace_zone.htm"));
            return;
        }*/
        final Clan clan = player.getClan();
        if (clan == null) {
            player.sendMessage("Get clan first.");
            return;
        }
        if (clan.getLeaderId() != player.getObjectId()) {
            player.sendMessage("Only clan leader can do that.");
            return;
        }
        if (clan.getLevel() < 1 || clan.getLevel() >= Config.SERVICES_CLAN_MAX_SELL_LEVEL) {
            player.sendMessage("Clan level to high or to low.");
            return;
        }
        final int toLvl = clan.getLevel() + 1;
        final int requiredItemId = Config.SERVICES_CLANLEVEL_SELL_ITEM[Math.min(Math.max(0, toLvl - 2), Config.SERVICES_CLANLEVEL_SELL_ITEM.length - 1)];
        final long requiredItemCount = Config.SERVICES_CLANLEVEL_SELL_PRICE[Math.min(Math.max(0, toLvl - 2), Config.SERVICES_CLANLEVEL_SELL_PRICE.length - 1)];
        if (Functions.getItemCount(player, requiredItemId) < requiredItemCount) {
            player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
            return;
        }
        Functions.removeItem(player, requiredItemId, requiredItemCount);
        clan.setLevel(clan.getLevel() + 1);
        clan.updateClanInDB();
        clan.broadcastClanStatus(true, true, true);
        player.sendMessage("Congratulation! Clan level up!.");
    }
}
