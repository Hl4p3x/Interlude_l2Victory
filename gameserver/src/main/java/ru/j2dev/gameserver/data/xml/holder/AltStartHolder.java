package ru.j2dev.gameserver.data.xml.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.gameserver.templates.item.StartItems;

import java.util.HashMap;
import java.util.Map;

/**
 * Solution
 * 15.08.2018
 * 17:20
 */

public class AltStartHolder extends AbstractHolder {

    private final Map<Integer, StartItems> startItems = new HashMap<>();

    public static AltStartHolder getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void AddStartItems(StartItems startItems) {
        this.startItems.put(startItems.getClassId(), startItems);
    }

    public StartItems getStartItems(final int classId) {
        return startItems.get(classId);
    }

    @Override
    public void clear() {
        startItems.clear();
    }

    @Override
    public int size() {
        return startItems.size();
    }

    private static class LazyHolder {
        protected static final AltStartHolder INSTANCE = new AltStartHolder();
    }
}
