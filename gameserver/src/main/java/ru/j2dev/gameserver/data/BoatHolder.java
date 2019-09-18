package ru.j2dev.gameserver.data;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.gameserver.idfactory.IdFactory;
import ru.j2dev.gameserver.model.entity.boat.Boat;
import ru.j2dev.gameserver.templates.CharTemplate;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public final class BoatHolder extends AbstractHolder {
    public static final CharTemplate TEMPLATE = new CharTemplate(CharTemplate.getEmptyStatsSet());

    private final Map<Integer, Boat> _boats = new HashMap<>();

    public static BoatHolder getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void spawnAll() {
        log();
        _boats.values().forEach(boat -> {
                    boat.spawnMe();
                    info("Spawning: " + boat.getName());
                }
        );
    }

    public Boat initBoat(final String name, final String clazz) {
        try {
            final Class<?> cl = Class.forName("ru.j2dev.gameserver.model.entity.boat." + clazz);
            final Constructor<?> constructor = cl.getConstructor(Integer.TYPE, CharTemplate.class);
            final Boat boat = (Boat) constructor.newInstance(IdFactory.getInstance().getNextId(), TEMPLATE);
            boat.setName(name);
            addBoat(boat);
            return boat;
        } catch (Exception e) {
            error("Fail to init boat: " + clazz, e);
            return null;
        }
    }

    public Boat getBoat(final String name) {
        return _boats.values().stream().filter(boat -> name.equals(boat.getName())).findFirst().orElse(null);
    }

    public Boat getBoat(final int objectId) {
        return _boats.get(objectId);
    }

    public void addBoat(final Boat boat) {
        _boats.put(boat.getObjectId(), boat);
    }

    public void removeBoat(final Boat boat) {
        _boats.remove(boat.getObjectId());
    }

    @Override
    public int size() {
        return _boats.size();
    }

    @Override
    public void clear() {
        _boats.clear();
    }

    private static class LazyHolder {
        private static final BoatHolder INSTANCE = new BoatHolder();
    }
}
