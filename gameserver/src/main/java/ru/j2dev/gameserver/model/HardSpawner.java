package ru.j2dev.gameserver.model;

import ru.j2dev.commons.collections.MultiValueSet;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.templates.StatsSet;
import ru.j2dev.gameserver.templates.spawn.SpawnNpcInfo;
import ru.j2dev.gameserver.templates.spawn.SpawnRange;
import ru.j2dev.gameserver.templates.spawn.SpawnTemplate;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class HardSpawner extends Spawner {
    private final SpawnTemplate _template;
    private final ConcurrentMap<Integer, Queue<NpcInstance>> _cache = new ConcurrentHashMap<>();
    private final List<NpcInstance> _reSpawned = new CopyOnWriteArrayList<>();
    private int _pointIndex;
    private int _npcIndex;

    public HardSpawner(final SpawnTemplate template) {
        _template = template;
        _spawned = new CopyOnWriteArrayList<>();
    }

    @Override
    public void decreaseCount(final NpcInstance oldNpc) {
        addToCache(oldNpc);
        _spawned.remove(oldNpc);
        final SpawnNpcInfo npcInfo = getNextNpcInfo();
        NpcInstance npc = getCachedNpc(npcInfo.getTemplate().getNpcId());
        if (npc == null) {
            npc = npcInfo.getTemplate().getNewInstance();
        } else {
            npc.refreshID();
        }
        npc.setSpawn(this);
        _reSpawned.add(npc);
        decreaseCount0(npcInfo.getTemplate(), npc, oldNpc.getDeadTime());
    }

    @Override
    public NpcInstance doSpawn(final boolean spawn) {
        final SpawnNpcInfo npcInfo = getNextNpcInfo();
        return doSpawn0(npcInfo.getTemplate(), spawn, npcInfo.getParameters());
    }

    @Override
    protected NpcInstance initNpc(final NpcInstance mob, final boolean spawn, final MultiValueSet<String> set) {
        _reSpawned.remove(mob);
        final SpawnRange range = _template.getSpawnRange(getNextRangeId());
        mob.setSpawnRange(range);
        return initNpc0(mob, range.getRandomLoc(getReflection().getGeoIndex()), spawn, set);
    }

    @Override
    public int getCurrentNpcId() {
        final SpawnNpcInfo npcInfo = _template.getNpcId(_npcIndex);
        return npcInfo.getTemplate().npcId;
    }

    @Override
    public SpawnRange getCurrentSpawnRange() {
        return _template.getSpawnRange(_pointIndex);
    }

    @Override
    public void respawnNpc(final NpcInstance oldNpc) {
        initNpc(oldNpc, true, StatsSet.EMPTY);
    }

    @Override
    public void deleteAll() {
        super.deleteAll();
        _reSpawned.forEach(this::addToCache);
        _reSpawned.clear();
        _cache.values().forEach(Collection::clear);
        _cache.clear();
    }

    private synchronized SpawnNpcInfo getNextNpcInfo() {
        SpawnNpcInfo npcInfo;
        int attempts = 0;
        final int old = _npcIndex++;
        if (_npcIndex >= _template.getNpcSize()) {
            _npcIndex = 0;
        }
        npcInfo = _template.getNpcId(old);
        if (npcInfo.getMax() > 0) {
            int count = (int) _spawned.stream().filter(npc -> npc.getNpcId() == npcInfo.getTemplate().getNpcId()).count();
            if (count >= npcInfo.getMax() && attempts++ > _template.getNpcSize() * 2) {
                throw new IllegalStateException("getNextNpcInfo failed (" + count + ", " + npcInfo.getMax() + ", " + npcInfo.getNpcId() + ")");
            }
        }
        return npcInfo;
    }

    private synchronized int getNextRangeId() {
        final int old = _pointIndex++;
        if (_pointIndex >= _template.getSpawnRangeSize()) {
            _pointIndex = 0;
        }
        return old;
    }

    @Override
    public HardSpawner clone() {
        final HardSpawner spawnDat = new HardSpawner(_template);
        spawnDat.setAmount(_maximumCount);
        spawnDat.setRespawnDelay(_respawnDelay, _respawnDelayRandom);
        spawnDat.setRespawnTime(0);
        spawnDat.setRespawnCron(getRespawnCron());
        return spawnDat;
    }

    private void addToCache(final NpcInstance npc) {
        npc.setSpawn(null);
        npc.decayMe();
        Queue<NpcInstance> queue = _cache.computeIfAbsent(npc.getNpcId(), k -> new ArrayDeque<>());
        queue.add(npc);
    }

    private NpcInstance getCachedNpc(final int id) {
        final Queue<NpcInstance> queue = _cache.get(id);
        if (queue == null) {
            return null;
        }
        final NpcInstance npc = queue.poll();
        if (npc != null && npc.isDeleted()) {
            HardSpawner.LOGGER.info("Npc: " + id + " is deleted, cant used by cache.");
            return getCachedNpc(id);
        }
        return npc;
    }

    public SpawnTemplate getTemplate() {
        return _template;
    }
}
