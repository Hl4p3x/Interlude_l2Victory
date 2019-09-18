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
public class KoreanStyleArena {
    private final int _id;
    private final Map<Integer, Location> _teamLeftCorner = new HashMap<>();
    private final Map<Integer, Location> _teamRightCorner = new HashMap<>();
    private final Map<Integer, Location> _fightLocations = new HashMap<>();
    private final Map<Integer, DoorTemplate> _doorsMap = new HashMap<>();
    private Map<String, ZoneTemplate> _zone = new HashMap<>();

    public KoreanStyleArena(final int id) {
        _id = id;
    }

    public int getId() {
        return _id;
    }

    public void addTeamLeftCorner(final int team, final Location loc) {
        _teamLeftCorner.put(team, loc);
    }

    public Location getTeamLeftCorner(final int team) {
        return _teamLeftCorner.get(team);
    }

    public void addTeamRightCorner(final int team, final Location loc) {
        _teamRightCorner.put(team, loc);
    }

    public Location getTeamRightCorner(final int team) {
        return _teamRightCorner.get(team);
    }

    public void addFightLocation(final int team, final Location loc) {
        _fightLocations.put(team, loc);
    }

    public Location getFightLocation(final int team) {
        return _fightLocations.get(team);
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
