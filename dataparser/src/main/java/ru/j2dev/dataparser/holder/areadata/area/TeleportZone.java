package ru.j2dev.dataparser.holder.areadata.area;

import ru.j2dev.dataparser.annotations.array.ObjectArray;
import ru.j2dev.dataparser.annotations.class_annotations.ParseSuper;
import ru.j2dev.dataparser.common.Point3;

/**
 * @author : Camelion
 * @date : 25.08.12  14:19
 */
@ParseSuper
public class TeleportZone extends DefaultArea {
    // Обязательно должно присутствовать для всех зон телепорта
    @ObjectArray(canBeNull = false)
    private Point3[] teleport_points; // Точки телепорта (если больше одной, то выбирается случайным образом)

    public TeleportZone(DefaultArea defaultSetting) {
        super(defaultSetting);
        teleport_points = ((TeleportZone) defaultSetting).teleport_points;
    }

    public TeleportZone() {

    }
}
