package ru.j2dev.dataparser.holder.cubicdata;

import ru.j2dev.dataparser.annotations.array.IntArray;
import ru.j2dev.dataparser.annotations.class_annotations.ParseSuper;
import ru.j2dev.dataparser.annotations.value.IntValue;
import ru.j2dev.dataparser.annotations.value.StringValue;

/**
 * @author : Camelion
 * @date : 26.08.12 13:19
 */
@ParseSuper
public class Agathion extends DefaultCubicData {
    @StringValue
    public String npc_name; // имя Npc, соответствующего агатиону (есть в
    // npcdata.txt)
    @IntArray
    public int[] item_ids = new int[0]; // Какие_то ID предметов
    @IntValue
    public int energy; // Какая-то энергия, активно везде, где item_ids.length >
    // 0
    @IntValue
    public int max_energy; // Максимальное кол-во энергии, активно везде, где
    // item_ids.length > 0
    // Присутствуют только у трех агатионов
    public AgathionTimeSkill timeskill1;
    public AgathionTimeSkill timeskill2;
    public AgathionTimeSkill timeskill3;

    @Override
    public boolean isAgathion() {
        return true;
    }

    public static class AgathionTimeSkill {
        public String skill_name; // Название скила
        public int targetStaticObject;
        public TargetType skill_target_type; // тип цели
        public int unknown; // Неизвестное значение
    }
}
