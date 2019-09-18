package ru.j2dev.gameserver.model.items.listeners;

import ru.j2dev.gameserver.data.xml.holder.OptionDataHolder;
import ru.j2dev.gameserver.listener.inventory.OnEquipListener;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.stats.triggers.TriggerInfo;
import ru.j2dev.gameserver.templates.OptionDataTemplate;

public final class ItemEnchantOptionsListener implements OnEquipListener {
    private static final ItemEnchantOptionsListener _instance = new ItemEnchantOptionsListener();

    public static ItemEnchantOptionsListener getInstance() {
        return _instance;
    }

    @Override
    public void onEquip(final int slot, final ItemInstance item, final Playable actor) {
        if (!item.isEquipable()) {
            return;
        }
        final Player player = actor.getPlayer();
        boolean needSendInfo = false;
        for (final int i : item.getEnchantOptions()) {
            final OptionDataTemplate template = OptionDataHolder.getInstance().getTemplate(i);
            if (template != null) {
                player.addStatFuncs(template.getStatFuncs(template));
                for (final Skill skill : template.getSkills()) {
                    player.addSkill(skill, false);
                    needSendInfo = true;
                }
                for (final TriggerInfo triggerInfo : template.getTriggerList()) {
                    player.addTrigger(triggerInfo);
                }
            }
        }
        if (needSendInfo) {
            player.sendSkillList();
        }
        player.sendChanges();
    }

    @Override
    public void onUnequip(final int slot, final ItemInstance item, final Playable actor) {
        if (!item.isEquipable()) {
            return;
        }
        final Player player = actor.getPlayer();
        boolean needSendInfo = false;
        for (final int i : item.getEnchantOptions()) {
            final OptionDataTemplate template = OptionDataHolder.getInstance().getTemplate(i);
            if (template != null) {
                player.removeStatsOwner(template);
                for (final Skill skill : template.getSkills()) {
                    player.removeSkill(skill, false);
                    needSendInfo = true;
                }
                for (final TriggerInfo triggerInfo : template.getTriggerList()) {
                    player.removeTrigger(triggerInfo);
                }
            }
        }
        if (needSendInfo) {
            player.sendSkillList();
        }
        player.sendChanges();
    }
}
