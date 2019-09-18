package ru.j2dev.gameserver.data.xml.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.gameserver.templates.arenas.KoreanStyleArena;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class KoreanStyleArenaHolder extends AbstractHolder {

    private final Map<Integer, KoreanStyleArena> _arenas = new HashMap<>();

    public static KoreanStyleArenaHolder getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void addArena(final KoreanStyleArena arena) {
        _arenas.put(arena.getId(), arena);
    }

    public List<KoreanStyleArena> getArenas() {
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
        protected static final KoreanStyleArenaHolder INSTANCE = new KoreanStyleArenaHolder();
    }
}
