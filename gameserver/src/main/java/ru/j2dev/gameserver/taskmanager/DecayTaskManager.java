package ru.j2dev.gameserver.taskmanager;

import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.threading.SteppingRunnableQueueManager;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.model.Creature;

import java.util.concurrent.Future;

public class DecayTaskManager extends SteppingRunnableQueueManager {
    private static final DecayTaskManager _instance = new DecayTaskManager();

    private DecayTaskManager() {
        super(500L);
        ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 500L, 500L);
        ThreadPoolManager.getInstance().scheduleAtFixedRate(new RunnableImpl() {
            @Override
            public void runImpl() {
                purge();
            }
        }, 60000L, 60000L);
    }

    public static DecayTaskManager getInstance() {
        return _instance;
    }

    public Future<?> addDecayTask(final Creature actor, final long delay) {
        return schedule(new RunnableImpl() {
            @Override
            public void runImpl() {
                actor.doDecay();
            }
        }, delay);
    }
}
