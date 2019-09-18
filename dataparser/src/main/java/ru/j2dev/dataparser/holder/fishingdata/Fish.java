package ru.j2dev.dataparser.holder.fishingdata;


import ru.j2dev.dataparser.annotations.value.DoubleValue;
import ru.j2dev.dataparser.annotations.value.EnumValue;
import ru.j2dev.dataparser.annotations.value.IntValue;
import ru.j2dev.dataparser.annotations.value.StringValue;

import static ru.j2dev.dataparser.holder.fishingdata.FishingDistribution.*;

/**
 * @author : Camelion
 * @date : 27.08.12 3:13
 */
public class Fish {
    @IntValue
    public int fish_id;
    @IntValue
    public int item_id;
    @StringValue
    public String item_name;
    @EnumValue
    public FishGroup fish_group;
    @IntValue
    public int fish_level;
    @DoubleValue
    public double fish_bite_rate;
    @DoubleValue
    public double fish_guts;
    @IntValue
    public int fish_hp;
    @IntValue
    public int fish_max_length;
    @DoubleValue
    public double fish_length_rate;
    @DoubleValue
    public double hp_regen;
    @IntValue
    public int start_combat_time;
    @IntValue
    public int combat_duration;
    @IntValue
    public int guts_check_time;
    @DoubleValue
    public double guts_check_probability;
    @DoubleValue
    public double cheating_prob;
    @EnumValue
    public FishGrade fish_grade;

    public enum FishGrade {
        fish_normal,
        fish_easy,
        fish_hard
    }
}
