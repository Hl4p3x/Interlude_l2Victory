package ru.j2dev.dataparser.holder.npcpos;

import ru.j2dev.dataparser.annotations.array.ObjectArray;
import ru.j2dev.dataparser.annotations.value.IntValue;
import ru.j2dev.dataparser.annotations.value.StringValue;
import ru.j2dev.dataparser.common.Point4;

/**
 * @author : Camelion
 * @date : 30.08.12 20:12
 */
public class Domain {
    @StringValue(withoutName = true)
    public String name;
    @IntValue
    public int domain_id;
    @ObjectArray(withoutName = true)
    public Point4[] territory;
}
