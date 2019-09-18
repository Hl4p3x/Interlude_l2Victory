package ru.j2dev.commons.util.concurrent.atomic;

import java.util.concurrent.atomic.AtomicLong;

public class AtomicEnumBitFlag<E extends Enum<E>> {
    private final AtomicLong _field;

    public AtomicEnumBitFlag() {
        _field = new AtomicLong();
    }

    public boolean set(final E emask, final boolean val) {
        final int o = emask.ordinal();
        if (o > 63) {
            throw new IllegalArgumentException("Maxium 64 enum values allowed");
        }
        final long mask = 1L << o;
        long c;
        long n;
        do {
            c = _field.get();
            n = (val ? (c | mask) : (c & ~mask));
        } while (!_field.compareAndSet(c, n));
        return (c & mask) != 0x0L;
    }

    public boolean get(final E emask) {
        final int o = emask.ordinal();
        if (o > 63) {
            throw new IllegalArgumentException("Maxium 64 enum values allowed");
        }
        final long mask = 1L << o;
        return (_field.get() & mask) != 0x0L;
    }
}
