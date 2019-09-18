package ru.j2dev.gameserver.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.ai.CtrlIntention;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.L2GameServerPacket;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public final class WorldRegion implements Iterable<GameObject> {
    public static final WorldRegion[] EMPTY_L2WORLDREGION_ARRAY = new WorldRegion[0];
    private static final Logger LOGGER = LoggerFactory.getLogger(WorldRegion.class);

    private final int tileX;
    private final int tileY;
    private final int tileZ;
    private final AtomicBoolean _isActive = new AtomicBoolean();
    private final Lock lock = new ReentrantLock();
    private volatile GameObject[] _objects = GameObject.EMPTY_GAME_OBJECT_ARRAY;
    private int _objectsCount;
    private List<Zone> _zones = new CopyOnWriteArrayList<>();
    private int _playersCount;
    private Future<?> _activateTask;

    WorldRegion(final int x, final int y, final int z) {
        tileX = x;
        tileY = y;
        tileZ = z;
    }

    int getX() {
        return tileX;
    }

    int getY() {
        return tileY;
    }

    int getZ() {
        return tileZ;
    }

    void addToPlayers(final GameObject object, final Creature dropper) {
        if (object == null) {
            return;
        }
        Player player = null;
        if (object.isPlayer()) {
            player = (Player) object;
        }
        final int oid = object.getObjectId();
        final int rid = object.getReflectionId();
        for (final GameObject obj : this) {
            if (obj.getObjectId() != oid) {
                if (obj.getReflectionId() != rid) {
                    continue;
                }
                if (player != null) {
                    player.sendPacket(player.addVisibleObject(obj, null));
                }
                if (!obj.isPlayer()) {
                    continue;
                }
                final Player p = (Player) obj;
                p.sendPacket(p.addVisibleObject(object, dropper));
            }
        }
    }

    void removeFromPlayers(final GameObject object) {
        if (object == null) {
            return;
        }
        Player player = null;
        if (object.isPlayer()) {
            player = (Player) object;
        }
        final int oid = object.getObjectId();
        final Reflection rid = object.getReflection();
        List<L2GameServerPacket> d = null;
        for (final GameObject obj : this) {
            if (obj.getObjectId() != oid) {
                if (obj.getReflection() != rid) {
                    continue;
                }
                if (player != null) {
                    player.sendPacket(player.removeVisibleObject(obj, null));
                }
                if (!obj.isPlayer()) {
                    continue;
                }
                final Player player2 = (Player) obj;
                player2.sendPacket(player2.removeVisibleObject(object, (d == null) ? (d = object.deletePacketList()) : d));
            }
        }
    }

    public void addObject(final GameObject obj) {
        if (obj == null) {
            return;
        }
        lock.lock();
        try {
            GameObject[] objects = _objects;
            final GameObject[] resizedObjects = new GameObject[_objectsCount + 1];
            System.arraycopy(objects, 0, resizedObjects, 0, _objectsCount);
            objects = resizedObjects;
            objects[_objectsCount++] = obj;
            _objects = resizedObjects;
            if (obj.isPlayer() && _playersCount++ == 0) {
                if (_activateTask != null) {
                    _activateTask.cancel(false);
                }
                _activateTask = ThreadPoolManager.getInstance().schedule(new ActivateTask(true), 1000L);
            }
        } finally {
            lock.unlock();
        }
    }

    public void removeObject(final GameObject obj) {
        if (obj == null) {
            return;
        }
        lock.lock();
        try {
            final GameObject[] objects = _objects;
            int index = IntStream.range(0, _objectsCount).filter(i -> objects[i] == obj).findFirst().orElse(-1);
            if (index == -1) {
                return;
            }
            --_objectsCount;
            final GameObject[] resizedObjects = new GameObject[_objectsCount];
            objects[index] = objects[_objectsCount];
            System.arraycopy(objects, 0, resizedObjects, 0, _objectsCount);
            _objects = resizedObjects;
            if (obj.isPlayer() && --_playersCount == 0) {
                if (_activateTask != null) {
                    _activateTask.cancel(false);
                }
                _activateTask = ThreadPoolManager.getInstance().schedule(new ActivateTask(false), 60000L);
            }
        } finally {
            lock.unlock();
        }
    }

    public int getObjectsSize() {
        return _objectsCount;
    }

    public int getPlayersCount() {
        return _playersCount;
    }

    public boolean isEmpty() {
        return _playersCount == 0;
    }

    public boolean isActive() {
        return _isActive.get();
    }

    void setActive(final boolean activate) {
        if (!_isActive.compareAndSet(!activate, activate)) {
            return;
        }
        for (final GameObject obj : this) {
            if (!obj.isNpc()) {
                continue;
            }
            final NpcInstance npc = (NpcInstance) obj;
            if (npc.getAI().isActive() == isActive()) {
                continue;
            }
            if (isActive()) {
                npc.getAI().startAITask();
                npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
                npc.startRandomAnimation();
            } else {
                if (npc.getAI().isGlobalAI() || npc.getTemplate().getNoSleepMode() > 0) {
                    continue;
                }
                npc.getAI().stopAITask();
                npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
                npc.stopRandomAnimation();
            }
        }
    }

    void addZone(final Zone zone) {
        _zones.add(zone);
    }

    void removeZone(final Zone zone) {
            _zones.remove(zone);
    }

    public Zone[] getZones() {
        // Без синхронизации и копирования, т.к. удаление/добавление зон происходит достаточно редко
        return _zones.toArray(Zone.EMPTY_L2ZONE_ARRAY);
    }

    @Override
    public String toString() {
        return "[" + tileX + ", " + tileY + ", " + tileZ + "]";
    }

    @Override
    public Iterator<GameObject> iterator() {
        return new InternalIterator(_objects);
    }

    public class ActivateTask extends RunnableImpl {
        private final boolean _isActivating;

        public ActivateTask(final boolean isActivating) {
            _isActivating = isActivating;
        }

        @Override
        public void runImpl() {
            if (_isActivating) {
                World.activate(WorldRegion.this);
            } else {
                World.deactivate(WorldRegion.this);
            }
        }
    }

    private class InternalIterator implements Iterator<GameObject> {
        final GameObject[] objects;
        int cursor;

        public InternalIterator(final GameObject[] objects) {
            cursor = 0;
            this.objects = objects;
        }

        @Override
        public boolean hasNext() {
            return cursor < objects.length && objects[cursor] != null;
        }

        @Override
        public GameObject next() {
            return objects[cursor++];
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
