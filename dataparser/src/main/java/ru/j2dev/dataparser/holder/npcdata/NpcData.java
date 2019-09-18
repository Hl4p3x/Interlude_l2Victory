package ru.j2dev.dataparser.holder.npcdata;

import ru.j2dev.dataparser.annotations.array.DoubleArray;
import ru.j2dev.dataparser.annotations.array.IntArray;
import ru.j2dev.dataparser.annotations.array.LinkedArray;
import ru.j2dev.dataparser.annotations.array.ObjectArray;
import ru.j2dev.dataparser.annotations.value.*;
import ru.j2dev.dataparser.holder.itemdata.ItemData;

import java.util.Map;

import static ru.j2dev.dataparser.holder.NpcDataHolder.NpcAIObjectFactory;

/**
 * @author : Camelion
 * @date : 30.08.12 14:45
 */
public class NpcData {
    @IntValue(withoutName = true)
    public int npc_class_id;
    @StringValue(withoutName = true)
    public String npc_name;
    @ObjectArray
    public Object[] category = new Object[0]; // Всегда пустой
    @IntValue
    public int level;
    @LongValue
    public long exp;
    @IntValue
    public int ex_crt_effect;
    @IntValue
    public int unique;
    @IntValue
    public int s_npc_prop_hp_rate;
    @EnumValue
    public NpcRace race;
    @EnumValue
    public NpcSex sex;
    @LinkedArray
    public int[] skill_list;
    @StringValue
    public String slot_chest; // Может быть пустой строкой
    @StringValue
    public String slot_rhand; // Может быть пустой строкой
    @StringValue
    public String slot_lhand; // Может быть пустой строкой
    @DoubleArray(canBeNull = false)
    public double[] collision_radius; // {Вперед-назад:влево-вправо}
    @DoubleArray(canBeNull = false)
    public double[] collision_height;
    @DoubleValue
    public double hit_time_factor;
    @DoubleValue
    public double hit_time_factor_skill;
    @DoubleArray(canBeNull = false)
    public double[] ground_high;
    @DoubleArray(canBeNull = false)
    public double[] ground_low;
    @IntValue
    public int str;
    @IntValue(name = "int")
    public int _int;
    @IntValue
    public int dex;
    @IntValue
    public int wit;
    @IntValue
    public int con;
    @IntValue
    public int men;
    @DoubleValue
    public double org_hp;
    @DoubleValue
    public double org_hp_regen;
    @DoubleValue
    public double org_mp;
    @DoubleValue
    public double org_mp_regen;
    @EnumValue
    public ItemData.WeaponType base_attack_type; // Всегда имеет значние, (Not null)
    @IntValue
    public int base_attack_range;
    @IntArray
    public int[] base_damage_range; // Всегда имеет значние (Not null)
    @IntValue
    public int base_rand_dam;
    @DoubleValue
    public double base_physical_attack;
    @DoubleValue
    public double base_magic_attack;
    @DoubleValue
    public double base_critical;
    @DoubleValue
    public double physical_hit_modify;
    @IntValue
    public int base_attack_speed;
    @IntValue
    public int base_reuse_delay;
    @DoubleValue
    public double base_defend;
    @DoubleValue
    public double base_magic_defend;
    @ObjectValue
    public ItemData.AttackAttribute base_attribute_attack; // Всегда имеет како-либо значение (Not null)
    @IntArray
    public int[] base_attribute_defend; // Аттрибуты защиты, поле всегда имеет какое-либо значение (Not null)
    @IntValue
    public int physical_avoid_modify;
    @IntValue
    public int shield_defense_rate;
    @DoubleValue
    public double shield_defense;
    @IntValue
    public int safe_height; // Вероятно, безопасная высота падения
    @IntValue
    public int soulshot_count;
    @LinkedArray
    public int[] clan = new int[0]; // Дружественные NPC кланы
    @LinkedArray
    public int[] ignore_clan_list = new int[0]; // Игнорируемые NPC в клане
    @IntValue
    public int clan_help_range; // Радиус помощи дружественному клану
    @IntValue
    public int undying;
    @IntValue
    public int can_be_attacked; // Можно ли атаковать этого NPC
    @IntValue
    public int corpse_time; // Время, в течении которого труп NPC будет лежать
    // на земле
    @IntValue
    public int no_sleep_mode; // Время, в течении которого труп NPC будет лежать
    // на земле
    @IntValue
    public int agro_range; // Радиус агрессивности
    @IntValue
    public int passable_door;
    @IntValue
    public int can_move; // Может ли передвиграться
    @IntValue
    public int flying; // 1 для летающих NPC
    @IntValue
    public int has_summoner;
    @IntValue
    public int targetable; // Можно ли взять в таргет
    @IntValue
    public int show_name_tag; // 1 - имя NPC отображается, 0 - нет
    @IntValue
    public int unsowing; // Битовая маска, Возможно, 1 - агрессивен, 0 - нет
    @IntValue
    public int private_respawn_log;
    @ObjectValue(objectFactory = NpcAIObjectFactory.class)
    public NpcDataAI npc_ai;
    @ObjectValue
    public EventFlag event_flag; // Всегда имеет значение, (0 либо 1) Не может
    // быть Null
    @DoubleValue
    public double acquire_exp_rate; // Рейт EXP, используемый при расчете опыта
    // за убийство монстра
    @IntValue
    public int acquire_sp; // Кол-во SP за убийство монстра
    @IntValue
    public int acquire_rp; // Кол-во очков репутации за убийство монстра
    @ObjectArray
    public DropInfo[] corpse_make_list = new DropInfo[0]; // Спойл с монстра (Не
    // групповой расчет)
    @ObjectArray
    public DropInfo[] additional_make_list = new DropInfo[0]; // Обычный дроп с
    // монстра (Не
    // групповой
    // расчет шанса)
    @ObjectArray
    public DropGroup[] additional_make_multi_list = new DropGroup[0]; // Обычный
    // дроп
    // с
    // монстра
    // (Групповой
    // расчет
    // шанса)
    @ObjectArray
    public DropGroup[] ex_item_drop_list = new DropGroup[0]; // Дроп Herb'ов
    // (Групповой
    // расчет)
    @IntValue
    public int fake_class_id; // если != -1, то в пакете NpcInfo вместо
    // npc_class_id должен использоваться этот
    // class_id

    public enum NpcRace {
        fairy,
        humanoid,
        animal,
        undead,
        beast,
        plant,
        bug,
        construct,
        elemental,
        demonic,
        giant,
        dragon,
        divine,
        human,
        kamael,
        elf,
        etc,
        dwarf,
        mercenary,
        orc,
        darkelf,
        castle_guard,
        siege_weapon
    }

    public enum NpcSex {
        male,
        female,
        etc
    }

    // Заполняется через NpcAIObjectFactory
    public static class NpcDataAI {
        public final String ai_name;
        public final Map<String, Object> params;

        public NpcDataAI(String ai_name, Map<String, Object> params) { // Конструктор для NpcAIObjectFactory
            this.ai_name = ai_name;
            this.params = params;
        }
    }

    public static class DropGroup {
        @ObjectArray(withoutName = true)
        public DropInfo[] drops;
        @DoubleValue(withoutName = true)
        public double group_chance;
    }

    public static class DropInfo {
        @StringValue(withoutName = true)
        public String item_name;
        @IntValue(withoutName = true)
        public int min;
        @IntValue(withoutName = true)
        public int max;
        @DoubleValue(withoutName = true)
        public double chance;
    }

    public static class EventFlag {
        @IntValue(withoutName = true)
        public int mask; // Битовая маска, Возможно, 1 - агрессивен, 0 - нет
    }

    public static class NpcDataPrivateInfo {
        @StringValue(withoutName = true)
        public String npc_name;
        @StringValue(withoutName = true)
        public String ai_type;
        @IntValue(withoutName = true)
        public int count;
        @IntValue(withoutName = true)
        public int unk;
    }
}
