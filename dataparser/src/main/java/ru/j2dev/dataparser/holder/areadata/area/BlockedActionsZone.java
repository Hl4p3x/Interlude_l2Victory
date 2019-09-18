package ru.j2dev.dataparser.holder.areadata.area;

import ru.j2dev.dataparser.annotations.array.EnumArray;
import ru.j2dev.dataparser.annotations.class_annotations.ParseSuper;
import ru.j2dev.dataparser.holder.restrictarea.RestrictAction;

import java.util.Arrays;

/**
 * @author KilRoy
 */
@ParseSuper
public class BlockedActionsZone extends DefaultArea {
    // активно не во всех peace_zone областях
    @EnumArray
    private RestrictAction[] blocked_actions; // Запрещенные в этой зоне действия

    public BlockedActionsZone(final DefaultArea defaultSetting) {
        super(defaultSetting);
        blocked_actions = ((BlockedActionsZone) defaultSetting).blocked_actions;
    }

    public BlockedActionsZone() {
    }

    public void addBlockedActions(final String action) {
        blocked_actions[0] = RestrictAction.valueOf(action);
    }

    public RestrictAction[] getBlockedActions() {
        return blocked_actions;
    }

    public String[] getBlockedActionsString() {
        return Arrays.copyOf(blocked_actions, blocked_actions.length, String[].class);
    }
}