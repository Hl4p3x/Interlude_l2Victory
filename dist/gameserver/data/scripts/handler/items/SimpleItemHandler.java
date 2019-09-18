package handler.items;

import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;

public abstract class SimpleItemHandler extends ScriptItemHandler {
    public static boolean useItem(final Player player, final ItemInstance item, final long count) {
        if (player.getInventory().destroyItem(item, count)) {
            player.sendPacket(new SystemMessage(47).addItemName(item.getItemId()));
            return true;
        }
        player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
        return false;
    }

    @Override
    public boolean useItem(final Playable playable, final ItemInstance item, final boolean ctrl) {
        Player player;
        if (playable.isPlayer()) {
            player = (Player) playable;
        } else {
            if (!playable.isPet()) {
                return false;
            }
            player = playable.getPlayer();
        }
        if (player.isInFlyingTransform()) {
            player.sendPacket(new SystemMessage(113).addItemName(item.getItemId()));
            return false;
        }
        return useItemImpl(player, item, ctrl);
    }

    protected abstract boolean useItemImpl(final Player p0, final ItemInstance p1, final boolean p2);
}
