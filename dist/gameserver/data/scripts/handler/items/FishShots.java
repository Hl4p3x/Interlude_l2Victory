package handler.items;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExAutoSoulShot;
import ru.j2dev.gameserver.network.lineage2.serverpackets.MagicSkillUse;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.templates.item.WeaponTemplate;
import ru.j2dev.gameserver.templates.item.WeaponTemplate.WeaponType;

public class FishShots extends ScriptItemHandler {
    private static final int[] _itemIds = {6535, 6536, 6537, 6538, 6539, 6540};
    private static final int[] _skillIds = {2181, 2182, 2183, 2184, 2185, 2186};

    @Override
    public boolean useItem(final Playable playable, final ItemInstance item, final boolean ctrl) {
        if (playable == null || !playable.isPlayer()) {
            return false;
        }
        final Player player = (Player) playable;
        final int FishshotId = item.getItemId();
        boolean isAutoSoulShot = false;
        if (player.getAutoSoulShot().contains(FishshotId)) {
            isAutoSoulShot = true;
        }
        final ItemInstance weaponInst = player.getActiveWeaponInstance();
        final WeaponTemplate weaponItem = player.getActiveWeaponItem();
        if (weaponInst == null || weaponItem.getItemType() != WeaponType.ROD) {
            if (!isAutoSoulShot) {
                player.sendPacket(Msg.CANNOT_USE_SOULSHOTS);
            }
            return false;
        }
        if (weaponInst.getChargedFishshot()) {
            return false;
        }
        if (item.getCount() < 1L) {
            if (isAutoSoulShot) {
                player.removeAutoSoulShot(FishshotId);
                player.sendPacket(new ExAutoSoulShot(FishshotId, false), new SystemMessage(1434).addString(item.getName()));
                return false;
            }
            player.sendPacket(Msg.NOT_ENOUGH_SPIRITSHOTS);
            return false;
        } else {
            final int grade = weaponItem.getCrystalType().gradeOrd();
            if ((grade != 0 || FishshotId == 6535) && (grade != 1 || FishshotId == 6536) && (grade != 2 || FishshotId == 6537) && (grade != 3 || FishshotId == 6538) && (grade != 4 || FishshotId == 6539) && (grade != 5 || FishshotId == 6540)) {
                if (player.getInventory().destroyItem(item, 1L)) {
                    weaponInst.setChargedFishshot(true);
                    player.sendPacket(Msg.POWER_OF_MANA_ENABLED);
                    player.broadcastPacket(new MagicSkillUse(player, player, _skillIds[grade], 1, 0, 0L));
                }
                return true;
            }
            if (isAutoSoulShot) {
                return false;
            }
            player.sendPacket(Msg.THIS_FISHING_SHOT_IS_NOT_FIT_FOR_THE_FISHING_POLE_CRYSTAL);
            return false;
        }
    }

    @Override
    protected boolean isShotsHandler() {
        return true;
    }

    @Override
    public int[] getItemIds() {
        return _itemIds;
    }
}
