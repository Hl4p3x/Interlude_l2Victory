package ru.j2dev.dataparser.holder.setting;

import ru.j2dev.dataparser.annotations.array.StringArray;

/**
 * @author : Camelion
 * @date : 23.08.12 2:23
 * <p/>
 * Содержит в себе информацию о настройках героев
 */
public class HeroGeneralSetting {
    // Список скилов, выдаваемых герою
    @StringArray(canBeNull = false)
    public String[] hero_skill;
}
