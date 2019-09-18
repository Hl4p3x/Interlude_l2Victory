package ru.j2dev.dataparser.holder.setting;

import ru.j2dev.dataparser.annotations.Element;
import ru.j2dev.dataparser.annotations.array.IntArray;
import ru.j2dev.dataparser.annotations.array.ObjectArray;
import ru.j2dev.dataparser.annotations.value.EnumValue;
import ru.j2dev.dataparser.annotations.value.IntValue;
import ru.j2dev.dataparser.annotations.value.ObjectValue;
import ru.j2dev.dataparser.annotations.value.StringValue;
import ru.j2dev.dataparser.common.Point4;
import ru.j2dev.dataparser.holder.setting.common.PlayerRace;

import java.util.List;

/**
 * @author : Camelion
 * @date : 22.08.12 18:26
 * <p/>
 * Класс содержит в себе информацию о точках, в которых появляется игрок
 * при рестарте.
 * <p/>
 * areas - Список зон points - Список точек, к которым привязаны зоны.
 */
public class RestartPoint {
    @Element(start = "area_begin", end = "area_end")
    public List<Area> areas;
    @Element(start = "point_begin", end = "point_end")
    public List<Point> points;

    public static class Area {
        // Список точек, которыми окружена область.
        @ObjectArray(name = "range")
        public Point4[] ranges;
        // Класс, содержащий в себе расу и название точки из списка
        // RestartPoint.points которая используется для выбора координаты
        @ObjectValue
        public List<Race_PointName> race;
    }

    public static class Race_PointName {
        @EnumValue(withoutName = true)
        public PlayerRace race; // целевая раса
        @StringValue(withoutName = true)
        public String point_name; // Название точки, используемой для выбора
        // координаты
    }

    public static class Point {
        @StringValue
        public String point_name; // Название точки
        @IntArray(name = "point", canBeNull = false)
        public List<int[]> points; // Список координат, в которых будут
        // появляться игроки
        @IntArray(name = "chao_point", canBeNull = false)
        public List<int[]> chao_points; // Список координат, в которых будут
        // появляться хаотичные (ПК) игроки
        // Список квадратов игрового мира, привязанных к данной точке.
        // Point точка выбирается по данным квадратам, если не нашлось Area
        // области для координат игрока.
        @IntArray(name = "map")
        public List<int[]> maps;
        // Раса, которая не может появиться в данной точке
        // Для такой расы в классе BanedRace указана альтернативная точка
        // появления
        @ObjectValue
        public BanedRace banned_race;
        // Возможно, номер, отображаемый в Комьюнити (неизвестно)
        @IntValue
        public int bbs;
        // Номер строка, в файле SystemMsg.dat соответствующий локации
        // (Неизвестно, для чего испльзуется.
        // Возможно для отображения в комьюнити или по центру экрана,
        // или просто каким-то образом передается клиенту)
        @IntValue
        public int loc_name;
    }

    public static class BanedRace {
        // Раса, которая не может появиться в данной точке
        @EnumValue(withoutName = true)
        public PlayerRace race;
        // Альтернативная точка появления
        @StringValue(withoutName = true)
        public String alternative_point;
    }
}
