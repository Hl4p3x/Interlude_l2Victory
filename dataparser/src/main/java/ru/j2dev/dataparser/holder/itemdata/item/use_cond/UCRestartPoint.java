package ru.j2dev.dataparser.holder.itemdata.item.use_cond;

import ru.j2dev.dataparser.annotations.class_annotations.ParseSuper;
import ru.j2dev.dataparser.annotations.value.IntValue;

/**
 * @author : Camelion
 * @date : 28.08.12  5:02
 */
@ParseSuper
public class UCRestartPoint extends DefaultUseCond {
    @IntValue(withoutName = true)
    public int point_id;
}
