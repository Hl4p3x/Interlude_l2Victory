package ru.j2dev.gameserver.stats.conditions;

import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.stats.Env;

public abstract class Condition {
    public static final Condition[] EMPTY_ARRAY = new Condition[0];

    private SystemMsg _message;

    public final SystemMsg getSystemMsg() {
        return _message;
    }

    public final void setSystemMsg(final int msgId) {
        _message = SystemMsg.valueOf(msgId);
    }

    public final boolean test(final Env env) {
        return testImpl(env);
    }

    protected abstract boolean testImpl(final Env p0);
}
