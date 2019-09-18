package ru.j2dev.gameserver.model.items.listeners;

import ru.j2dev.gameserver.data.xml.holder.ArmorSetsHolder;
import ru.j2dev.gameserver.listener.inventory.OnEquipListener;
import ru.j2dev.gameserver.model.ArmorSet;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.items.ItemInstance;

import java.util.ArrayList;
import java.util.List;

public final class ArmorSetListener implements OnEquipListener {
    private static final ArmorSetListener _instance = new ArmorSetListener();

    public static ArmorSetListener getInstance() {
        return _instance;
    }

    @Override
    public void onEquip(final int slot, final ItemInstance item, final Playable actor) {
        if (!item.isEquipable()) {
            return;
        }
        final Player player = (Player) actor;
        final ItemInstance chestItem = player.getInventory().getPaperdollItem(10);
        if (chestItem == null) {
            return;
        }
        final ArmorSet armorSet = ArmorSetsHolder.getInstance().getArmorSet(chestItem.getItemId());
        if (armorSet == null) {
            return;
        }
        boolean update = false;
        if (armorSet.containItem(slot, item.getItemId())) {
            if (armorSet.containAll(player)) {
                List<Skill> skills = armorSet.getSkills();
                for (final Skill skill : skills) {
                    player.addSkill(skill, false);
                    update = true;
                }
                if (armorSet.containShield(player)) {
                    skills = armorSet.getShieldSkills();
                    for (final Skill skill : skills) {
                        player.addSkill(skill, false);
                        update = true;
                    }
                }
                if (armorSet.isEnchanted6(player)) {
                    skills = armorSet.getEnchant6skills();
                    for (final Skill skill : skills) {
                        player.addSkill(skill, false);
                        update = true;
                    }
                }
            }
        } else if (armorSet.containShield(item.getItemId()) && armorSet.containAll(player)) {
            final List<Skill> skills = armorSet.getShieldSkills();
            for (final Skill skill : skills) {
                player.addSkill(skill, false);
                update = true;
            }
        }
        if (update) {
            player.sendSkillList();
            player.updateStats();
        }
    }

    @Override
    public void onUnequip(final int slot, final ItemInstance item, final Playable actor) {
        if (!item.isEquipable()) {
            return;
        }
        final Player player = (Player) actor;
        boolean remove = false;
        List<Skill> removeSkillId1 = new ArrayList<>(1);
        List<Skill> removeSkillId2 = new ArrayList<>(1);
        List<Skill> removeSkillId3 = new ArrayList<>(1);
        if (slot == 10) {
            final ArmorSet armorSet = ArmorSetsHolder.getInstance().getArmorSet(item.getItemId());
            if (armorSet == null) {
                return;
            }
            remove = true;
            removeSkillId1 = armorSet.getSkills();
            removeSkillId2 = armorSet.getShieldSkills();
            removeSkillId3 = armorSet.getEnchant6skills();
        } else {
            final ItemInstance chestItem = player.getInventory().getPaperdollItem(10);
            if (chestItem == null) {
                return;
            }
            final ArmorSet armorSet2 = ArmorSetsHolder.getInstance().getArmorSet(chestItem.getItemId());
            if (armorSet2 == null) {
                return;
            }
            if (armorSet2.containItem(slot, item.getItemId())) {
                remove = true;
                removeSkillId1 = armorSet2.getSkills();
                removeSkillId2 = armorSet2.getShieldSkills();
                removeSkillId3 = armorSet2.getEnchant6skills();
            } else if (armorSet2.containShield(item.getItemId())) {
                remove = true;
                removeSkillId2 = armorSet2.getShieldSkills();
            }
        }
        boolean update = false;
        if (remove) {
            for (final Skill skill : removeSkillId1) {
                player.removeSkill(skill, false);
                update = true;
            }
            for (final Skill skill : removeSkillId2) {
                player.removeSkill(skill);
                update = true;
            }
            for (final Skill skill : removeSkillId3) {
                player.removeSkill(skill);
                update = true;
            }
        }
        if (update) {
            player.sendSkillList();
            player.updateStats();
        }
    }
}
