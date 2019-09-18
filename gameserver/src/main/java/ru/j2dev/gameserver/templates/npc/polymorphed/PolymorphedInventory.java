package ru.j2dev.gameserver.templates.npc.polymorphed;


import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.items.Inventory;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.items.ItemInstance.ItemLocation;

/**
 * Created by JunkyFunky
 * on 06.01.2018 18:47
 * group j2dev
 */
public class PolymorphedInventory extends Inventory {

    private final NpcInstance _owner;

    public PolymorphedInventory(final NpcInstance owner) {
        super(owner.getObjectId());
        _owner = owner;
    }

    @Override
    public NpcInstance getActor() {
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

    @Override
    protected void sendAddItem(ItemInstance p0) {

    }

    @Override
    protected void sendModifyItem(ItemInstance p0) {

    }

    @Override
    protected void sendRemoveItem(ItemInstance p0) {

    }

    @Override
    protected void onRefreshWeight() {

    }
}
