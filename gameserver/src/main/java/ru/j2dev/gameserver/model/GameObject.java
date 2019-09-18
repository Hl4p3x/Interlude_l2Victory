package ru.j2dev.gameserver.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.lang.reference.HardReferences;
import ru.j2dev.gameserver.geodata.GeoEngine;
import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.model.base.InvisibleType;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.model.entity.events.EventOwner;
import ru.j2dev.gameserver.model.entity.events.GlobalEvent;
import ru.j2dev.gameserver.network.lineage2.serverpackets.DeleteObject;
import ru.j2dev.gameserver.network.lineage2.serverpackets.L2GameServerPacket;
import ru.j2dev.gameserver.scripts.Events;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.Log;
import ru.j2dev.gameserver.utils.Util;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class GameObject extends EventOwner {
    public static final GameObject[] EMPTY_GAME_OBJECT_ARRAY = new GameObject[0];
    protected static final int CREATED = 0;
    protected static final int VISIBLE = 1;
    protected static final int DELETED = -1;
    private static final Logger LOGGER = LoggerFactory.getLogger(GameObject.class);

    private final AtomicInteger _state = new AtomicInteger(CREATED);
    protected int objectId;
    protected Reflection _reflection = ReflectionManager.DEFAULT;
    private int _x;
    private int _y;
    private int _z;
    private WorldRegion _currentRegion;

    public GameObject(final int objectId) {
        this.objectId = objectId;
    }

    public HardReference<? extends GameObject> getRef() {
        return HardReferences.emptyRef();
    }

    private void clearRef() {
        final HardReference<? extends GameObject> reference = getRef();
        if (reference != null) {
            reference.clear();
        }
    }

    public Reflection getReflection() {
        return _reflection;
    }

    public void setReflection(final int reflectionId) {
        final Reflection r = ReflectionManager.getInstance().get(reflectionId);
        if (r == null) {
            Log.debug("Trying to set unavailable reflection: " + reflectionId + " for object: " + this + "!", new Throwable().fillInStackTrace());
            return;
        }
        setReflection(r);
    }

    public void setReflection(final Reflection reflection) {
        if (_reflection == reflection) {
            return;
        }
        boolean respawn = false;
        if (isVisible()) {
            decayMe();
            respawn = true;
        }
        final Reflection r = getReflection();
        if (!r.isDefault()) {
            r.removeObject(this);
        }
        _reflection = reflection;
        if (!reflection.isDefault()) {
            reflection.addObject(this);
        }
        if (respawn) {
            spawnMe();
        }
    }

    public int getReflectionId() {
        return _reflection.getId();
    }

    public int getGeoIndex() {
        return _reflection.getGeoIndex();
    }

    @Override
    public final int hashCode() {
        return objectId;
    }

    public final int getObjectId() {
        return objectId;
    }

    public int getX() {
        return _x;
    }

    public int getY() {
        return _y;
    }

    public int getZ() {
        return _z;
    }

    public Location getLoc() {
        return new Location(_x, _y, _z, getHeading());
    }

    public void setLoc(final Location loc) {
        setXYZ(loc.x, loc.y, loc.z);
    }

    public int getGeoZ(final Location loc) {
        return GeoEngine.getHeight(loc, getGeoIndex());
    }

    public void setXYZ(final int x, final int y, final int z) {
        _x = World.validCoordX(x);
        _y = World.validCoordY(y);
        _z = World.validCoordZ(z);
        World.addVisibleObject(this, null);
    }

    public final boolean isVisible() {
        return _state.get() == VISIBLE;
    }

    public InvisibleType getInvisibleType() {
        return InvisibleType.NONE;
    }

    public final boolean isInvisible() {
        return getInvisibleType() != InvisibleType.NONE;
    }

    public void spawnMe(final Location loc) {
        spawnMe0(loc, null);
    }

    protected void spawnMe0(final Location loc, final Creature dropper) {
        _x = loc.x;
        _y = loc.y;
        _z = getGeoZ(loc);
        spawn0(dropper);
    }

    public final void spawnMe() {
        spawn0(null);
    }

    protected void spawn0(final Creature dropper) {
        if (!_state.compareAndSet(CREATED, VISIBLE)) {
            return;
        }
        World.addVisibleObject(this, dropper);
        onSpawn();
    }

    public void toggleVisible() {
        if (isVisible()) {
            decayMe();
        } else {
            spawnMe();
        }
    }

    protected void onSpawn() {
    }

    public final void decayMe() {
        if (!_state.compareAndSet(VISIBLE, CREATED)) {
            return;
        }
        World.removeVisibleObject(this);
        onDespawn();
    }

    protected void onDespawn() {
    }

    public final void deleteMe() {
        decayMe();
        if (!_state.compareAndSet(CREATED, DELETED)) {
            return;
        }
        onDelete();
    }

    public final boolean isDeleted() {
        return _state.get() == -1;
    }

    protected void onDelete() {
        final Reflection r = getReflection();
        if (!r.isDefault()) {
            r.removeObject(this);
        }
        clearRef();
    }

    public void onAction(final Player player, final boolean shift) {
        if (Events.onAction(player, this, shift)) {
            return;
        }
        player.sendActionFailed();
    }

    public int getActingRange() {
        return -1;
    }

    public void onForcedAttack(final Player player, final boolean shift) {
        player.sendActionFailed();
    }

    public boolean isAttackable(final Creature attacker) {
        return false;
    }

    public String getL2ClassShortName() {
        return getClass().getSimpleName();
    }

    public final long getXYDeltaSq(final int x, final int y) {
        final long dx = x - getX();
        final long dy = y - getY();
        return dx * dx + dy * dy;
    }

    public final long getXYDeltaSq(final Location loc) {
        return getXYDeltaSq(loc.x, loc.y);
    }

    public final long getZDeltaSq(final int z) {
        final long dz = z - getZ();
        return dz * dz;
    }

    public final long getZDeltaSq(final Location loc) {
        return getZDeltaSq(loc.z);
    }

    public final long getXYZDeltaSq(final int x, final int y, final int z) {
        return getXYDeltaSq(x, y) + getZDeltaSq(z);
    }

    public final long getXYZDeltaSq(final Location loc) {
        return getXYDeltaSq(loc.x, loc.y) + getZDeltaSq(loc.z);
    }

    public final double getDistance(final int x, final int y) {
        return Math.sqrt(getXYDeltaSq(x, y));
    }

    public final double getDistance(final int x, final int y, final int z) {
        return Math.sqrt(getXYZDeltaSq(x, y, z));
    }

    public final double getDistance(final Location loc) {
        return getDistance(loc.x, loc.y, loc.z);
    }

    public final boolean isInRange(final GameObject obj, final long range) {
        if (obj == null) {
            return false;
        }
        if (obj.getReflection() != getReflection()) {
            return false;
        }
        final long dx = Math.abs(obj.getX() - getX());
        if (dx > range) {
            return false;
        }
        final long dy = Math.abs(obj.getY() - getY());
        if (dy > range) {
            return false;
        }
        final long dz = Math.abs(obj.getZ() - getZ());
        return dz <= 1500L && dx * dx + dy * dy <= range * range;
    }

    public final boolean isInActingRange(final GameObject obj) {
        return isInRangeZ(obj, getActingRange());
    }

    public final boolean isInRangeZ(final GameObject obj, final long range) {
        if (obj == null) {
            return false;
        }
        if (obj.getReflection() != getReflection()) {
            return false;
        }
        final long dx = Math.abs(obj.getX() - getX());
        if (dx > range) {
            return false;
        }
        final long dy = Math.abs(obj.getY() - getY());
        if (dy > range) {
            return false;
        }
        final long dz = Math.abs(obj.getZ() - getZ());
        return dz <= range && dx * dx + dy * dy + dz * dz <= range * range;
    }

    public final boolean isInRange(final Location loc, final long range) {
        return isInRangeSq(loc, range * range);
    }

    public final boolean isInRangeSq(final Location loc, final long range) {
        return getXYDeltaSq(loc) <= range;
    }

    public final boolean isInRangeZ(final Location loc, final long range) {
        return isInRangeZSq(loc, range * range);
    }

    public final boolean isInRangeZSq(final Location loc, final long range) {
        return getXYZDeltaSq(loc) <= range;
    }

    public final double getDistance(final GameObject obj) {
        if (obj == null) {
            return 0.0;
        }
        return Math.sqrt(getXYDeltaSq(obj.getX(), obj.getY()));
    }

    public final double getDistance3D(final GameObject obj) {
        if (obj == null) {
            return 0.0;
        }
        return Math.sqrt(getXYZDeltaSq(obj.getX(), obj.getY(), obj.getZ()));
    }

    public final double getRealDistance(final GameObject obj) {
        return getRealDistance3D(obj, true);
    }

    public final double getRealDistance3D(final GameObject obj) {
        return getRealDistance3D(obj, false);
    }

    public final double getRealDistance3D(final GameObject obj, final boolean ignoreZ) {
        double distance = ignoreZ ? getDistance(obj) : getDistance3D(obj);
        if (isCreature()) {
            distance -= ((Creature) this).getTemplate().getCollisionRadius();
        }
        if (obj.isCreature()) {
            distance -= ((Creature) obj).getTemplate().getCollisionRadius();
        }
        return (distance > 0.0) ? distance : 0.0;
    }

    public final long getSqDistance(final int x, final int y) {
        return getXYDeltaSq(x, y);
    }

    public final long getSqDistance(final GameObject obj) {
        if (obj == null) {
            return 0L;
        }
        return getXYDeltaSq(obj.getLoc());
    }

    public Player getPlayer() {
        return null;
    }

    public int getHeading() {
        return 0;
    }

    public int getMoveSpeed() {
        return 0;
    }

    public WorldRegion getCurrentRegion() {
        return _currentRegion;
    }

    public void setCurrentRegion(final WorldRegion region) {
        _currentRegion = region;
    }

    public boolean isInObserverMode() {
        return false;
    }

    public boolean isOlyParticipant() {
        return false;
    }

    public boolean isInBoat() {
        return false;
    }

    public boolean isFlying() {
        return false;
    }

    public double getColRadius() {
        LOGGER.warn("getColRadius called directly from GameObject");
        Thread.dumpStack();
        return 0.0;
    }

    public double getColHeight() {
        LOGGER.warn("getColHeight called directly from GameObject");
        Thread.dumpStack();
        return 0.0;
    }

    public boolean isCreature() {
        return false;
    }

    public boolean isPlayable() {
        return false;
    }

    public boolean isPlayer() {
        return false;
    }

    public boolean isPet() {
        return false;
    }

    public boolean isSummon() {
        return false;
    }

    public boolean isNpc() {
        return false;
    }

    public boolean isMonster() {
        return false;
    }

    public boolean isItem() {
        return false;
    }

    public boolean isRaid() {
        return false;
    }

    public boolean isBoss() {
        return false;
    }

    public boolean isTrap() {
        return false;
    }

    public boolean isDoor() {
        return false;
    }

    public boolean isArtefact() {
        return false;
    }

    public boolean isSiegeGuard() {
        return false;
    }

    public boolean isBoat() {
        return false;
    }

    public boolean isVehicle() {
        return false;
    }

    public boolean isMinion() {
        return false;
    }

    public String getName() {
        return getClass().getSimpleName() + ":" + objectId;
    }

    public String dump() {
        return dump(true);
    }

    public String dump(final boolean simpleTypes) {
        return Util.dumpObject(this, simpleTypes, true, true);
    }

    public List<L2GameServerPacket> addPacketList(final Player forPlayer, final Creature dropper) {
        return Collections.emptyList();
    }

    public List<L2GameServerPacket> deletePacketList() {
        return Collections.singletonList(new DeleteObject(this));
    }

    @Override
    public void addEvent(final GlobalEvent event) {
        event.onAddEvent(this);
        super.addEvent(event);
    }

    @Override
    public void removeEvent(final GlobalEvent event) {
        event.onRemoveEvent(this);
        super.removeEvent(event);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj == this || (obj != null && obj.getClass() == getClass() && ((GameObject) obj).getObjectId() == getObjectId());
    }
}
