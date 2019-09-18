package ru.j2dev.commons.collections;

import java.util.Iterator;

/**
 * Created by JunkyFunky
 * on 10.07.2018 11:43
 * group j2dev
 */
public final class IntMap<T> implements Iterable<IntMap.Entry>, Cloneable {

    private Entry[] table;
    private final float loadFactor;
    private int size, mask, capacity, threshold;

    public IntMap() {
        this(16, 0.75f);
    }

    public IntMap(int initialCapacity) {
        this(initialCapacity, 0.75f);
    }

    public IntMap(int initialCapacity, float loadFactor) {
        if (initialCapacity > 1 << 30) {
            throw new IllegalArgumentException("initialCapacity is too large.");
        }
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("initialCapacity must be greater than zero.");
        }
        if (loadFactor <= 0) {
            throw new IllegalArgumentException("loadFactor must be greater than zero.");
        }
        capacity = 1;
        while (capacity < initialCapacity) {
            capacity <<= 1;
        }
        this.loadFactor = loadFactor;
        threshold = (int) (capacity * loadFactor);
        table = new Entry[capacity];
        mask = capacity - 1;
    }

    @Override
    @SuppressWarnings("unchecked")
    public IntMap<T> clone() {
        try {
            IntMap<T> clone = (IntMap<T>) super.clone();
            Entry[] newTable = new Entry[table.length];
            for (int i = table.length - 1; i >= 0; i--) {
                if (table[i] != null) {
                    newTable[i] = table[i].clone();
                }
            }
            clone.table = newTable;
            return clone;
        }
        catch (CloneNotSupportedException ex) {
        }
        return null;
    }

    public boolean containsValue(Object value) {
        Entry[] table = this.table;
        for (int i = table.length; i-- > 0;) {
            for (Entry e = table[i]; e != null; e = e.next) {
                if (e.value.equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean containsKey(int key) {
        int index = (key) & mask;
        for (Entry e = table[index]; e != null; e = e.next) {
            if (e.key == key) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public T get(int key) {
        int index = key & mask;
        for (Entry e = table[index]; e != null; e = e.next) {
            if (e.key == key) {
                return (T) e.value;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public T put(int key, T value) {
        int index = key & mask;
        // Check if key already exists.
        for (Entry e = table[index]; e != null; e = e.next) {
            if (e.key != key) {
                continue;
            }
            Object oldValue = e.value;
            e.value = value;
            return (T) oldValue;
        }
        table[index] = new Entry(key, value, table[index]);
        if (size++ >= threshold) {
            // Rehash.
            int newCapacity = 2 * capacity;
            Entry[] newTable = new Entry[newCapacity];
            Entry[] src = table;
            int bucketmask = newCapacity - 1;
            for (int j = 0; j < src.length; j++) {
                Entry e = src[j];
                if (e != null) {
                    src[j] = null;
                    do {
                        Entry next = e.next;
                        index = e.key & bucketmask;
                        e.next = newTable[index];
                        newTable[index] = e;
                        e = next;
                    }
                    while (e != null);
                }
            }
            table = newTable;
            capacity = newCapacity;
            threshold = (int) (newCapacity * loadFactor);
            mask = capacity - 1;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public T remove(int key) {
        int index = key & mask;
        Entry prev = table[index];
        Entry e = prev;
        while (e != null) {
            Entry next = e.next;
            if (e.key == key) {
                size--;
                if (prev == e) {
                    table[index] = next;
                }
                else {
                    prev.next = next;
                }
                return (T) e.value;
            }
            prev = e;
            e = next;
        }
        return null;
    }

    public int size() {
        return size;
    }

    public void clear() {
        Entry[] table = this.table;
        for (int index = table.length; --index >= 0;) {
            table[index] = null;
        }
        size = 0;
    }

    @Override
    public Iterator<Entry> iterator() {
        return new IntMapIterator();
    }

    final class IntMapIterator implements Iterator<Entry> {

        private Entry cur;
        private int idx;
        private int el;

        public IntMapIterator() {
            cur = table[0];
        }

        @Override
        public boolean hasNext() {
            return el < size;
        }

        @Override
        public Entry next() {
            if (el >= size) {
                throw new IllegalStateException("No more elements!");
            }

            if (cur != null) {
                Entry e = cur;
                cur = cur.next;
                el++;
                return e;
            }

            do {
                cur = table[idx++];
            }
            while (cur == null);
            Entry e = cur;
            cur = cur.next;
            el++;
            return e;
        }

        @Override
        public void remove() {
        }
    }

    public static final class Entry<T> implements Cloneable {

        final int key;
        T value;
        Entry next;

        Entry(int k, T v, Entry n) {
            key = k;
            value = v;
            next = n;
        }

        public int getKey() {
            return key;
        }

        public T getValue() {
            return value;
        }

        @Override
        public String toString() {
            return key + " => " + value;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Entry<T> clone() {
            try {
                Entry<T> clone = (Entry<T>) super.clone();
                clone.next = next != null ? next.clone() : null;
                return clone;
            }
            catch (CloneNotSupportedException ex) {
            }
            return null;
        }
    }
}
