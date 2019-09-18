package ru.j2dev.gameserver.model.items;

import ru.j2dev.gameserver.dao.ItemsDAO;
import ru.j2dev.gameserver.model.items.ItemInstance.ItemLocation;
import ru.j2dev.gameserver.templates.item.ItemTemplate.ItemClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public abstract class Warehouse extends ItemContainer {
    private static final ItemsDAO _itemsDAO = ItemsDAO.getInstance();

    protected final int _ownerId;

    protected Warehouse(final int ownerId) {
        _ownerId = ownerId;
    }

    public int getOwnerId() {
        return _ownerId;
    }

    public abstract ItemLocation getItemLocation();

    public List<ItemInstance> getItems(final ItemClass itemClass) {
        final List<ItemInstance> result = new ArrayList<>();
        readLock();
        try {
            for (final ItemInstance item : _items) {
                if (itemClass == null || itemClass == ItemClass.ALL || item.getItemClass() == itemClass) {
                    result.add(item);
                }
            }
        } finally {
            readUnlock();
        }
        return result;
    }

    public long getCountOfAdena() {
        return getCountOf(57);
    }

    @Override
    protected void onAddItem(final ItemInstance item) {
        item.setOwnerId(getOwnerId());
        item.setLocation(getItemLocation());
        item.setLocData(0);
        item.save();
    }

    @Override
    protected void onModifyItem(final ItemInstance item) {
        item.save();
    }

    @Override
    protected void onRemoveItem(final ItemInstance item) {
        item.setLocData(-1);
        item.save();
    }

    @Override
    protected void onDestroyItem(final ItemInstance item) {
        item.setCount(0L);
        item.delete();
    }

    public void restore() {
        final int ownerId = getOwnerId();
        writeLock();
        try {
            final Collection<ItemInstance> items = _itemsDAO.loadItemsByOwnerIdAndLoc(ownerId, getItemLocation());
            _items.addAll(items);
        } finally {
            writeUnlock();
        }
    }

    public enum WarehouseType {
        NONE,
        PRIVATE,
        CLAN,
        CASTLE,
        FREIGHT
    }

    public static class ItemClassComparator implements Comparator<ItemInstance> {
        private static final Comparator<ItemInstance> instance = new ItemClassComparator();

        public static Comparator<ItemInstance> getInstance() {
            return instance;
        }

        @Override
        public int compare(final ItemInstance o1, final ItemInstance o2) {
            if (o1 == null || o2 == null) {
                return 0;
            }
            int diff = o1.getItemClass().ordinal() - o2.getItemClass().ordinal();
            if (diff == 0) {
                diff = o1.getCrystalType().ordinal() - o2.getCrystalType().ordinal();
            }
            if (diff == 0) {
                diff = o1.getItemId() - o2.getItemId();
            }
            if (diff == 0) {
                diff = o1.getEnchantLevel() - o2.getEnchantLevel();
            }
            return diff;
        }
    }
}
