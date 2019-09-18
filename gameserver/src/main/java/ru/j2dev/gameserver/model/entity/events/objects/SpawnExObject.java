package ru.j2dev.gameserver.model.entity.events.objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.manager.SpawnManager;
import ru.j2dev.gameserver.model.Spawner;
import ru.j2dev.gameserver.model.entity.events.GlobalEvent;
import ru.j2dev.gameserver.model.instances.NpcInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpawnExObject implements SpawnableObject {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpawnExObject.class);

    private final List<Spawner> _spawns;
    private final String _name;
    private boolean _spawned;

    public SpawnExObject(final String name) {
        _name = name;
        _spawns = SpawnManager.getInstance().getSpawners(_name);
        if (_spawns.isEmpty() && Config.ALT_DEBUG_ENABLED) {
            LOGGER.info("SpawnExObject: not found spawn group: " + name);
        }
    }

    @Override
    public void spawnObject(final GlobalEvent event) {
        if (_spawned) {
            LOGGER.info("SpawnExObject: can't spawn twice: " + _name + "; event: " + event, new Exception());
        } else {
            _spawns.forEach(spawn -> {
                if (event.isInProgress()) {
                    spawn.addEvent(event);
                } else {
                    spawn.removeEvent(event);
                }
                spawn.setReflection(event.getReflection());
                spawn.init();
            });
            _spawned = true;
        }
    }

    @Override
    public void despawnObject(final GlobalEvent event) {
        if (!_spawned) {
            return;
        }
        _spawned = false;
        _spawns.forEach(spawn -> {
            spawn.removeEvent(event);
            spawn.deleteAll();
        });
    }

    @Override
    public void refreshObject(final GlobalEvent event) {
        getAllSpawned().forEach(npc -> {
            if (event.isInProgress()) {
                npc.addEvent(event);
            } else {
                npc.removeEvent(event);
            }
        });
    }

    public List<Spawner> getSpawns() {
        return _spawns;
    }

    public List<NpcInstance> getAllSpawned() {
        final List<NpcInstance> npcs = new ArrayList<>();
        _spawns.stream().map(Spawner::getAllSpawned).forEach(npcs::addAll);
        return npcs.isEmpty() ? Collections.emptyList() : npcs;
    }

    public NpcInstance getFirstSpawned() {
        final List<NpcInstance> npcs = getAllSpawned();
        return (npcs.size() > 0) ? npcs.get(0) : null;
    }

    public boolean isSpawned() {
        return _spawned;
    }
}
