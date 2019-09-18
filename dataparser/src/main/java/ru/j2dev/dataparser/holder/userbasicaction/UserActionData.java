package ru.j2dev.dataparser.holder.userbasicaction;

import ru.j2dev.dataparser.annotations.value.IntValue;
import ru.j2dev.dataparser.annotations.value.StringValue;

/**
 * @author KilRoy
 */
public class UserActionData {
    @IntValue
    private int id;
    @StringValue
    private String handler;
    @StringValue(withoutBounds = true)
    private String option;

    public int getId() {
        return id;
    }

    public String getHandler() {
        return handler;
    }

    public String getOption() {
        return option;
    }

    public ActionHandler getActionHandler() {
        if (handler == null || handler.length() == 0) {
            return ActionHandler.NONE;
        }
        return ActionHandler.valueOf(handler);
    }

    public ActionOption getActionOption() {
        if (option == null || option.length() == 0) {
            return ActionOption.none;
        }
        return ActionOption.valueOf(option);
    }

    public enum ActionHandler {
        NONE,
        SIT_STAND,
        WALK_RUN,
        ATTACK,
        TRADE,
        CLIENT_ACTION,
        PRIVATE_STORE,
        SOCIAL_ACTION,
        PET_ACTION,
        PET_DEPOSIT,
        SUMMON_ACTION,
        PRIVATE_BUY,
        MAKE_ITEM,
        RIDE,
        MAKE_ITEM2,
        SUMMON_DESPAWN,
        PACKAGE_PRIVATE_STORE,
        TELEPORT_BOOKMARK,
        BOT_REPORT,
        AIRSHIP_ACTION,
        COUPLE_ACTION,
    }

    public enum ActionOption {
        none,
        change_mode,
        attack,
        stop,
        get_item,
        skill,
        move,
        wheel,
        leave_wheel,
        start_ship,
        leave_ship,
    }
}