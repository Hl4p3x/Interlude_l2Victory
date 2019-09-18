package handler.items;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.MagicSkillUse;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.tables.SkillTable;

public class Potions extends SimpleItemHandler {
    private static final int[] ITEM_IDS = {7906, 7907, 7908, 7909, 7910, 7911};

    @Override
    public int[] getItemIds() {
        return ITEM_IDS;
    }

    @Override
    protected boolean useItemImpl(final Player player, final ItemInstance item, final boolean ctrl) {
        final int itemId = item.getItemId();
        if (player.isOlyParticipant()) {
            player.sendPacket(new SystemMessage(113).addItemName(itemId));
            return false;
        }
        if (!SimpleItemHandler.useItem(player, item, 1L)) {
            return false;
        }
        switch (itemId) {
            case 7906: {
                player.broadcastPacket(new MagicSkillUse(player, player, 2248, 1, 0, 0L));
                player.altOnMagicUseTimer(player, SkillTable.getInstance().getInfo(2248, 1));
                break;
            }
            case 7907: {
                player.broadcastPacket(new MagicSkillUse(player, player, 2249, 1, 0, 0L));
                player.altOnMagicUseTimer(player, SkillTable.getInstance().getInfo(2249, 1));
                break;
            }
            case 7908: {
                player.broadcastPacket(new MagicSkillUse(player, player, 2250, 1, 0, 0L));
                player.altOnMagicUseTimer(player, SkillTable.getInstance().getInfo(2250, 1));
                break;
            }
            case 7909: {
                player.broadcastPacket(new MagicSkillUse(player, player, 2251, 1, 0, 0L));
                player.altOnMagicUseTimer(player, SkillTable.getInstance().getInfo(2251, 1));
                break;
            }
            case 7910: {
                player.broadcastPacket(new MagicSkillUse(player, player, 2252, 1, 0, 0L));
                player.altOnMagicUseTimer(player, SkillTable.getInstance().getInfo(2252, 1));
                break;
            }
            case 7911: {
                player.broadcastPacket(new MagicSkillUse(player, player, 2253, 1, 0, 0L));
                player.altOnMagicUseTimer(player, SkillTable.getInstance().getInfo(2253, 1));
                break;
            }
            default: {
                return false;
            }
        }
        return true;
    }
}
