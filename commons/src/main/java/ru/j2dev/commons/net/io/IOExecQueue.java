package ru.j2dev.commons.net.io;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

public class IOExecQueue<IOCli extends IOClient<? extends IOContext<IOCli>>> implements Queue<ReceivablePacket<IOCli>>, Runnable {
    private final Executor executor;
    private final Queue<ReceivablePacket<IOCli>> queue;
    private final AtomicReference<EQueueState> state;
    private volatile long lastAddTime;

    public IOExecQueue(final Executor executor) {
        this.executor = executor;
        queue = new ArrayDeque<>();
        state = new AtomicReference<>(EQueueState.NONE);
    }

    private void exec() {
        if (lastAddTime == 0L) {
            lastAddTime = System.currentTimeMillis();
        }
        executor.execute(this);
    }

    @Override
    public void run() {
        while (state.compareAndSet(EQueueState.QUEUED, EQueueState.RUNNING)) {
            try {
                Runnable r;
                while ((r = poll()) != null) {
                    r.run();
                }
            } finally {
                state.compareAndSet(EQueueState.RUNNING, EQueueState.NONE);
            }
        }
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<ReceivablePacket<IOCli>> iterator() {
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
    public boolean addAll(final Collection<? extends ReceivablePacket<IOCli>> c) {
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
        synchronized (queue) {
            queue.clear();
        }
    }

    @Override
    public boolean add(final ReceivablePacket<IOCli> e) {
        synchronized (queue) {
            if (!queue.add(e)) {
                return false;
            }
        }
        if (state.getAndSet(EQueueState.QUEUED) == EQueueState.NONE) {
            exec();
        }
        return true;
    }

    @Override
    public boolean offer(final ReceivablePacket<IOCli> e) {
        synchronized (queue) {
            return queue.offer(e);
        }
    }

    @Override
    public ReceivablePacket<IOCli> remove() {
        synchronized (queue) {
            return queue.remove();
        }
    }

    @Override
    public ReceivablePacket<IOCli> poll() {
        synchronized (queue) {
            return queue.poll();
        }
    }

    @Override
    public ReceivablePacket<IOCli> element() {
        synchronized (queue) {
            return queue.element();
        }
    }

    @Override
    public ReceivablePacket<IOCli> peek() {
        synchronized (queue) {
            return queue.peek();
        }
    }

    private enum EQueueState {
        NONE,
        QUEUED,
        RUNNING
    }
}