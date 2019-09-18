package ru.j2dev.dataparser.holder.manordata;

import ru.j2dev.dataparser.annotations.array.ObjectArray;
import ru.j2dev.dataparser.annotations.value.StringValue;

/**
 * @author KilRoy
 */
public class ProcureInfo {
    @StringValue(withoutName = true, withoutBounds = true)
    public String procure_name;
    @ObjectArray(withoutName = true)
    public RewardInfo[] procure_reward;
}