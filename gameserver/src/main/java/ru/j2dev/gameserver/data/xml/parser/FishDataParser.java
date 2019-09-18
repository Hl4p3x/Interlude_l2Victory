package ru.j2dev.gameserver.data.xml.parser;

import org.jdom2.Attribute;
import org.jdom2.Element;
import ru.j2dev.commons.collections.MultiValueSet;
import ru.j2dev.commons.data.xml.AbstractFileParser;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.FishDataHolder;
import ru.j2dev.gameserver.templates.item.support.FishGroup;
import ru.j2dev.gameserver.templates.item.support.FishTemplate;
import ru.j2dev.gameserver.templates.item.support.LureTemplate;
import ru.j2dev.gameserver.templates.item.support.LureType;

import java.io.File;
import java.util.Map;
import java.util.stream.Collectors;

public class FishDataParser extends AbstractFileParser<FishDataHolder> {

    private FishDataParser() {
        super(FishDataHolder.getInstance());
    }

    public static FishDataParser getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public File getXMLFile() {
        return new File(Config.DATAPACK_ROOT, "data/xml/others/fishdata.xml");
    }

    @Override
    protected void readData(final FishDataHolder holder, final Element rootElement) {
        for (Element e : rootElement.getChildren()) {
            switch (e.getName()) {
                case "fish": {
                    final MultiValueSet<String> map = e.getAttributes().stream().collect(Collectors.toMap(Attribute::getName, Attribute::getValue, (a, b) -> b, MultiValueSet::new));
                    holder.addFish(new FishTemplate(map));
                    break;
                }
                case "lure": {
                    final MultiValueSet<String> map = e.getAttributes().stream().collect(Collectors.toMap(Attribute::getName, Attribute::getValue, (a, b) -> b, MultiValueSet::new));
                    final Map<FishGroup, Integer> chances = e.getChildren().stream().collect(Collectors.toMap(chanceElement -> FishGroup.valueOf(chanceElement.getAttributeValue("type")), chanceElement -> Integer.parseInt(chanceElement.getAttributeValue("value")), (a, b) -> b));
                    map.put("chances", chances);
                    holder.addLure(new LureTemplate(map));
                    break;
                }
                default:
                    if (!"distribution".equals(e.getName())) {
                        continue;
                    }
                    final int id = Integer.parseInt(e.getAttributeValue("id"));
                    e.getChildren().forEach(forLureElement -> {
                        final LureType lureType = LureType.valueOf(forLureElement.getAttributeValue("type"));
                        final Map<FishGroup, Integer> chances2 = forLureElement.getChildren().stream().collect(Collectors.toMap(chanceElement2 -> FishGroup.valueOf(chanceElement2.getAttributeValue("type")), chanceElement2 -> Integer.parseInt(chanceElement2.getAttributeValue("value")), (a, b) -> b));
                        holder.addDistribution(id, lureType, chances2);
                    });
                    break;
            }
        }
    }

    private static class LazyHolder {
        protected static final FishDataParser INSTANCE = new FishDataParser();
    }
}
