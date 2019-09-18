package ru.j2dev.dataparser.parser;

import ru.j2dev.dataparser.common.AbstractDataParser;
import ru.j2dev.dataparser.holder.PCParameterHolder;

public class PCParameterParser extends AbstractDataParser<PCParameterHolder> {
    private static final PCParameterParser ourInstance = new PCParameterParser();

    private PCParameterParser() {
        super(PCParameterHolder.getInstance());
    }

    public static PCParameterParser getInstance() {
        return ourInstance;
    }

    @Override
    protected String getFileName() {
        return "data/pts_scripts/pc_parameter.txt";
    }
}