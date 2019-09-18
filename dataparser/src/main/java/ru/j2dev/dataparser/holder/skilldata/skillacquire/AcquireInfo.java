package ru.j2dev.dataparser.holder.skilldata.skillacquire;

import ru.j2dev.dataparser.annotations.Element;
import ru.j2dev.dataparser.annotations.value.StringValue;

import java.util.List;

/**
 * @author KilRoy
 */
public class AcquireInfo {
    @StringValue(withoutName = true)
    private String parrent_class;
    @Element(start = "skill_begin", end = "skill_end")
    private List<SkillAcquireInfo> skillAcquireInfo;

    public List<SkillAcquireInfo> getSkillAcquireInfo() {
        return skillAcquireInfo;
    }

    public SkillAcquireInfo getSkillInfo(final String skillName) {
        return skillAcquireInfo.stream().filter(s -> skillName.equals(s.getSkillName())).findFirst().get();
    }
}