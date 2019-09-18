package handler.items;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExAutoSoulShot;
import ru.j2dev.gameserver.network.lineage2.serverpackets.MagicSkillUse;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.templates.item.WeaponTemplate;

public class SpiritShot extends ScriptItemHandler {
    private static final int[] _itemIds = {5790, 2509, 2510, 2511, 2512, 2513, 2514};
    private static final int[] _skillIds = {2061, 2155, 2156, 2157, 2158, 2159};

    @Override
    public boolean useItem(final Playable playable, final ItemInstance item, final boolean ctrl) {
        if (playable == null || !playable.isPlayer()) {
            return false;
        }
        final Player player = (Player) playable;
        final ItemInstance weaponInst = player.getActiveWeaponInstance();
        final WeaponTemplate weaponItem = player.getActiveWeaponItem();
        final int SoulshotId = item.getItemId();
        boolean isAutoSoulShot = false;
        if (player.getAutoSoulShot().contains(SoulshotId)) {
            isAutoSoulShot = true;
        }
        if (weaponInst == null) {
            if (!isAutoSoulShot) {
                player.sendPacket(Msg.CANNOT_USE_SPIRITSHOTS);
            }
            return false;
        }
        if (weaponInst.getChargedSpiritshot() != 0) {
            return false;
        }
        final int SpiritshotId = item.getItemId();
        final int grade = weaponItem.getCrystalType().gradeOrd();
        final int soulSpiritConsumption = weaponItem.getSpiritShotCount();
        final long count = item.getCount();
        if (soulSpiritConsumption == 0) {
            if (isAutoSoulShot) {
                player.removeAutoSoulShot(SoulshotId);
                player.sendPacket(new ExAutoSoulShot(SoulshotId, false), new SystemMessage(1434).addItemName(SoulshotId));
                return false;
            }
            player.sendPacket(Msg.CANNOT_USE_SPIRITSHOTS);
            return false;
        } else if ((grade == 0 && SpiritshotId != 5790 && SpiritshotId != 2509) || (grade == 1 && SpiritshotId != 2510) || (grade == 2 && SpiritshotId != 2511) || (grade == 3 && SpiritshotId != 2512) || (grade == 4 && SpiritshotId != 2513) || (grade == 5 && SpiritshotId != 2514)) {
            if (isAutoSoulShot) {
                return false;
            }
            player.sendPacket(Msg.SPIRITSHOT_DOES_NOT_MATCH_WEAPON_GRADE);
            return false;
        } else if (count < soulSpiritConsumption) {
            if (isAutoSoulShot) {
                player.removeAutoSoulShot(SoulshotId);
                player.sendPacket(new ExAutoSoulShot(SoulshotId, false), new SystemMessage(1434).addItemName(SoulshotId));
                return false;
            }
            player.sendPacket(Msg.NOT_ENOUGH_SPIRITSHOTS);
            return false;
        } else {
            if (Config.ALT_CONSUME_SPIRITSHOTS && !player.getInventory().destroyItem(item, (long) soulSpiritConsumption)) {
                player.sendPacket(Msg.NOT_ENOUGH_SPIRITSHOTS);
                return false;
            }
            weaponInst.setChargedSpiritshot(1);
            player.sendPacket(Msg.POWER_OF_MANA_ENABLED);
            player.broadcastPacket(new MagicSkillUse(player, player, _skillIds[grade], 1, 0, 0L));
            return true;
        }
    }

    @Override
    public final int[] getItemIds() {
        return _itemIds;
    }

    @Override
    protected boolean isShotsHandler() {
        return true;
    }
}
