package ru.j2dev.gameserver.data.xml.parser;

import org.jdom2.Element;
import ru.j2dev.commons.data.xml.AbstractFileParser;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.ShadowTradeHolder;
import ru.j2dev.gameserver.templates.shadowtrade.ShadowTradeGroup;
import ru.j2dev.gameserver.templates.shadowtrade.ShadowTradeItem;
import ru.j2dev.gameserver.templates.shadowtrade.ShadowTradeLoc;

import java.io.File;

/**
 * Created by JunkyFunky
 * on 18.01.2018 22:00
 * group j2dev
 */
public class ShadowTradeParser extends AbstractFileParser<ShadowTradeHolder> {

    private ShadowTradeParser() {
        super(ShadowTradeHolder.getInstance());
    }

    public static ShadowTradeParser getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public File getXMLFile() {
        return new File(Config.DATAPACK_ROOT, "data/xml/others/shadow_trade.xml");
    }

    @Override
    protected void readData(final ShadowTradeHolder holder, Element rootElement) {
        rootElement.getChildren("group").forEach(groupElement -> {
            ShadowTradeGroup tradeGroup = new ShadowTradeGroup();
            groupElement.getAttributes().forEach(attribute -> {
                if ("name".equalsIgnoreCase(attribute.getName())) {
                    tradeGroup.setName(attribute.getValue());
                }
                if ("chance".equalsIgnoreCase(attribute.getName())) {
                    tradeGroup.setChance(Integer.parseInt(attribute.getValue()));
                }
                groupElement.getChildren("production").forEach(element -> {
                    ShadowTradeItem shadowTradeItem = new ShadowTradeItem();
                    element.getAttributes().forEach(productAttribute -> {
                        if ("id".equalsIgnoreCase(productAttribute.getName())) {
                            shadowTradeItem.setItemId(Integer.parseInt(productAttribute.getValue()));
                        }
                        if ("count".equalsIgnoreCase(productAttribute.getName())) {
                            shadowTradeItem.setCount(Integer.parseInt(productAttribute.getValue()));
                        }
                        if ("priceItemId".equalsIgnoreCase(productAttribute.getName())) {
                            shadowTradeItem.setPriceItemId(Integer.parseInt(productAttribute.getValue()));
                        }
                        if ("priceCount".equalsIgnoreCase(productAttribute.getName())) {
                            shadowTradeItem.setPriceCount(Integer.parseInt(productAttribute.getValue()));
                        }
                    });
                    tradeGroup.addShadowTradeItem(shadowTradeItem);
                });
            });
            holder.addShadowItem(tradeGroup);
        });
        rootElement.getChild("night_locations").getChildren().forEach(element -> {
            ShadowTradeLoc location = new ShadowTradeLoc();
            element.getAttributes().forEach(attribute -> {
                if ("x".equalsIgnoreCase(attribute.getName())) {
                    location.setX(Integer.parseInt(attribute.getValue()));
                }
                if ("y".equalsIgnoreCase(attribute.getName())) {
                    location.setY(Integer.parseInt(attribute.getValue()));
                }
                if ("z".equalsIgnoreCase(attribute.getName())) {
                    location.setZ(Integer.parseInt(attribute.getValue()));
                }
                if ("h".equalsIgnoreCase(attribute.getName())) {
                    location.setH(Integer.parseInt(attribute.getValue()));
                }
                if ("desc".equalsIgnoreCase(attribute.getName())) {
                    location.setDecription(attribute.getValue());
                }
            });
            holder.addNightLoc(location);
        });
        rootElement.getChild("day_locations").getChildren().forEach(element -> {
            ShadowTradeLoc location = new ShadowTradeLoc();
            element.getAttributes().forEach(attribute -> {
                if ("x".equalsIgnoreCase(attribute.getName())) {
                    location.setX(Integer.parseInt(attribute.getValue()));
                }
                if ("y".equalsIgnoreCase(attribute.getName())) {
                    location.setY(Integer.parseInt(attribute.getValue()));
                }
                if ("z".equalsIgnoreCase(attribute.getName())) {
                    location.setZ(Integer.parseInt(attribute.getValue()));
                }
                if ("h".equalsIgnoreCase(attribute.getName())) {
                    location.setH(Integer.parseInt(attribute.getValue()));
                }
                if ("desc".equalsIgnoreCase(attribute.getName())) {
                    location.setDecription(attribute.getValue());
                }
            });
            holder.addDayLoc(location);
        });

    }

    private static class LazyHolder {
        protected static final ShadowTradeParser INSTANCE = new ShadowTradeParser();
    }
}
