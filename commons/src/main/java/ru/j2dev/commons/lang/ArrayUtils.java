package ru.j2dev.commons.lang;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class ArrayUtils {

    public static final int INDEX_NOT_FOUND = -1;

    /**
     * Check if index is in valid range of array, if so return array value
     *
     * @param array
     * @param index <p>
     * @return array element or null, if index out of range
     */
    public static <T> T valid(final T[] array, final int index) {
        if (array == null) {
            return null;
        }
        if (index < 0 || array.length <= index) {
            return null;
        }
        return array[index];
    }

    /**
     * Check if index is in valid range of array, if so return array value
     *
     * @param array
     * @param index <p>
     * @return array element or null, if index out of range
     */
    public static <T> T valid(final List<T> array, final int index) {
        if (array == null) {
            return null;
        }
        if (index < 0 || array.size() <= index) {
            return null;
        }
        return array.get(index);
    }

    /**
     * Enlarge and add element to array
     *
     * @param array
     * @param element <p>
     * @return new array with element
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> T[] add(final T[] array, final T element) {
        final Class type = array != null ? array.getClass().getComponentType() : element != null ? element.getClass() : Object.class;
        final T[] newArray = (T[]) copyArrayGrow(array, type);
        newArray[newArray.length - 1] = element;
        return newArray;
    }

    @SuppressWarnings("unchecked")
    private static <T> T[] copyArrayGrow(final T[] array, final Class<? extends T> type) {
        if (array != null) {
            final int arrayLength = Array.getLength(array);
            final T[] newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), arrayLength + 1);
            System.arraycopy(array, 0, newArray, 0, arrayLength);
            return newArray;
        }
        return (T[]) Array.newInstance(type, 1);
    }

    /**
     * Check if value is one of the elements of array, from starting index, and
     * returns it position in array
     *
     * @param array
     * @param value
     * @param index <p>
     * @return position of value in array, or INDEX_NOT_FOUND
     */
    public static <T> int indexOf(final T[] array, final T value, final int index) {
        if (index < 0 || array.length <= index) {
            return INDEX_NOT_FOUND;
        }

        return IntStream.range(index, array.length).filter(i -> value == array[i]).findFirst().orElse(INDEX_NOT_FOUND);

    }

    /**
     * Trim and remove element from array
     *
     * @param array
     * @param value <p>
     * @return new array without element, if it present in array
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] remove(final T[] array, final T value) {
        if (array == null) {
            return null;
        }

        final int index = indexOf(array, value, 0);

        if (index == INDEX_NOT_FOUND) {
            return array;
        }

        final int length = array.length;

        final T[] newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), length - 1);
        System.arraycopy(array, 0, newArray, 0, index);
        if (index < length - 1) {
            System.arraycopy(array, index + 1, newArray, index, length - index - 1);
        }

        return newArray;
    }

    public static int[] createAscendingArray(final int min, final int max) {
        final int length = max - min;
        final int[] array = new int[length + 1];
        int x = 0;
        for (int i = min; i <= max; i++, x++) {
            array[x] = i;
        }
        return array;
    }

    /**
     * @param array - the array to look into
     * @param obj   - the integer to search for
     *              <p>
     * @return {@code true} if the {@code array} contains the {@code obj},
     * {@code false} otherwise
     */
    public static boolean contains(final int[] array, final int obj) {
        return IntStream.of(array).anyMatch(element -> element == obj);
    }

    public static <T> boolean contains(final T[] array, final T obj) {
        return Stream.of(array).anyMatch(element -> element == obj);
    }

    public static <T> boolean contains(final List<T> array, final T obj) {
        return array.stream().anyMatch(element -> element == obj);
    }

    public static boolean equals(final String[] array, final String obj) {
        return Stream.of(array).anyMatch(element -> element.equalsIgnoreCase(obj));
    }

    public static String ArrayToString(final String[] array, final int start) {
        StringBuilder text = new StringBuilder();
        if (array.length > 1) {
            int count = 1;
            for (int i = start; i < array.length; ++i) {
                text.append(count > 1 ? " " : "").append(array[i]);
                ++count;
            }
        } else {
            text = new StringBuilder(array[start]);
        }
        return text.toString();
    }

    public static int[] toIntArray(final Collection<Integer> collection) {
        final int[] ar = new int[collection.size()];
        int i = 0;
        for (final int t : collection) {
            ar[i++] = t;
        }
        return ar;
    }

    public static int[] toArray(final Collection<Integer> collection) {
        final int[] ar = new int[collection.size()];
        int i = 0;
        for (final int t : collection) {
            ar[i++] = t;
        }
        return ar;
    }

    public static double[] toDoubleArray(final Collection<Double> collection) {
        final double[] ar = new double[collection.size()];
        int i = 0;
        for (final double t : collection) {
            ar[i++] = t;
        }
        return ar;
    }

    public static Object[] toObjectArray(final Collection<Object> collection) {
        final Object[] ar = new Object[collection.size()];
        int i = 0;
        for (final Object t : collection) {
            ar[i++] = t;
        }
        return ar;
    }

    public static int indexOf(final int[] array, final int valueToFind) {
        return indexOf(array, valueToFind, 0);
    }

    public static int indexOf(final int[] array, final int valueToFind, int startIndex) {
        if (array == null) {
            return -1;
        } else {
            if (startIndex < 0) {
                startIndex = 0;
            }

            return IntStream.range(startIndex, array.length).filter(i -> valueToFind == array[i]).findFirst().orElse(-1);

        }
    }

    public static int[] parseInt(String range, String spliter) {
        if (range.contains(spliter)) {
            return getIntegerList(range.split(spliter));
        }
        return new int[]{getInt(range)};
    }

    private static int getInt(String number) {
        return Integer.parseInt(number);
    }

    public static int[] getIntegerList(String[] numbers) {
        return Stream.of(numbers).mapToInt(ArrayUtils::getInt).toArray();
    }

    public static double[] parseDouble(String range, String spliter) {
        if (range.contains(spliter)) {
            return getDoubleList(range.split(spliter));
        }
        return new double[]{getDouble(range)};
    }

    private static double getDouble(String number) {
        return Double.parseDouble(number);
    }

    public static double[] getDoubleList(String[] numbers) {
        return Stream.of(numbers).mapToDouble(ArrayUtils::getDouble).toArray();
    }

    public static long[] parseLong(String range, String spliter) {
        if (range.contains(spliter)) {
            return getLongList(range.split(spliter));
        }
        return new long[]{getLong(range)};
    }

    private static long getLong(String number) {
        return Long.parseLong(number);
    }

    public static long[] getLongList(String[] numbers) {
        return Stream.of(numbers).mapToLong(ArrayUtils::getLong).toArray();
    }
}
