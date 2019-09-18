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
import ru.j2dev.gameserver.data.xml.holder.StringHolder;
import ru.j2dev.gameserver.listener.actor.OnDeathListener;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Zone;
import ru.j2dev.gameserver.model.actor.listener.CharListenerList;
import ru.j2dev.gameserver.model.instances.BossInstance;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PlaySound;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PlaySound.Type;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SocialAction;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.Log;
import ru.j2dev.gameserver.utils.ReflectionUtils;
import ru.j2dev.gameserver.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class AntharasManager extends Functions implements OnInitScriptListener {
    public static final int ANTHARAS_STRONG = 29068;
    private static final Logger LOGGER = LoggerFactory.getLogger(AntharasManager.class);
    private static final int _teleportCubeId = 31859;
    private static final int PORTAL_STONE = 3865;
    private static final Location TELEPORT_POSITION = new Location(179892, 114915, -7704);
    private static final Location _teleportCubeLocation = new Location(177615, 114941, -7709, 0);
    private static final Location _antharasLocation = new Location(181911, 114835, -7678, 32542);
    private static final List<NpcInstance> _spawnedMinions = new ArrayList<>();
    private static final AtomicBoolean Dying = new AtomicBoolean(false);
    private static BossInstance _antharas;
    private static NpcInstance _teleCube;
    private static ScheduledFuture<?> _monsterSpawnTask;
    private static ScheduledFuture<?> _intervalEndTask;
    private static ScheduledFuture<?> _socialTask;
    private static ScheduledFuture<?> _moveAtRandomTask;
    private static ScheduledFuture<?> _sleepCheckTask;
    private static ScheduledFuture<?> _onAnnihilatedTask;
    private static EpicBossState _state;
    private static Zone _zone;
    private static long _lastAttackTime;
    private static volatile boolean _entryLocked;

    public static EpicBossState getEpicBossState() {
        return _state;
    }

    private static void banishForeigners() {
        getPlayersInside().forEach(Player::teleToClosestTown);
    }

    private static synchronized void checkAnnihilated() {
        if (_onAnnihilatedTask == null && isPlayersAnnihilated()) {
            _onAnnihilatedTask = ThreadPoolManager.getInstance().schedule(new onAnnihilated(), Config.ANTHARAS_CLEAR_ZONE_IF_ALL_DIE);
        }
    }

    private static List<Player> getPlayersInside() {
        return getZone().getInsidePlayers();
    }

    private static long getRespawnInterval() {
        SchedulingPattern timePattern;
        if (!Config.FWA_FIXTIMEPATTERNOFANTHARAS.isEmpty()) {
            final long now = System.currentTimeMillis();
            try {
                timePattern = new SchedulingPattern(Config.FWA_FIXTIMEPATTERNOFANTHARAS);
                final long delay = timePattern.next(now) - now;
                return Math.max(60000L, delay);
            } catch (InvalidPatternException ipe) {
                throw new RuntimeException("Invalid respawn data \"" + Config.FWA_FIXTIMEPATTERNOFANTHARAS + "\" in " + AntharasManager.class.getSimpleName(), ipe);
            }
        }
        return (long) (Config.ALT_RAID_RESPAWN_MULTIPLIER * (Config.FWA_FIXTIMEINTERVALOFANTHARAS + Rnd.get(0L, Config.FWA_RANDOMINTERVALOFANTHARAS)));
    }

    public static Zone getZone() {
        return _zone;
    }

    private static boolean isPlayersAnnihilated() {
        return getPlayersInside().stream().allMatch(Player::isDead);
    }

    private static void onAntharasDie() {
        if (!Dying.compareAndSet(false, true)) {
            return;
        }
        _state.setRespawnDate(getRespawnInterval());
        _state.setState(State.INTERVAL);
        _state.update();
        scheduleIntervalEnd();
        _entryLocked = false;
        _teleCube = Functions.spawn(AntharasManager._teleportCubeLocation, _teleportCubeId);
        Log.add("Antharas died", "bosses");
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
        if (_antharas != null) {
            _antharas.deleteMe();
        }
        _spawnedMinions.forEach(GameObject::deleteMe);
        if (_teleCube != null) {
            _teleCube.deleteMe();
        }
        _entryLocked = false;
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
        if (_moveAtRandomTask != null) {
            _moveAtRandomTask.cancel(false);
            _moveAtRandomTask = null;
        }
        if (_sleepCheckTask != null) {
            _sleepCheckTask.cancel(false);
            _sleepCheckTask = null;
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

    public static void setLastAttackTime() {
        _lastAttackTime = System.currentTimeMillis();
    }

    private static synchronized void setAntharasSpawnTask() {
        if (_monsterSpawnTask == null) {
            _monsterSpawnTask = ThreadPoolManager.getInstance().schedule(new AntharasSpawn(1), Config.FWA_APPTIMEOFANTHARAS);
        }
    }

    private static void broadcastCustomScreenMessage(final String address) {
        getPlayersInside().forEach(p -> p.sendPacket(new ExShowScreenMessage(StringHolder.getInstance().getNotNull(p, address), 8000, ScreenMessageAlign.TOP_CENTER, false)));
    }

    public static void addSpawnedMinion(final NpcInstance npc) {
        _spawnedMinions.add(npc);
    }

    public static void enterTheLair(final Player player) {
        if (player == null) {
            return;
        }
        if (_entryLocked) {
            player.sendMessage(new CustomMessage("AntharasStillReborn", player));
            return;
        }
        switch (_state.getState()) {
            case ALIVE: {
                player.sendMessage(new CustomMessage("AntharasAlreadyReborn", player));
            }
            case DEAD:
            case INTERVAL: {
                player.sendMessage(new CustomMessage("AntharasStillReborn", player));
            }
            default: {
                if (player.isDead() || player.isFlying() || player.isCursedWeaponEquipped() || player.getInventory().getCountOf(PORTAL_STONE) < 1L || !player.isInRange(player, 500L)) {
                    player.sendMessage(new CustomMessage("AntharasPlayerNotRequirements", player));
                    return;
                }
                player.getInventory().destroyItemByItemId(PORTAL_STONE, 1L);
                player.teleToLocation(TELEPORT_POSITION);
                setAntharasSpawnTask();
            }
        }
    }

    private class OnDeathListenerImpl implements OnDeathListener {

        @Override
        public void onDeath(final Creature self, final Creature killer) {
            if (self.isPlayer() && _state != null && _state.getState() == State.ALIVE && _zone != null && _zone.checkIfInZone(self.getX(), self.getY())) {
                checkAnnihilated();
            } else if (self.isNpc() && self.getNpcId() == ANTHARAS_STRONG) {
                ThreadPoolManager.getInstance().schedule(new AntharasSpawn(8), 10L);
            }
        }
    }

    private void init() {
        _state = new EpicBossState(ANTHARAS_STRONG);
        _zone = ReflectionUtils.getZone("[antharas_epic]");
        CharListenerList.addGlobal(new OnDeathListenerImpl());
        LOGGER.info("AntharasManager: State of Antharas is " + _state.getState() + ".");
        if (_state.getState() != State.NOTSPAWN) {
            setIntervalEndTask();
        }
        LOGGER.info("AntharasManager: Next spawn date of Antharas is " + TimeUtils.toSimpleFormat(_state.getRespawnDate()) + ".");
    }

    @Override
    public void onInit() {
        init();
    }


    private static class AntharasSpawn extends RunnableImpl {
        private final int _distance;
        private final List<Player> _players;
        private int _taskId;

        AntharasSpawn(final int taskId) {
            _distance = 2550;
            _taskId = 0;
            _players = getPlayersInside();
            _taskId = taskId;
        }

        @Override
        public void runImpl() {
            switch (_taskId) {
                case 1: {
                    Dying.set(false);
                    _antharas = (BossInstance) Functions.spawn(_antharasLocation, ANTHARAS_STRONG);
                    _antharas.setAggroRange(0);
                    _state.setRespawnDate(getRespawnInterval());
                    _state.setState(State.ALIVE);
                    _state.update();
                    _socialTask = ThreadPoolManager.getInstance().schedule(new AntharasSpawn(2), 2000L);
                    _entryLocked = true;
                    break;
                }
                case 2: {
                    _players.forEach(pc -> {
                        if (pc.getDistance(_antharas) <= _distance) {
                            pc.enterMovieMode();
                            pc.specialCamera(_antharas, 700, 13, -19, 0, 20000, 0, 0, 0, 0);
                        } else {
                            pc.leaveMovieMode();
                        }
                    });
                    _socialTask = ThreadPoolManager.getInstance().schedule(new AntharasSpawn(3), 3000L);
                    break;
                }
                case 3: {
                    _antharas.broadcastPacket(new SocialAction(_antharas.getObjectId(), 1));
                    _players.forEach(pc -> {
                        if (pc.getDistance(_antharas) <= _distance) {
                            pc.enterMovieMode();
                            pc.specialCamera(_antharas, 700, 13, 0, 6000, 20000, 0, 0, 0, 0);
                        } else {
                            pc.leaveMovieMode();
                        }
                    });
                    _socialTask = ThreadPoolManager.getInstance().schedule(new AntharasSpawn(4), 10000L);
                    break;
                }
                case 4: {
                    _antharas.broadcastPacket(new SocialAction(_antharas.getObjectId(), 2));
                    _players.forEach(pc -> {
                        if (pc.getDistance(_antharas) <= _distance) {
                            pc.enterMovieMode();
                            pc.specialCamera(_antharas, 3700, 0, -3, 0, 10000, 0, 0, 0, 0);
                        } else {
                            pc.leaveMovieMode();
                        }
                    });
                    _socialTask = ThreadPoolManager.getInstance().schedule(new AntharasSpawn(5), 200L);
                    break;
                }
                case 5: {
                    _players.forEach(pc -> {
                        if (pc.getDistance(_antharas) <= _distance) {
                            pc.enterMovieMode();
                            pc.specialCamera(_antharas, 1100, 0, -3, 22000, 30000, 0, 0, 0, 0);
                        } else {
                            pc.leaveMovieMode();
                        }
                    });
                    _socialTask = ThreadPoolManager.getInstance().schedule(new AntharasSpawn(6), 10800L);
                    break;
                }
                case 6: {
                    _players.forEach(pc -> {
                        if (pc.getDistance(_antharas) <= _distance) {
                            pc.enterMovieMode();
                            pc.specialCamera(_antharas, 1100, 0, -3, 300, 7000, 0, 0, 0, 0);
                        } else {
                            pc.leaveMovieMode();
                        }
                    });
                    _socialTask = ThreadPoolManager.getInstance().schedule(new AntharasSpawn(7), 7000L);
                    break;
                }
                case 7: {
                    _players.forEach(Player::leaveMovieMode);
                    broadcastCustomScreenMessage("AntharasYoucannothope");
                    _antharas.broadcastPacket(new PlaySound(Type.MUSIC, "BS02_A", 1, _antharas.getObjectId(), _antharas.getLoc()));
                    _antharas.setAggroRange(AntharasManager._antharas.getTemplate().aggroRange);
                    _antharas.setRunning();
                    _antharas.moveToLocation(new Location(179011, 114871, -7704), 0, false);
                    _sleepCheckTask = ThreadPoolManager.getInstance().schedule(new CheckLastAttack(), 600000L);
                    break;
                }
                case 8: {
                    _players.forEach(pc -> {
                        if (pc.getDistance(_antharas) <= _distance) {
                            pc.enterMovieMode();
                            pc.specialCamera(_antharas, 1200, 20, -10, 0, 13000, 0, 0, 0, 0);
                        } else {
                            pc.leaveMovieMode();
                        }
                    });
                    _socialTask = ThreadPoolManager.getInstance().schedule(new AntharasSpawn(9), 13000L);
                    break;
                }
                case 9: {
                    _players.forEach(Player::leaveMovieMode);
                    broadcastCustomScreenMessage("ANTHARAS_THE_EVIL_LAND_DRAGON_ANTHARAS_DEFEATED");
                    onAntharasDie();
                    break;
                }
            }
        }
    }

    private static class CheckLastAttack extends RunnableImpl {
        @Override
        public void runImpl() {
            if (_state.getState() == State.ALIVE) {
                if (_lastAttackTime + Config.FWA_LIMITUNTILSLEEPANTHARAS < System.currentTimeMillis()) {
                    sleep();
                } else {
                    _sleepCheckTask = ThreadPoolManager.getInstance().schedule(new CheckLastAttack(), 60000L);
                }
            }
        }
    }

    private static class IntervalEnd extends RunnableImpl {
        @Override
        public void runImpl() {
            setUnspawn();
            _state.setState(State.NOTSPAWN);
            _state.update();
        }
    }

    private static class onAnnihilated extends RunnableImpl {
        @Override
        public void runImpl() {
            synchronized (AntharasManager.class) {
                if (!isPlayersAnnihilated() || _state.getState() == State.INTERVAL) {
                    _onAnnihilatedTask = null;
                    return;
                }
            }
            sleep();
        }
    }
}
