package ru.j2dev.dataparser.holder.npcpos;

import ru.j2dev.dataparser.annotations.array.ObjectArray;
import ru.j2dev.dataparser.annotations.value.StringValue;
import ru.j2dev.dataparser.common.Point4;

/**
 * @author : Camelion
 * @date : 30.08.12 20:15
 */
public class PosTerritory {
    @StringValue(withoutName = true)
    private String name;
    @ObjectArray(withoutName = true)
    private Point4[] points;

    public String getPosTerritoryName() {
        return name;
    }

    public Point4[] getPoints() {
        return points;
    }
}