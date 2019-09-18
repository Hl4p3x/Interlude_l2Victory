package ru.j2dev.gameserver.taskmanager;

import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.threading.SteppingRunnableQueueManager;
import ru.j2dev.gameserver.ThreadPoolManager;

public class RegenTaskManager extends SteppingRunnableQueueManager {
    private static final RegenTaskManager _instance = new RegenTaskManager();

    private RegenTaskManager() {
        super(1000L);
        ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 1000L, 1000L);
        ThreadPoolManager.getInstance().scheduleAtFixedRate(new RunnableImpl() {
            @Override
            public void runImpl() {
                purge();
            }
        }, 10000L, 10000L);
    }

    public static RegenTaskManager getInstance() {
        return _instance;
    }
}
