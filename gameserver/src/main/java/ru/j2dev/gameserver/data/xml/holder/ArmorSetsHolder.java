package ru.j2dev.gameserver.data.xml.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.gameserver.model.ArmorSet;

import java.util.HashSet;
import java.util.Set;

public final class ArmorSetsHolder extends AbstractHolder {

    private final Set<ArmorSet> _armorSets = new HashSet<>();

    public static ArmorSetsHolder getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void addArmorSet(final ArmorSet armorset) {
        _armorSets.add(armorset);
    }

    public ArmorSet getArmorSet(final int chestItemId) {
        return _armorSets.stream().filter(as -> as.getChestItemIds().contains(chestItemId)).findFirst().orElse(null);
    }

    @Override
    public int size() {
        return _armorSets.size();
    }

    @Override
    public void clear() {
        _armorSets.clear();
    }

    private static class LazyHolder {
        private static final ArmorSetsHolder INSTANCE = new ArmorSetsHolder();
    }
}
