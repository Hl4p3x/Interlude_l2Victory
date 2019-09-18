package handler.items;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExAutoSoulShot;
import ru.j2dev.gameserver.network.lineage2.serverpackets.MagicSkillUse;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.stats.Stats;
import ru.j2dev.gameserver.templates.item.WeaponTemplate;
import ru.j2dev.gameserver.templates.item.WeaponTemplate.WeaponType;

public class SoulShots extends ScriptItemHandler {
    private static final int[] _itemIds = {5789, 1835, 1463, 1464, 1465, 1466, 1467};
    private static final int[] _skillIds = {2039, 2150, 2151, 2152, 2153, 2154};

    @Override
    public boolean useItem(final Playable playable, final ItemInstance item, final boolean ctrl) {
        if (playable == null || !playable.isPlayer()) {
            return false;
        }
        final Player player = (Player) playable;
        final WeaponTemplate weaponItem = player.getActiveWeaponItem();
        final ItemInstance weaponInst = player.getActiveWeaponInstance();
        final int SoulshotId = item.getItemId();
        boolean isAutoSoulShot = false;
        if (player.getAutoSoulShot().contains(SoulshotId)) {
            isAutoSoulShot = true;
        }
        if (weaponInst == null) {
            if (!isAutoSoulShot) {
                player.sendPacket(Msg.CANNOT_USE_SOULSHOTS);
            }
            return false;
        }
        if (weaponInst.getChargedSoulshot() != 0) {
            return false;
        }
        final int grade = weaponItem.getCrystalType().gradeOrd();
        int soulShotConsumption = weaponItem.getSoulShotCount();
        if (soulShotConsumption == 0) {
            if (isAutoSoulShot) {
                player.removeAutoSoulShot(SoulshotId);
                player.sendPacket(new ExAutoSoulShot(SoulshotId, false), new SystemMessage(1434).addItemName(SoulshotId));
                return false;
            }
            player.sendPacket(Msg.CANNOT_USE_SOULSHOTS);
            return false;
        } else if ((grade == 0 && SoulshotId != 5789 && SoulshotId != 1835) || (grade == 1 && SoulshotId != 1463) || (grade == 2 && SoulshotId != 1464) || (grade == 3 && SoulshotId != 1465) || (grade == 4 && SoulshotId != 1466) || (grade == 5 && SoulshotId != 1467)) {
            if (isAutoSoulShot) {
                return false;
            }
            player.sendPacket(Msg.SOULSHOT_DOES_NOT_MATCH_WEAPON_GRADE);
            return false;
        } else {
            if (weaponItem.getItemType() == WeaponType.BOW) {
                final int newSS = (int) player.calcStat(Stats.SS_USE_BOW, (double) soulShotConsumption, null, null);
                if (newSS < soulShotConsumption && Rnd.chance(player.calcStat(Stats.SS_USE_BOW_CHANCE, (double) soulShotConsumption, null, null))) {
                    soulShotConsumption = newSS;
                }
            }
            if (Config.ALT_CONSUME_SOULSHOTS && !player.getInventory().destroyItem(item, (long) soulShotConsumption)) {
                player.sendPacket(Msg.NOT_ENOUGH_SOULSHOTS);
                return false;
            }
            weaponInst.setChargedSoulshot(1);
            player.sendPacket(Msg.POWER_OF_THE_SPIRITS_ENABLED);
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
