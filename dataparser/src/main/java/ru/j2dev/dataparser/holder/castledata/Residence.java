package ru.j2dev.dataparser.holder.castledata;

import ru.j2dev.dataparser.annotations.array.EnumArray;
import ru.j2dev.dataparser.annotations.array.ObjectArray;
import ru.j2dev.dataparser.annotations.array.StringArray;
import ru.j2dev.dataparser.annotations.value.*;
import ru.j2dev.dataparser.common.Point3;
import ru.j2dev.dataparser.common.Point3WithHeading;
import ru.j2dev.dataparser.common.Point4;
import ru.j2dev.dataparser.common.Territory;

import java.util.Date;

/**
 * @author : Camelion
 * @date : 25.08.12 23:15
 */
public class Residence {
    @StringValue(withoutName = true)
    public String residence_name; // Название
    @IntValue(withoutName = true)
    public int residence_id; // ID
    @EnumValue(withoutName = true)
    public GetType get_type; // Тип получения, осада, битва с npc (для осадных
    // КХ), аукцион
    @ObjectValue
    public Territory residence_territory; // Территория резиденции
    @ObjectValue
    public Territory battlefield_territory; // Боевая территория?
    @ObjectValue
    public Territory headquarter_territory; // Территория штаб-квартиры?
    @ObjectArray
    public Point3[] owner_restart_point_list; // Возможно, cписок точек, в
    // которых появляются владельцы
    // резиденции после смерти
    @ObjectArray
    public Point3[] other_restart_village_list; // Неизвестно
    @ObjectArray
    public Point3[] chao_restart_point_list; // Неизвестно
    @ObjectArray
    public Point3[] banish_point_list; // Неизвестно
    @ObjectArray
    public ResidenceGuardMap[] residence_guard_mapping; // Неизвестно, какие-то
    // нпс, есть зависимость
    // от выигравшей печати
    @ObjectArray
    public ResidenceNpc[] owning_npc_list; // Список каких-то НПС
    @StringArray
    public String[] boss_npc_list; // Список каких-то НПС, присутствует у
    // devastated_castle
    @DateValue
    public Date next_siege; // Дата следующей осады
    @IntValue
    public int siege_term; // Продолжительность осады (в минутах)
    @EnumValue
    public ResidenceState residence_state; // флаг, означающий состояние
    // резиденции. (в офф скриптах везде
    // initial)
    @ObjectArray
    public ResidenceGate[] residence_gate_list;
    @StringArray
    public String[] control_tower_list; // контрольные башни?
    @ObjectArray
    public ResidenceHolything[] holything; // Вероятно, артефакты
    @IntValue
    public int tax_rate; // Налог (в процентах)
    @IntValue
    public int tax_sysget_rate; // Неизвестно
    @IntValue
    public int tribute_rate; // Неизвестно
    @IntValue
    public int agit_auction_min; // Минимальная цена на аукционе
    @IntValue
    public int agit_deposit; // Неизвестно
    @IntValue
    public int agit_lease; // Неизвестно
    @IntValue
    public int agit_size; // Неизвестно
    @IntValue
    public int agit_parent_castleid; // ID резиденции, к которой привязана
    // данная резиденция (-1 если
    // непривязана ни к чему, 0 для Замка
    // Аден)
    @IntValue
    public int agit_auction; // 1 - резиденция включена в аукцион, 0 - нет
    @DateValue(format = "yyyy/MM/dd/HH")
    public Date next_auction; // Дата следующего аукциона
    @ObjectArray
    public Point4[] owner_restart_territory;
    @ObjectArray
    public Point3WithHeading[] battle_royal_flag_point_list; // Список каких-то
    // точек у
    // beast_farm_guildhall,
    // и
    // bandits'_stronghold
    @ObjectArray
    public Point3WithHeading[] final_flag_point_list; // Список каких-то точек у
    // beast_farm_guildhall,
    // и bandits'_stronghold
    @StringArray
    public String[] final_open_door_list; // Список дверей, активен у
    // beast_farm_guildhall, и
    // bandits'_stronghold
    @ObjectArray
    public Point3[] challenger_restart_point_list; // Неизвестно
    @IntValue
    public int agit_preparatory_term; // Неизвестно, активно у rainbow_spring
    @IntValue
    public int agit_progress_term; // Неизвестно, активно у rainbow_spring
    @IntValue
    public int agit_close_term; // Неизвестно, активно у rainbow_spring
    @ObjectArray
    public Point3[] agit_entrance_point; // Неизвестно, активно у rainbow_spring
    @StringArray
    public String[] agit_delete_item_list; // Неизвестно, активно у
    // rainbow_spring
    @StringArray
    public String[] residence_skill_list; // Список скилов, активно у земель,
    // фортов и замков
    @StringValue
    public String dominion_main_castle; // Главный замок, активно для земель
    // В списке - нпс, координаты, радиус и высота.
    // Имя имеет только первый NPC, остальные, возможно, устанавливаются из базы
    @ObjectArray
    public ResidenceOwnthing[] ownthing; // Координаты флагов земель,
    @StringValue
    public String pickup_ownthing;// Какой-то ItemId флага
    @StringValue
    public String hide_name_item; // ItemId, скрывающий имя

    public enum GetType {
        siege,
        npc_battle,
        auction,
        team_battle,
        minigame
    }

    private enum ResidenceState {
        initial,
        owned
    }

    public static class ResidenceOwnthing {
        @StringValue(withoutName = true)
        public String npc_name; // Имя NPC
        @ObjectValue(withoutName = true)
        public Point3 point; // Координаты
        @IntValue(withoutName = true)
        public int heading; // Угол разворота
        @DoubleValue(withoutName = true)
        public double radius; // радиус
        @DoubleValue(withoutName = true)
        public double height; // высота
    }

    public static class ResidenceHolything {
        @StringValue(withoutName = true)
        public String name; // Название
        @ObjectValue(withoutName = true)
        public Point3 point; // Координаты
        @IntValue(withoutName = true)
        public int heading; // Угол поворота
        @DoubleValue(withoutName = true)
        public double radius; // collision_radius
        @DoubleValue(withoutName = true)
        public double height; // collision_height
    }

    public static class ResidenceGate {
        @StringValue(withoutName = true)
        public String name; // Название ворот
        @IntValue(withoutName = true)
        public int unknown; // Неизвестно
    }

    public static class ResidenceNpc {
        @StringValue(withoutName = true)
        public String name; // Название NPC
        @IntValue(withoutName = true)
        public int unknown; // Неизвестно
    }

    public static class ResidenceGuardMap {
        @EnumArray(withoutName = true)
        public SSQType[] ssq_type; // Выигравшие печати, при которых активны
        @IntValue(withoutName = true)
        private int unknown;// Неизвестно
        @EnumValue(withoutName = true)
        private GuardType guard_type; // Тип гварда
        @EnumValue(withoutName = true)
        private MoveType move_type; // Тип передвижения, двигается или стоит на
        // месте
        @IntValue(withoutName = true)
        private int npc_id; // Id гварда
        @IntValue(withoutName = true)
        private int count; // Вероятнее всего - количество
        // данные гварды

        public enum GuardType {
            sword,
            bow,
            pole,
            cleric,
            wizard,
            teleporter
        }

        public enum SSQType {
            ssq_normal,
            ssq_dawn,
            ssq_twilight
        }

        public enum MoveType {
            notmove,
            move
        }
    }
}
