package ru.j2dev.dataparser.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.dataparser.annotations.Element;
import ru.j2dev.dataparser.holder.skilldata.SkillData;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author KilRoy
 */
public class SkillDataHolder extends AbstractHolder {
    private static final SkillDataHolder ourInstance = new SkillDataHolder();
    @Element(start = "skill_begin", end = "skill_end")
    private List<SkillData> skill;

    private SkillDataHolder() {
    }

    public static SkillDataHolder getInstance() {
        return ourInstance;
    }

    public List<SkillData> getSkills() {
        return new ArrayList<>(skill);
    }

    public List<SkillData> getSkillsByID(final int skillID) {
        return skill.stream().filter(skill -> skill.skill_id == skillID).collect(Collectors.toList());
    }

    public SkillData getFirstSkillByID(final int skillID) {
        return skill.stream().filter(skill -> skill.skill_id == skillID).findFirst().get();
    }

    public SkillData getSkillByIDAndLevel(final int skillID, final int level) {
        return skill.stream().filter(skill -> skill.skill_id == skillID && skill.level == level).findFirst().get();
    }

    public SkillData getSkillFromName(final String skillName) {
        return skill.stream().filter(skill -> skill.skill_name.equalsIgnoreCase(skillName)).findFirst().get();
    }

    @Override
    public int size() {
        return skill.size();
    }

    @Override
    public void clear() {
        skill.clear();
    }
}