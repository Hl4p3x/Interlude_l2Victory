package handler.items;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.scripts.Functions;

public class Special extends SimpleItemHandler {
    private static final int[] ITEM_IDS = {8060};

    @Override
    public int[] getItemIds() {
        return ITEM_IDS;
    }

    @Override
    protected boolean useItemImpl(final Player player, final ItemInstance item, final boolean ctrl) {
        final int itemId = item.getItemId();
        switch (itemId) {
            case 8060: {
                return use8060(player, ctrl);
            }
            default: {
                return false;
            }
        }
    }

    private boolean use8060(final Player player, final boolean ctrl) {
        if (Functions.removeItem(player, 8058, 1L) == 1L) {
            Functions.addItem(player, 8059, 1L);
            return true;
        }
        return false;
    }
}
