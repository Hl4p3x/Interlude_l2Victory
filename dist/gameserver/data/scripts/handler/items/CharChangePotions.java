package handler.items;

import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.MagicSkillUse;

public class CharChangePotions extends ScriptItemHandler {
    private static final int[] _itemIds = {5235, 5236, 5237, 5238, 5239, 5240, 5241, 5242, 5243, 5244, 5245, 5246, 5247, 5248};

    @Override
    public boolean useItem(final Playable playable, final ItemInstance item, final boolean ctrl) {
        if (playable == null || !playable.isPlayer()) {
            return false;
        }
        final Player player = (Player) playable;
        final int itemId = item.getItemId();
        if (!player.getInventory().destroyItem(item, 1L)) {
            player.sendActionFailed();
            return false;
        }
        switch (itemId) {
            case 5235: {
                player.setFace(0);
                break;
            }
            case 5236: {
                player.setFace(1);
                break;
            }
            case 5237: {
                player.setFace(2);
                break;
            }
            case 5238: {
                player.setHairColor(0);
                break;
            }
            case 5239: {
                player.setHairColor(1);
                break;
            }
            case 5240: {
                player.setHairColor(2);
                break;
            }
            case 5241: {
                player.setHairColor(3);
                break;
            }
            case 5242: {
                player.setHairStyle(0);
                break;
            }
            case 5243: {
                player.setHairStyle(1);
                break;
            }
            case 5244: {
                player.setHairStyle(2);
                break;
            }
            case 5245: {
                player.setHairStyle(3);
                break;
            }
            case 5246: {
                player.setHairStyle(4);
                break;
            }
            case 5247: {
                player.setHairStyle(5);
                break;
            }
            case 5248: {
                player.setHairStyle(6);
                break;
            }
        }
        player.broadcastPacket(new MagicSkillUse(player, player, 2003, 1, 1, 0L));
        player.broadcastCharInfo();
        return true;
    }

    @Override
    public final int[] getItemIds() {
        return _itemIds;
    }
}
