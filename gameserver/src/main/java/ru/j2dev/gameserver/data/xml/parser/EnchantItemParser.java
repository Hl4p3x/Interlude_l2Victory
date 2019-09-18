package ru.j2dev.gameserver.data.xml.parser;

import org.jdom2.Element;
import ru.j2dev.commons.data.xml.AbstractFileParser;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.EnchantItemHolder;
import ru.j2dev.gameserver.templates.item.ItemGrade;
import ru.j2dev.gameserver.templates.item.support.EnchantScroll;
import ru.j2dev.gameserver.templates.item.support.EnchantScrollOnFailAction;
import ru.j2dev.gameserver.templates.item.support.EnchantTargetType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class EnchantItemParser extends AbstractFileParser<EnchantItemHolder> {

    private EnchantItemParser() {
        super(EnchantItemHolder.getInstance());
    }

    public static EnchantItemParser getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public File getXMLFile() {
        return new File(Config.DATAPACK_ROOT, "data/xml/others/enchant_items.xml");
    }

    @Override
    protected void readData(final EnchantItemHolder holder, final Element rootElement) {
        for (final Element el : rootElement.getChildren()) {
            if ("scroll".equals(el.getName())) {
                final int scroll_id = Integer.parseInt(el.getAttributeValue("id"));
                final boolean infallible = Boolean.parseBoolean(el.getAttributeValue("infallible", "false"));
                EnchantScrollOnFailAction esofa = EnchantScrollOnFailAction.NONE;
                int rfl = 0;
                if (!infallible) {
                    esofa = EnchantScrollOnFailAction.valueOf(el.getAttributeValue("on_fail"));
                    rfl = Integer.parseInt(el.getAttributeValue("reset_lvl", "0"));
                }
                final double chance_bonus = Double.parseDouble(el.getAttributeValue("chance_bonus", "0"));
                final Set<ItemGrade> gradesSet = new HashSet<>();
                final String[] grades = el.getAttributeValue("grade") == null ? new String[]{"NONE"} : el.getAttributeValue("grade").split(";");
                for (String gradeArray : grades) {
                    gradesSet.add(ItemGrade.valueOf(gradeArray.toUpperCase()));
                }
                int minLvl = 0;
                int maxLvl = Config.ENCHANT_MAX;
                final int increment = Integer.parseInt(el.getAttributeValue("increment", "1"));
                EnchantTargetType ett = EnchantTargetType.ALL;
                final ArrayList<Integer> itemRestricted = new ArrayList<>();
                for (final Element el2 : el.getChildren()) {
                    if ("levels".equals(el2.getName())) {
                        minLvl = Integer.parseInt(el2.getAttributeValue("min"));
                        maxLvl = Integer.parseInt(el2.getAttributeValue("max"));
                    } else {
                        if (!"items_restrict".equals(el2.getName())) {
                            continue;
                        }
                        ett = EnchantTargetType.valueOf(el2.getAttributeValue("type"));
                        el2.getChildren("item").stream().map(itemElement -> Integer.valueOf(itemElement.getAttributeValue("id"))).forEach(itemRestricted::add);
                    }
                }
                final EnchantScroll es = new EnchantScroll(scroll_id, increment, chance_bonus, minLvl, maxLvl, ett, esofa, rfl, infallible, false);
                es.setItemGrades(gradesSet);
                if (!itemRestricted.isEmpty()) {
                    itemRestricted.forEach(es::addItemRestrict);
                }
                holder.addEnchantItem(es);
            } else {
                error("Unknown entry " + el.getName());
            }
        }
    }

    private static class LazyHolder {
        protected static final EnchantItemParser INSTANCE = new EnchantItemParser();
    }
}
