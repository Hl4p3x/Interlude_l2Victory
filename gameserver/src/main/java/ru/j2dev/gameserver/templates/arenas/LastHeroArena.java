package ru.j2dev.gameserver.templates.arenas;

import ru.j2dev.gameserver.data.xml.holder.DoorHolder;
import ru.j2dev.gameserver.data.xml.holder.ZoneHolder;
import ru.j2dev.gameserver.model.instances.DoorInstance;
import ru.j2dev.gameserver.templates.DoorTemplate;
import ru.j2dev.gameserver.templates.ZoneTemplate;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.ReflectionUtils;

import java.util.*;

/**
 * Created by JunkyFunky
 * on 28.10.2016.
 * group j2dev
 */
public class LastHeroArena {
    private final int _id;
    private final Map<Integer, DoorTemplate> _doorsMap = new HashMap<>();
    private List<Location> _teleportLocations = Collections.emptyList();
    private Map<String, ZoneTemplate> _zone = new HashMap<>();

    public LastHeroArena(final int id) {
        _id = id;
    }

    public int getId() {
        return _id;
    }

    public void addTeleportLocation(final Location loc) {
        if (_teleportLocations == null) {
            _teleportLocations = new ArrayList<>();
            _teleportLocations.add(loc);
        }
    }

    public List<Location> getTeleportLocations() {
        return _teleportLocations;
    }

    public Map<String, ZoneTemplate> getZones() {
        return _zone;
    }

    public void addZone(final String zone) {
        _zone.put(zone, ZoneHolder.getInstance().getTemplate(zone));
    }

    public void initDoors() {
        final List<Integer> doorsId = new ArrayList<>();
        _zone.keySet().forEach(zone -> ReflectionUtils.getZone(zone).getInsideDoors().stream()
                .mapToInt(DoorInstance::getDoorId).forEach(doorsId::add));
        doorsId.forEach(doorId -> _doorsMap.put(doorId, DoorHolder.getInstance().getTemplate(doorId)));
    }

    public Map<Integer, DoorTemplate> getArenaDoors() {
        return _doorsMap;
    }
}
