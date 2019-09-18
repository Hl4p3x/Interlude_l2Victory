package ru.j2dev.gameserver.templates.spawn;

import ru.j2dev.commons.time.cron.NextTime;

import java.util.ArrayList;
import java.util.List;

public class SpawnTemplate {
    private final String _makerName;
    private final String _eventName;
    private final PeriodOfDay _periodOfDay;
    private final int _count;
    private final long _respawn;
    private final long _respawnRandom;
    private final NextTime _respawnCron;
    private final List<SpawnNpcInfo> _npcList;
    private final List<SpawnRange> _spawnRangeList;

    public SpawnTemplate(final String makerName, final String eventName, final PeriodOfDay periodOfDay, final int count, final long respawn, final long respawnRandom, final NextTime respawnCron) {
        _npcList = new ArrayList<>(1);
        _spawnRangeList = new ArrayList<>(1);
        _makerName = makerName;
        _eventName = eventName;
        _periodOfDay = periodOfDay;
        _count = count;
        _respawn = respawn;
        _respawnRandom = respawnRandom;
        _respawnCron = respawnCron;
    }

    public void addSpawnRange(final SpawnRange range) {
        _spawnRangeList.add(range);
    }

    public SpawnRange getSpawnRange(final int index) {
        return _spawnRangeList.get(index);
    }

    public void addNpc(final SpawnNpcInfo info) {
        _npcList.add(info);
    }

    public SpawnNpcInfo getNpcId(final int index) {
        return _npcList.get(index);
    }

    public String getMakerName() {
        return _makerName;
    }

    public int getNpcSize() {
        return _npcList.size();
    }

    public int getSpawnRangeSize() {
        return _spawnRangeList.size();
    }

    public int getCount() {
        return _count;
    }

    public long getRespawn() {
        return _respawn;
    }

    public long getRespawnRandom() {
        return _respawnRandom;
    }

    public NextTime getRespawnCron() {
        return _respawnCron;
    }

    public PeriodOfDay getPeriodOfDay() {
        return _periodOfDay;
    }

    public String getEventName() {
        return _eventName;
    }
}
