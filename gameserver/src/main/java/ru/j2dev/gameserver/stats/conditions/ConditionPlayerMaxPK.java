package ru.j2dev.gameserver.stats.conditions;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.stats.Env;

public class ConditionPlayerMaxPK extends Condition {
    private final int _pk;

    public ConditionPlayerMaxPK(final int pk) {
        _pk = pk;
    }

    @Override
    protected boolean testImpl(final Env env) {
        return env.character.isPlayer() && ((Player) env.character).getPkKills() <= _pk;
    }
}
