package ru.j2dev.gameserver.data.xml.parser;


import org.jdom2.Element;
import ru.j2dev.commons.data.xml.AbstractFileParser;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.BaseStatsBonusHolder;
import ru.j2dev.gameserver.templates.BaseStatsBonus;

import java.io.File;

/**
 * @author JunkyFunky
 **/
public final class BaseStatsBonusParser extends AbstractFileParser<BaseStatsBonusHolder> {

    private BaseStatsBonusParser() {
        super(BaseStatsBonusHolder.getInstance());
    }

    public static BaseStatsBonusParser getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public File getXMLFile() {
        return new File(Config.DATAPACK_ROOT, "data/xml/others/base_stats_bonus_data.xml");
    }


    @Override
    protected void readData(BaseStatsBonusHolder holder, Element rootElement) throws Exception {
        rootElement.getChildren().stream().filter(element -> "base_stats_bonus".equalsIgnoreCase(element.getName())).forEach(element -> element.getChildren().forEach(e -> {
            int value = Integer.parseInt(e.getAttributeValue("value"));
            double str = (100. + Integer.parseInt(e.getAttributeValue("str"))) / 100;
            double _int = (100. + Integer.parseInt(e.getAttributeValue("int"))) / 100;
            double dex = (100. + Integer.parseInt(e.getAttributeValue("dex"))) / 100;
            double wit = (100. + Integer.parseInt(e.getAttributeValue("wit"))) / 100;
            double con = (100. + Integer.parseInt(e.getAttributeValue("con"))) / 100;
            double men = (100. + Integer.parseInt(e.getAttributeValue("men"))) / 100;
            holder.addBaseStatsBonus(value, new BaseStatsBonus(_int, str, con, men, dex, wit));
        }));
    }

    private static class LazyHolder {
        protected static final BaseStatsBonusParser INSTANCE = new BaseStatsBonusParser();
    }
}