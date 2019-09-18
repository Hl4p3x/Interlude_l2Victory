package handler.items;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.reward.RewardData;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.tables.FishTable;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.Util;

import java.util.List;

public class FishItem extends ScriptItemHandler {
    @Override
    public boolean useItem(final Playable playable, final ItemInstance item, final boolean ctrl) {
        if (playable == null || !playable.isPlayer()) {
            return false;
        }
        final Player player = (Player) playable;
        if (player.getWeightPenalty() >= 3 || player.getInventory().getSize() > player.getInventoryLimit() - 10) {
            player.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
            return false;
        }
        if (!player.getInventory().destroyItem(item, 1L)) {
            player.sendActionFailed();
            return false;
        }
        int count = 0;
        final List<RewardData> rewards = FishTable.getInstance().getFishReward(item.getItemId());
        for (final RewardData d : rewards) {
            final long roll = Util.rollDrop(d.getMinDrop(), d.getMaxDrop(), d.getChance() * Config.RATE_FISH_DROP_COUNT * Config.RATE_DROP_ITEMS * player.getRateItems(), false);
            if (roll > 0L) {
                ItemFunctions.addItem(player, d.getItemId(), roll, true);
                ++count;
            }
        }
        if (count == 0) {
            player.sendPacket(SystemMsg.THERE_WAS_NOTHING_FOUND_INSIDE);
        }
        return true;
    }

    @Override
    public int[] getItemIds() {
        return FishTable.getInstance().getFishIds();
    }
}
