package ru.j2dev.gameserver.data.xml.parser;

import gnu.trove.map.hash.TIntIntHashMap;
import org.jdom2.Element;
import ru.j2dev.commons.data.xml.AbstractFileParser;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.CubicHolder;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.templates.CubicTemplate;
import ru.j2dev.gameserver.templates.CubicTemplate.ActionType;
import ru.j2dev.gameserver.templates.CubicTemplate.SkillInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class CubicParser extends AbstractFileParser<CubicHolder> {

    protected CubicParser() {
        super(CubicHolder.getInstance());
    }

    public static CubicParser getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public File getXMLFile() {
        return new File(Config.DATAPACK_ROOT, "data/xml/others/cubics.xml");
    }

    @Override
    protected void readData(final CubicHolder holder, final Element rootElement) {
        for (Element cubicElement : rootElement.getChildren()) {
            final int id = Integer.parseInt(cubicElement.getAttributeValue("id"));
            final int level = Integer.parseInt(cubicElement.getAttributeValue("level"));
            final int delay = Integer.parseInt(cubicElement.getAttributeValue("delay"));
            final CubicTemplate template = new CubicTemplate(id, level, delay);
            holder.addCubicTemplate(template);
            for (Element skillsElement : cubicElement.getChildren()) {
                final int chance = Integer.parseInt(skillsElement.getAttributeValue("chance"));
                final List<SkillInfo> skills = new ArrayList<>(1);
                for (Element skillElement : skillsElement.getChildren()) {
                    final int id2 = Integer.parseInt(skillElement.getAttributeValue("id"));
                    final int level2 = Integer.parseInt(skillElement.getAttributeValue("level"));
                    String val = skillElement.getAttributeValue("chance");
                    final int chance2 = (val == null) ? 0 : Integer.parseInt(val);
                    final boolean canAttackDoor = Boolean.parseBoolean(skillElement.getAttributeValue("can_attack_door"));
                    val = skillElement.getAttributeValue("min_hp");
                    final int minHp = (val == null) ? 0 : Integer.parseInt(val);
                    val = skillElement.getAttributeValue("min_hp_per");
                    final int minHpPer = (val == null) ? 0 : Integer.parseInt(val);
                    final ActionType type = ActionType.valueOf(skillElement.getAttributeValue("action_type"));
                    final TIntIntHashMap set = new TIntIntHashMap();
                    for (Element chanceElement : skillElement.getChildren()) {
                        final int min = Integer.parseInt(chanceElement.getAttributeValue("min"));
                        final int max = Integer.parseInt(chanceElement.getAttributeValue("max"));
                        final int value = Integer.parseInt(chanceElement.getAttributeValue("value"));
                        for (int i = min; i <= max; ++i) {
                            set.put(i, value);
                        }
                    }
                    if (chance2 == 0 && set.isEmpty()) {
                        warn("Wrong skill chance. Cubic: " + id + "/" + level);
                    }
                    final Skill skill = SkillTable.getInstance().getInfo(id2, level2);
                    if (skill != null) {
                        skill.setCubicSkill(true);
                        skills.add(new SkillInfo(skill, chance2, type, canAttackDoor, minHp, minHpPer, set));
                    }
                }
                template.putSkills(chance, skills);
            }
        }
    }

    private static class LazyHolder {
        private static final CubicParser INSTANCE = new CubicParser();
    }
}
