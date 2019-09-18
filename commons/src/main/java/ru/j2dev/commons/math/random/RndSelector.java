package ru.j2dev.commons.math.random;

import ru.j2dev.commons.util.Rnd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RndSelector<E> {

    private final List<RndNode<E>> nodes;
    private int totalWeight;

    public RndSelector() {
        nodes = new ArrayList<>();
    }

    public RndSelector(final int initialCapacity) {
        nodes = new ArrayList<>(initialCapacity);
    }

    public void add(final E value, final int weight) {
        if (value == null || weight <= 0) {
            return;
        }
        totalWeight += weight;
        nodes.add(new RndNode<>(value, weight));
    }

    /**
     * Вернет один из елементов или null, null возможен только если сумма весов
     * всех элементов меньше maxWeight
     */
    public E chance(final int maxWeight) {
        if (maxWeight <= 0) {
            return null;
        }

        Collections.sort(nodes);

        final int r = Rnd.get(maxWeight);
        int weight = 0;
        for (final RndNode<E> node : nodes) {
            if ((weight += node.weight) > r) {
                return node.value;
            }
        }
        return null;
    }

    /**
     * Вернет один из елементов или null, null возможен только если сумма весов
     * всех элементов меньше 100
     */
    public E chance() {
        return chance(100);
    }

    /**
     * Вернет один из елементов
     */
    public E select() {
        return chance(totalWeight);
    }

    public void clear() {
        totalWeight = 0;
        nodes.clear();
    }

    private static class RndNode<T> implements Comparable<RndNode<T>> {

        private final T value;
        private final int weight;

        public RndNode(final T value, final int weight) {
            this.value = value;
            this.weight = weight;
        }

        @Override
        public int compareTo(final RndNode<T> o) {
            return 0;
        }
    }
}
