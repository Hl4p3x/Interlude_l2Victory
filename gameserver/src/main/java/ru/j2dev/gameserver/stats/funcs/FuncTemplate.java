package ru.j2dev.gameserver.stats.funcs;

import ru.j2dev.gameserver.stats.Stats;
import ru.j2dev.gameserver.stats.conditions.Condition;

public final class FuncTemplate {
    public static final FuncTemplate[] EMPTY_ARRAY = new FuncTemplate[0];

    private final Condition _applyCond;
    private final EFunction _func;
    private final Stats _stat;
    private final int _order;
    private final double _value;

    public FuncTemplate(final Condition applyCond, final String func, final Stats stat, final int order, final double value) {
        _applyCond = applyCond;
        _stat = stat;
        _order = order;
        _value = value;
        _func = EFunction.VALUES_BY_LOWER_NAME.get(func.toLowerCase());
        if (getFunc() == null) {
            throw new RuntimeException("Unknown function " + func);
        }
    }

    public FuncTemplate(final Condition applyCond, final EFunction func, final Stats stat, final int order, final double value) {
        _applyCond = applyCond;
        _stat = stat;
        _order = order;
        _value = value;
        _func = func;
    }

    public Func getFunc(final Object owner) {
        final Func f = getFunc().create(getStat(), getOrder(), owner, getValue());
        if (getApplyCond() != null) {
            f.setCondition(getApplyCond());
        }
        return f;
    }

    public Condition getApplyCond() {
        return _applyCond;
    }

    public EFunction getFunc() {
        return _func;
    }

    public Stats getStat() {
        return _stat;
    }

    public int getOrder() {
        return _order;
    }

    public double getValue() {
        return _value;
    }
}
