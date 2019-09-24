package ru.j2dev.gameserver.model.items.listeners;

import gnu.trove.map.hash.TIntObjectHashMap;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.listener.inventory.OnEquipListener;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.Skill.SkillType;
import ru.j2dev.gameserver.model.instances.AgathionInstance;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.skills.skillclasses.Agathion;
import ru.j2dev.gameserver.skills.skillclasses.RemAgathion;

public final class AccessoryListener implements OnEquipListener {
    private static final AccessoryListener _instance = new AccessoryListener();

    public static AccessoryListener getInstance() {
        return _instance;
    }

    @Override
    public void onUnequip(final int slot, final ItemInstance item, final Playable actor) {
        if (!item.isEquipable()) {
            return;
        }
        final Player player = (Player) actor;
        if (item.getBodyPart() == 2097152 && item.getTemplate().getAttachedSkills().length > 0) {
            //нужно что то с эти придумать
            /*
            final int agathionId = player.getAgathionId();
            final int transformNpcId = player.getTransformationTemplate();
            for (final Skill skill : item.getTemplate().getAttachedSkills()) {
                if (agathionId > 0 && skill.getNpcId() == agathionId) {
                    player.setAgathion(0);
                }
                if (skill.getNpcId() == transformNpcId && skill.getSkillType() == SkillType.TRANSFORMATION) {
                    player.setTransformation(0);
                }
            }
            */

        }

        if (item.getTemplate().isAgathion())
        {
            if (player.getAgathion() != null) {
                player.getAgathion().doDespawn();
            }
        }
        if (item.isAccessory() || item.getTemplate().isTalisman() || item.getTemplate().isBracelet()) {
            player.sendUserInfo(true);
        } else {
            player.broadcastCharInfo();
        }
    }

    @Override
    public void onEquip(final int slot, final ItemInstance item, final Playable actor) {
        if (!item.isEquipable()) {
            return;
        }
        final Player player = (Player) actor;
        if (item.isAccessory() || item.getTemplate().isTalisman() || item.getTemplate().isBracelet()) {
            player.sendUserInfo(true);
        } else {
            player.broadcastCharInfo();
        }
    }
}
