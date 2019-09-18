package handler.items;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.scripts.Functions;

public class ClanReputation extends SimpleItemHandler {
    private static final int[] ITEM_IDS = {Config.ITEM_CLAN_REPUTATION_ID};

    @Override
    protected boolean useItemImpl(final Player player, final ItemInstance item, final boolean ctrl) {
        if (player == null) {
            return false;
        }
        if (!Config.ITEM_CLAN_REPUTATION_ENABLE) {
            player.sendMessage("Item clan reputation disabled");
            return false;
        }
        final Clan clan = player.getClan();
        if (clan == null) {
            player.sendMessage("Get clan first.");
            return false;
        }
        Functions.removeItem(player, Config.ITEM_CLAN_REPUTATION_ID, 1L);
        clan.incReputation(Config.ITEM_CLAN_REPUTATION_AMOUNT, true, "ClanReputationItemAdd");
        player.sendPacket(new SystemMessage(1777).addNumber(Config.ITEM_CLAN_REPUTATION_AMOUNT));
        return true;
    }

    @Override
    public int[] getItemIds() {
        return ITEM_IDS;
    }
}
