package ru.j2dev.dataparser.holder.manordata;

import ru.j2dev.dataparser.annotations.value.IntValue;
import ru.j2dev.dataparser.annotations.value.StringValue;

/**
 * @author KilRoy
 */
public class CropInfo {
    @StringValue(withoutName = true, withoutBounds = true)
    public String crop_item_name_1;
    @StringValue(withoutName = true, withoutBounds = true)
    public String crop_item_name_2;
    @StringValue(withoutName = true, withoutBounds = true)
    public String crop_item_name_3;
    @IntValue(withoutName = true)
    public int crop_level;
    @IntValue(withoutName = true)
    public int limit_for_seeds;
    @IntValue(withoutName = true)
    public int limit_for_crops;
}