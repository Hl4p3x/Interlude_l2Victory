package ru.j2dev.dataparser.parser;

import ru.j2dev.dataparser.common.AbstractDataParser;
import ru.j2dev.dataparser.holder.CubicDataHolder;

/**
 * @author : Camelion
 * @date : 26.08.12 13:13
 */
public class CubicDataParser extends AbstractDataParser<CubicDataHolder> {
    private static final CubicDataParser ourInstance = new CubicDataParser();

    private CubicDataParser() {
        super(CubicDataHolder.getInstance());
    }

    public static CubicDataParser getInstance() {
        return ourInstance;
    }

    @Override
    protected String getFileName() {
        return "data/pts_scripts/cubicdata.txt";
    }
}