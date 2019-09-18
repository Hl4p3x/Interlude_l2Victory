package ru.j2dev.dataparser.holder.variationdata.support;

public class VariationOption {
    private final int _id;
    private final double _chance;

    public VariationOption(final int id, final double chance) {
        _id = id;
        _chance = chance;
    }

    public int getId() {
        return _id;
    }

    public double getChance() {
        return _chance;
    }
}
