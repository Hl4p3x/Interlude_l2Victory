package ru.j2dev.dataparser.parser;

import ru.j2dev.dataparser.common.AbstractDataParser;
import ru.j2dev.dataparser.holder.VariationDataHolder;

/**
 * @author : Mangol
 */
public class VariationParser extends AbstractDataParser<VariationDataHolder> {
    private static final VariationParser ourInstance = new VariationParser();

    private VariationParser() {
        super(VariationDataHolder.getInstance());
    }

    public static VariationParser getInstance() {
        return ourInstance;
    }

    @Override
    protected String getFileName() {
        return "data/pts_scripts/variationdata.txt";
    }
}
