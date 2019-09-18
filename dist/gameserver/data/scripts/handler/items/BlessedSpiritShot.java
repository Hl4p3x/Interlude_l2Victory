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

public class BlessedSpiritShot extends ScriptItemHandler {
    private static final int[] _itemIds = {3947, 3948, 3949, 3950, 3951, 3952};
    private static final int[] _skillIds = {2061, 2160, 2161, 2162, 2163, 2164};

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
        if (weaponInst.getChargedSpiritshot() == 2) {
            return false;
        }
        final int spiritshotId = item.getItemId();
        final int grade = weaponItem.getCrystalType().gradeOrd();
        final int blessedsoulSpiritConsumption = weaponItem.getSpiritShotCount();
        if (blessedsoulSpiritConsumption == 0) {
            if (isAutoSoulShot) {
                player.removeAutoSoulShot(SoulshotId);
                player.sendPacket(new ExAutoSoulShot(SoulshotId, false), new SystemMessage(1434).addItemName(spiritshotId));
                return false;
            }
            player.sendPacket(Msg.CANNOT_USE_SPIRITSHOTS);
            return false;
        } else if ((grade == 0 && spiritshotId != 3947) || (grade == 1 && spiritshotId != 3948) || (grade == 2 && spiritshotId != 3949) || (grade == 3 && spiritshotId != 3950) || (grade == 4 && spiritshotId != 3951) || (grade == 5 && spiritshotId != 3952)) {
            if (isAutoSoulShot) {
                return false;
            }
            player.sendPacket(Msg.SPIRITSHOT_DOES_NOT_MATCH_WEAPON_GRADE);
            return false;
        } else {
            if (!Config.ALT_CONSUME_SPIRITSHOTS || player.getInventory().destroyItem(item, (long) blessedsoulSpiritConsumption)) {
                weaponInst.setChargedSpiritshot(2);
                player.sendPacket(Msg.POWER_OF_MANA_ENABLED);
                player.broadcastPacket(new MagicSkillUse(player, player, _skillIds[grade], 1, 0, 0L));
                return true;
            }
            if (isAutoSoulShot) {
                player.removeAutoSoulShot(SoulshotId);
                player.sendPacket(new ExAutoSoulShot(SoulshotId, false), new SystemMessage(1434).addItemName(spiritshotId));
                return false;
            }
            player.sendPacket(Msg.NOT_ENOUGH_SPIRITSHOTS);
            return false;
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
