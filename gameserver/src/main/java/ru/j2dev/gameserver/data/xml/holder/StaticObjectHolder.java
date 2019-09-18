package ru.j2dev.gameserver.data.xml.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.gameserver.model.instances.StaticObjectInstance;
import ru.j2dev.gameserver.templates.StaticObjectTemplate;

import java.util.HashMap;
import java.util.Map;

public final class StaticObjectHolder extends AbstractHolder {

    private final Map<Integer, StaticObjectTemplate> _templates = new HashMap<>();
    private final Map<Integer, StaticObjectInstance> _spawned = new HashMap<>();

    public static StaticObjectHolder getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void addTemplate(final StaticObjectTemplate template) {
        _templates.put(template.getUId(), template);
    }

    public StaticObjectTemplate getTemplate(final int id) {
        return _templates.get(id);
    }

    public void spawnAll() {
        for (final StaticObjectTemplate template : _templates.values()) {
            if (template.isSpawn()) {
                final StaticObjectInstance obj = template.newInstance();
                _spawned.put(template.getUId(), obj);
            }
        }
        info("spawned: " + _spawned.size() + " static object(s).");
    }

    public StaticObjectInstance getObject(final int id) {
        return _spawned.get(id);
    }

    @Override
    public int size() {
        return _templates.size();
    }

    @Override
    public void clear() {
        _templates.clear();
    }

    private static class LazyHolder {
        private static final StaticObjectHolder INSTANCE = new StaticObjectHolder();
    }
}
