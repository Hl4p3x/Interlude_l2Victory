package ru.j2dev.gameserver.stats.funcs;

import ru.j2dev.gameserver.stats.Env;
import ru.j2dev.gameserver.stats.Stats;
import ru.j2dev.gameserver.stats.conditions.Condition;

public abstract class Func implements Comparable<Func> {
    public static final Func[] EMPTY_FUNC_ARRAY = new Func[0];

    private final Stats stat;
    private final int order;
    private final Object owner;
    private final double value;
    private Condition cond;

    public Func(final Stats stat, final int order, final Object owner) {
        this(stat, order, owner, 0.0);
    }

    public Func(final Stats stat, final int order, final Object owner, final double value) {
        this.stat = stat;
        this.order = order;
        this.owner = owner;
        this.value = value;
    }

    public Condition getCondition() {
        return getCond();
    }

    public void setCondition(final Condition cond) {
        setCond(cond);
    }

    public abstract void calc(final Env p0);

    @Override
    public int compareTo(final Func f) throws NullPointerException {
        return getOrder() - f.getOrder();
    }

    public Stats getStat() {
        return stat;
    }

    public int getOrder() {
        return order;
    }

    public Object getOwner() {
        return owner;
    }

    public double getValue() {
        return value;
    }

    public Condition getCond() {
        return cond;
    }

    public void setCond(Condition cond) {
        this.cond = cond;
    }
}
