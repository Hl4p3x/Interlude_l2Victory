package ru.j2dev.gameserver.model.quest;

import org.apache.commons.lang3.ArrayUtils;

public class Drop {
    public final int condition;
    public final int maxcount;
    public final int chance;
    public int[] itemList;

    public Drop(final int condition, final int maxcount, final int chance) {
        itemList = ArrayUtils.EMPTY_INT_ARRAY;
        this.condition = condition;
        this.maxcount = maxcount;
        this.chance = chance;
    }

    public Drop addItem(final int item) {
        itemList = ArrayUtils.add(itemList, item);
        return this;
    }
}
