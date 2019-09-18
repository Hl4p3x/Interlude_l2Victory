package ru.j2dev.gameserver.data.xml.holder;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.templates.Henna;

import java.util.ArrayList;
import java.util.List;

public final class HennaHolder extends AbstractHolder {

    private final TIntObjectHashMap<Henna> _hennas = new TIntObjectHashMap<>();

    public static HennaHolder getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void addHenna(final Henna h) {
        _hennas.put(h.getSymbolId(), h);
    }

    public Henna getHenna(final int symbolId) {
        return _hennas.get(symbolId);
    }

    public List<Henna> generateList(final Player player) {
        final List<Henna> list = new ArrayList<>();
        final TIntObjectIterator<Henna> iterator = _hennas.iterator();
        while (iterator.hasNext()) {
            iterator.advance();
            final Henna h = iterator.value();
            if (h.isForThisClass(player)) {
                list.add(h);
            }
        }
        return list;
    }

    @Override
    public int size() {
        return _hennas.size();
    }

    @Override
    public void clear() {
        _hennas.clear();
    }

    private static class LazyHolder {
        private static final HennaHolder INSTANCE = new HennaHolder();
    }
}
