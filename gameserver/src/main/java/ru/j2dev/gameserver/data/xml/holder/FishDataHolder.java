package ru.j2dev.gameserver.data.xml.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.gameserver.templates.item.support.FishGroup;
import ru.j2dev.gameserver.templates.item.support.FishTemplate;
import ru.j2dev.gameserver.templates.item.support.LureTemplate;
import ru.j2dev.gameserver.templates.item.support.LureType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FishDataHolder extends AbstractHolder {

    private final List<FishTemplate> _fishes = new ArrayList<>();
    private final Map<Integer, LureTemplate> _lures = new HashMap<>();
    private final Map<Integer, Map<LureType, Map<FishGroup, Integer>>> _distributionsForZones = new HashMap<>();

    public static FishDataHolder getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void addFish(final FishTemplate fishTemplate) {
        _fishes.add(fishTemplate);
    }

    public void addLure(final LureTemplate template) {
        _lures.put(template.getItemId(), template);
    }

    public void addDistribution(final int id, final LureType lureType, final Map<FishGroup, Integer> map) {
        Map<LureType, Map<FishGroup, Integer>> byLureType = _distributionsForZones.computeIfAbsent(id, k -> new HashMap<>());
        byLureType.put(lureType, map);
    }

    @Override
    public void log() {
        info("load " + _fishes.size() + " fish(es).");
        info("load " + _lures.size() + " lure(s).");
        info("load " + _distributionsForZones.size() + " distribution(s).");
    }

    @Deprecated
    @Override
    public int size() {
        return 0;
    }

    @Override
    public void clear() {
        _fishes.clear();
        _lures.clear();
        _distributionsForZones.clear();
    }

    private static class LazyHolder {
        private static final FishDataHolder INSTANCE = new FishDataHolder();
    }
}
