package ru.j2dev.commons.threading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public final class LoggingRejectedExecutionHandler implements RejectedExecutionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingRejectedExecutionHandler.class);

    @Override
    public void rejectedExecution(final Runnable r, final ThreadPoolExecutor executor) {
        if (executor.isShutdown()) {
            return;
        }
        LOGGER.error(r + " from " + executor, new RejectedExecutionException());
    }
}
