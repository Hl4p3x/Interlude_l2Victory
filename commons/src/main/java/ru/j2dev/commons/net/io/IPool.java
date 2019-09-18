package ru.j2dev.commons.net.io;

public interface IPool<T> {
    T acquire() throws PoolUnderflowException;

    void release(final T p0);
}