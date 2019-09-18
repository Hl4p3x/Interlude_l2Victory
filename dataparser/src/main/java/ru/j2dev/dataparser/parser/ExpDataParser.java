package ru.j2dev.dataparser.parser;

import ru.j2dev.dataparser.common.AbstractDataParser;
import ru.j2dev.dataparser.holder.ExpDataHolder;

/**
 * @author KilRoy
 */
public class ExpDataParser extends AbstractDataParser<ExpDataHolder> {
    private static final ExpDataParser ourInstance = new ExpDataParser();

    private ExpDataParser() {
        super(ExpDataHolder.getInstance());
    }

    public static ExpDataParser getInstance() {
        return ourInstance;
    }

    @Override
    protected String getFileName() {
        return "data/pts_scripts/expdata.txt";
    }
}