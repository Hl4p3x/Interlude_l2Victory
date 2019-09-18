package ru.j2dev.dataparser.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.dataparser.annotations.ElementArray;
import ru.j2dev.dataparser.holder.expdata.ExpDataTable;

/**
 * @author KilRoy
 */
public class ExpDataHolder extends AbstractHolder {
    private static final ExpDataHolder ourInstance = new ExpDataHolder();
    @ElementArray(start = "exp_table_begin", end = "exp_table_end")
    private ExpDataTable[] expDataTable;

    private ExpDataHolder() {
    }

    public static ExpDataHolder getInstance() {
        return ourInstance;
    }

    public ExpDataTable[] getExpTableData() {
        return expDataTable;
    }

    public long getExpForLevel(final int level) {
        return expDataTable[level - 1].getExp();
    }

    public int getLevelForExp(final long exp) {
        int level = 1;
        for (final ExpDataTable expData : expDataTable) {
            if (exp >= expData.getExp()) {
                level = expData.getLevel() + 1;
            }
        }
        return level;
    }

    public double getExpPercent(final int level, final long exp) {
        return (exp - getExpForLevel(level)) / ((expDataTable[level].getExp() - getExpForLevel(level)) / 100.0D) * 0.01D;
    }

    @Override
    public int size() {
        return expDataTable.length;
    }

    @Override
    public void clear() {
        //
    }
}