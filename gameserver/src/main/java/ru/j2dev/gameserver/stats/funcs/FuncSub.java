package ru.j2dev.gameserver.stats.funcs;

import ru.j2dev.gameserver.stats.Env;
import ru.j2dev.gameserver.stats.Stats;

public class FuncSub extends Func {
    public FuncSub(final Stats stat, final int order, final Object owner, final double value) {
        super(stat, order, owner, value);
    }

    @Override
    public void calc(final Env env) {
        switch (getStat()) {
            case MAX_CP:
            case MAX_HP:
            case MAX_MP: {
                env.value = Math.max(env.value - getValue(), 1.0);
                break;
            }
            default: {
                env.value -= getValue();
                break;
            }
        }
    }
}
