package ru.j2dev.gameserver.data.xml.parser;

import org.jdom2.Element;
import ru.j2dev.commons.data.xml.AbstractFileParser;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.PolymorphedHolder;
import ru.j2dev.gameserver.model.base.ClassId;
import ru.j2dev.gameserver.tables.CharTemplateTable;
import ru.j2dev.gameserver.templates.PlayerTemplate;
import ru.j2dev.gameserver.templates.npc.polymorphed.PolymorphedData;

import java.io.File;
import java.util.Arrays;

/**
 * Created by JunkyFunky
 * on 03.01.2018 17:28
 * group j2dev
 */
public class PolymorphedParser extends AbstractFileParser<PolymorphedHolder> {

    protected PolymorphedParser() {
        super(PolymorphedHolder.getInstance());
    }

    public static PolymorphedParser getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public File getXMLFile() {
        return new File(Config.DATAPACK_ROOT, "data/xml/polymorphed/polymorphed.xml");
    }

    @Override
    protected void readData(final PolymorphedHolder holder, Element rootElement) {
        for (final Element polymorphedElement : rootElement.getChildren()) {
            if ("polymorphed".equalsIgnoreCase(polymorphedElement.getName())) {
                final PolymorphedData polymorphedData = new PolymorphedData(Integer.parseInt(polymorphedElement.getAttributeValue("npcId")));

                for (final Element subElement : polymorphedElement.getChildren()) {
                    if (subElement.getName().equalsIgnoreCase("class_settings")) {
                        final int classId = Integer.parseInt(subElement.getAttributeValue("classId"));
                        polymorphedData.setClassId(classId);
                        if (subElement.getAttribute("hairStyle ") != null) {
                            final int hairStyle = Integer.parseInt(subElement.getAttributeValue("hairStyle"));
                            polymorphedData.setHairStyle(hairStyle);
                        }
                        if (subElement.getAttribute("sex ") != null) {
                            final int sex = Integer.parseInt(subElement.getAttributeValue("sex"));
                            polymorphedData.setSex(sex);
                        }
                        if (subElement.getAttribute("hairColor ") != null) {
                            final int hairColor = Integer.parseInt(subElement.getAttributeValue("hairColor"));
                            polymorphedData.setHairColor(hairColor);
                        }
                        if (subElement.getAttribute("face ") != null) {
                            final int face = Integer.parseInt(subElement.getAttributeValue("face"));
                            polymorphedData.setFace(face);
                        }
                        if (subElement.getAttribute("hero ") != null) {
                            final int hero = Integer.parseInt(subElement.getAttributeValue("hero"));
                            polymorphedData.setHero(hero);
                        }
                        if (subElement.getAttribute("magicRate ") != null) {
                            final int magicRate = Integer.parseInt(subElement.getAttributeValue("magicRate"));
                            polymorphedData.setMagicRate(magicRate);
                        }

                        final int race = ClassId.findById(classId).getRace().ordinal();
                        polymorphedData.setRace(race);
                        PlayerTemplate template = CharTemplateTable.getInstance().getTemplate(classId, polymorphedData.getSex() == 1);
                        polymorphedData.setCollisionHeight((int) template.getCollisionHeight());
                        polymorphedData.setCollisionRadius((int) template.getCollisionRadius());

                        final String items = subElement.getAttributeValue("itemId");
                        for (String item_id : items.split(";")) {
                            polymorphedData.addItem(Integer.parseInt(item_id));
                        }
                    }
                    if (subElement.getName().equalsIgnoreCase("colors")) {
                        final String titleColor = subElement.getAttributeValue("title_color");
                        Arrays.stream(titleColor.split(";")).map(String::toLowerCase).forEach(polymorphedData::addTitleColors);
                        final String nameColor = subElement.getAttributeValue("name_color");
                        for (String color : nameColor.split(";")) {
                            polymorphedData.addNameColors(color.toLowerCase());
                        }
                        if (subElement.getAttribute("recomendation ") != null) {
                            final int recomendation = Integer.parseInt(subElement.getAttributeValue("recomendation"));
                            polymorphedData.setRecomend(recomendation);
                        }

                        if (subElement.getAttribute("enchantLvl ") != null) {
                            final int enchant = Integer.parseInt(subElement.getAttributeValue("enchantLvl"));
                            polymorphedData.setWeaponEnchant(enchant);
                        }
                    }

                }

                holder.addPolymorphedData(polymorphedData);
            }
        }
    }

    private static class LazyHolder {
        protected static final PolymorphedParser INSTANCE = new PolymorphedParser();
    }
}
