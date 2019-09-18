package ru.j2dev.gameserver.model.items.listeners;

import ru.j2dev.gameserver.listener.inventory.OnEquipListener;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.items.ItemInstance;

public final class StatsListener implements OnEquipListener {
    private static final StatsListener _instance = new StatsListener();

    public static StatsListener getInstance() {
        return _instance;
    }

    @Override
    public void onUnequip(final int slot, final ItemInstance item, final Playable actor) {
        actor.removeStatsOwner(item);
        actor.updateStats();
    }

    @Override
    public void onEquip(final int slot, final ItemInstance item, final Playable actor) {
        actor.addStatFuncs(item.getStatFuncs());
        actor.updateStats();
    }
}
