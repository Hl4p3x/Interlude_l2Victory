package ru.j2dev.gameserver.stats.conditions;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.stats.Env;

public class ConditionPlayerClassIsMage extends Condition {
    private final boolean _value;

    public ConditionPlayerClassIsMage(final boolean value) {
        _value = value;
    }

    @Override
    protected boolean testImpl(final Env env) {
        if (env.character == null) {
            return false;
        }
        final Player player = env.character.getPlayer();
        return player != null && player.isMageClass() == _value;
    }
}
