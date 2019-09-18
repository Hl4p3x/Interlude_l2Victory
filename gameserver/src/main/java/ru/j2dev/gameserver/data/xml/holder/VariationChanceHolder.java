package ru.j2dev.gameserver.data.xml.holder;

import org.apache.commons.lang3.tuple.Pair;
import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.gameserver.templates.item.support.VariationChanceData;

import java.util.HashMap;

public class VariationChanceHolder extends AbstractHolder {

    private final HashMap<Integer, Pair<VariationChanceData, VariationChanceData>> _minerallChances = new HashMap<>();

    public static VariationChanceHolder getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public int size() {
        return _minerallChances.size();
    }

    @Override
    public void clear() {
        _minerallChances.clear();
    }

    public void add(final Pair<VariationChanceData, VariationChanceData> vcdp) {
        if (vcdp.getLeft() != null && vcdp.getRight() != null && vcdp.getLeft().getMineralItemId() == vcdp.getRight().getMineralItemId()) {
            _minerallChances.put(vcdp.getLeft().getMineralItemId(), vcdp);
        } else if (vcdp.getLeft() != null) {
            _minerallChances.put(vcdp.getLeft().getMineralItemId(), vcdp);
        } else {
            if (vcdp.getRight() == null) {
                throw new RuntimeException("Empty mineral");
            }
            _minerallChances.put(vcdp.getRight().getMineralItemId(), vcdp);
        }
    }

    public Pair<VariationChanceData, VariationChanceData> getVariationChanceDataForMineral(final int mineralItemId) {
        return _minerallChances.get(mineralItemId);
    }

    private static class LazyHolder {
        private static final VariationChanceHolder INSTANCE = new VariationChanceHolder();
    }
}
