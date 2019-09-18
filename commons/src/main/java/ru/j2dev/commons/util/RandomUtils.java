package ru.j2dev.commons.util;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.Comparator;

public class RandomUtils {
    public static final Comparator<Pair<?, Double>> DOUBLE_GROUP_COMPARATOR = (o1, o2) -> {
        final double v = o1.getRight() - o2.getRight();
        return Double.compare(v, 0.0);
    };

    public static <G> G pickRandomSortedGroup(final Collection<Pair<G, Double>> sortedGroups, final double total) {
        final double r = total * Rnd.get();
        double share = 0.0;
        for (final Pair<G, Double> group : sortedGroups) {
            share += group.getRight();
            if (r <= share) {
                return group.getLeft();
            }
        }
        return null;
    }
}
