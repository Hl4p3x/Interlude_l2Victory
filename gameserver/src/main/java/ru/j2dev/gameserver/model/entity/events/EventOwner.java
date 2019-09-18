package ru.j2dev.gameserver.model.entity.events;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public abstract class EventOwner implements Serializable {
    private final Set<GlobalEvent> _events;

    public EventOwner() {
        _events = new HashSet<>(2);
    }

    @SuppressWarnings("unchecked")
    public <E extends GlobalEvent> E getEvent(final Class<E> eventClass) {
        for (final GlobalEvent e : _events) {
            if (e.getClass() == eventClass) {
                return (E) e;
            }
            if (eventClass.isAssignableFrom(e.getClass())) {
                return (E) e;
            }
        }
        return null;
    }

    public void addEvent(final GlobalEvent event) {
        _events.add(event);
    }

    public void removeEvent(final GlobalEvent event) {
        _events.remove(event);
    }

    public void removeEventsByClass(final Class<? extends GlobalEvent> eventClass) {
        for (final GlobalEvent e : _events) {
            if (e.getClass() == eventClass) {
                _events.remove(e);
            } else {
                if (!eventClass.isAssignableFrom(e.getClass())) {
                    continue;
                }
                _events.remove(e);
            }
        }
    }

    public Set<GlobalEvent> getEvents() {
        return _events;
    }
}
