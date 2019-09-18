package ru.j2dev.gameserver.data.xml.parser;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jdom2.Element;
import ru.j2dev.commons.data.xml.AbstractFileParser;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.AltStartHolder;
import ru.j2dev.gameserver.templates.item.StartItems;

import java.io.File;

/**
 * Solution
 * 15.08.2018
 * 17:29
 */

public class AltStartParser extends AbstractFileParser<AltStartHolder> {

    protected AltStartParser() {
        super(AltStartHolder.getInstance());
    }

    public static AltStartParser getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public File getXMLFile() {
        return new File(Config.DATAPACK_ROOT,"data/alt_start.xml");
    }

    @Override
    protected void readData(AltStartHolder holder, Element rootElement) {
        for(Element classElements : rootElement.getChildren()) {
            if ("class".equalsIgnoreCase(classElements.getName())) {
                final StartItems startItems = new StartItems(Integer.parseInt(classElements.getAttributeValue("id")));
                for(Element classChildren : classElements.getChildren()) {
                    if (classChildren.getName().equalsIgnoreCase("amunitions")) {
                        final String items = classChildren.getAttributeValue("idArmor");
                        for (String item_id : items.split(",")) {
                           startItems.addItem(Integer.parseInt(item_id));
                        }
                    }
                    if (classChildren.getName().equalsIgnoreCase("enchant")) {
                        final String enclvl = classChildren.getAttributeValue("lvl");
                        startItems.setEnchantLvL(Integer.parseInt(enclvl));
                    }
                }
                holder.AddStartItems(startItems);
            }
        }

    }

    private static class LazyHolder {
        protected static final AltStartParser INSTANCE = new AltStartParser();
    }
}
