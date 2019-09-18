package ru.j2dev.gameserver.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.collections.MultiValueSet;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.templates.StatsSet;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.templates.spawn.SpawnRange;
import ru.j2dev.gameserver.utils.Location;

import java.util.ArrayList;

@Deprecated
public class SimpleSpawner extends Spawner {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleSpawner.class);

    private NpcTemplate _npcTemplate;
    private int _locx;
    private int _locy;
    private int _locz;
    private int _heading;
    private Territory _territory;

    public SimpleSpawner(final NpcTemplate mobTemplate) {
        if (mobTemplate == null) {
            throw new NullPointerException();
        }
        _npcTemplate = mobTemplate;
        _spawned = new ArrayList<>(1);
    }

    public SimpleSpawner(final int npcId) {
        final NpcTemplate mobTemplate = NpcTemplateHolder.getInstance().getTemplate(npcId);
        if (mobTemplate == null) {
            throw new NullPointerException("Not find npc: " + npcId);
        }
        _npcTemplate = mobTemplate;
        _spawned = new ArrayList<>(1);
    }

    public int getAmount() {
        return _maximumCount;
    }

    public int getSpawnedCount() {
        return _currentCount.get();
    }

    public int getSheduledCount() {
        return _scheduledCount.get();
    }

    public Territory getTerritory() {
        return _territory;
    }

    public void setTerritory(final Territory territory) {
        _territory = territory;
    }

    public Location getLoc() {
        return new Location(_locx, _locy, _locz);
    }

    public void setLoc(final Location loc) {
        _locx = loc.x;
        _locy = loc.y;
        _locz = loc.z;
        _heading = loc.h;
    }

    public int getLocx() {
        return _locx;
    }

    public void setLocx(final int locx) {
        _locx = locx;
    }

    public int getLocy() {
        return _locy;
    }

    public void setLocy(final int locy) {
        _locy = locy;
    }

    public int getLocz() {
        return _locz;
    }

    public void setLocz(final int locz) {
        _locz = locz;
    }

    @Override
    public int getCurrentNpcId() {
        return _npcTemplate.getNpcId();
    }

    @Override
    public SpawnRange getCurrentSpawnRange() {
        if (_locx == 0 && _locz == 0) {
            return _territory;
        }
        return getLoc();
    }

    public int getHeading() {
        return _heading;
    }

    public void setHeading(final int heading) {
        _heading = heading;
    }

    public void restoreAmount() {
        _maximumCount = _referenceCount;
    }

    @Override
    public void decreaseCount(final NpcInstance oldNpc) {
        decreaseCount0(_npcTemplate, oldNpc, oldNpc.getDeadTime());
    }

    @Override
    public NpcInstance doSpawn(final boolean spawn) {
        return doSpawn0(_npcTemplate, spawn, StatsSet.EMPTY);
    }

    @Override
    protected NpcInstance initNpc(final NpcInstance mob, final boolean spawn, final MultiValueSet<String> set) {
        Location newLoc;
        if (_territory != null) {
            newLoc = _territory.getRandomLoc(_reflection.getGeoIndex());
            newLoc.setH(Rnd.get(65535));
        } else {
            newLoc = getLoc();
            newLoc.h = ((getHeading() == -1) ? Rnd.get(65535) : getHeading());
        }
        return initNpc0(mob, newLoc, spawn, set);
    }

    @Override
    public void respawnNpc(final NpcInstance oldNpc) {
        oldNpc.refreshID();
        initNpc(oldNpc, true, StatsSet.EMPTY);
    }

    @Override
    public SimpleSpawner clone() {
        final SimpleSpawner spawnDat = new SimpleSpawner(_npcTemplate);
        spawnDat.setTerritory(_territory);
        spawnDat.setLocx(_locx);
        spawnDat.setLocy(_locy);
        spawnDat.setLocz(_locz);
        spawnDat.setHeading(_heading);
        spawnDat.setAmount(_maximumCount);
        spawnDat.setRespawnDelay(_respawnDelay, _respawnDelayRandom);
        spawnDat.setRespawnCron(getRespawnCron());
        return spawnDat;
    }
}
