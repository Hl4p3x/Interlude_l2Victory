package ru.j2dev.gameserver.data.xml.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.commons.time.cron.SchedulingPattern;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.templates.InstantZone;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InstantZoneHolder extends AbstractHolder {

    private final Map<Integer, InstantZone> _zones = new HashMap<>();

    public static InstantZoneHolder getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void addInstantZone(final InstantZone zone) {
        _zones.put(zone.getId(), zone);
    }

    public InstantZone getInstantZone(final int id) {
        return _zones.get(id);
    }

    private SchedulingPattern getResetReuseById(final int id) {
        final InstantZone zone = getInstantZone(id);
        return (zone == null) ? null : zone.getResetReuse();
    }

    public int getMinutesToNextEntrance(final int id, final Player player) {
        final SchedulingPattern resetReuse = getResetReuseById(id);
        if (resetReuse == null) {
            return 0;
        }
        Long time = null;
        if (getSharedReuseInstanceIds(id) != null && !getSharedReuseInstanceIds(id).isEmpty()) {
            final List<Long> reuses = getSharedReuseInstanceIds(id).stream().mapToInt(i -> i).filter(i -> player.getInstanceReuse(i) != null).mapToObj(player::getInstanceReuse).collect(Collectors.toList());
            if (!reuses.isEmpty()) {
                Collections.sort(reuses);
                time = reuses.get(reuses.size() - 1);
            }
        } else {
            time = player.getInstanceReuse(id);
        }
        if (time == null) {
            return 0;
        }
        return (int) Math.max((resetReuse.next(time) - System.currentTimeMillis()) / 60000L, 0L);
    }

    public List<Integer> getSharedReuseInstanceIds(final int id) {
        if (getInstantZone(id).getSharedReuseGroup() < 1) {
            return null;
        }
        return _zones.values().stream().filter(iz -> iz.getSharedReuseGroup() > 0 && getInstantZone(id).getSharedReuseGroup() > 0 && iz.getSharedReuseGroup() == getInstantZone(id).getSharedReuseGroup()).map(InstantZone::getId).collect(Collectors.toList());
    }

    public List<Integer> getSharedReuseInstanceIdsByGroup(final int groupId) {
        if (groupId < 1) {
            return null;
        }
        return _zones.values().stream().filter(iz -> iz.getSharedReuseGroup() > 0 && iz.getSharedReuseGroup() == groupId).map(InstantZone::getId).collect(Collectors.toList());
    }

    @Override
    public int size() {
        return _zones.size();
    }

    @Override
    public void clear() {
        _zones.clear();
    }

    private static class LazyHolder {
        private static final InstantZoneHolder INSTANCE = new InstantZoneHolder();
    }
}
