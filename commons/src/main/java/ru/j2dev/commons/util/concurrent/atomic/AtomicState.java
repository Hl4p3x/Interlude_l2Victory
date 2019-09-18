package ru.j2dev.commons.util.concurrent.atomic;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

public class AtomicState {
    private static final AtomicIntegerFieldUpdater<AtomicState> stateUpdater = AtomicIntegerFieldUpdater.newUpdater(AtomicState.class, "value");

    private volatile int value;

    public AtomicState(final boolean initialValue) {
        value = (initialValue ? 1 : 0);
    }

    public AtomicState() {
    }

    public final boolean get() {
        return value != 0;
    }

    private boolean getBool(final int value) {
        if (value < 0) {
            throw new IllegalStateException();
        }
        return value > 0;
    }

    public final boolean setAndGet(final boolean newValue) {
        if (newValue) {
            return getBool(stateUpdater.incrementAndGet(this));
        }
        return getBool(stateUpdater.decrementAndGet(this));
    }

    public final boolean getAndSet(final boolean newValue) {
        if (newValue) {
            return getBool(stateUpdater.getAndIncrement(this));
        }
        return getBool(stateUpdater.getAndDecrement(this));
    }
}
