package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;

public class SystemMessage2 extends SysMsgContainer<SystemMessage2> {
    public SystemMessage2(final SystemMsg message) {
        super(message);
    }

    public static SystemMessage2 obtainItems(final int itemId, final long count, final int enchantLevel) {
        if (itemId == 57) {
            return new SystemMessage2(SystemMsg.YOU_HAVE_EARNED_S1_ADENA).addLong(count);
        }
        if (count > 1L) {
            return new SystemMessage2(SystemMsg.YOU_HAVE_EARNED_S2_S1S).addItemName(itemId).addLong(count);
        }
        if (enchantLevel > 0) {
            return new SystemMessage2(SystemMsg.YOU_HAVE_OBTAINED_A_S1_S2).addInteger(enchantLevel).addItemName(itemId);
        }
        return new SystemMessage2(SystemMsg.YOU_HAVE_EARNED_S1).addItemName(itemId);
    }

    public static SystemMessage2 obtainItems(final ItemInstance item) {
        return obtainItems(item.getItemId(), item.getCount(), item.isEquipable() ? item.getEnchantLevel() : 0);
    }

    public static SystemMessage2 obtainItemsBy(final int itemId, final long count, final int enchantLevel, final Creature target) {
        if (count > 1L) {
            return new SystemMessage2(SystemMsg.C1_HAS_OBTAINED_S3_S2).addName(target).addItemName(itemId).addLong(count);
        }
        if (enchantLevel > 0) {
            return new SystemMessage2(SystemMsg.C1_HAS_OBTAINED_S2S3).addName(target).addInteger(enchantLevel).addItemName(itemId);
        }
        return new SystemMessage2(SystemMsg.C1_HAS_OBTAINED_S2).addName(target).addItemName(itemId);
    }

    public static SystemMessage2 obtainItemsBy(final ItemInstance item, final Creature target) {
        return obtainItemsBy(item.getItemId(), item.getCount(), item.isEquipable() ? item.getEnchantLevel() : 0, target);
    }

    public static SystemMessage2 removeItems(final int itemId, final long count) {
        if (itemId == 57) {
            return new SystemMessage2(SystemMsg.S1_ADENA_DISAPPEARED).addLong(count);
        }
        if (count > 1L) {
            return new SystemMessage2(SystemMsg.S2_S1_HAS_DISAPPEARED).addItemName(itemId).addLong(count);
        }
        return new SystemMessage2(SystemMsg.S1_HAS_DISAPPEARED).addItemName(itemId);
    }

    public static SystemMessage2 removeItems(final ItemInstance item) {
        return removeItems(item.getItemId(), item.getCount());
    }

    @Override
    protected void writeImpl() {
        writeC(0x64);
        writeElements();
    }
}
