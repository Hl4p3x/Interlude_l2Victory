package ru.j2dev.gameserver.model.items;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.math.SafeMath;
import ru.j2dev.gameserver.dao.ItemsDAO;
import ru.j2dev.gameserver.idfactory.IdFactory;
import ru.j2dev.gameserver.utils.ItemFunctions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class ItemContainer {
    protected static final ItemsDAO _itemsDAO = ItemsDAO.getInstance();
    private static final Logger _log = LoggerFactory.getLogger(ItemContainer.class);

    protected final List<ItemInstance> _items = new CopyOnWriteArrayList<>();
    protected final ReadWriteLock lock = new ReentrantReadWriteLock();
    protected final Lock readLock = lock.readLock();
    protected final Lock writeLock = lock.writeLock();

    public int getSize() {
        return _items.size();
    }

    public List<ItemInstance> getItems() {
        readLock();
        try {
            return _items;
        } finally {
            readUnlock();
        }
    }

    public void clear() {
        writeLock();
        try {
            _items.clear();
        } finally {
            writeUnlock();
        }
    }

    public final void writeLock() {
        writeLock.lock();
    }

    public final void writeUnlock() {
        writeLock.unlock();
    }

    public final void readLock() {
        readLock.lock();
    }

    public final void readUnlock() {
        readLock.unlock();
    }

    public ItemInstance getItemByObjectId(final int objectId) {
        readLock();
        try {
            for (final ItemInstance item : _items) {
                if (item.getObjectId() == objectId) {
                    return item;
                }
            }
        } finally {
            readUnlock();
        }
        return null;
    }

    public ItemInstance getItemByItemId(final int itemId) {
        readLock();
        try {
            for (final ItemInstance item : _items) {
                if (item.getItemId() == itemId) {
                    return item;
                }
            }
        } finally {
            readUnlock();
        }
        return null;
    }

    public List<ItemInstance> getItemsByItemId(final int itemId) {
        final List<ItemInstance> result = new ArrayList<>();
        readLock();
        try {
            for (final ItemInstance item : _items) {
                if (item.getItemId() == itemId) {
                    result.add(item);
                }
            }
        } finally {
            readUnlock();
        }
        return result;
    }

    public long getCountOf(final int itemId) {
        long count = 0L;
        readLock();
        try {
            for (final ItemInstance item : _items) {
                if (item.getItemId() == itemId) {
                    count = SafeMath.addAndLimit(count, item.getCount());
                }
            }
        } finally {
            readUnlock();
        }
        return count;
    }

    public ItemInstance addItem(final int itemId, final long count) {
        if (count < 1L) {
            return null;
        }
        writeLock();
        ItemInstance item;
        try {
            item = getItemByItemId(itemId);
            if (item != null && item.isStackable()) {
                synchronized (item) {
                    item.setCount(SafeMath.addAndLimit(item.getCount(), count));
                    onModifyItem(item);
                }
            } else {
                item = ItemFunctions.createItem(itemId);
                item.setCount(count);
                _items.add(item);
                onAddItem(item);
            }
        } finally {
            writeUnlock();
        }
        return item;
    }

    public ItemInstance addItem(final ItemInstance item) {
        if (item == null) {
            return null;
        }
        if (item.getCount() < 1L) {
            return null;
        }
        ItemInstance result = null;
        writeLock();
        try {
            if (getItemByObjectId(item.getObjectId()) != null) {
                return null;
            }
            if (item.isStackable()) {
                final int itemId = item.getItemId();
                result = getItemByItemId(itemId);
                if (result != null) {
                    synchronized (result) {
                        result.setCount(SafeMath.addAndLimit(item.getCount(), result.getCount()));
                        onModifyItem(result);
                        onDestroyItem(item);
                    }
                }
            }
            if (result == null) {
                _items.add(item);
                result = item;
                onAddItem(result);
            }
        } finally {
            writeUnlock();
        }
        return result;
    }

    public ItemInstance removeItemByObjectId(final int objectId, final long count) {
        if (count < 1L) {
            return null;
        }
        writeLock();
        ItemInstance result;
        try {
            final ItemInstance item;
            if ((item = getItemByObjectId(objectId)) == null) {
                return null;
            }
            synchronized (item) {
                result = removeItem(item, count);
            }
        } finally {
            writeUnlock();
        }
        return result;
    }

    public ItemInstance removeItemByItemId(final int itemId, final long count) {
        if (count < 1L) {
            return null;
        }
        writeLock();
        ItemInstance result;
        try {
            final ItemInstance item;
            if ((item = getItemByItemId(itemId)) == null) {
                return null;
            }
            synchronized (item) {
                result = removeItem(item, count);
            }
        } finally {
            writeUnlock();
        }
        return result;
    }

    public ItemInstance removeItem(final ItemInstance item, final long count) {
        if (item == null) {
            return null;
        }
        if (count < 1L) {
            return null;
        }
        if (item.getCount() < count) {
            return null;
        }
        writeLock();
        try {
            if (!_items.contains(item)) {
                return null;
            }
            if (item.getCount() > count) {
                item.setCount(item.getCount() - count);
                onModifyItem(item);
                final ItemInstance newItem = new ItemInstance(IdFactory.getInstance().getNextId(), item.getItemId());
                newItem.setCount(count);
                return newItem;
            }
            return removeItem(item);
        } finally {
            writeUnlock();
        }
    }

    public ItemInstance removeItem(final ItemInstance item) {
        if (item == null) {
            return null;
        }
        writeLock();
        try {
            if (!_items.remove(item)) {
                return null;
            }
            onRemoveItem(item);
            return item;
        } finally {
            writeUnlock();
        }
    }

    public boolean destroyItemByObjectId(final int objectId, final long count) {
        writeLock();
        try {
            final ItemInstance item;
            if ((item = getItemByObjectId(objectId)) == null) {
                return false;
            }
            synchronized (item) {
                return destroyItem(item, count);
            }
        } finally {
            writeUnlock();
        }
    }

    public boolean destroyItemByItemId(final int itemId, final long count) {
        writeLock();
        try {
            final ItemInstance item;
            if ((item = getItemByItemId(itemId)) == null) {
                return false;
            }
            synchronized (item) {
                return destroyItem(item, count);
            }
        } finally {
            writeUnlock();
        }
    }

    public boolean destroyItem(final ItemInstance item, final long count) {
        if (item == null) {
            return false;
        }
        if (count < 1L) {
            return false;
        }
        if (item.getCount() < count) {
            return false;
        }
        writeLock();
        try {
            if (!_items.contains(item)) {
                return false;
            }
            if (item.getCount() > count) {
                item.setCount(item.getCount() - count);
                onModifyItem(item);
                return true;
            }
            return destroyItem(item);
        } finally {
            writeUnlock();
        }
    }

    public boolean destroyItem(final ItemInstance item) {
        if (item == null) {
            return false;
        }
        writeLock();
        try {
            if (!_items.remove(item)) {
                return false;
            }
            onRemoveItem(item);
            onDestroyItem(item);
            return true;
        } finally {
            writeUnlock();
        }
    }

    protected abstract void onAddItem(final ItemInstance p0);

    protected abstract void onModifyItem(final ItemInstance p0);

    protected abstract void onRemoveItem(final ItemInstance p0);

    protected abstract void onDestroyItem(final ItemInstance p0);
}
