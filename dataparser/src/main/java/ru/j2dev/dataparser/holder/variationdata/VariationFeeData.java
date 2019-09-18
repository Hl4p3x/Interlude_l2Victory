package ru.j2dev.dataparser.holder.variationdata;

import ru.j2dev.dataparser.annotations.value.IntValue;
import ru.j2dev.dataparser.annotations.value.ObjectValue;
import ru.j2dev.dataparser.annotations.value.StringValue;

/**
 * @author : Mangol
 */
public class VariationFeeData {
    @StringValue
    public String item_group_name;
    @StringValue
    public String mineral;
    @StringValue
    public String fee_item_name;
    @ObjectValue
    public FeeItemCount fee_item_count;
    @ObjectValue
    public CancelFee cancel_fee;

    public static class FeeItemCount {
        @IntValue(withoutName = true)
        public int feeItemCount;
    }

    public static class CancelFee {
        @IntValue(withoutName = true)
        public int cancelFee;
    }
}
