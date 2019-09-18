package ru.j2dev.dataparser.parser;

import ru.j2dev.dataparser.common.AbstractDataParser;
import ru.j2dev.dataparser.holder.SettingHolder;

/**
 * @author : Camelion
 * @date : 22.08.12 1:34
 */
public class SettingParser extends AbstractDataParser<SettingHolder> {
    private static final SettingParser ourInstance = new SettingParser();

    private SettingParser() {
        super(SettingHolder.getInstance());
    }

    public static SettingParser getInstance() {
        return ourInstance;
    }

    @Override
    protected String getFileName() {
        return "data/pts_scripts/setting.txt";
    }
}