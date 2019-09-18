package ru.j2dev.gameserver.data.xml.parser;

import org.jdom2.Element;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.OptionDataHolder;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.stats.triggers.TriggerInfo;
import ru.j2dev.gameserver.stats.triggers.TriggerType;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.templates.OptionDataTemplate;

import java.io.File;

public final class OptionDataParser extends StatParser<OptionDataHolder> {

    protected OptionDataParser() {
        super(OptionDataHolder.getInstance());
    }

    public static OptionDataParser getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public File getXMLDir() {
        return new File(Config.DATAPACK_ROOT, "data/xml/stats/optiondata");
    }

    @Override
    public boolean isIgnored(final File f) {
        return false;
    }

    @Override
    protected void readData(final OptionDataHolder holder, final Element rootElement) {
        rootElement.getChildren().forEach(optionDataElement -> {
            final OptionDataTemplate template = new OptionDataTemplate(Integer.parseInt(optionDataElement.getAttributeValue("id")));
            for (final Element subElement : optionDataElement.getChildren()) {
                final String subName = subElement.getName();
                if ("for".equalsIgnoreCase(subName)) {
                    parseFor(subElement, template);
                } else if ("trigger".equalsIgnoreCase(subName)) {
                    final int id = parseNumber(subElement.getAttributeValue("id")).intValue();
                    final int level = parseNumber(subElement.getAttributeValue("level")).intValue();
                    final TriggerType t = TriggerType.valueOf(subElement.getAttributeValue("type"));
                    final double chance = parseNumber(subElement.getAttributeValue("chance")).doubleValue();
                    final TriggerInfo trigger = new TriggerInfo(id, level, t, chance);
                    template.addTrigger(trigger);
                } else {
                    if (!"skill".equalsIgnoreCase(subName)) {
                        return;
                    }
                    final int id = Integer.parseInt(subElement.getAttributeValue("id"));
                    final int level = Integer.parseInt(subElement.getAttributeValue("level"));
                    final Skill skill = SkillTable.getInstance().getInfo(id, level);
                    if (skill != null) {
                        template.addSkill(skill);
                    } else {
                        info("Skill not found(" + id + "," + level + ") for option data:" + template.getId() + "; file:" + getCurrentFileName());
                    }
                }
            }
            holder.addTemplate(template);
        });
    }

    @Override
    protected Object getTableValue(final String name) {
        return null;
    }

    private static class LazyHolder {
        protected static final OptionDataParser INSTANCE = new OptionDataParser();
    }
}
