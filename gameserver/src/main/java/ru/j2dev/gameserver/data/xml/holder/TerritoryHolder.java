package ru.j2dev.gameserver.data.xml.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.gameserver.model.Territory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author JunkyFunky
 */
public final class TerritoryHolder extends AbstractHolder {


    private final Map<String, Territory> _territories = new HashMap<>();

    private TerritoryHolder() {
    }

    public static TerritoryHolder getInstance() {
        return SingletonHolder._instance;
    }

    public Map<String, Territory> getTerritories() {
        return _territories;
    }

    public Territory getTerritoryByName(final String name) {
        return _territories.get(name);
    }

    public void addTerritory(final String name, final Territory territory) {
        _territories.putIfAbsent(name, territory);
    }

    @Override
    public int size() {
        return _territories.values().size();
    }

    @Override
    public void clear() {
        _territories.clear();
    }


    private static class SingletonHolder {
        protected static final TerritoryHolder _instance = new TerritoryHolder();
    }
}
