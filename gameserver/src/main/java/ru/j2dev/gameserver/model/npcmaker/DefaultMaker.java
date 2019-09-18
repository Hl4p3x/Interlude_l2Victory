package ru.j2dev.gameserver.model.npcmaker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.collections.MultiValueSet;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.model.Territory;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.templates.InstantZone;
import ru.j2dev.gameserver.templates.StatsSet;
import ru.j2dev.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

/**
 * @author: JunkyFunky
 * @date: 20.03.18 18:29
 */
public class DefaultMaker implements Cloneable {
    protected static final Logger LOGGER = LoggerFactory.getLogger(DefaultMaker.class);

    public final int maximum_npc;
    public final String name;
    protected final List<Territory> territories = new ArrayList<>(1);
    public int on_start_spawn = 1;
    public boolean debug = Config.MAKERS_DEBUG;
    public volatile int npc_count;
    protected int i_ai0;
    protected int i_ai1;
    protected int i_ai2;
    protected int i_ai3;
    protected int i_ai4;
    protected int i_ai5;
    protected int i_ai6;
    protected int i_ai7;
    protected int i_ai8;
    protected List<SpawnDefine> spawn_defines = new CopyOnWriteArrayList<>();
    protected int reflectionId;
    private MultiValueSet<String> _parameters = StatsSet.EMPTY;
    private Map<Integer, ScheduledFuture<?>> _timers;

    public DefaultMaker(int maximum_npc, String name) {
        this.maximum_npc = maximum_npc;
        this.name = name;
    }

    public void setParameter(final String str, final Object val) {
        if (_parameters == StatsSet.EMPTY) {
            _parameters = new StatsSet();
        }
        _parameters.set(str, val);
    }

    public int getParameter(final String str, final int val) {
        return _parameters.getInteger(str, val);
    }

    public long getParameter(final String str, final long val) {
        return _parameters.getLong(str, val);
    }

    public boolean getParameter(final String str, final boolean val) {
        return _parameters.getBool(str, val);
    }

    public String getParameter(final String str, final String val) {
        return _parameters.getString(str, val);
    }

    public Location getRandomPos(final int geoindex) {
        Territory terr = territories.get(Rnd.get(territories.size()));
        Location loc = terr.getRandomLoc(geoindex);
        loc.setH(Location.getRandomHeading());
        return loc;
    }

    public Location getRandomPos() {
        return getRandomPos(0);
    }

    public void addTerritory(Territory terr) {
        territories.add(terr);
    }

    public void addBannedTerritory(Territory banned_terr) {
        territories.forEach(terr -> terr.addBanned(banned_terr));
    }

    public void addSpawnDefine(SpawnDefine sd) {
        spawn_defines.add(sd);
    }

    public void onStart() {
        if (on_start_spawn != 0) {
            spawn_defines.forEach(sd -> {
                if (debug) {
                    LOGGER.info("{} onStart: {}", this, sd);
                }
                if (maximum_npc >= npc_count + sd.total && atomicIncrease(sd, sd.total)) {
                    sd.spawn(sd.total, 0, 0);
                }
            });
        }
    }

    public void onNpcDeleted(NpcInstance npc) {
        if (debug) {
            LOGGER.info("{} npc deleted: {}", this, npc);
        }
        SpawnDefine sd = npc.getSpawnDefine();
        if (sd.respawn != 0 && maximum_npc >= npc_count + 1 && atomicIncrease(sd, 1)) {
            sd.respawn(npc, sd.respawn, sd.respawn_rand);
        } else {
            npc.deleteMe();
        }
    }

    public void onNpcCreated(NpcInstance npc) {
        if (debug) {
            LOGGER.info("{} npc created: {}", this, npc);
        }
    }

    public void onAllNpcDeleted() {
        if (debug) {
            LOGGER.info("{} all npc deleted.", this);
        }
    }

    public void onScriptEvent(int eventId, Object arg1, Object arg2) {
        if (debug) {
            LOGGER.info("{} script event: "+this+" "+eventId+" "+" "+arg1+" "+" "+arg2);
        }
        if (eventId == 1000) {
            despawn();
        } else if (eventId == 1001) {
            spawn_defines.forEach(spawnDefine -> {
                int c = spawnDefine.total - spawnDefine.npc_count;
                if (c > 0 && maximum_npc >= npc_count + c && atomicIncrease(spawnDefine, c)) {
                    spawnDefine.spawn(c, (Integer) arg1, 0);
                }
            });
        }
    }

    public final boolean isInside(int x, int y) {
        return territories.stream().anyMatch(territory -> territory.isInside(x, y));
    }

    public void onEvtTimer(int timerId, Object arg1, Object arg2) {
    }

    public final void despawn() {
        if (debug) {
            LOGGER.info("{} despawn()", this);
        }
        spawn_defines.forEach(SpawnDefine::despawn);

        npc_count = 0;
    }

    public final void save() {
        spawn_defines.forEach(SpawnDefine::save);
    }

    public final void stopTimers() {
        if (_timers != null) {
            _timers.values().forEach(task -> task.cancel(true));
            _timers.clear();
        }
    }

    public final synchronized boolean atomicIncrease(SpawnDefine sd, int total) {
        if (maximum_npc >= npc_count + total && sd.total >= sd.npc_count + total) {
            npc_count += total;
            sd.npc_count += total;
            if (debug) {
                LOGGER.info(this+" atomicIncrease: maximum_npc="+maximum_npc+" npc_count="+npc_count+" sd.total="+sd.total+" sd.npc_count="+sd.npc_count+" total="+total+" "+sd+" true");
            }
            return true;
        }

        if (debug) {
            LOGGER.info(this+" atomicIncrease: maximum_npc="+maximum_npc+" npc_count="+npc_count+" sd.total="+sd.total+" sd.npc_count="+sd.npc_count+" total="+sd.total+" "+sd+" false");
        }

        return false;
    }

    public final synchronized boolean atomicIncrease(int total) {
        if (maximum_npc >= npc_count + total) {
            npc_count += total;
            if (debug) {
                LOGGER.info(this+" atomicIncrease: maximum_npc="+maximum_npc+" npc_count="+npc_count+" true");
            }
            return true;
        }

        if (debug) {
            LOGGER.info(this+" atomicIncrease: maximum_npc="+maximum_npc+" npc_count="+npc_count+" false");
        }

        return false;
    }

    public final synchronized boolean atomicDecrease(SpawnDefine sd, int total) {
        if (npc_count >= total && sd.npc_count >= total) {
            npc_count -= total;
            sd.npc_count -= total;
            if (debug) {
                LOGGER.info(this+" atomicDecrease: npc_count="+npc_count+" sd.npc_count="+sd.npc_count+" total="+total+" true");
            }
            return true;
        }

        if (debug) {
            LOGGER.info(this+" atomicDecrease: npc_count="+npc_count+" sd.npc_count="+sd.npc_count+" total="+total+" false");
        }
        return false;
    }

    public final synchronized boolean atomicDecrease(int total) {
        if (npc_count >= total) {
            npc_count -= total;
            if (debug) {
                LOGGER.info("{} atomicDecrease: npc_count={} true", this, npc_count);
            }
            return true;
        }

        if (debug) {
            LOGGER.info("{} atomicDecrease: npc_count={} false", this, npc_count);
        }

        return false;
    }

    public final void addTimer(int timerId, long delay) {
        addTimer(timerId, null, null, delay);
    }

    public final void addTimer(int timerId, Object arg1, long delay) {
        addTimer(timerId, arg1, null, delay);
    }

    public final void addTimer(int timerId, Object arg1, Object arg2, long delay) {
        if (_timers == null) {
            _timers = new ConcurrentHashMap<>();
        }

        ScheduledFuture<?> timer = ThreadPoolManager.getInstance().schedule(new Timer(timerId, arg1, arg2), delay);
        if (timer != null) {
            _timers.put(timerId, timer);
        }
    }

    public final void blockTimer(int timerId) {
        if (_timers == null) {
            return;
        }

        ScheduledFuture<?> timer = _timers.remove(timerId);
        if (timer != null) {
            timer.cancel(true);
        }
    }

    public void notifyScriptEvent(final int eventId, final Object arg1, final Object arg2) {
        ThreadPoolManager.getInstance().execute(() -> onScriptEvent(eventId, arg1, arg2));
    }

    public boolean inTerritory(int x, int y) {

        return territories.stream().anyMatch(terr -> terr.isInside(x, y));
    }

    public List<Territory> getTerritories() {
        return territories;
    }

    @Override
    public DefaultMaker clone() {
        try {
            DefaultMaker dm = (DefaultMaker) super.clone();
            dm.spawn_defines = new CopyOnWriteArrayList<>();
            if (debug) {
                LOGGER.info("Cloned maker: {}", dm);
            }
            spawn_defines.stream().map(SpawnDefine::clone).forEach(s -> {
                s.setMaker(dm);
                dm.spawn_defines.add(s);
            });
            return dm;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void onInstanceZoneEvent(InstantZone inst, int eventId) {
    }

    public void setReflectionId(int refId) {
        reflectionId = refId;
        spawn_defines.forEach(sd -> sd.setReflection(reflectionId));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + name + ";npc_count=" + npc_count + ";maximum_npc=" + maximum_npc + "]";
    }

    protected class Timer implements Runnable {
        private int _timerId;
        private Object _arg1;
        private Object _arg2;

        Timer(int timerId, Object arg1, Object arg2) {
            _timerId = timerId;
            _arg1 = arg1;
            _arg2 = arg2;
        }

        public void run() {
            if (_timers != null) {
                _timers.remove(_timerId);
            }
            onEvtTimer(_timerId, _arg1, _arg2);
        }
    }
}
