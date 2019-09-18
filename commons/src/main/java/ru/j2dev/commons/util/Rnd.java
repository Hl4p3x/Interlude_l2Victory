package ru.j2dev.commons.util;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Rnd {
    private Rnd() {
    }

    public static double get() {
        return ThreadLocalRandom.current().nextDouble();
    }

    /**
     * Gets a random number from 0(inclusive) to n(exclusive)
     *
     * @param n The superior limit (exclusive)
     * @return A number from 0 to n-1
     */
    public static int get(final int n) {
        return ThreadLocalRandom.current().nextInt(n);
    }

    public static long get(final long n) {
        return ThreadLocalRandom.current().nextLong(n);
    }

    public static double get(final double n) {
        return ThreadLocalRandom.current().nextDouble(n);
    }

    public static int get(final int min, final int max) { // get random number from min to max (not max-1 !)
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static long get(final long min, final long max) { // get random number from min to max (not max-1 !)
        return ThreadLocalRandom.current().nextLong(min, max + 1);
    }

    public static double get(final double min, final double max) { // get random number from min to max (not max-1 !)
        return ThreadLocalRandom.current().nextDouble(min, max + 1);
    }

    public static int nextInt() {
        return ThreadLocalRandom.current().nextInt();
    }

    public static double nextDouble() {
        return ThreadLocalRandom.current().nextDouble();
    }

    public static double nextGaussian() {
        return ThreadLocalRandom.current().nextGaussian();
    }

    public static boolean nextBoolean() {
        return ThreadLocalRandom.current().nextBoolean();
    }

    public static byte[] nextBytes(final byte[] array) {
        ThreadLocalRandom.current().nextBytes(array);
        return array;
    }

    /**
     * Рандомайзер для подсчета шансов.<br>
     * Рекомендуется к использованию вместо Rnd.get() если нужны очень маленькие шансы
     *
     * @param value в процентах от 0 до 100
     * @return true в случае успешного выпадания.
     */

    public static boolean chance(final double value) {
        return get() * 100.0 < value;
    }

    public static <E> E get(final E[] list) {
        return list[get(list.length)];
    }

    public static int get(final int[] list) {
        return list[get(list.length)];
    }

    public static long get(final long[] list) {
        return list[get(list.length)];
    }

    public static double get(final double[] list) {
        return list[get(list.length)];
    }

    public static short get(final short[] list) {
        return list[get(list.length)];
    }

    public static <E> E get(final List<E> list) {
        return list.get(get(list.size()));
    }
}
