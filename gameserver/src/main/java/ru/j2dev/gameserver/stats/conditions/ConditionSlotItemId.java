package ru.j2dev.gameserver.stats.conditions;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.Inventory;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.stats.Env;

public final class ConditionSlotItemId extends ConditionInventory {
    private final int _itemId;
    private final int _enchantLevel;

    public ConditionSlotItemId(final int slot, final int itemId, final int enchantLevel) {
        super(slot);
        _itemId = itemId;
        _enchantLevel = enchantLevel;
    }

    @Override
    protected boolean testImpl(final Env env) {
        if (!env.character.isPlayer()) {
            return false;
        }
        final Inventory inv = ((Player) env.character).getInventory();
        final ItemInstance item = inv.getPaperdollItem(_slot);
        if (item == null) {
            return _itemId == 0;
        }
        return item.getItemId() == _itemId && item.getEnchantLevel() >= _enchantLevel;
    }
}
