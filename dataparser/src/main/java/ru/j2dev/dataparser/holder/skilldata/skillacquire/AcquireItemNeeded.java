package ru.j2dev.dataparser.holder.skilldata.skillacquire;

import ru.j2dev.dataparser.annotations.value.LongValue;
import ru.j2dev.dataparser.annotations.value.StringValue;

/**
 * @author KilRoy
 */
public class AcquireItemNeeded {
    @StringValue(withoutName = true)
    public String item_name;
    @LongValue(withoutName = true)
    public long count;
}