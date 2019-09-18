package ru.j2dev.gameserver.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.items.ItemInfo;
import ru.j2dev.gameserver.model.items.ItemInstance;

import java.util.concurrent.TimeUnit;

public class ItemInfoCache {
    private static final ItemInfoCache _instance = new ItemInfoCache();

    private final Cache<Integer, ItemInfo> cache = Caffeine.newBuilder().maximumSize(10000).expireAfterWrite(1, TimeUnit.HOURS).build();

    public static ItemInfoCache getInstance() {
        return _instance;
    }

    public void put(final ItemInstance item) {
        cache.put(item.getObjectId(), new ItemInfo(item));
    }

    public ItemInfo get(final int objectId) {
        final ItemInfo element = cache.getIfPresent(objectId);
        ItemInfo info = null;
        if (element != null) {
            info = element;
        }
        Player player;
        if (info != null) {
            player = World.getPlayer(info.getOwnerId());
            ItemInstance item = null;
            if (player != null) {
                item = player.getInventory().getItemByObjectId(objectId);
            }
            if (item != null && item.getItemId() == info.getItemId()) {
                cache.put(item.getObjectId(), info = new ItemInfo(item));
            }
        }
        return info;
    }
}
