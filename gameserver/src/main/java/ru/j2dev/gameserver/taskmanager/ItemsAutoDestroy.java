package ru.j2dev.gameserver.taskmanager;

import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.items.ItemInstance.ItemLocation;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ItemsAutoDestroy {
    private static ItemsAutoDestroy _instance;
    private ConcurrentLinkedQueue<ItemInstance> _items;
    private ConcurrentLinkedQueue<ItemInstance> _herbs;

    private ItemsAutoDestroy() {
        _items = null;
        _herbs = null;
        _herbs = new ConcurrentLinkedQueue<>();
        if (Config.AUTODESTROY_ITEM_AFTER > 0) {
            _items = new ConcurrentLinkedQueue<>();
            ThreadPoolManager.getInstance().scheduleAtFixedRate(new CheckItemsForDestroy(), 60000L, 60000L);
        }
        ThreadPoolManager.getInstance().scheduleAtFixedRate(new CheckHerbsForDestroy(), 1000L, 1000L);
    }

    public static ItemsAutoDestroy getInstance() {
        if (_instance == null) {
            _instance = new ItemsAutoDestroy();
        }
        return _instance;
    }

    public void addItem(final ItemInstance item) {
        item.setDropTime(System.currentTimeMillis());
        _items.add(item);
    }

    public void addHerb(final ItemInstance herb) {
        herb.setDropTime(System.currentTimeMillis());
        _herbs.add(herb);
    }

    public class CheckItemsForDestroy extends RunnableImpl {
        @Override
        public void runImpl() {
            final long _sleep = Config.AUTODESTROY_ITEM_AFTER * 1000L;
            final long curtime = System.currentTimeMillis();
            for (final ItemInstance item : _items) {
                if (item == null || item.getLastDropTime() == 0L || item.getLocation() != ItemLocation.VOID) {
                    _items.remove(item);
                } else {
                    if (item.getLastDropTime() + _sleep >= curtime) {
                        continue;
                    }
                    item.deleteMe();
                    item.delete();
                    _items.remove(item);
                }
            }
        }
    }

    public class CheckHerbsForDestroy extends RunnableImpl {
        static final long _sleep = 60000L;

        @Override
        public void runImpl() {
            final long curtime = System.currentTimeMillis();
            for (final ItemInstance item : _herbs) {
                if (item == null || item.getLastDropTime() == 0L || item.getLocation() != ItemLocation.VOID) {
                    _herbs.remove(item);
                } else {
                    if (item.getLastDropTime() + 60000L >= curtime) {
                        continue;
                    }
                    item.deleteMe();
                    _herbs.remove(item);
                }
            }
        }
    }
}
