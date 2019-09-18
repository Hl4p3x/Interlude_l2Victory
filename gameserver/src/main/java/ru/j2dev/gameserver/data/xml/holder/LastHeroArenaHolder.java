package ru.j2dev.gameserver.data.xml.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.gameserver.templates.arenas.LastHeroArena;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LastHeroArenaHolder extends AbstractHolder {

    private final Map<Integer, LastHeroArena> _arenas = new HashMap<>();

    public static LastHeroArenaHolder getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void addArena(final LastHeroArena arena) {
        _arenas.put(arena.getId(), arena);
    }

    public List<LastHeroArena> getArenas() {
        return new ArrayList<>(_arenas.values());
    }

    @Override
    public int size() {
        return _arenas.size();
    }

    @Override
    public void clear() {
        _arenas.clear();
    }

    private static class LazyHolder {
        protected static final LastHeroArenaHolder INSTANCE = new LastHeroArenaHolder();
    }
}
