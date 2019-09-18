package ru.j2dev.gameserver.handler.items;

import org.apache.commons.lang3.ArrayUtils;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.Log;
import ru.j2dev.gameserver.utils.Log.ItemLog;

public interface IItemHandler {
    IItemHandler NULL = new IItemHandler() {
        @Override
        public boolean useItem(final Playable playable, final ItemInstance item, final boolean ctrl) {
            return false;
        }

        @Override
        public void dropItem(final Player player, ItemInstance item, final long count, final Location loc) {
            if (item.isEquipped()) {
                player.getInventory().unEquipItem(item);
                player.sendUserInfo(true);
            }
            item = player.getInventory().removeItemByObjectId(item.getObjectId(), count);
            if (item == null) {
                player.sendActionFailed();
                return;
            }
            Log.LogItem(player, ItemLog.Drop, item);
            item.dropToTheGround(player, loc);
            player.disableDrop(1000);
            player.sendChanges();
        }

        @Override
        public boolean pickupItem(final Playable playable, final ItemInstance item) {
            return true;
        }

        @Override
        public int[] getItemIds() {
            return ArrayUtils.EMPTY_INT_ARRAY;
        }
    };

    boolean useItem(final Playable p0, final ItemInstance p1, final boolean p2);

    void dropItem(final Player p0, final ItemInstance p1, final long p2, final Location p3);

    boolean pickupItem(final Playable p0, final ItemInstance p1);

    int[] getItemIds();
}
