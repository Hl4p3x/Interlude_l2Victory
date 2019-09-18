package bosses;

import bosses.EpicBossState.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.time.cron.SchedulingPattern;
import ru.j2dev.commons.time.cron.SchedulingPattern.InvalidPatternException;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.ai.DefaultAI;
import ru.j2dev.gameserver.listener.actor.OnDeathListener;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Zone;
import ru.j2dev.gameserver.model.actor.listener.CharListenerList;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SocialAction;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.Log;
import ru.j2dev.gameserver.utils.ReflectionUtils;
import ru.j2dev.gameserver.utils.TimeUtils;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class SailrenManager extends Functions implements OnInitScriptListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(SailrenManager.class);
    private static final int Sailren = 29065;
    private static final int Velociraptor = 22198;
    private static final int Pterosaur = 22199;
    private static final int Tyrannosaurus = 22217;
    private static final int TeleportCubeId = 31759;
    private static final boolean FWS_ENABLESINGLEPLAYER = Boolean.TRUE;
    private static final Location _enter = new Location(27734, -6938, -1982);
    private static final AtomicBoolean _isAlreadyEnteredOtherParty = new AtomicBoolean(false);
    private static final AtomicBoolean Dying = new AtomicBoolean(false);
    private static NpcInstance _velociraptor;
    private static NpcInstance _pterosaur;
    private static NpcInstance _tyranno;
    private static NpcInstance _sailren;
    private static NpcInstance _teleportCube;
    private static ScheduledFuture<?> _cubeSpawnTask;
    private static ScheduledFuture<?> _monsterSpawnTask;
    private static ScheduledFuture<?> _intervalEndTask;
    private static ScheduledFuture<?> _socialTask;
    private static ScheduledFuture<?> _activityTimeEndTask;
    private static ScheduledFuture<?> _onAnnihilatedTask;
    private static EpicBossState _state;
    private static Zone _zone;

    private static void banishForeigners() {
        getPlayersInside().forEach(Player::teleToClosestTown);
    }

    private static synchronized void checkAnnihilated() {
        if (_onAnnihilatedTask == null && isPlayersAnnihilated()) {
            _onAnnihilatedTask = ThreadPoolManager.getInstance().schedule(new onAnnihilated(), Config.SAILREN_CLEAR_ZONE_IF_ALL_DIE);
        }
    }

    private static List<Player> getPlayersInside() {
        return getZone().getInsidePlayers();
    }

    private static long getRespawnInterval() {
        SchedulingPattern timePattern;
        if (!Config.FWA_FIXTIMEPATTERNOFSAILREN.isEmpty()) {
            final long now = System.currentTimeMillis();
            try {
                timePattern = new SchedulingPattern(Config.FWA_FIXTIMEPATTERNOFSAILREN);
                final long delay = timePattern.next(now) - now;
                return Math.max(60000L, delay);
            } catch (InvalidPatternException ipe) {
                throw new RuntimeException("Invalid respawn data \"" + Config.FWA_FIXTIMEPATTERNOFSAILREN + "\" in " + SailrenManager.class.getSimpleName(), ipe);
            }
        }
        return (long) (Config.ALT_RAID_RESPAWN_MULTIPLIER * (Config.FWS_FIXINTERVALOFSAILRENSPAWN + Rnd.get(0L, Config.FWS_RANDOMINTERVALOFSAILRENSPAWN)));
    }

    public static Zone getZone() {
        return _zone;
    }

    private static boolean isPlayersAnnihilated() {
        return getPlayersInside().stream().allMatch(Player::isDead);
    }

    private static void onSailrenDie(final Creature killer) {
        if (!Dying.compareAndSet(false, true)) {
            return;
        }
        _state.setRespawnDate(getRespawnInterval());
        _state.setState(State.INTERVAL);
        _state.update();
        scheduleIntervalEnd();
        Log.add("Sailren died", "bosses");
        _cubeSpawnTask = ThreadPoolManager.getInstance().schedule(new CubeSpawn(), 10000L);
    }

    private static void setIntervalEndTask() {
        setUnspawn();
        if (_state.getState() == State.ALIVE) {
            _state.setState(State.NOTSPAWN);
            _state.update();
            return;
        }
        if (_state.getState() != State.INTERVAL) {
            _state.setRespawnDate(getRespawnInterval());
            _state.setState(State.INTERVAL);
            _state.update();
        }
        scheduleIntervalEnd();
    }

    private static void scheduleIntervalEnd() {
        if (_intervalEndTask != null) {
            _intervalEndTask.cancel(false);
            _intervalEndTask = null;
        }
        _intervalEndTask = ThreadPoolManager.getInstance().schedule(new IntervalEnd(), _state.getInterval());
    }

    private static void setUnspawn() {
        banishForeigners();
        if (_velociraptor != null) {
            if (_velociraptor.getSpawn() != null) {
                _velociraptor.getSpawn().stopRespawn();
            }
            _velociraptor.deleteMe();
            _velociraptor = null;
        }
        if (_pterosaur != null) {
            if (_pterosaur.getSpawn() != null) {
                _pterosaur.getSpawn().stopRespawn();
            }
            _pterosaur.deleteMe();
            _pterosaur = null;
        }
        if (_tyranno != null) {
            if (_tyranno.getSpawn() != null) {
                _tyranno.getSpawn().stopRespawn();
            }
            _tyranno.deleteMe();
            _tyranno = null;
        }
        if (_sailren != null) {
            if (_sailren.getSpawn() != null) {
                _sailren.getSpawn().stopRespawn();
            }
            _sailren.deleteMe();
            _sailren = null;
        }
        if (_teleportCube != null) {
            if (_teleportCube.getSpawn() != null) {
                _teleportCube.getSpawn().stopRespawn();
            }
            _teleportCube.deleteMe();
            _teleportCube = null;
        }
        if (_cubeSpawnTask != null) {
            _cubeSpawnTask.cancel(false);
            _cubeSpawnTask = null;
        }
        if (_monsterSpawnTask != null) {
            _monsterSpawnTask.cancel(false);
            _monsterSpawnTask = null;
        }
        if (_intervalEndTask != null) {
            _intervalEndTask.cancel(false);
            _intervalEndTask = null;
        }
        if (_socialTask != null) {
            _socialTask.cancel(false);
            _socialTask = null;
        }
        if (_activityTimeEndTask != null) {
            _activityTimeEndTask.cancel(false);
            _activityTimeEndTask = null;
        }
        if (_onAnnihilatedTask != null) {
            _onAnnihilatedTask.cancel(false);
            _onAnnihilatedTask = null;
        }
    }

    private static void sleep() {
        setUnspawn();
        if (_state.getState() == State.ALIVE) {
            _state.setState(State.NOTSPAWN);
            _state.update();
        }
    }

    public static synchronized void setSailrenSpawnTask() {
        if (_monsterSpawnTask == null) {
            _monsterSpawnTask = ThreadPoolManager.getInstance().schedule(new SailrenSpawn(22198), Config.FWS_INTERVALOFNEXTMONSTER);
        }
    }

    public static boolean isEnableEnterToLair() {
        return _state.getState() == State.NOTSPAWN;
    }

    public static int canIntoSailrenLair(final Player pc) {
        if (!FWS_ENABLESINGLEPLAYER && pc.getParty() == null) {
            return 4;
        }
        if (_isAlreadyEnteredOtherParty.get()) {
            return 2;
        }
        if (_state.getState() == State.NOTSPAWN) {
            return 0;
        }
        if (_state.getState() == State.ALIVE || _state.getState() == State.DEAD) {
            return 1;
        }
        if (_state.getState() == State.INTERVAL) {
            return 3;
        }
        return 0;
    }

    public static void entryToSailrenLair(final Player pc) {
        if (_isAlreadyEnteredOtherParty.compareAndSet(false, true)) {
            if (pc.getParty() == null) {
                pc.teleToLocation(Location.findPointToStay(_enter, 80, pc.getGeoIndex()));
            } else {
                final List<Player> members = pc.getParty().getPartyMembers().stream().filter(mem -> mem != null && !mem.isDead() && mem.isInRange(pc, 1000L)).collect(Collectors.toList());
                members.forEach(mem -> mem.teleToLocation(Location.findPointToStay(_enter, 80, mem.getGeoIndex())));
            }
        }
    }

    private void init() {
        CharListenerList.addGlobal((OnDeathListener) (self, killer) -> {
            if (self.isPlayer() && _state != null && _state.getState() == State.ALIVE && _zone != null && _zone.checkIfInZone(self.getX(), self.getY())) {
                checkAnnihilated();
            } else if (self == _velociraptor) {
                if (_monsterSpawnTask != null) {
                    _monsterSpawnTask.cancel(false);
                }
                _monsterSpawnTask = ThreadPoolManager.getInstance().schedule(new SailrenSpawn(22199), Config.FWS_INTERVALOFNEXTMONSTER);
            } else if (self == _pterosaur) {
                if (_monsterSpawnTask != null) {
                    _monsterSpawnTask.cancel(false);
                }
                _monsterSpawnTask = ThreadPoolManager.getInstance().schedule(new SailrenSpawn(22217), Config.FWS_INTERVALOFNEXTMONSTER);
            } else if (self == _tyranno) {
                if (_monsterSpawnTask != null) {
                    _monsterSpawnTask.cancel(false);
                }
                _monsterSpawnTask = ThreadPoolManager.getInstance().schedule(new SailrenSpawn(29065), Config.FWS_INTERVALOFNEXTMONSTER);
            } else if (self == _sailren) {
                onSailrenDie(killer);
            }
        });
        _state = new EpicBossState(29065);
        _zone = ReflectionUtils.getZone("[sailren_epic]");
        LOGGER.info("SailrenManager: State of Sailren is " + _state.getState() + ".");
        if (_state.getState() != State.NOTSPAWN) {
            setIntervalEndTask();
        }
        LOGGER.info("SailrenManager: Next spawn date of Sailren is " + TimeUtils.toSimpleFormat(_state.getRespawnDate()) + ".");
    }

    @Override
    public void onInit() {
        init();
    }

    private static class ActivityTimeEnd extends RunnableImpl {
        @Override
        public void runImpl() {
            sleep();
        }
    }

    private static class CubeSpawn extends RunnableImpl {
        @Override
        public void runImpl() {
            _teleportCube = Functions.spawn(new Location(27734, -6838, -1982, 0), 31759);
        }
    }

    private static class IntervalEnd extends RunnableImpl {
        @Override
        public void runImpl() {
            setUnspawn();
            _isAlreadyEnteredOtherParty.set(false);
            _state.setState(State.NOTSPAWN);
            _state.update();
        }
    }

    private static class Social extends RunnableImpl {
        private final int _action;
        private final NpcInstance _npc;

        public Social(final NpcInstance npc, final int actionId) {
            _npc = npc;
            _action = actionId;
        }

        @Override
        public void runImpl() {
            _npc.broadcastPacket(new SocialAction(_npc.getObjectId(), _action));
        }
    }

    private static class onAnnihilated extends RunnableImpl {
        @Override
        public void runImpl() {
            synchronized (SailrenManager.class) {
                if (!isPlayersAnnihilated() || _state.getState() == State.INTERVAL) {
                    _onAnnihilatedTask = null;
                    return;
                }
            }
            sleep();
        }
    }

    private static class SailrenSpawn extends RunnableImpl {
        private final Location _pos;
        private final int _npcId;

        SailrenSpawn(final int npcId) {
            _pos = new Location(27628, -6109, -1982, 44732);
            _npcId = npcId;
        }

        @Override
        public void runImpl() {
            if (_socialTask != null) {
                _socialTask.cancel(false);
                _socialTask = null;
            }
            switch (_npcId) {
                case 22198: {
                    Dying.set(false);
                    _velociraptor = Functions.spawn(new Location(27852, -5536, -1983, 44732), 22198);
                    ((DefaultAI) _velociraptor.getAI()).addTaskMove(_pos, false);
                    if (_socialTask != null) {
                        _socialTask.cancel(false);
                        _socialTask = null;
                    }
                    _socialTask = ThreadPoolManager.getInstance().schedule(new Social(_velociraptor, 2), 6000L);
                    if (_activityTimeEndTask != null) {
                        _activityTimeEndTask.cancel(false);
                        _activityTimeEndTask = null;
                    }
                    _activityTimeEndTask = ThreadPoolManager.getInstance().schedule(new ActivityTimeEnd(), Config.FWS_ACTIVITYTIMEOFMOBS);
                    break;
                }
                case 22199: {
                    _pterosaur = Functions.spawn(new Location(27852, -5536, -1983, 44732), 22199);
                    ((DefaultAI) _pterosaur.getAI()).addTaskMove(_pos, false);
                    if (_socialTask != null) {
                        _socialTask.cancel(false);
                        _socialTask = null;
                    }
                    _socialTask = ThreadPoolManager.getInstance().schedule(new Social(_pterosaur, 2), 6000L);
                    if (_activityTimeEndTask != null) {
                        _activityTimeEndTask.cancel(false);
                        _activityTimeEndTask = null;
                    }
                    _activityTimeEndTask = ThreadPoolManager.getInstance().schedule(new ActivityTimeEnd(), Config.FWS_ACTIVITYTIMEOFMOBS);
                    break;
                }
                case 22217: {
                    _tyranno = Functions.spawn(new Location(27852, -5536, -1983, 44732), 22217);
                    ((DefaultAI) _tyranno.getAI()).addTaskMove(_pos, false);
                    if (_socialTask != null) {
                        _socialTask.cancel(false);
                        _socialTask = null;
                    }
                    _socialTask = ThreadPoolManager.getInstance().schedule(new Social(_tyranno, 2), 6000L);
                    if (_activityTimeEndTask != null) {
                        _activityTimeEndTask.cancel(false);
                        _activityTimeEndTask = null;
                    }
                    _activityTimeEndTask = ThreadPoolManager.getInstance().schedule(new ActivityTimeEnd(), Config.FWS_ACTIVITYTIMEOFMOBS);
                    break;
                }
                case 29065: {
                    _sailren = Functions.spawn(new Location(27810, -5655, -1983, 44732), 29065);
                    _state.setRespawnDate(getRespawnInterval() + Config.FWS_ACTIVITYTIMEOFMOBS);
                    _state.setState(State.ALIVE);
                    _state.update();
                    _sailren.setRunning();
                    ((DefaultAI) _sailren.getAI()).addTaskMove(_pos, false);
                    if (_socialTask != null) {
                        _socialTask.cancel(false);
                        _socialTask = null;
                    }
                    _socialTask = ThreadPoolManager.getInstance().schedule(new Social(_sailren, 2), 6000L);
                    if (_activityTimeEndTask != null) {
                        _activityTimeEndTask.cancel(false);
                        _activityTimeEndTask = null;
                    }
                    _activityTimeEndTask = ThreadPoolManager.getInstance().schedule(new ActivityTimeEnd(), Config.FWS_ACTIVITYTIMEOFMOBS);
                    break;
                }
            }
        }
    }
}
