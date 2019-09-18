package ru.j2dev.dataparser.holder.dyedata;

import ru.j2dev.dataparser.annotations.array.IntArray;
import ru.j2dev.dataparser.annotations.value.IntValue;
import ru.j2dev.dataparser.annotations.value.StringValue;

/**
 * @author : Camelion
 * @date : 27.08.12 1:37
 */
public class DyeData {
    @StringValue
    public String dye_name; // Название, есть в itemdata.txt
    @IntValue
    public int dye_id; // ID
    @IntValue
    public int dye_item_id; // Item ID
    @IntValue
    public int dye_level; //
    @IntValue
    public int str; // STR
    @IntValue
    public int con; // CON
    @IntValue
    public int dex; // DEX
    @IntValue(name = "int")
    public int _int; // INT
    @IntValue
    public int men; // MEN
    @IntValue
    public int wit; // WIT
    @IntValue
    public int need_count; // Необходимое кол-во таких предметов для нанесения тату
    @IntValue
    public int wear_fee; // Вероятно, цена нанесения
    @IntValue
    public int cancel_count; // Количество предметов, возвращаемое при снятии тату
    @IntValue
    public int cancel_fee; // Цена снятия тату
    @IntArray
    public int[] wear_class; // Список классов, которым доступна эта тату
}
