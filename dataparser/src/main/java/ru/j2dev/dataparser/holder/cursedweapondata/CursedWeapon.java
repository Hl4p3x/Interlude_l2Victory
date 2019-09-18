package ru.j2dev.dataparser.holder.cursedweapondata;

import ru.j2dev.dataparser.annotations.value.IntValue;
import ru.j2dev.dataparser.annotations.value.StringValue;

/**
 * @author : Camelion
 * @date : 26.08.12 21:36
 */
public class CursedWeapon {
    @StringValue
    public String item_name; // Название предмета (есть в itemdata.txt)
    @IntValue
    public int create_rate_per_10min; // * 0.001
    @IntValue
    public int create_rate_per_npc; // * 0.001
    @IntValue
    public int drop_rate_ondying; // * 0.001
    @IntValue
    public int life_time; // Время жизни? В секундах
    @IntValue
    public int expire_by_nonpk; // Не известно, какое-то время (секунды)
    @IntValue
    public int expire_by_drop; // Не известно, какое-то время (секунды)
    @IntValue
    public int transform_id; // ID трансформации, которая появляется при подборе
    // этого оружия
}
