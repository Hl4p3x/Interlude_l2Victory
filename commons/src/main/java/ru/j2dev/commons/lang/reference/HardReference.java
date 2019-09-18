package ru.j2dev.commons.lang.reference;

public interface HardReference<T> {
    T get();

    void clear();
}
