package ru.j2dev.dataparser.holder.variationdata;

import ru.j2dev.dataparser.annotations.array.StringArray;
import ru.j2dev.dataparser.annotations.value.IntValue;
import ru.j2dev.dataparser.annotations.value.StringValue;

/**
 * @author : Mangol
 */
public class VariationItemData {
    @StringValue(withoutName = true)
    public String item_group;
    @IntValue(withoutName = true)
    public int id;
    @StringArray
    public String[] item_list = new String[0];
}
