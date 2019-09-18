package ru.j2dev.dataparser.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.dataparser.annotations.Element;
import ru.j2dev.dataparser.holder.userbasicaction.UserActionData;

import java.util.List;

/**
 * @author KilRoy
 */
public class UserBasicActionHolder extends AbstractHolder {
    private static final UserBasicActionHolder ourInstance = new UserBasicActionHolder();
    @Element(start = "action_begin", end = "action_end")
    private List<UserActionData> user_action_data;

    public static UserBasicActionHolder getInstance() {
        return ourInstance;
    }

    public List<UserActionData> getUserBasicAction() {
        return user_action_data;
    }

    public UserActionData getUserAction(final int actionId) {
        return user_action_data.stream().filter(action -> action.getId() == actionId).findFirst().get();
    }

    @Override
    public int size() {
        return user_action_data.size();
    }

    @Override
    public void clear() {
        user_action_data.clear();
    }
}