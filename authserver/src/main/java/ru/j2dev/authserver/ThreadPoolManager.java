package ru.j2dev.authserver;

import ru.j2dev.commons.threading.RunnableImpl;

import java.util.concurrent.*;

public class ThreadPoolManager {
    private static final long MAX_DELAY = TimeUnit.NANOSECONDS.toMillis(Long.MAX_VALUE - System.nanoTime()) / 2L;
    private static final ThreadPoolManager _instance = new ThreadPoolManager();

    private final ScheduledThreadPoolExecutor scheduledExecutor;
    private final ThreadPoolExecutor executor;

    private ThreadPoolManager() {
        scheduledExecutor = new ScheduledThreadPoolExecutor(1);
        executor = new ThreadPoolExecutor(1, 1, 5L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        scheduleAtFixedRate(new RunnableImpl() {
            @Override
            public void runImpl() {
                executor.purge();
                scheduledExecutor.purge();
            }
        }, 600000L, 600000L);
    }

    public static ThreadPoolManager getInstance() {
        return _instance;
    }

    private long validate(final long delay) {
        return Math.max(0L, Math.min(MAX_DELAY, delay));
    }

    public void execute(final Runnable r) {
        executor.execute(r);
    }

    public ScheduledFuture<?> schedule(final Runnable r, final long delay) {
        return scheduledExecutor.schedule(r, validate(delay), TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(final Runnable r, final long initial, final long delay) {
        return scheduledExecutor.scheduleAtFixedRate(r, validate(initial), validate(delay), TimeUnit.MILLISECONDS);
    }
}
