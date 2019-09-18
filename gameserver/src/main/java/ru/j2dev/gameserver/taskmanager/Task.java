package ru.j2dev.gameserver.taskmanager;

import ru.j2dev.gameserver.taskmanager.TaskManager.ExecutedTask;

import java.util.concurrent.ScheduledFuture;

public abstract class Task {
    public abstract void init();

    public ScheduledFuture<?> launchSpecial(final ExecutedTask instance) {
        return null;
    }

    public abstract String getName();

    public abstract void onTimeElapsed(final ExecutedTask p0);

    public void onDestroy() {
    }
}
