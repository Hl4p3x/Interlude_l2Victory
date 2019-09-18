package ru.j2dev.gameserver.phantoms.data.holder;

import ru.j2dev.gameserver.phantoms.template.PhantomArmorTemplate;
import ru.j2dev.commons.data.xml.AbstractHolder;

import java.util.HashMap;
import java.util.Map;

public class PhantomArmorHolder extends AbstractHolder {
    private static PhantomArmorHolder instance = new PhantomArmorHolder();
    private final Map<Integer, PhantomArmorTemplate> sets;

    public PhantomArmorHolder() {
        sets = new HashMap<>();
    }

    public static PhantomArmorHolder getInstance() {
        return PhantomArmorHolder.instance;
    }

    public void addSet(final int id, final PhantomArmorTemplate set) {
        sets.put(id, set);
    }

    public PhantomArmorTemplate getSet(final int id) {
        return sets.get(id);
    }

    @Override
    public int size() {
        return sets.size();
    }

    @Override
    public void clear() {
        sets.clear();
    }

}
