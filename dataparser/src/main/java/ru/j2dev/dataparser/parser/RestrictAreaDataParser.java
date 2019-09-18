package ru.j2dev.dataparser.parser;

import ru.j2dev.dataparser.common.AbstractDataParser;
import ru.j2dev.dataparser.holder.RestrictAreaDataHolder;

/**
 * @author KilRoy
 */
public class RestrictAreaDataParser extends AbstractDataParser<RestrictAreaDataHolder> {
    private static final RestrictAreaDataParser ourInstance = new RestrictAreaDataParser();

    private RestrictAreaDataParser() {
        super(RestrictAreaDataHolder.getInstance());
    }

    public static RestrictAreaDataParser getInstance() {
        return ourInstance;
    }

    @Override
    protected String getFileName() {
        return "data/pts_scripts/RestrictAreaData.txt";
    }
}