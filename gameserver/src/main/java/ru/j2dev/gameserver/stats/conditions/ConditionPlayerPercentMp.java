package ru.j2dev.gameserver.stats.conditions;

import ru.j2dev.gameserver.stats.Env;

public class ConditionPlayerPercentMp extends Condition {
    private final double _mp;

    public ConditionPlayerPercentMp(final int mp) {
        _mp = mp / 100.0;
    }

    @Override
    protected boolean testImpl(final Env env) {
        return env.character.getCurrentMpRatio() <= _mp;
    }
}
