package ru.j2dev.dataparser.holder.setting;

import ru.j2dev.dataparser.annotations.Element;
import ru.j2dev.dataparser.annotations.array.IntArray;

/**
 * @author : Camelion
 * @date : 23.08.12 2:26
 * <p/>
 * Содержит в себе информацию о настройках неизвестного PVPMatch'а.
 */
public class PVPMatchSetting {
    @Element(start = "give_pvppoint_time_start", end = "give_pvppoint_time_end")
    public GivePVPPointTime givePVPPointTime;

    public static class GivePVPPointTime {
        // Время начала матча (часы:минуты)
        @IntArray(canBeNull = false, splitter = ":")
        public int[] give_pvppoint_start_time;
        // Время окончания матча (часы:минуты)
        @IntArray(canBeNull = false, splitter = ":")
        public int[] give_pvppoint_end_time;
        // Дни недели, по которым запускается матч
        @IntArray(canBeNull = false)
        public int[] give_pvppoint_week_days;
    }
}
