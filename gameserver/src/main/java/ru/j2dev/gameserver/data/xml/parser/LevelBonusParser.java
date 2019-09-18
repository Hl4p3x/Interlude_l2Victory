package ru.j2dev.gameserver.data.xml.parser;

import org.jdom2.Element;
import ru.j2dev.commons.data.xml.AbstractFileParser;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.LevelBonusHolder;

import java.io.File;


/**
 * @author JunkyFunky
 **/
public final class LevelBonusParser extends AbstractFileParser<LevelBonusHolder> {

    private LevelBonusParser() {
        super(LevelBonusHolder.getInstance());
    }

    public static LevelBonusParser getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public File getXMLFile() {
        return new File(Config.DATAPACK_ROOT, "data/xml/others/lvl_bonus_data.xml");
    }

    @Override
    protected void readData(final LevelBonusHolder holder, final Element rootElement) throws Exception {
        for (Element element : rootElement.getChildren()) {
            if ("lvl_bonus".equalsIgnoreCase(element.getName())) {
                element.getChildren().forEach(e -> {
                    int lvl = Integer.parseInt(e.getAttributeValue("lvl"));
                    double bonusMod = Double.parseDouble(e.getAttributeValue("value"));
                    holder.addLevelBonus(lvl, bonusMod);
                });
            }
        }
    }

    private static class LazyHolder {
        protected static final LevelBonusParser INSTANCE = new LevelBonusParser();
    }
}