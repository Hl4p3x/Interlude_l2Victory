package ru.j2dev.commons.lang.reference;

public class AbstractHardReference<T> implements HardReference<T> {
    private T reference;

    public AbstractHardReference(final T reference) {
        this.reference = reference;
    }

    @Override
    public T get() {
        return reference;
    }

    @Override
    public void clear() {
        reference = null;
    }

    @Override
    public boolean equals(final Object o) {
        return o == this || (o instanceof AbstractHardReference && ((AbstractHardReference) o).get() != null && ((AbstractHardReference) o).get().equals(get()));
    }
}
