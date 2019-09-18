package ru.j2dev.gameserver.stats.conditions;

import ru.j2dev.gameserver.stats.Env;

public abstract class ConditionInventory extends Condition {
    protected final int _slot;

    public ConditionInventory(final int slot) {
        _slot = slot;
    }

    @Override
    protected abstract boolean testImpl(final Env p0);
}
