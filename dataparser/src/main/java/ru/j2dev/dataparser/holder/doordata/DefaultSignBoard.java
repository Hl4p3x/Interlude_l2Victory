package ru.j2dev.dataparser.holder.doordata;

import ru.j2dev.dataparser.annotations.value.ObjectValue;
import ru.j2dev.dataparser.annotations.value.StringValue;
import ru.j2dev.dataparser.common.Point3;

/**
 * @author : Camelion
 * @date : 26.08.12 22:31
 */
public class DefaultSignBoard {
    @StringValue(withoutName = true)
    public String signBoardName; // Название, есть у всех
    @ObjectValue
    public Point3 pos; // Позиция, есть у всех
}
