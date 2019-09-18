package ru.j2dev.dataparser.holder.itemdata.item.use_cond;

import ru.j2dev.dataparser.annotations.array.IntArray;
import ru.j2dev.dataparser.annotations.class_annotations.ParseSuper;

/**
 * @author : Camelion
 * @date : 28.08.12  4:52
 */
@ParseSuper
public class UCRace extends DefaultUseCond {
    @IntArray(withoutName = true)
    public int[] races;
}
