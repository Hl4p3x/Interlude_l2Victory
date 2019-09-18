package ru.j2dev.commons.net.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class IOExecutor extends AbstractExecutorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(IOExecutor.class);
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final AtomicInteger THREAD_FACTORY_COUNT = new AtomicInteger();
    private final int count;
    private final int priority;
    private final long fillDelay;
    private final IOExecThread[] executorThreads;
    private final AtomicBoolean shutdown;
    private final AtomicInteger activeTask;
    private final AtomicLong executedTask;
    private boolean isThreadsStarted;

    public IOExecutor(final int count, final long fillDelay, final int priority) {
        this.count = count;
        this.fillDelay = fillDelay;
        this.priority = priority;
        shutdown = new AtomicBoolean(false);
        activeTask = new AtomicInteger(0);
        executedTask = new AtomicLong(0L);
        final ArrayList<IOExecThread> executorThreads = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            IOExecThread execThread = new IOExecThread();
            execThread.setName("IOExecThread-" + i);
            executorThreads.add(execThread);
        }
        this.executorThreads = executorThreads.toArray(new IOExecThread[0]);
        isThreadsStarted = false;
    }

    public IOExecutor() {
        this(CPU_COUNT, 16L, 5);
    }

    public static ThreadFactory ioThreadFactory(final String name, final int priority) {
        return r ->
        {
            Thread thread = new Thread(r);
            thread.setName(name + "-" + IOExecutor.THREAD_FACTORY_COUNT.incrementAndGet());
            thread.setPriority(priority);
            return thread;
        };
    }

    void startup() {
        if (!isThreadsStarted) {
            for (final Thread executorThread : executorThreads) {
                executorThread.start();
                try {
                    Thread.sleep(Math.min(16L, fillDelay));
                } catch (final Exception ex) {
                    ex.printStackTrace();
                }
            }
            isThreadsStarted = true;
        }
    }

    public int getActiveTaskCount() {
        return activeTask.get();
    }

    public long getExecutedTaskCount() {
        return executedTask.get();
    }

    public int getQueuedTaskCount() {
        int count = 0;
        for (final IOExecThread execThread : executorThreads) {
            count += execThread.getTaskCount();
        }
        return count;
    }

    public int getThreadCount() {
        return executorThreads.length;
    }

    @Override
    public void shutdown() {
        shutdown.set(true);
    }

    @Override
    public List<Runnable> shutdownNow() {
        shutdown.set(true);
        final ArrayList<Runnable> result = new ArrayList<>();
        for (final IOExecThread execThread : executorThreads) {
            Runnable r;
            while ((r = execThread.pollTask()) != null) {
                result.add(r);
            }
        }
        return result;
    }

    @Override
    public boolean isShutdown() {
        return shutdown.get();
    }

    @Override
    public boolean isTerminated() {
        return shutdown.get();
    }

    private boolean isAllThreadFinished() {
        for (final IOExecThread execThread : executorThreads) {
            if (!execThread.isFinished()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
        final long maxTime = System.currentTimeMillis() + unit.toMillis(timeout);
        shutdown();
        while ((System.currentTimeMillis() < maxTime) && (!isAllThreadFinished())) {
            Thread.sleep(fillDelay * 16L);
        }
        return isAllThreadFinished();
    }

    private void doExec(final Runnable r) {
        activeTask.incrementAndGet();
        try {
            r.run();
            executedTask.incrementAndGet();
        } catch (final Throwable th) {
            LOGGER.error("Exception in IOExecutor", th);
        } finally {
            activeTask.decrementAndGet();
        }
    }

    @Override
    public void execute(final Runnable r) {
        int minTasks = Integer.MAX_VALUE;
        int minIdx = 0;
        for (int idx = 0; idx < executorThreads.length; idx++) {
            int currTasks;
            if ((currTasks = executorThreads[idx].getTaskCount()) == 0) {
                executorThreads[idx].put(r);
                return;
            }
            if (currTasks < minTasks) {
                minTasks = currTasks;
                minIdx = idx;
            }
        }
        if (minTasks < Integer.MAX_VALUE) {
            executorThreads[minIdx].put(r);
        } else {
            doExec(r);
        }
    }

    private class IOExecThread extends Thread {
        private final Queue<Runnable> workingQueue;
        private final AtomicInteger taskCount;
        private boolean isFinished;

        private IOExecThread() {
            workingQueue = new ConcurrentLinkedQueue<>();
            taskCount = new AtomicInteger(0);
            isFinished = false;
            setPriority(priority);
        }

        public boolean isFinished() {
            return isFinished;
        }

        private Runnable pollTask() {
            return workingQueue.poll();
        }

        int getTaskCount() {
            return taskCount.get();
        }

        public void put(Runnable r) {
            workingQueue.add(r);
            taskCount.incrementAndGet();
        }

        @Override
        public void run() {
            while (!shutdown.get()) {
                int execCount = 0;
                try {
                    if (taskCount.get() > 0) {
                        Runnable r;
                        while ((r = pollTask()) != null) {
                            ++execCount;
                            doExec(r);
                        }
                    }
                } catch (final Exception ex) {
                    LOGGER.error("Exception in IO executor thread", ex);
                    if (execCount != 0) {
                        if (taskCount.addAndGet(-execCount) != 0) {
                            continue;
                        }
                    }
                    try {
                        Thread.sleep(fillDelay);
                    } catch (final Exception ex1) {
                        ex1.printStackTrace();
                    }
                } finally {
                    if (execCount == 0 || taskCount.addAndGet(-execCount) == 0) {
                        try {
                            Thread.sleep(fillDelay);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
            isFinished = true;
        }
    }
}