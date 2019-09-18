package ru.j2dev.dataparser.parser;

import ru.j2dev.dataparser.common.AbstractDataParser;
import ru.j2dev.dataparser.holder.PetDataHolder;

/**
 * @author KilRoy
 */
public class PetDataParser extends AbstractDataParser<PetDataHolder> {
    private static final PetDataParser INSTANCE = new PetDataParser();

    private PetDataParser() {
        super(PetDataHolder.getInstance());
    }

    public static PetDataParser getInstance() {
        return INSTANCE;
    }

    @Override
    protected String getFileName() {
        return "data/pts_scripts/petdata.txt";
    }
}