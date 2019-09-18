package ru.j2dev.gameserver.data.xml.parser;

import org.jdom2.Element;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.data.xml.holder.OptionDataHolder;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.stats.conditions.Condition;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.templates.OptionDataTemplate;
import ru.j2dev.gameserver.templates.StatsSet;
import ru.j2dev.gameserver.templates.item.*;
import ru.j2dev.gameserver.templates.item.ItemTemplate.ItemClass;

import java.io.File;

public final class ItemTemplateParser extends StatParser<ItemTemplateHolder> {

    protected ItemTemplateParser() {
        super(ItemTemplateHolder.getInstance());
    }

    public static ItemTemplateParser getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public File getXMLDir() {
        return new File(Config.DATAPACK_ROOT, "data/xml/stats/items/");
    }

    @Override
    public boolean isIgnored(final File f) {
        return false;
    }

    @Override
    protected void readData(final ItemTemplateHolder holder, final Element rootElement) {
        for (final Element itemElement : rootElement.getChildren()) {
            final StatsSet set = new StatsSet();
            set.set("item_id", itemElement.getAttributeValue("id"));
            set.set("name", itemElement.getAttributeValue("name"));
            set.set("add_name", itemElement.getAttributeValue("add_name", ""));
            int slot = 0;
            for (final Element subElement : itemElement.getChildren()) {
                final String subName = subElement.getName();
                if ("set".equalsIgnoreCase(subName)) {
                    set.set(subElement.getAttributeValue("name"), subElement.getAttributeValue("value"));
                } else {
                    if (!"equip".equalsIgnoreCase(subName)) {
                        continue;
                    }
                    for (final Element slotElement : subElement.getChildren()) {
                        final Bodypart bodypart = Bodypart.valueOf(slotElement.getAttributeValue("id"));
                        if (bodypart.getReal() != null) {
                            slot = bodypart.mask();
                        } else {
                            slot |= bodypart.mask();
                        }
                    }
                }
            }
            set.set("bodypart", slot);
            ItemTemplate template;
            try {
                if ("weapon".equalsIgnoreCase(itemElement.getName())) {
                    if (!set.containsKey("class")) {
                        if ((slot & 0x100) > 0) {
                            set.set("class", ItemClass.ARMOR);
                        } else {
                            set.set("class", ItemClass.WEAPON);
                        }
                    }
                    template = new WeaponTemplate(set);
                } else if ("armor".equalsIgnoreCase(itemElement.getName())) {
                    if (!set.containsKey("class")) {
                        if ((slot & 0x2BF40) > 0) {
                            set.set("class", ItemClass.ARMOR);
                        } else if ((slot & 0x3E) > 0) {
                            set.set("class", ItemClass.JEWELRY);
                        } else {
                            set.set("class", ItemClass.ACCESSORY);
                        }
                    }
                    template = new ArmorTemplate(set);
                } else {
                    template = new EtcItemTemplate(set);
                }
            } catch (Exception e) {
                warn("Fail create item: " + set.get("item_id"), e);
                continue;
            }
            for (final Element subElement2 : itemElement.getChildren()) {
                final String subName2 = subElement2.getName();
                if ("for".equalsIgnoreCase(subName2)) {
                    parseFor(subElement2, template);
                } else if ("triggers".equalsIgnoreCase(subName2)) {
                    parseTriggers(subElement2, template);
                } else if ("skills".equalsIgnoreCase(subName2)) {
                    subElement2.getChildren().forEach(nextElement -> {
                        final int id = Integer.parseInt(nextElement.getAttributeValue("id"));
                        final int level = Integer.parseInt(nextElement.getAttributeValue("level"));
                        final Skill skill = SkillTable.getInstance().getInfo(id, level);
                        if (skill != null) {
                            template.attachSkill(skill);
                        } else {
                            info("Skill not found(" + id + "," + level + ") for item:" + set.getObject("item_id") + "; file:" + getCurrentFileName());
                        }
                    });
                } else if ("enchant4_skill".equalsIgnoreCase(subName2)) {
                    final int id2 = Integer.parseInt(subElement2.getAttributeValue("id"));
                    final int level2 = Integer.parseInt(subElement2.getAttributeValue("level"));
                    final Skill skill2 = SkillTable.getInstance().getInfo(id2, level2);
                    if (skill2 == null) {
                        continue;
                    }
                    template.setEnchant4Skill(skill2);
                } else if ("cond".equalsIgnoreCase(subName2)) {
                    final Condition condition = parseFirstCond(subElement2);
                    if (condition == null) {
                        continue;
                    }
                    final int msgId = parseNumber(subElement2.getAttributeValue("msgId")).intValue();
                    condition.setSystemMsg(msgId);
                    template.addCondition(condition);
                } else if ("attributes".equalsIgnoreCase(subName2)) {
                    final int[] attributes = new int[6];
                    subElement2.getChildren().stream().filter(nextElement2 -> "attribute".equalsIgnoreCase(nextElement2.getName())).forEach(nextElement2 -> {
                        final ru.j2dev.gameserver.model.base.Element element = ru.j2dev.gameserver.model.base.Element.getElementByName(nextElement2.getAttributeValue("element"));
                        attributes[element.getId()] = Integer.parseInt(nextElement2.getAttributeValue("value"));
                    });
                    template.setBaseAtributeElements(attributes);
                } else {
                    if (!"enchant_options".equalsIgnoreCase(subName2)) {
                        continue;
                    }
                    for (final Element nextElement : subElement2.getChildren()) {
                        if ("level".equalsIgnoreCase(nextElement.getName())) {
                            final int val = Integer.parseInt(nextElement.getAttributeValue("val"));
                            int i = 0;
                            final int[] options = new int[3];
                            for (final Element optionElement : nextElement.getChildren()) {
                                final OptionDataTemplate optionData = OptionDataHolder.getInstance().getTemplate(Integer.parseInt(optionElement.getAttributeValue("id")));
                                if (optionData == null) {
                                    error("Not found option_data for id: " + optionElement.getAttributeValue("id") + "; item_id: " + set.get("item_id"));
                                } else {
                                    options[i++] = optionData.getId();
                                }
                            }
                            template.addEnchantOptions(val, options);
                        }
                    }
                }
            }
            holder.addItem(template);
        }
    }

    @Override
    protected Object getTableValue(final String name) {
        return null;
    }

    private static class LazyHolder {
        protected static final ItemTemplateParser INSTANCE = new ItemTemplateParser();
    }
}
