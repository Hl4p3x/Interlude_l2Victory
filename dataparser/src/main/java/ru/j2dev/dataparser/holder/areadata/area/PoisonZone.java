package ru.j2dev.dataparser.holder.areadata.area;

import ru.j2dev.dataparser.annotations.array.LinkedArray;
import ru.j2dev.dataparser.annotations.class_annotations.ParseSuper;
import ru.j2dev.dataparser.annotations.value.EnumValue;
import ru.j2dev.dataparser.annotations.value.IntValue;
import ru.j2dev.dataparser.annotations.value.StringValue;
import ru.j2dev.dataparser.holder.areadata.SkillActionType;

/**
 * @author : Camelion
 * @date : 25.08.12  2:36
 */
@ParseSuper
public class PoisonZone extends DefaultArea {
    // Активно не для всех poison зон
    @LinkedArray
    private int[] skill_list; // Список скилов

    // Активно не для всех poison зон
    @EnumValue
    private SkillActionType skill_action_type; // Неизвестно, вероятно обозначение того, как будут использоваться скилы из skill_list

    // Активно не для всех poison зон (но присутствует в параметрах по умолчанию)
    @StringValue
    private String skill_name; // название скила, используемого зоной

    @IntValue
    private int skill_prob; // неизвестно


    // Активно не для всех poison зон
    @EnumValue
    private OnOffZoneParam show_dangerzone = OnOffZoneParam.off; // Показывать или нет значок опасной зоны?

    public PoisonZone(DefaultArea defaultSetting) {
        super(defaultSetting);
        skill_list = ((PoisonZone) defaultSetting).skill_list;
        skill_action_type = ((PoisonZone) defaultSetting).skill_action_type;
        skill_name = ((PoisonZone) defaultSetting).skill_name;
        skill_prob = ((PoisonZone) defaultSetting).skill_prob;
    }

    public PoisonZone() {

    }
}
