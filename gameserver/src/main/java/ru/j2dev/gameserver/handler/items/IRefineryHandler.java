package ru.j2dev.gameserver.handler.items;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;

public interface IRefineryHandler {
    void onInitRefinery(final Player p0);

    void onPutTargetItem(final Player p0, final ItemInstance p1);

    void onPutMineralItem(final Player p0, final ItemInstance p1, final ItemInstance p2);

    void onPutGemstoneItem(final Player p0, final ItemInstance p1, final ItemInstance p2, final ItemInstance p3, final long p4);

    void onRequestRefine(final Player p0, final ItemInstance p1, final ItemInstance p2, final ItemInstance p3, final long p4);

    void onInitRefineryCancel(final Player p0);

    void onPutTargetCancelItem(final Player p0, final ItemInstance p1);

    void onRequestCancelRefine(final Player p0, final ItemInstance p1);
}
