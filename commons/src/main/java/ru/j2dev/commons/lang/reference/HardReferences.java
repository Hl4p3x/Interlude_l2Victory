package ru.j2dev.commons.lang.reference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("unchecked")
public class HardReferences {
    private static HardReference<?> EMPTY_REF = new EmptyReferencedHolder(null);

    public static <T> HardReference<T> emptyRef() {
        return (HardReference<T>) EMPTY_REF;
    }

    public static <T> Collection<T> unwrap(final Collection<HardReference<T>> refs) {
        final List<T> result = new ArrayList<>(refs.size());
        for (final HardReference<T> ref : refs) {
            final T obj = ref.get();
            if (obj != null) {
                result.add(obj);
            }
        }
        return result;
    }

    public static <T> Iterable<T> iterate(final Iterable<HardReference<T>> refs) {
        return new WrappedIterable<>(refs);
    }

    private static class EmptyReferencedHolder extends AbstractHardReference<Object> {
        public EmptyReferencedHolder(final Object reference) {
            super(reference);
        }
    }

    private static class WrappedIterable<T> implements Iterable<T> {
        final Iterable<HardReference<T>> refs;

        WrappedIterable(final Iterable<HardReference<T>> refs) {
            this.refs = refs;
        }

        @Override
        public Iterator<T> iterator() {
            return new WrappedIterator<>(refs.iterator());
        }

        private static class WrappedIterator<T> implements Iterator<T> {
            final Iterator<HardReference<T>> iterator;

            WrappedIterator(final Iterator<HardReference<T>> iterator) {
                this.iterator = iterator;
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public T next() {
                return iterator.next().get();
            }

            @Override
            public void remove() {
                iterator.remove();
            }
        }
    }
}
