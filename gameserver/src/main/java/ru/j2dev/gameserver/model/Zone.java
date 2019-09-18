package ru.j2dev.gameserver.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.collections.MultiValueSet;
import ru.j2dev.commons.listener.Listener;
import ru.j2dev.commons.listener.ListenerList;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.listener.zone.OnZoneEnterLeaveListener;
import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.model.instances.DoorInstance;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.EventTrigger;
import ru.j2dev.gameserver.network.lineage2.serverpackets.L2GameServerPacket;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.stats.Stats;
import ru.j2dev.gameserver.stats.funcs.FuncAdd;
import ru.j2dev.gameserver.taskmanager.EffectTaskManager;
import ru.j2dev.gameserver.templates.ZoneTemplate;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.PositionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class Zone {
    public static final Zone[] EMPTY_L2ZONE_ARRAY = new Zone[0];
    public static final String BLOCKED_ACTION_PRIVATE_STORE = "open_private_store";
    public static final String BLOCKED_ACTION_PRIVATE_WORKSHOP = "open_private_workshop";
    public static final String BLOCKED_ACTION_DROP_MERCHANT_GUARD = "drop_merchant_guard";
    public static final String BLOCKED_ACTION_SAVE_BOOKMARK = "save_bookmark";
    public static final String BLOCKED_ACTION_USE_BOOKMARK = "use_bookmark";
    public static final String BLOCKED_ACTION_MINIMAP = "open_minimap";
    public static final String BLOCKED_ACTION_DROP_ITEM = "drop_item";
    public static final int ZONE_STATS_ORDER = 64;
    private static final Logger LOGGER = LoggerFactory.getLogger(Zone.class);

    private final MultiValueSet<String> _params;
    private final ZoneTemplate _template;
    private final ZoneListenerList listeners = new ZoneListenerList();
    private final Lock readLock;
    private final Lock writeLock;
    private final List<Creature> _objects = new ArrayList<>(32);
    private final Map<Creature, ZoneTimer> _zoneTimers = new ConcurrentHashMap<>();
    private ZoneType _type;
    private boolean _active;
    private Reflection _reflection;

    public Zone(final ZoneTemplate template) {
        this(template.getType(), template);
    }

    public Zone(final ZoneType type, final ZoneTemplate template) {
        ReadWriteLock lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
        _type = type;
        _template = template;
        _params = template.getParams();
    }

    public ZoneTemplate getTemplate() {
        return _template;
    }

    public final String getName() {
        return getTemplate().getName();
    }

    public ZoneType getType() {
        return _type;
    }

    public void setType(final ZoneType type) {
        _type = type;
    }

    public Territory getTerritory() {
        return getTemplate().getTerritory();
    }

    public final int getEnteringMessageId() {
        return getTemplate().getEnteringMessageId();
    }

    public final int getLeavingMessageId() {
        return getTemplate().getLeavingMessageId();
    }

    public Skill getZoneSkill() {
        return getTemplate().getZoneSkill();
    }

    public ZoneTarget getZoneTarget() {
        return getTemplate().getZoneTarget();
    }

    public Race getAffectRace() {
        return getTemplate().getAffectRace();
    }

    public int getDamageMessageId() {
        return getTemplate().getDamageMessageId();
    }

    public int getDamageOnHP() {
        return getTemplate().getDamageOnHP();
    }

    public int getDamageOnMP() {
        return getTemplate().getDamageOnMP();
    }

    public double getMoveBonus() {
        return getTemplate().getMoveBonus();
    }

    public double getRegenBonusHP() {
        return getTemplate().getRegenBonusHP();
    }

    public double getRegenBonusMP() {
        return getTemplate().getRegenBonusMP();
    }

    public long getRestartTime() {
        return getTemplate().getRestartTime();
    }

    public List<Location> getRestartPoints() {
        return getTemplate().getRestartPoints();
    }

    public List<Location> getPKRestartPoints() {
        return getTemplate().getPKRestartPoints();
    }

    public Location getSpawn() {
        if (getRestartPoints() == null) {
            return null;
        }
        final Location loc = getRestartPoints().get(Rnd.get(getRestartPoints().size()));
        return loc.clone();
    }

    public Location getPKSpawn() {
        if (getPKRestartPoints() == null) {
            return getSpawn();
        }
        final Location loc = getPKRestartPoints().get(Rnd.get(getPKRestartPoints().size()));
        return loc.clone();
    }

    /**
     * Проверяет находятся ли даные координаты в зоне. _loc - стандартная
     * территория для зоны
     *
     * @param x координата
     * @param y координата
     *          <p>
     * @return находятся ли координаты в локации
     */
    public boolean checkIfInZone(final int x, final int y) {
        return getTerritory().isInside(x, y);
    }

    public boolean checkIfInZone(final int x, final int y, final int z) {
        return checkIfInZone(x, y, z, _reflection);
    }

    public boolean checkIfInZone(final Location loc, final Reflection reflection) {
        return _active && _reflection == reflection && getTerritory().isInside(loc.x, loc.y, loc.z);
    }

    public boolean checkIfInZone(final int x, final int y, final int z, final Reflection reflection) {
        return _active && _reflection == reflection && getTerritory().isInside(x, y, z);
    }

    public boolean checkIfInZone(final Creature cha) {
        readLock.lock();
        try {
            return _objects.contains(cha);
        } finally {
            readLock.unlock();
        }
    }

    public final double findDistanceToZone(final GameObject obj, final boolean includeZAxis) {
        return findDistanceToZone(obj.getX(), obj.getY(), obj.getZ(), includeZAxis);
    }

    public final double findDistanceToZone(final int x, final int y, final int z, final boolean includeZAxis) {
        return PositionUtils.calculateDistance(x, y, z, (getTerritory().getXmax() + getTerritory().getXmin()) / 2, (getTerritory().getYmax() + getTerritory().getYmin()) / 2, (getTerritory().getZmax() + getTerritory().getZmin()) / 2, includeZAxis);
    }

    public void doEnter(final Creature cha) {
        boolean added = false;
        writeLock.lock();
        try {
            if (!_objects.contains(cha)) {
                added = _objects.add(cha);
            }
        } finally {
            writeLock.unlock();
        }
        if (added) {
            onZoneEnter(cha);
        }
        if (cha != null && cha.isPlayer() && cha.getPlayer().isGM()) {
            ((Player) cha).sendAdminMessage(cha.getPlayer().isLangRus() ? "Вы вошли в зону " + getName() : "You have entered the zone " + getName());
        }
    }

    protected void onZoneEnter(final Creature actor) {
        checkEffects(actor, true);
        addZoneStats(actor);
        if (actor.isPlayer()) {
            if (getEnteringMessageId() != 0) {
                actor.sendPacket(new SystemMessage(getEnteringMessageId()));
            }
            if (getTemplate().getEventId() != 0) {
                actor.sendPacket(new EventTrigger(getTemplate().getEventId(), true));
            }
            if (getTemplate().getBlockedActions() != null) {
                ((Player) actor).blockActions(getTemplate().getBlockedActions());
            }
        }
        listeners.onEnter(actor);
    }

    public void doLeave(final Creature cha) {
        boolean removed;
        writeLock.lock();
        try {
            removed = _objects.remove(cha);
        } finally {
            writeLock.unlock();
        }
        if (removed) {
            onZoneLeave(cha);
        }
        if (cha != null && cha.isPlayer() && cha.getPlayer().isGM()) {
            ((Player) cha).sendAdminMessage(cha.getPlayer().isLangRus() ? "Вы вышли из зоны " + getName() : "You have leave from zone " + getName());
        }
    }

    protected void onZoneLeave(final Creature actor) {
        checkEffects(actor, false);
        removeZoneStats(actor);
        if (actor.isPlayer()) {
            if (getLeavingMessageId() != 0 && actor.isPlayer()) {
                actor.sendPacket(new SystemMessage(getLeavingMessageId()));
            }
            if (getTemplate().getEventId() != 0 && actor.isPlayer()) {
                actor.sendPacket(new EventTrigger(getTemplate().getEventId(), false));
            }
            if (getTemplate().getBlockedActions() != null) {
                ((Player) actor).unblockActions(getTemplate().getBlockedActions());
            }
        }
        listeners.onLeave(actor);
    }

    private void addZoneStats(final Creature cha) {
        if (!checkTarget(cha)) {
            return;
        }
        if (getMoveBonus() != 0.0 && cha.isPlayable()) {
            cha.addStatFunc(new FuncAdd(Stats.RUN_SPEED, 64, this, getMoveBonus()));
            cha.sendChanges();
        }
        if (getRegenBonusHP() != 0.0) {
            cha.addStatFunc(new FuncAdd(Stats.REGENERATE_HP_RATE, 64, this, getRegenBonusHP()));
        }
        if (getRegenBonusMP() != 0.0) {
            cha.addStatFunc(new FuncAdd(Stats.REGENERATE_MP_RATE, 64, this, getRegenBonusMP()));
        }
    }

    private void removeZoneStats(final Creature cha) {
        if (getRegenBonusHP() == 0.0 && getRegenBonusMP() == 0.0 && getMoveBonus() == 0.0) {
            return;
        }
        cha.removeStatsOwner(this);
        cha.sendChanges();
    }

    private void checkEffects(final Creature cha, final boolean enter) {
        if (checkTarget(cha)) {
            if (enter) {
                if (getZoneSkill() != null) {
                    final ZoneTimer timer = new SkillTimer(cha);
                    _zoneTimers.put(cha, timer);
                    timer.start();
                } else if (getDamageOnHP() > 0 || getDamageOnMP() > 0) {
                    final ZoneTimer timer = new DamageTimer(cha);
                    _zoneTimers.put(cha, timer);
                    timer.start();
                }
            } else {
                final ZoneTimer timer = _zoneTimers.remove(cha);
                if (timer != null) {
                    timer.stop();
                }
                if (getZoneSkill() != null) {
                    cha.getEffectList().stopEffect(getZoneSkill());
                }
            }
        }
    }

    private boolean checkTarget(final Creature cha) {
        switch (getZoneTarget()) {
            case pc: {
                if (!cha.isPlayable()) {
                    return false;
                }
                break;
            }
            case only_pc: {
                if (!cha.isPlayer()) {
                    return false;
                }
                break;
            }
            case npc: {
                if (!cha.isNpc()) {
                    return false;
                }
                break;
            }
        }
        if (getAffectRace() != null) {
            final Player player = cha.getPlayer();
            if (player == null) {
                return false;
            }
            return player.getRace() == getAffectRace();
        }
        return true;
    }

    public List<Creature> getObjects() {
        readLock.lock();
        try {
            return _objects;
        } finally {
            readLock.unlock();
        }
    }

    public List<DoorInstance> getInsideDoors() {
        final List<DoorInstance> result = new ArrayList<>();
        readLock.lock();
        try {
            result.addAll(_objects.stream().filter(creature -> creature != null && creature.isDoor()).map(creature -> (DoorInstance) creature).collect(Collectors.toList()));
        } finally {
            readLock.unlock();
        }
        return result;
    }

    public List<Player> getInsidePlayers() {
        final List<Player> result = new ArrayList<>();
        readLock.lock();
        try {
            result.addAll(_objects.stream().filter(creature -> creature != null && creature.isPlayer()).map(creature -> (Player) creature).collect(Collectors.toList()));
        } finally {
            readLock.unlock();
        }
        return result;
    }

    public List<Playable> getInsidePlayables() {
        final List<Playable> result = new ArrayList<>();
        readLock.lock();
        try {
            result.addAll(_objects.stream().filter(creature -> creature != null && creature.isPlayable()).map(creature -> (Playable) creature).collect(Collectors.toList()));
        } finally {
            readLock.unlock();
        }
        return result;
    }

    public List<NpcInstance> getInsideNpcs() {
        final List<NpcInstance> result = new ArrayList<>();
        readLock.lock();
        try {
            result.addAll(_objects.stream().filter(object -> object != null && object.isNpc()).map(object -> (NpcInstance) object).collect(Collectors.toList()));
        } finally {
            readLock.unlock();
        }
        return result;
    }

    public boolean isActive() {
        return _active;
    }

    public void setActive(final boolean value) {
        writeLock.lock();
        try {
            if (_active == value) {
                return;
            }
            _active = value;
        } finally {
            writeLock.unlock();
        }
        if (_active) {
            World.addZone(this);
        } else {
            World.removeZone(this);
        }
    }

    public Reflection getReflection() {
        return _reflection;
    }

    public void setReflection(final Reflection reflection) {
        _reflection = reflection;
    }

    public void setParam(final String name, final String value) {
        _params.put(name, value);
    }

    public void setParam(final String name, final Object value) {
        _params.put(name, value);
    }

    public MultiValueSet<String> getParams() {
        return _params;
    }

    public <T extends Listener<Zone>> boolean addListener(final T listener) {
        return listeners.add(listener);
    }

    public <T extends Listener<Zone>> boolean removeListener(final T listener) {
        return listeners.remove(listener);
    }

    @Override
    public final String toString() {
        return "[Zone " + getType() + " name: " + getName() + "]";
    }

    public void broadcastPacket(final L2GameServerPacket packet, final boolean toAliveOnly) {
        final List<Player> insideZoners = getInsidePlayers();
        if (insideZoners != null && !insideZoners.isEmpty()) {
            insideZoners.forEach(player -> {
                if (toAliveOnly) {
                    if (player.isDead()) {
                        return;
                    }
                    player.broadcastPacket(packet);
                } else {
                    player.broadcastPacket(packet);
                }
            });
        }
    }

    public enum ZoneType {
        SIEGE,
        RESIDENCE,
        HEADQUARTER,
        FISHING,
        water,
        battle_zone,
        damage,
        instant_skill,
        mother_tree,
        peace_zone,
        poison,
        ssq_zone,
        swamp,
        no_escape,
        no_landing,
        no_restart,
        no_summon,
        dummy,
        offshore,
        epic,
        fun
    }

    public enum ZoneTarget {
        pc,
        npc,
        only_pc
    }

    private abstract class ZoneTimer extends RunnableImpl {
        protected final Creature cha;
        protected Future<?> future;
        protected boolean active;

        ZoneTimer(final Creature cha) {
            this.cha = cha;
        }

        public void start() {
            active = true;
            future = EffectTaskManager.getInstance().schedule(this, getTemplate().getInitialDelay() * 1000L);
        }

        public void stop() {
            active = false;
            if (future != null) {
                future.cancel(false);
                future = null;
            }
        }

        public void next() {
            if (!active) {
                return;
            }
            if (getTemplate().getUnitTick() == 0 && getTemplate().getRandomTick() == 0) {
                return;
            }
            future = EffectTaskManager.getInstance().schedule(this, (getTemplate().getUnitTick() + Rnd.get(0, getTemplate().getRandomTick())) * 1000L);
        }

        @Override
        public abstract void runImpl();
    }

    private class SkillTimer extends ZoneTimer {
        SkillTimer(final Creature cha) {
            super(cha);
        }

        @Override
        public void runImpl() {
            if (!isActive()) {
                return;
            }
            if (!checkTarget(cha)) {
                return;
            }
            final Skill skill = getZoneSkill();
            if (skill == null) {
                return;
            }
            if (Rnd.chance(getTemplate().getSkillProb()) && !cha.isDead()) {
                skill.getEffects(cha, cha, false, false);
            }
            next();
        }
    }

    private class DamageTimer extends ZoneTimer {
        DamageTimer(final Creature cha) {
            super(cha);
        }

        @Override
        public void runImpl() {
            if (!isActive()) {
                return;
            }
            if (!checkTarget(cha)) {
                return;
            }
            final int hp = getDamageOnHP();
            final int mp = getDamageOnMP();
            final int message = getDamageMessageId();
            if (hp == 0 && mp == 0) {
                return;
            }
            if (hp > 0) {
                cha.reduceCurrentHp(hp, cha, null, false, false, true, false, false, false, true);
                if (message > 0) {
                    cha.sendPacket(new SystemMessage(message).addNumber(hp));
                }
            }
            if (mp > 0) {
                cha.reduceCurrentMp(mp, null);
                if (message > 0) {
                    cha.sendPacket(new SystemMessage(message).addNumber(mp));
                }
            }
            next();
        }
    }

    public class ZoneListenerList extends ListenerList<Zone> {
        void onEnter(final Creature actor) {
            if (!getListeners().isEmpty()) {
                getListeners().forEach(listener -> ((OnZoneEnterLeaveListener) listener).onZoneEnter(Zone.this, actor));
            }
        }

        void onLeave(final Creature actor) {
            if (!getListeners().isEmpty()) {
                getListeners().forEach(listener -> ((OnZoneEnterLeaveListener) listener).onZoneLeave(Zone.this, actor));
            }
        }
    }
}
