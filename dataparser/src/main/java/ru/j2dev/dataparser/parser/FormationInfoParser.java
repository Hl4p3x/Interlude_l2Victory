package ru.j2dev.dataparser.parser;

import ru.j2dev.dataparser.common.AbstractDataParser;
import ru.j2dev.dataparser.holder.FormationInfoHolder;

/**
 * @author : Camelion
 * @date : 27.08.12 13:04
 */
public class FormationInfoParser extends AbstractDataParser<FormationInfoHolder> {
    private static final FormationInfoParser ourInstance = new FormationInfoParser();

    private FormationInfoParser() {
        super(FormationInfoHolder.getInstance());
    }

    public static FormationInfoParser getInstance() {
        return ourInstance;
    }

    @Override
    protected String getFileName() {
        return "data/pts_scripts/formationinfo.txt";
    }
}