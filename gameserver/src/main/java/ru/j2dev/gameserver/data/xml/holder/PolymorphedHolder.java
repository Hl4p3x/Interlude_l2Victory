package ru.j2dev.gameserver.data.xml.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.gameserver.templates.npc.polymorphed.PolymorphedData;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by JunkyFunky
 * on 03.01.2018 17:23
 * group j2dev
 */
public class PolymorphedHolder extends AbstractHolder {

    private final Map<Integer, PolymorphedData> polymorphedData = new HashMap<>();

    public static PolymorphedHolder getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void addPolymorphedData(PolymorphedData polymorphedData) {
        this.polymorphedData.put(polymorphedData.getNpcId(), polymorphedData);
    }

    public boolean contains(final int id) {
        return polymorphedData.containsKey(id);
    }

    public PolymorphedData getPolymorphedData(final int polyId) {
        return polymorphedData.get(polyId);
    }

    @Override
    public void clear() {
        polymorphedData.clear();
    }

    @Override
    public int size() {
        return polymorphedData.size();
    }

    private static class LazyHolder {
        protected static final PolymorphedHolder INSTANCE = new PolymorphedHolder();
    }
}
