package ru.j2dev.dataparser.parser;

import ru.j2dev.dataparser.common.AbstractDataParser;
import ru.j2dev.dataparser.holder.DoorDataHolder;

/**
 * @author : Camelion
 * @date : 26.08.12 22:29
 */
public class DoorDataParser extends AbstractDataParser<DoorDataHolder> {
    private static final DoorDataParser ourInstance = new DoorDataParser();

    private DoorDataParser() {
        super(DoorDataHolder.getInstance());
    }

    public static DoorDataParser getInstance() {
        return ourInstance;
    }

    @Override
    protected String getFileName() {
        return "data/pts_scripts/doordata.txt";
    }
}