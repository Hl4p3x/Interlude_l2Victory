package ru.j2dev.commons.net.nio.impl;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class MMOExecutableQueue<T extends MMOClient> implements Queue<ReceivablePacket<T>>, Runnable {
    private static final int NONE = 0;
    private static final int QUEUED = 1;
    private static final int RUNNING = 2;
    private final IMMOExecutor<T> _executor;
    private final Queue<ReceivablePacket<T>> _queue;
    private AtomicInteger _state;

    public MMOExecutableQueue(final IMMOExecutor<T> executor) {
        _state = new AtomicInteger(0);
        _executor = executor;
        _queue = new ArrayDeque<>();
    }

    @Override
    public void run() {
        while (_state.compareAndSet(1, 2)) {
            try {
                while (true) {
                    final Runnable t = poll();
                    if (t == null) {
                        break;
                    }
                    t.run();
                }
            } finally {
                _state.compareAndSet(2, 0);
            }
        }
    }

    @Override
    public int size() {
        return _queue.size();
    }

    @Override
    public boolean isEmpty() {
        return _queue.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<ReceivablePacket<T>> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <E> E[] toArray(final E[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(final Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(final Collection<? extends ReceivablePacket<T>> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        synchronized (_queue) {
            _queue.clear();
        }
    }

    @Override
    public boolean add(final ReceivablePacket<T> e) {
        synchronized (_queue) {
            if (!_queue.add(e)) {
                return false;
            }
        }
        if (_state.getAndSet(1) == 0) {
            _executor.execute(this);
        }
        return true;
    }

    @Override
    public boolean offer(final ReceivablePacket<T> e) {
        synchronized (_queue) {
            return _queue.offer(e);
        }
    }

    @Override
    public ReceivablePacket<T> remove() {
        synchronized (_queue) {
            return _queue.remove();
        }
    }

    @Override
    public ReceivablePacket<T> poll() {
        synchronized (_queue) {
            return _queue.poll();
        }
    }

    @Override
    public ReceivablePacket<T> element() {
        synchronized (_queue) {
            return _queue.element();
        }
    }

    @Override
    public ReceivablePacket<T> peek() {
        synchronized (_queue) {
            return _queue.peek();
        }
    }
}
