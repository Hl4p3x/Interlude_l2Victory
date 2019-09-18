package ru.j2dev.gameserver.data.xml.holder;

import gnu.trove.map.hash.TIntObjectHashMap;
import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.gameserver.templates.SoulCrystal;

public final class SoulCrystalHolder extends AbstractHolder {

    private final TIntObjectHashMap<SoulCrystal> _crystals = new TIntObjectHashMap<>();

    public static SoulCrystalHolder getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void addCrystal(final SoulCrystal crystal) {
        _crystals.put(crystal.getItemId(), crystal);
    }

    public SoulCrystal getCrystal(final int item) {
        return _crystals.get(item);
    }

    public SoulCrystal[] getCrystals() {
        return _crystals.values(new SoulCrystal[_crystals.size()]);
    }

    @Override
    public int size() {
        return _crystals.size();
    }

    @Override
    public void clear() {
        _crystals.clear();
    }

    private static class LazyHolder {
        private static final SoulCrystalHolder INSTANCE = new SoulCrystalHolder();
    }
}
