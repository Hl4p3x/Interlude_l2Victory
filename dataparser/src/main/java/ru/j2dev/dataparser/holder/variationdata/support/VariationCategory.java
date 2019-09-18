package ru.j2dev.dataparser.holder.variationdata.support;

import java.util.ArrayList;
import java.util.List;

public class VariationCategory {
    private final double _probability;
    private final List<VariationOption> _options = new ArrayList<>();

    public VariationCategory(final double probability) {
        _probability = probability;
    }

    public double getProbability() {
        return _probability;
    }

    public void addOption(VariationOption option) {
        _options.add(option);
    }

    public VariationOption[] getOptions() {
        return _options.toArray(new VariationOption[0]);
    }
}
