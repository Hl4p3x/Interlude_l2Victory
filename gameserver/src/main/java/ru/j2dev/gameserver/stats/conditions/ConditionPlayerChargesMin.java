package ru.j2dev.gameserver.stats.conditions;

import ru.j2dev.gameserver.stats.Env;

public class ConditionPlayerChargesMin extends Condition {
    private final int _minCharges;

    public ConditionPlayerChargesMin(final int minCharges) {
        _minCharges = minCharges;
    }

    @Override
    protected boolean testImpl(final Env env) {
        return env.character != null && env.character.isPlayer() && env.character.getIncreasedForce() >= _minCharges;
    }
}
