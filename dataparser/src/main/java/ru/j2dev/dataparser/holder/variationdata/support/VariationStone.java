package ru.j2dev.dataparser.holder.variationdata.support;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public class VariationStone {
    private final int _id;
    private final TIntObjectMap<VariationInfo> _variations = new TIntObjectHashMap<>();

    public VariationStone(final int id) {
        _id = id;
    }

    public int getId() {
        return _id;
    }

    public void addVariation(final VariationInfo variation) {
        _variations.put(variation.getId(), variation);
    }

    public VariationInfo getVariation(final int id) {
        return _variations.get(id);
    }

    public VariationInfo[] getVariations() {
        return _variations.values(new VariationInfo[_variations.size()]);
    }
}
