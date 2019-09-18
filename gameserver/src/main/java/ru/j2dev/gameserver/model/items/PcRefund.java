package ru.j2dev.gameserver.model.items;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance.ItemLocation;

public class PcRefund extends ItemContainer {
    public PcRefund(final Player player) {
    }

    @Override
    protected void onAddItem(final ItemInstance item) {
        item.setLocation(ItemLocation.VOID);
        item.save();
        if (_items.size() > 12) {
            destroyItem(_items.remove(0));
        }
    }

    @Override
    protected void onModifyItem(final ItemInstance item) {
        item.save();
    }

    @Override
    protected void onRemoveItem(final ItemInstance item) {
        item.save();
    }

    @Override
    protected void onDestroyItem(final ItemInstance item) {
        item.setCount(0L);
        item.delete();
    }

    @Override
    public void clear() {
        writeLock();
        try {
            PcRefund._itemsDAO.delete(_items);
            _items.clear();
        } finally {
            writeUnlock();
        }
    }
}
