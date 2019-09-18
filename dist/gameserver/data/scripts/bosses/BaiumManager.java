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
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.listener.actor.OnDeathListener;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.model.actor.listener.CharListenerList;
import ru.j2dev.gameserver.model.instances.BossInstance;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.Earthquake;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PlaySound;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PlaySound.Type;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SocialAction;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.Log;
import ru.j2dev.gameserver.utils.ReflectionUtils;
import ru.j2dev.gameserver.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class BaiumManager extends Functions implements OnInitScriptListener {
    private static final int BAIUM_NPC_ID = 29020;
    private static final Logger LOGGER = LoggerFactory.getLogger(BaiumManager.class);
    private static final int ARCHANGEL = 29021;
    private static final int BAIUM_NPC = 29025;
    private static final Location[] ANGEL_LOCATION = {new Location(113004, 16209, 10076, 60242), new Location(114053, 16642, 10076, 4411), new Location(114563, 17184, 10076, 49241), new Location(116356, 16402, 10076, 31109), new Location(115015, 16393, 10076, 32760), new Location(115481, 15335, 10076, 16241), new Location(114680, 15407, 10051, 32485), new Location(114886, 14437, 10076, 16868), new Location(115391, 17593, 10076, 55346), new Location(115245, 17558, 10076, 35536)};
    private static final Location CUBE_LOCATION = new Location(115203, 16620, 10078, 0);
    private static final Location STATUE_LOCATION = new Location(116026, 17426, 10106, 37604);
    private static final int TELEPORT_CUBE = 31759;
    private static final List<NpcInstance> _monsters = new ArrayList<>();
    private static final Map<Integer, SimpleSpawner> _monsterSpawn = new ConcurrentHashMap<>();
    private static final List<NpcInstance> _angels = new ArrayList<>();
    private static final List<SimpleSpawner> _angelSpawns = new ArrayList<>();
    private static final AtomicBoolean Dying = new AtomicBoolean(false);
    private static ScheduledFuture<?> _callAngelTask;
    private static ScheduledFuture<?> _cubeSpawnTask;
    private static ScheduledFuture<?> _intervalEndTask;
    private static ScheduledFuture<?> _killPcTask;
    private static ScheduledFuture<?> _mobiliseTask;
    private static ScheduledFuture<?> _moveAtRandomTask;
    private static ScheduledFuture<?> _sleepCheckTask;
    private static ScheduledFuture<?> _socialTask;
    private static ScheduledFuture<?> _socialTask2;
    private static ScheduledFuture<?> _onAnnihilatedTask;
    private static EpicBossState _state;
    private static long _lastAttackTime;
    private static SimpleSpawner _statueSpawn;
    private static NpcInstance _teleportCube;
    private static SimpleSpawner _teleportCubeSpawn;

    private static Zone _zone;

    public static EpicBossState getEpicBossState() {
        return _state;
    }

    private static void banishForeigners() {
        getPlayersInside().forEach(Player::teleToClosestTown);
    }

    private static synchronized void checkAnnihilated() {
        if (_onAnnihilatedTask == null && isPlayersAnnihilated()) {
            _onAnnihilatedTask = ThreadPoolManager.getInstance().schedule(new onAnnihilated(), Config.BAIUM_CLEAR_ZONE_IF_ALL_DIE);
        }
    }

    private static void deleteArchangels() {
        _angels.stream().filter(angel -> angel != null && angel.getSpawn() != null).forEach(angel -> {
            angel.getSpawn().stopRespawn();
            angel.deleteMe();
        });
        _angels.clear();
    }

    private static List<Player> getPlayersInside() {
        return getZone().getInsidePlayers();
    }

    public static Zone getZone() {
        return _zone;
    }

    private static boolean isPlayersAnnihilated() {
        return getPlayersInside().stream().allMatch(Player::isDead);
    }

    public static void onBaiumDie(final Creature self) {
        if (!Dying.compareAndSet(false, true)) {
            return;
        }
        self.broadcastPacket(new PlaySound(Type.MUSIC, "BS02_D", 1, 0, self.getLoc()));
        _state.setRespawnDate(getRespawnInterval());
        _state.setState(State.INTERVAL);
        _state.update();
        scheduleIntervalEnd();
        Log.add("Baium died", "bosses");
        deleteArchangels();
        _cubeSpawnTask = ThreadPoolManager.getInstance().schedule(new CubeSpawn(), 10000L);
    }

    private static long getRespawnInterval() {
        SchedulingPattern timePattern;
        if (!Config.FWA_FIXTIMEPATTERNOFBAIUM.isEmpty()) {
            final long now = System.currentTimeMillis();
            try {
                timePattern = new SchedulingPattern(Config.FWA_FIXTIMEPATTERNOFBAIUM);
                final long delay = timePattern.next(now) - now;
                return Math.max(60000L, delay);
            } catch (InvalidPatternException ipe) {
                throw new RuntimeException("Invalid respawn data \"" + Config.FWA_FIXTIMEPATTERNOFBAIUM + "\" in " + BaiumManager.class.getSimpleName(), ipe);
            }
        }
        return (long) (Config.ALT_RAID_RESPAWN_MULTIPLIER * (Config.FWB_FIXINTERVALOFBAIUM + Rnd.get(0L, Config.FWB_RANDOMINTERVALOFBAIUM)));
    }

    private static void setIntervalEndTask() {
        setUnspawn();
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

    public static void setLastAttackTime() {
        _lastAttackTime = System.currentTimeMillis();
    }

    public static void setUnspawn() {
        banishForeigners();
        deleteArchangels();
        _monsters.forEach(mob -> {
            mob.getSpawn().stopRespawn();
            mob.deleteMe();
        });
        _monsters.clear();
        if (_teleportCube != null) {
            _teleportCube.getSpawn().stopRespawn();
            _teleportCube.deleteMe();
            _teleportCube = null;
        }
        if (_cubeSpawnTask != null) {
            _cubeSpawnTask.cancel(false);
            _cubeSpawnTask = null;
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
        if (_socialTask2 != null) {
            _socialTask2.cancel(false);
            _socialTask2 = null;
        }
        if (_killPcTask != null) {
            _killPcTask.cancel(false);
            _killPcTask = null;
        }
        if (_callAngelTask != null) {
            _callAngelTask.cancel(false);
            _callAngelTask = null;
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

    private static void sleepBaium() {
        setUnspawn();
        Log.add("Baium going to sleep, spawning statue", "bosses");
        _state.setState(State.NOTSPAWN);
        _state.update();
        _statueSpawn.doSpawn(true);
    }

    public static void spawnBaium(final NpcInstance npcBaium, final Player awake_by) {
        Dying.set(false);
        final SimpleSpawner baiumSpawn = _monsterSpawn.get(BAIUM_NPC_ID);
        baiumSpawn.setLoc(npcBaium.getLoc());
        npcBaium.getSpawn().stopRespawn();
        npcBaium.deleteMe();
        final BossInstance baium = (BossInstance) baiumSpawn.doSpawn(true);
        _monsters.add(baium);
        _state.setRespawnDate(getRespawnInterval());
        _state.setState(State.ALIVE);
        _state.update();
        Log.add("Spawned Baium, awake by: " + awake_by, "bosses");
        setLastAttackTime();
        baium.startImmobilized();
        baium.broadcastPacket(new PlaySound(Type.MUSIC, "BS02_A", 1, 0, baium.getLoc()));
        baium.broadcastPacket(new SocialAction(baium.getObjectId(), 2));
        _socialTask = ThreadPoolManager.getInstance().schedule(new Social(baium, 3), 15000L);
        ThreadPoolManager.getInstance().schedule(new EarthquakeTask(baium), 25000L);
        _socialTask2 = ThreadPoolManager.getInstance().schedule(new Social(baium, 1), 25000L);
        _killPcTask = ThreadPoolManager.getInstance().schedule(new KillPc(awake_by, baium), 26000L);
        _callAngelTask = ThreadPoolManager.getInstance().schedule(new CallArchAngel(), 35000L);
        _mobiliseTask = ThreadPoolManager.getInstance().schedule(new SetMobilised(baium), 35500L);
        final Location pos = new Location(Rnd.get(112826, 116241), Rnd.get(15575, 16375), 10078, 0);
        _moveAtRandomTask = ThreadPoolManager.getInstance().schedule(new MoveAtRandom(baium, pos), 36000L);
        _sleepCheckTask = ThreadPoolManager.getInstance().schedule(new CheckLastAttack(), 600000L);
    }

    private void init() {
        _state = new EpicBossState(29020);
        _zone = ReflectionUtils.getZone("[baium_epic]");
        CharListenerList.addGlobal(new OnDeathListenerImpl());
        try {
            (_statueSpawn = new SimpleSpawner(NpcTemplateHolder.getInstance().getTemplate(BAIUM_NPC))).setAmount(1);
            _statueSpawn.setLoc(STATUE_LOCATION);
            _statueSpawn.stopRespawn();
            final SimpleSpawner tempSpawn = new SimpleSpawner(NpcTemplateHolder.getInstance().getTemplate(29020));
            tempSpawn.setAmount(1);
            _monsterSpawn.put(29020, tempSpawn);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            final NpcTemplate Cube = NpcTemplateHolder.getInstance().getTemplate(TELEPORT_CUBE);
            (_teleportCubeSpawn = new SimpleSpawner(Cube)).setAmount(1);
            _teleportCubeSpawn.setLoc(CUBE_LOCATION);
            _teleportCubeSpawn.setRespawnDelay(60);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            final NpcTemplate angel = NpcTemplateHolder.getInstance().getTemplate(ARCHANGEL);
            _angelSpawns.clear();
            final List<Integer> random = new ArrayList<>();
            for (int i = 0; i < 5; ++i) {
                int r;
                r = -1;
                while (r == -1 || random.contains(r)) {
                    r = Rnd.get(10);
                }
                random.add(r);
            }
            random.forEach(j -> {
                final SimpleSpawner spawnDat = new SimpleSpawner(angel);
                spawnDat.setAmount(1);
                spawnDat.setLoc(ANGEL_LOCATION[j]);
                spawnDat.setRespawnDelay(300000);
                _angelSpawns.add(spawnDat);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("BaiumManager: State of Baium is " + _state.getState() + ".");
        switch (_state.getState()) {
            case NOTSPAWN:
                _statueSpawn.doSpawn(true);
                break;
            case ALIVE:
                _state.setState(State.NOTSPAWN);
                _state.update();
                _statueSpawn.doSpawn(true);
                break;
            case INTERVAL:
            case DEAD:
                setIntervalEndTask();
                break;
        }
        LOGGER.info("BaiumManager: Next spawn date: " + TimeUtils.toSimpleFormat(_state.getRespawnDate()));
    }

    private class OnDeathListenerImpl implements OnDeathListener {

        @Override
        public void onDeath(final Creature self, final Creature killer) {
            if (self.isPlayer() && _state != null && _state.getState() == State.ALIVE && _zone != null && _zone.checkIfInZone(self)) {
                checkAnnihilated();
            } else if (self.isNpc() && self.getNpcId() == 29020) {
                onBaiumDie(self);
            }
        }
    }

    @Override
    public void onInit() {
        init();
    }

    public static class CallArchAngel extends RunnableImpl {
        @Override
        public void runImpl() {
            _angelSpawns.forEach(spawn -> _angels.add(spawn.doSpawn(true)));
        }
    }

    public static class CheckLastAttack extends RunnableImpl {
        @Override
        public void runImpl() {
            if (_state.getState() == State.ALIVE) {
                if (_lastAttackTime + Config.FWB_LIMITUNTILSLEEPBAIUM < System.currentTimeMillis()) {
                    sleepBaium();
                } else {
                    _sleepCheckTask = ThreadPoolManager.getInstance().schedule(new CheckLastAttack(), 60000L);
                }
            }
        }
    }

    public static class CubeSpawn extends RunnableImpl {
        @Override
        public void runImpl() {
            _teleportCube = _teleportCubeSpawn.doSpawn(true);
        }
    }

    public static class IntervalEnd extends RunnableImpl {
        @Override
        public void runImpl() {
            setUnspawn();
            _state.setState(State.NOTSPAWN);
            _state.update();
            _statueSpawn.doSpawn(true);
        }
    }

    public static class KillPc extends RunnableImpl {
        private final BossInstance _boss;
        private final Player _target;

        public KillPc(final Player target, final BossInstance boss) {
            _target = target;
            _boss = boss;
        }

        @Override
        public void runImpl() {
            final Skill skill = SkillTable.getInstance().getInfo(4136, 1);
            if (_target != null && skill != null) {
                _boss.setTarget(_target);
                _boss.doCast(skill, _target, false);
            }
        }
    }

    public static class MoveAtRandom extends RunnableImpl {
        private final NpcInstance _npc;
        private final Location _pos;

        public MoveAtRandom(final NpcInstance npc, final Location pos) {
            _npc = npc;
            _pos = pos;
        }

        @Override
        public void runImpl() {
            if (_npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE) {
                _npc.moveToLocation(_pos, 0, false);
            }
        }
    }

    public static class SetMobilised extends RunnableImpl {
        private final BossInstance _boss;

        public SetMobilised(final BossInstance boss) {
            _boss = boss;
        }

        @Override
        public void runImpl() {
            _boss.stopImmobilized();
        }
    }

    public static class Social extends RunnableImpl {
        private final int _action;
        private final NpcInstance _npc;

        public Social(final NpcInstance npc, final int actionId) {
            _npc = npc;
            _action = actionId;
        }

        @Override
        public void runImpl() {
            final SocialAction sa = new SocialAction(_npc.getObjectId(), _action);
            _npc.broadcastPacket(sa);
        }
    }

    public static class onAnnihilated extends RunnableImpl {
        @Override
        public void runImpl() {
            synchronized (BaiumManager.class) {
                if (!isPlayersAnnihilated() || _state.getState() == State.INTERVAL) {
                    _onAnnihilatedTask = null;
                    return;
                }
            }
            sleepBaium();
        }
    }

    public static class EarthquakeTask extends RunnableImpl {
        private final BossInstance baium;

        public EarthquakeTask(final BossInstance _baium) {
            baium = _baium;
        }

        @Override
        public void runImpl() {
            final Earthquake eq = new Earthquake(baium.getLoc(), 40, 5);
            baium.broadcastPacket(eq);
        }
    }
}
