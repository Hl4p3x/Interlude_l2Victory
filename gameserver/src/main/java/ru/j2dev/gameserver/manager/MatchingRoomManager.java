package ru.j2dev.gameserver.manager;

import org.apache.commons.lang3.ArrayUtils;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.matching.MatchingRoom;
import ru.j2dev.gameserver.templates.mapregion.RestartArea;
import ru.j2dev.gameserver.templates.mapregion.RestartPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

public class MatchingRoomManager {

    private final RoomsHolder[] _holder = new RoomsHolder[2];
    private final Set<Player> _players = new CopyOnWriteArraySet<>();

    public MatchingRoomManager() {
        _holder[MatchingRoom.PARTY_MATCHING] = new RoomsHolder();
        _holder[MatchingRoom.CC_MATCHING] = new RoomsHolder();
    }

    public static MatchingRoomManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void addToWaitingList(final Player player) {
        _players.add(player);
    }

    public void removeFromWaitingList(final Player player) {
        _players.remove(player);
    }

    public List<Player> getWaitingList(final int minLevel, final int maxLevel, final int[] classes) {
        return _players.stream().filter($member -> $member.getLevel() >= minLevel && $member.getLevel() <= maxLevel && (classes.length == 0 || ArrayUtils.contains(classes, $member.getClassId().getId()))).collect(Collectors.toList());
    }

    public List<MatchingRoom> getMatchingRooms(final int type, final int region, final boolean allLevels, final Player activeChar) {
        final List<MatchingRoom> res = new ArrayList<>();
        _holder[type]._rooms.values().stream().filter(room -> region <= 0 || room.getLocationId() == region).filter(room -> region != -2 || room.getLocationId() == getInstance().getLocation(activeChar)).forEach(room -> {
            if (!allLevels) {
                if (room.getMinLevel() > activeChar.getLevel()) {
                    return;
                }
                if (room.getMaxLevel() < activeChar.getLevel()) {
                    return;
                }
            }
            res.add(room);
        });
        return res;
    }

    public int addMatchingRoom(final MatchingRoom r) {
        return _holder[r.getType()].addRoom(r);
    }

    public void removeMatchingRoom(final MatchingRoom r) {
        _holder[r.getType()]._rooms.remove(r.getId());
    }

    public MatchingRoom getMatchingRoom(final int type, final int id) {
        return _holder[type]._rooms.get(id);
    }

    public int getLocation(final Player player) {
        if (player == null) {
            return 0;
        }
        final RestartArea ra = MapRegionManager.getInstance().getRegionData(RestartArea.class, player);
        if (ra != null) {
            final RestartPoint rp = ra.getRestartPoint().get(player.getRace());
            return rp.getBbs();
        }
        return 0;
    }

    private static class LazyHolder {
        private static final MatchingRoomManager INSTANCE = new MatchingRoomManager();
    }

    private class RoomsHolder {
        private final ConcurrentMap<Integer, MatchingRoom> _rooms = new ConcurrentHashMap<>();
        private int _id;

        private RoomsHolder() {
            _id = 1;
        }

        public int addRoom(final MatchingRoom r) {
            final int val = _id++;
            _rooms.put(val, r);
            return val;
        }
    }
}
