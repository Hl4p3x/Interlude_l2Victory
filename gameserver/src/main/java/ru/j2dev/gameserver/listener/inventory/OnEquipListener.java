package ru.j2dev.gameserver.listener.inventory;

import ru.j2dev.commons.listener.Listener;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.items.ItemInstance;

public interface OnEquipListener extends Listener<Playable> {
    void onEquip(final int p0, final ItemInstance p1, final Playable p2);

    void onUnequip(final int p0, final ItemInstance p1, final Playable p2);
}
