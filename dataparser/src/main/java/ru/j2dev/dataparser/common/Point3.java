package ru.j2dev.dataparser.common;

import ru.j2dev.dataparser.annotations.value.IntValue;

public class Point3 {
    @IntValue(withoutName = true)
    public int x;
    @IntValue(withoutName = true)
    public int y;
    @IntValue(withoutName = true)
    public int z;
}