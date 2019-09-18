package ru.j2dev.dataparser.parser;

import ru.j2dev.dataparser.common.AbstractDataParser;
import ru.j2dev.dataparser.holder.InstantZoneDataHolder;

/**
 * @author : Camelion
 * @date : 27.08.12 13:54
 */
public class InstantZoneDataParser extends AbstractDataParser<InstantZoneDataHolder> {
    private static final InstantZoneDataParser ourInstance = new InstantZoneDataParser();

    private InstantZoneDataParser() {
        super(InstantZoneDataHolder.getInstance());
    }

    public static InstantZoneDataParser getInstance() {
        return ourInstance;
    }

    @Override
    protected String getFileName() {
        return "data/pts_scripts/instantzonedata.txt";
    }
}