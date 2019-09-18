package ru.j2dev.dataparser.holder.itemdata.item.ec_cond;

import ru.j2dev.dataparser.annotations.class_annotations.ParseSuper;
import ru.j2dev.dataparser.annotations.value.IntValue;

/**
 * @author : Camelion
 * @date : 28.08.12  12:36
 */
@ParseSuper
public class ECFortress extends DefaultEquipCond {
    @IntValue(withoutName = true)
    public int fortress_id;
}
