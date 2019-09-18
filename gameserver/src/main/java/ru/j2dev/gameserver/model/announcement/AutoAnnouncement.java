package ru.j2dev.gameserver.model.announcement;

import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.utils.AnnouncementUtils;

import java.util.concurrent.Future;

/**
 * @author Java-man
 */
public class AutoAnnouncement extends RunnableImpl {
    private Future<?> task;
    private Announcement announcement;
    private int limit;

    public AutoAnnouncement(final Announcement announcement) {
        this.announcement = announcement;
        limit = announcement.getLimit();
        task = ThreadPoolManager.getInstance().schedule(this, announcement.getInitialDelay() * 1000);
    }

    @Override
    public void runImpl() {
        start();
    }

    public void start() {
        if (limit == 0 || announcement == null) {
            stop();
            return;
        }
        AnnouncementUtils.announceToAll(announcement.getMessage());
        task = ThreadPoolManager.getInstance().schedule(this, announcement.getDelay() * 1000);
        limit--;
    }

    public void stop() {
        if (task != null) {
            task.cancel(false);
            task = null;
        }
        announcement = null;
        limit = -1;
    }

    public Announcement getAnnouncement() {
        return announcement;
    }
}
