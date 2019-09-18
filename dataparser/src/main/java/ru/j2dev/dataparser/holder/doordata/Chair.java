package ru.j2dev.dataparser.holder.doordata;

import ru.j2dev.dataparser.annotations.value.IntValue;
import ru.j2dev.dataparser.annotations.value.ObjectValue;
import ru.j2dev.dataparser.annotations.value.StringValue;
import ru.j2dev.dataparser.common.Point3;

/**
 * @author : Camelion
 * @date : 27.08.12 0:57
 */
public class Chair {
    @StringValue(withoutName = true)
    public String name; // название
    @IntValue
    public int editor_id;
    @ObjectValue
    public Point3 pos; // Позиция
}
