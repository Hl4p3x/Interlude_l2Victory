package ru.j2dev.gameserver.manager;

import ru.j2dev.gameserver.data.xml.holder.DoorHolder;
import ru.j2dev.gameserver.data.xml.holder.InstantZoneHolder;
import ru.j2dev.gameserver.data.xml.holder.ZoneHolder;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.utils.Location;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReflectionManager {
    public static final Reflection DEFAULT = Reflection.createReflection(0);
    public static final Reflection GIRAN_HARBOR = Reflection.createReflection(-1);
    public static final Reflection JAIL = Reflection.createReflection(-2);

    private final Map<Integer, Reflection> _reflections = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    public void init() {
        add(DEFAULT);
        add(GIRAN_HARBOR);
        add(JAIL);
        DEFAULT.init(DoorHolder.getInstance().getDoors(), ZoneHolder.getInstance().getZones());
        GIRAN_HARBOR.fillSpawns(InstantZoneHolder.getInstance().getInstantZone(10).getSpawnsInfo());
        JAIL.setCoreLoc(new Location(-114648, -249384, -2984));
    }

    public static ReflectionManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    public Reflection get(final int id) {
        readLock.lock();
        try {
            return _reflections.get(id);
        } finally {
            readLock.unlock();
        }
    }

    public Reflection add(final Reflection ref) {
        writeLock.lock();
        try {
            return _reflections.put(ref.getId(), ref);
        } finally {
            writeLock.unlock();
        }
    }

    public Reflection remove(final Reflection ref) {
        writeLock.lock();
        try {
            return _reflections.remove(ref.getId());
        } finally {
            writeLock.unlock();
        }
    }

    public Reflection[] getAll() {
        readLock.lock();
        try {
            return _reflections.values().toArray(new Reflection[0]);
        } finally {
            readLock.unlock();
        }
    }

    public int getCountByIzId(final int izId) {
        readLock.lock();
        try {
            return (int) Arrays.stream(getAll()).filter(r -> r.getInstancedZoneId() == izId).count();
        } finally {
            readLock.unlock();
        }
    }

    public int size() {
        return _reflections.size();
    }

    private static class LazyHolder {
        private static final ReflectionManager INSTANCE = new ReflectionManager();
    }
}
