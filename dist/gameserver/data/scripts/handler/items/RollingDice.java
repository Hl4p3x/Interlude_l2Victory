package handler.items;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.Dice;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;

public class RollingDice extends ScriptItemHandler {
    private static final int[] _itemIds = {4625, 4626, 4627, 4628};

    @Override
    public boolean useItem(final Playable playable, final ItemInstance item, final boolean ctrl) {
        if (playable == null || !playable.isPlayer()) {
            return false;
        }
        final Player player = (Player) playable;
        final int itemId = item.getItemId();
        if (player.isOlyParticipant()) {
            player.sendPacket(Msg.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
            return false;
        }
        if (player.isSitting()) {
            player.sendPacket(Msg.YOU_CANNOT_MOVE_WHILE_SITTING);
            return false;
        }
        final int number = Rnd.get(1, 6);
        if (number == 0) {
            player.sendPacket(Msg.YOU_MAY_NOT_THROW_THE_DICE_AT_THIS_TIMETRY_AGAIN_LATER);
            return false;
        }
        player.broadcastPacket(new Dice(player.getObjectId(), itemId, number, player.getX() - 30, player.getY() - 30, player.getZ()), new SystemMessage(834).addString(player.getName()).addNumber(number));
        return true;
    }

    @Override
    public final int[] getItemIds() {
        return _itemIds;
    }
}
