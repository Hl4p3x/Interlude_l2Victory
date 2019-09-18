package ru.j2dev.gameserver.phantoms.model;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.items.PcInventory;

public class PhantomInventory extends PcInventory {
    public PhantomInventory(final Player owner) {
        super(owner);
    }

    @Override
    public void store() {
    }

    @Override
    public void restore() {
    }

    @Override
    protected void onRestoreItem(final ItemInstance item) {
        _totalWeight += (int) (item.getTemplate().getWeight() * item.getCount());
    }

    @Override
    protected void onAddItem(final ItemInstance item) {
        item.setOwnerId(getOwnerId());
        item.setLocation(getBaseLocation());
        item.setLocData(findSlot());
    }

    @Override
    protected void onModifyItem(final ItemInstance item) {
    }

    @Override
    protected void onRemoveItem(final ItemInstance item) {
        if (item.isEquipped()) {
            unEquipItem(item);
        }
        item.setLocData(-1);
        refreshWeight();
    }

    @Override
    protected void onDestroyItem(final ItemInstance item) {
        item.setCount(0L);
        item.delete();
    }

    @Override
    protected void onEquip(final int slot, final ItemInstance item) {
        item.setLocation(getEquipLocation());
        item.setLocData(slot);
        _wearedMask |= item.getTemplate().getItemMask();
    }

    @Override
    protected void onUnequip(final int slot, final ItemInstance item) {
        item.setLocation(getBaseLocation());
        item.setLocData(findSlot());
        item.setEquipped(false);
        item.setChargedSpiritshot(0);
        item.setChargedSoulshot(0);
        _wearedMask &= ~item.getTemplate().getItemMask();
    }

    private int findSlot() {
        int slot = 0;
        slot = 0;
        Label_0004:
        while (slot < _items.size()) {
            for (final ItemInstance item : _items) {
                if (!item.isEquipped()) {
                    if (!item.getTemplate().isQuest()) {
                        if (item.getEquipSlot() == slot) {
                            ++slot;
                            continue Label_0004;
                        }
                    }
                }
            }
            break;
        }
        return slot;
    }
}
