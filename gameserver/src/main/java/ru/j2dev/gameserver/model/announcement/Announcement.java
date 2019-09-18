package ru.j2dev.gameserver.model.announcement;

/**
 * @author Java-man
 */
public class Announcement {
    private String message;
    private boolean critical;
    private boolean auto;
    private int initialDelay = -1;
    private int delay = -1;
    private int limit = -1;

    public Announcement(final String message, final boolean critical, final boolean auto, final int initialDelay, final int delay,
                        final int limit) {
        this.message = message;
        this.critical = critical;
        this.auto = auto;
        this.initialDelay = initialDelay;
        this.delay = delay;
        this.limit = limit;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public boolean isCritical() {
        return critical;
    }

    public void setCritical(final boolean critical) {
        this.critical = critical;
    }

    public boolean isAuto() {
        return auto;
    }

    public void setAuto(final boolean auto) {
        this.auto = auto;
    }

    public int getInitialDelay() {
        return initialDelay;
    }

    public void setInitialDelay(final int initialDelay) {
        this.initialDelay = initialDelay;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(final int delay) {
        this.delay = delay;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(final int limit) {
        this.limit = limit;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Announcement)) {
            return false;
        }

        final Announcement that = (Announcement) o;

        if (auto != that.auto)
            return false;
        if (critical != that.critical)
            return false;
        if (delay != that.delay)
            return false;
        if (initialDelay != that.initialDelay)
            return false;
        if (limit != that.limit)
            return false;
        if (!message.equals(that.message))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = message.hashCode();
        result = 31 * result + (critical ? 1 : 0);
        result = 31 * result + (auto ? 1 : 0);
        result = 31 * result + initialDelay;
        result = 31 * result + delay;
        result = 31 * result + limit;
        return result;
    }
}
