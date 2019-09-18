package ru.j2dev.commons.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by JunkyFunky
 * on 26.10.2017.
 * group j2dev
 */
public class ConcurrentHashSet<T> implements Set<T> {

    private final Set<T> set;

    public ConcurrentHashSet() {
        set = ConcurrentHashMap.newKeySet();
    }

    public ConcurrentHashSet(int initCount) {
        set = ConcurrentHashMap.newKeySet(initCount);
    }

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return set.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return set.iterator();
    }

    @Override
    public Object[] toArray() {
        return set.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return set.toArray(a);
    }

    @Override
    public boolean add(T e) {
        return set.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return set.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return set.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        set.addAll(c);
        return true;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return set.retainAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        c.forEach(set::remove);
        return true;
    }

    @Override
    public void clear() {
        set.clear();
    }

}
