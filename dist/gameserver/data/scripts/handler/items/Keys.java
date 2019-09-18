package handler.items;

import gnu.trove.set.hash.TIntHashSet;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.DoorHolder;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.DoorInstance;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;
import ru.j2dev.gameserver.templates.DoorTemplate;

public class Keys extends ScriptItemHandler {
    private int[] _itemIds;

    public Keys() {
        _itemIds = null;
        final TIntHashSet keys = new TIntHashSet();
        for (final DoorTemplate door : DoorHolder.getInstance().getDoors().values()) {
            if (door != null && door.getKey() > 0) {
                keys.add(door.getKey());
            }
        }
        _itemIds = keys.toArray();
    }

    @Override
    public boolean useItem(final Playable playable, final ItemInstance item, final boolean ctrl) {
        if (playable == null || !playable.isPlayer()) {
            return false;
        }
        final Player player = playable.getPlayer();
        final GameObject target = player.getTarget();
        if (target == null || !target.isDoor()) {
            player.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
            return false;
        }
        final DoorInstance door = (DoorInstance) target;
        if (door.isOpen()) {
            player.sendPacket(Msg.IT_IS_NOT_LOCKED);
            return false;
        }
        if (door.getKey() <= 0 || item.getItemId() != door.getKey()) {
            player.sendPacket(Msg.YOU_ARE_UNABLE_TO_UNLOCK_THE_DOOR);
            return false;
        }
        if (player.getDistance(door) > 300.0) {
            player.sendPacket(Msg.YOU_CANNOT_CONTROL_BECAUSE_YOU_ARE_TOO_FAR);
            return false;
        }
        if (!player.getInventory().destroyItem(item, 1L)) {
            player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
            return false;
        }
        player.sendPacket(SystemMessage2.removeItems(item.getItemId(), 1L));
        player.sendMessage(new CustomMessage("l2p.gameserver.skills.skillclasses.Unlock.Success", player));
        door.openMe(player, true);
        return true;
    }

    @Override
    public int[] getItemIds() {
        return _itemIds;
    }
}
