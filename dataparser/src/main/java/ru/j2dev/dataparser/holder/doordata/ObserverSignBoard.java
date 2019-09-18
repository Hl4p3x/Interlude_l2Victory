package ru.j2dev.dataparser.holder.doordata;

import ru.j2dev.dataparser.annotations.class_annotations.ParseSuper;
import ru.j2dev.dataparser.annotations.value.StringValue;

import java.util.List;

/**
 * @author : Camelion
 * @date : 26.08.12 22:31
 */
@ParseSuper
public class ObserverSignBoard extends DefaultSignBoard {
    @StringValue
    public String display_npc; // неизвестно, есть в npcdata.txt
    // Заполняется через SignBoardObjectFactory
    public List<ObserverGroup> observers;

    // Создается и заполняется через SignBoardObjectFactory
    public static class ObserverGroup {
        public int observer_group; // Id группы, устанавливается через
        // SignBoardObjectFactory
        public List<int[]> observer_poses; // Неизвестно, какие - то координаты,
        // и ещё куча информации
    }
}
