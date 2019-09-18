package ru.j2dev.gameserver.data.xml.holder;

import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import ru.j2dev.commons.data.xml.AbstractHolder;


/**
 * @author JunkyFunky
 **/
public final class LevelBonusHolder extends AbstractHolder {

    private final TIntDoubleMap _bonusList = new TIntDoubleHashMap();

    public static LevelBonusHolder getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void addLevelBonus(int lvl, double bonus) {
        _bonusList.put(lvl, bonus);
    }

    public double getLevelBonus(int lvl) {
        return _bonusList.get(lvl);
    }

    @Override
    public int size() {
        return _bonusList.size();
    }

    @Override
    public void clear() {
        _bonusList.clear();
    }

    private static class LazyHolder {
        protected static final LevelBonusHolder INSTANCE = new LevelBonusHolder();
    }
}