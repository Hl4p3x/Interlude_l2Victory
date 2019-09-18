package ru.j2dev.gameserver.data.xml.parser;

import org.jdom2.Element;
import ru.j2dev.commons.data.xml.AbstractFileParser;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.SuperPointHolder;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;
import ru.j2dev.gameserver.templates.npc.superPoint.SuperPoinCoordinate;
import ru.j2dev.gameserver.templates.npc.superPoint.SuperPoint;
import ru.j2dev.gameserver.templates.npc.superPoint.SuperPointType;

import java.io.File;

public final class SuperPointParser extends AbstractFileParser<SuperPointHolder> {

    public SuperPointParser() {
        super(SuperPointHolder.getInstance());
    }

    public static SuperPointParser getInstance() {
        return SingletonHolder._instance;
    }

    private static class SingletonHolder {
        protected static final SuperPointParser _instance = new SuperPointParser();
    }

    @Override
    public File getXMLFile() {
        return new File(Config.DATAPACK_ROOT, "data/xml/superpoint/superpointinfo.xml");
    }

    @Override
    protected void readData(final SuperPointHolder holder, final Element rootElement) {
        rootElement.getChildren("superpoint").forEach(superPointElement -> {
            final String pointName = superPointElement.getAttributeValue("name");
            final Boolean isRunning = Boolean.parseBoolean(superPointElement.getAttributeValue("running"));
            final SuperPoint point = new SuperPoint();
            point.setName(pointName);
            point.setRunning(isRunning);
            point.setType(SuperPointType.valueOf(superPointElement.getAttributeValue("moveType", "NONE")));
            superPointElement.getChildren("point").forEach(pointElement -> {
                final int x = Integer.parseInt(pointElement.getAttributeValue("x"));
                final int y = Integer.parseInt(pointElement.getAttributeValue("y"));
                final int z = Integer.parseInt(pointElement.getAttributeValue("z"));
                final SuperPoinCoordinate coords = new SuperPoinCoordinate(x, y, z);
                final Element mgsElement = pointElement.getChild("msg");
                if (mgsElement != null) {
                    coords.setMsgId(Integer.parseInt(mgsElement.getAttributeValue("id", "0")));
                    coords.setMsgChatType(ChatType.valueOf(mgsElement.getAttributeValue("chat", "ALL")));
                    coords.setMsgRadius(Integer.parseInt(mgsElement.getAttributeValue("radius", "1500")));
                }
                final Element delayElement = pointElement.getChild("delay");
                if (delayElement != null) {
                    coords.setDelayInSec(Integer.parseInt(delayElement.getAttributeValue("sec", "0")));
                }
                final Element socialElement = pointElement.getChild("social");
                if (socialElement != null) {
                    coords.setSocialId(Integer.parseInt(socialElement.getAttributeValue("id", "0")));
                }
                point.addMoveCoordinats(coords);
            });
            holder.addSuperPoints(pointName, point);
        });
    }
}