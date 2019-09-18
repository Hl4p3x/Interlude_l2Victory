package ru.j2dev.dataparser.common;

import ru.j2dev.dataparser.annotations.array.ObjectArray;

/**
 * @author : Camelion
 * @date : 25.08.12 23:18
 */
public class Territory {
    @ObjectArray(withoutName = true)
    public Point4[] points;
}
