package ru.j2dev.dataparser.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.dataparser.annotations.array.IntArray;
import ru.j2dev.dataparser.annotations.value.IntValue;

/**
 * @author : Camelion
 * @date : 24.08.12 21:39
 */
public class ArmorEnchantBonusDataHolder extends AbstractHolder {
    private static final ArmorEnchantBonusDataHolder ourInstance = new ArmorEnchantBonusDataHolder();
    @IntValue
    private int onepiece_factor; // неизвестно
    @IntArray
    private int[] bonus_grade_none; // Бонус от заточки No-grade предметов
    @IntArray
    private int[] bonus_grade_d; // Бонус от заточки D-grade предметов
    @IntArray
    private int[] bonus_grade_c; // Бонус от заточки C-grade предметов
    @IntArray
    private int[] bonus_grade_b; // Бонус от заточки B-grade предметов
    @IntArray
    private int[] bonus_grade_a; // Бонус от заточки A-grade предметов
    @IntArray
    private int[] bonus_grade_s; // Бонус от заточки S-grade предметов

    private ArmorEnchantBonusDataHolder() {
    }

    public static ArmorEnchantBonusDataHolder getInstance() {
        return ourInstance;
    }

    @Override
    public int size() {
        return bonus_grade_none.length + bonus_grade_d.length + bonus_grade_c.length + bonus_grade_b.length + bonus_grade_a.length + bonus_grade_s.length;
    }

    @Override
    public void clear() {
    }

    public final int getHpBonus(final int crystalType, final int enchantLevel, final boolean fullArmor) {
        if (enchantLevel == 0) {
            return 0;
        }

        int[] values = null;
        switch (crystalType) {
            case 0:
                values = bonus_grade_none;
                break;
            case 1:
                values = bonus_grade_d;
                break;
            case 2:
                values = bonus_grade_c;
                break;
            case 3:
                values = bonus_grade_b;
                break;
            case 4:
                values = bonus_grade_a;
                break;
            case 5:
                values = bonus_grade_s;
                break;
        }

        if (values == null || values.length == 0) {
            return 0;
        }

        double bonus = values[Math.min(enchantLevel, values.length) - 1];
        if (fullArmor) {
            bonus = (bonus * onepiece_factor / 100.0D);
        }

        return (int) bonus;
    }
}