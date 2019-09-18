package ru.j2dev.gameserver.templates.arenas;

import ru.j2dev.gameserver.data.xml.holder.DoorHolder;
import ru.j2dev.gameserver.data.xml.holder.ZoneHolder;
import ru.j2dev.gameserver.model.instances.DoorInstance;
import ru.j2dev.gameserver.templates.DoorTemplate;
import ru.j2dev.gameserver.templates.ZoneTemplate;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.ReflectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by JunkyFunky
 * on 28.10.2016.
 * group j2dev
 */
public class TvTArena {
    private final int _id;
    private final Map<Integer, List<Location>> _teleportLocations = new HashMap<>();
    private final Map<Integer, DoorTemplate> _doorsMap = new HashMap<>();
    private Map<String, ZoneTemplate> _zone = new HashMap<>();

    public TvTArena(final int id) {
        _id = id;
    }

    public int getId() {
        return _id;
    }

    public void addTeleportLocation(final int team, final Location loc) {
        List<Location> locations = _teleportLocations.computeIfAbsent(team, k -> new ArrayList<>());
        locations.add(loc);
    }

    public List<Location> getTeleportLocations(final int team) {
        return _teleportLocations.get(team);
    }

    public Map<String, ZoneTemplate> getZones() {
        return _zone;
    }

    public void addZone(final String zone) {
        _zone.put(zone, ZoneHolder.getInstance().getTemplate(zone));
    }

    public void initDoors() {
        final List<Integer> doorsId = new ArrayList<>();
        _zone.keySet()
                .forEach(zone -> ReflectionUtils.getZone(zone).getInsideDoors().stream()
                .mapToInt(DoorInstance::getDoorId).forEach(doorsId::add));
        doorsId.forEach(doorId -> _doorsMap.put(doorId, DoorHolder.getInstance().getTemplate(doorId)));
    }

    public Map<Integer, DoorTemplate> getArenaDoors() {
        return _doorsMap;
    }
}
