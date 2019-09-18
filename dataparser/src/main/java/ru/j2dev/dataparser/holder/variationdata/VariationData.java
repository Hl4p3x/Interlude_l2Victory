package ru.j2dev.dataparser.holder.variationdata;

import ru.j2dev.dataparser.annotations.array.ObjectArray;
import ru.j2dev.dataparser.annotations.value.DoubleValue;
import ru.j2dev.dataparser.annotations.value.EnumValue;
import ru.j2dev.dataparser.annotations.value.StringValue;

/**
 * @author : Mangol
 */
public class VariationData {
    @EnumValue
    public WeaponType weapon_type;
    @StringValue
    public String mineral;
    @ObjectArray
    public VariationGroup[] variation1 = new VariationGroup[0];
    @ObjectArray
    public VariationGroup[] variation2 = new VariationGroup[0];

    public enum WeaponType {
        warrior,
        mage
    }

    public static class VariationGroup {
        @ObjectArray(withoutName = true)
        public VariationInfo[] option;
        @DoubleValue(withoutName = true)
        public double group_chance;
    }

    public static class VariationInfo {
        @StringValue(withoutName = true)
        public String option_name;
        @DoubleValue(withoutName = true)
        public double chance;
    }
}
