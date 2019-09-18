package ru.j2dev.gameserver.data.xml.holder;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.gameserver.templates.item.support.VariationGroupData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VariationGroupHolder extends AbstractHolder {

    private final List<Pair<int[], VariationGroupData>> _variationGroupData = new ArrayList<>();

    public static VariationGroupHolder getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public int size() {
        return _variationGroupData.size();
    }

    @Override
    public void clear() {
        _variationGroupData.clear();
    }

    public void add(final int[] itemIds, final VariationGroupData vgd) {
        final int[] sortedIds = itemIds.clone();
        Arrays.sort(sortedIds);
        _variationGroupData.add(new ImmutablePair<>(sortedIds, vgd));
    }

    public void addSorted(final int[] sortedIds, final VariationGroupData vgd) {
        _variationGroupData.add(new ImmutablePair<>(sortedIds, vgd));
    }

    public List<VariationGroupData> getDataForItemId(final int itemId) {
        final List<VariationGroupData> resultList = new ArrayList<>();
        _variationGroupData.forEach(e -> {
            final int[] ids = e.getLeft();
            if (Arrays.binarySearch(ids, itemId) >= 0) {
                resultList.add(e.getRight());
            }
        });
        return resultList;
    }

    private static class LazyHolder {
        private static final VariationGroupHolder INSTANCE = new VariationGroupHolder();
    }
}
