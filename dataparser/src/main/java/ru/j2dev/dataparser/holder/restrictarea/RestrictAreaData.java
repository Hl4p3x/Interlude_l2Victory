package ru.j2dev.dataparser.holder.restrictarea;

import ru.j2dev.dataparser.annotations.array.ObjectArray;
import ru.j2dev.dataparser.annotations.value.StringValue;
import ru.j2dev.dataparser.common.Point4;

/**
 * @author KilRoy
 */
public class RestrictAreaData {
    @StringValue
    private String name; // название области имеющей ограничение

    @ObjectArray
    private Point4[] range; // описание координат области ограничения(x, y, z-min, z-max)

    public String getName() {
        return name;
    }

    public Point4[] getRestrictedPoints() {
        return range;
    }
}