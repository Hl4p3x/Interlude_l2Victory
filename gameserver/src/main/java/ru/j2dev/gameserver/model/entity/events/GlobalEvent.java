package ru.j2dev.gameserver.model.entity.events;

import ru.j2dev.commons.collections.MultiValueSet;
import ru.j2dev.commons.listener.Listener;
import ru.j2dev.commons.listener.ListenerList;
import ru.j2dev.commons.logging.LoggerObject;
import ru.j2dev.gameserver.dao.ItemsDAO;
import ru.j2dev.gameserver.listener.event.OnStartStopListener;
import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.model.base.RestartType;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.model.entity.events.objects.DoorObject;
import ru.j2dev.gameserver.model.entity.events.objects.InitableObject;
import ru.j2dev.gameserver.model.entity.events.objects.SpawnableObject;
import ru.j2dev.gameserver.model.entity.events.objects.ZoneObject;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.components.IStaticPacket;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.L2GameServerPacket;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.taskmanager.actionrunner.ActionRunner;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.TimeUtils;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class GlobalEvent extends LoggerObject {
    public static final String EVENT = "event";
    protected final Map<Integer, List<EventAction>> _onTimeActions;
    protected final List<EventAction> _onStartActions;
    protected final List<EventAction> _onStopActions;
    protected final List<EventAction> _onInitActions;
    protected final Map<String, List<Serializable>> _objects;
    protected final int _id;
    protected final String _name;
    protected final String _timerName;
    protected final ListenerListImpl _listenerList;
    protected Map<Integer, ItemInstance> _banishedItems;

    protected GlobalEvent(final MultiValueSet<String> set) {
        this(set.getInteger("id"), set.getString("name"));
    }

    protected GlobalEvent(final int id, final String name) {
        _onTimeActions = new TreeMap<>();
        _onStartActions = new ArrayList<>(0);
        _onStopActions = new ArrayList<>(0);
        _onInitActions = new ArrayList<>(0);
        _objects = new HashMap<>(0);
        _listenerList = new ListenerListImpl();
        _banishedItems = Collections.emptyMap();
        _id = id;
        _name = name;
        _timerName = id + "_" + name.toLowerCase().replace(" ", "_");
    }

    public void initEvent() {
        callActions(_onInitActions);
        reCalcNextTime(true);
        printInfo();
    }

    public void startEvent() {
        callActions(_onStartActions);
        _listenerList.onStart();
    }

    public void stopEvent() {
        callActions(_onStopActions);
        _listenerList.onStop();
    }

    protected void printInfo() {
        info(getName() + " time - " + TimeUtils.toSimpleFormat(startTimeMillis()));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + getId() + ";" + getName() + "]";
    }

    protected void callActions(final List<EventAction> actions) {
        for (final EventAction action : actions) {
            action.call(this);
        }
    }

    public void addOnStartActions(final List<EventAction> start) {
        _onStartActions.addAll(start);
    }

    public void addOnStopActions(final List<EventAction> start) {
        _onStopActions.addAll(start);
    }

    public void addOnInitActions(final List<EventAction> start) {
        _onInitActions.addAll(start);
    }

    public void addOnTimeAction(final int time, final EventAction action) {
        final List<EventAction> list = _onTimeActions.get(time);
        if (list != null) {
            list.add(action);
        } else {
            final List<EventAction> actions = new ArrayList<>(1);
            actions.add(action);
            _onTimeActions.put(time, actions);
        }
    }

    public void addOnTimeActions(final int time, final List<EventAction> actions) {
        if (actions.isEmpty()) {
            return;
        }
        final List<EventAction> list = _onTimeActions.get(time);
        if (list != null) {
            list.addAll(actions);
        } else {
            _onTimeActions.put(time, new ArrayList<>(actions));
        }
    }

    public void timeActions(final int time) {
        final List<EventAction> actions = _onTimeActions.get(time);
        if (actions == null) {
            info("Undefined time : " + time + " for " + toString());
            return;
        }
        callActions(actions);
    }

    public Integer[] timeActions() {
        return _onTimeActions.keySet().toArray(new Integer[0]);
    }

    public void registerActions() {
        final long t = startTimeMillis();
        if (t == 0L) {
            return;
        }
        _onTimeActions.keySet().forEach(key -> ActionRunner.getInstance().register(t + key * 1000L, new EventWrapper(_timerName, this, key)));
    }

    public void clearActions() {
        ActionRunner.getInstance().clear(_timerName);
    }

    @SuppressWarnings("unchecked")
    public <O extends Serializable> List<O> getObjects(final String name) {
        final List<Serializable> objects = _objects.get(name);
        return (List<O>) ((objects == null) ? Collections.emptyList() : objects);
    }

    public <O extends Serializable> O getFirstObject(final String name) {
        final List<O> objects = getObjects(name);
        return (objects.size() > 0) ? objects.get(0) : null;
    }

    public void addObject(final String name, final Serializable object) {
        if (object == null) {
            return;
        }
        List<Serializable> list = _objects.get(name);
        if (list != null) {
            list.add(object);
        } else {
            list = new CopyOnWriteArrayList<>();
            list.add(object);
            _objects.put(name, list);
        }
    }

    public void removeObject(final String name, final Serializable o) {
        if (o == null) {
            return;
        }
        final List<Serializable> list = _objects.get(name);
        if (list != null) {
            list.remove(o);
        }
    }

    @SuppressWarnings("unchecked")
    public <O extends Serializable> List<O> removeObjects(final String name) {
        final List<Serializable> objects = _objects.remove(name);
        return (List<O>) ((objects == null) ? Collections.emptyList() : objects);
    }

    @SuppressWarnings("unchecked")
    public void addObjects(final String name, final List<? extends Serializable> objects) {
        if (objects.isEmpty()) {
            return;
        }
        final List<Serializable> list = _objects.get(name);
        if (list != null) {
            list.addAll(objects);
        } else {
            _objects.put(name, (List<Serializable>) objects);
        }
    }

    public Map<String, List<Serializable>> getObjects() {
        return _objects;
    }

    public void spawnAction(final String name, final boolean spawn) {
        final List<Serializable> objects = getObjects(name);
        if (objects.isEmpty()) {
            info("Undefined objects: " + name);
            return;
        }
        for (final Serializable object : objects) {
            if (object instanceof SpawnableObject) {
                if (spawn) {
                    ((SpawnableObject) object).spawnObject(this);
                } else {
                    ((SpawnableObject) object).despawnObject(this);
                }
            }
        }
    }

    public void doorAction(final String name, final boolean open) {
        final List<Serializable> objects = getObjects(name);
        if (objects.isEmpty()) {
            info("Undefined objects: " + name);
            return;
        }
        for (final Serializable object : objects) {
            if (object instanceof DoorObject) {
                if (open) {
                    ((DoorObject) object).open(this);
                } else {
                    ((DoorObject) object).close(this);
                }
            }
        }
    }

    public void zoneAction(final String name, final boolean active) {
        final List<Serializable> objects = getObjects(name);
        if (objects.isEmpty()) {
            info("Undefined objects: " + name);
            return;
        }
        for (final Serializable object : objects) {
            if (object instanceof ZoneObject) {
                ((ZoneObject) object).setActive(active, this);
            }
            if (object instanceof String) {
                zoneAction((String) object, active);
            }
        }
    }

    public void initAction(final String name) {
        final List<Serializable> objects = getObjects(name);
        if (objects.isEmpty()) {
            info("Undefined objects: " + name);
            return;
        }
        for (final Serializable object : objects) {
            if (object instanceof InitableObject) {
                ((InitableObject) object).initObject(this);
            }
        }
    }

    public void action(final String name, final boolean start) {
        if ("event".equalsIgnoreCase(name)) {
            if (start) {
                startEvent();
            } else {
                stopEvent();
            }
        }
    }

    public void refreshAction(final String name) {
        final List<Serializable> objects = getObjects(name);
        if (objects.isEmpty()) {
            info("Undefined objects: " + name);
            return;
        }
        for (final Serializable object : objects) {
            if (object instanceof SpawnableObject) {
                ((SpawnableObject) object).refreshObject(this);
            }
        }
    }

    public abstract void reCalcNextTime(final boolean p0);

    protected abstract long startTimeMillis();

    public void broadcastToWorld(final IStaticPacket packet) {
        GameObjectsStorage.getPlayers().stream().filter(Objects::nonNull).forEach(player -> player.sendPacket(packet));
    }

    public void broadcastToWorld(final L2GameServerPacket packet) {
        GameObjectsStorage.getPlayers().stream().filter(Objects::nonNull).forEach(player -> player.sendPacket(packet));
    }

    public int getId() {
        return _id;
    }

    public String getName() {
        return _name;
    }

    public GameObject getCenterObject() {
        return null;
    }

    public Reflection getReflection() {
        return ReflectionManager.DEFAULT;
    }

    public int getRelation(final Player thisPlayer, final Player target, final int oldRelation) {
        return oldRelation;
    }

    public int getUserRelation(final Player thisPlayer, final int oldRelation) {
        return oldRelation;
    }

    public void checkRestartLocs(final Player player, final Map<RestartType, Boolean> r) {
    }

    public Location getRestartLoc(final Player player, final RestartType type) {
        return null;
    }

    public boolean canAttack(final Creature target, final Creature attacker, final Skill skill, final boolean force) {
        return false;
    }

    public SystemMsg checkForAttack(final Creature target, final Creature attacker, final Skill skill, final boolean force) {
        return null;
    }

    public boolean isInProgress() {
        return false;
    }

    public boolean isParticle(final Player player) {
        return false;
    }

    public void announce(final int a) {
        throw new UnsupportedOperationException();
    }

    public void teleportPlayers(final String teleportWho) {
        throw new UnsupportedOperationException();
    }

    public boolean ifVar(final String name) {
        throw new UnsupportedOperationException();
    }

    public List<Player> itemObtainPlayers() {
        throw new UnsupportedOperationException();
    }

    public void giveItem(final Player player, final int itemId, final long count) {
        Functions.addItem(player, itemId, count);
    }

    public List<Player> broadcastPlayers(final int range) {
        throw new UnsupportedOperationException();
    }

    public boolean canResurrect(final Player resurrectPlayer, final Creature creature, final boolean force) {
        return true;
    }

    public void onAddEvent(final GameObject o) {
    }

    public void onRemoveEvent(final GameObject o) {
    }

    public void addBanishItem(final ItemInstance item) {
        if (_banishedItems.isEmpty()) {
            _banishedItems = new ConcurrentHashMap<>();
        }
        _banishedItems.put(item.getObjectId(), item);
    }

    public void removeBanishItems() {
        _banishedItems.forEach((key, value) -> {
            ItemInstance item = ItemsDAO.getInstance().load(key);
            if (item != null) {
                if (item.getOwnerId() > 0) {
                    final GameObject object = GameObjectsStorage.findObject(item.getOwnerId());
                    if (object != null && object.isPlayable()) {
                        ((Playable) object).getInventory().destroyItem(item);
                        object.getPlayer().sendPacket(SystemMessage2.removeItems(item));
                    }
                }
                item.delete();
            } else {
                item = value;
            }
            item.deleteMe();
        });
    }

    public void addListener(final Listener<GlobalEvent> l) {
        _listenerList.add(l);
    }

    public void removeListener(final Listener<GlobalEvent> l) {
        _listenerList.remove(l);
    }

    public void cloneTo(final GlobalEvent e) {
        e._onInitActions.addAll(_onInitActions);
        e._onStartActions.addAll(_onStartActions);
        e._onStopActions.addAll(_onStopActions);
        _onTimeActions.forEach(e::addOnTimeActions);
    }

    private class ListenerListImpl extends ListenerList<GlobalEvent> {
        public void onStart() {
            getListeners().stream().filter(OnStartStopListener.class::isInstance).forEach(listener -> ((OnStartStopListener) listener).onStart(GlobalEvent.this));
        }

        public void onStop() {
            getListeners().stream().filter(OnStartStopListener.class::isInstance).forEach(listener -> ((OnStartStopListener) listener).onStop(GlobalEvent.this));
        }
    }
}
