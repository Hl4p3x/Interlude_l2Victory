package ru.j2dev.dataparser.holder.expdata;

import ru.j2dev.dataparser.annotations.value.IntValue;
import ru.j2dev.dataparser.annotations.value.LongValue;

/**
 * @author KilRoy
 */
public class ExpDataTable {
    @IntValue
    private int level;
    @LongValue
    private long exp;

    public int getLevel() {
        return level;
    }

    public long getExp() {
        return exp;
    }
}