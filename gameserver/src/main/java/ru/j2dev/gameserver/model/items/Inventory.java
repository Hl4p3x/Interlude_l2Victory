package ru.j2dev.gameserver.model.items;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.listener.ListenerList;
import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.listener.inventory.OnEquipListener;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance.ItemLocation;
import ru.j2dev.gameserver.model.items.listeners.StatsListener;
import ru.j2dev.gameserver.templates.item.EtcItemTemplate.EtcItemType;
import ru.j2dev.gameserver.templates.item.ItemTemplate;
import ru.j2dev.gameserver.templates.item.WeaponTemplate.WeaponType;

import java.util.Comparator;

public abstract class Inventory extends ItemContainer {
    public static final int PAPERDOLL_UNDER = 0;
    public static final int PAPERDOLL_REAR = 1;
    public static final int PAPERDOLL_LEAR = 2;
    public static final int PAPERDOLL_NECK = 3;
    public static final int PAPERDOLL_RFINGER = 4;
    public static final int PAPERDOLL_LFINGER = 5;
    public static final int PAPERDOLL_HEAD = 6;
    public static final int PAPERDOLL_RHAND = 7;
    public static final int PAPERDOLL_LHAND = 8;
    public static final int PAPERDOLL_GLOVES = 9;
    public static final int PAPERDOLL_CHEST = 10;
    public static final int PAPERDOLL_LEGS = 11;
    public static final int PAPERDOLL_FEET = 12;
    public static final int PAPERDOLL_BACK = 13;
    public static final int PAPERDOLL_FACE = 14;
    public static final int PAPERDOLL_HAIR = 15;
    public static final int PAPERDOLL_DHAIR = 16;
    public static final int PAPERDOLL_MAX = 17;
    public static final int[] PAPERDOLL_ORDER = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 7, 15, 16};
    private static final Logger LOGGER = LoggerFactory.getLogger(Inventory.class);
    protected final int _ownerId;
    protected final ItemInstance[] _paperdoll = new ItemInstance[PAPERDOLL_MAX];
    protected final InventoryListenerList _listeners = new InventoryListenerList();
    protected int _totalWeight;
    protected long _wearedMask;

    protected Inventory(final int ownerId) {
        _ownerId = ownerId;
        addListener(StatsListener.getInstance());
    }

    public static int getPaperdollIndex(final int slot) {
        switch (slot) {
            case ItemTemplate.SLOT_UNDERWEAR:
                return PAPERDOLL_UNDER;
            case ItemTemplate.SLOT_R_EAR:
                return PAPERDOLL_REAR;
            case ItemTemplate.SLOT_L_EAR:
                return PAPERDOLL_LEAR;
            case ItemTemplate.SLOT_NECK:
                return PAPERDOLL_NECK;
            case ItemTemplate.SLOT_R_FINGER:
                return PAPERDOLL_RFINGER;
            case ItemTemplate.SLOT_L_FINGER:
                return PAPERDOLL_LFINGER;
            case ItemTemplate.SLOT_HEAD:
                return PAPERDOLL_HEAD;
            case ItemTemplate.SLOT_R_HAND:
            case ItemTemplate.SLOT_LR_HAND:
                return PAPERDOLL_RHAND;
            case ItemTemplate.SLOT_L_HAND:
                return PAPERDOLL_LHAND;
            case ItemTemplate.SLOT_GLOVES:
                return PAPERDOLL_GLOVES;
            case ItemTemplate.SLOT_CHEST:
            case ItemTemplate.SLOT_FULL_ARMOR:
            case ItemTemplate.SLOT_FORMAL_WEAR:
                return PAPERDOLL_CHEST;
            case ItemTemplate.SLOT_LEGS:
                return PAPERDOLL_LEGS;
            case ItemTemplate.SLOT_FEET:
                return PAPERDOLL_FEET;
            case ItemTemplate.SLOT_BACK:
                return PAPERDOLL_BACK;
            case ItemTemplate.SLOT_HAIR:
            case ItemTemplate.SLOT_HAIRALL:
                return PAPERDOLL_FACE;
            case ItemTemplate.SLOT_DHAIR:
                return PAPERDOLL_HAIR;
        }
        return -1;
    }

    public abstract Creature getActor();

    protected abstract ItemLocation getBaseLocation();

    protected abstract ItemLocation getEquipLocation();

    public int getOwnerId() {
        return _ownerId;
    }

    protected void onRestoreItem(final ItemInstance item) {
        _totalWeight += (int) (item.getTemplate().getWeight() * item.getCount());
    }

    @Override
    protected void onAddItem(final ItemInstance item) {
        item.setOwnerId(getOwnerId());
        item.setLocation(getBaseLocation());
        item.setLocData(findSlot());
        sendAddItem(item);
        refreshWeight();
        item.save();
    }

    @Override
    protected void onModifyItem(final ItemInstance item) {
        sendModifyItem(item);
        refreshWeight();
    }

    @Override
    protected void onRemoveItem(final ItemInstance item) {
        if (item.isEquipped()) {
            unEquipItem(item);
        }
        sendRemoveItem(item);
        item.setLocData(-1);
        item.save();
        refreshWeight();
    }

    @Override
    protected void onDestroyItem(final ItemInstance item) {
        item.setCount(0L);
        item.delete();
    }

    protected void onEquip(final int slot, final ItemInstance item) {
        _listeners.onEquip(slot, item);
        item.setLocation(getEquipLocation());
        item.setLocData(slot);
        item.setEquipped(true);
        sendModifyItem(item);
        _wearedMask |= item.getTemplate().getItemMask();
        item.save();
    }

    protected void onUnequip(final int slot, final ItemInstance item) {
        item.setLocation(getBaseLocation());
        item.setLocData(findSlot());
        item.setEquipped(false);
        item.setChargedSpiritshot(0);
        item.setChargedSoulshot(0);
        sendModifyItem(item);
        _wearedMask &= ~item.getTemplate().getItemMask();
        _listeners.onUnequip(slot, item);
        item.save();
    }

    /**
     * Находит и возвращает пустой слот в инвентаре.
     */
    private int findSlot() {
        ItemInstance item;
        int slot;
        loop:
        for (slot = 0; slot < _items.size(); slot++) {
            for (final ItemInstance _item : _items) {
                item = _item;
                if (item.isEquipped() || item.getTemplate().isQuest()) { // игнорируем надетое и квестовые вещи
                    continue;
                }
                if (item.getEquipSlot() == slot) { // слот занят?
                    continue loop;
                }
            }
            break;
        }
        return slot; // слот не занят, возвращаем
    }

    public ItemInstance getPaperdollItem(final int slot) {
        return _paperdoll[slot];
    }

    public ItemInstance[] getPaperdollItems() {
        return _paperdoll;
    }

    public int getPaperdollBodyPart(final int slot) {
        ItemInstance item = getPaperdollItem(slot);
        if (item != null) {
            return item.getBodyPart();
        }
        if (slot == 15) {
            item = _paperdoll[16];
            if (item != null) {
                return item.getBodyPart();
            }
        }
        return 0;
    }

    public int getPaperdollItemId(final int slot) {
        ItemInstance item = getPaperdollItem(slot);
        if (item != null) {
            return item.getVisibleItemId();
        }
        if (slot == 15) {
            item = _paperdoll[16];
            if (item != null) {
                return item.getVisibleItemId();
            }
        }
        return 0;
    }

    public int getPaperdollObjectId(final int slot) {
        ItemInstance item = _paperdoll[slot];
        if (item != null) {
            return item.getObjectId();
        }
        if (slot == 15) {
            item = _paperdoll[16];
            if (item != null) {
                return item.getObjectId();
            }
        }
        return 0;
    }

    public void addListener(final OnEquipListener listener) {
        _listeners.add(listener);
    }

    public void removeListener(final OnEquipListener listener) {
        _listeners.remove(listener);
    }

    public ItemInstance setPaperdollItem(final int slot, final ItemInstance item) {
        writeLock();
        ItemInstance old;
        try {
            old = _paperdoll[slot];
            if (old != item) {
                if (old != null) {
                    _paperdoll[slot] = null;
                    onUnequip(slot, old);
                }
                if (item != null) {
                    onEquip(slot, _paperdoll[slot] = item);
                }
            }
        } finally {
            writeUnlock();
        }
        return old;
    }

    public long getWearedMask() {
        return _wearedMask;
    }

    public void unEquipItem(final ItemInstance item) {
        if (item.isEquipped()) {
            unEquipItemInBodySlot(item.getBodyPart(), item);
        }
    }

    public void unEquipItemInBodySlot(final int bodySlot) {
        unEquipItemInBodySlot(bodySlot, null);
    }

    private void unEquipItemInBodySlot(final int bodySlot, final ItemInstance item) {
        int pdollSlot = -1;
        switch (bodySlot) {
            case 8: {
                pdollSlot = 3;
                break;
            }
            case 4: {
                pdollSlot = 2;
                break;
            }
            case 2: {
                pdollSlot = 1;
                break;
            }
            case 6: {
                if (item == null) {
                    return;
                }
                if (getPaperdollItem(2) == item) {
                    pdollSlot = 2;
                }
                if (getPaperdollItem(1) == item) {
                    pdollSlot = 1;
                    break;
                }
                break;
            }
            case 32: {
                pdollSlot = 5;
                break;
            }
            case 16: {
                pdollSlot = 4;
                break;
            }
            case 48: {
                if (item == null) {
                    return;
                }
                if (getPaperdollItem(5) == item) {
                    pdollSlot = 5;
                }
                if (getPaperdollItem(4) == item) {
                    pdollSlot = 4;
                    break;
                }
                break;
            }
            case 65536: {
                pdollSlot = 15;
                break;
            }
            case 262144: {
                pdollSlot = 16;
                break;
            }
            case 524288: {
                setPaperdollItem(16, null);
                pdollSlot = 15;
                break;
            }
            case 64: {
                pdollSlot = 6;
                break;
            }
            case 128: {
                pdollSlot = 7;
                break;
            }
            case 256: {
                pdollSlot = 8;
                break;
            }
            case 512: {
                pdollSlot = 9;
                break;
            }
            case 2048: {
                pdollSlot = 11;
                break;
            }
            case 1024:
            case 32768:
            case 131072: {
                pdollSlot = 10;
                break;
            }
            case 8192: {
                pdollSlot = 13;
                break;
            }
            case 4096: {
                pdollSlot = 12;
                break;
            }
            case 1: {
                pdollSlot = 0;
                break;
            }
            case 16384: {
                setPaperdollItem(8, null);
                pdollSlot = 7;
                break;
            }
            default: {
                LOGGER.warn("Requested invalid body slot: " + bodySlot + ", Item: " + item + ", ownerId: '" + getOwnerId() + "'");
                return;
            }
        }
        if (pdollSlot >= 0) {
            setPaperdollItem(pdollSlot, null);
        }
    }

    public void equipItem(final ItemInstance item) {
        final int bodySlot = item.getBodyPart();
        final double hp = getActor().getCurrentHp();
        final double mp = getActor().getCurrentMp();
        final double cp = getActor().getCurrentCp();
        switch (bodySlot) {
            case 16384: {
                setPaperdollItem(8, null);
                setPaperdollItem(7, item);
                break;
            }
            case 256: {
                final ItemInstance rHandItem = getPaperdollItem(7);
                final ItemTemplate rHandItemTemplate = (rHandItem == null) ? null : rHandItem.getTemplate();
                final ItemTemplate newItem = item.getTemplate();
                if (newItem.getItemType() == EtcItemType.ARROW) {
                    if (rHandItemTemplate == null) {
                        return;
                    }
                    if (rHandItemTemplate.getItemType() != WeaponType.BOW) {
                        return;
                    }
                    if (rHandItemTemplate.getCrystalType() != newItem.getCrystalType()) {
                        return;
                    }
                } else if (newItem.getItemType() == EtcItemType.BAIT) {
                    if (rHandItemTemplate == null) {
                        return;
                    }
                    if (rHandItemTemplate.getItemType() != WeaponType.ROD) {
                        return;
                    }
                    if (!getActor().isPlayer()) {
                        return;
                    }
                    final Player owner = (Player) getActor();
                    owner.setVar("LastLure", String.valueOf(item.getObjectId()), -1L);
                } else if (rHandItemTemplate != null && rHandItemTemplate.getBodyPart() == 16384) {
                    setPaperdollItem(7, null);
                }
                setPaperdollItem(8, item);
                break;
            }
            case 128: {
                setPaperdollItem(7, item);
                break;
            }
            case 2:
            case 4:
            case 6: {
                if (_paperdoll[1] == null) {
                    setPaperdollItem(1, item);
                    break;
                }
                if (_paperdoll[2] == null) {
                    setPaperdollItem(2, item);
                    break;
                }
                setPaperdollItem(2, item);
                break;
            }
            case 16:
            case 32:
            case 48: {
                if (_paperdoll[4] == null) {
                    setPaperdollItem(4, item);
                    break;
                }
                if (_paperdoll[5] == null) {
                    setPaperdollItem(5, item);
                    break;
                }
                setPaperdollItem(5, item);
                break;
            }
            case 8: {
                setPaperdollItem(3, item);
                break;
            }
            case 32768: {
                setPaperdollItem(11, null);
                setPaperdollItem(10, item);
                break;
            }
            case 1024: {
                setPaperdollItem(10, item);
                break;
            }
            case 2048: {
                final ItemInstance chest = getPaperdollItem(10);
                if ((chest != null && chest.getBodyPart() == 32768) || getPaperdollBodyPart(10) == 131072) {
                    setPaperdollItem(10, null);
                }
                setPaperdollItem(11, item);
                break;
            }
            case 4096: {
                if (getPaperdollBodyPart(10) == 131072) {
                    setPaperdollItem(10, null);
                }
                setPaperdollItem(12, item);
                break;
            }
            case 512: {
                if (getPaperdollBodyPart(10) == 131072) {
                    setPaperdollItem(10, null);
                }
                setPaperdollItem(9, item);
                break;
            }
            case 64: {
                if (getPaperdollBodyPart(10) == 131072) {
                    setPaperdollItem(10, null);
                }
                setPaperdollItem(6, item);
                break;
            }
            case 65536: {
                final ItemInstance old = getPaperdollItem(16);
                if (old != null && old.getBodyPart() == 524288) {
                    setPaperdollItem(16, null);
                }
                setPaperdollItem(15, item);
                break;
            }
            case 262144: {
                final ItemInstance slot2 = getPaperdollItem(16);
                if (slot2 != null && slot2.getBodyPart() == 524288) {
                    setPaperdollItem(15, null);
                }
                setPaperdollItem(16, item);
                break;
            }
            case 524288: {
                setPaperdollItem(15, null);
                setPaperdollItem(16, item);
                break;
            }
            case 1: {
                setPaperdollItem(0, item);
                break;
            }
            case 8192: {
                setPaperdollItem(13, item);
                break;
            }
            case 131072: {
                setPaperdollItem(11, null);
                setPaperdollItem(6, null);
                setPaperdollItem(12, null);
                setPaperdollItem(9, null);
                setPaperdollItem(10, item);
                break;
            }
            default: {
                LOGGER.warn("unknown body slot:" + bodySlot + " for item id: " + item.getItemId());
                return;
            }
        }
        getActor().setCurrentHp(hp, false);
        getActor().setCurrentMp(mp);
        getActor().setCurrentCp(cp);
        if (getActor().isPlayer()) {
            ((Player) getActor()).autoShot();
        }
    }

    protected abstract void sendAddItem(final ItemInstance p0);

    protected abstract void sendModifyItem(final ItemInstance p0);

    protected abstract void sendRemoveItem(final ItemInstance p0);

    protected void refreshWeight() {
        int weight;
        readLock();
        try {
            weight = _items.stream().mapToInt(item -> (int) (item.getTemplate().getWeight() * item.getCount())).sum();
        } finally {
            readUnlock();
        }
        if (_totalWeight == weight) {
            return;
        }
        _totalWeight = weight;
        onRefreshWeight();
    }

    protected abstract void onRefreshWeight();

    public int getTotalWeight() {
        return _totalWeight;
    }

    public boolean validateCapacity(final ItemInstance item) {
        long slots = 0L;
        if (!item.isStackable() || getItemByItemId(item.getItemId()) == null) {
            ++slots;
        }
        return validateCapacity(slots);
    }

    public boolean validateCapacity(final int itemId, final long count) {
        final ItemTemplate item = ItemTemplateHolder.getInstance().getTemplate(itemId);
        return validateCapacity(item, count);
    }

    public boolean validateCapacity(final ItemTemplate item, final long count) {
        long slots = 0L;
        if (!item.isStackable() || getItemByItemId(item.getItemId()) == null) {
            slots = count;
        }
        return validateCapacity(slots);
    }

    public boolean validateCapacity(final long slots) {
        return slots == 0L || (slots >= -2147483648L && slots <= 2147483647L && getSize() + (int) slots >= 0 && getSize() + slots <= ((Playable) getActor()).getInventoryLimit());
    }

    public boolean validateWeight(final ItemInstance item) {
        final long weight = item.getTemplate().getWeight() * item.getCount();
        return validateWeight(weight);
    }

    public boolean validateWeight(final int itemId, final long count) {
        final ItemTemplate item = ItemTemplateHolder.getInstance().getTemplate(itemId);
        return validateWeight(item, count);
    }

    public boolean validateWeight(final ItemTemplate item, final long count) {
        final long weight = item.getWeight() * count;
        return validateWeight(weight);
    }

    public boolean validateWeight(final long weight) {
        return weight == 0L || (weight >= Integer.MIN_VALUE && weight <= Integer.MAX_VALUE && getTotalWeight() + (int) weight >= 0 && getTotalWeight() + weight <= ((Playable) getActor()).getMaxLoad());
    }

    public abstract void restore();

    public abstract void store();

    @Override
    public int getSize() {
        return super.getSize() - getQuestSize();
    }

    public int getAllSize() {
        return super.getSize();
    }

    public int getQuestSize() {
        return (int) getItems().stream().filter(item -> item.getTemplate().isQuest()).count();
    }

    public static class ItemOrderComparator implements Comparator<ItemInstance> {
        private static final Comparator<ItemInstance> instance = new ItemOrderComparator();

        public static Comparator<ItemInstance> getInstance() {
            return instance;
        }

        @Override
        public int compare(final ItemInstance o1, final ItemInstance o2) {
            if (o1 == null || o2 == null) {
                return 0;
            }
            return o1.getLocData() - o2.getLocData();
        }
    }

    public class InventoryListenerList extends ListenerList<Playable> {
        public void onEquip(final int slot, final ItemInstance item) {
            getListeners().forEach(listener -> ((OnEquipListener) listener).onEquip(slot, item, ((Playable) getActor())));
        }

        public void onUnequip(final int slot, final ItemInstance item) {
            getListeners().forEach(listener -> ((OnEquipListener) listener).onUnequip(slot, item, ((Playable) getActor())));
        }
    }
}
