package ru.j2dev.dataparser.holder.freeway;

import ru.j2dev.dataparser.annotations.Element;
import ru.j2dev.dataparser.annotations.value.IntValue;
import ru.j2dev.dataparser.annotations.value.ObjectValue;
import ru.j2dev.dataparser.annotations.value.StringValue;
import ru.j2dev.dataparser.common.Point3;

import java.util.List;

/**
 * @author : Camelion
 * @date : 27.08.12 13:16
 * <p/>
 * Что-то, связанное с decodata.txt
 */
public class Freeway {
    @IntValue
    public int freeway_id; // ID
    @StringValue
    public String freeway_name; // Название
    @Element(start = "node_begin", end = "node_end")
    public List<FreewayNode> nodes; // Список точек

    public static class FreewayNode {
        @IntValue
        public int node_id; // Ид точки
        @ObjectValue
        public Point3 pos; // Координата
        @IntValue
        public int delay; // Задержка перед выполнением действия
        @IntValue
        public int social; // Социальное действие в этой точке (-1 = нет
        // действия)
        @IntValue
        public int state; // Неизвестно, всегда -1
    }
}
