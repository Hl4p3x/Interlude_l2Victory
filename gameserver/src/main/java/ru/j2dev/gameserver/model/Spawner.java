package ru.j2dev.gameserver.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.collections.MultiValueSet;
import ru.j2dev.commons.time.cron.NextTime;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.geodata.GeoEngine;
import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.model.entity.events.EventOwner;
import ru.j2dev.gameserver.model.instances.MinionInstance;
import ru.j2dev.gameserver.model.instances.MonsterInstance;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.instances.PetInstance;
import ru.j2dev.gameserver.taskmanager.SpawnTaskManager;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.templates.spawn.SpawnRange;
import ru.j2dev.gameserver.utils.Location;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public abstract class Spawner extends EventOwner implements Cloneable {
    protected static final Logger LOGGER = LoggerFactory.getLogger(Spawner.class);
    protected static final int MIN_RESPAWN_DELAY = 300;
    private static final long serialVersionUID = 1L;
    protected final AtomicInteger _currentCount = new AtomicInteger(0);
    protected final AtomicInteger _scheduledCount = new AtomicInteger(0);
    protected int _maximumCount;
    protected int _referenceCount;
    protected long _respawnDelay;
    protected long _respawnDelayRandom;
    protected NextTime _respawnCron;
    protected int _respawnTime;
    protected boolean _doRespawn;
    protected NpcInstance _lastSpawn;
    protected List<NpcInstance> _spawned;
    protected Reflection _reflection = ReflectionManager.DEFAULT;

    public Spawner() {
    }

    public void decreaseScheduledCount() {
        int scheduledCount;
        do {
            scheduledCount = _scheduledCount.get();
            if (scheduledCount > 0) {
                continue;
            }
        } while (!_scheduledCount.compareAndSet(scheduledCount, scheduledCount - 1));
    }

    public boolean isDoRespawn() {
        return _doRespawn;
    }

    public Reflection getReflection() {
        return _reflection;
    }

    public void setReflection(final Reflection reflection) {
        _reflection = reflection;
    }

    public long getRespawnDelay() {
        return _respawnDelay;
    }

    public void setRespawnDelay(final int respawnDelay) {
        setRespawnDelay(respawnDelay, 0L);
    }

    public long getRespawnDelayRandom() {
        return _respawnDelayRandom;
    }

    public long getRespawnDelayWithRnd() {
        if (_respawnDelayRandom == 0L) {
            return _respawnDelay;
        }
        return Rnd.get(_respawnDelay - _respawnDelayRandom, _respawnDelay);
    }

    public int getRespawnTime() {
        return _respawnTime;
    }

    public void setRespawnTime(final int respawnTime) {
        _respawnTime = respawnTime;
    }

    public NpcInstance getLastSpawn() {
        return _lastSpawn;
    }

    public void setAmount(final int amount) {
        if (_referenceCount == 0) {
            _referenceCount = amount;
        }
        _maximumCount = amount;
    }

    public void deleteAll() {
        stopRespawn();
        _spawned.forEach(GameObject::deleteMe);
        _spawned.clear();
        _respawnTime = 0;
        _scheduledCount.set(0);
        _currentCount.set(0);
    }

    public abstract void decreaseCount(final NpcInstance p0);

    public abstract NpcInstance doSpawn(final boolean p0);

    public abstract void respawnNpc(final NpcInstance p0);

    protected abstract NpcInstance initNpc(final NpcInstance p0, final boolean p1, final MultiValueSet<String> p2);

    public abstract int getCurrentNpcId();

    public abstract SpawnRange getCurrentSpawnRange();

    public int init() {
        while (_currentCount.get() + _scheduledCount.get() < _maximumCount) {
            doSpawn(false);
        }
        _doRespawn = true;
        return _currentCount.get();
    }

    public NpcInstance spawnOne() {
        return doSpawn(false);
    }

    public void stopRespawn() {
        _doRespawn = false;
    }

    public void startRespawn() {
        _doRespawn = true;
    }

    public List<NpcInstance> getAllSpawned() {
        return _spawned;
    }

    public NpcInstance getFirstSpawned() {
        final List<NpcInstance> npcs = getAllSpawned();
        return (npcs.size() > 0) ? npcs.get(0) : null;
    }

    public void setRespawnDelay(final long respawnDelay, final long respawnDelayRandom) {
        if (respawnDelay < 0L) {
            LOGGER.warn("respawn delay is negative");
        }
        _respawnDelay = respawnDelay;
        _respawnDelayRandom = respawnDelayRandom;
    }

    public NextTime getRespawnCron() {
        return _respawnCron;
    }

    public void setRespawnCron(final NextTime respawnCron) {
        _respawnCron = respawnCron;
    }

    protected NpcInstance doSpawn0(final NpcTemplate template, boolean spawn, final MultiValueSet<String> set) {
        if (template.isInstanceOf(PetInstance.class) || template.isInstanceOf(MinionInstance.class)) {
            _currentCount.incrementAndGet();
            return null;
        }
        final NpcInstance tmp = template.getNewInstance();
        if (tmp == null) {
            return null;
        }
        if (!spawn) {
            spawn = (_respawnTime <= System.currentTimeMillis() / 1000L + MIN_RESPAWN_DELAY);
        }
        return initNpc(tmp, spawn, set);
    }

    protected NpcInstance initNpc0(final NpcInstance mob, Location newLoc, final boolean spawn, final MultiValueSet<String> set) {
        mob.setParameters(set);
        mob.setCurrentHpMp(mob.getMaxHp(), mob.getMaxMp(), true);
        mob.setSpawn(this);
        // save spawned points
        mob.setSpawnedLoc(newLoc);
        mob.setUnderground(GeoEngine.getHeight(newLoc, getReflection().getGeoIndex()) < GeoEngine.getHeight(newLoc.clone().changeZ(5000), getReflection().getGeoIndex()));
        getEvents().forEach(mob::addEvent);
        if (spawn) {
            mob.setReflection(getReflection());
            if (mob.isMonster()) {
                ((MonsterInstance) mob).setChampion();
            }
            mob.spawnMe(newLoc);
            _currentCount.incrementAndGet();
        } else {
            mob.setLoc(newLoc);
            _scheduledCount.incrementAndGet();
            SpawnTaskManager.getInstance().addSpawnTask(mob, _respawnTime * 1000L - System.currentTimeMillis());
        }
        _spawned.add(mob);
        return _lastSpawn = mob;
    }

    public void decreaseCount0(final NpcTemplate template, final NpcInstance spawnedNpc, final long deadTime) {
        int currentCount;
        do {
            currentCount = _currentCount.get();
        } while (currentCount > 0 && !_currentCount.compareAndSet(currentCount, currentCount - 1));
        if (getRespawnDelay() == 0L && getRespawnCron() == null) {
            return;
        }
        if (_doRespawn && _scheduledCount.get() + _currentCount.get() < _maximumCount) {
            final long now = System.currentTimeMillis();
            long delay;
            if (getRespawnCron() == null) {
                if (template.isRaid) {
                    delay = (long) (Config.ALT_RAID_RESPAWN_MULTIPLIER * getRespawnDelayWithRnd()) * 1000L;
                } else {
                    delay = getRespawnDelayWithRnd() * 1000L;
                }
            } else {
                delay = getRespawnCron().next(now) - now;
            }
            delay = Math.max(1000L, delay - deadTime);
            _respawnTime = (int) ((now + delay) / 1000L);
            _scheduledCount.incrementAndGet();
            SpawnTaskManager.getInstance().addSpawnTask(spawnedNpc, delay);
        }
    }

    @Override
    public String toString() {
        final String sb = _spawned.stream().map(spawnedNpc -> String.valueOf(spawnedNpc.getNpcId())).collect(Collectors.joining());
        return "Spawner{_currentCount=" + _currentCount + ", _maximumCount=" + _maximumCount + ", _referenceCount=" + _referenceCount + ", _scheduledCount=" + _scheduledCount + ", _respawnDelay=" + _respawnDelay + ", _respawnCron=" + _respawnCron + ", _respawnDelayRandom=" + _respawnDelayRandom + ", _respawnTime=" + _respawnTime + ", _doRespawn=" + _doRespawn + ", _lastSpawn=" + _lastSpawn + ", _spawned=" + _spawned + ", _reflection=" + _reflection + '}';
    }
}
