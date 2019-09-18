package ru.j2dev.commons.util;

import gnu.trove.TIntCollection;
import gnu.trove.function.TIntFunction;
import gnu.trove.function.TObjectFunction;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.list.TIntList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.TIntSet;
import org.apache.commons.lang3.ArrayUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Random;

/**
 * @author VISTALL
 * @author Java-man
 * @date 21:59/15.12.2010
 */
public class TroveUtils {
    public static final TIntList EMPTY_INT_LIST = new EmptyTIntList();
    public static final TIntSet EMPTY_INT_SET = new EmptyTIntSet();

    @SuppressWarnings("rawtypes")
    private static final TIntObjectHashMap EMPTY_INT_OBJECT_MAP = new EmptyTIntObjectHashMap();

    @SuppressWarnings("unchecked")
    public static <V> TIntObjectHashMap<V> emptyIntObjectMap() {
        return EMPTY_INT_OBJECT_MAP;
    }

    private static class EmptyTIntList implements TIntList, Serializable {
        private static final long serialVersionUID = 1596961260242480323L;

        @Override
        public int getNoEntryValue() {
            return 0;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean add(final int val) {
            return false;
        }

        @Override
        public void add(final int[] vals) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(final int[] vals, final int offset, final int length) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void insert(final int offset, final int value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void insert(final int offset, final int[] values) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void insert(final int offset, final int[] values, final int valOffset, final int len) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int get(final int offset) {
            return 0;
        }

        @Override
        public int set(final int offset, final int val) {
            return 0;
        }

        @Override
        public void set(final int offset, final int[] values) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(final int offset, final int[] values, final int valOffset, final int length) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int replace(final int offset, final int val) {
            return 0;
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(final int value) {
            return false;
        }

        @Override
        public boolean containsAll(final Collection<?> collection) {
            return false;
        }

        @Override
        public boolean containsAll(final TIntCollection collection) {
            return false;
        }

        @Override
        public boolean containsAll(final int[] array) {
            return false;
        }

        @Override
        public boolean addAll(final Collection<? extends Integer> collection) {
            return false;
        }

        @Override
        public boolean addAll(final TIntCollection collection) {
            return false;
        }

        @Override
        public boolean addAll(final int[] array) {
            return false;
        }

        @Override
        public boolean retainAll(final Collection<?> collection) {
            return false;
        }

        @Override
        public boolean retainAll(final TIntCollection collection) {
            return false;
        }

        @Override
        public boolean retainAll(final int[] array) {
            return false;
        }

        @Override
        public boolean removeAll(final Collection<?> collection) {
            return false;
        }

        @Override
        public boolean removeAll(final TIntCollection collection) {
            return false;
        }

        @Override
        public boolean removeAll(final int[] array) {
            return false;
        }

        @Override
        public int removeAt(final int offset) {
            return 0;
        }

        @Override
        public void remove(final int offset, final int length) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void transformValues(final TIntFunction function) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void reverse() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void reverse(final int from, final int to) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void shuffle(final Random rand) {
            throw new UnsupportedOperationException();
        }

        @Override
        public TIntList subList(final int begin, final int end) {
            return null;
        }

        @Override
        public int[] toArray() {
            return ArrayUtils.EMPTY_INT_ARRAY;
        }

        @Override
        public int[] toArray(final int offset, final int len) {
            return ArrayUtils.EMPTY_INT_ARRAY;
        }

        @Override
        public int[] toArray(final int[] dest) {
            return ArrayUtils.EMPTY_INT_ARRAY;
        }

        @Override
        public int[] toArray(final int[] dest, final int offset, final int len) {
            return ArrayUtils.EMPTY_INT_ARRAY;
        }

        @Override
        public int[] toArray(final int[] dest, final int source_pos, final int dest_pos, final int len) {
            return ArrayUtils.EMPTY_INT_ARRAY;
        }

        @Override
        public boolean forEach(final TIntProcedure procedure) {
            return false;
        }

        @Override
        public boolean forEachDescending(final TIntProcedure procedure) {
            return false;
        }

        @Override
        public void sort() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void sort(final int fromIndex, final int toIndex) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void fill(final int val) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void fill(final int fromIndex, final int toIndex, final int val) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int binarySearch(final int value) {
            return 0;
        }

        @Override
        public int binarySearch(final int value, final int fromIndex, final int toIndex) {
            return 0;
        }

        @Override
        public int indexOf(final int value) {
            return 0;
        }

        @Override
        public int indexOf(final int offset, final int value) {
            return 0;
        }

        @Override
        public int lastIndexOf(final int value) {
            return 0;
        }

        @Override
        public int lastIndexOf(final int offset, final int value) {
            return 0;
        }

        @Override
        public boolean contains(final int value) {
            return false;
        }

        @Override
        public TIntIterator iterator() {
            return null;
        }

        @Override
        public TIntList grep(final TIntProcedure condition) {
            return null;
        }

        @Override
        public TIntList inverseGrep(final TIntProcedure condition) {
            return null;
        }

        @Override
        public int max() {
            return 0;
        }

        @Override
        public int min() {
            return 0;
        }

        @Override
        public int sum() {
            return 0;
        }
    }

    private static class EmptyTIntObjectHashMap extends TIntObjectHashMap<Object> {
        @Override
        public int getNoEntryKey() {
            return 0;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean containsKey(final int key) {
            return false;
        }

        @Override
        public boolean containsValue(final Object value) {
            return false;
        }

        @Override
        public Object get(final int key) {
            return null;
        }

        @Override
        public Object put(final int key, final Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object putIfAbsent(final int key, final Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object remove(final int key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(final Map<? extends Integer, ?> m) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(final TIntObjectMap<?> map) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public TIntSet keySet() {
            return null;
        }

        @Override
        public int[] keys() {
            return ArrayUtils.EMPTY_INT_ARRAY;
        }

        @Override
        public int[] keys(final int[] array) {
            return ArrayUtils.EMPTY_INT_ARRAY;
        }

        @Override
        public Collection<Object> valueCollection() {
            return null;
        }

        @Override
        public Object[] values() {
            return ArrayUtils.EMPTY_OBJECT_ARRAY;
        }

        @Override
        public Object[] values(final Object[] array) {
            return ArrayUtils.EMPTY_OBJECT_ARRAY;
        }

        @Override
        public TIntObjectIterator<Object> iterator() {
            return null;
        }

        @Override
        public boolean forEachKey(final TIntProcedure procedure) {
            return false;
        }

        @Override
        public boolean forEachValue(final TObjectProcedure<? super Object> procedure) {
            return false;
        }

        @Override
        public boolean forEachEntry(final TIntObjectProcedure<? super Object> procedure) {
            return false;
        }

        @Override
        public void transformValues(final TObjectFunction<Object, Object> function) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainEntries(final TIntObjectProcedure<? super Object> procedure) {
            return false;
        }

        @Override
        public boolean equals(final Object o) {
            return false;
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }

    private static class EmptyTIntSet implements TIntSet, Serializable {
        private static final long serialVersionUID = -1645460375266388995L;

        @Override
        public int getNoEntryValue() {
            return 0;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean contains(final int entry) {
            return false;
        }

        @Override
        public TIntIterator iterator() {
            return null;
        }

        @Override
        public int[] toArray() {
            return ArrayUtils.EMPTY_INT_ARRAY;
        }

        @Override
        public int[] toArray(final int[] dest) {
            return ArrayUtils.EMPTY_INT_ARRAY;
        }

        @Override
        public boolean add(final int entry) {
            return false;
        }

        @Override
        public boolean remove(final int entry) {
            return false;
        }

        @Override
        public boolean containsAll(final Collection<?> collection) {
            return false;
        }

        @Override
        public boolean containsAll(final TIntCollection collection) {
            return false;
        }

        @Override
        public boolean containsAll(final int[] array) {
            return false;
        }

        @Override
        public boolean addAll(final Collection<? extends Integer> collection) {
            return false;
        }

        @Override
        public boolean addAll(final TIntCollection collection) {
            return false;
        }

        @Override
        public boolean addAll(final int[] array) {
            return false;
        }

        @Override
        public boolean retainAll(final Collection<?> collection) {
            return false;
        }

        @Override
        public boolean retainAll(final TIntCollection collection) {
            return false;
        }

        @Override
        public boolean retainAll(final int[] array) {
            return false;
        }

        @Override
        public boolean removeAll(final Collection<?> collection) {
            return false;
        }

        @Override
        public boolean removeAll(final TIntCollection collection) {
            return false;
        }

        @Override
        public boolean removeAll(final int[] array) {
            return false;
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean forEach(final TIntProcedure procedure) {
            return false;
        }

        // Preserves singleton property
        protected Object readResolve() {
            return EMPTY_INT_SET;
        }
    }
}
