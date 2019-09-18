package ru.j2dev.dataparser.holder.areadata.area;

import ru.j2dev.dataparser.annotations.array.LinkedArray;
import ru.j2dev.dataparser.annotations.class_annotations.ParseSuper;
import ru.j2dev.dataparser.annotations.value.IntValue;

/**
 * @author KilRoy
 */
@ParseSuper
public class SkilledZone extends DefaultArea {
    @LinkedArray
    private int[] skill_name; // название скила, используемого зоной

    @IntValue
    private int skill_prob; // неизвестно

    public SkilledZone(final DefaultArea defaultSetting) {
        super(defaultSetting);
        skill_name = ((SkilledZone) defaultSetting).skill_name;
        skill_prob = ((SkilledZone) defaultSetting).skill_prob;
    }

    public SkilledZone() {
    }

    public int[] getSkillIdLevel() {
        return skill_name;
    }

    public int getSkillProb() {
        return skill_prob;
    }
}