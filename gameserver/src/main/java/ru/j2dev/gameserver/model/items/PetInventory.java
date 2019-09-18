package ru.j2dev.gameserver.model.items;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.PetInstance;
import ru.j2dev.gameserver.model.items.ItemInstance.ItemLocation;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PetInventoryUpdate;
import ru.j2dev.gameserver.utils.ItemFunctions;

import java.util.Collection;

public class PetInventory extends Inventory {
    private final PetInstance _actor;

    public PetInventory(final PetInstance actor) {
        super(actor.getPlayer().getObjectId());
        _actor = actor;
    }

    @Override
    public PetInstance getActor() {
        return _actor;
    }

    public Player getOwner() {
        return _actor.getPlayer();
    }

    @Override
    protected ItemLocation getBaseLocation() {
        return ItemLocation.PET_INVENTORY;
    }

    @Override
    protected ItemLocation getEquipLocation() {
        return ItemLocation.PET_PAPERDOLL;
    }

    @Override
    protected void onRefreshWeight() {
        getActor().sendPetInfo();
    }

    @Override
    protected void sendAddItem(final ItemInstance item) {
        getOwner().sendPacket(new PetInventoryUpdate().addNewItem(item));
    }

    @Override
    protected void sendModifyItem(final ItemInstance item) {
        getOwner().sendPacket(new PetInventoryUpdate().addModifiedItem(item));
    }

    @Override
    protected void sendRemoveItem(final ItemInstance item) {
        getOwner().sendPacket(new PetInventoryUpdate().addRemovedItem(item));
    }

    @Override
    public void restore() {
        final int ownerId = getOwnerId();
        writeLock();
        try {
            Collection<ItemInstance> items = _itemsDAO.loadItemsByOwnerIdAndLoc(ownerId, getBaseLocation());
            for (final ItemInstance item : items) {
                _items.add(item);
                onRestoreItem(item);
            }
            items = _itemsDAO.loadItemsByOwnerIdAndLoc(ownerId, getEquipLocation());
            for (final ItemInstance item : items) {
                _items.add(item);
                onRestoreItem(item);
                if (ItemFunctions.checkIfCanEquip(getActor(), item) == null) {
                    setPaperdollItem(item.getEquipSlot(), item);
                }
            }
        } finally {
            writeUnlock();
        }
        refreshWeight();
    }

    @Override
    public void store() {
        writeLock();
        try {
            _itemsDAO.store(_items);
        } finally {
            writeUnlock();
        }
    }

    public void validateItems() {
        for (final ItemInstance item : _paperdoll) {
            if (item != null && (ItemFunctions.checkIfCanEquip(getActor(), item) != null || !item.getTemplate().testCondition(getActor(), item, false))) {
                unEquipItem(item);
            }
        }
    }
}
