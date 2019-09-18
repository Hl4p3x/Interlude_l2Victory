package ru.j2dev.dataparser.common;


import ru.j2dev.dataparser.annotations.value.LongValue;
import ru.j2dev.dataparser.annotations.value.StringValue;

public class ItemName_Count {
    @StringValue(withoutName = true)
    public String itemName; // Название предмета
    @LongValue(withoutName = true)
    public long count; // Количество
}