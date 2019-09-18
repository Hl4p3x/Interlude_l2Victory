package ru.j2dev.dataparser.holder.itemdata.item.use_cond;

import ru.j2dev.dataparser.annotations.array.LinkedArray;
import ru.j2dev.dataparser.annotations.class_annotations.ParseSuper;

/**
 * @author : Camelion
 * @date : 28.08.12  4:51
 */
@ParseSuper
public class UCCategory extends DefaultUseCond {
    @LinkedArray(withoutName = true)
    public int[] categories;
}
