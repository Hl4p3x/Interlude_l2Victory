package ru.j2dev.dataparser.holder.monrace;

import ru.j2dev.dataparser.annotations.array.ObjectArray;
import ru.j2dev.dataparser.annotations.value.IntValue;
import ru.j2dev.dataparser.annotations.value.ObjectValue;
import ru.j2dev.dataparser.annotations.value.StringValue;
import ru.j2dev.dataparser.common.Point3;

/**
 * @author : Camelion
 * @date : 30.08.12 14:05
 */
public class MonRace {
    @IntValue
    public int return_rate;
    @IntValue
    public int residenceid;
    @StringValue
    public String begin_music;
    @StringValue
    public String begin_sound;
    @ObjectArray(name = "race_area")
    public MonArea[] race_areas;

    public static class MonArea {
        @IntValue(withoutName = true)
        public int unknown;
        @ObjectValue(withoutName = true)
        public Point3 point1;
        @ObjectValue(withoutName = true)
        public Point3 point2;
        @ObjectValue(withoutName = true)
        public Point3 point3;
        @ObjectValue(withoutName = true)
        public Point3 point4;
    }
}
