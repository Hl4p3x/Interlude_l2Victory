package ru.j2dev.gameserver.stats.conditions;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.stats.Env;

public class ConditionPlayerAgathion extends Condition {
    private final int _agathionId;

    public ConditionPlayerAgathion(final int agathionId) {
        _agathionId = agathionId;
    }

    @Override
    protected boolean testImpl(final Env env) {
        return env.character.isPlayer() && ((((Player) env.character).getAgathionId() > 0 && _agathionId == -1) || ((Player) env.character).getAgathionId() == _agathionId);
    }
}
