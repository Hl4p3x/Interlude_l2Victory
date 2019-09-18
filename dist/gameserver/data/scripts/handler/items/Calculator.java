package handler.items;

import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ShowCalc;

public class Calculator extends ScriptItemHandler {
    private static final int CALCULATOR = 4393;

    @Override
    public boolean useItem(final Playable playable, final ItemInstance item, final boolean ctrl) {
        if (!playable.isPlayer()) {
            return false;
        }
        playable.sendPacket(new ShowCalc(item.getItemId()));
        return true;
    }

    @Override
    public int[] getItemIds() {
        return new int[]{CALCULATOR};
    }
}
