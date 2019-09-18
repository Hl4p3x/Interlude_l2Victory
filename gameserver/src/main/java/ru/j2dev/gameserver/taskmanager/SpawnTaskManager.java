package ru.j2dev.gameserver.taskmanager;

import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.model.Spawner;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.utils.Util;

import java.util.ArrayList;
import java.util.List;

public class SpawnTaskManager {
    private static SpawnTaskManager _instance;
    private final Object spawnTasks_lock;
    private SpawnTask[] _spawnTasks;
    private int _spawnTasksSize;

    public SpawnTaskManager() {
        _spawnTasks = new SpawnTask[500];
        _spawnTasksSize = 0;
        spawnTasks_lock = new Object();
        ThreadPoolManager.getInstance().scheduleAtFixedRate(new SpawnScheduler(), 2000L, 2000L);
    }

    public static SpawnTaskManager getInstance() {
        if (_instance == null) {
            _instance = new SpawnTaskManager();
        }
        return _instance;
    }

    public void addSpawnTask(final NpcInstance actor, final long interval) {
        removeObject(actor);
        addObject(new SpawnTask(actor, System.currentTimeMillis() + interval));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("============= SpawnTask Manager Report ============\n\r");
        sb.append("Tasks count: ").append(_spawnTasksSize).append("\n\r");
        sb.append("Tasks dump:\n\r");
        final long current = System.currentTimeMillis();
        for (final SpawnTask container : _spawnTasks) {
            if (container != null) {
                sb.append("Class/Name: ").append(container.getClass().getSimpleName()).append('/').append(container.getActor());
                sb.append(" spawn timer: ").append(Util.formatTime((int) ((container.endtime - current) / 1000L))).append("\n\r");
            }
        }
        return sb.toString();
    }

    private void addObject(final SpawnTask decay) {
        synchronized (spawnTasks_lock) {
            if (_spawnTasksSize >= _spawnTasks.length) {
                final SpawnTask[] temp = new SpawnTask[_spawnTasks.length * 2];
                System.arraycopy(_spawnTasks, 0, temp, 0, _spawnTasksSize);
                _spawnTasks = temp;
            }
            _spawnTasks[_spawnTasksSize] = decay;
            ++_spawnTasksSize;
        }
    }

    public void removeObject(final NpcInstance actor) {
        synchronized (spawnTasks_lock) {
            if (_spawnTasksSize > 1) {
                int k = -1;
                for (int i = 0; i < _spawnTasksSize; ++i) {
                    if (_spawnTasks[i].getActor() == actor) {
                        k = i;
                    }
                }
                if (k > -1) {
                    _spawnTasks[k] = _spawnTasks[_spawnTasksSize - 1];
                    _spawnTasks[_spawnTasksSize - 1] = null;
                    --_spawnTasksSize;
                }
            } else if (_spawnTasksSize == 1 && _spawnTasks[0].getActor() == actor) {
                _spawnTasks[0] = null;
                _spawnTasksSize = 0;
            }
        }
    }

    public class SpawnScheduler extends RunnableImpl {
        @Override
        public void runImpl() {
            if (_spawnTasksSize > 0) {
                try {
                    final List<NpcInstance> works = new ArrayList<>();
                    synchronized (spawnTasks_lock) {
                        final long current = System.currentTimeMillis();
                        final int size = _spawnTasksSize;
                        for (int i = size - 1; i >= 0; --i) {
                            try {
                                final SpawnTask container = _spawnTasks[i];
                                if (container != null && container.endtime > 0L && current > container.endtime) {
                                    final NpcInstance actor = container.getActor();
                                    if (actor != null && actor.getSpawn() != null) {
                                        works.add(actor);
                                    }
                                    container.endtime = -1L;
                                }
                                if (container == null || container.getActor() == null || container.endtime < 0L) {
                                    if (i == _spawnTasksSize - 1) {
                                        _spawnTasks[i] = null;
                                    } else {
                                        _spawnTasks[i] = _spawnTasks[_spawnTasksSize - 1];
                                        _spawnTasks[_spawnTasksSize - 1] = null;
                                    }
                                    if (_spawnTasksSize > 0) {
                                        _spawnTasksSize--;
                                    }
                                }
                            } catch (Exception e) {
                                SpawnScheduler.LOGGER.error("", e);
                            }
                        }
                    }
                    for (final NpcInstance work : works) {
                        final Spawner spawn = work.getSpawn();
                        if (spawn == null) {
                            continue;
                        }
                        spawn.decreaseScheduledCount();
                        if (!spawn.isDoRespawn()) {
                            continue;
                        }
                        spawn.respawnNpc(work);
                    }
                } catch (Exception e2) {
                    SpawnScheduler.LOGGER.error("", e2);
                }
            }
        }
    }

    private class SpawnTask {
        private final HardReference<NpcInstance> _npcRef;
        public long endtime;

        SpawnTask(final NpcInstance cha, final long delay) {
            _npcRef = cha.getRef();
            endtime = delay;
        }

        public NpcInstance getActor() {
            return _npcRef.get();
        }
    }
}
