/*
 * Copyright (c) 2010-2016 fork3
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
 * THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ru.j2dev.commons.collections;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Версия CopyOnWriteArraySet с методом на небезопасное получение внутреннего массива. Требуется, что бы не мусорить итераторами на задачах
 * частого итерирования коллекции.
 * <p>
 * Запрещено менять значения в небезопасном массиве.
 *
 * @author n3k0nation
 */
public class UnsafeCopyOnWriteArraySet<E> extends AbstractSet<E> {
    private final UnsafeCopyOnWriteArrayList<E> al;

    public UnsafeCopyOnWriteArraySet() {
        al = new UnsafeCopyOnWriteArrayList<>();
    }

    /**
     * Creates a set containing all of the elements of the specified collection.
     *
     * @param c the collection of elements to initially contain
     * @throws NullPointerException if the specified collection is null
     */
    public UnsafeCopyOnWriteArraySet(Collection<? extends E> c) {
        if (c.getClass() == UnsafeCopyOnWriteArraySet.class) {
            @SuppressWarnings("unchecked")
            UnsafeCopyOnWriteArraySet<E> cc = (UnsafeCopyOnWriteArraySet<E>) c;
            al = new UnsafeCopyOnWriteArrayList<>(cc.al);
        } else {
            al = new UnsafeCopyOnWriteArrayList<>();
            al.addAllAbsent(c);
        }
    }

    private static boolean eq(Object o1, Object o2) {
        return (o1 == null) ? o2 == null : o1.equals(o2);
    }

    public final Object[] getUnsafeArray() {
        return al.getUnsafeArray();
    }

    @Override
    public int size() {
        return al.size();
    }

    @Override
    public boolean isEmpty() {
        return al.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return al.contains(o);
    }

    @Override
    public Object[] toArray() {
        return al.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return al.toArray(a);
    }

    @Override
    public void clear() {
        al.clear();
    }

    @Override
    public boolean remove(Object o) {
        return al.remove(o);
    }

    @Override
    public boolean add(E e) {
        return al.addIfAbsent(e);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return al.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return al.addAllAbsent(c) > 0;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return al.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return al.retainAll(c);
    }

    @Override
    public Iterator<E> iterator() {
        return al.iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Set))
            return false;
        Set<?> set = (Set<?>) (o);
        Iterator<?> it = set.iterator();

        // Uses O(n^2) algorithm that is only appropriate
        // for small sets, which CopyOnWriteArraySets should be.

        // Use a single snapshot of underlying array
        Object[] elements = al.getArray();
        int len = elements.length;
        // Mark matched elements to avoid re-checking
        boolean[] matched = new boolean[len];
        int k = 0;
        outer:
        while (it.hasNext()) {
            if (++k > len)
                return false;
            Object x = it.next();
            for (int i = 0; i < len; ++i) {
                if (!matched[i] && eq(x, elements[i])) {
                    matched[i] = true;
                    continue outer;
                }
            }
            return false;
        }
        return k == len;
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        return al.removeIf(filter);
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        al.forEach(action);
    }

    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(al.getArray(), Spliterator.IMMUTABLE | Spliterator.DISTINCT);
    }
}
