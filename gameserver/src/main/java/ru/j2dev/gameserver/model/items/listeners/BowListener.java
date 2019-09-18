package ru.j2dev.gameserver.model.items.listeners;

import ru.j2dev.gameserver.listener.inventory.OnEquipListener;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.templates.item.WeaponTemplate.WeaponType;

public final class BowListener implements OnEquipListener {
    private static final BowListener _instance = new BowListener();

    public static BowListener getInstance() {
        return _instance;
    }

    @Override
    public void onUnequip(final int slot, final ItemInstance item, final Playable actor) {
        if (!item.isEquipable() || slot != 7) {
            return;
        }
        final Player player = (Player) actor;
        if (item.getItemType() == WeaponType.BOW || item.getItemType() == WeaponType.ROD) {
            player.getInventory().setPaperdollItem(8, null);
        }
    }

    @Override
    public void onEquip(final int slot, final ItemInstance item, final Playable actor) {
        if (!item.isEquipable() || slot != 7) {
            return;
        }
        final Player player = (Player) actor;
        if (item.getItemType() == WeaponType.BOW) {
            final ItemInstance arrow = player.getInventory().findArrowForBow(item.getTemplate());
            if (arrow != null) {
                player.getInventory().setPaperdollItem(8, arrow);
            }
        }
        if (item.getItemType() == WeaponType.ROD) {
            final ItemInstance bait = player.getInventory().findEquippedLure();
            if (bait != null) {
                player.getInventory().setPaperdollItem(8, bait);
            }
        }
    }
}
