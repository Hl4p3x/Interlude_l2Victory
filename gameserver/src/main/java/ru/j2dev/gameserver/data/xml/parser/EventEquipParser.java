package ru.j2dev.gameserver.data.xml.parser;

import org.jdom2.Element;
import ru.j2dev.commons.data.xml.AbstractFileParser;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.EventEquipHolder;
import ru.j2dev.gameserver.model.event.PvpEventType;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by JunkyFunky
 * on 07.07.2018 18:14
 * group j2dev
 */
public class EventEquipParser extends AbstractFileParser<EventEquipHolder> {

    private EventEquipParser() {
        super(EventEquipHolder.getInstance());
    }

    public static EventEquipParser getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public File getXMLFile() {
        return new File(Config.DATAPACK_ROOT, "data/xml/others/event_equip.xml");
    }

    @Override
    protected void readData(EventEquipHolder holder, Element rootElement) {
        rootElement.getChildren().forEach(element -> {
            final String[] eventType = element.getAttributeValue("eventType").split(",");
            final int classId = Integer.parseInt(element.getAttributeValue("id"));
            String[] items = element.getAttributeValue("items").split(",");
            ArrayList<Integer> list = new ArrayList<>();
            Arrays.stream(items).forEach(item -> list.add(Integer.parseInt(item)));
            Arrays.stream(eventType).forEach(type -> holder.addEquipForClass(PvpEventType.valueOf(type), classId, list));
        });
    }

    private static class LazyHolder {
        protected static final EventEquipParser INSTANCE = new EventEquipParser();
    }
}
