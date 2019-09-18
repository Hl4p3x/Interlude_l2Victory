package ru.j2dev.dataparser.parser;

import org.jdom2.Element;
import ru.j2dev.commons.data.xml.AbstractFileParser;
import ru.j2dev.dataparser.holder.EventDataHolder;
import ru.j2dev.dataparser.holder.eventdata.EventItemInfoDrop;
import ru.j2dev.dataparser.holder.eventdata.EventTemplate;
import ru.j2dev.dataparser.pch.LinkerFactory;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author KilRoy
 */
public class EventDataParser extends AbstractFileParser<EventDataHolder> {
    public static final ZonedDateTime DEFAULT_START_DATE = ZonedDateTime.of(LocalDateTime.of(1980, 1, 1, 0, 0), ZoneId.systemDefault());
    public static final ZonedDateTime DEFAULT_END_DATE = ZonedDateTime.of(LocalDateTime.of(2010, 6, 1, 23, 59), ZoneId.systemDefault());
    public static final DateTimeFormatter DATE_TIME_FORMATTER_4 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String FILE_NAME = "data/pts_scripts/eventdata.xml";
    private static final EventDataParser INSTANCE = new EventDataParser();

    private EventDataParser() {
        super(EventDataHolder.getInstance());
    }

    public static EventDataParser getInstance() {
        return INSTANCE;
    }

    private static ZonedDateTime getMillisecondsFromString(final String datetime) {
        try {
            return DATE_TIME_FORMATTER_4.withZone(ZoneId.systemDefault()).parse(datetime, ZonedDateTime::from);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public File getXMLFile() {
        return new File(FILE_NAME);
    }

    @Override
    protected void readData(final EventDataHolder holder, Element rootElement) {
        for (final Element eventDataElement : rootElement.getChildren("L2EventData")) {
            for (final Element dropEventElement : eventDataElement.getChildren("DropEvent")) {
                final String eventName = dropEventElement.getAttributeValue("Name");
                final boolean register = Boolean.parseBoolean(dropEventElement.getAttributeValue("Register"));
                final int panelId = dropEventElement.getAttributeValue("PanelsID") != null ? Integer.parseInt(dropEventElement.getAttributeValue("PanelsID")) : 0;
                final EventTemplate template = new EventTemplate(eventName);
                template.setRegister(register);
                template.setPanelId(panelId);

                for (final Element npcElement : dropEventElement.getChildren("NPC")) {
                    final int tick = Integer.parseInt(npcElement.getAttributeValue("Tick"));
                    final ZonedDateTime startEventDate = npcElement.getAttributeValue("Start") != null ? getMillisecondsFromString(npcElement.getAttributeValue("Start")) : DEFAULT_START_DATE;
                    final ZonedDateTime endEventDate = npcElement.getAttributeValue("End") != null ? getMillisecondsFromString(npcElement.getAttributeValue("End")) : DEFAULT_END_DATE;
                    template.setNpcTick(tick);
                    template.setStartEventTime(startEventDate);
                    template.setEndEventTime(endEventDate);

                    for (final Element spawnElement : npcElement.getChildren("SpawnTimes")) {
                        final String makerName = spawnElement.getAttributeValue("MakerEventName");
                        template.setEventMakerName(makerName);

                        for (final Element spawnTimeElement : spawnElement.getChildren("SpawnTime")) {
                            final ZonedDateTime startDropDate = spawnTimeElement.getAttributeValue("Start") != null ? getMillisecondsFromString(spawnTimeElement.getAttributeValue("Start")) : DEFAULT_START_DATE;
                            final ZonedDateTime endDropDate = spawnTimeElement.getAttributeValue("End") != null ? getMillisecondsFromString(spawnTimeElement.getAttributeValue("End")) : DEFAULT_END_DATE;
                            template.setStartDropTime(startDropDate);
                            template.setEndDropTime(endDropDate);
                        }
                    }
                }
                for (final Element dropInfoElement : dropEventElement.getChildren("ITEM")) {
                    final int chance = dropInfoElement.getAttributeValue("DropItemChance").equalsIgnoreCase("RND") ? -1 : Integer.parseInt(dropInfoElement.getAttributeValue("DropItemChance"));
                    template.setDropItemChance(chance);

                    for (final Element dropItemElement : dropInfoElement.getChildren("DropItem")) {
                        final String itemName = dropItemElement.getAttributeValue("Name");
                        final int itemId = LinkerFactory.getInstance().findClearValue(itemName);
                        final long itemCount = Long.parseLong(dropItemElement.getAttributeValue("Count"));
                        final int itemChance = Integer.parseInt(dropItemElement.getAttributeValue("Chance"));
                        final EventItemInfoDrop item = new EventItemInfoDrop(itemId, itemCount, itemChance);
                        template.addDropItem(item);
                    }
                }

                holder.addEventTemplate(template);
            }
        }
    }
}