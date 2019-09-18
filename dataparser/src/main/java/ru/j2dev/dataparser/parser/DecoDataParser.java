package ru.j2dev.dataparser.parser;

import ru.j2dev.dataparser.common.AbstractDataParser;
import ru.j2dev.dataparser.holder.DecoDataHolder;

/**
 * @author : Camelion
 * @date : 26.08.12 21:44
 */
public class DecoDataParser extends AbstractDataParser<DecoDataHolder> {
    private static final DecoDataParser ourInstance = new DecoDataParser();

    private DecoDataParser() {
        super(DecoDataHolder.getInstance());
    }

    public static DecoDataParser getInstance() {
        return ourInstance;
    }

    @Override
    protected String getFileName() {
        return "data/pts_scripts/decodata.txt";
    }
}