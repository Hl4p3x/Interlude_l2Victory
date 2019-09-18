package ru.j2dev.dataparser.holder.areadata.area;

import ru.j2dev.dataparser.annotations.class_annotations.ParseSuper;
import ru.j2dev.dataparser.annotations.value.DoubleValue;
import ru.j2dev.dataparser.annotations.value.ObjectValue;

/**
 * @author : Camelion
 * @date : 24.08.12  23:43
 * <p/>
 * Водные области
 */
@ParseSuper
public class WaterArea extends DefaultArea {
    @ObjectValue(canBeNull = false)
    private WaterRange water_range;

    public WaterArea() {
    }

    public WaterArea(DefaultArea setting) {
        super(setting);
    }

    public WaterRange getWaterRange() {
        return water_range;
    }

    public static class WaterRange {
        // Min range
        @DoubleValue(withoutName = true)
        private double min_x;
        @DoubleValue(withoutName = true)
        private double min_y;
        @DoubleValue(withoutName = true)
        private double min_z;

        // Max range
        @DoubleValue(withoutName = true)
        private double max_x;
        @DoubleValue(withoutName = true)
        private double max_y;
        @DoubleValue(withoutName = true)
        private double max_z;

        public double getMinX() {
            return min_x;
        }

        public double getMinY() {
            return min_y;
        }

        public double getMinZ() {
            return min_z;
        }

        public double getMaxX() {
            return max_x;
        }

        public double getMaxY() {
            return max_y;
        }

        public double getMaxZ() {
            return max_z;
        }
    }
}
