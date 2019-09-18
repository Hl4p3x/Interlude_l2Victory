package ru.j2dev.dataparser.holder.airship;

import ru.j2dev.dataparser.annotations.array.IntArray;
import ru.j2dev.dataparser.annotations.array.ObjectArray;
import ru.j2dev.dataparser.annotations.value.EnumValue;
import ru.j2dev.dataparser.annotations.value.IntValue;
import ru.j2dev.dataparser.annotations.value.ObjectValue;
import ru.j2dev.dataparser.common.Point4;

/**
 * @author : Camelion
 * @date : 24.08.12 20:31
 */
public class AirshipArea {
    @IntArray(name = "map_no")
    public int[] map;
    @ObjectValue
    public AreaType area_type; // Тип области. При type = AIRPORT param = ID
    // Аэропорта. При type = SPEED, значение param
    // неизвествно
    @ObjectArray(canBeNull = false)
    public Point4[] area_range;
    @IntValue
    private int id; // ID бласти

    public enum Type {
        AIRPORT,
        SPEED
    }

    public static class AreaType {
        @EnumValue(withoutName = true)
        private Type type;
        @IntValue(withoutName = true)
        private int param;
    }
}
