package ru.j2dev.dataparser.parser;

import ru.j2dev.dataparser.common.AbstractDataParser;
import ru.j2dev.dataparser.holder.NpcPosHolder;

/**
 * @author : Camelion
 * @date : 30.08.12 20:10
 */
public class NpcPosParser extends AbstractDataParser<NpcPosHolder> {
    private static final NpcPosParser ourInstance = new NpcPosParser();

    private NpcPosParser() {
        super(NpcPosHolder.getInstance());
    }

    public static NpcPosParser getInstance() {
        return ourInstance;
    }

    @Override
    protected String getFileName() {
        return "data/pts_scripts/npcpos.txt";
    }
}