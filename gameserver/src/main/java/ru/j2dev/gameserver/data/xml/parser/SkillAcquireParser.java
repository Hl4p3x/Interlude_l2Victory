package ru.j2dev.gameserver.data.xml.parser;

import gnu.trove.map.hash.TIntObjectHashMap;
import org.jdom2.Element;
import ru.j2dev.commons.data.xml.AbstractDirParser;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.SkillAcquireHolder;
import ru.j2dev.gameserver.model.SkillLearn;
import ru.j2dev.gameserver.model.base.ClassType2;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class SkillAcquireParser extends AbstractDirParser<SkillAcquireHolder> {

    protected SkillAcquireParser() {
        super(SkillAcquireHolder.getInstance());
    }

    public static SkillAcquireParser getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public File getXMLDir() {
        return new File(Config.DATAPACK_ROOT, "data/xml/skill_tree/");
    }

    @Override
    public boolean isIgnored(final File b) {
        return false;
    }

    @Override
    protected void readData(final SkillAcquireHolder holder, final Element rootElement) {
        Iterator<Element> iterator = rootElement.getChildren("pledge_skill_tree").iterator();
        while (iterator.hasNext()) {
            holder.addAllPledgeLearns(parseSkillLearn(iterator.next()));
        }
        rootElement.getChildren("fishing_skill_tree").forEach(nxt -> nxt.getChildren("race").forEach(classElement -> {
            final int race = Integer.parseInt(classElement.getAttributeValue("id"));
            final List<SkillLearn> learns = parseSkillLearn(classElement);
            holder.addAllFishingLearns(race, learns);
        }));
        iterator = rootElement.getChildren("normal_skill_tree").iterator();
        while (iterator.hasNext()) {
            final TIntObjectHashMap<List<SkillLearn>> map = new TIntObjectHashMap<>();
            final Element nxt2 = iterator.next();
            nxt2.getChildren("class").forEach(classElement2 -> {
                final int classId = Integer.parseInt(classElement2.getAttributeValue("id"));
                final List<SkillLearn> learns2 = parseSkillLearn(classElement2);
                map.put(classId, learns2);
            });
            holder.addAllNormalSkillLearns(map);
        }
    }

    private List<SkillLearn> parseSkillLearn(final Element tree) {
        final List<SkillLearn> skillLearns = new ArrayList<>();
        tree.getChildren("skill").forEach(element -> {
            final int id = Integer.parseInt(element.getAttributeValue("id"));
            final int level = Integer.parseInt(element.getAttributeValue("level"));
            final int cost = (element.getAttributeValue("cost") == null) ? 0 : Integer.parseInt(element.getAttributeValue("cost"));
            final int min_level = Integer.parseInt(element.getAttributeValue("min_level"));
            final int item_id = (element.getAttributeValue("item_id") == null) ? 0 : Integer.parseInt(element.getAttributeValue("item_id"));
            final long item_count = (element.getAttributeValue("item_count") == null) ? 1L : Long.parseLong(element.getAttributeValue("item_count"));
            final boolean clicked = element.getAttributeValue("clicked") != null && Boolean.parseBoolean(element.getAttributeValue("clicked"));
            final boolean autoLearn = Boolean.parseBoolean(element.getAttributeValue("auto_learn", "true"));
            final ClassType2 classtype2 = ClassType2.valueOf(element.getAttributeValue("classtype2", "None"));
            skillLearns.add(new SkillLearn(id, level, min_level, cost * Config.SKILL_COST_RATE, item_id, item_count, clicked, classtype2, autoLearn));
        });
        return skillLearns;
    }

    private static class LazyHolder {
        protected static final SkillAcquireParser INSTANCE = new SkillAcquireParser();
    }
}
