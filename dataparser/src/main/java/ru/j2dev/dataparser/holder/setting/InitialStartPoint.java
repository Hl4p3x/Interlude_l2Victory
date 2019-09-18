package ru.j2dev.dataparser.holder.setting;

import ru.j2dev.dataparser.annotations.Element;
import ru.j2dev.dataparser.annotations.array.EnumArray;
import ru.j2dev.dataparser.annotations.array.IntArray;

import java.util.List;

/**
 * @author : Camelion
 * @date : 21.08.12 22:23
 * <p/>
 * Класс, хранящий в себе координаты появления новых персонажей Координата
 * выбирается случайным образом из списка, который зависит от класса
 * игрока
 */
public class InitialStartPoint {
    @Element(start = "point_begin", end = "point_end")
    public List<StartPoint> points;

    public List<StartPoint> getPoints() {
        return points;
    }

    public enum PlayerClasses {
        human_fighter,
        human_magician,
        elf_fighter,
        elf_magician,
        darkelf_fighter,
        darkelf_magician,
        orc_fighter,
        orc_shaman,
        dwarf_apprentice,
        kamael_m_soldier,
        kamael_f_soldier
    }

    public static class StartPoint {
        // Список точек.
        @IntArray(name = "point", canBeNull = false)
        private List<int[]> points;
        // Целевые классы игроков
        @EnumArray(name = "class")
        private PlayerClasses[] classes;

        public List<int[]> getPoints() {
            return points;
        }

        public PlayerClasses[] getClasses() {
            return classes;
        }
    }
}