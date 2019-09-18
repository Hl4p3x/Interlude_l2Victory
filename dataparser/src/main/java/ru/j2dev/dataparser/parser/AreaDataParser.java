package ru.j2dev.dataparser.parser;

import ru.j2dev.dataparser.common.AbstractDataParser;
import ru.j2dev.dataparser.holder.AreaDataHolder;

/**
 * @author : Camelion
 * @date : 24.08.12 23:23
 */
public class AreaDataParser extends AbstractDataParser<AreaDataHolder> {
    private static final AreaDataParser ourInstance = new AreaDataParser();

    private AreaDataParser() {
        super(AreaDataHolder.getInstance());
    }

    public static AreaDataParser getInstance() {
        return ourInstance;
    }

    @Override
    protected String getFileName() {
        return "data/pts_scripts/areadata.txt";
    }
}