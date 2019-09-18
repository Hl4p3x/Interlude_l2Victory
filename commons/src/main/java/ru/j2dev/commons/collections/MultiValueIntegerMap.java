package ru.j2dev.commons.collections;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class MultiValueIntegerMap {
    private final Map<Integer, List<Integer>> map;

    public MultiValueIntegerMap() {
        map = new ConcurrentHashMap<>();
    }

    public Set<Integer> keySet() {
        return map.keySet();
    }

    public Collection<List<Integer>> values() {
        return map.values();
    }

    public List<Integer> allValues() {
        final List<Integer> result = new ArrayList<>();
        map.values().forEach(result::addAll);
        return result;
    }

    public Set<Entry<Integer, List<Integer>>> entrySet() {
        return map.entrySet();
    }

    public List<Integer> remove(final Integer key) {
        return map.remove(key);
    }

    public List<Integer> get(final Integer key) {
        return map.get(key);
    }

    public boolean containsKey(final Integer key) {
        return map.containsKey(key);
    }

    public void clear() {
        map.clear();
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public Integer remove(final Integer key, final Integer value) {
        final List<Integer> valuesForKey = map.get(key);
        if (valuesForKey == null) {
            return null;
        }
        final boolean removed = valuesForKey.remove(value);
        if (!removed) {
            return null;
        }
        if (valuesForKey.isEmpty()) {
            remove(key);
        }
        return value;
    }

    public Integer removeValue(final Integer value) {
        final List<Integer> toRemove = new ArrayList<>(1);
        for (final Entry<Integer, List<Integer>> entry : map.entrySet()) {
            entry.getValue().remove(value);
            if (entry.getValue().isEmpty()) {
                toRemove.add(entry.getKey());
            }
        }
        for (final Integer key : toRemove) {
            remove(key);
        }
        return value;
    }

    public Integer put(final Integer key, final Integer value) {
        List<Integer> coll = map.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>());
        coll.add(value);
        return value;
    }

    public boolean containsValue(final Integer value) {
        for (final Entry<Integer, List<Integer>> entry : map.entrySet()) {
            if (entry.getValue().contains(value)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsValue(final Integer key, final Integer value) {
        final List<Integer> coll = map.get(key);
        return coll != null && coll.contains(value);
    }

    public int size(final Integer key) {
        final List<Integer> coll = map.get(key);
        if (coll == null) {
            return 0;
        }
        return coll.size();
    }

    public boolean putAll(final Integer key, final Collection<? extends Integer> values) {
        if (values == null || values.size() == 0) {
            return false;
        }
        boolean result = false;
        List<Integer> coll = map.get(key);
        if (coll == null) {
            coll = new CopyOnWriteArrayList<>();
            coll.addAll(values);
            if (coll.size() > 0) {
                map.put(key, coll);
                result = true;
            }
        } else {
            result = coll.addAll(values);
        }
        return result;
    }

    public int totalSize() {
        int total = 0;
        for (final Entry<Integer, List<Integer>> entry : map.entrySet()) {
            total += entry.getValue().size();
        }
        return total;
    }
}
