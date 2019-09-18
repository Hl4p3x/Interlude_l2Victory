package ru.j2dev.dataparser.holder.skilldata;

import ru.j2dev.dataparser.annotations.value.*;
import ru.j2dev.dataparser.holder.skilldata.abnormal.AbnormalType;
import ru.j2dev.dataparser.holder.skilldata.abnormal.AbnormalVisualEffect;

/**
 * @author KilRoy
 * TODO[K] - нихуёвый такой тодошка на все время :) Куча инфы неизвестна, куча вообще непонятна...короче попадос тут полный
 */
public class SkillData {
    @StringValue(withoutName = true)
    public String skill_name; // Название скила
    @IntValue(withoutName = true)
    public int skill_id; // ID скила
    @IntValue
    public int level; // уровень скила
    @EnumValue
    public OperateType operate_type; // тайп юза скила
    @IntValue
    public int magic_level; // уровень с которого доступен данный скилл
    @StringValue(withoutBounds = true)
    public String self_effect; // какие эффекты будут наложены на себя
    @StringValue(withoutBounds = true)
    public String effect; // список эффектов
    @StringValue(withoutBounds = true)
    public String pvp_effect; // список эффектов
    @StringValue(withoutBounds = true)
    public String pve_effect; // список эффектов
    @StringValue(withoutBounds = true)
    public String start_effect;
    @StringValue(withoutBounds = true)
    public String tick_effect;
    @StringValue(withoutBounds = true)
    public String end_effect; // эффект налаживается в конце основного
    @StringValue(withoutBounds = true)
    public String fail_effect;
    @StringValue(withoutBounds = true)
    public String operate_cond; // условия использования скила
    @StringValue(withoutBounds = true)
    public String passive_conditions; // условия использования скила
    @StringValue(withoutBounds = true)
    public String target_operate_cond; // условия в котором должна находится цель для юза скила
    @IntValue
    public int tick_interval;
    @ObjectValue
    public AttachedSkill attached_skill;
    @IntValue
    public int is_magic; // магический ли скил?
    @IntValue
    public int mp_consume1; // потребление МП нужное для каста скила(начало каста)
    @IntValue
    public int mp_consume2; // потребление МП за использование скила
    @IntValue
    public int mp_consume_tick;
    @IntValue
    public int hp_consume; // количество потребляемого ХП при касте скила
    @ObjectValue
    public ItemConsume item_consume;
    @ObjectValue
    public ConsumeEtc consume_etc;
    @IntValue
    public int cast_range; // растояние каста скила
    @IntValue
    public int effective_range; // дальность эффективной аттаки
    @DoubleValue
    public double skill_hit_time; // время каста скила
    @DoubleValue
    public double skill_cool_time; // время задержки после каста скила
    @DoubleValue
    public double skill_hit_cancel_time; // время в течении которого возможна отмена каста скила
    @IntValue
    public int reuse_delay; // время задержки до повторного использования скила
    @IntValue
    public int reuse_delay_lock; // флаг ставится в том случае, если у скила фиксированное время отката скила(не зависят от статов итемов и бафов)
    @StringValue
    public String reuse_delay_type;
    @IntValue
    public int activate_rate; // шанс срабатывания скилла
    @IntValue
    public int lv_bonus_rate; // дополнительный бонус к модификатору шанса от маг. уровня скилла(он же lvlDependMod в явах)
    @EnumValue
    public BasicProperty basic_property; // основной модификатор для скилла (con, men, dex и т.п.)
    @IntValue
    public int abnormal_time; // время действия налаживаемого эффекта.
    @IntValue
    public int abnormal_lv; // как и abnormal_type служит для проверки замены или стыковки эффектов (аналог стакТайпа на явах)
    @EnumValue
    public AbnormalType abnormal_type; // тип налаживаемого эффекта (в случае офф сборки определяет именно визуальный вид и для проверку стыковки эффектов)
    @IntValue
    public int abnormal_instant; // как вариант может указывает на то что после окончания херба надо восстановтить обратно тот бафф, который херб заменил
    @IntValue
    public int irreplaceable_buff; // мейби указывает что данный скилл заменить им же или другим того же типа нельзя т.е. то что у нас(ява) по идее делает стэкОрдер -1
    @IntValue
    public int buff_protect_level; // предположительно флаг, для сохранения данного скила(юзанули хаст, поверх нубоХаст. Нубо хаст кончился, восстановился старый хаст)
    @IntValue
    public int abnormal_delete_leaveworld;
    @ObjectValue
    public Attribute attribute; // аттрибут скила.
    @ObjectValue
    public Trait trait; // тип скилла для резистов, в эффектах обычно указывается трейт атакующий или защитный. К примеру в основных эффектах указывается p_defence_trait;trait_bleed или p_attack_trait;trait_bleed то значит скилл с трейтом trait_bleed будет участвовать в расчете шанса прохождения. Т.е. можно сказать это тип дебаффа. Но у корейцев трейты используются так же и для защиты от оружия.
    @IntValue
    public int effect_point; // очки агра на мобов
    @EnumValue
    public TargetType target_type; // доступный тип цели
    @ObjectValue
    public AffectScopeHeight affect_scope_height;
    @EnumValue
    public AffectScope affect_scope; // тип выбора цели(говорят так по крайней мере)
    @IntValue
    public int affect_range; // радиус действия массового скилла, не важно, бафф, хил, атака итд
    @EnumValue
    public AffectObject affect_object; // по какому типу будет выбираться цель, к примеру not_friend - будут браться все враждебные цели
    @ObjectValue
    public AffectLimit affect_limit; // мин и макс количество целей на действие скила
    @ObjectValue
    public FanRange fan_range;
    @EnumValue
    public NextAction next_action; // действие выполняемое после завершения обработки скила
    @ObjectValue
    public TransformType transform_type; // действие выполняемое после завершения обработки скила
    @ObjectValue
    public VisualEffect abnormal_visual_effect; // указатель на абнормал(эффект).
    @IntValue
    public int debuff; // является ли скилл дебаффом
    @ObjectValue
    public Ride ride_state; // флаг на возможность использование скила при моунте пета(верхом на пете)
    @IntValue
    public int multi_class; // всегда 0, кроме хиро скилов
    @IntValue
    public int olympiad_use; // возможность использования на олимпиаде
    @IntValue
    public int npc_notice;
    @IntValue
    public int block_action_use_skill;


	/* new parameters GoD GL
    	special_level
		magic_critical_rate
		change_skill_id
		is_double
		is_hate
		subordination_abnormal
		undeletable
		channeling_start
		pledgegame_use
		reuse_delay_group
		without_action
		no_transfer_abnormal
		resist_cast
		event_type
		alter_skill_list
		pledge_nv_consume
		is_necessarytg
		attach_toggle_id
		attach_toggle_list
		double_cast_skill
		reduceskill
		pvp_point_consume
	*/

    public enum AffectObject {
        all,
        friend,
        not_friend,
        clan,
        undead_real_enemy,
        object_dead_npc_body,
        invisible,
        noe,
        hidden_place,
        wyvern_object
    }

    public enum BasicProperty {
        none,
        str,
        con,
        men
    }

    public enum OperateType {
        A1, //active skill
        A2, //active skill
        A3, //active skill
        A4, //active skill
        P, //passive skill
        T, //trigger
        CA1, //простой скил продолжительного воздействия
        CA5, //скил продолжительного воздействия, использует attached_skill как налаживаемый скил на цель, через время tick_interval производится повторное воздействие attached_skill и усиление n-го, то есть возрастает уровень скила на 1 и так далее до истечения времени произношения скила.
        DA1, //простые рывки с нанесением урона
        DA2 //рывки с нанесением урона и наложение последующих эффектов на врагов(стан, кровотечении)
    }

    public enum TargetType {
        none,
        self,
        summon,
        target,
        enemy,
        enemy_not,
        enemy_only,
        pc_body,
        npc_body,
        holything,
        ground,
        advance_base,
        fortress_flagpole,
        artillery,
        door_treasure,
        wyvern_target,
        item,
        others
    }

    public enum AffectScope {
        none,
        single,
        party,
        pledge,
        range,
        ring_range,
        fan,
        square,
        square_pb,
        party_pledge,
        dead_pledge,
        point_blank,
        static_object_scope,
        wyvern_scope,
        balakas_scope
    }

    public enum NextAction {
        none,
        attack,
        sit,
        fake_death
    }

    public enum DefaultAttribute {
        attr_none,
        attr_fire,
        attr_water,
        attr_wind,
        attr_earth,
        attr_holy,
        attr_unholy
    }

    public enum TransformTypeState {
        tt_none,
        tt_flying
    }

    public enum RideState {
        ride_none,
        ride_wolf,
        ride_strider,
        ride_wyvern
    }

    public static class VisualEffect {
        @EnumValue(withoutName = true)
        public AbnormalVisualEffect ave;
    }

    public static class TransformType {
        @EnumValue(withoutName = true)
        public TransformTypeState type;
    }

    public static class Ride {
        @EnumValue(withoutName = true)
        public RideState ride;
    }

    public static class Attribute {
        @EnumValue(withoutName = true)
        public DefaultAttribute attr;
        @IntValue(withoutName = true)
        public int value;
    }

    public static class Trait {
        @EnumValue(withoutName = true)
        public TraitData trait;
    }

    public static class FanRange {
        @IntValue(withoutName = true)
        public int x;
        @IntValue(withoutName = true)
        public int y;
        @IntValue(withoutName = true)
        public int range;
        @IntValue(withoutName = true)
        public int height;
    }

    public static class ItemConsume {
        @StringValue(withoutName = true)
        public String itemName;
        @IntValue(withoutName = true)
        public int amount;
    }

    public static class ConsumeEtc {
        @IntValue(withoutName = true)
        public int forceAmount;
        @IntValue(withoutName = true)
        public int soulAmount;
    }

    public static class AffectScopeHeight {
        @IntValue(withoutName = true)
        public int min;
        @IntValue(withoutName = true)
        public int max;
    }

    public static class AffectLimit {
        @IntValue(withoutName = true)
        public int min;
        @IntValue(withoutName = true)
        public int max;
    }

    public static class AttachedSkill {
        @StringValue(withoutName = true)
        String name;
    }
}