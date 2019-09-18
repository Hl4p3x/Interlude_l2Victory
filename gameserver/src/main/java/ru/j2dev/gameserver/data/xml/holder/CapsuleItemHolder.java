package ru.j2dev.gameserver.data.xml.holder;

import org.apache.commons.lang3.tuple.Pair;
import ru.j2dev.commons.collections.IntMap;
import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.commons.util.RandomUtils;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.handler.items.IItemHandler;
import ru.j2dev.gameserver.handler.items.ItemHandler;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.InventoryUpdate;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;
import ru.j2dev.gameserver.templates.item.ItemTemplate;
import ru.j2dev.gameserver.utils.Location;

import java.util.*;

public class CapsuleItemHolder extends AbstractHolder {
    private final IntMap<List<CapsuledItem>> _capsuleItems = new IntMap<>();
    private final IntMap<Pair<Integer, Long>> _capsuleRequiredItems = new IntMap<>();
    private CapsuleItemsHandler _itemsHandler;

    public static CapsuleItemHolder getInstance() {
        return LazyHolder.INSTANCE;
    }

    public Pair<Integer, Long> getCapsuleRequiredItems(final int capsuleItemId) {
        return _capsuleRequiredItems.get(capsuleItemId);
    }

    public List<CapsuledItem> getCapsuledItems(final int capsuleItemId) {
        return _capsuleItems.get(capsuleItemId);
    }

    public void add(final int itemId, final List<CapsuledItem> capsuledItems_) {
        add(itemId, null, capsuledItems_);
    }

    public void add(final int itemId, final Pair<Integer, Long> requiredItem, final List<CapsuledItem> capsuledItems_) {
        if (_capsuleItems.containsKey(itemId)) {
            warn("Capsule item " + itemId + " already defined.");
        }
        _capsuleItems.put(itemId, Collections.unmodifiableList(capsuledItems_));
        if (requiredItem != null) {
            _capsuleRequiredItems.put(itemId, requiredItem);
        }
    }

    @Override
    public int size() {
        return _capsuleItems.size();
    }

    @Override
    public void clear() {
        ItemHandler.getInstance().unregisterItemHandler(_itemsHandler);
        _capsuleItems.clear();
    }

    @Override
    protected void process() {
        final int[] itemIds = new int[_capsuleItems.size()];
        final Iterator<IntMap.Entry> capsuleItemIdIt = _capsuleItems.iterator();
        int i = 0;
        while (capsuleItemIdIt.hasNext()) {
            itemIds[i++] = capsuleItemIdIt.next().getKey();
        }
        _itemsHandler = new CapsuleItemsHandler(itemIds);
        ItemHandler.getInstance().registerItemHandler(_itemsHandler);
    }

    private static class LazyHolder {
        private static final CapsuleItemHolder INSTANCE = new CapsuleItemHolder();
    }

    public static class CapsuleItemsHandler implements IItemHandler {
        private final int[] _capsuleItemIds;

        CapsuleItemsHandler(final int[] capsuleItemIds) {
            _capsuleItemIds = capsuleItemIds;
        }

        private static List<ItemInstance> addItem(final Playable playable, final int itemId, final long count) {
            if (playable == null || count < 1L) {
                return Collections.emptyList();
            }
            Playable player;
            if (playable.isSummon()) {
                player = playable.getPlayer();
            } else {
                player = playable;
            }
            final List<ItemInstance> result = new LinkedList<>();
            final ItemTemplate t = ItemTemplateHolder.getInstance().getTemplate(itemId);
            if (t != null && t.isStackable()) {
                result.add(player.getInventory().addItem(itemId, count));
            } else {
                for (long i = 0L; i < count; ++i) {
                    result.add(player.getInventory().addItem(itemId, 1L));
                }
            }
            player.sendPacket(SystemMessage2.obtainItems(itemId, count, 0));
            return result;
        }

        @Override
        public boolean useItem(final Playable playable, final ItemInstance item, final boolean ctrl) {
            final int itemId = item.getItemId();
            final List<CapsuledItem> capsuledItems = getInstance().getCapsuledItems(itemId);
            if (capsuledItems == null) {
                return false;
            }
            final Pair<Integer, Long> reqiredItem = getInstance().getCapsuleRequiredItems(itemId);
            if (reqiredItem != null && reqiredItem.getKey() > 0 && reqiredItem.getValue() > 0L && !playable.getInventory().destroyItemByItemId(reqiredItem.getKey(), reqiredItem.getValue())) {
                playable.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
                return false;
            }
            if (!playable.getInventory().destroyItem(item, 1L)) {
                playable.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
                return false;
            }
            playable.sendPacket(new SystemMessage(47).addItemName(item.getItemId()));
            final List<Pair<CapsuledItem, Double>> chancedItems = new ArrayList<>();
            for (final CapsuledItem capsuledItem : capsuledItems) {
                if (capsuledItem.getChance() == 100.0) {
                    final long count = (capsuledItem.getMax() > capsuledItem.getMin()) ? Rnd.get(capsuledItem.getMin(), capsuledItem.getMax()) : capsuledItem.getMin();
                    final List<ItemInstance> addedItems = addItem(playable, capsuledItem.getItemId(), count);
                    for (final ItemInstance addedItem : addedItems) {
                        if (!addedItem.canBeEnchanted(true)) {
                            continue;
                        }
                        if (capsuledItem.getMaxEnchant() > capsuledItem.getMinEnchant()) {
                            addedItem.setEnchantLevel(Rnd.get(capsuledItem.getMinEnchant(), capsuledItem.getMaxEnchant()));
                        } else {
                            addedItem.setEnchantLevel(capsuledItem.getMinEnchant());
                        }
                        playable.sendPacket(new InventoryUpdate().addModifiedItem(addedItem));
                    }
                } else {
                    chancedItems.add(Pair.of(capsuledItem, capsuledItem.getChance()));
                }
            }
            if (!chancedItems.isEmpty()) {
                chancedItems.sort(RandomUtils.DOUBLE_GROUP_COMPARATOR);
                final CapsuledItem capsuledItem2 = RandomUtils.pickRandomSortedGroup(chancedItems, 100.0);
                if (capsuledItem2 != null) {
                    final long count2 = (capsuledItem2.getMax() > capsuledItem2.getMin()) ? Rnd.get(capsuledItem2.getMin(), capsuledItem2.getMax()) : capsuledItem2.getMin();
                    final List<ItemInstance> addedItems2 = addItem(playable, capsuledItem2.getItemId(), count2);
                    for (final ItemInstance addedItem2 : addedItems2) {
                        if (!addedItem2.canBeEnchanted(true)) {
                            continue;
                        }
                        if (capsuledItem2.getMaxEnchant() > capsuledItem2.getMinEnchant()) {
                            addedItem2.setEnchantLevel(Rnd.get(capsuledItem2.getMinEnchant(), capsuledItem2.getMaxEnchant()));
                        } else {
                            addedItem2.setEnchantLevel(capsuledItem2.getMinEnchant());
                        }
                        playable.sendPacket(new InventoryUpdate().addModifiedItem(addedItem2));
                    }
                }
            }
            return true;
        }

        @Override
        public void dropItem(final Player player, final ItemInstance item, final long count, final Location loc) {
            IItemHandler.NULL.dropItem(player, item, count, loc);
        }

        @Override
        public boolean pickupItem(final Playable playable, final ItemInstance item) {
            return IItemHandler.NULL.pickupItem(playable, item);
        }

        @Override
        public int[] getItemIds() {
            return _capsuleItemIds;
        }
    }

    public static class CapsuledItem {
        private final int _itemId;
        private final long _min;
        private final long _max;
        private final double _chance;
        private final int _minEnchant;
        private final int _maxEnchant;

        public CapsuledItem(final int itemId, final long min, final long max, final double chance, final int minEnchant, final int maxEnchant) {
            _itemId = itemId;
            _min = min;
            _max = max;
            _chance = chance;
            _minEnchant = minEnchant;
            _maxEnchant = maxEnchant;
        }

        public int getItemId() {
            return _itemId;
        }

        public double getChance() {
            return _chance;
        }

        public long getMax() {
            return _max;
        }

        public long getMin() {
            return _min;
        }

        public int getMinEnchant() {
            return _minEnchant;
        }

        public int getMaxEnchant() {
            return _maxEnchant;
        }
    }
}
