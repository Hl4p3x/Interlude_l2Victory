package ru.j2dev.dataparser.holder.manordata;

import ru.j2dev.dataparser.annotations.value.IntValue;
import ru.j2dev.dataparser.annotations.value.StringValue;

/**
 * @author KilRoy
 */
public class RewardInfo {
    @IntValue(withoutName = true)
    public int reward_id;
    @StringValue(withoutName = true, withoutBounds = true)
    public String reward_item_name;
}