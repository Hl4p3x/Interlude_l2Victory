package ru.j2dev.dataparser.holder.monrace;

import ru.j2dev.dataparser.annotations.array.IntArray;
import ru.j2dev.dataparser.annotations.value.IntValue;

/**
 * @author : Camelion
 * @date : 30.08.12 14:11
 */
public class MonTrack {
    @IntValue(withoutName = true)
    public int track_id;
    @IntArray(withoutName = true, splitter = ",")
    public int[] track_coords; // begin_x, begin_y, begin_z, end_x, end_y, end_z
}
