package ru.j2dev.gameserver.model.items;

import gnu.trove.list.array.TIntArrayList;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.manager.CursedWeaponsManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance.ItemLocation;
import ru.j2dev.gameserver.model.items.listeners.*;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExBR_AgathionEnergyInfo;
import ru.j2dev.gameserver.network.lineage2.serverpackets.InventoryUpdate;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.taskmanager.DelayedItemsManager;
import ru.j2dev.gameserver.templates.item.EtcItemTemplate.EtcItemType;
import ru.j2dev.gameserver.templates.item.ItemTemplate;
import ru.j2dev.gameserver.utils.ItemFunctions;

import java.util.Collection;
import java.util.stream.IntStream;

public class PcInventory extends Inventory {
    private static final int[][] arrows = {{17}, {1341, 22067}, {1342, 22068}, {1343, 22069}, {1344, 22070}, {1345, 22071}};

    private final Player _owner;
    public boolean isRefresh;
    private LockType _lockType = LockType.NONE;
    private TIntArrayList _lockedIdList = new TIntArrayList();

    public PcInventory(final Player owner) {
        super(owner.getObjectId());
        _owner = owner;
        addListener(ItemSkillsListener.getInstance());
        addListener(ItemAugmentationListener.getInstance());
        addListener(ItemEnchantOptionsListener.getInstance());
        addListener(ArmorSetListener.getInstance());
        addListener(BowListener.getInstance());
        addListener(AccessoryListener.getInstance());
    }

    @Override
    public Player getActor() {
        return _owner;
    }

    @Override
    protected ItemLocation getBaseLocation() {
        return ItemLocation.INVENTORY;
    }

    @Override
    protected ItemLocation getEquipLocation() {
        return ItemLocation.PAPERDOLL;
    }

    public long getAdena() {
        final ItemInstance _adena = getItemByItemId(57);
        if (_adena == null) {
            return 0L;
        }
        return _adena.getCount();
    }

    public ItemInstance addAdena(final long amount) {
        return addItem(57, amount);
    }

    public boolean reduceAdena(final long adena) {
        return destroyItemByItemId(57, adena);
    }

    public int getPaperdollAugmentationId(final int slot) {
        final ItemInstance item = _paperdoll[slot];
        if (item != null && item.isAugmented()) {
            return (item.getVariationStat1() & 0xFFFF) | item.getVariationStat2() << 16;
        }
        return 0;
    }

    @Override
    protected void onRefreshWeight() {
        getActor().refreshOverloaded();
    }

    public void validateItems() {
        for (final ItemInstance item : _paperdoll) {
            if (item != null && (ItemFunctions.checkIfCanEquip(getActor(), item) != null || !item.getTemplate().testCondition(getActor(), item, true))) {
                unEquipItem(item);
                getActor().sendDisarmMessage(item);
            }
        }
    }

    public void validateItemsSkills() {
        for (final ItemInstance item : _paperdoll) {
            if (item != null) {
                if (item.getTemplate().getType2() == 0) {
                    final boolean needUnequipSkills = getActor().getGradePenalty() > 0;
                    if (item.getTemplate().getAttachedSkills().length > 0) {
                        final boolean has = getActor().getSkillLevel(item.getTemplate().getAttachedSkills()[0].getId()) > 0;
                        if (needUnequipSkills && has) {
                            ItemSkillsListener.getInstance().onUnequip(item.getEquipSlot(), item, getActor());
                        } else if (!needUnequipSkills && !has) {
                            ItemSkillsListener.getInstance().onEquip(item.getEquipSlot(), item, getActor());
                        }
                    } else if (item.getTemplate().getEnchant4Skill() != null) {
                        final boolean has = getActor().getSkillLevel(item.getTemplate().getEnchant4Skill().getId()) > 0;
                        if (needUnequipSkills && has) {
                            ItemSkillsListener.getInstance().onUnequip(item.getEquipSlot(), item, getActor());
                        } else if (!needUnequipSkills && !has) {
                            ItemSkillsListener.getInstance().onEquip(item.getEquipSlot(), item, getActor());
                        }
                    } else if (!item.getTemplate().getTriggerList().isEmpty()) {
                        if (needUnequipSkills) {
                            ItemSkillsListener.getInstance().onUnequip(item.getEquipSlot(), item, getActor());
                        } else {
                            ItemSkillsListener.getInstance().onEquip(item.getEquipSlot(), item, getActor());
                        }
                    }
                }
            }
        }
    }

    public void refreshEquip() {
        isRefresh = true;
        getItems().forEach(item -> {
            if (item.isEquipped()) {
                final int slot = item.getEquipSlot();
                _listeners.onUnequip(slot, item);
                _listeners.onEquip(slot, item);
            } else if (item.getItemType() == EtcItemType.RUNE) {
                _listeners.onUnequip(-1, item);
                _listeners.onEquip(-1, item);
            }
        });
        isRefresh = false;
    }

    public void sort(final int[][] order) {
        boolean needSort = false;
        for (final int[] element : order) {
            final ItemInstance item = getItemByObjectId(element[0]);
            if (item != null) {
                if (item.getLocation() == ItemLocation.INVENTORY) {
                    if (item.getLocData() != element[1]) {
                        item.setLocData(element[1]);
                        needSort = true;
                    }
                }
            }
        }
        if (needSort) {
            _items.sort(ItemOrderComparator.getInstance());
        }
    }

    public ItemInstance findArrowForBow(final ItemTemplate bow) {
        final int[] arrowsId = PcInventory.arrows[bow.getCrystalType().gradeOrd()];
        ItemInstance ret;
        for (final int id : arrowsId) {
            if ((ret = getItemByItemId(id)) != null) {
                return ret;
            }
        }
        return null;
    }

    public ItemInstance findEquippedLure() {
        ItemInstance res = null;
        int last_lure = 0;
        final Player owner = getActor();
        final String LastLure = owner.getVar("LastLure");
        if (LastLure != null && !LastLure.isEmpty()) {
            last_lure = Integer.parseInt(LastLure);
        }
        for (final ItemInstance temp : getItems()) {
            if (temp.getItemType() == EtcItemType.BAIT) {
                if (temp.getLocation() == ItemLocation.PAPERDOLL && temp.getEquipSlot() == 8) {
                    return temp;
                }
                if (last_lure > 0 && res == null && temp.getObjectId() == last_lure) {
                    res = temp;
                }
            }
        }
        return res;
    }

    public void lockItems(final LockType lock, final int[] items) {
        if (_lockType != LockType.NONE) {
            return;
        }
        _lockType = lock;
        _lockedIdList.add(items);
        getActor().sendItemList(false);
    }

    public void unlockItems(final int[] items) {
        if (_lockType == LockType.NONE) {
            return;
        }

        IntStream.of(items).forEach(item -> _lockedIdList.remove(item));
        if(_lockedIdList.isEmpty()) {
            _lockType = LockType.NONE;
            _lockedIdList.clear();
        }
        getActor().sendItemList(false);
    }

    public void unlock() {
        if (_lockType == LockType.NONE) {
            return;
        }
        _lockType = LockType.NONE;
        _lockedIdList.clear();
        getActor().sendItemList(false);
    }

    public boolean itemIsLocked(final ItemInstance item) {
        switch (_lockType) {
            case INCLUDE: {
                return _lockedIdList.contains(item.getItemId());
            }
            case EXCLUDE: {
                return !_lockedIdList.contains(item.getItemId());
            }
            default: {
                return false;
            }
        }
    }

    public LockType getLockType() {
        return _lockType;
    }

    public int[] getLockItems() {
        return _lockedIdList.toArray();
    }

    @Override
    protected void onRestoreItem(final ItemInstance item) {
        super.onRestoreItem(item);
        if (item.getItemType() == EtcItemType.RUNE) {
            _listeners.onEquip(-1, item);
        }
        if (item.isTemporalItem()) {
            item.startTimer(new LifeTimeTask(item));
        }
        if (item.isCursed()) {
            CursedWeaponsManager.getInstance().checkPlayer(getActor(), item);
        }
    }

    @Override
    protected void onAddItem(final ItemInstance item) {
        super.onAddItem(item);
        if (item.getItemType() == EtcItemType.RUNE) {
            _listeners.onEquip(-1, item);
        }
        if (item.isTemporalItem()) {
            item.startTimer(new LifeTimeTask(item));
        }
        if (item.isCursed()) {
            CursedWeaponsManager.getInstance().checkPlayer(getActor(), item);
        }
    }

    @Override
    protected void onRemoveItem(final ItemInstance item) {
        super.onRemoveItem(item);
        getActor().removeItemFromShortCut(item.getObjectId());
        if (item.getItemType() == EtcItemType.RUNE) {
            _listeners.onUnequip(-1, item);
        }
        if (item.isTemporalItem()) {
            item.stopTimer();
        }
    }

    @Override
    protected void onEquip(final int slot, final ItemInstance item) {
        super.onEquip(slot, item);
        if (item.isShadowItem()) {
            item.startTimer(new ShadowLifeTimeTask(item));
        }
    }

    @Override
    protected void onUnequip(final int slot, final ItemInstance item) {
        super.onUnequip(slot, item);
        if (item.isShadowItem()) {
            item.stopTimer();
        }
    }

    @Override
    public void restore() {
        final int ownerId = getOwnerId();
        writeLock();
        try {
            Collection<ItemInstance> items = PcInventory._itemsDAO.loadItemsByOwnerIdAndLoc(ownerId, getBaseLocation());
            for (final ItemInstance item : items) {
                _items.add(item);
                onRestoreItem(item);
            }
            _items.sort(ItemOrderComparator.getInstance());
            items = PcInventory._itemsDAO.loadItemsByOwnerIdAndLoc(ownerId, getEquipLocation());
            for (final ItemInstance item : items) {
                _items.add(item);
                onRestoreItem(item);
                if (item.getEquipSlot() >= 17) {
                    item.setLocation(getBaseLocation());
                    item.setLocData(0);
                    item.setEquipped(false);
                } else {
                    setPaperdollItem(item.getEquipSlot(), item);
                }
            }
        } finally {
            writeUnlock();
        }
        DelayedItemsManager.getInstance().loadDelayed(getActor(), false);
        refreshWeight();
    }

    @Override
    public void store() {
        writeLock();
        try {
            PcInventory._itemsDAO.store(_items);
        } finally {
            writeUnlock();
        }
    }

    @Override
    protected void sendAddItem(final ItemInstance item) {
        final Player actor = getActor();
        actor.sendPacket(new InventoryUpdate().addNewItem(item));
        if (item.getTemplate().getAgathionEnergy() > 0) {
            actor.sendPacket(new ExBR_AgathionEnergyInfo(1, item));
        }
    }

    @Override
    protected void sendModifyItem(final ItemInstance item) {
        final Player actor = getActor();
        actor.sendPacket(new InventoryUpdate().addModifiedItem(item));
        if (item.getTemplate().getAgathionEnergy() > 0) {
            actor.sendPacket(new ExBR_AgathionEnergyInfo(1, item));
        }
    }

    @Override
    protected void sendRemoveItem(final ItemInstance item) {
        getActor().sendPacket(new InventoryUpdate().addRemovedItem(item));
    }

    public void startTimers() {
    }

    public void stopAllTimers() {
        getItems().stream().filter(item -> item.isShadowItem() || item.isTemporalItem()).forEach(ItemInstance::stopTimer);
    }

    protected class ShadowLifeTimeTask extends RunnableImpl {
        private final ItemInstance item;

        ShadowLifeTimeTask(final ItemInstance item) {
            this.item = item;
        }

        @Override
        public void runImpl() {
            final Player player = getActor();
            if (!item.isEquipped()) {
                return;
            }
            final int duration;
            synchronized (item) {
                duration = Math.max(0, item.getDuration() - 1);
                item.setDuration(duration);
                if (duration == 0) {
                    destroyItem(item);
                }
            }
            SystemMessage sm = null;
            if (duration == 10) {
                sm = new SystemMessage(1979);
            } else if (duration == 5) {
                sm = new SystemMessage(1980);
            } else if (duration == 1) {
                sm = new SystemMessage(1981);
            } else if (duration <= 0) {
                sm = new SystemMessage(1982);
            } else {
                player.sendPacket(new InventoryUpdate().addModifiedItem(item));
            }
            if (sm != null) {
                sm.addItemName(item.getItemId());
                player.sendPacket(sm);
            }
        }
    }

    protected class LifeTimeTask extends RunnableImpl {
        private final ItemInstance item;

        LifeTimeTask(final ItemInstance item) {
            this.item = item;
        }

        @Override
        public void runImpl() {
            final Player player = getActor();
            final int left;
            synchronized (item) {
                left = item.getPeriod();
                if (left <= 0) {
                    destroyItem(item);
                }
            }
            if (left <= 0) {
                player.sendPacket(new SystemMessage(1726).addItemName(item.getItemId()));
            }
        }
    }
}
