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
import ru.j2dev.gameserver.ai.CtrlIntention;
import ru.j2dev.gameserver.data.xml.holder.StringHolder;
import ru.j2dev.gameserver.listener.actor.OnDeathListener;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Spawner;
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
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class ValakasManager extends Functions implements OnInitScriptListener {
    public static final int VALAKAS_NPC_ID = 29028;
    private static final Logger LOGGER = LoggerFactory.getLogger(ValakasManager.class);
    private static final int[][] _teleportCubeLocation = {{214880, -116144, -1644, 0}, {213696, -116592, -1644, 0}, {212112, -116688, -1644, 0}, {211184, -115472, -1664, 0}, {210336, -114592, -1644, 0}, {211360, -113904, -1644, 0}, {213152, -112352, -1644, 0}, {214032, -113232, -1644, 0}, {214752, -114592, -1644, 0}, {209824, -115568, -1421, 0}, {210528, -112192, -1403, 0}, {213120, -111136, -1408, 0}, {215184, -111504, -1392, 0}, {215456, -117328, -1392, 0}, {213200, -118160, -1424, 0}};
    private static final int _teleportCubeId = 31759;
    private static final Location TELEPORT_POSITION = new Location(203940, -111840, 66);
    private static final List<NpcInstance> _teleportCube = new ArrayList<>();
    private static final AtomicBoolean Dying = new AtomicBoolean(false);
    private static BossInstance _valakas;
    private static ScheduledFuture<?> _valakasSpawnTask;
    private static ScheduledFuture<?> _intervalEndTask;
    private static ScheduledFuture<?> _socialTask;
    private static ScheduledFuture<?> _mobiliseTask;
    private static ScheduledFuture<?> _moveAtRandomTask;
    private static ScheduledFuture<?> _respawnValakasTask;
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
            _onAnnihilatedTask = ThreadPoolManager.getInstance().schedule(new onAnnihilated(), Config.VALAKAS_CLEAR_ZONE_IF_ALL_DIE);
        }
    }

    private static List<Player> getPlayersInside() {
        return getZone().getInsidePlayers();
    }

    private static long getRespawnInterval() {
        SchedulingPattern timePattern;
        if (!Config.FWA_FIXTIMEPATTERNOFVALAKAS.isEmpty()) {
            final long now = System.currentTimeMillis();
            try {
                timePattern = new SchedulingPattern(Config.FWA_FIXTIMEPATTERNOFVALAKAS);
                final long delay = timePattern.next(now) - now;
                return Math.max(60000L, delay);
            } catch (InvalidPatternException ipe) {
                throw new RuntimeException("Invalid respawn data \"" + Config.FWA_FIXTIMEPATTERNOFVALAKAS + "\" in " + AntharasManager.class.getSimpleName(), ipe);
            }
        }
        return (long) (Config.ALT_RAID_RESPAWN_MULTIPLIER * (Config.FWV_FIXINTERVALOFVALAKAS + Rnd.get(0L, Config.FWV_RANDOMINTERVALOFVALAKAS)));
    }

    public static Zone getZone() {
        return _zone;
    }

    private static boolean isPlayersAnnihilated() {
        return getPlayersInside().stream().allMatch(Player::isDead);
    }

    private static void onValakasDie() {
        if (!Dying.compareAndSet(false, true)) {
            return;
        }
        _state.setRespawnDate(getRespawnInterval());
        _state.setState(State.INTERVAL);
        _state.update();
        scheduleIntervalEnd();
        _entryLocked = false;
        for (int[] ints : _teleportCubeLocation) {
            _teleportCube.add(Functions.spawn(new Location(ints[0], ints[1], ints[2], ints[3]), _teleportCubeId));
        }
        Log.add("Valakas died", "bosses");
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
        _entryLocked = false;
        if (_valakas != null) {
            _valakas.deleteMe();
        }
        _teleportCube.stream().filter(Objects::nonNull).forEach(cube -> {
            final Spawner spawner = cube.getSpawn();
            if (spawner != null) {
                spawner.stopRespawn();
            }
            cube.deleteMe();
        });
        _teleportCube.clear();
        if (_valakasSpawnTask != null) {
            _valakasSpawnTask.cancel(false);
            _valakasSpawnTask = null;
        }
        if (_intervalEndTask != null) {
            _intervalEndTask.cancel(false);
            _intervalEndTask = null;
        }
        if (_socialTask != null) {
            _socialTask.cancel(false);
            _socialTask = null;
        }
        if (_mobiliseTask != null) {
            _mobiliseTask.cancel(false);
            _mobiliseTask = null;
        }
        if (_moveAtRandomTask != null) {
            _moveAtRandomTask.cancel(false);
            _moveAtRandomTask = null;
        }
        if (_sleepCheckTask != null) {
            _sleepCheckTask.cancel(false);
            _sleepCheckTask = null;
        }
        if (_respawnValakasTask != null) {
            _respawnValakasTask.cancel(false);
            _respawnValakasTask = null;
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

    public static synchronized void setValakasSpawnTask() {
        if (_valakasSpawnTask == null) {
            _valakasSpawnTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(1), Config.FWV_APPTIMEOFVALAKAS);
        }
    }

    public static void broadcastCustomScreenMessage(final String address) {
        getPlayersInside().forEach(p -> p.sendPacket(new ExShowScreenMessage(StringHolder.getInstance().getNotNull(p, address), 8000, ScreenMessageAlign.TOP_CENTER, false)));
    }

    public static boolean isEnableEnterToLair() {
        return _state.getState() == State.NOTSPAWN;
    }

    public static void enterTheLair(final Player player) {
        if (player == null) {
            return;
        }
        if (_entryLocked) {
            player.sendMessage(new CustomMessage("ValakasStillReborn", player));
            return;
        }
        switch (_state.getState()) {
            case ALIVE: {
                player.sendMessage(new CustomMessage("ValakasAlreadyReborn", player));
            }
            case DEAD:
            case INTERVAL: {
                player.sendMessage(new CustomMessage("ValakasStillReborn", player));
            }
            default: {
                if (player.isDead() || player.isFlying() || player.isCursedWeaponEquipped() || !player.isInRange(player, 500L)) {
                    player.sendMessage(new CustomMessage("ValakasPlayerNotRequirements", player));
                    return;
                }
                player.teleToLocation(TELEPORT_POSITION);
                setValakasSpawnTask();
            }
        }
    }

    private void init() {
        CharListenerList.addGlobal((OnDeathListener) (self, killer) -> {
            if (self.isPlayer() && _state != null && _state.getState() == State.ALIVE && _zone != null && _zone.checkIfInZone(self.getX(), self.getY())) {
                checkAnnihilated();
            } else if (self.isNpc() && self.getNpcId() == VALAKAS_NPC_ID) {
                ThreadPoolManager.getInstance().schedule(new SpawnDespawn(12), 1L);
            }
        });
        _state = new EpicBossState(VALAKAS_NPC_ID);
        _zone = ReflectionUtils.getZone("[valakas_epic]");
        LOGGER.info("ValakasManager: State of Valakas is " + _state.getState() + ".");
        if (_state.getState() != State.NOTSPAWN) {
            setIntervalEndTask();
        }
        LOGGER.info("ValakasManager: Next spawn date of Valakas is " + TimeUtils.toSimpleFormat(_state.getRespawnDate()) + ".");
    }

    @Override
    public void onInit() {
        init();
    }

    private static class CheckLastAttack extends RunnableImpl {
        @Override
        public void runImpl() {
            if (_state.getState() == State.ALIVE) {
                if (_lastAttackTime + Config.FWV_LIMITUNTILSLEEPVALAKAS < System.currentTimeMillis()) {
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
            synchronized (ValakasManager.class) {
                if (!isPlayersAnnihilated() || _state.getState() == State.INTERVAL) {
                    _onAnnihilatedTask = null;
                    return;
                }
            }
            sleep();
        }
    }

    private static class SpawnDespawn extends RunnableImpl {
        private final int _distance;
        private final int _taskId;
        private final List<Player> _players;

        SpawnDespawn(final int taskId) {
            _distance = 2550;
            _players = getPlayersInside();
            _taskId = taskId;
        }

        @Override
        public void runImpl() {
            switch (_taskId) {
                case 1: {
                    _valakas = (BossInstance) Functions.spawn(new Location(212852, -114842, -1632, 833), VALAKAS_NPC_ID);
                    Dying.set(false);
                    _valakas.block();
                    _valakas.broadcastPacket(new PlaySound(Type.MUSIC, "BS03_A", 1, _valakas.getObjectId(), _valakas.getLoc()));
                    _state.setRespawnDate((long) (Config.ALT_RAID_RESPAWN_MULTIPLIER * (Config.FWV_FIXINTERVALOFVALAKAS + Rnd.get(0L, Config.FWV_RANDOMINTERVALOFVALAKAS))));
                    _state.setState(State.ALIVE);
                    _state.update();
                    _socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(2), 16L);
                    _entryLocked = true;
                    break;
                }
                case 2: {
                    _valakas.broadcastPacket(new SocialAction(_valakas.getObjectId(), 1));
                    _players.forEach(pc -> {
                        if (pc.getDistance(_valakas) <= _distance) {
                            pc.enterMovieMode();
                            pc.specialCamera(_valakas, 1800, 180, -1, 1500, 15000, 0, 0, 1, 0);
                        } else {
                            pc.leaveMovieMode();
                        }
                    });
                    _socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(3), 1500L);
                    break;
                }
                case 3: {
                    _players.forEach(pc -> {
                        if (pc.getDistance(_valakas) <= _distance) {
                            pc.enterMovieMode();
                            pc.specialCamera(_valakas, 1300, 180, -5, 3000, 15000, 0, -5, 1, 0);
                        } else {
                            pc.leaveMovieMode();
                        }
                    });
                    _socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(4), 3300L);
                    break;
                }
                case 4: {
                    _players.forEach(pc -> {
                        if (pc.getDistance(_valakas) <= _distance) {
                            pc.enterMovieMode();
                            pc.specialCamera(_valakas, 500, 180, -8, 600, 15000, 0, 60, 1, 0);
                        } else {
                            pc.leaveMovieMode();
                        }
                    });
                    _socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(5), 2900L);
                    break;
                }
                case 5: {
                    _players.forEach(pc -> {
                        if (pc.getDistance(_valakas) <= _distance) {
                            pc.enterMovieMode();
                            pc.specialCamera(_valakas, 800, 180, -8, 2700, 15000, 0, 30, 1, 0);
                        } else {
                            pc.leaveMovieMode();
                        }
                    });
                    _socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(6), 2700L);
                    break;
                }
                case 6: {
                    _players.forEach(pc -> {
                        if (pc.getDistance(_valakas) <= _distance) {
                            pc.enterMovieMode();
                            pc.specialCamera(_valakas, 200, 250, 70, 0, 15000, 30, 80, 1, 0);
                        } else {
                            pc.leaveMovieMode();
                        }
                    });
                    _socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(7), 1L);
                    break;
                }
                case 7: {
                    _players.forEach(pc -> {
                        if (pc.getDistance(_valakas) <= _distance) {
                            pc.enterMovieMode();
                            pc.specialCamera(_valakas, 1100, 250, 70, 2500, 15000, 30, 80, 1, 0);
                        } else {
                            pc.leaveMovieMode();
                        }
                    });
                    _socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(8), 3200L);
                    break;
                }
                case 8: {
                    _players.forEach(pc -> {
                        if (pc.getDistance(_valakas) <= _distance) {
                            pc.enterMovieMode();
                            pc.specialCamera(_valakas, 700, 150, 30, 0, 15000, -10, 60, 1, 0);
                        } else {
                            pc.leaveMovieMode();
                        }
                    });
                    _socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(9), 1400L);
                    break;
                }
                case 9: {
                    _players.forEach(pc -> {
                        if (pc.getDistance(_valakas) <= _distance) {
                            pc.enterMovieMode();
                            pc.specialCamera(_valakas, 1200, 150, 20, 2900, 15000, -10, 30, 1, 0);
                        } else {
                            pc.leaveMovieMode();
                        }
                    });
                    _socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(10), 6700L);
                    break;
                }
                case 10: {
                    _players.forEach(pc -> {
                        if (pc.getDistance(_valakas) <= _distance) {
                            pc.enterMovieMode();
                            pc.specialCamera(_valakas, 750, 170, -10, 3400, 15000, 10, -15, 1, 0);
                        } else {
                            pc.leaveMovieMode();
                        }
                    });
                    _socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(11), 5700L);
                    break;
                }
                case 11: {
                    _players.forEach(Player::leaveMovieMode);
                    _valakas.unblock();
                    broadcastCustomScreenMessage("ValakasFool");
                    if (_valakas.getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE) {
                        _valakas.moveToLocation(new Location(Rnd.get(211080, 214909), Rnd.get(-115841, -112822), -1662, 0), 0, false);
                    }
                    _sleepCheckTask = ThreadPoolManager.getInstance().schedule(new CheckLastAttack(), 600000L);
                    break;
                }
                case 12: {
                    _valakas.broadcastPacket(new PlaySound(Type.MUSIC, "B03_D", 1, _valakas.getObjectId(), _valakas.getLoc()));
                    broadcastCustomScreenMessage("VALAKAS_THE_EVIL_FIRE_DRAGON_VALAKAS_DEFEATED");
                    onValakasDie();
                    _players.forEach(pc -> {
                        if (pc.getDistance(_valakas) <= _distance) {
                            pc.enterMovieMode();
                            pc.specialCamera(_valakas, 2000, 130, -1, 0, 15000, 0, 0, 1, 1);
                        } else {
                            pc.leaveMovieMode();
                        }
                    });
                    _socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(13), 500L);
                    break;
                }
                case 13: {
                    _players.forEach(pc -> {
                        if (pc.getDistance(_valakas) <= _distance) {
                            pc.enterMovieMode();
                            pc.specialCamera(_valakas, 1100, 210, -5, 3000, 15000, -13, 0, 1, 1);
                        } else {
                            pc.leaveMovieMode();
                        }
                    });
                    _socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(14), 3500L);
                    break;
                }
                case 14: {
                    _players.forEach(pc -> {
                        if (pc.getDistance(_valakas) <= _distance) {
                            pc.enterMovieMode();
                            pc.specialCamera(_valakas, 1300, 200, -8, 3000, 15000, 0, 15, 1, 1);
                        } else {
                            pc.leaveMovieMode();
                        }
                    });
                    _socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(15), 4500L);
                    break;
                }
                case 15: {
                    _players.forEach(pc -> {
                        if (pc.getDistance(_valakas) <= _distance) {
                            pc.enterMovieMode();
                            pc.specialCamera(_valakas, 1000, 190, 0, 500, 15000, 0, 10, 1, 1);
                        } else {
                            pc.leaveMovieMode();
                        }
                    });
                    _socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(16), 500L);
                    break;
                }
                case 16: {
                    _players.forEach(pc -> {
                        if (pc.getDistance(_valakas) <= _distance) {
                            pc.enterMovieMode();
                            pc.specialCamera(_valakas, 1700, 120, 0, 2500, 15000, 12, 40, 1, 1);
                        } else {
                            pc.leaveMovieMode();
                        }
                    });
                    _socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(17), 4600L);
                    break;
                }
                case 17: {
                    _players.forEach(pc -> {
                        if (pc.getDistance(_valakas) <= _distance) {
                            pc.enterMovieMode();
                            pc.specialCamera(_valakas, 1700, 20, 0, 700, 15000, 10, 10, 1, 1);
                        } else {
                            pc.leaveMovieMode();
                        }
                    });
                    _socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(18), 750L);
                    break;
                }
                case 18: {
                    _players.forEach(pc -> {
                        if (pc.getDistance(_valakas) <= _distance) {
                            pc.enterMovieMode();
                            pc.specialCamera(_valakas, 1700, 10, 0, 1000, 15000, 20, 70, 1, 1);
                        } else {
                            pc.leaveMovieMode();
                        }
                    });
                    _socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(19), 2500L);
                    break;
                }
                case 19: {
                    _players.forEach(Player::leaveMovieMode);
                    break;
                }
            }
        }
    }
}
