package ru.j2dev.dataparser.holder.minigame;

import ru.j2dev.dataparser.annotations.Element;
import ru.j2dev.dataparser.annotations.array.ObjectArray;
import ru.j2dev.dataparser.annotations.array.StringArray;
import ru.j2dev.dataparser.annotations.value.IntValue;
import ru.j2dev.dataparser.annotations.value.ObjectValue;
import ru.j2dev.dataparser.annotations.value.StringValue;
import ru.j2dev.dataparser.common.ItemName_Count;
import ru.j2dev.dataparser.common.Point3;
import ru.j2dev.dataparser.common.Point4;

import java.util.List;

/**
 * @author : Camelion
 * @date : 30.08.12 13:34
 */
public class BlockupsetSettings {
    @IntValue
    public int game_period;
    @StringArray
    public String[] blue_enter_skill;
    @StringArray
    public String[] red_enter_skill;
    @StringArray
    public String[] waiting_skill;
    @ObjectValue
    public ItemName_Count default_reward;
    @ObjectValue
    public WinnerReward winner_reward;
    @StringArray
    public String[] delete_items_after_match;
    @Element(start = "blockupset_stage_begin", end = "blockupset_stage_end")
    public List<BlockupsetStage> stages;

    public static class BlockupsetStage {
        @IntValue
        public int stage;
        @ObjectArray
        public Point4[] blockupset_zone_territory;
        @Element(start = "red_start_point_begin", end = "red_start_point_end")
        public PointList red_start_point;
        @Element(start = "blue_start_point_begin", end = "blue_start_point_end")
        public PointList blue_start_point;
        @Element(start = "red_banish_point_begin", end = "red_banish_point_end")
        public PointList red_banish_point;
        @Element(start = "blue_banish_point_begin", end = "blue_banish_point_end")
        public PointList blue_banish_point;
    }

    public static class WinnerReward {
        @StringValue(withoutName = true)
        public String item_name;
        @IntValue(withoutName = true)
        public int count;
        @IntValue(withoutName = true)
        public int unknown;
    }

    public static class PointList {
        @ObjectValue(name = "point")
        public List<Point3> points;
    }
}
