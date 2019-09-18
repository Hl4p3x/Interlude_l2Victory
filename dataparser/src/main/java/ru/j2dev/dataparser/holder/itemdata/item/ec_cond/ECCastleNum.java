package ru.j2dev.dataparser.holder.itemdata.item.ec_cond;

import ru.j2dev.dataparser.annotations.array.IntArray;
import ru.j2dev.dataparser.annotations.class_annotations.ParseSuper;

/**
 * @author : Camelion
 * @date : 28.08.12  12:43
 */
@ParseSuper
public class ECCastleNum extends DefaultEquipCond {
    @IntArray(withoutName = true)
    public int[] castle_ids;
}
