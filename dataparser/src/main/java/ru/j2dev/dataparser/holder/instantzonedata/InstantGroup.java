package ru.j2dev.dataparser.holder.instantzonedata;

import ru.j2dev.dataparser.annotations.array.IntArray;
import ru.j2dev.dataparser.annotations.value.IntValue;
import ru.j2dev.dataparser.annotations.value.ObjectValue;
import ru.j2dev.dataparser.holder.instantzonedata.InstantZone.ResetBindig;

/**
 * @author : Camelion
 * @date : 27.08.12 15:39
 */
public class InstantGroup {
    @IntValue
    public int group_id; // ID группы
    @IntValue
    public int max_entrance; // Максимальное кол-во входов в один из инстансов
    // группы
    @IntValue
    public int extra_entrance; // Максимальное кол-во входов в один из инстансов
    // группы, с использованием спец. билетов
    @IntArray
    public int[] instantzone_list; // Список инстанс зон, попадающих в эту
    // группу
    @IntValue
    public int time_limit; // Время отката для группы
    @ObjectValue
    public ResetBindig reset_binding; // Время сброса откатов
}
