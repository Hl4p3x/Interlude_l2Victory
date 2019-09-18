package ru.j2dev.dataparser.holder.airship;

import ru.j2dev.dataparser.annotations.Element;
import ru.j2dev.dataparser.annotations.ElementArray;
import ru.j2dev.dataparser.annotations.array.ObjectArray;
import ru.j2dev.dataparser.annotations.value.EnumValue;
import ru.j2dev.dataparser.annotations.value.IntValue;
import ru.j2dev.dataparser.annotations.value.ObjectValue;
import ru.j2dev.dataparser.common.Point3;

import java.util.List;

/**
 * @author : Camelion
 * @date : 24.08.12 12:25
 * <p/>
 * Содержит в себе информацию об аэропортах
 */
public class AirPort {
    @ElementArray(start = "teleport_begin", end = "teleport_end")
    public AirportTeleport[] teleports; // Может быть null, для тех аэроопортов,
    // у которых нет платформы (в этом
    // случае должно быть указано
    // airship_pos)
    @Element(start = "platform_begin", end = "platform_end")
    public List<AirportPlatform> platforms; // Может быть null, для тех
    // аэроопортов, у которых нет
    // платформы (в этом случае должно
    // быть указано airship_pos)
    @ObjectValue()
    public Point3 airship_pos; // Позиция корабля (null для тех аэропорторв, у
    // которых указаны платформы)
    @ObjectArray
    public Point3[][] getoff_pos; // (null для тех аэропорторв, у которых
    @IntValue
    private int id; // ID аэропорта
    @EnumValue
    private AirportType airport_type; // Тип аэропорта
    // указаны платформы)
    @IntValue
    private int stopover;

    private enum AirportType {
        REGULAR,
        FREE
    }

    public static class AirportTeleport {
        @ObjectValue(canBeNull = false)
        public Point3 tel_pos; // Возможно, позиция телепорта
        @IntValue(replacements = {"expel", "-1"})
        // Для обхода бестолковых параметров оффа
        private int id;
        @IntValue
        private int fuel; // Количество топлива
    }

    public static class AirportPlatform {
        @ObjectArray
        public Point3[] landing_path; // Неизвестно
        @ObjectValue
        public Point3 airship_pos; // Какая-то позиция корабля
        @ObjectArray
        public Point3[] takeoff_path; // Неизвестно
        @ObjectValue
        public AirportPlatformGetoffPos getoff_pos;
        @IntValue
        private int id; // ID платофрмы
    }

    public static class AirportPlatformGetoffPos {
        @ObjectValue(withoutName = true)
        public Point3 point;//
        @IntValue(withoutName = true)
        private int id;// Возможно, ID проигрываемого ролика
    }
}
