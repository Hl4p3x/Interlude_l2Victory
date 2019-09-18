package ru.j2dev.dataparser.holder.itemdata.item.ec_cond;

import ru.j2dev.dataparser.annotations.array.IntArray;
import ru.j2dev.dataparser.annotations.class_annotations.ParseSuper;

/**
 * @author : Camelion
 * @date : 28.08.12  12:44
 */
@ParseSuper
public class ECAgitNum extends DefaultEquipCond {
    @IntArray(withoutName = true)
    public int[] agit_nums;
}
