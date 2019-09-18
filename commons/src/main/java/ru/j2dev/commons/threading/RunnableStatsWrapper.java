package ru.j2dev.commons.threading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunnableStatsWrapper implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(RunnableStatsWrapper.class);

    private final Runnable _runnable;

    RunnableStatsWrapper(final Runnable runnable) {
        _runnable = runnable;
    }

    public static Runnable wrap(final Runnable runnable) {
        return new RunnableStatsWrapper(runnable);
    }

    public static void execute(final Runnable runnable) {
        final long begin = System.nanoTime();
        try {
            runnable.run();
            RunnableStatsManager.getInstance().handleStats(runnable.getClass(), System.nanoTime() - begin);
        } catch (Exception e) {
            LOGGER.error("Exception in a Runnable execution:", e);
        }
    }

    @Override
    public void run() {
        execute(_runnable);
    }
}
