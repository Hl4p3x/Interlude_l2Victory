package ru.j2dev.dataparser.holder.areadata.area;

import ru.j2dev.dataparser.annotations.class_annotations.ParseSuper;
import ru.j2dev.dataparser.annotations.value.IntValue;

/**
 * @author : Camelion
 * @date : 25.08.12  2:19
 * <p/>
 * Зоны, в которых тем или иным образом персонажам наносится урон
 */
@ParseSuper
public class DamageZone extends DefaultArea {
    @IntValue
    private int damage_on_hp; // Урон по HP

    // Активно не для всех damage зон
    @IntValue
    private int damage_on_mp; // Урон по MP

    // Активно не для всех damage зон
    @IntValue
    private int message_no; // Номер сообщения из Systemmsg.dat эффекте зоны (вызывается с интервалом unit_tick)

    public DamageZone(DefaultArea defaultSetting) {
        super(defaultSetting);
        damage_on_hp = ((DamageZone) defaultSetting).damage_on_hp;
        damage_on_mp = ((DamageZone) defaultSetting).damage_on_mp;
        message_no = ((DamageZone) defaultSetting).message_no;
    }

    public DamageZone() {

    }
}
