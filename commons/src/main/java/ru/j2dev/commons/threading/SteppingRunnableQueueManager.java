package ru.j2dev.commons.threading;

import org.apache.commons.lang3.mutable.MutableLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Delayed;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("unchecked")
public abstract class SteppingRunnableQueueManager implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(SteppingRunnableQueueManager.class);

    protected final long tickPerStepInMillis;
    private final List<SteppingScheduledFuture<?>> queue;
    private final AtomicBoolean isRunning;

    public SteppingRunnableQueueManager(final long tickPerStepInMillis) {
        queue = new CopyOnWriteArrayList<>();
        isRunning = new AtomicBoolean();
        this.tickPerStepInMillis = tickPerStepInMillis;
    }

    public SteppingScheduledFuture<?> schedule(final Runnable r, final long delay) {
        return schedule(r, delay, delay, false);
    }

    public SteppingScheduledFuture<?> scheduleAtFixedRate(final Runnable r, final long initial, final long delay) {
        return schedule(r, initial, delay, true);
    }

    private SteppingScheduledFuture<?> schedule(final Runnable r, final long initial, final long delay, final boolean isPeriodic) {
        final long initialStepping = getStepping(initial);
        final long stepping = getStepping(delay);
        final SteppingScheduledFuture<?> sr;
        queue.add(sr = new SteppingScheduledFuture<>(r, initialStepping, stepping, isPeriodic));
        return sr;
    }

    private long getStepping(long delay) {
        delay = Math.max(0L, delay);
        return (delay % tickPerStepInMillis > tickPerStepInMillis / 2L) ? (delay / tickPerStepInMillis + 1L) : ((delay < tickPerStepInMillis) ? 1L : (delay / tickPerStepInMillis));
    }

    @Override
    public void run() {
        try {
            if (!isRunning.compareAndSet(false, true)) {
                LOGGER.warn("Slow running queue, managed by " + this + ", queue size : " + queue.size() + "!");
                return;
            }
            try {
                if (queue.isEmpty()) {
                    return;
                }
                for (final SteppingScheduledFuture<?> sr : queue) {
                    if (!sr.isDone()) {
                        sr.run();
                    }
                }
            } finally {
                isRunning.set(false);
            }
        } catch (Throwable th) {
            LOGGER.error("Exception in stepped queue manager", th);
        }
    }

    public void purge() {
        final List<SteppingScheduledFuture<?>> purge = new ArrayList<>();
        for (final SteppingScheduledFuture<?> sr : queue) {
            if (sr.isDone()) {
                purge.add(sr);
            }
        }
        queue.removeAll(purge);
        purge.clear();
    }

    public CharSequence getStats() {
        final StringBuilder list = new StringBuilder();
        final Map<String, MutableLong> stats = new TreeMap<>();
        int total = 0;
        int done = 0;
        for (final SteppingScheduledFuture<?> sr : queue) {
            if (sr.isDone()) {
                ++done;
            } else {
                ++total;
                MutableLong count = stats.get(((SteppingScheduledFuture<Object>) sr).r.getClass().getName());
                if (count == null) {
                    stats.put(((SteppingScheduledFuture<Object>) sr).r.getClass().getName(), count = new MutableLong(1L));
                } else {
                    count.increment();
                }
            }
        }
        stats.forEach((key, value) -> list.append("\t").append(key).append(" : ").append(value.longValue()).append("\n"));
        list.append("Scheduled: ....... ").append(total).append("\n");
        list.append("Done/Cancelled: .. ").append(done).append("\n");
        return list;
    }

    public class SteppingScheduledFuture<V> implements RunnableScheduledFuture<V> {
        private final Runnable r;
        private final long stepping;
        private final boolean isPeriodic;
        private long step;
        private boolean isCancelled;

        public SteppingScheduledFuture(final Runnable r, final long initial, final long stepping, final boolean isPeriodic) {
            this.r = r;
            step = initial;
            this.stepping = stepping;
            this.isPeriodic = isPeriodic;
        }

        @Override
        public void run() {
            final long step = this.step - 1L;
            this.step = step;
            if (step == 0L) {
                try {
                    r.run();
                } catch (Throwable th) {
                    LOGGER.error("Exception in a Runnable execution:", th);
                } finally {
                    if (isPeriodic) {
                        this.step = stepping;
                    }
                }
            }
        }

        @Override
        public boolean isDone() {
            return isCancelled || (!isPeriodic && step == 0L);
        }

        @Override
        public boolean isCancelled() {
            return isCancelled;
        }

        @Override
        public boolean cancel(final boolean mayInterruptIfRunning) {
            return isCancelled = true;
        }

        @Override
        public V get() {
            return null;
        }

        @Override
        public V get(final long timeout, final TimeUnit unit) {
            return null;
        }

        @Override
        public long getDelay(final TimeUnit unit) {
            return unit.convert(step * tickPerStepInMillis, TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(final Delayed o) {
            return 0;
        }

        @Override
        public boolean isPeriodic() {
            return isPeriodic;
        }
    }
}
