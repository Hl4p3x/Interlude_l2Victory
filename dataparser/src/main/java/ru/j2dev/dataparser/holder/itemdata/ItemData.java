package ru.j2dev.dataparser.holder.itemdata;

import org.apache.commons.lang3.ArrayUtils;
import ru.j2dev.dataparser.annotations.array.EnumArray;
import ru.j2dev.dataparser.annotations.array.IntArray;
import ru.j2dev.dataparser.annotations.array.ObjectArray;
import ru.j2dev.dataparser.annotations.array.StringArray;
import ru.j2dev.dataparser.annotations.value.*;
import ru.j2dev.dataparser.holder.itemdata.item.ec_cond.DefaultEquipCond;
import ru.j2dev.dataparser.holder.itemdata.item.use_cond.DefaultUseCond;

import static ru.j2dev.dataparser.holder.ItemDataHolder.*;

/**
 * @author : Camelion
 * @date : 27.08.12 17:11
 */
public class ItemData {
    @IntValue(withoutName = true)
    public int itemId; // ID предмета
    @StringValue(withoutName = true)
    public String item_name; // Название предмета
    @EnumValue
    public ItemType item_type;
    @EnumValue
    public ArmorType armor_type;
    @EnumValue
    public WeaponType weapon_type;
    @EnumValue
    public EtcitemType etcitem_type;
    @ObjectValue
    public SlotBitType slot_bit_type;
    @IntValue
    public int delay_share_group;
    @StringArray
    public String[] item_multi_skill_list;
    @IntValue
    public int recipe_id;
    @IntValue
    public int blessed;
    @IntValue
    public int weight; // Масса предмета
    @EnumValue
    public ItemAction default_action; // Действие, выполняемое при двойном
    // щелчке по предмету
    @EnumValue
    public ConsumeType consume_type;
    @IntValue
    public int initial_count;
    @IntValue
    public int soulshot_count;
    @IntValue
    public int spiritshot_count;
    @IntArray
    public int[] reduced_soulshot = ArrayUtils.EMPTY_INT_ARRAY;
    @IntArray
    public int[] reduced_spiritshot = ArrayUtils.EMPTY_INT_ARRAY;
    @IntArray
    public int[] reduced_mp_consume = ArrayUtils.EMPTY_INT_ARRAY;
    @IntValue
    public int immediate_effect;
    @IntValue
    public int ex_immediate_effect;
    @IntValue
    public int drop_period;
    @IntValue
    public int duration;
    @IntValue
    public int use_skill_distime;
    @IntValue
    public int period;
    @IntValue
    public int equip_reuse_delay;
    @IntValue
    public int price;
    @IntValue
    public int default_price;
    @StringValue
    public String item_skill;
    @StringValue
    public String critical_attack_skill;
    @StringValue
    public String attack_skill;
    @StringValue
    public String magic_skill;
    @StringValue
    public String item_skill_enchanted_four;
    @ObjectArray
    public CapsuledItem[] capsuled_items = new CapsuledItem[0];
    @EnumValue
    public MaterialType material_type;
    @EnumValue
    public CrystalType crystal_type;
    @IntValue
    public int crystal_count;
    @IntValue
    public int is_trade; // Можно-ли продать (1=true)
    @IntValue
    public int is_drop; // Можно-ли выкинуть
    @IntValue
    public int is_destruct; // Можно-ли разрушить
    @IntValue
    public int is_private_store; // Можно-ли выставить в личной лавке
    @IntValue
    public int keep_type; // Неизвестно
    @IntValue
    public int physical_damage; // P.Dam
    @IntValue
    public int random_damage; //
    @IntValue
    public int magical_damage; // M.Dam
    @IntValue
    public int critical;
    @DoubleValue
    public double hit_modify;
    @IntValue
    public int avoid_modify;
    @IntValue
    public int dual_fhit_rate;
    @IntValue
    public int shield_defense;
    @IntValue
    public int shield_defense_rate;
    @IntValue
    public int attack_range;
    @IntArray()
    public int[] damage_range;
    @IntValue
    public int attack_speed;
    @IntValue
    public int reuse_delay;
    @IntValue
    public int mp_consume;
    @IntValue
    public int durability;
    @IntValue
    public int damaged;
    @IntValue
    public int physical_defense; // P.Def
    @IntValue
    public int magical_defense; // M.Def
    @IntValue
    public int mp_bonus;
    @IntArray()
    public int[] category = ArrayUtils.EMPTY_INT_ARRAY; // Неизвестно, всегда
    // пустой
    @IntValue
    public int enchanted;
    @ObjectValue
    public AttackAttribute base_attribute_attack; // Аттрибут атаки
    @IntArray
    public int[] base_attribute_defend; // Аттрибут защиты
    @StringValue
    public String html;
    @IntValue
    public int magic_weapon;
    @IntValue
    public int enchant_enable;
    @IntValue
    public int elemental_enable;
    @StringArray
    public String[] unequip_skill = new String[0];
    @IntValue
    public int for_npc;
    @EnumArray
    public ItemEquipOption[] item_equip_option = new ItemEquipOption[0];
    @ObjectArray(objectFactory = UseCondObjectFactory.class)
    public DefaultUseCond[] use_condition = new DefaultUseCond[0];
    @ObjectArray(objectFactory = EquipCondObjectFactory.class)
    public DefaultEquipCond[] equip_condition = new DefaultEquipCond[0];
    @IntValue
    public int is_olympiad_can_use;
    @IntValue
    public int can_move;
    @IntValue
    public int is_premium;

    public enum ItemEquipOption {
        no_attack,
        only_use_weapon_skill,
        force_equip
    }

    public enum DefendAttribute {
        none,
        fire,
        water,
        wind,
        earth,
        holy,
        unholy
    }

    public enum CrystalType {
        none,
        d,
        c,
        b,
        a,
        s,
        s80,
        s84,
        crystal_free,
        event
    }

    public enum MaterialType {
        steel,
        fine_steel,
        wood,
        bone,
        bronze,
        leather,
        cloth,
        fish,
        gold,
        mithril,
        liquid,
        oriharukon,
        damascus,
        adamantaite,
        blood_steel,
        paper,
        silver,
        chrysolite,
        crystal,
        horn,
        scale_of_dragon,
        cotton,
        dyestuff,
        cobweb,
        rune_xp,
        rune_sp,
        rune_remove_penalty
    }

    public enum ConsumeType {
        consume_type_normal,
        consume_type_stackable,
        consume_type_asset
    }

    public enum ItemAction {
        action_none,
        action_equip,
        action_peel,
        action_skill_reduce,
        action_skill_reduce_new, //TODO[K] - временный экшн!
        action_soulshot,
        action_spiritshot,
        action_recipe,
        action_skill_maintain,
        action_dice,
        action_calc,
        action_seed,
        action_harvest,
        action_capsule,
        action_xmas_open,
        action_show_html,
        action_show_ssq_status,
        action_fishingshot,
        action_summon_soulshot,
        action_summon_spiritshot,
        action_call_skill,
        action_show_adventurer_guide_book,
        action_keep_exp,
        action_create_mpcc,
        action_nick_color,
        action_hide_name,
        action_start_quest
    }

    public enum ItemType {
        weapon,
        armor,
        etcitem,
        asset,
        accessary,
        questitem
    }

    public enum WeaponType {
        none,
        sword,
        blunt,
        dagger,
        bow,
        pole,
        dual,
        etc,
        fist,
        dualfist,
        fishingrod,
        rapier,
        ancientsword,
        crossbow,
        flag,
        ownthing,
        dualdagger
    }

    public enum ArmorType {
        none,
        light,
        heavy,
        magic,
        sigil
    }

    public enum EtcitemType {
        none,
        arrow,
        potion,
        scrl_enchant_wp,
        scrl_enchant_am,
        scroll,
        recipe,
        material,
        pet_collar,
        castle_guard,
        lotto,
        race_ticket,
        dye,
        seed,
        crop,
        maturecrop,
        harvest,
        seed2,
        ticket_of_lord,
        lure,
        bless_scrl_enchant_wp,
        bless_scrl_enchant_am,
        coupon,
        elixir,
        scrl_enchant_attr,
        bolt,
        scrl_inc_enchant_prop_wp,
        scrl_inc_enchant_prop_am,
        ancient_crystal_enchant_wp,
        ancient_crystal_enchant_am,
        rune_select,
        rune,
        teleportbookmark
    }

    public enum ItemSlotBitType {
        none(0x00000),
        underwear(0x00001),
        rear(0x00002),
        lear(0x00004),
        neck(0x00008),
        rfinger(0x00010),
        lfinger(0x00020),
        head(0x00040),
        rhand(0x00080),
        lhand(0x00100),
        gloves(0x00200),
        chest(0x00400),
        legs(0x00800),
        feet(0x01000),
        back(0x02000),
        lrhand(0x04000),
        onepiece(0x08000), // FULL_ARMOR
        hair(0x10000),
        alldress(0x20000),
        hair2(0x40000),
        hairall(0x80000),
        rbracelet(0x100000),
        lbracelet(0x200000),
        deco1(0x400000),
        waist(0x10000000); // Может быть 0x800000?
        public final int type;

        ItemSlotBitType(int type) {
            this.type = type;
        }
    }

    public static class AttackAttribute {
        @EnumValue(withoutName = true)
        public DefendAttribute element;
        @IntValue(withoutName = true)
        public int value;
    }

    public static class CapsuledItem {
        @StringValue(withoutName = true)
        public String item_name;
        @IntValue
        public int min;
        @IntValue
        public int max;
        @IntValue
        public int chance;
    }

    public static class SlotBitType {
        @EnumValue(withoutName = true)
        public ItemSlotBitType value;
    }
}