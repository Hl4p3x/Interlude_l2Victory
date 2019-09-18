package ru.j2dev.dataparser.holder.instantzonedata;

import ru.j2dev.dataparser.annotations.array.IntArray;
import ru.j2dev.dataparser.annotations.array.ObjectArray;
import ru.j2dev.dataparser.annotations.array.StringArray;
import ru.j2dev.dataparser.annotations.value.EnumValue;
import ru.j2dev.dataparser.annotations.value.IntValue;
import ru.j2dev.dataparser.annotations.value.ObjectValue;
import ru.j2dev.dataparser.annotations.value.StringValue;
import ru.j2dev.dataparser.common.ItemName_Count;
import ru.j2dev.dataparser.common.Point3;
import ru.j2dev.dataparser.holder.InstantZoneDataHolder.EntranceCondObjectFactory;
import ru.j2dev.dataparser.holder.instantzonedata.entrance_cond.DefaultEntranceCond;

/**
 * @author : Camelion
 * @date : 27.08.12 14:01
 */
public class InstantZone {
    @IntValue
    public int id; // ID Зоны, есть у всех
    @IntValue
    public int cluster_id; // Неизвестно, есть не у всех
    @IntValue
    public int start_binding; // Неизвестно, есть не у всех
    @StringValue(withoutBounds = true)
    public String name; // Название зоны, есть у всех
    @IntValue
    public int max_channel; // Максимальное кол-во одновременно активных зон
    // данного типа
    @IntValue
    public int min_user = 1; // Минимальное кол-во игроков в одной инстанс-зоне
    // данного типа, есть не у всех
    @IntValue
    public int max_user; // Максимальное кол-во игроков в одной инстанс-зоне
    // данного типа
    @IntValue
    public int duration; // Продолжительность (в минутах)
    @IntValue
    public int time_limit; // Время "отката" инстанс зоны (в минутах).
    @ObjectValue
    public ResetBindig reset_binding; // Время сброса инстанс зоны для всех
    // игроков, может быть null
    @ObjectArray(name = "entrance_cond", objectFactory = EntranceCondObjectFactory.class)
    public DefaultEntranceCond[] entrance_conds = new DefaultEntranceCond[0];
    @IntValue
    public int delete_no_user; // Время, через которое будет удалена пустая
    // инстанс зона
    @ObjectArray
    public ItemName_Count[] required_item = new ItemName_Count[0]; // Предметы,
    // которые
    // должны
    // быть в
    // инвентаре,
    // при входе
    // в инстанс
    // зону (не
    // известно,
    // всегда,
    // или
    // только
    // при
    // первом
    // входе)
    @ObjectArray
    public ItemName_Count[] deleted_item = new ItemName_Count[0]; // Предметы,
    // которые
    // удаляются
    // при входе
    // в инстанс
    // зону (не
    // известно,
    // всегда,
    // или
    // только
    // при
    // первом
    // входе)
    @IntValue
    public int remove_buff; // Удалять ли бафф с персонажа. 1 - удалять, 0 - не
    // удалять
    @EnumValue
    public StartPosType start_pos_type; // Тип стартовой позиции
    @ObjectArray(name = "start_pos")
    public Point3[] start_poses; // Список точек - стартовых позиций
    // Для успешного завершения инстанс зоны
    @EnumValue
    public EndPosType order_end_pos_type; // Тип конечной позиции
    @ObjectArray(name = "order_end_pos")
    public Point3[] end_poses = new Point3[0]; // Список точек - конечных
    // позиций, для end_pos_type =
    // origin - список пуст
    // Для не успешного завершения инстанс зоны
    @EnumValue
    public EndPosType disorder_end_pos_type; // Тип конечной позиции
    @ObjectArray(name = "disorder_end_pos")
    public Point3[] disorder_end_poses = new Point3[0]; // Список точек -
    // конечных позиций
    @StringArray
    public String[] door_list = new String[0]; // Список дверей, привязанных к
    // инстанс зоне
    @StringArray
    public String[] area_list = new String[0]; // Список областей, привязанных к
    // инстанс зоне
    @EnumValue
    public YesNo spawn_npc; // Спаунить ли NPС при создании зоны (список NPC
    // находится в npcpos.txt)
    @EnumValue
    public YesNo associative; // Неизвестно
    @IntValue
    public int required_point; // Количество необходимых PC Bang Point для входа

    public enum StartPosType {
        designatory,
        random
    }

    public enum EndPosType {
        origin,
        nomination
    }

    public enum YesNo {
        yes,
        no
    }

    public static class ResetBindig {
        @IntValue(withoutName = true)
        public int hour; // Часы
        @IntValue(withoutName = true)
        public int min; // Минуты
        @IntArray(withoutName = true)
        public int[] day_of_week; // Дни недели, по которым выполняется сброс (0
        // = воскресенье, 1 = понедельник, 2=
        // вторник...)
    }
}
