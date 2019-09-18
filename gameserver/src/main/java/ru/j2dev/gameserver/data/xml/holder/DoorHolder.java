package ru.j2dev.gameserver.data.xml.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.gameserver.templates.DoorTemplate;

import java.util.HashMap;
import java.util.Map;

public final class DoorHolder extends AbstractHolder {

    private final Map<Integer, DoorTemplate> _doors = new HashMap<>();

    public static DoorHolder getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void addTemplate(final DoorTemplate door) {
        _doors.put(door.getNpcId(), door);
    }

    public DoorTemplate getTemplate(final int doorId) {
        return _doors.get(doorId);
    }

    public Map<Integer, DoorTemplate> getDoors() {
        return _doors;
    }

    @Override
    public int size() {
        return _doors.size();
    }

    @Override
    public void clear() {
        _doors.clear();
    }

    private static class LazyHolder {
        private static final DoorHolder INSTANCE = new DoorHolder();
    }
}
