package handler.items;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SSQStatus;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ShowXMasSeal;

public class Books extends SimpleItemHandler {
    private static final int[] ITEM_IDS = {5555, 5707};

    @Override
    public int[] getItemIds() {
        return ITEM_IDS;
    }

    @Override
    protected boolean useItemImpl(final Player player, final ItemInstance item, final boolean ctrl) {
        final int itemId = item.getItemId();
        switch (itemId) {
            case 5555: {
                player.sendPacket(new ShowXMasSeal(5555));
                break;
            }
            case 5707: {
                player.sendPacket(new SSQStatus(player, 1));
                break;
            }
            default: {
                return false;
            }
        }
        return true;
    }
}
