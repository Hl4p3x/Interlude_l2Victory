package ru.j2dev.dataparser.holder.itemdata;


import ru.j2dev.dataparser.annotations.array.IntArray;
import ru.j2dev.dataparser.annotations.value.IntValue;
import ru.j2dev.dataparser.annotations.value.StringValue;

/**
 * @author : Camelion
 * @date : 28.08.12 23:57
 */
public class ItemSet {
    @IntValue(withoutName = true)
    public int set_id;
    @IntArray
    public int[] slot_chest;
    @IntArray
    public int[] slot_legs;
    @IntArray
    public int[] slot_head;
    @IntArray
    public int[] slot_feet;
    @IntArray
    public int[] slot_gloves;
    @IntArray
    public int[] slot_lhand;
    @StringValue
    public String slot_additional;
    @StringValue
    public String set_skill;
    @StringValue
    public String set_effect_skill;
    @StringValue
    public String set_additional_effect_skill;
    @IntValue
    public int set_additional2_condition;
    @StringValue
    public String set_additional2_effect_skill;
    @IntArray
    public int[] str_inc;
    @IntArray
    public int[] con_inc;
    @IntArray
    public int[] dex_inc;
    @IntArray
    public int[] int_inc;
    @IntArray
    public int[] men_inc;
    @IntArray
    public int[] wit_inc;
}
