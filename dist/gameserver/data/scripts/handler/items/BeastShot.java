package handler.items;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Summon;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.MagicSkillUse;

public class BeastShot extends ScriptItemHandler {
    private static final int[] _itemIds = {6645, 6646, 6647};

    @Override
    public boolean useItem(final Playable playable, final ItemInstance item, final boolean ctrl) {
        if (playable == null || !playable.isPlayer()) {
            return false;
        }
        final Player player = (Player) playable;
        boolean isAutoSoulShot = false;
        if (player.getAutoSoulShot().contains(item.getItemId())) {
            isAutoSoulShot = true;
        }
        final Summon pet = player.getPet();
        if (pet == null) {
            if (!isAutoSoulShot) {
                player.sendPacket(Msg.PETS_AND_SERVITORS_ARE_NOT_AVAILABLE_AT_THIS_TIME);
            }
            return false;
        }
        if (pet.isDead()) {
            if (!isAutoSoulShot) {
                player.sendPacket(Msg.WHEN_PET_OR_SERVITOR_IS_DEAD_SOULSHOTS_OR_SPIRITSHOTS_FOR_PET_OR_SERVITOR_ARE_NOT_AVAILABLE);
            }
            return false;
        }
        int consumption;
        int skillid = 0;
        switch (item.getItemId()) {
            case 6645: {
                if (pet.getChargedSoulShot()) {
                    return false;
                }
                consumption = pet.getSoulshotConsumeCount();
                if (!player.getInventory().destroyItem(item, (long) consumption)) {
                    player.sendPacket(Msg.YOU_DONT_HAVE_ENOUGH_SOULSHOTS_NEEDED_FOR_A_PET_SERVITOR);
                    return false;
                }
                pet.chargeSoulShot();
                skillid = 2033;
                break;
            }
            case 6646: {
                if (pet.getChargedSpiritShot() > 0) {
                    return false;
                }
                consumption = pet.getSpiritshotConsumeCount();
                if (!player.getInventory().destroyItem(item, (long) consumption)) {
                    player.sendPacket(Msg.YOU_DONT_HAVE_ENOUGH_SPIRITSHOTS_NEEDED_FOR_A_PET_SERVITOR);
                    return false;
                }
                pet.chargeSpiritShot(1);
                skillid = 2008;
                break;
            }
            case 6647: {
                if (pet.getChargedSpiritShot() > 1) {
                    return false;
                }
                consumption = pet.getSpiritshotConsumeCount();
                if (!player.getInventory().destroyItem(item, (long) consumption)) {
                    player.sendPacket(Msg.YOU_DONT_HAVE_ENOUGH_SPIRITSHOTS_NEEDED_FOR_A_PET_SERVITOR);
                    return false;
                }
                pet.chargeSpiritShot(2);
                skillid = 2009;
                break;
            }
        }
        pet.broadcastPacket(new MagicSkillUse(pet, pet, skillid, 1, 0, 0L));
        return true;
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
