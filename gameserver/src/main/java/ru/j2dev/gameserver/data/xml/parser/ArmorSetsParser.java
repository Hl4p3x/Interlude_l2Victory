package ru.j2dev.gameserver.data.xml.parser;

import org.jdom2.Element;
import ru.j2dev.commons.data.xml.AbstractFileParser;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.ArmorSetsHolder;
import ru.j2dev.gameserver.model.ArmorSet;

import java.io.File;

public final class ArmorSetsParser extends AbstractFileParser<ArmorSetsHolder> {

    private ArmorSetsParser() {
        super(ArmorSetsHolder.getInstance());
    }

    public static ArmorSetsParser getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public File getXMLFile() {
        return new File(Config.DATAPACK_ROOT, "data/xml/others/armor_sets.xml");
    }

    @Override
    protected void readData(final ArmorSetsHolder holder, final Element rootElement) {
        rootElement.getChildren("set").forEach(element -> {
            String[] chest = null;
            String[] legs = null;
            String[] head = null;
            String[] gloves = null;
            String[] feet = null;
            String[] skills = null;
            String[] shield = null;
            String[] shield_skills = null;
            String[] enchant6skills = null;
            final int id = Integer.parseInt(element.getAttributeValue("id"));
            if (element.getAttributeValue("chest") != null) {
                chest = element.getAttributeValue("chest").split(";");
            }
            if (element.getAttributeValue("legs") != null) {
                legs = element.getAttributeValue("legs").split(";");
            }
            if (element.getAttributeValue("head") != null) {
                head = element.getAttributeValue("head").split(";");
            }
            if (element.getAttributeValue("gloves") != null) {
                gloves = element.getAttributeValue("gloves").split(";");
            }
            if (element.getAttributeValue("feet") != null) {
                feet = element.getAttributeValue("feet").split(";");
            }
            if (element.getAttributeValue("skills") != null) {
                skills = element.getAttributeValue("skills").split(";");
            }
            if (element.getAttributeValue("shield") != null) {
                shield = element.getAttributeValue("shield").split(";");
            }
            if (element.getAttributeValue("shield_skills") != null) {
                shield_skills = element.getAttributeValue("shield_skills").split(";");
            }
            if (element.getAttributeValue("enchant6skills") != null) {
                enchant6skills = element.getAttributeValue("enchant6skills").split(";");
            }
            holder.addArmorSet(new ArmorSet(id, chest, legs, head, gloves, feet, skills, shield, shield_skills, enchant6skills));
        });
    }

    private static class LazyHolder {
        private static final ArmorSetsParser INSTANCE = new ArmorSetsParser();
    }
}
