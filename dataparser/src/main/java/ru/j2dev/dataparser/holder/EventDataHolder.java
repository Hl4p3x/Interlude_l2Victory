package ru.j2dev.dataparser.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.dataparser.holder.eventdata.EventTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author KilRoy
 */
public class EventDataHolder extends AbstractHolder {
    private static final EventDataHolder INSTANCE = new EventDataHolder();
    private final List<EventTemplate> events = new ArrayList<>();

    private EventDataHolder() {
    }

    public static EventDataHolder getInstance() {
        return INSTANCE;
    }

    public void addEventTemplate(final EventTemplate template) {
        events.add(template);
    }

    public List<EventTemplate> getEvents() {
        return events;
    }

    public EventTemplate getEventByName(final String eventName) {
        final Optional<EventTemplate> eventOptional = events.stream().filter(event -> event.getEventName().equalsIgnoreCase(eventName)).findFirst();
        return eventOptional.orElse(null);
    }

    @Override
    public void clear() {
        events.clear();
    }

    @Override
    public int size() {
        return events.size();
    }
}