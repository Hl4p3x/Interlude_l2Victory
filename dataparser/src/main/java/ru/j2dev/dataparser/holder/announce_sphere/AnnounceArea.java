package ru.j2dev.dataparser.holder.announce_sphere;

import ru.j2dev.dataparser.annotations.array.ObjectArray;
import ru.j2dev.dataparser.annotations.value.StringValue;
import ru.j2dev.dataparser.common.Point4;

/**
 * @author : Camelion
 * @date : 24.08.12 21:28
 */
public class AnnounceArea {
    @StringValue(withoutName = true)
    public String string_id; // Id, по которому можно найти строку. Передается в
    // функции АИ gg::ShowMsgInTerritory
    @ObjectArray(withoutName = true)
    public Point4[] area_range; // Точки, которыми ограничена область
}
