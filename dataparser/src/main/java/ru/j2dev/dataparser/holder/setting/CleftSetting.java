package ru.j2dev.dataparser.holder.setting;

import ru.j2dev.dataparser.annotations.Element;
import ru.j2dev.dataparser.annotations.array.IntArray;
import ru.j2dev.dataparser.annotations.array.ObjectArray;
import ru.j2dev.dataparser.annotations.array.StringArray;
import ru.j2dev.dataparser.annotations.value.IntValue;
import ru.j2dev.dataparser.annotations.value.ObjectValue;
import ru.j2dev.dataparser.common.ItemName_Count;
import ru.j2dev.dataparser.common.Point4;

import java.util.List;

/**
 * @author : Camelion
 * @date : 23.08.12 2:32
 * <p/>
 * Содержит в себе информацию о каком-то эвенте.
 */
public class CleftSetting {
    @ObjectArray
    public Point4[] cleft_zone_territory;
    // Точки старта синей и красной команды
    @Element(start = "red_start_point_begin", end = "red_start_point_end")
    public ListPoint redStartPoint;
    @Element(start = "blue_start_point_begin", end = "blue_start_point_end")
    public ListPoint blueStartPoint;
    // Какие-то точки для синей и красной команды
    @Element(start = "red_banish_point_begin", end = "red_banish_point_end")
    public ListPoint redBanishPoint;
    @Element(start = "blue_banish_point_begin", end = "blue_banish_point_end")
    public ListPoint blueBanishPoint;
    // Период категорий?
    @IntValue
    public int CAT_period;
    // Награда победителю
    @ObjectValue(canBeNull = false)
    public ItemName_Count winner_reward;
    // Награда проигравшему
    @ObjectValue(canBeNull = false)
    public ItemName_Count loser_reward;
    // Награда в SP победителю
    @IntValue
    public int winner_sp_bonus;
    // Награда в SP победителю
    @IntValue
    public int loser_sp_bonus;
    // Неизвестно. Возможно награда за убийство
    @IntValue
    public int kill_sp_bonus;
    @StringArray
    public String[] waiting_skill;

    public static class ListPoint {
        @IntArray(name = "point", canBeNull = false)
        public List<int[]> points;
    }
}
