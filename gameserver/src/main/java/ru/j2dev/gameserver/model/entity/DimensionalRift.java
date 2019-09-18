package ru.j2dev.gameserver.model.entity;

import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.data.xml.holder.InstantZoneHolder;
import ru.j2dev.gameserver.manager.DimensionalRiftManager;
import ru.j2dev.gameserver.manager.DimensionalRiftManager.DimensionalRiftRoom;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.templates.InstantZone;
import ru.j2dev.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DimensionalRift extends Reflection {
    protected static final long seconds_5 = 5000L;
    protected static final int MILLISECONDS_IN_MINUTE = 60000;
    protected final int _roomType;
    protected List<Integer> _completedRooms;
    protected int jumps_current;
    protected int _choosenRoom;
    protected boolean _hasJumped;
    protected boolean isBossRoom;
    private Future<?> teleporterTask;
    private Future<?> spawnTask;
    private Future<?> killRiftTask;

    public DimensionalRift(final Party party, final int type, final int room) {
        _completedRooms = new ArrayList<>();
        jumps_current = 0;
        _choosenRoom = -1;
        _hasJumped = false;
        isBossRoom = false;
        onCreate();
        startCollapseTimer(7200000L);
        setName("Dimensional Rift");
        if (this instanceof DelusionChamber) {
            final InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(type + 120);
            setInstancedZone(iz);
            setName(iz.getName());
        }
        _roomType = type;
        setParty(party);
        if (!(this instanceof DelusionChamber)) {
            party.setDimensionalRift(this);
        }
        party.setReflection(this);
        checkBossRoom(_choosenRoom = room);
        final Location coords = getRoomCoord(_choosenRoom);
        setReturnLoc(party.getPartyLeader().getLoc());
        setTeleportLoc(coords);
        party.getPartyMembers().forEach(p -> {
            p.setVar("backCoords", getReturnLoc().toXYZString(), -1L);
            DimensionalRiftManager.teleToLocation(p, Location.findPointToStay(coords, 50, 100, getGeoIndex()), this);
            p.setReflection(this);
        });
        createSpawnTimer(_choosenRoom);
        createTeleporterTimer();
    }

    public int getType() {
        return _roomType;
    }

    public int getCurrentRoom() {
        return _choosenRoom;
    }

    protected void createTeleporterTimer() {
        if (teleporterTask != null) {
            teleporterTask.cancel(false);
            teleporterTask = null;
        }
        teleporterTask = ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
            @Override
            public void runImpl() {
                if (jumps_current < getMaxJumps() && getPlayersInside(true) > 0) {
                    final DimensionalRift this$0 = DimensionalRift.this;
                    ++this$0.jumps_current;
                    teleportToNextRoom();
                    createTeleporterTimer();
                } else {
                    createNewKillRiftTimer();
                }
            }
        }, calcTimeToNextJump());
    }

    public void createSpawnTimer(final int room) {
        if (spawnTask != null) {
            spawnTask.cancel(false);
            spawnTask = null;
        }
        final DimensionalRiftRoom riftRoom = DimensionalRiftManager.getInstance().getRoom(_roomType, room);
        spawnTask = ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
            @Override
            public void runImpl() {
                for (final SimpleSpawner s : riftRoom.getSpawns()) {
                    final SimpleSpawner sp = s.clone();
                    sp.setReflection(DimensionalRift.this);
                    addSpawn(sp);
                    if (!isBossRoom) {
                        sp.startRespawn();
                    }
                    for (int i = 0; i < sp.getAmount(); ++i) {
                        sp.doSpawn(true);
                    }
                }
                addSpawnWithoutRespawn(getManagerId(), riftRoom.getTeleportCoords(), 0);
            }
        }, Config.RIFT_SPAWN_DELAY);
    }

    public synchronized void createNewKillRiftTimer() {
        if (killRiftTask != null) {
            killRiftTask.cancel(false);
            killRiftTask = null;
        }
        killRiftTask = ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
            @Override
            public void runImpl() {
                if (isCollapseStarted()) {
                    return;
                }
                getParty().getPartyMembers().stream().filter(p -> p != null && p.getReflection() == DimensionalRift.this).forEach(p -> DimensionalRiftManager.getInstance().teleportToWaitingRoom(p));
                collapse();
            }
        }, 100L);
    }

    public void partyMemberInvited() {
        createNewKillRiftTimer();
    }

    public void partyMemberExited(final Player player) {
        if (getParty().getMemberCount() < Config.RIFT_MIN_PARTY_SIZE || getParty().getMemberCount() == 1 || getPlayersInside(true) == 0) {
            createNewKillRiftTimer();
        }
    }

    public void manualTeleport(final Player player, final NpcInstance npc) {
        if (!player.isInParty() || !player.getParty().isInReflection() || !(player.getParty().getReflection() instanceof DimensionalRift)) {
            return;
        }
        if (!player.getParty().isLeader(player)) {
            DimensionalRiftManager.getInstance().showHtmlFile(player, "rift/NotPartyLeader.htm", npc);
            return;
        }
        if (isBossRoom) {
            manualExitRift(player, npc);
            return;
        }
        if (_hasJumped) {
            DimensionalRiftManager.getInstance().showHtmlFile(player, "rift/AlreadyTeleported.htm", npc);
            return;
        }
        _hasJumped = true;
        teleportToNextRoom();
    }

    public void manualExitRift(final Player player, final NpcInstance npc) {
        if (!player.isInParty() || !player.getParty().isInDimensionalRift()) {
            return;
        }
        if (!player.getParty().isLeader(player)) {
            DimensionalRiftManager.getInstance().showHtmlFile(player, "rift/NotPartyLeader.htm", npc);
            return;
        }
        createNewKillRiftTimer();
    }

    protected void teleportToNextRoom() {
        _completedRooms.add(_choosenRoom);
        getSpawns().forEach(Spawner::deleteAll);
        final int size = DimensionalRiftManager.getInstance().getRooms(_roomType).size();
        if (getType() >= 5 && jumps_current == getMaxJumps()) {
            _choosenRoom = 9;
        } else {
            final List<Integer> notCompletedRooms = IntStream.rangeClosed(1, size).filter(i -> !_completedRooms.contains(i)).boxed().collect(Collectors.toList());
            _choosenRoom = notCompletedRooms.get(Rnd.get(notCompletedRooms.size()));
        }
        checkBossRoom(_choosenRoom);
        setTeleportLoc(getRoomCoord(_choosenRoom));
        getParty().getPartyMembers().stream().filter(p -> p.getReflection() == this).forEach(p -> DimensionalRiftManager.teleToLocation(p, Location.findPointToStay(getRoomCoord(_choosenRoom), 50, 100, getGeoIndex()), this));
        createSpawnTimer(_choosenRoom);
    }

    @Override
    public void collapse() {
        if (isCollapseStarted()) {
            return;
        }
        Future<?> task = teleporterTask;
        if (task != null) {
            teleporterTask = null;
            task.cancel(false);
        }
        task = spawnTask;
        if (task != null) {
            spawnTask = null;
            task.cancel(false);
        }
        task = killRiftTask;
        if (task != null) {
            killRiftTask = null;
            task.cancel(false);
        }
        _completedRooms = null;
        final Party party = getParty();
        if (party != null) {
            party.setDimensionalRift(null);
        }
        super.collapse();
    }

    protected long calcTimeToNextJump() {
        if (isBossRoom) {
            return 3600000L;
        }
        return Config.RIFT_AUTO_JUMPS_TIME * 60000 + Rnd.get(Config.RIFT_AUTO_JUMPS_TIME_RAND);
    }

    public void memberDead(final Player player) {
        if (getPlayersInside(true) == 0) {
            createNewKillRiftTimer();
        }
    }

    public void usedTeleport(final Player player) {
        if (getPlayersInside(false) < Config.RIFT_MIN_PARTY_SIZE) {
            createNewKillRiftTimer();
        }
    }

    public void checkBossRoom(final int room) {
        isBossRoom = DimensionalRiftManager.getInstance().getRoom(_roomType, room).isBossRoom();
    }

    public Location getRoomCoord(final int room) {
        return DimensionalRiftManager.getInstance().getRoom(_roomType, room).getTeleportCoords();
    }

    public int getMaxJumps() {
        return Math.max(Math.min(Config.RIFT_MAX_JUMPS, 8), 1);
    }

    @Override
    public boolean canChampions() {
        return true;
    }

    @Override
    public String getName() {
        return "Dimensional Rift";
    }

    protected int getManagerId() {
        return 31865;
    }

    protected int getPlayersInside(final boolean alive) {
        if (_playerCount == 0) {
            return 0;
        }
        return (int) getPlayers().stream().filter(p -> !alive || !p.isDead()).count();
    }

    @Override
    public void removeObject(final GameObject o) {
        if (o.isPlayer() && _playerCount <= 1) {
            createNewKillRiftTimer();
        }
        super.removeObject(o);
    }
}
