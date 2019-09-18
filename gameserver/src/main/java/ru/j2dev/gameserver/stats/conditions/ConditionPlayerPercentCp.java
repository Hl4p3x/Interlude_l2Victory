package ru.j2dev.gameserver.stats.conditions;

import ru.j2dev.gameserver.stats.Env;

public class ConditionPlayerPercentCp extends Condition {
    private final double _cp;

    public ConditionPlayerPercentCp(final int cp) {
        _cp = cp / 100.0;
    }

    @Override
    protected boolean testImpl(final Env env) {
        return env.character.getCurrentCpRatio() <= _cp;
    }
}
