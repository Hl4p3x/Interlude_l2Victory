package ru.j2dev.gameserver.data.xml.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.gameserver.model.event.PvpEventType;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by JunkyFunky
 * on 07.07.2018 18:14
 * group j2dev
 */
public class EventEquipHolder extends AbstractHolder {
    private static Map<PvpEventType, Map<Integer, ArrayList<Integer>>> equip = new HashMap<>();

    static {
        EnumSet.of(PvpEventType.LastHero, PvpEventType.values()).forEach(type -> equip.put(type, new HashMap<>()));
    }

    public static EventEquipHolder getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void addEquipForClass(final PvpEventType type, final int classId, final ArrayList<Integer> list) {
        equip.get(type).put(classId, list);
    }

    public ArrayList<Integer> getEquipForEventTypeAndClassId(final PvpEventType type, final int classId) {
        return equip.get(type).get(classId);
    }

    @Override
    public void clear() {
        equip.clear();
    }

    @Override
    public void log() {
        equip.forEach((key, value) -> info("Event's equip for " + key.toString() + " : " + value.size() + " classes"));
    }

    private static class LazyHolder {
        private static final EventEquipHolder INSTANCE = new EventEquipHolder();
    }
}
