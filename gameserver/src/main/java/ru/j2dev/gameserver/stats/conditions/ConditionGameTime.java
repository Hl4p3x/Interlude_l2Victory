package ru.j2dev.gameserver.stats.conditions;

import ru.j2dev.gameserver.GameTimeController;
import ru.j2dev.gameserver.stats.Env;

public class ConditionGameTime extends Condition {
    private final CheckGameTime _check;
    private final boolean _required;

    public ConditionGameTime(final CheckGameTime check, final boolean required) {
        _check = check;
        _required = required;
    }

    @Override
    protected boolean testImpl(final Env env) {
        switch (_check) {
            case NIGHT: {
                return GameTimeController.getInstance().isNowNight() == _required;
            }
            default: {
                return !_required;
            }
        }
    }

    public enum CheckGameTime {
        NIGHT
    }
}
