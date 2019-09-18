package bosses;

import bosses.EpicBossState.State;
import handler.items.SimpleItemHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.listener.Listener;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.time.cron.SchedulingPattern;
import ru.j2dev.commons.time.cron.SchedulingPattern.InvalidPatternException;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.handler.admincommands.AdminCommandHandler;
import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.handler.items.ItemHandler;
import ru.j2dev.gameserver.listener.CharListener;
import ru.j2dev.gameserver.listener.actor.OnCurrentHpDamageListener;
import ru.j2dev.gameserver.listener.actor.OnDeathListener;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.listener.zone.OnZoneEnterLeaveListener;
import ru.j2dev.gameserver.manager.SpawnManager;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.model.instances.DoorInstance;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.*;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.NpcUtils;
import ru.j2dev.gameserver.utils.ReflectionUtils;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FrintezzaManager implements OnInitScriptListener, IAdminCommandHandler {
    private static final int BREAKING_ARROW_ITEM_ID = 8192;
    private static final int DEWDROP_OF_DESSTRUCTION_ID = 8556;
    private static final int FRINTEZZA_SEEKER_NPC_ID = 29059;
    private static final long BREAKING_ARROW_CANCEL_TIME = 60000L;
    private static final Logger LOGGER = LoggerFactory.getLogger(FrintezzaManager.class);
    private static final FrintezzaManager INSTANCE = new FrintezzaManager();
    private static final Location[] TELEPORT_PARTY_LOCATIONS = {new Location(173247, -76979, -5104, 1000), new Location(174985, -75161, -5104, 1000), new Location(173917, -76109, -5104, 1000), new Location(174037, -76321, -5104, 1000), new Location(173249, -75154, -5104, 1000)};
    private static final Location FRINTEZZA_SEEKER_LOCATION = new Location(174259, -86550, -5088, 16440);
    private static final String LAST_IMPERIAL_TOMB_ZONE_NAME = "[LastImperialTomb]";
    private static final String FRINTESSA_ZONE_NAME = "[Frintezza]";
    private static final String ROOM_A_ALARM_DEVICES_NAME = "[last_imperial_tomb_a_alarm_devices]";
    private static final String ROOM_A_HALL_KEEPERS_NAME = "[last_imperial_tomb_a_hall_keeper]";
    private static final String ROOM_B_UNDEADBAND_PLAYERS = "[last_imperial_tomb_b_undeadband_players]";
    private static final String ROOM_B_UNDEADBAND_MANSTER = "[last_imperial_tomb_b_undeadband_masters]";
    private static final String ROOM_B_UNDEADBAND = "[last_imperial_tomb_b_undeadband_member]";
    private static final long battleStartDelay = 300000L;
    private static final int _intervalOfFrintezzaSongs = 30000;
    private static final NpcLocation scarletSpawnWeak = new NpcLocation(174234, -88015, -5116, 48028, 29046);
    private static final NpcLocation[] portraitSpawns = {new NpcLocation(175880, -88696, -5104, 35048, 29048), new NpcLocation(175816, -87160, -5104, 28205, 29049), new NpcLocation(172648, -87176, -5104, 64817, 29048), new NpcLocation(172600, -88664, -5104, 57730, 29049)};
    private static final NpcLocation[] demonSpawns = {new NpcLocation(175880, -88696, -5104, 35048, 29050), new NpcLocation(175816, -87160, -5104, 28205, 29051), new NpcLocation(172648, -87176, -5104, 64817, 29051), new NpcLocation(172600, -88664, -5104, 57730, 29050)};
    public static int FRINTEZZA_NPC_ID = 29045;
    private static final NpcLocation frintezzaSpawn = new NpcLocation(174240, -89805, -5022, 16048, FRINTEZZA_NPC_ID);
    private final List<Spawner> _roomAAlarmDevices = new ArrayList<>();
    private final List<Spawner> _roomAHallKeepers = new ArrayList<>();
    private final List<Spawner> _roomBUndeadBandPlayers = new ArrayList<>();
    private final List<Spawner> _roomBUndeadBandMasters = new ArrayList<>();
    private final List<Spawner> _roomBUndeadBandMembers = new ArrayList<>();
    private final List<DoorInstance> _gravePathwayA = new ArrayList<>();
    private final List<DoorInstance> _gravePathwayB = new ArrayList<>();
    private final List<DoorInstance> _wallDoorA = new ArrayList<>();
    private final List<DoorInstance> _wallDoorB = new ArrayList<>();
    private final NpcInstance[] portraits = new NpcInstance[4];
    private final NpcInstance[] demons = new NpcInstance[4];
    private EpicBossState _state;
    private Skill _arrowSkill;
    private Zone _lastImperialTombZone = ReflectionUtils.getZone(LAST_IMPERIAL_TOMB_ZONE_NAME);
    private Zone _frintessaZone = ReflectionUtils.getZone(FRINTESSA_ZONE_NAME);
    private Zone _areadataHeal1 = ReflectionUtils.getZone("[25_15_frintessa_01_01]");
    private Zone _areadataHeal2 = ReflectionUtils.getZone("[25_15_frintessa_02_01]");
    private Zone _areadataPower1 = ReflectionUtils.getZone("[25_15_frintessa_01_02]");
    private Zone _areadataPower2 = ReflectionUtils.getZone("[25_15_frintessa_02_02]");
    private Zone _areadataPsycho1 = ReflectionUtils.getZone("[25_15_frintessa_01_03]");
    private Zone _areadataPsycho2 = ReflectionUtils.getZone("[25_15_frintessa_02_03]");
    private Zone _areadataRampage1 = ReflectionUtils.getZone("[25_15_frintessa_01_04]");
    private Zone _areadataRampage2 = ReflectionUtils.getZone("[25_15_frintessa_02_04]");
    private Zone _areadataPlague1 = ReflectionUtils.getZone("[25_15_frintessa_01_05]");
    private Zone _areadataPlague2 = ReflectionUtils.getZone("[25_15_frintessa_02_05]");
    private OnDeathListenerImpl _deathListener = new OnDeathListenerImpl();
    private OnZoneEnterLeaveListenerImpl _frintessaEnterListener = new OnZoneEnterLeaveListenerImpl();
    private OnCurrentHpDamageListenerImpl _currentHpListener = new OnCurrentHpDamageListenerImpl();
    private AtomicInteger _progress = new AtomicInteger(0);
    private NpcInstance _frintezzaDummy;
    private NpcInstance frintezza;
    private NpcInstance weakScarlet;
    private NpcInstance strongScarlet;
    private Future<?> _intervalEndTask;
    private Future<?> _frintezzaSpawnTask;
    private int _scarletMorph;
    private ScheduledFuture<?> musicTask;

    public static FrintezzaManager getInstance() {
        return INSTANCE;
    }

    private static long getRespawnInterval() {
        SchedulingPattern timePattern;
        if (!Config.FWA_FIXTIMEPATTERNOFFRINTEZZA.isEmpty()) {
            final long now = System.currentTimeMillis();
            try {
                timePattern = new SchedulingPattern(Config.FWA_FIXTIMEPATTERNOFFRINTEZZA);
                final long delay = timePattern.next(now) - now;
                return Math.max(60000L, delay);
            } catch (InvalidPatternException ipe) {
                throw new RuntimeException("Invalid respawn data \"" + Config.FWA_FIXTIMEPATTERNOFFRINTEZZA + "\" in " + FrintezzaManager.class.getSimpleName(), ipe);
            }
        }
        return (long) (Config.ALT_RAID_RESPAWN_MULTIPLIER * (Config.FWF_FIXINTERVALOFFRINTEZZA + Rnd.get(0L, Config.FWF_RANDOMINTERVALOFFRINTEZZA)));
    }

    public EpicBossState getEpicBossState() {
        return _state;
    }

    private void init() {
        _state = new EpicBossState(FRINTEZZA_NPC_ID);
        LOGGER.info("FrintezzaManager: State of Frintezza is " + _state.getState() + ".");
        if (State.NOTSPAWN != _state.getState()) {
            setIntervalEndTask();
        }
        final Date dt = new Date(_state.getRespawnDate());
        LOGGER.info("FrintezzaManager: Next spawn date of Frintezza is " + dt + ".");
        IntStream.rangeClosed(25150042, 25150043).mapToObj(ReflectionUtils::getDoor).forEach(_gravePathwayA::add);
        IntStream.rangeClosed(25150045, 25150046).mapToObj(ReflectionUtils::getDoor).forEach(_gravePathwayB::add);
        IntStream.rangeClosed(25150051, 25150058).mapToObj(ReflectionUtils::getDoor).forEach(_wallDoorA::add);
        IntStream.rangeClosed(25150061, 25150070).mapToObj(ReflectionUtils::getDoor).forEach(_wallDoorB::add);
        _roomAAlarmDevices.addAll(SpawnManager.getInstance().getSpawners(ROOM_A_ALARM_DEVICES_NAME));
        _roomAHallKeepers.addAll(SpawnManager.getInstance().getSpawners(ROOM_A_HALL_KEEPERS_NAME));
        _roomBUndeadBandPlayers.addAll(SpawnManager.getInstance().getSpawners(ROOM_B_UNDEADBAND_PLAYERS));
        _roomBUndeadBandMasters.addAll(SpawnManager.getInstance().getSpawners(ROOM_B_UNDEADBAND_MANSTER));
        _roomBUndeadBandMembers.addAll(SpawnManager.getInstance().getSpawners(ROOM_B_UNDEADBAND));
        _arrowSkill = SkillTable.getInstance().getInfo(2234, 1);
        _frintessaZone.addListener(_frintessaEnterListener);
        ItemHandler.getInstance().registerItemHandler(new FrintezzaItemHandler());
    }

    private void setIntervalEndTask() {
        if (State.INTERVAL != _state.getState()) {
            _state.setRespawnDate(getRespawnInterval());
            _state.setState(State.INTERVAL);
            _state.update();
        }
        _intervalEndTask = ThreadPoolManager.getInstance().schedule(new IntervalEnd(), _state.getInterval());
    }

    private void done() {
    }

    public boolean isInUse() {
        return _progress.get() != 0;
    }

    public boolean canEnter() {
        return !isInUse() && _state.getState() == State.NOTSPAWN;
    }

    public boolean tryEnter(final List<Party> parties) {
        try {
            if (!startUp()) {
                return false;
            }
            IntStream.range(0, parties.size()).forEach(i -> {
                final Party party = parties.get(i);
                final Location teleLoc = TELEPORT_PARTY_LOCATIONS[i % TELEPORT_PARTY_LOCATIONS.length];
                party.getPartyMembers().forEach(member -> {
                    preparePlayableToEnter(member);
                    final Location loc = Location.findPointToStay(teleLoc, 256, member.getGeoIndex());
                    member.teleToLocation(loc);
                });
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return true;
    }

    public boolean startUp() {
        try {
            if (_progress.compareAndSet(0, 1)) {
                banishForeigners();
                SpawnManager.getInstance().despawn("[frintezza_teleporter]");
                cleanupPrev();
                closeDoors(_wallDoorA);
                closeDoors(_gravePathwayA);
                removeListenerFromAllSpawned(_roomAAlarmDevices, _deathListener);
                despawn(_roomAAlarmDevices);
                despawn(_roomAHallKeepers);
                removeListenerFromAllSpawned(_roomBUndeadBandPlayers, _deathListener);
                despawn(_roomBUndeadBandPlayers);
                removeListenerFromAllSpawned(_roomBUndeadBandMasters, _deathListener);
                despawn(_roomBUndeadBandMasters);
                despawn(_roomBUndeadBandMembers);
                spawn(_roomAAlarmDevices);
                stopRespawn(_roomAAlarmDevices);
                addListenerToAllSpawned(_roomAAlarmDevices, _deathListener);
                spawn(_roomAHallKeepers);
                Future<?> _timeoutTask = ThreadPoolManager.getInstance().schedule(new TombTimeoutTask(), 5000L);
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private void cleanupPrev() {
        if (_frintezzaDummy != null) {
            _frintezzaDummy.deleteMe();
            _frintezzaDummy = null;
        }
        if (frintezza != null) {
            frintezza.deleteMe();
            frintezza = null;
        }
        if (weakScarlet != null) {
            weakScarlet.deleteMe();
            weakScarlet = null;
        }
        if (strongScarlet != null) {
            strongScarlet.deleteMe();
            strongScarlet = null;
        }
        IntStream.range(0, portraits.length).filter(pidx -> portraits[pidx] != null).forEach(pidx -> {
            portraits[pidx].deleteMe();
            portraits[pidx] = null;
        });
        IntStream.range(0, demons.length).filter(didx -> demons[didx] != null).forEach(didx -> {
            demons[didx].deleteMe();
            demons[didx] = null;
        });
        _lastImperialTombZone.getInsideNpcs().stream().filter(Objects::nonNull).forEach(GameObject::deleteMe);
        if (musicTask != null) {
            musicTask.cancel(true);
            musicTask = null;
        }
        if (_frintezzaSpawnTask != null) {
            _frintezzaSpawnTask.cancel(true);
            _frintezzaSpawnTask = null;
        }
        disableMusicZones();
    }

    public boolean cleanUp() {
        if (_progress.get() != 0) {
            banishForeigners();
            removeListenerFromAllSpawned(_roomAAlarmDevices, _deathListener);
            despawn(_roomAAlarmDevices);
            despawn(_roomAHallKeepers);
            removeListenerFromAllSpawned(_roomBUndeadBandPlayers, _deathListener);
            despawn(_roomBUndeadBandPlayers);
            removeListenerFromAllSpawned(_roomBUndeadBandMasters, _deathListener);
            despawn(_roomBUndeadBandMasters);
            despawn(_roomBUndeadBandMembers);
            closeDoors(_wallDoorA);
            closeDoors(_gravePathwayA);
            closeDoors(_wallDoorB);
            closeDoors(_gravePathwayB);
            cleanupPrev();
            if (_intervalEndTask != null) {
                _intervalEndTask.cancel(true);
                _intervalEndTask = null;
            }
            if (_state.getState() == State.ALIVE || (_progress.get() > 0 && _progress.get() < 5)) {
                _state.setState(State.NOTSPAWN);
                _state.update();
            } else {
                setIntervalEndTask();
            }
            _progress.set(0);
            return true;
        }
        return false;
    }

    private void openDoors(final List<DoorInstance> doors) {
        doors.stream().filter(Objects::nonNull).forEach(DoorInstance::openMe);
    }

    private void closeDoors(final List<DoorInstance> doors) {
        doors.stream().filter(Objects::nonNull).forEach(DoorInstance::closeMe);
    }

    private void spawn(final List<Spawner> spawners) {
        spawners.forEach(Spawner::init);
    }

    private void stopRespawn(final List<Spawner> spawners) {
        spawners.forEach(Spawner::stopRespawn);
    }

    private void despawn(final List<Spawner> spawners) {
        spawners.forEach(Spawner::deleteAll);
    }

    private List<NpcInstance> getAllSpawned(final List<Spawner> spawners) {
        final List<NpcInstance> result = new ArrayList<>();
        spawners.stream().map(Spawner::getAllSpawned).forEach(result::addAll);
        return result;
    }

    private boolean isNpcOfSpawn(final NpcInstance npc, final List<Spawner> spawners) {
        final Spawner npcSpawner = npc.getSpawn();
        return spawners.stream().anyMatch(spawner -> npcSpawner == spawner);
    }

    private List<NpcInstance> getAliveSpawned(final List<Spawner> spawners) {
        return spawners.stream().map(Spawner::getAllSpawned).flatMap(Collection::stream).filter(spanedNpc -> !spanedNpc.isDead()).collect(Collectors.toList());
    }

    private boolean isAllSpawnedDead(final List<Spawner> spawners) {
        return spawners.stream().map(Spawner::getAllSpawned).flatMap(Collection::stream).allMatch(Creature::isDead);
    }

    private void addListenerToAllSpawned(final List<Spawner> spawners, final CharListener listener) {
        final List<NpcInstance> allSpawned = getAllSpawned(spawners);
        allSpawned.forEach(spawnedNpc -> spawnedNpc.addListener((Listener) listener));
    }

    private void removeListenerFromAllSpawned(final List<Spawner> spawners, final CharListener listener) {
        final List<NpcInstance> allSpawned = getAllSpawned(spawners);
        allSpawned.forEach(spawnedNpc -> spawnedNpc.removeListener((Listener) listener));
    }

    private void onRoomAAlarmDeviceDied(final NpcInstance device) {
        if (isAllSpawnedDead(_roomAAlarmDevices)) {
            if (_progress.compareAndSet(1, 2)) {
                removeListenerFromAllSpawned(_roomAAlarmDevices, _deathListener);
                despawn(_roomAHallKeepers);
                openDoors(_wallDoorA);
                openDoors(_gravePathwayA);
                Functions.npcShoutCustomMessage(device, "LastImperialTomb.DeactivateTheAlarm");
                removeListenerFromAllSpawned(_roomAAlarmDevices, _deathListener);
                closeDoors(_wallDoorB);
                closeDoors(_gravePathwayB);
                spawn(_roomBUndeadBandPlayers);
                stopRespawn(_roomBUndeadBandPlayers);
                addListenerToAllSpawned(_roomBUndeadBandPlayers, _deathListener);
                spawn(_roomBUndeadBandMasters);
                stopRespawn(_roomBUndeadBandMasters);
                addListenerToAllSpawned(_roomBUndeadBandMasters, _deathListener);
                spawn(_roomBUndeadBandMembers);
            }
        } else {
            Functions.npcShoutCustomMessage(device, "LastImperialTomb.Intruders");
        }
    }

    private void undeadBandMasterRandomShout() {
        final List<NpcInstance> undeadBandAliveMasters = getAliveSpawned(_roomBUndeadBandMasters);
        if (!undeadBandAliveMasters.isEmpty()) {
            final NpcInstance rndUndeadBansMaster = undeadBandAliveMasters.get(Rnd.get(undeadBandAliveMasters.size()));
            if (rndUndeadBansMaster != null) {
                Functions.npcShoutCustomMessage(rndUndeadBansMaster, String.format("LastImperialTomb.UndeadBandMasterShout%d", 1 + Rnd.get(3)));
            }
        }
    }

    private void checkUndeadBandAllDead() {
        if (isAllSpawnedDead(_roomBUndeadBandMasters) && _progress.compareAndSet(2, 3)) {
            stopRespawn(_roomBUndeadBandMembers);
            despawn(_roomBUndeadBandMembers);
            removeListenerFromAllSpawned(_roomBUndeadBandMasters, _deathListener);
            despawn(_roomBUndeadBandMasters);
            openDoors(_gravePathwayB);
        }
    }

    private void onRoomBUndeadBandPlayerDied(final NpcInstance undeadBandPlayer) {
        if (isAllSpawnedDead(_roomBUndeadBandPlayers)) {
            closeDoors(_wallDoorA);
            closeDoors(_gravePathwayA);
            openDoors(_wallDoorB);
            undeadBandMasterRandomShout();
            removeListenerFromAllSpawned(_roomBUndeadBandPlayers, _deathListener);
            despawn(_roomBUndeadBandPlayers);
            undeadBandMasterRandomShout();
        }
        checkUndeadBandAllDead();
    }

    private void onRoomBUndeadBandMasterDied(final NpcInstance undeadBandMaster, final Creature lastAttaker) {
        if (lastAttaker != null && lastAttaker.getPlayer() != null && getAliveSpawned(_roomBUndeadBandMasters).size() % 2 == 0) {
            undeadBandMaster.dropItem(lastAttaker.getPlayer(), BREAKING_ARROW_ITEM_ID, 1L);
        }
        checkUndeadBandAllDead();
    }

    private void onEnterFrontessaZone() {
        if (_progress.compareAndSet(3, 4)) {
            _frintezzaSpawnTask = ThreadPoolManager.getInstance().schedule(new Spawn(1), battleStartDelay);
        }
    }

    private void cancelMusic() {
        if (musicTask != null) {
            musicTask.cancel(false);
        }
        if (frintezza != null) {
            frintezza.broadcastPacket(new MagicSkillCanceled(frintezza));
            musicTask = ThreadPoolManager.getInstance().schedule(new Music(1), BREAKING_ARROW_CANCEL_TIME + Rnd.get(10000));
        }
    }

    private void disableMusicZones() {
        _areadataHeal1.setActive(false);
        _areadataHeal2.setActive(false);
        _areadataPower1.setActive(false);
        _areadataPower2.setActive(false);
        _areadataPsycho1.setActive(false);
        _areadataPsycho2.setActive(false);
        _areadataRampage1.setActive(false);
        _areadataRampage2.setActive(false);
        _areadataPlague1.setActive(false);
        _areadataPlague2.setActive(false);
    }

    private NpcInstance spawn(final NpcLocation loc) {
        return NpcUtils.spawnSingle(loc.npcId, loc);
    }

    private void showSocialActionMovie(final NpcInstance target, final int dist, final int yaw, final int pitch, final int time, final int duration, final int socialAction) {
        if (target == null) {
            return;
        }
        _frintessaZone.getInsidePlayers().forEach(pc -> {
            if (pc.getDistance(target) <= 2550.0) {
                pc.enterMovieMode();
                pc.specialCamera(target, dist, yaw, pitch, time, duration);
            } else {
                pc.leaveMovieMode();
            }
        });
        if (socialAction > 0 && socialAction < 5) {
            target.broadcastPacket(new SocialAction(target.getObjectId(), socialAction));
        }
    }

    private void blockAll(final boolean flag) {
        block(frintezza, flag);
        block(weakScarlet, flag);
        block(strongScarlet, flag);
        IntStream.range(0, 4).forEach(i -> {
            block(portraits[i], flag);
            block(demons[i], flag);
        });
    }

    private void block(final NpcInstance npc, final boolean flag) {
        if (npc == null || npc.isDead()) {
            return;
        }
        if (flag) {
            npc.abortAttack(true, false);
            npc.abortCast(true, true);
            npc.setTarget(null);
            if (npc.isMoving()) {
                npc.stopMove();
            }
            npc.block();
        } else {
            npc.unblock();
        }
        npc.setIsInvul(flag);
    }

    public void preparePlayableToEnter(final Playable playable) {
        final long breakingArrowAmount = ItemFunctions.getItemCount(playable, BREAKING_ARROW_ITEM_ID);
        if (breakingArrowAmount > 0L) {
            ItemFunctions.removeItem(playable, BREAKING_ARROW_ITEM_ID, breakingArrowAmount, true);
        }
        final long dewdropCount = ItemFunctions.getItemCount(playable, DEWDROP_OF_DESSTRUCTION_ID);
        if (dewdropCount > 0L) {
            ItemFunctions.removeItem(playable, DEWDROP_OF_DESSTRUCTION_ID, dewdropCount, true);
        }
        if (playable.getPet() != null) {
            preparePlayableToEnter(playable.getPet());
        }
    }

    private void banishForeigners() {
        _lastImperialTombZone.getInsidePlayers().forEach(Player::teleToClosestTown);
    }

    @Override
    public boolean useAdminCommand(final Enum comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        switch (command) {
            case admin_fri_startup: {
                getInstance().startUp();
                break;
            }
            case admin_fri_cleanup: {
                getInstance().cleanUp();
                break;
            }
            case admin_fri_go: {
                activeChar.teleToLocation(TELEPORT_PARTY_LOCATIONS[Rnd.get(TELEPORT_PARTY_LOCATIONS.length)]);
                break;
            }
            case admin_fri_devdump: {
                System.out.println("progress: " + _progress.get());
                System.out.println("room A devices: " + Arrays.deepToString(_roomAAlarmDevices.toArray(new Spawner[0])));
                System.out.println("room A keepers: " + Arrays.deepToString(_roomAHallKeepers.toArray(new Spawner[0])));
                System.out.println("room B Players: " + Arrays.deepToString(_roomBUndeadBandPlayers.toArray(new Spawner[0])));
                System.out.println("room B masters: " + Arrays.deepToString(_roomBUndeadBandMasters.toArray(new Spawner[0])));
                System.out.println("state: " + _state.getState());
                break;
            }
            case admin_fri_musdump: {
                System.out.println("areadataHeal1: " + _areadataHeal1 + " | " + _areadataHeal1.isActive() + " " + _areadataHeal1.getInsidePlayers().size());
                System.out.println("areadataHeal2: " + _areadataHeal2 + " | " + _areadataHeal2.isActive() + " " + _areadataHeal2.getInsidePlayers().size());
                System.out.println("areadataPower1: " + _areadataPower1 + " | " + _areadataPower1.isActive() + " " + _areadataPower1.getInsidePlayers().size());
                System.out.println("areadataPower2: " + _areadataPower2 + " | " + _areadataPower2.isActive() + " " + _areadataPower2.getInsidePlayers().size());
                System.out.println("areadataPsycho1: " + _areadataPsycho1 + " | " + _areadataPsycho1.isActive() + " " + _areadataPsycho1.getInsidePlayers().size());
                System.out.println("areadataPsycho2: " + _areadataPsycho2 + " | " + _areadataPsycho2.isActive() + " " + _areadataPsycho2.getInsidePlayers().size());
                System.out.println("areadataRampage1: " + _areadataRampage1 + " | " + _areadataRampage1.isActive() + " " + _areadataRampage1.getInsidePlayers().size());
                System.out.println("areadataRampage2: " + _areadataRampage2 + " | " + _areadataRampage2.isActive() + " " + _areadataRampage2.getInsidePlayers().size());
                System.out.println("areadataPlague1: " + _areadataPlague1 + " | " + _areadataPlague1.isActive() + " " + _areadataPlague1.getInsidePlayers().size());
                System.out.println("areadataPlague2: " + _areadataPlague2 + " | " + _areadataPlague2.isActive() + " " + _areadataPlague2.getInsidePlayers().size());
                break;
            }
        }
        return false;
    }

    @Override
    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    @Override
    public void onInit() {
        AdminCommandHandler.getInstance().registerAdminCommandHandler(getInstance());
        getInstance().init();
    }

    private enum Commands {
        admin_fri_startup,
        admin_fri_cleanup,
        admin_fri_go,
        admin_fri_devdump,
        admin_fri_musdump
    }

    public static class NpcLocation extends Location {
        public int npcId;

        NpcLocation() {
        }

        NpcLocation(final int x, final int y, final int z, final int heading, final int npcId) {
            super(x, y, z, heading);
            this.npcId = npcId;
        }
    }

    private class IntervalEnd implements Runnable {
        @Override
        public void run() {
            _state.setState(State.NOTSPAWN);
            _state.update();
        }
    }

    private class Spawn extends RunnableImpl {
        private int _taskId;

        public Spawn(final int taskId) {
            _taskId = 0;
            _taskId = taskId;
        }

        @Override
        public void runImpl() {
            try {
                switch (_taskId) {
                    case 1: {
                        _frintezzaDummy = spawn(new NpcLocation(174232, -89816, -5016, 16048, FRINTEZZA_SEEKER_NPC_ID));
                        ThreadPoolManager.getInstance().schedule(new Spawn(2), 1000L);
                        break;
                    }
                    case 2: {
                        _state.setRespawnDate(getRespawnInterval());
                        _state.setState(State.ALIVE);
                        _state.update();
                        _progress.compareAndSet(4, 5);
                        closeDoors(_gravePathwayB);
                        frintezza = spawn(frintezzaSpawn);
                        showSocialActionMovie(frintezza, 500, 90, 0, 6500, 8000, 0);
                        IntStream.range(0, 4).forEach(i -> {
                            (portraits[i] = spawn(portraitSpawns[i])).startImmobilized();
                            demons[i] = spawn(demonSpawns[i]);
                        });
                        blockAll(true);
                        ThreadPoolManager.getInstance().schedule(new Spawn(3), 6500L);
                        break;
                    }
                    case 3: {
                        showSocialActionMovie(_frintezzaDummy, 1800, 90, 8, 6500, 7000, 0);
                        ThreadPoolManager.getInstance().schedule(new Spawn(4), 900L);
                        break;
                    }
                    case 4: {
                        showSocialActionMovie(_frintezzaDummy, 140, 90, 10, 2500, 4500, 0);
                        ThreadPoolManager.getInstance().schedule(new Spawn(5), 4000L);
                        break;
                    }
                    case 5: {
                        showSocialActionMovie(frintezza, 40, 75, -10, 0, 1000, 0);
                        showSocialActionMovie(frintezza, 40, 75, -10, 0, 12000, 0);
                        ThreadPoolManager.getInstance().schedule(new Spawn(6), 1350L);
                        break;
                    }
                    case 6: {
                        frintezza.broadcastPacket(new SocialAction(frintezza.getObjectId(), 2));
                        musicTask = ThreadPoolManager.getInstance().schedule(new Music(0), 5000L);
                        ThreadPoolManager.getInstance().schedule(new Spawn(7), 7000L);
                        break;
                    }
                    case 7: {
                        _frintezzaDummy.deleteMe();
                        _frintezzaDummy = null;
                        ThreadPoolManager.getInstance().schedule(new Spawn(8), 1000L);
                        break;
                    }
                    case 8: {
                        showSocialActionMovie(demons[0], 140, 0, 3, 22000, 3000, 1);
                        ThreadPoolManager.getInstance().schedule(new Spawn(9), 2800L);
                        break;
                    }
                    case 9: {
                        showSocialActionMovie(demons[1], 140, 0, 3, 22000, 3000, 1);
                        ThreadPoolManager.getInstance().schedule(new Spawn(10), 2800L);
                        break;
                    }
                    case 10: {
                        showSocialActionMovie(demons[2], 140, 180, 3, 22000, 3000, 1);
                        ThreadPoolManager.getInstance().schedule(new Spawn(11), 2800L);
                        break;
                    }
                    case 11: {
                        showSocialActionMovie(demons[3], 140, 180, 3, 22000, 3000, 1);
                        ThreadPoolManager.getInstance().schedule(new Spawn(12), 3000L);
                        break;
                    }
                    case 12: {
                        showSocialActionMovie(frintezza, 240, 90, 0, 0, 1000, 0);
                        showSocialActionMovie(frintezza, 240, 90, 25, 5500, 10000, 3);
                        ThreadPoolManager.getInstance().schedule(new Spawn(13), 3000L);
                        break;
                    }
                    case 13: {
                        showSocialActionMovie(frintezza, 100, 195, 35, 0, 10000, 0);
                        ThreadPoolManager.getInstance().schedule(new Spawn(14), 700L);
                        break;
                    }
                    case 14: {
                        showSocialActionMovie(frintezza, 100, 195, 35, 0, 10000, 0);
                        ThreadPoolManager.getInstance().schedule(new Spawn(15), 1300L);
                        break;
                    }
                    case 15: {
                        showSocialActionMovie(frintezza, 120, 180, 45, 1500, 10000, 0);
                        frintezza.broadcastPacket(new MagicSkillUse(frintezza, frintezza, 5006, 1, 34000, 0L));
                        ThreadPoolManager.getInstance().schedule(new Spawn(16), 1500L);
                        break;
                    }
                    case 16: {
                        showSocialActionMovie(frintezza, 520, 135, 45, 8000, 10000, 0);
                        ThreadPoolManager.getInstance().schedule(new Spawn(17), 7500L);
                        break;
                    }
                    case 17: {
                        showSocialActionMovie(frintezza, 1500, 110, 25, 10000, 13000, 0);
                        ThreadPoolManager.getInstance().schedule(new Spawn(18), 9500L);
                        break;
                    }
                    case 18: {
                        weakScarlet = spawn(scarletSpawnWeak);
                        block(weakScarlet, true);
                        weakScarlet.addListener((Listener) _currentHpListener);
                        weakScarlet.broadcastPacket(new MagicSkillUse(weakScarlet, weakScarlet, 5016, 1, 3000, 0L));
                        _frintessaZone.broadcastPacket(new Earthquake(weakScarlet.getLoc(), 50, 6), false);
                        showSocialActionMovie(weakScarlet, 1000, 160, 20, 6000, 6000, 0);
                        ThreadPoolManager.getInstance().schedule(new Spawn(19), 5500L);
                        break;
                    }
                    case 19: {
                        showSocialActionMovie(weakScarlet, 800, 160, 5, 1000, 10000, 2);
                        ThreadPoolManager.getInstance().schedule(new Spawn(20), 2100L);
                        break;
                    }
                    case 20: {
                        showSocialActionMovie(weakScarlet, 300, 60, 8, 0, 10000, 0);
                        ThreadPoolManager.getInstance().schedule(new Spawn(21), 2000L);
                        break;
                    }
                    case 21: {
                        showSocialActionMovie(weakScarlet, 1000, 90, 10, 3000, 5000, 0);
                        ThreadPoolManager.getInstance().schedule(new Spawn(22), 3000L);
                        break;
                    }
                    case 22: {
                        ThreadPoolManager.getInstance().schedule(new Spawn(23), 2000L);
                        _frintessaZone.getInsidePlayers().forEach(Player::leaveMovieMode);
                        break;
                    }
                    case 23: {
                        blockAll(false);
                        _scarletMorph = 1;
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class Music extends RunnableImpl {
        private final int _sondId;

        private Music(final int sondId) {
            _sondId = sondId;
        }

        @Override
        public void runImpl() {
            if (frintezza == null) {
                return;
            }
            disableMusicZones();
            if (!frintezza.isBlocked()) {
                switch (_sondId) {
                    case 1: {
                        final String songName = "Requiem of Hatred";
                        _areadataHeal1.setActive(true);
                        _areadataHeal2.setActive(true);
                        frintezza.broadcastPacket(new ExShowScreenMessage(songName, 3000, 0, ScreenMessageAlign.TOP_CENTER, true, 1, -1, true));
                        frintezza.broadcastPacket(new MagicSkillUse(frintezza, frintezza, 5007, 1, _intervalOfFrintezzaSongs, 0L));
                        break;
                    }
                    case 2: {
                        final String songName = "Frenetic Toccata";
                        _areadataRampage1.setActive(true);
                        _areadataRampage2.setActive(true);
                        frintezza.broadcastPacket(new ExShowScreenMessage(songName, 3000, 0, ScreenMessageAlign.TOP_CENTER, true, 1, -1, true));
                        frintezza.broadcastPacket(new MagicSkillUse(frintezza, frintezza, 5007, 2, _intervalOfFrintezzaSongs, 0L));
                        break;
                    }
                    case 3: {
                        final String songName = "Fugue of Jubilation";
                        _areadataPower1.setActive(true);
                        _areadataPower2.setActive(true);
                        frintezza.broadcastPacket(new ExShowScreenMessage(songName, 3000, 0, ScreenMessageAlign.TOP_CENTER, true, 1, -1, true));
                        frintezza.broadcastPacket(new MagicSkillUse(frintezza, frintezza, 5007, 3, _intervalOfFrintezzaSongs, 0L));
                        break;
                    }
                    case 4: {
                        final String songName = "Mournful Chorale Prelude";
                        _areadataPlague1.setActive(true);
                        _areadataPlague2.setActive(true);
                        frintezza.broadcastPacket(new ExShowScreenMessage(songName, 3000, 0, ScreenMessageAlign.TOP_CENTER, true, 1, -1, true));
                        frintezza.broadcastPacket(new MagicSkillUse(frintezza, frintezza, 5007, 4, _intervalOfFrintezzaSongs, 0L));
                        break;
                    }
                    case 5: {
                        final String songName = "Hypnotic Mazurka";
                        _areadataPsycho1.setActive(true);
                        _areadataPsycho2.setActive(true);
                        frintezza.broadcastPacket(new ExShowScreenMessage(songName, 3000, 0, ScreenMessageAlign.TOP_CENTER, true, 1, -1, true));
                        frintezza.broadcastPacket(new MagicSkillUse(frintezza, frintezza, 5007, 5, _intervalOfFrintezzaSongs, 0L));
                        break;
                    }
                    default: {
                        final String songName = "Rondo of Solitude";
                        if (frintezza != null) {
                            frintezza.broadcastPacket(new ExShowScreenMessage(songName, 3000, 0, ScreenMessageAlign.TOP_CENTER, true, 1, -1, true));
                            frintezza.broadcastPacket(new MagicSkillUse(frintezza, frintezza, 5006, 1, _intervalOfFrintezzaSongs, 0L));
                        }
                        disableMusicZones();
                        break;
                    }
                }
            }
            musicTask = ThreadPoolManager.getInstance().schedule(new Music(getSong()), (long) (_intervalOfFrintezzaSongs + Rnd.get(10000)));
        }

        private List<Creature> getSongTargets(final int songId) {
            final List<Creature> targets = new ArrayList<>();
            if (songId < 4) {
                if (weakScarlet != null && !weakScarlet.isDead()) {
                    targets.add(weakScarlet);
                }
                if (strongScarlet != null && !strongScarlet.isDead()) {
                    targets.add(strongScarlet);
                }
                IntStream.range(0, 4).forEach(i -> {
                    if (portraits[i] != null && !portraits[i].isDead() && portraits[i] != null) {
                        targets.add(portraits[i]);
                    }
                    if (demons[i] != null && !demons[i].isDead()) {
                        targets.add(demons[i]);
                    }
                });
            } else {
                _frintessaZone.getInsidePlayers().stream().filter(pc -> !pc.isDead()).forEach(targets::add);
            }
            return targets;
        }

        private int getSong() {
            if (minionsNeedHeal()) {
                return 1;
            }
            return 1 + Rnd.get(5);
        }

        private boolean minionsNeedHeal() {
            if (!Rnd.chance(40)) {
                return false;
            }
            if (weakScarlet != null && !weakScarlet.isAlikeDead() && weakScarlet.getCurrentHp() < weakScarlet.getMaxHp() * 2 / 3) {
                return true;
            }
            if (strongScarlet != null && !strongScarlet.isAlikeDead() && strongScarlet.getCurrentHp() < strongScarlet.getMaxHp() * 2 / 3) {
                return true;
            }
            for (int i = 0; i < 4; ++i) {
                if (portraits[i] != null && !portraits[i].isDead() && portraits[i].getCurrentHp() < portraits[i].getMaxHp() / 3) {
                    return true;
                }
                if (demons[i] != null && !demons[i].isDead() && demons[i].getCurrentHp() < demons[i].getMaxHp() / 3) {
                    return true;
                }
            }
            return false;
        }
    }

    private class SecondMorph extends RunnableImpl {
        private int _taskId;

        SecondMorph(final int taskId) {
            _taskId = 0;
            _taskId = taskId;
        }

        @Override
        public void runImpl() {
            try {
                switch (_taskId) {
                    case 1: {
                        final int angle = Math.abs(((weakScarlet.getHeading() < 32768) ? 180 : 540) - (int) (weakScarlet.getHeading() / 182.044444444));
                        _frintessaZone.getInsidePlayers().forEach(Player::enterMovieMode);
                        blockAll(true);
                        showSocialActionMovie(weakScarlet, 500, angle, 5, 500, 15000, 0);
                        ThreadPoolManager.getInstance().schedule(new SecondMorph(2), 2000L);
                        break;
                    }
                    case 2: {
                        weakScarlet.broadcastPacket(new SocialAction(weakScarlet.getObjectId(), 1));
                        weakScarlet.setCurrentHp((double) (weakScarlet.getMaxHp() * 3 / 4), false);
                        weakScarlet.setRHandId(7903);
                        weakScarlet.broadcastCharInfo();
                        ThreadPoolManager.getInstance().schedule(new SecondMorph(3), 5500L);
                        break;
                    }
                    case 3: {
                        weakScarlet.broadcastPacket(new SocialAction(weakScarlet.getObjectId(), 4));
                        blockAll(false);
                        final Skill skill = SkillTable.getInstance().getInfo(5017, 1);
                        skill.getEffects(weakScarlet, weakScarlet, false, false);
                        _frintessaZone.getInsidePlayers().forEach(Player::leaveMovieMode);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class ThirdMorph extends RunnableImpl {
        private int _taskId;
        private int _angle;

        ThirdMorph(final int taskId) {
            _taskId = 0;
            _angle = 0;
            _taskId = taskId;
        }

        @Override
        public void runImpl() {
            try {
                switch (_taskId) {
                    case 1: {
                        _angle = Math.abs(((weakScarlet.getHeading() < 32768) ? 180 : 540) - (int) (weakScarlet.getHeading() / 182.044444444));
                        _frintessaZone.getInsidePlayers().forEach(Player::enterMovieMode);
                        blockAll(true);
                        frintezza.broadcastPacket(new MagicSkillCanceled(frintezza));
                        frintezza.broadcastPacket(new SocialAction(frintezza.getObjectId(), 4));
                        ThreadPoolManager.getInstance().schedule(new ThirdMorph(2), 100L);
                        break;
                    }
                    case 2: {
                        showSocialActionMovie(frintezza, 250, 120, 15, 0, 1000, 0);
                        showSocialActionMovie(frintezza, 250, 120, 15, 0, 10000, 0);
                        ThreadPoolManager.getInstance().schedule(new ThirdMorph(3), 6500L);
                        break;
                    }
                    case 3: {
                        frintezza.broadcastPacket(new MagicSkillUse(frintezza, frintezza, 5006, 1, 34000, 0L));
                        showSocialActionMovie(frintezza, 500, 70, 15, 3000, 10000, 0);
                        ThreadPoolManager.getInstance().schedule(new ThirdMorph(4), 3000L);
                        break;
                    }
                    case 4: {
                        showSocialActionMovie(frintezza, 2500, 90, 12, 6000, 10000, 0);
                        ThreadPoolManager.getInstance().schedule(new ThirdMorph(5), 3000L);
                        break;
                    }
                    case 5: {
                        showSocialActionMovie(weakScarlet, 250, _angle, 12, 0, 1000, 0);
                        showSocialActionMovie(weakScarlet, 250, _angle, 12, 0, 10000, 0);
                        ThreadPoolManager.getInstance().schedule(new ThirdMorph(6), 500L);
                        break;
                    }
                    case 6: {
                        weakScarlet.doDie(weakScarlet);
                        showSocialActionMovie(weakScarlet, 450, _angle, 14, 8000, 8000, 0);
                        ThreadPoolManager.getInstance().schedule(new ThirdMorph(7), 6250L);
                        break;
                    }
                    case 7: {
                        final NpcLocation loc = new NpcLocation();
                        loc.set(weakScarlet.getLoc());
                        loc.npcId = 29047;
                        weakScarlet.deleteMe();
                        weakScarlet = null;
                        strongScarlet = spawn(loc);
                        strongScarlet.addListener(_deathListener);
                        block(strongScarlet, true);
                        showSocialActionMovie(strongScarlet, 450, _angle, 12, 500, 14000, 2);
                        ThreadPoolManager.getInstance().schedule(new ThirdMorph(9), 5000L);
                        break;
                    }
                    case 9: {
                        blockAll(false);
                        _frintessaZone.getInsidePlayers().forEach(Player::leaveMovieMode);
                        final Skill skill = SkillTable.getInstance().getInfo(5017, 1);
                        skill.getEffects(strongScarlet, strongScarlet, false, false);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class Die extends RunnableImpl {
        private int _taskId;

        public Die(final int taskId) {
            _taskId = 0;
            _taskId = taskId;
        }

        @Override
        public void runImpl() {
            try {
                switch (_taskId) {
                    case 1: {
                        blockAll(true);
                        final int _angle = Math.abs(((strongScarlet.getHeading() < 32768) ? 180 : 540) - (int) (strongScarlet.getHeading() / 182.044444444));
                        showSocialActionMovie(strongScarlet, 300, _angle - 180, 5, 0, 7000, 0);
                        showSocialActionMovie(strongScarlet, 200, _angle, 85, 4000, 10000, 0);
                        ThreadPoolManager.getInstance().schedule(new Die(2), 7500L);
                        break;
                    }
                    case 2: {
                        showSocialActionMovie(frintezza, 100, 120, 5, 0, 7000, 0);
                        showSocialActionMovie(frintezza, 100, 90, 5, 5000, 15000, 0);
                        ThreadPoolManager.getInstance().schedule(new Die(3), 6000L);
                        break;
                    }
                    case 3: {
                        showSocialActionMovie(frintezza, 900, 90, 25, 7000, 10000, 0);
                        frintezza.doDie(frintezza);
                        frintezza = null;
                        ThreadPoolManager.getInstance().schedule(new Die(4), 7000L);
                        break;
                    }
                    case 4: {
                        disableMusicZones();
                        _frintessaZone.getInsidePlayers().forEach(Player::leaveMovieMode);
                        SpawnManager.getInstance().spawn("[frintezza_teleporter]");
                        _state.setState(State.DEAD);
                        _state.update();
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class OnDeathListenerImpl implements OnDeathListener {
        @Override
        public void onDeath(final Creature actor, final Creature killer) {
            if (actor.isNpc()) {
                final NpcInstance npc = (NpcInstance) actor;
                if (npc == weakScarlet) {
                    npc.decayMe();
                } else if (npc == strongScarlet) {
                    ThreadPoolManager.getInstance().schedule(new Die(1), 10L);
                } else if (isNpcOfSpawn(npc, _roomAAlarmDevices)) {
                    onRoomAAlarmDeviceDied(npc);
                } else if (isNpcOfSpawn(npc, _roomBUndeadBandPlayers)) {
                    onRoomBUndeadBandPlayerDied((NpcInstance) actor);
                } else if (isNpcOfSpawn(npc, _roomBUndeadBandMasters)) {
                    onRoomBUndeadBandMasterDied(npc, killer);
                }
            }
        }
    }

    private class TombTimeoutTask extends RunnableImpl {
        private int _timeLeft;

        TombTimeoutTask() {
            _timeLeft = Config.FRINTEZZA_TOMB_TIMEOUT;
        }

        @Override
        public void runImpl() {
            if (_timeLeft > 0) {
                _lastImperialTombZone.getInsidePlayers().forEach(player -> player.sendPacket(new ExShowScreenMessage(new CustomMessage("LastImperialTomb.Remaining", player, new Object[]{_timeLeft}).toString(), 10000, ScreenMessageAlign.BOTTOM_RIGHT, false)));
                if (_timeLeft > 5) {
                    _timeLeft -= 5;
                    ThreadPoolManager.getInstance().schedule(this, battleStartDelay);
                } else {
                    --_timeLeft;
                    ThreadPoolManager.getInstance().schedule(this, 60000L);
                }
            } else {
                cleanUp();
            }
        }
    }

    public class OnCurrentHpDamageListenerImpl implements OnCurrentHpDamageListener {
        @Override
        public void onCurrentHpDamage(final Creature actor, final double damage, final Creature attacker, final Skill skill) {
            if (actor.isDead() || actor != weakScarlet) {
                return;
            }
            final double newHp = actor.getCurrentHp() - damage;
            final double maxHp = actor.getMaxHp();
            switch (_scarletMorph) {
                case 1: {
                    if (newHp < 0.75 * maxHp) {
                        _scarletMorph = 2;
                        ThreadPoolManager.getInstance().schedule(new SecondMorph(1), 1100L);
                        break;
                    }
                    break;
                }
                case 2: {
                    if (newHp < 0.1 * maxHp) {
                        _scarletMorph = 3;
                        ThreadPoolManager.getInstance().schedule(new ThirdMorph(1), 2000L);
                        break;
                    }
                    break;
                }
            }
        }
    }

    private class OnZoneEnterLeaveListenerImpl implements OnZoneEnterLeaveListener {
        @Override
        public void onZoneEnter(final Zone zone, final Creature actor) {
            onEnterFrontessaZone();
        }

        @Override
        public void onZoneLeave(final Zone zone, final Creature cha) {
            if (cha.isNpc() && (cha == weakScarlet || cha == strongScarlet)) {
                cha.teleToLocation(new Location(174240, -88020, -5112));
                ((NpcInstance) cha).getAggroList().clear(true);
                cha.setCurrentHpMp((double) cha.getMaxHp(), (double) cha.getMaxMp());
                cha.broadcastCharInfo();
            }
        }
    }

    private class FrintezzaItemHandler extends SimpleItemHandler {
        @Override
        protected boolean useItemImpl(final Player player, final ItemInstance item, final boolean ctrl) {
            final int itemId = item.getItemId();
            if (!SimpleItemHandler.useItem(player, item, 1L)) {
                return false;
            }
            switch (itemId) {
                case BREAKING_ARROW_ITEM_ID: {
                    if (player.getTarget() != null && player.getTarget().isNpc() && player.getTarget() == getInstance().frintezza) {
                        final NpcInstance npc = (NpcInstance) player.getTarget();
                        if (npc.getNpcId() == FRINTEZZA_NPC_ID) {
                            player.callSkill(_arrowSkill, Collections.singletonList(getInstance().frintezza), false);
                            player.broadcastPacket(new MagicSkillUse(player, getInstance().frintezza, 2234, 1, 1000, 0L));
                            getInstance().cancelMusic();
                        }
                        break;
                    }
                    break;
                }
                case DEWDROP_OF_DESSTRUCTION_ID: {
                    if (player.getTarget() == null || !player.getTarget().isNpc()) {
                        break;
                    }
                    final NpcInstance portrait = (NpcInstance) player.getTarget();
                    if (portrait.getNpcId() == 29048 || portrait.getNpcId() == 29049) {
                        portrait.doDie(player);
                        break;
                    }
                    break;
                }
            }
            return false;
        }

        @Override
        public int[] getItemIds() {
            return new int[]{DEWDROP_OF_DESSTRUCTION_ID, BREAKING_ARROW_ITEM_ID};
        }
    }
}
