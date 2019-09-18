package ru.j2dev.dataparser.holder.fishingdata;

import ru.j2dev.dataparser.annotations.array.IntArray;
import ru.j2dev.dataparser.annotations.value.DoubleValue;

/**
 * @author : Camelion
 * @date : 27.08.12 3:22
 */
public class FishingMonster {
    @IntArray
    public int[] user_level;
    @DoubleValue
    public double monster_probability;
    // Устанавливается через фабрику объектов
    public String[] fishingmonsters;
}
