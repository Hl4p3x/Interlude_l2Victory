package ru.j2dev.commons.threading;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class PriorityThreadFactory implements ThreadFactory {
    private int _prio;
    private String _name;
    private AtomicInteger _threadNumber;
    private ThreadGroup _group;

    public PriorityThreadFactory(final String name, final int prio) {
        _threadNumber = new AtomicInteger(1);
        _prio = prio;
        _name = name;
        _group = new ThreadGroup(_name);
    }

    @Override
    public Thread newThread(final Runnable r) {
        final Thread t = new Thread(_group, r);
        t.setName(_name + "-" + _threadNumber.getAndIncrement());
        t.setPriority(_prio);
        return t;
    }

    public ThreadGroup getGroup() {
        return _group;
    }
}
