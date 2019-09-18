package ru.j2dev.gameserver.data.xml.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.events.EventType;
import ru.j2dev.gameserver.model.entity.events.GlobalEvent;

import java.util.Map;
import java.util.TreeMap;

public final class EventHolder extends AbstractHolder {

    private final Map<Integer, GlobalEvent> _events = new TreeMap<>();

    public static EventHolder getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void addEvent(final EventType type, final GlobalEvent event) {
        _events.put(type.step() + event.getId(), event);
    }

    @SuppressWarnings("unchecked")
    public <E extends GlobalEvent> E getEvent(final EventType type, final int id) {
        return (E) _events.get(type.step() + id);
    }

    public void findEvent(final Player player) {
        _events.values().stream().filter(event -> event.isParticle(player)).forEach(player::addEvent);
    }

    public void callInit() {
        _events.values().forEach(GlobalEvent::initEvent);
    }

    @Override
    public int size() {
        return _events.size();
    }

    @Override
    public void clear() {
        _events.clear();
    }

    private static class LazyHolder {
        private static final EventHolder INSTANCE = new EventHolder();
    }
}
