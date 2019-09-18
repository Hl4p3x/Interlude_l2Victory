package bosses;

import npc.model.SepulcherNpcInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.idfactory.IdFactory;
import ru.j2dev.gameserver.listener.actor.OnDeathListener;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Zone;
import ru.j2dev.gameserver.model.actor.listener.CharListenerList;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.ReflectionUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

import static bosses.FourSepulchersSpawn.*;

public class FourSepulchersManager extends Functions implements OnInitScriptListener {
    public static final String QUEST_ID = "_620_FourGoblets";
    private static final Logger LOGGER = LoggerFactory.getLogger(FourSepulchersManager.class);
    private static final int ENTRANCE_PASS = 7075;
    private static final int USED_PASS = 7261;
    private static final int CHAPEL_KEY = 7260;
    private static final int ANTIQUE_BROOCH = 7262;
    private static final List<Zone> _zone = new ArrayList<>(4);
    private static final int _newCycleMin = 55;
    private static boolean _inEntryTime;
    private static boolean _inAttackTime;
    private static ScheduledFuture<?> _changeCoolDownTimeTask;
    private static ScheduledFuture<?> _changeEntryTimeTask;
    private static ScheduledFuture<?> _changeWarmUpTimeTask;
    private static ScheduledFuture<?> _changeAttackTimeTask;
    private static long _coolDownTimeEnd;
    private static long _entryTimeEnd;
    private static long _warmUpTimeEnd;
    private static long _attackTimeEnd;
    private static boolean _firstTimeRun;

    private static void timeSelector() {
        timeCalculator();
        final long currentTime = System.currentTimeMillis();
        if (currentTime >= _coolDownTimeEnd && currentTime < _entryTimeEnd) {
            cleanUp();
            _changeEntryTimeTask = ThreadPoolManager.getInstance().schedule(new ChangeEntryTime(), 0L);
            LOGGER.info("FourSepulchersManager: Beginning in Entry time");
        } else if (currentTime >= _entryTimeEnd && currentTime < _warmUpTimeEnd) {
            cleanUp();
            _changeWarmUpTimeTask = ThreadPoolManager.getInstance().schedule(new ChangeWarmUpTime(), 0L);
            LOGGER.info("FourSepulchersManager: Beginning in WarmUp time");
        } else if (currentTime >= _warmUpTimeEnd && currentTime < _attackTimeEnd) {
            cleanUp();
            _changeAttackTimeTask = ThreadPoolManager.getInstance().schedule(new ChangeAttackTime(), 0L);
            LOGGER.info("FourSepulchersManager: Beginning in Attack time");
        } else {
            _changeCoolDownTimeTask = ThreadPoolManager.getInstance().schedule(new ChangeCoolDownTime(), 0L);
            LOGGER.info("FourSepulchersManager: Beginning in Cooldown time");
        }
    }

    private static void timeCalculator() {
        final Calendar tmp = Calendar.getInstance();
        if (tmp.get(Calendar.MINUTE) < _newCycleMin) {
            tmp.set(Calendar.HOUR, Calendar.getInstance().get(Calendar.HOUR) - 1);
        }
        tmp.set(Calendar.MINUTE, _newCycleMin);
        _coolDownTimeEnd = tmp.getTimeInMillis();
        _entryTimeEnd = _coolDownTimeEnd + 180000L;
        _warmUpTimeEnd = _entryTimeEnd + 120000L;
        _attackTimeEnd = _warmUpTimeEnd + 3000000L;
    }

    private static void cleanUp() {
        for (final Player player : getPlayersInside()) {
            player.teleToClosestTown();
        }
        deleteAllMobs();
        closeAllDoors();
        _hallInUse.clear();
        _hallInUse.put(31921, false);
        _hallInUse.put(31922, false);
        _hallInUse.put(31923, false);
        _hallInUse.put(31924, false);
        if (!_archonSpawned.isEmpty()) {
            final Set<Integer> npcIdSet = _archonSpawned.keySet();
            npcIdSet.forEach(npcId -> _archonSpawned.put(npcId, false));
        }
    }

    public static boolean isEntryTime() {
        return _inEntryTime;
    }

    public static boolean isAttackTime() {
        return _inAttackTime;
    }

    public static synchronized void tryEntry(final NpcInstance npc, final Player player) {
        final int npcId = npc.getNpcId();
        switch (npcId) {
            case 31921:
            case 31922:
            case 31923:
            case 31924: {
                if (_hallInUse.get(npcId)) {
                    showHtmlFile(player, npcId + "-FULL.htm", npc, null);
                    return;
                }
                if (!player.isInParty() || player.getParty().getMemberCount() < Config.FOUR_SEPULCHER_MIN_PARTY_MEMBERS) {
                    showHtmlFile(player, npcId + "-SP.htm", npc, null);
                    return;
                }
                if (!player.getParty().isLeader(player)) {
                    showHtmlFile(player, npcId + "-NL.htm", npc, null);
                    return;
                }
                for (final Player mem : player.getParty().getPartyMembers()) {
                    final QuestState qs = mem.getQuestState("_620_FourGoblets");
                    if (qs == null || (!qs.isStarted() && !qs.isCompleted())) {
                        showHtmlFile(player, npcId + "-NS.htm", npc, mem);
                        return;
                    }
                    if (mem.getInventory().getItemByItemId(7075) == null) {
                        showHtmlFile(player, npcId + "-SE.htm", npc, mem);
                        return;
                    }
                    if (!mem.isQuestContinuationPossible(true)) {
                        return;
                    }
                    if (mem.isDead() || !mem.isInRange(player, 700L)) {
                        return;
                    }
                }
                if (!isEntryTime()) {
                    showHtmlFile(player, npcId + "-NE.htm", npc, null);
                    return;
                }
                showHtmlFile(player, npcId + "-OK.htm", npc, null);
                entry(npcId, player);
            }
            default: {
            }
        }
    }

    private static void entry(final int npcId, final Player player) {
        final Location loc = _startHallSpawns.get(npcId);
        player.getParty().getPartyMembers().forEach(member -> {
            member.teleToLocation(Location.findPointToStay(member, loc, 0, 80));
            Functions.removeItem(member, 7075, 1L);
            if (member.getInventory().getItemByItemId(7262) == null) {
                Functions.addItem(member, 7261, 1L);
            }
            Functions.removeItem(member, 7260, 999999L);
        });
        _hallInUse.put(npcId, true);
    }

    public static void checkAnnihilated(final Player player) {
        if (isPlayersAnnihilated()) {
            ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
                @Override
                public void runImpl() {
                    if (player.getParty() != null) {
                        for (final Player mem : player.getParty().getPartyMembers()) {
                            if (!mem.isDead()) {
                                break;
                            }
                            mem.teleToLocation(169589 + Rnd.get(-80, 80), -90493 + Rnd.get(-80, 80), -2914);
                        }
                    } else {
                        player.teleToLocation(169589 + Rnd.get(-80, 80), -90493 + Rnd.get(-80, 80), -2914);
                    }
                }
            }, 5000L);
        }
    }

    private static int minuteSelect(final int min) {
        switch (min % 5) {
            case 0: {
                return min;
            }
            case 1: {
                return min - 1;
            }
            case 2: {
                return min - 2;
            }
            case 3: {
                return min + 2;
            }
            default: {
                return min + 1;
            }
        }
    }

    public static void managerSay(int min) {
        if (_inAttackTime) {
            if (min < 5) {
                return;
            }
            min = minuteSelect(min);
            String msg = min + " minute(s) have passed.";
            if (min == 90) {
                msg = "Game over. The teleport will appear momentarily";
            }
            for (final SepulcherNpcInstance npc : _managers) {
                if (!_hallInUse.get(npc.getNpcId())) {
                    continue;
                }
                npc.sayInShout(msg);
            }
        } else if (_inEntryTime) {
            final String msg2 = "You may now enter the Sepulcher";
            final String msg3 = "If you place your hand on the stone statue in front of each sepulcher, you will be able to enter";
            _managers.forEach(npc2 -> {
                npc2.sayInShout(msg2);
                npc2.sayInShout(msg3);
            });
        }
    }

    public static GateKeeper getHallGateKeeper(final int npcId) {
        return _GateKeepers.stream().filter(gk -> gk.template.npcId == npcId).findFirst().orElse(null);
    }

    public static void showHtmlFile(final Player player, final String file, final NpcInstance npc, final Player member) {
        final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
        html.setFile("SepulcherNpc/" + file);
        if (member != null) {
            html.replace("%member%", member.getName());
        }
        player.sendPacket(html);
    }

    private static boolean isPlayersAnnihilated() {
        return getPlayersInside().stream().allMatch(Player::isDead);
    }

    private static List<Player> getPlayersInside() {
        final List<Player> result = new ArrayList<>();
        getZones().stream().map(Zone::getInsidePlayers).forEach(result::addAll);
        return result;
    }

    private static boolean checkIfInZone(final Creature cha) {
        return getZones().stream().anyMatch(zone -> zone.checkIfInZone(cha));
    }

    public static List<Zone> getZones() {
        return _zone;
    }

    public void init() {
        CharListenerList.addGlobal(new OnDeathListenerImpl());
        _zone.add(ReflectionUtils.getZone("[FourSepulchers1]"));
        _zone.add(ReflectionUtils.getZone("[FourSepulchers2]"));
        _zone.add(ReflectionUtils.getZone("[FourSepulchers3]"));
        _zone.add(ReflectionUtils.getZone("[FourSepulchers4]"));
        if (_changeCoolDownTimeTask != null) {
            _changeCoolDownTimeTask.cancel(false);
        }
        if (_changeEntryTimeTask != null) {
            _changeEntryTimeTask.cancel(false);
        }
        if (_changeWarmUpTimeTask != null) {
            _changeWarmUpTimeTask.cancel(false);
        }
        if (_changeAttackTimeTask != null) {
            _changeAttackTimeTask.cancel(false);
        }
        _changeCoolDownTimeTask = null;
        _changeEntryTimeTask = null;
        _changeWarmUpTimeTask = null;
        _changeAttackTimeTask = null;
        _inEntryTime = false;
        _inAttackTime = false;
        _firstTimeRun = true;
        FourSepulchersSpawn.init();
        timeSelector();
    }

    @Override
    public void onInit() {
        if (Config.FOUR_SEPULCHER_ENABLE) {
            init();
        }
    }

    private static class ManagerSay extends RunnableImpl {
        @Override
        public void runImpl() {
            if (_inAttackTime) {
                final Calendar tmp = Calendar.getInstance();
                tmp.setTimeInMillis(System.currentTimeMillis() - _warmUpTimeEnd);
                if (tmp.get(Calendar.MINUTE) + 5 < 50) {
                    managerSay(tmp.get(Calendar.MINUTE));
                    ThreadPoolManager.getInstance().schedule(new ManagerSay(), 300000L);
                } else if (tmp.get(Calendar.MINUTE) + 5 >= 50) {
                    managerSay(90);
                }
            } else if (_inEntryTime) {
                managerSay(0);
            }
        }
    }

    private static class ChangeEntryTime extends RunnableImpl {
        @Override
        public void runImpl() {
            _inEntryTime = true;
            _inAttackTime = false;
            long interval;
            if (_firstTimeRun) {
                interval = _entryTimeEnd - System.currentTimeMillis();
            } else {
                interval = 180000L;
            }
            ThreadPoolManager.getInstance().execute(new ManagerSay());
            _changeWarmUpTimeTask = ThreadPoolManager.getInstance().schedule(new ChangeWarmUpTime(), interval);
            if (_changeEntryTimeTask != null) {
                _changeEntryTimeTask.cancel(false);
                _changeEntryTimeTask = null;
            }
        }
    }

    private static class ChangeWarmUpTime extends RunnableImpl {
        @Override
        public void runImpl() {
            _inEntryTime = true;
            _inAttackTime = false;
            long interval;
            if (_firstTimeRun) {
                interval = _warmUpTimeEnd - System.currentTimeMillis();
            } else {
                interval = 120000L;
            }
            _changeAttackTimeTask = ThreadPoolManager.getInstance().schedule(new ChangeAttackTime(), interval);
            if (_changeWarmUpTimeTask != null) {
                _changeWarmUpTimeTask.cancel(false);
                _changeWarmUpTimeTask = null;
            }
        }
    }

    private static class ChangeAttackTime extends RunnableImpl {
        @Override
        public void runImpl() {
            _inEntryTime = false;
            _inAttackTime = true;
            _GateKeepers.forEach(gk -> {
                final SepulcherNpcInstance npc = new SepulcherNpcInstance(IdFactory.getInstance().getNextId(), gk.template);
                npc.spawnMe(gk);
                _allMobs.add(npc);
            });
            locationShadowSpawns();
            spawnMysteriousBox(31921);
            spawnMysteriousBox(31922);
            spawnMysteriousBox(31923);
            spawnMysteriousBox(31924);
            if (!_firstTimeRun) {
                _warmUpTimeEnd = System.currentTimeMillis();
            }
            long interval;
            if (_firstTimeRun) {
                for (double min = Calendar.getInstance().get(Calendar.MINUTE); min < _newCycleMin; ++min) {
                    if (min % 5.0 == 0.0) {
                        ChangeAttackTime.LOGGER.info(Calendar.getInstance().getTime() + " Atk announce scheduled to " + min + " minute of this hour.");
                        final Calendar inter = Calendar.getInstance();
                        inter.set(Calendar.MINUTE, (int) min);
                        ThreadPoolManager.getInstance().schedule(new ManagerSay(), inter.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());
                        break;
                    }
                }
            } else {
                ThreadPoolManager.getInstance().schedule(new ManagerSay(), 302000L);
            }
            if (_firstTimeRun) {
                interval = _attackTimeEnd - System.currentTimeMillis();
            } else {
                interval = 3000000L;
            }
            _changeCoolDownTimeTask = ThreadPoolManager.getInstance().schedule(new ChangeCoolDownTime(), interval);
            if (_changeAttackTimeTask != null) {
                _changeAttackTimeTask.cancel(false);
                _changeAttackTimeTask = null;
            }
        }
    }

    private static class ChangeCoolDownTime extends RunnableImpl {
        @Override
        public void runImpl() {
            _inEntryTime = false;
            _inAttackTime = false;
            cleanUp();
            final Calendar time = Calendar.getInstance();
            if (Calendar.getInstance().get(Calendar.MINUTE) > _newCycleMin && !_firstTimeRun) {
                time.set(Calendar.HOUR, Calendar.getInstance().get(Calendar.HOUR) + 1);
            }
            time.set(Calendar.MINUTE, _newCycleMin);
            ChangeCoolDownTime.LOGGER.info("FourSepulchersManager: Entry time: " + time.getTime());
            if (_firstTimeRun) {
                _firstTimeRun = false;
            }
            final long interval = time.getTimeInMillis() - System.currentTimeMillis();
            _changeEntryTimeTask = ThreadPoolManager.getInstance().schedule(new ChangeEntryTime(), interval);
            if (_changeCoolDownTimeTask != null) {
                _changeCoolDownTimeTask.cancel(false);
                _changeCoolDownTimeTask = null;
            }
        }
    }

    private static class OnDeathListenerImpl implements OnDeathListener {
        @Override
        public void onDeath(final Creature self, final Creature killer) {
            if (self.isPlayer() && self.getZ() >= -7250 && self.getZ() <= -6841 && checkIfInZone(self)) {
                checkAnnihilated((Player) self);
            }
        }
    }
}
