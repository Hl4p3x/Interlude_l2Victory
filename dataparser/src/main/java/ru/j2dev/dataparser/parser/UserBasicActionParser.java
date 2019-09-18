package ru.j2dev.dataparser.parser;

import ru.j2dev.dataparser.common.AbstractDataParser;
import ru.j2dev.dataparser.holder.UserBasicActionHolder;

/**
 * @author KilRoy
 */
public class UserBasicActionParser extends AbstractDataParser<UserBasicActionHolder> {
    private static final UserBasicActionParser ourInstance = new UserBasicActionParser();

    private UserBasicActionParser() {
        super(UserBasicActionHolder.getInstance());
    }

    public static UserBasicActionParser getInstance() {
        return ourInstance;
    }

    @Override
    protected String getFileName() {
        return "data/pts_scripts/userbasicaction.txt";
    }
}