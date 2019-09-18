package ru.j2dev.gameserver.stats;

import ru.j2dev.commons.lang.ArrayUtils;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.stats.funcs.Func;
import ru.j2dev.gameserver.stats.funcs.FuncOwner;

import java.util.Arrays;

public final class Calculator {
    public final Stats _stat;
    public final Creature _character;
    private Func[] _functions;
    private double _base;
    private double _last;

    public Calculator(final Stats stat, final Creature character) {
        _stat = stat;
        _character = character;
        _functions = Func.EMPTY_FUNC_ARRAY;
    }

    public int size() {
        return _functions.length;
    }

    public void addFunc(final Func f) {
        Arrays.sort(_functions = ArrayUtils.add(_functions, f));
    }

    public void removeFunc(final Func f) {
        _functions = ArrayUtils.remove(_functions, f);
        if (_functions.length == 0) {
            _functions = Func.EMPTY_FUNC_ARRAY;
        } else {
            Arrays.sort(_functions);
        }
    }

    public void removeOwner(final Object owner) {
        final Func[] functions = _functions;
        for (final Func element : functions) {
            if (element.getOwner() == owner) {
                removeFunc(element);
            }
        }
    }

    public void calc(final Env env) {
        final Func[] funcs = _functions;
        _base = env.value;
        boolean overrideLimits = false;
        for (final Func func : funcs) {
            Label_0122:
            {
                if (func != null) {
                    if (func.getOwner() instanceof FuncOwner) {
                        if (!((FuncOwner) func.getOwner()).isFuncEnabled()) {
                            break Label_0122;
                        }
                        if (((FuncOwner) func.getOwner()).overrideLimits()) {
                            overrideLimits = true;
                        }
                    }
                    if (func.getCondition() == null || func.getCondition().test(env)) {
                        func.calc(env);
                    }
                }
            }
        }
        if (!overrideLimits) {
            env.value = _stat.validate(env.value);
        }
        if (env.value != _last) {
            final double last = _last;
            _last = env.value;
        }
    }

    public Func[] getFunctions() {
        return _functions;
    }

    public double getBase() {
        return _base;
    }

    public double getLast() {
        return _last;
    }
}
