package ru.j2dev.gameserver.model.instances;

import ru.j2dev.commons.geometry.Shape;
import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.ai.CtrlIntention;
import ru.j2dev.gameserver.ai.DoorAI;
import ru.j2dev.gameserver.geodata.GeoCollision;
import ru.j2dev.gameserver.geodata.GeoEngine;
import ru.j2dev.gameserver.listener.actor.door.OnOpenCloseListener;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.entity.SevenSigns;
import ru.j2dev.gameserver.model.entity.events.impl.SiegeEvent;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.*;
import ru.j2dev.gameserver.scripts.Events;
import ru.j2dev.gameserver.templates.DoorTemplate;
import ru.j2dev.gameserver.templates.DoorTemplate.DoorType;
import ru.j2dev.gameserver.templates.item.WeaponTemplate;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.Log;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class DoorInstance extends Creature implements GeoCollision {
    private final Lock _openLock;
    protected ScheduledFuture<?> _autoActionTask;
    private boolean _open;
    private boolean _geoOpen;
    private int _upgradeHp;
    private byte[][] _geoAround;

    public DoorInstance(final int objectId, final DoorTemplate template) {
        super(objectId, template);
        _open = true;
        _geoOpen = true;
        _openLock = new ReentrantLock();
    }

    public boolean isUnlockable() {
        return getTemplate().isUnlockable();
    }

    @Override
    public String getName() {
        return getTemplate().getName();
    }

    @Override
    public int getLevel() {
        return 1;
    }

    public int getDoorId() {
        return getTemplate().getNpcId();
    }

    public boolean isOpen() {
        return _open;
    }

    protected boolean setOpen(final boolean open) {
        if (_open == open) {
            return false;
        }
        _open = open;
        return true;
    }

    public void scheduleAutoAction(final boolean open, final long actionDelay) {
        if (_autoActionTask != null) {
            _autoActionTask.cancel(false);
            _autoActionTask = null;
        }
        _autoActionTask = ThreadPoolManager.getInstance().schedule(new AutoOpenClose(open), actionDelay);
    }

    public int getDamage() {
        final int dmg = 6 - (int) Math.ceil(getCurrentHpRatio() * 6.0);
        return Math.max(0, Math.min(6, dmg));
    }

    @Override
    public boolean isAutoAttackable(final Creature attacker) {
        return isAttackable(attacker);
    }

    @Override
    public boolean isAttackable(final Creature attacker) {
        if (attacker == null || isOpen()) {
            return false;
        }
        final SiegeEvent siegeEvent = getEvent(SiegeEvent.class);
        return !isInvul();
    }

    @Override
    public void sendChanges() {
    }

    @Override
    public ItemInstance getActiveWeaponInstance() {
        return null;
    }

    @Override
    public WeaponTemplate getActiveWeaponItem() {
        return null;
    }

    @Override
    public ItemInstance getSecondaryWeaponInstance() {
        return null;
    }

    @Override
    public WeaponTemplate getSecondaryWeaponItem() {
        return null;
    }

    public Location getCenterPoint() {
        final Shape shape = getShape();
        return new Location(shape.getXmin() + (shape.getXmax() - shape.getXmin() >> 1), shape.getYmin() + (shape.getYmax() - shape.getYmin() >> 1), shape.getZmin() + (shape.getZmax() - shape.getZmin() >> 1));
    }

    @Override
    public void onAction(final Player player, final boolean shift) {
        if (Events.onAction(player, this, shift)) {
            return;
        }
        if (this != player.getTarget()) {
            player.setTarget(this);
            player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel()));
            if (isAutoAttackable(player)) {
                player.sendPacket(new DoorInfo(this, player));
            }
            player.sendPacket(new ValidateLocation(this));
        } else {
            player.sendPacket(new MyTargetSelected(getObjectId(), 0));
            if (isAutoAttackable(player)) {
                player.getAI().Attack(this, false, shift);
                return;
            }
            if (!isInActingRange(player)) {
                if (!player.getAI().isIntendingInteract(this)) {
                    player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
                }
                return;
            }
            getAI().onEvtTwiceClick(player);
        }
    }

    @Override
    public int getActingRange() {
        return 150;
    }

    @Override
    public DoorAI getAI() {
        if (_ai == null) {
            synchronized (this) {
                if (_ai == null) {
                    _ai = getTemplate().getNewAI(this);
                }
            }
        }
        return (DoorAI) _ai;
    }

    @Override
    public void broadcastStatusUpdate() {
        World.getAroundPlayers(this).stream().filter(Objects::nonNull).forEach(player -> player.sendPacket(new DoorStatusUpdate(this, player)));
    }

    public boolean openMe() {
        return openMe(null, true);
    }

    public boolean openMe(final Player opener, final boolean autoClose) {
        _openLock.lock();
        try {
            if (!setOpen(true)) {
                return false;
            }
            setGeoOpen(true);
        } finally {
            _openLock.unlock();
        }
        broadcastStatusUpdate();
        if (autoClose && getTemplate().getCloseTime() > 0) {
            scheduleAutoAction(false, getTemplate().getCloseTime() * 1000L);
        }
        getAI().onEvtOpen(opener);
        getListeners().getListeners().stream().filter(l -> l instanceof OnOpenCloseListener).forEach(l -> ((OnOpenCloseListener) l).onOpen(this));
        return true;
    }

    public boolean closeMe() {
        return closeMe(null, true);
    }

    public boolean closeMe(final Player closer, final boolean autoOpen) {
        if (isDead()) {
            return false;
        }
        _openLock.lock();
        try {
            if (!setOpen(false)) {
                return false;
            }
            setGeoOpen(false);
        } finally {
            _openLock.unlock();
        }
        broadcastStatusUpdate();
        if (autoOpen && getTemplate().getOpenTime() > 0) {
            long openDelay = getTemplate().getOpenTime() * 1000L;
            if (getTemplate().getRandomTime() > 0) {
                openDelay += Rnd.get(0, getTemplate().getRandomTime()) * 1000L;
            }
            scheduleAutoAction(true, openDelay);
        }
        getAI().onEvtClose(closer);
        getListeners().getListeners().stream().filter(l -> l instanceof OnOpenCloseListener).forEach(l -> ((OnOpenCloseListener) l).onClose(this));
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public HardReference<? extends DoorInstance> getRef() {
        return (HardReference<DoorInstance>) super.getRef();
    }

    @Override
    public String toString() {
        return "[Door " + getDoorId() + "]";
    }

    @Override
    protected void onDeath(final Creature killer) {
        _openLock.lock();
        try {
            setGeoOpen(true);
        } finally {
            _openLock.unlock();
        }
        final SiegeEvent<?, ?> siegeEvent = getEvent(SiegeEvent.class);
        if (siegeEvent != null && siegeEvent.isInProgress()) {
            Log.add(toString(), getDoorType() + " destroyed by " + killer + ", " + siegeEvent);
        }
        super.onDeath(killer);
    }

    @Override
    protected void onRevive() {
        super.onRevive();
        _openLock.lock();
        try {
            if (!isOpen()) {
                setGeoOpen(false);
            }
        } finally {
            _openLock.unlock();
        }
    }

    @Override
    protected void onSpawn() {
        super.onSpawn();
        setCurrentHpMp(getMaxHp(), getMaxMp(), true);
        closeMe(null, true);
    }

    @Override
    protected void onDespawn() {
        if (_autoActionTask != null) {
            _autoActionTask.cancel(false);
            _autoActionTask = null;
        }
        super.onDespawn();
    }

    public boolean isHPVisible() {
        return getTemplate().isHPVisible();
    }

    @Override
    public int getMaxHp() {
        return super.getMaxHp() + _upgradeHp;
    }

    public int getUpgradeHp() {
        return _upgradeHp;
    }

    public void setUpgradeHp(final int hp) {
        _upgradeHp = hp;
    }

    @Override
    public int getPDef(final Creature target) {
        switch (SevenSigns.getInstance().getSealOwner(3)) {
            case 2: {
                return (int) (super.getPDef(target) * 1.2);
            }
            case 1: {
                return (int) (super.getPDef(target) * 0.3);
            }
            default: {
                return super.getPDef(target);
            }
        }
    }

    @Override
    public int getMDef(final Creature target, final Skill skill) {
        switch (SevenSigns.getInstance().getSealOwner(3)) {
            case 2: {
                return (int) (super.getMDef(target, skill) * 1.2);
            }
            case 1: {
                return (int) (super.getMDef(target, skill) * 0.3);
            }
            default: {
                return super.getMDef(target, skill);
            }
        }
    }

    @Override
    public boolean isInvul() {
        if (!getTemplate().isHPVisible()) {
            return true;
        }
        final SiegeEvent<?, ?> siegeEvent = getEvent(SiegeEvent.class);
        return (siegeEvent == null || !siegeEvent.isInProgress()) && super.isInvul();
    }

    public boolean setGeoOpen(final boolean open) {
        if (_geoOpen == open) {
            return false;
        }
        _geoOpen = open;
        if (Config.ALLOW_GEODATA) {
            if (open) {
                GeoEngine.removeGeoCollision(this, getGeoIndex());
            } else {
                GeoEngine.applyGeoCollision(this, getGeoIndex());
            }
        }
        return true;
    }

    @Override
    public boolean isMovementDisabled() {
        return true;
    }

    @Override
    public boolean isActionsDisabled() {
        return true;
    }

    @Override
    public boolean isFearImmune() {
        return true;
    }

    @Override
    public boolean isParalyzeImmune() {
        return true;
    }

    @Override
    public boolean isLethalImmune() {
        return true;
    }

    @Override
    public boolean isConcrete() {
        return true;
    }

    @Override
    public boolean isHealBlocked() {
        return true;
    }

    @Override
    public boolean isEffectImmune() {
        return true;
    }

    @Override
    public List<L2GameServerPacket> addPacketList(final Player forPlayer, final Creature dropper) {
        return Arrays.asList(new DoorInfo(this, forPlayer), new DoorStatusUpdate(this, forPlayer));
    }

    @Override
    public boolean isDoor() {
        return true;
    }

    @Override
    public Shape getShape() {
        return getTemplate().getPolygon();
    }

    @Override
    public byte[][] getGeoAround() {
        return _geoAround;
    }

    @Override
    public void setGeoAround(final byte[][] geo) {
        _geoAround = geo;
    }

    @Override
    public DoorTemplate getTemplate() {
        return (DoorTemplate) super.getTemplate();
    }

    public DoorType getDoorType() {
        return getTemplate().getDoorType();
    }

    public int getKey() {
        return getTemplate().getKey();
    }

    private class AutoOpenClose extends RunnableImpl {
        private final boolean _open;

        public AutoOpenClose(final boolean open) {
            _open = open;
        }

        @Override
        public void runImpl() {
            if (_open) {
                openMe(null, true);
            } else {
                closeMe(null, true);
            }
        }
    }
}
