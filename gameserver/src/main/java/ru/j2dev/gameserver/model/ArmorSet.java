package ru.j2dev.gameserver.model;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.items.Inventory;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.tables.SkillTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

public final class ArmorSet {
    private final int _set_id;
    private final List<Integer> _chest;
    private final List<Integer> _legs;
    private final List<Integer> _head;
    private final List<Integer> _gloves;
    private final List<Integer> _feet;
    private final List<Integer> _shield;
    private final List<Skill> _skills;
    private final List<Skill> _shieldSkills;
    private final List<Skill> _enchant6skills;

    public ArmorSet(final int set_id, final String[] chest, final String[] legs, final String[] head, final String[] gloves, final String[] feet, final String[] skills, final String[] shield, final String[] shield_skills, final String[] enchant6skills) {
        _chest = new ArrayList<>(1);
        _legs = new ArrayList<>(1);
        _head = new ArrayList<>(1);
        _gloves = new ArrayList<>(1);
        _feet = new ArrayList<>(1);
        _shield = new ArrayList<>(1);
        _skills = new ArrayList<>(1);
        _shieldSkills = new ArrayList<>(1);
        _enchant6skills = new ArrayList<>(1);
        _set_id = set_id;
        if (chest != null) {
            Arrays.stream(chest).map(Integer::parseInt).forEach(_chest::add);
        }
        if (legs != null) {
            Arrays.stream(legs).map(Integer::parseInt).forEach(_legs::add);
        }
        if (head != null) {
            Arrays.stream(head).map(Integer::parseInt).forEach(_head::add);
        }
        if (gloves != null) {
            Arrays.stream(gloves).map(Integer::parseInt).forEach(_gloves::add);
        }
        if (feet != null) {
            Arrays.stream(feet).map(Integer::parseInt).forEach(_feet::add);
        }
        if (shield != null) {
            Arrays.stream(shield).map(Integer::parseInt).forEach(_shield::add);
        }
        if (skills != null) {
            Arrays.stream(skills).map(skill -> new StringTokenizer(skill, "-")).forEach(st -> {
                if (st.hasMoreTokens()) {
                    final int skillId = Integer.parseInt(st.nextToken());
                    final int skillLvl = Integer.parseInt(st.nextToken());
                    _skills.add(SkillTable.getInstance().getInfo(skillId, skillLvl));
                }
                _skills.add(SkillTable.getInstance().getInfo(3006, 1));
            });
        }
        if (shield_skills != null) {
            Arrays.stream(shield_skills).map(skill -> new StringTokenizer(skill, "-")).filter(StringTokenizer::hasMoreTokens).forEach(st -> {
                final int skillId = Integer.parseInt(st.nextToken());
                final int skillLvl = Integer.parseInt(st.nextToken());
                _shieldSkills.add(SkillTable.getInstance().getInfo(skillId, skillLvl));
            });
        }
        if (enchant6skills != null) {
            Arrays.stream(enchant6skills).map(skill -> new StringTokenizer(skill, "-")).filter(StringTokenizer::hasMoreTokens).forEach(st -> {
                final int skillId = Integer.parseInt(st.nextToken());
                final int skillLvl = Integer.parseInt(st.nextToken());
                _enchant6skills.add(SkillTable.getInstance().getInfo(skillId, skillLvl));
            });
        }
    }

    public boolean containAll(final Player player) {
        final Inventory inv = player.getInventory();
        final ItemInstance chestItem = inv.getPaperdollItem(10);
        final ItemInstance legsItem = inv.getPaperdollItem(11);
        final ItemInstance headItem = inv.getPaperdollItem(6);
        final ItemInstance glovesItem = inv.getPaperdollItem(9);
        final ItemInstance feetItem = inv.getPaperdollItem(12);
        final int chest = 0;
        int legs = 0;
        int head = 0;
        int gloves = 0;
        int feet = 0;
        if (chestItem != null) {
            legs = chestItem.getItemId();
        }
        if (legsItem != null) {
            legs = legsItem.getItemId();
        }
        if (headItem != null) {
            head = headItem.getItemId();
        }
        if (glovesItem != null) {
            gloves = glovesItem.getItemId();
        }
        if (feetItem != null) {
            feet = feetItem.getItemId();
        }
        return containAll(chest, legs, head, gloves, feet);
    }

    public boolean containAll(final int chest, final int legs, final int head, final int gloves, final int feet) {
        return (!_chest.isEmpty() || _chest.contains(chest)) && (_legs.isEmpty() || _legs.contains(legs)) && (_head.isEmpty() || _head.contains(head)) && (_gloves.isEmpty() || _gloves.contains(gloves)) && (_feet.isEmpty() || _feet.contains(feet));
    }

    public boolean containItem(final int slot, final int itemId) {
        switch (slot) {
            case 10: {
                return _chest.contains(itemId);
            }
            case 11: {
                return _legs.contains(itemId);
            }
            case 6: {
                return _head.contains(itemId);
            }
            case 9: {
                return _gloves.contains(itemId);
            }
            case 12: {
                return _feet.contains(itemId);
            }
            default: {
                return false;
            }
        }
    }

    public int getSetById() {
        return _set_id;
    }

    public List<Integer> getChestItemIds() {
        return _chest;
    }

    public List<Skill> getSkills() {
        return _skills;
    }

    public List<Skill> getShieldSkills() {
        return _shieldSkills;
    }

    public List<Skill> getEnchant6skills() {
        return _enchant6skills;
    }

    public boolean containShield(final Player player) {
        final Inventory inv = player.getInventory();
        final ItemInstance shieldItem = inv.getPaperdollItem(8);
        return shieldItem != null && _shield.contains(shieldItem.getItemId());
    }

    public boolean containShield(final int shield_id) {
        return !_shield.isEmpty() && _shield.contains(shield_id);
    }

    public boolean isEnchanted6(final Player player) {
        if (!containAll(player)) {
            return false;
        }
        final Inventory inv = player.getInventory();
        final ItemInstance chestItem = inv.getPaperdollItem(10);
        final ItemInstance legsItem = inv.getPaperdollItem(11);
        final ItemInstance headItem = inv.getPaperdollItem(6);
        final ItemInstance glovesItem = inv.getPaperdollItem(9);
        final ItemInstance feetItem = inv.getPaperdollItem(12);
        return (_chest.isEmpty() || chestItem.getEnchantLevel() >= Config.ARMOR_ENCHANT_6_SKILL) && (_legs.isEmpty() || legsItem.getEnchantLevel() >= Config.ARMOR_ENCHANT_6_SKILL) && (_gloves.isEmpty() || glovesItem.getEnchantLevel() >= Config.ARMOR_ENCHANT_6_SKILL) && (_head.isEmpty() || headItem.getEnchantLevel() >= Config.ARMOR_ENCHANT_6_SKILL) && (_feet.isEmpty() || feetItem.getEnchantLevel() >= Config.ARMOR_ENCHANT_6_SKILL);
    }
}
