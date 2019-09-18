package ru.j2dev.dataparser.holder.itemdata.item.ec_cond;

import ru.j2dev.dataparser.annotations.array.LinkedArray;
import ru.j2dev.dataparser.annotations.class_annotations.ParseSuper;

/**
 * @author : Camelion
 * @date : 28.08.12  12:24
 */
@ParseSuper
public class ECCategory extends DefaultEquipCond {
    @LinkedArray(withoutName = true)
    public int[] categories;
}
