package ru.j2dev.gameserver;

import ru.j2dev.commons.threading.LoggingRejectedExecutionHandler;
import ru.j2dev.commons.threading.PriorityThreadFactory;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.threading.RunnableStatsWrapper;

import java.util.concurrent.*;

public class ThreadPoolManager {
    private final ScheduledThreadPoolExecutor scheduledExecutor;
    private final ThreadPoolExecutor executor;
    private volatile boolean shutdown;

    private ThreadPoolManager() {
        scheduledExecutor = new ScheduledThreadPoolExecutor(Config.SCHEDULED_THREAD_POOL_SIZE, new PriorityThreadFactory("ScheduledThreadPool", Thread.NORM_PRIORITY), new LoggingRejectedExecutionHandler());
        executor = new ThreadPoolExecutor(Config.EXECUTOR_THREAD_POOL_SIZE, Integer.MAX_VALUE, 5L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new PriorityThreadFactory("ThreadPoolExecutor", Thread.NORM_PRIORITY), new LoggingRejectedExecutionHandler());

        //Очистка каждые 5 минут
        scheduleAtFixedRate(new RunnableImpl() {
            @Override
            public void runImpl() {
                scheduledExecutor.purge();
                executor.purge();
            }
        }, 5L, 5L, TimeUnit.MINUTES);
    }

    public static ThreadPoolManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    public static Runnable wrap(final Runnable r) {
        return Config.ENABLE_RUNNABLE_STATS ? RunnableStatsWrapper.wrap(r) : r;
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public ScheduledFuture<?> schedule(final Runnable r, final long delay) {
        return schedule(r, delay, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> schedule(final Runnable r, final long delay, final TimeUnit timeUnit) {
        return scheduledExecutor.schedule(wrap(r), delay, timeUnit);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(final Runnable r, final long initial, final long delay) {
        return scheduleAtFixedRate(r, initial, delay, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(final Runnable r, final long initial, final long delay, final TimeUnit timeUnit) {
        return scheduledExecutor.scheduleAtFixedRate(wrap(r), initial, delay, timeUnit);
    }

    public ScheduledFuture<?> scheduleAtFixedDelay(final Runnable r, final long initial, final long delay) {
        return scheduleAtFixedDelay(r, initial, delay, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> scheduleAtFixedDelay(final Runnable r, final long initial, final long delay, final TimeUnit timeUnit) {
        return scheduledExecutor.scheduleWithFixedDelay(wrap(r), initial, delay, timeUnit);
    }

    public void execute(final Runnable r) {
        executor.execute(wrap(r));
    }

    public void shutdown() throws InterruptedException {
        shutdown = true;
        try {
            scheduledExecutor.shutdown();

            scheduledExecutor.awaitTermination(10, TimeUnit.SECONDS);
        } finally {
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.MINUTES);
        }
    }

    public CharSequence getStats() {
        final StringBuilder list = new StringBuilder();

        list.append("ScheduledThreadPool\n");
        list.append("=================================================\n");
        list.append("\tgetActiveCount: ...... ").append(scheduledExecutor.getActiveCount()).append('\n');
        list.append("\tgetCorePoolSize: ..... ").append(scheduledExecutor.getCorePoolSize()).append('\n');
        list.append("\tgetPoolSize: ......... ").append(scheduledExecutor.getPoolSize()).append('\n');
        list.append("\tgetLargestPoolSize: .. ").append(scheduledExecutor.getLargestPoolSize()).append('\n');
        list.append("\tgetMaximumPoolSize: .. ").append(scheduledExecutor.getMaximumPoolSize()).append('\n');
        list.append("\tgetCompletedTaskCount: ").append(scheduledExecutor.getCompletedTaskCount()).append('\n');
        list.append("\tgetQueuedTaskCount: .. ").append(scheduledExecutor.getQueue().size()).append('\n');
        list.append("\tgetTaskCount: ........ ").append(scheduledExecutor.getTaskCount()).append('\n');
        list.append("ThreadPoolExecutor\n");
        list.append("=================================================\n");
        list.append("\tgetActiveCount: ...... ").append(executor.getActiveCount()).append('\n');
        list.append("\tgetCorePoolSize: ..... ").append(executor.getCorePoolSize()).append('\n');
        list.append("\tgetPoolSize: ......... ").append(executor.getPoolSize()).append('\n');
        list.append("\tgetLargestPoolSize: .. ").append(executor.getLargestPoolSize()).append('\n');
        list.append("\tgetMaximumPoolSize: .. ").append(executor.getMaximumPoolSize()).append('\n');
        list.append("\tgetCompletedTaskCount: ").append(executor.getCompletedTaskCount()).append('\n');
        list.append("\tgetQueuedTaskCount: .. ").append(executor.getQueue().size()).append('\n');
        list.append("\tgetTaskCount: ........ ").append(executor.getTaskCount()).append('\n');
        list.append("PathfindThreadPool\n");
        list.append("=================================================\n");

        return list;
    }

    private static class LazyHolder {
        private static final ThreadPoolManager INSTANCE = new ThreadPoolManager();
    }
}
