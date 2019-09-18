package ru.j2dev.gameserver.model.entity;

import gnu.trove.set.hash.TIntHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.listener.Listener;
import ru.j2dev.commons.listener.ListenerList;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.database.mysql;
import ru.j2dev.gameserver.geodata.GeoEngine;
import ru.j2dev.gameserver.idfactory.IdFactory;
import ru.j2dev.gameserver.listener.actor.door.impl.MasterOnOpenCloseListenerImpl;
import ru.j2dev.gameserver.listener.reflection.OnReflectionCollapseListener;
import ru.j2dev.gameserver.listener.zone.impl.NoLandingZoneListener;
import ru.j2dev.gameserver.listener.zone.impl.ResidenceEnterLeaveListenerImpl;
import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.model.instances.DoorInstance;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.templates.DoorTemplate;
import ru.j2dev.gameserver.templates.InstantZone;
import ru.j2dev.gameserver.templates.InstantZone.DoorInfo;
import ru.j2dev.gameserver.templates.InstantZone.SpawnInfo;
import ru.j2dev.gameserver.templates.InstantZone.ZoneInfo;
import ru.j2dev.gameserver.templates.ZoneTemplate;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.NpcUtils;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class Reflection {
    private static final Logger LOGGER = LoggerFactory.getLogger(Reflection.class);
    private static final AtomicInteger _nextId = new AtomicInteger();

    protected final Lock lock = new ReentrantLock();
    protected final List<Spawner> _spawns = new ArrayList<>();
    protected final List<GameObject> _objects = new ArrayList<>();
    protected final TIntHashSet _visitors = new TIntHashSet();
    private final int _id;
    private final ReflectionListenerList listeners = new ReflectionListenerList();
    protected Map<Integer, DoorInstance> _doors = Collections.emptyMap();
    protected Map<String, Zone> _zones = Collections.emptyMap();
    protected Map<String, List<Spawner>> _spawners = Collections.emptyMap();
    protected int _playerCount;
    protected Party _party;
    protected CommandChannel _commandChannel;
    private String _name = "";
    private InstantZone _instance;
    private int _geoIndex;
    private Location _resetLoc;
    private Location _returnLoc;
    private Location _teleportLoc;
    private int _collapseIfEmptyTime;
    private boolean _isCollapseStarted;
    private Future<?> _collapseTask;
    private Future<?> _collapse1minTask;
    private Future<?> _hiddencollapseTask;
    private boolean _event;

    public Reflection() {
        this(_nextId.incrementAndGet());
    }

    private Reflection(final int id) {
        _id = id;
    }

    public Reflection(final int id, final boolean event) {
        _id = id;
        _event = event;
    }

    public static Reflection createReflection(final int id) {
        if (id > 0) {
            throw new IllegalArgumentException("id should be <= 0");
        }
        return new Reflection(id);
    }

    public int getId() {
        return _id;
    }

    public int getInstancedZoneId() {
        return (_instance == null) ? 0 : _instance.getId();
    }

    public Party getParty() {
        return _party;
    }

    public void setParty(final Party party) {
        _party = party;
    }

    public void setCommandChannel(final CommandChannel commandChannel) {
        _commandChannel = commandChannel;
    }

    public void setCollapseIfEmptyTime(final int value) {
        _collapseIfEmptyTime = value;
    }

    public String getName() {
        return _name;
    }

    protected void setName(final String name) {
        _name = name;
    }

    public InstantZone getInstancedZone() {
        return _instance;
    }

    protected void setInstancedZone(final InstantZone iz) {
        _instance = iz;
    }

    public int getGeoIndex() {
        return _geoIndex;
    }

    protected void setGeoIndex(final int geoIndex) {
        _geoIndex = geoIndex;
    }

    public Location getCoreLoc() {
        return _resetLoc;
    }

    public void setCoreLoc(final Location l) {
        _resetLoc = l;
    }

    public Location getReturnLoc() {
        return _returnLoc;
    }

    public void setReturnLoc(final Location l) {
        _returnLoc = l;
    }

    public Location getTeleportLoc() {
        return _teleportLoc;
    }

    public void setTeleportLoc(final Location l) {
        _teleportLoc = l;
    }

    public List<Spawner> getSpawns() {
        return _spawns;
    }

    public Collection<DoorInstance> getDoors() {
        return _doors.values();
    }

    public DoorInstance getDoor(final int id) {
        return _doors.get(id);
    }

    public Zone getZone(final String name) {
        return _zones.get(name);
    }

    public void startCollapseTimer(final long timeInMillis) {
        if ((isDefault() || isStatic()) && !_event) {
            new Exception("Basic reflection " + _id + " could not be collapsed!").printStackTrace();
            return;
        }
        lock.lock();
        try {
            if (_collapseTask != null) {
                _collapseTask.cancel(false);
                _collapseTask = null;
            }
            if (_collapse1minTask != null) {
                _collapse1minTask.cancel(false);
                _collapse1minTask = null;
            }
            _collapseTask = ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
                @Override
                public void runImpl() {
                    collapse();
                }
            }, timeInMillis);
            if (timeInMillis >= 60000L) {
                _collapse1minTask = ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
                    @Override
                    public void runImpl() {
                        minuteBeforeCollapse();
                    }
                }, timeInMillis - 60000L);
            }
        } finally {
            lock.unlock();
        }
    }

    public void stopCollapseTimer() {
        lock.lock();
        try {
            if (_collapseTask != null) {
                _collapseTask.cancel(false);
                _collapseTask = null;
            }
            if (_collapse1minTask != null) {
                _collapse1minTask.cancel(false);
                _collapse1minTask = null;
            }
        } finally {
            lock.unlock();
        }
    }

    public void minuteBeforeCollapse() {
        if (_isCollapseStarted) {
            return;
        }
        lock.lock();
        try {
            _objects.stream().filter(GameObject::isPlayer).map(GameObject::getPlayer).forEach(player -> player.sendMessage(new CustomMessage("THIS_INSTANCE_ZONE_WILL_BE_TERMINATED_IN_S1_MINUTES_YOU_WILL_BE_FORCED_OUT_OF_THE_DANGEON_THEN_TIME_EXPIRES", player).addNumber(1L)));
        } finally {
            lock.unlock();
        }
    }

    public void collapse() {
        if (_id <= 0 && !_event) {
            new Exception("Basic or not event reflection " + _id + " could not be collapsed!").printStackTrace();
            return;
        }
        lock.lock();
        try {
            if (_isCollapseStarted) {
                return;
            }
            _isCollapseStarted = true;
        } finally {
            lock.unlock();
        }
        listeners.onCollapse();
        try {
            stopCollapseTimer();
            if (_hiddencollapseTask != null) {
                _hiddencollapseTask.cancel(false);
                _hiddencollapseTask = null;
            }
            _spawns.forEach(Spawner::deleteAll);
            _spawners.keySet().forEach(this::despawnByGroup);
            _doors.values().forEach(GameObject::deleteMe);
            _doors.clear();
            _zones.values().forEach(zone -> zone.setActive(false));
            _zones.clear();
            final List<Player> teleport = new ArrayList<>();
            final List<GameObject> delete = new ArrayList<>();
            lock.lock();
            try {
                _objects.forEach(o -> {
                    if (o.isPlayer()) {
                        teleport.add((Player) o);
                    } else {
                        if (o.isPlayable()) {
                            return;
                        }
                        delete.add(o);
                    }
                });
            } finally {
                lock.unlock();
            }
            teleport.forEach(player -> {
                if (player.getParty() != null) {
                    if (equals(player.getParty().getReflection())) {
                        player.getParty().setReflection(null);
                    }
                    if (player.getParty().getCommandChannel() != null && equals(player.getParty().getCommandChannel().getReflection())) {
                        player.getParty().getCommandChannel().setReflection(null);
                    }
                }
                if (equals(player.getReflection())) {
                    if (getReturnLoc() != null) {
                        player.teleToLocation(getReturnLoc(), ReflectionManager.DEFAULT);
                    } else {
                        player.setReflection(ReflectionManager.DEFAULT);
                    }
                }
            });
            if (_commandChannel != null) {
                _commandChannel.setReflection(null);
                _commandChannel = null;
            }
            if (_party != null) {
                _party.setReflection(null);
                _party = null;
            }
            delete.forEach(GameObject::deleteMe);
            _spawns.clear();
            _objects.clear();
            _visitors.clear();
            _doors.clear();
            _playerCount = 0;
            onCollapse();
        } finally {
            ReflectionManager.getInstance().remove(this);
            GeoEngine.FreeGeoIndex(getGeoIndex());
        }
    }

    protected void onCollapse() {
    }

    public void addObject(final GameObject o) {
        if (_isCollapseStarted) {
            return;
        }
        lock.lock();
        try {
            _objects.add(o);
            if (o.isPlayer()) {
                ++_playerCount;
                _visitors.add(o.getObjectId());
                onPlayerEnter(o.getPlayer());
            }
        } finally {
            lock.unlock();
        }
        if (_collapseIfEmptyTime > 0 && _hiddencollapseTask != null) {
            _hiddencollapseTask.cancel(false);
            _hiddencollapseTask = null;
        }
    }

    public void removeObject(final GameObject o) {
        if (_isCollapseStarted) {
            return;
        }
        lock.lock();
        try {
            if (!_objects.remove(o)) {
                return;
            }
            if (o.isPlayer()) {
                --_playerCount;
                onPlayerExit(o.getPlayer());
            }
        } finally {
            lock.unlock();
        }
        if (_playerCount <= 0 && !isDefault() && !isStatic() && _hiddencollapseTask == null) {
            if (_collapseIfEmptyTime <= 0) {
                collapse();
            } else {
                _hiddencollapseTask = ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
                    @Override
                    public void runImpl() {
                        collapse();
                    }
                }, _collapseIfEmptyTime * 60 * 1000L);
            }
        }
    }

    public void onPlayerEnter(final Player player) {
        player.getInventory().validateItems();
    }

    public void onPlayerExit(final Player player) {
        player.getInventory().validateItems();
    }

    public List<Player> getPlayers() {
        final List<Player> result;
        lock.lock();
        try {
            result = _objects.stream().filter(GameObject::isPlayer).map(o -> (Player) o).collect(Collectors.toList());
        } finally {
            lock.unlock();
        }
        return result;
    }

    public List<NpcInstance> getNpcs() {
        final List<NpcInstance> result;
        lock.lock();
        try {
            result = _objects.stream().filter(GameObject::isNpc).map(o -> (NpcInstance) o).collect(Collectors.toList());
        } finally {
            lock.unlock();
        }
        return result;
    }

    public List<NpcInstance> getAllByNpcId(final int npcId, final boolean onlyAlive) {
        final List<NpcInstance> result;
        lock.lock();
        try {
            result = _objects.stream().filter(GameObject::isNpc).map(o -> (NpcInstance) o).filter(npc -> npcId == npc.getNpcId() && (!onlyAlive || !npc.isDead())).collect(Collectors.toList());
        } finally {
            lock.unlock();
        }
        return result;
    }

    public boolean canChampions() {
        return _id <= 0;
    }

    public boolean isAutolootForced() {
        return false;
    }

    public boolean isCollapseStarted() {
        return _isCollapseStarted;
    }

    public void addSpawn(final SimpleSpawner spawn) {
        if (spawn != null) {
            _spawns.add(spawn);
        }
    }

    public void fillSpawns(final List<SpawnInfo> si) {
        if (si == null) {
            return;
        }
        for (final SpawnInfo s : si) {
            switch (s.getSpawnType()) {
                case 0: {
                    s.getCoords().forEach(loc -> {
                        final SimpleSpawner c = new SimpleSpawner(s.getNpcId());
                        c.setReflection(this);
                        c.setRespawnDelay(s.getRespawnDelay(), s.getRespawnRnd());
                        c.setAmount(s.getCount());
                        c.setLoc(loc);
                        c.doSpawn(true);
                        if (s.getRespawnDelay() == 0) {
                            c.stopRespawn();
                        } else {
                            c.startRespawn();
                        }
                        addSpawn(c);
                    });
                    break;
                }
                case 1: {
                    final SimpleSpawner c = new SimpleSpawner(s.getNpcId());
                    c.setReflection(this);
                    c.setRespawnDelay(s.getRespawnDelay(), s.getRespawnRnd());
                    c.setAmount(1);
                    c.setLoc(s.getCoords().get(Rnd.get(s.getCoords().size())));
                    c.doSpawn(true);
                    if (s.getRespawnDelay() == 0) {
                        c.stopRespawn();
                    } else {
                        c.startRespawn();
                    }
                    addSpawn(c);
                    break;
                }
                case 2: {
                    final SimpleSpawner c = new SimpleSpawner(s.getNpcId());
                    c.setReflection(this);
                    c.setRespawnDelay(s.getRespawnDelay(), s.getRespawnRnd());
                    c.setAmount(s.getCount());
                    c.setTerritory(s.getLoc());
                    for (int j = 0; j < s.getCount(); ++j) {
                        c.doSpawn(true);
                    }
                    if (s.getRespawnDelay() == 0) {
                        c.stopRespawn();
                    } else {
                        c.startRespawn();
                    }
                    addSpawn(c);
                }
            }
        }
    }

    public void init(final Map<Integer, DoorTemplate> doors, final Map<String, ZoneTemplate> zones) {
        if (!doors.isEmpty()) {
            _doors = new HashMap<>(doors.size());
        }
        doors.values().forEach(template -> {
            final DoorInstance door = new DoorInstance(IdFactory.getInstance().getNextId(), template);
            door.setReflection(this);
            door.setIsInvul(true);
            door.spawnMe(template.getLoc());
            if (template.isOpened()) {
                door.openMe();
            }
            _doors.put(template.getNpcId(), door);
        });
        initDoors();
        if (!zones.isEmpty()) {
            _zones = new HashMap<>(zones.size());
        }
        zones.values().forEach(template -> {
            final Zone zone = new Zone(template);
            zoneAddListeners(zone);
            if (template.isEnabled()) {
                zone.setActive(true);
            }
            _zones.put(template.getName(), zone);
        });
    }


    private void zoneAddListeners(final Zone zone) {
        zone.setReflection(this);
        switch (zone.getType()) {
            case no_landing:
            case SIEGE: {
                zone.addListener(NoLandingZoneListener.STATIC);
                break;
            }
            case RESIDENCE: {
                zone.addListener(ResidenceEnterLeaveListenerImpl.STATIC);
                break;
            }
        }
    }

    private void init0(final Map<Integer, DoorInfo> doors, final Map<String, ZoneInfo> zones) {
        if (!doors.isEmpty()) {
            _doors = new HashMap<>(doors.size());
        }
        doors.values().forEach(info -> {
            final DoorInstance door = new DoorInstance(IdFactory.getInstance().getNextId(), info.getTemplate());
            door.setReflection(this);
            door.setIsInvul(info.isInvul());
            door.spawnMe(info.getTemplate().getLoc());
            if (info.isOpened()) {
                door.openMe();
            }
            _doors.put(info.getTemplate().getNpcId(), door);
        });
        initDoors();
        if (!zones.isEmpty()) {
            _zones = new HashMap<>(zones.size());
        }
        zones.values().forEach(t -> {
            final Zone zone = new Zone(t.getTemplate());
            zoneAddListeners(zone);
            if (t.isActive()) {
                zone.setActive(true);
            }
            _zones.put(t.getTemplate().getName(), zone);
        });
    }

    private void initDoors() {
        _doors.values().stream().filter(door -> door.getTemplate().getMasterDoor() > 0).forEach(door -> {
            final DoorInstance masterDoor = getDoor(door.getTemplate().getMasterDoor());
            masterDoor.addListener(new MasterOnOpenCloseListenerImpl(door));
        });
    }

    public void openDoor(final int doorId) {
        final DoorInstance door = _doors.get(doorId);
        if (door != null) {
            door.openMe();
        }
    }

    public void closeDoor(final int doorId) {
        final DoorInstance door = _doors.get(doorId);
        if (door != null) {
            door.closeMe();
        }
    }

    public void clearReflection(final int timeInMinutes, final boolean message) {
        if (isDefault() || isStatic()) {
            return;
        }
        getNpcs().forEach(GameObject::deleteMe);
        startCollapseTimer(timeInMinutes * 60 * 1000L);
        if (message) {
            getPlayers().stream().filter(Objects::nonNull).forEach(pl -> {
                for (final Player partyPlayer : pl) {
                    partyPlayer.sendMessage(new CustomMessage("THIS_DUNGEON_WILL_EXPIRE_IN_S1_MINUTES", partyPlayer).addNumber(timeInMinutes));
                }
            });
        }
    }

    public NpcInstance addSpawnWithoutRespawn(final int npcId, final Location loc, final int randomOffset) {
        Location newLoc;
        if (randomOffset > 0) {
            newLoc = Location.findPointToStay(loc, 0, randomOffset, getGeoIndex()).setH(loc.h);
        } else {
            newLoc = loc;
        }
        return NpcUtils.spawnSingle(npcId, newLoc, this);
    }

    public NpcInstance addSpawnWithRespawn(final int npcId, final Location loc, final int randomOffset, final int respawnDelay) {
        final SimpleSpawner sp = new SimpleSpawner(NpcTemplateHolder.getInstance().getTemplate(npcId));
        sp.setLoc((randomOffset > 0) ? Location.findPointToStay(loc, 0, randomOffset, getGeoIndex()) : loc);
        sp.setReflection(this);
        sp.setAmount(1);
        sp.setRespawnDelay(respawnDelay);
        sp.doSpawn(true);
        sp.startRespawn();
        return sp.getLastSpawn();
    }

    public boolean isDefault() {
        return getId() <= 0;
    }

    public boolean isStatic() {
        return false;
    }

    public int[] getVisitors() {
        return _visitors.toArray();
    }

    public void setReenterTime(final long time) {
        int[] players;
        lock.lock();
        try {
            players = _visitors.toArray();
        } finally {
            lock.unlock();
        }
        if (players != null) {
            Arrays.stream(players).forEach(objectId -> {
                try {
                    final Player player = World.getPlayer(objectId);
                    if (player != null) {
                        player.setInstanceReuse(getInstancedZoneId(), time);
                    } else {
                        mysql.set("REPLACE INTO character_instances (obj_id, id, reuse) VALUES (?,?,?)", objectId, getInstancedZoneId(), time);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    protected void onCreate() {
        ReflectionManager.getInstance().add(this);
    }

    public void init(final InstantZone instantZone) {
        setName(instantZone.getName());
        setInstancedZone(instantZone);
        if (instantZone.getMapX() >= 0) {
            final int geoIndex = GeoEngine.NextGeoIndex(instantZone.getMapX(), instantZone.getMapY(), getId());
            setGeoIndex(geoIndex);
        }
        setTeleportLoc(instantZone.getTeleportCoord());
        if (instantZone.getReturnCoords() != null) {
            setReturnLoc(instantZone.getReturnCoords());
        }
        fillSpawns(instantZone.getSpawnsInfo());
        if (instantZone.getSpawns().size() > 0) {
            _spawners = new HashMap<>(instantZone.getSpawns().size());
            instantZone.getSpawns().forEach((key, value) -> {
                final List<Spawner> spawnList = new ArrayList<>(value.getTemplates().size());
                _spawners.put(key, spawnList);
                value.getTemplates().forEach(template -> {
                    final HardSpawner spawner = new HardSpawner(template);
                    spawnList.add(spawner);
                    spawner.setAmount(template.getCount());
                    spawner.setRespawnDelay(template.getRespawn(), template.getRespawnRandom());
                    spawner.setReflection(this);
                    spawner.setRespawnTime(0);
                });
                if (value.isSpawned()) {
                    spawnByGroup(key);
                }
            });
        }
        init0(instantZone.getDoors(), instantZone.getZones());
        if (!isStatic()) {
            setCollapseIfEmptyTime(instantZone.getCollapseIfEmpty());
            startCollapseTimer(instantZone.getTimelimit() * 60 * 1000L);
        }
        onCreate();
    }

    public void spawnByGroup(final String name) {
        final List<Spawner> list = _spawners.get(name);
        if (list == null) {
            throw new IllegalArgumentException();
        }
        list.forEach(Spawner::init);
    }

    public void despawnByGroup(final String name) {
        final List<Spawner> list = _spawners.get(name);
        if (list == null) {
            throw new IllegalArgumentException();
        }
        list.forEach(Spawner::deleteAll);
    }

    public Collection<Zone> getZones() {
        return _zones.values();
    }

    public <T extends Listener<Reflection>> boolean addListener(final T listener) {
        return listeners.add(listener);
    }

    public <T extends Listener<Reflection>> boolean removeListener(final T listener) {
        return listeners.remove(listener);
    }

    public class ReflectionListenerList extends ListenerList<Reflection> {
        void onCollapse() {
            if (!getListeners().isEmpty()) {
                getListeners().forEach(listener -> ((OnReflectionCollapseListener) listener).onReflectionCollapse(Reflection.this));
            }
        }
    }
}
