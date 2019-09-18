package ru.j2dev.dataparser.parser;

import ru.j2dev.dataparser.common.AbstractDataParser;
import ru.j2dev.dataparser.holder.SkillAcquireHolder;

/**
 * @author KilRoy
 */
public class SkillAcquireParser extends AbstractDataParser<SkillAcquireHolder> {
    private static final SkillAcquireParser ourInstance = new SkillAcquireParser();

    protected SkillAcquireParser() {
        super(SkillAcquireHolder.getInstance());
    }

    public static SkillAcquireParser getInstance() {
        return ourInstance;
    }

    @Override
    protected String getFileName() {
        return "data/pts_scripts/skillacquire.txt";
    }
}