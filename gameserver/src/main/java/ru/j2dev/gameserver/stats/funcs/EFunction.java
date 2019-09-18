package ru.j2dev.gameserver.stats.funcs;

import ru.j2dev.gameserver.stats.Stats;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum EFunction {
    Set {
        @Override
        public Func create(final Stats stat, final int order, final Object owner, final double value) {
            return new FuncSet(stat, order, owner, value);
        }
    },
    Add {
        @Override
        public Func create(final Stats stat, final int order, final Object owner, final double value) {
            return new FuncAdd(stat, order, owner, value);
        }
    },
    Sub {
        @Override
        public Func create(final Stats stat, final int order, final Object owner, final double value) {
            return new FuncSub(stat, order, owner, value);
        }
    },
    Mul {
        @Override
        public Func create(final Stats stat, final int order, final Object owner, final double value) {
            return new FuncMul(stat, order, owner, value);
        }
    },
    Div {
        @Override
        public Func create(final Stats stat, final int order, final Object owner, final double value) {
            return new FuncDiv(stat, order, owner, value);
        }
    },
    Enchant {
        @Override
        public Func create(final Stats stat, final int order, final Object owner, final double value) {
            return new FuncEnchant(stat, order, owner, value);
        }
    };

    public static final EFunction[] VALUES = values();
    public static final Map<String, EFunction> VALUES_BY_LOWER_NAME = new HashMap<>();

    static {
        Arrays.stream(VALUES).forEach(eFunc -> VALUES_BY_LOWER_NAME.put(eFunc.name().toLowerCase(), eFunc));
    }

    public abstract Func create(final Stats p0, final int p1, final Object p2, final double p3);
}
