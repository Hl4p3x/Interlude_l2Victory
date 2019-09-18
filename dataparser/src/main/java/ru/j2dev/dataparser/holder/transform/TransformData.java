package ru.j2dev.dataparser.holder.transform;

import ru.j2dev.dataparser.annotations.Element;
import ru.j2dev.dataparser.annotations.array.DoubleArray;
import ru.j2dev.dataparser.annotations.array.IntArray;
import ru.j2dev.dataparser.annotations.array.LinkedArray;
import ru.j2dev.dataparser.annotations.array.ObjectArray;
import ru.j2dev.dataparser.annotations.value.EnumValue;
import ru.j2dev.dataparser.annotations.value.IntValue;
import ru.j2dev.dataparser.annotations.value.StringValue;
import ru.j2dev.dataparser.holder.transform.type.TransformType;

import java.util.List;

/**
 * @author : Mangol
 */
public class TransformData {
    @IntValue
    public int id; // ID трансформы
    @EnumValue
    public TransformType type; // Тип трансформы
    @IntValue
    public int can_swim; // Может ли находится в воде
    @IntValue
    public int spawn_height;
    @IntValue
    public int normal_attackable; // 1 означает что игрок приобретает неуязвимость
    @Element(start = "female_begin", end = "female_end")
    public List<female> female_begin;
    @Element(start = "male_begin", end = "male_end")
    public List<male> male_begin;

    public enum BaseAttackType {
        bow,
        sword,
        dual
    }

    public static class female {
        @Element(start = "common_begin", end = "common_end")
        public List<common> common_begin;
        @Element(start = "combat_begin", end = "combat_end")
        public List<combat> combat_begin;
    }

    public static class male {
        @Element(start = "common_begin", end = "common_end")
        public List<common> common_begin;
        @Element(start = "combat_begin", end = "combat_end")
        public List<combat> combat_begin;
    }

    public static class common {
        @DoubleArray
        public double[] collision_box; // Всегда имеет значние (Not null),Колизии
        @DoubleArray
        public double[] moving_speed;    // 0-Скорость ходьбы, 1-Скорость бега,
        // 2-Скорость ходьбы под водой, 3-Скорость
        // бега под водой 4-Скорость ходьбы в
        // полёте, 5-Скорость бега в полёте
        @LinkedArray
        public int[] skill;
        @ObjectArray
        public AdditionalSkill[] additional_skill = new AdditionalSkill[0];
        @IntArray
        public int[] action; // Список социальных действий которые будут видны/можно использовать
        @EnumValue
        public BaseAttackType base_attack_type;
        @IntValue
        public int base_attack_range; // Базовый радиус атаки
        @IntValue
        public int base_random_damage; // Базовый рандомный урон
        @IntValue
        public int base_attack_speed; // Базовая скорость атаки
        @IntValue
        public int base_critical_prob; // Базовый физ. шанс крита
        @IntValue
        public int base_physical_attack; // Базовая физ. атака
        @IntValue
        public int base_magical_attack; // Базовая маг. атака
        @ObjectArray
        public ItemCheck[] item_check = new ItemCheck[0];
    }

    public static class combat {
        @DoubleArray
        public double[] basic_stat; // str;int;con;dex;wit;men
        @DoubleArray
        public double[] base_defend; // Chest,Legs,Pitch,Boots,Glooves,Underwear,Cloak
        @DoubleArray
        public double[] base_magic_defend;
        @DoubleArray
        public double[] org_hp_regen;
        @DoubleArray
        public double[] org_mp_regen;
        @DoubleArray
        public double[] org_cp_regen;
        @DoubleArray
        public double[] level_bonus;
        @DoubleArray
        public double[] str_bonus;
        @DoubleArray
        public double[] int_bonus;
        @DoubleArray
        public double[] con_bonus;
        @DoubleArray
        public double[] dex_bonus;
        @DoubleArray
        public double[] wit_bonus;
        @DoubleArray
        public double[] men_bonus;
        @DoubleArray
        public double[] hp_table;
        @DoubleArray
        public double[] mp_table;
        @DoubleArray
        public double[] cp_table;
    }

    public static class AdditionalSkill {
        @IntValue(withoutName = true)
        public int level;
        @LinkedArray(withoutName = true)
        public int[] skillId;
    }

    public static class ItemCheck {
        @StringValue(withoutName = true)
        public String name;
        @LinkedArray(withoutName = true)
        public int[] itemId;
    }
}