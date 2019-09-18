package ru.j2dev.gameserver.taskmanager.actionrunner;

import ru.j2dev.commons.logging.LoggerObject;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.taskmanager.actionrunner.tasks.AutomaticTask;
import ru.j2dev.gameserver.taskmanager.actionrunner.tasks.DeleteExpiredMailTask;
import ru.j2dev.gameserver.taskmanager.actionrunner.tasks.DeleteExpiredVarsTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ActionRunner extends LoggerObject {
    private static ActionRunner _instance = new ActionRunner();

    private final Lock _lock;
    private Map<String, List<ActionWrapper>> _futures;

    private ActionRunner() {
        _futures = new HashMap<>();
        _lock = new ReentrantLock();
        register(new DeleteExpiredVarsTask());
        register(new DeleteExpiredMailTask());
    }

    public static ActionRunner getInstance() {
        return _instance;
    }

    public void register(final AutomaticTask task) {
        register(task.reCalcTime(true), task);
    }

    public void register(final long time, final ActionWrapper wrapper) {
        if (time == 0L) {
            info("Try register " + wrapper.getName() + " not defined time.");
            return;
        }
        if (time <= System.currentTimeMillis()) {
            ThreadPoolManager.getInstance().execute(wrapper);
            return;
        }
        addScheduled(wrapper.getName(), wrapper, time - System.currentTimeMillis());
    }

    protected void addScheduled(final String name, final ActionWrapper r, final long diff) {
        _lock.lock();
        try {
            final String lower = name.toLowerCase();
            List<ActionWrapper> wrapperList = _futures.computeIfAbsent(lower, k -> new ArrayList<>());
            r.schedule(diff);
            wrapperList.add(r);
        } finally {
            _lock.unlock();
        }
    }

    protected void remove(final String name, final ActionWrapper f) {
        _lock.lock();
        try {
            final String lower = name.toLowerCase();
            final List<ActionWrapper> wrapperList = _futures.get(lower);
            if (wrapperList == null) {
                return;
            }
            wrapperList.remove(f);
            if (wrapperList.isEmpty()) {
                _futures.remove(lower);
            }
        } finally {
            _lock.unlock();
        }
    }

    public void clear(final String name) {
        _lock.lock();
        try {
            final String lower = name.toLowerCase();
            final List<ActionWrapper> wrapperList = _futures.remove(lower);
            if (wrapperList == null) {
                return;
            }
            for (final ActionWrapper f : wrapperList) {
                f.cancel();
            }
            wrapperList.clear();
        } finally {
            _lock.unlock();
        }
    }

    public void info() {
        _lock.lock();
        try {
            for (final Entry<String, List<ActionWrapper>> entry : _futures.entrySet()) {
                info("Name: " + entry.getKey() + "; size: " + entry.getValue().size());
            }
        } finally {
            _lock.unlock();
        }
    }
}
