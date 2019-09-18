package ru.j2dev.dataparser.holder.fishingdata;

import ru.j2dev.dataparser.annotations.array.ObjectArray;
import ru.j2dev.dataparser.annotations.value.DoubleValue;
import ru.j2dev.dataparser.annotations.value.EnumValue;
import ru.j2dev.dataparser.annotations.value.IntValue;
import ru.j2dev.dataparser.holder.fishingdata.FishingDistribution.Distribution;

/**
 * @author : Camelion
 * @date : 27.08.12 3:09
 */
public class Lure {
    @IntValue
    public int lure_id;
    @IntValue
    public int lure_item_id;
    @DoubleValue
    public double revision_number;
    @IntValue
    public int length_bonus;
    @DoubleValue
    public double length_rate_bonus;
    @EnumValue
    public LureType lure_type;
    @ObjectArray
    public Distribution[] fish_group_preference;

    public enum LureType {
        normal_lure,
        night_lure
    }
}
