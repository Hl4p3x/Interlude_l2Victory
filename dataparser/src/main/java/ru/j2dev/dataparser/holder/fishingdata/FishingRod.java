package ru.j2dev.dataparser.holder.fishingdata;

import ru.j2dev.dataparser.annotations.value.DoubleValue;
import ru.j2dev.dataparser.annotations.value.IntValue;
import ru.j2dev.dataparser.annotations.value.StringValue;

/**
 * @author : Camelion
 * @date : 27.08.12 3:20
 */
public class FishingRod {
    @IntValue
    public int fishingrod_id;
    @IntValue
    public int fishingrod_item_id;
    @IntValue
    public int fishingrod_level;
    @StringValue
    public String fishingrod_name;
    @DoubleValue
    public double fishingrod_damage;
}
