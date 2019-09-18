package ru.j2dev.gameserver.data.xml.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.gameserver.templates.spawn.SpawnTemplate;

import java.util.*;

public final class SpawnHolder extends AbstractHolder {

    private final Map<String, List<SpawnTemplate>> _spawns = new HashMap<>();

    public static SpawnHolder getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void addSpawn(final String group, final SpawnTemplate spawn) {
        List<SpawnTemplate> spawns = _spawns.computeIfAbsent(group, k -> new ArrayList<>());
        spawns.add(spawn);
    }

    public List<SpawnTemplate> getSpawn(final String name) {
        final List<SpawnTemplate> template = _spawns.get(name);
        return (template == null) ? Collections.emptyList() : template;
    }

    @Override
    public int size() {
        int i = 0;
        for (final List l : _spawns.values()) {
            i += l.size();
        }
        return i;
    }

    @Override
    public void clear() {
        _spawns.clear();
    }

    public Map<String, List<SpawnTemplate>> getSpawns() {
        return _spawns;
    }

    private static class LazyHolder {
        private static final SpawnHolder INSTANCE = new SpawnHolder();
    }
}
