package ru.j2dev.dataparser.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.dataparser.annotations.Element;
import ru.j2dev.dataparser.holder.variationdata.VariationData;
import ru.j2dev.dataparser.holder.variationdata.VariationFeeData;
import ru.j2dev.dataparser.holder.variationdata.VariationItemData;

import java.util.List;

/**
 * @author : Mangol
 */
public class VariationDataHolder extends AbstractHolder {
    @Element(start = "variation_begin", end = "variation_end")
    private static List<VariationData> variation_data;
    @Element(start = "item_group_begin", end = "item_group_end")
    private List<VariationItemData> item_group;
    @Element(start = "fee_begin", end = "fee_end")
    private List<VariationFeeData> fee_data;

    public static VariationDataHolder getInstance() {
        return LazyHolder._instance;
    }

    @Override
    public int size() {
        return variation_data.size() + item_group.size() + fee_data.size();
    }

    public List<VariationItemData> getVariationItemGroup() {
        return item_group;
    }

    public List<VariationData> getVariationData() {
        return variation_data;
    }

    public List<VariationFeeData> getVariationFreeData() {
        return fee_data;
    }

    private static class LazyHolder {
        protected static final VariationDataHolder _instance = new VariationDataHolder();
    }

    @Override
    public void clear() {

    }
}
