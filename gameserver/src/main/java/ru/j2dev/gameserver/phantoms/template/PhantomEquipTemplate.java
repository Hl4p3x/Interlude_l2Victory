package ru.j2dev.gameserver.phantoms.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.templates.item.ItemGrade;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhantomEquipTemplate {
    private static final Logger log = LoggerFactory.getLogger(PhantomEquipTemplate.class);

    private final Map<ItemGrade, List<Integer>> weapon;
    private final Map<ItemGrade, List<Integer>> shield;
    private final Map<ItemGrade, List<PhantomArmorTemplate>> armor;
    private int classId;

    public PhantomEquipTemplate() {
        weapon = new HashMap<>();
        shield = new HashMap<>();
        armor = new HashMap<>();
    }

    public void addWeaponList(final ItemGrade itemGrade, final List<Integer> weapons) {
        weapon.put(itemGrade, weapons);
    }

    public void addArmorList(final ItemGrade itemGrade, final List<PhantomArmorTemplate> armors) {
        armor.put(itemGrade, armors);
    }

    public void addShieldList(final ItemGrade itemGrade, final List<Integer> shields) {
        shield.put(itemGrade, shields);
    }

    public int getRandomWeaponId(final ItemGrade itemGrade) {
        final List<Integer> weapons = weapon.get(itemGrade);
        if (weapons == null || weapons.size() == 0) {
            log.warn("Can't find weapon with grade: " + itemGrade + " for class id: " + classId);
            return 0;
        }
        return weapons.get(Rnd.get(weapons.size()));
    }

    public PhantomArmorTemplate getRandomArmor(final ItemGrade itemGrade) {
        final List<PhantomArmorTemplate> sets = armor.get(itemGrade);
        if (sets == null || sets.size() == 0) {
            log.warn("Can't find armor with grade: " + itemGrade + " for class id: " + classId);
            return null;
        }
        return sets.get(Rnd.get(sets.size()));
    }

    public int getRandomShieldId(final ItemGrade itemGrade) {
        final List<Integer> shields = shield.get(itemGrade);
        if (shields == null || shields.size() == 0) {
            return 0;
        }
        return shields.get(Rnd.get(shields.size()));
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(final int classId) {
        this.classId = classId;
    }
}
