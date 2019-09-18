package ru.j2dev.dataparser.parser;

import ru.j2dev.dataparser.common.AbstractDataParser;
import ru.j2dev.dataparser.holder.ConvertDataHolder;

/**
 * @author : Camelion
 * @date : 25.08.12 22:47
 */
public class ConvertDataParser extends AbstractDataParser<ConvertDataHolder> {
    private static final ConvertDataParser ourInstance = new ConvertDataParser();

    private ConvertDataParser() {
        super(ConvertDataHolder.getInstance());
    }

    public static ConvertDataParser getInstance() {
        return ourInstance;
    }

    @Override
    protected String getFileName() {
        return "data/pts_scripts/convertdata.txt";
    }
}