package ru.j2dev.dataparser.parser;

import ru.j2dev.dataparser.common.AbstractDataParser;
import ru.j2dev.dataparser.holder.MonraceHolder;

/**
 * @author : Camelion
 * @date : 30.08.12 14:03
 */
public class MonraceParser extends AbstractDataParser<MonraceHolder> {
    private static final MonraceParser ourInstance = new MonraceParser();

    private MonraceParser() {
        super(MonraceHolder.getInstance());
    }

    public static MonraceParser getInstance() {
        return ourInstance;
    }

    @Override
    protected String getFileName() {
        return "data/pts_scripts/monrace.txt";
    }
}