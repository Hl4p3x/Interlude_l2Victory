package ru.j2dev.dataparser.holder.setting;

import ru.j2dev.dataparser.annotations.array.IntArray;
import ru.j2dev.dataparser.annotations.array.StringArray;
import ru.j2dev.dataparser.annotations.value.IntValue;
import ru.j2dev.dataparser.annotations.value.ObjectValue;
import ru.j2dev.dataparser.common.ItemName_Count;

/**
 * @author : Camelion
 * @date : 23.08.12 1:10
 * <p/>
 * Содержит в себе информацию о настройках олимпиады
 * <p/>
 * Не обработаны параметры give_skill_class, give_skill_classless,
 * give_skill_team Так как эти параметры во первых пусты, во вторых - пока
 * не понятно, какая структура наиболее подходит к ним
 */
public class OlympiadGeneralSetting {
    // Запрещенное на олимпиаде оружие.
    @StringArray(name = "forbidden_weapon", canBeNull = false)
    public String[] forbidden_weapons;
    // Награда за внеклассовые бои
    @ObjectValue(canBeNull = false)
    public ItemName_Count classless_bonus;
    // Награда за классовые бои
    @ObjectValue(canBeNull = false)
    public ItemName_Count class_bonus;
    // Награда за командные бои
    @ObjectValue(canBeNull = false)
    public ItemName_Count team_bonus;
    // Время запуска олимпиады (часы:минуты)
    @IntArray(canBeNull = false, splitter = ":")
    public int[] olympiad_time_setting;
    // Неизвестно
    @IntArray(canBeNull = false)
    public int[] olympiad_point_denominator;
    // Неизвестно
    @IntArray(canBeNull = false)
    public int[] olympiad_point_weight;
    // Точно не известно, предположительно сброс отката скилов
    @IntValue
    public int olympiad_reset_skill;
    // Список скилов, которые не будут сбрасываться
    @StringArray
    public String[] olympiad_reset_skill_excluded;
}
