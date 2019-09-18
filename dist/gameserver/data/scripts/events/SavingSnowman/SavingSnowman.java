package events.SavingSnowman;

import ai.FlyingSantaAI;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.util.RandomUtils;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Announcements;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.data.xml.holder.ZoneHolder;
import ru.j2dev.gameserver.geodata.GeoEngine;
import ru.j2dev.gameserver.handler.npcdialog.INpcDialogAppender;
import ru.j2dev.gameserver.idfactory.IdFactory;
import ru.j2dev.gameserver.listener.actor.OnDeathListener;
import ru.j2dev.gameserver.listener.actor.player.OnPlayerEnterListener;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.model.actor.listener.CharListenerList;
import ru.j2dev.gameserver.model.actor.listener.PlayerListenerList;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;
import ru.j2dev.gameserver.network.lineage2.serverpackets.CharMoveToLocation;
import ru.j2dev.gameserver.network.lineage2.serverpackets.RadarControl;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.templates.ZoneTemplate;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.PositionUtils;
import ru.j2dev.gameserver.utils.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;

public class SavingSnowman extends Functions implements OnInitScriptListener, INpcDialogAppender {
    private static final Logger LOGGER = LoggerFactory.getLogger(SavingSnowman.class);
    private static final int INITIAL_SAVE_DELAY = 1800000;
    private static final int SAVE_INTERVAL = 7200000;
    private static final int SNOWMAN_SHOUT_INTERVAL = 60000;
    private static final int THOMAS_EAT_DELAY = 600000;
    private static final int SATNA_SAY_INTERVAL = 600000;
    private static final int SANTA_ACTION_SPAWN_INTERVAL = 6600000;
    private static final int EVENT_MANAGER_ID = 13184;
    private static final int CTREE_ID = 13006;
    private static final int EVENT_REWARDER_ID = 13186;
    private static final int SNOWMAN_ID = 13160;
    private static final int THOMAS_ID = 13183;
    private static final Location[] _santaSpawn = {new Location(82632, 148712, -3472), new Location(147448, 28552, -2272), new Location(145873, -54756, -2807)};
    private static final List<Pair<Pair<Integer, Long>, Double>> REWARD;
    private static final double REWARD_CHANCE_SUM;
    private static final ReentrantLock _loc = new ReentrantLock();
    private static final List<SimpleSpawner> _spawns = new ArrayList<>();
    private static final int[] DROP_ITEM_ID = {17130, 17131, 17132, 17133, 17134, 17135, 17136, 17137};
    private static final long[] DROP_ITEM_COUNT = {1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L};
    private static final double DROP_ITEM_CHANCE = 10.0;
    public static SnowmanState _snowmanState;
    private static ScheduledFuture<?> _snowmanShoutTask;
    private static ScheduledFuture<?> _saveTask;
    private static ScheduledFuture<?> _sayTask;
    private static ScheduledFuture<?> _eatTask;
    private static ScheduledFuture<?> _santaTask;
    private static NpcInstance _snowman;
    private static NpcInstance _thomas;
    private static boolean _active;
    private ListenersImpl listeners = new ListenersImpl();

    static {
        (REWARD = Arrays.asList(new ImmutablePair<>(new ImmutablePair<>(14612, 1L), 20.0),
                new ImmutablePair<>(new ImmutablePair<>(9627, 1L), 10.0),
                new ImmutablePair<>(new ImmutablePair<>(5561, 1L), 8.0),
                new ImmutablePair<>(new ImmutablePair<>(5560, 1L), 5.0),
                new ImmutablePair<>(new ImmutablePair<>(22206, 1L), 5.0),
                new ImmutablePair<>(new ImmutablePair<>(22207, 1L), 5.0),
                new ImmutablePair<>(new ImmutablePair<>(21587, 1L), 5.0),
                new ImmutablePair<>(new ImmutablePair<>(21588, 1L), 5.0),
                new ImmutablePair<>(new ImmutablePair<>(21583, 1L), 5.0),
                new ImmutablePair<>(new ImmutablePair<>(21126, 1L), 5.0),
                new ImmutablePair<>(new ImmutablePair<>(21124, 1L), 5.0),
                new ImmutablePair<>(new ImmutablePair<>(20020, 1L), 5.0),
                new ImmutablePair<>(new ImmutablePair<>(13490, 1L), 5.0),
                new ImmutablePair<>(new ImmutablePair<>(21618, 1L), 1.0),
                new ImmutablePair<>(new ImmutablePair<>(21924, 1L), 1.0),
                new ImmutablePair<>(new ImmutablePair<>(21925, 1L), 1.0),
                new ImmutablePair<>(new ImmutablePair<>(21926, 1L), 1.0),
                new ImmutablePair<>(new ImmutablePair<>(21944, 1L), 1.0),
                new ImmutablePair<>(new ImmutablePair<>(21945, 1L), 1.0),
                new ImmutablePair<>(new ImmutablePair<>(21946, 1L), 1.0),
                new ImmutablePair<>(new ImmutablePair<>(21938, 1L), 1.0),
                new ImmutablePair<>(new ImmutablePair<>(21956, 1L), 1.0),
                new ImmutablePair<>(new ImmutablePair<>(21963, 1L), 1.0),
                new ImmutablePair<>(new ImmutablePair<>(21964, 1L), 1.0),
                new ImmutablePair<>(new ImmutablePair<>(21965, 1L), 1.0))).sort(RandomUtils.DOUBLE_GROUP_COMPARATOR);
        REWARD_CHANCE_SUM = REWARD.stream().mapToDouble(Pair::getRight).sum();
    }

    private static boolean isActive() {
        return IsActive("SavingSnowman");
    }

    public static void spawnRewarder(final Player rewarded) {
        for (final NpcInstance npc : rewarded.getAroundNpc(1500, 300)) {
            if (npc.getNpcId() == 13186) {
                return;
            }
        }
        Location spawnLoc = Location.findPointToStay(rewarded, 300, 400);
        for (int i = 0; i < 20 && !GeoEngine.canSeeCoord(rewarded, spawnLoc.x, spawnLoc.y, spawnLoc.z, false); spawnLoc = Location.findPointToStay(rewarded, 300, 400), ++i) {
        }
        final NpcTemplate template = NpcTemplateHolder.getInstance().getTemplate(13186);
        if (template == null) {
            LOGGER.info("WARNING! events.SavingSnowman.spawnRewarder template is null for npc: 13186");
            Thread.dumpStack();
            return;
        }
        final NpcInstance rewarder = new NpcInstance(IdFactory.getInstance().getNextId(), template);
        rewarder.setLoc(spawnLoc);
        rewarder.setHeading((int) (Math.atan2(spawnLoc.y - rewarded.getY(), spawnLoc.x - rewarded.getX()) * 10430.378350470453) + 32768);
        rewarder.spawnMe();
        Functions.npcSayCustomMessage(rewarder, "scripts.events.SavingSnowman.RewarderPhrase1");
        final Location targetLoc = Location.findPointToStay(rewarded, 40, 50);
        rewarder.setSpawnedLoc(targetLoc);
        rewarder.broadcastPacket(new CharMoveToLocation(rewarder.getObjectId(), rewarder.getLoc(), targetLoc));
        executeTask("events.SavingSnowman.SavingSnowman", "reward", new Object[]{rewarder, rewarded}, 5000L);
    }

    public static void reward(final NpcInstance rewarder, final Player rewarded) {
        if (!_active || rewarder == null || rewarded == null) {
            return;
        }
        Functions.npcSayCustomMessage(rewarder, "scripts.events.SavingSnowman.RewarderPhrase2", rewarded.getName());
        Functions.addItem(rewarded, 14616, 1L);
        executeTask("events.SavingSnowman.SavingSnowman", "removeRewarder", new Object[]{rewarder}, 5000L);
    }

    public static void removeRewarder(final NpcInstance rewarder) {
        if (!_active || rewarder == null) {
            return;
        }
        Functions.npcSayCustomMessage(rewarder, "scripts.events.SavingSnowman.RewarderPhrase3");
        final Location loc = rewarder.getSpawnedLoc();
        final double radian = PositionUtils.convertHeadingToRadian(rewarder.getHeading());
        final int x = loc.x - (int) (Math.sin(radian) * 300.0);
        final int y = loc.y + (int) (Math.cos(radian) * 300.0);
        final int z = loc.z;
        rewarder.broadcastPacket(new CharMoveToLocation(rewarder.getObjectId(), loc, new Location(x, y, z)));
        executeTask("events.SavingSnowman.SavingSnowman", "unspawnRewarder", new Object[]{rewarder}, 2000L);
    }

    public static void unspawnRewarder(final NpcInstance rewarder) {
        if (!_active || rewarder == null) {
            return;
        }
        rewarder.decayMe();
        rewarder.deleteMe();
    }

    private static Location getRandomSpawnPoint() {
        final ArrayList<ZoneTemplate> zones = new ArrayList<>();
        for (final ZoneTemplate zt : ZoneHolder.getInstance().getZones().values()) {
            if (zt != null && zt.isDefault()) {
                zones.add(zt);
            }
        }
        final Territory terr = zones.get(Rnd.get(zones.size())).getTerritory();
        return terr.getRandomLoc(0);
    }

    public static void eatSnowman() {
        if (_snowman == null || _thomas == null) {
            return;
        }
        GameObjectsStorage.getPlayers().forEach(player -> Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.SavingSnowman.AnnounceSnowmanKilled", null, ChatType.ANNOUNCEMENT));
        _snowmanState = SnowmanState.KILLED;
        if (_snowmanShoutTask != null) {
            _snowmanShoutTask.cancel(true);
            _snowmanShoutTask = null;
        }
        _snowman.deleteMe();
        _thomas.deleteMe();
    }

    public static void freeSnowman(final Creature topDamager) {
        _loc.lock();
        try {
            if (_snowmanState != SnowmanState.CAPTURED || _snowman == null || topDamager == null || !topDamager.isPlayable()) {
                return;
            }
            _snowmanState = SnowmanState.SAVED;
            GameObjectsStorage.getPlayers().forEach(player -> Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.SavingSnowman.AnnounceSnowmanSaved", null, ChatType.ANNOUNCEMENT));
            if (_snowmanShoutTask != null) {
                _snowmanShoutTask.cancel(true);
                _snowmanShoutTask = null;
            }
            if (_eatTask != null) {
                _eatTask.cancel(true);
                _eatTask = null;
            }
            final Player player2 = topDamager.getPlayer();
            Functions.npcSayCustomMessage(_snowman, "scripts.events.SavingSnowman.SnowmanSayTnx", player2.getName());
            addItem(player2, 14616, 5L);
            if (topDamager.isPlayer()) {
                spawnRewarder(topDamager.getPlayer());
            }
            _snowman.decayMe();
            _snowman.deleteMe();
        } finally {
            _loc.unlock();
        }
    }

    @Override
    public void onInit() {
        if (isActive()) {
            _active = true;
            spawnEventManagers();
            LOGGER.info("Loaded Event: SavingSnowman [state: activated]");
            _saveTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new SaveTask(), 1800000L, 7200000L);
            _sayTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new SayTask(), 600000L, 600000L);
            _santaTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new SantaTask(), 6600000L, 6600000L);
            _snowmanState = SnowmanState.SAVED;
            PlayerListenerList.addGlobal(listeners);
            CharListenerList.addGlobal(listeners);
        } else {
            LOGGER.info("Loaded Event: SavingSnowman [state: deactivated]");
        }
    }

    public void startEvent() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (SetActive("SavingSnowman", true)) {
            spawnEventManagers();
            LOGGER.info("Event 'SavingSnowman' started.");
            Announcements.getInstance().announceByCustomMessage("scripts.events.SavingSnowman.AnnounceEventStarted", null);
            if (_saveTask == null) {
                _saveTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new SaveTask(), 1800000L, 7200000L);
            }
            if (_sayTask == null) {
                _sayTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new SayTask(), 600000L, 600000L);
            }
            if (_santaTask == null) {
                _santaTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new SantaTask(), 6600000L, 6600000L);
            }
            _snowmanState = SnowmanState.SAVED;
            PlayerListenerList.addGlobal(listeners);
            CharListenerList.addGlobal(listeners);
        } else {
            player.sendMessage("Event 'SavingSnowman' already started.");
        }
        _active = true;
        show("admin/events/events.htm", player);
    }

    public void stopEvent() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (SetActive("SavingSnowman", false)) {
            unSpawnEventManagers();
            if (_snowman != null) {
                _snowman.deleteMe();
            }
            if (_thomas != null) {
                _thomas.deleteMe();
            }
            LOGGER.info("Event 'SavingSnowman' stopped.");
            Announcements.getInstance().announceByCustomMessage("scripts.events.SavingSnowman.AnnounceEventStoped", null);
            if (_saveTask != null) {
                _saveTask.cancel(true);
                _saveTask = null;
            }
            if (_sayTask != null) {
                _sayTask.cancel(true);
                _sayTask = null;
            }
            if (_eatTask != null) {
                _eatTask.cancel(true);
                _eatTask = null;
            }
            if (_santaTask != null) {
                _santaTask.cancel(true);
                _santaTask = null;
            }
            _snowmanState = SnowmanState.SAVED;
            PlayerListenerList.removeGlobal(listeners);
            CharListenerList.removeGlobal(listeners);
        } else {
            player.sendMessage("Event 'SavingSnowman' not started.");
        }
        _active = false;
        show("admin/events/events.htm", player);
    }

    private void spawnEventManagers() {
        final int[][] EVENT_MANAGERS = {{83640, 147976, -3400, 24575}, {146405, 28360, -2269, 49648}, {19319, 144919, -3103, 31135}, {-82805, 149890, -3129, 16384}, {-12347, 122549, -3104, 16384}, {110642, 220165, -3655, 61898}, {116619, 75463, -2721, 20881}, {85513, 16014, -3668, 23681}, {81999, 53793, -1496, 61621}, {148159, -55484, -2734, 44315}, {44185, -48502, -797, 27479}, {86899, -143229, -1293, 8192}};
        final int[][] CTREES = {{83656, 148056, -3400, 24576}, {83204, 147912, -3368, 32768}, {83204, 148395, -3368, 32768}, {83204, 148844, -3368, 32768}, {83202, 149325, -3368, 32768}, {146445, 28360, -2269, 0}, {19319, 144959, -3103, 0}, {-82845, 149890, -3129, 0}, {-12387, 122549, -3104, 0}, {110602, 220165, -3655, 0}, {116659, 75463, -2721, 0}, {85553, 16014, -3668, 0}, {81999, 53743, -1496, 0}, {148199, -55484, -2734, 0}, {44185, -48542, -797, 0}, {86859, -143229, -1293, 0}};
        SpawnNPCs(13184, EVENT_MANAGERS, _spawns);
        SpawnNPCs(13006, CTREES, _spawns);
    }

    private void unSpawnEventManagers() {
        deSpawnNPCs(_spawns);
    }


    public void buff() {
        final Player player = getSelf();
        if (!_active || player.isActionsDisabled() || player.isSitting() || player.getLastNpc() == null || player.getLastNpc().getDistance(player) > 300.0) {
            return;
        }
        if (!player.isQuestContinuationPossible(true)) {
            return;
        }
        if (_snowmanState != SnowmanState.SAVED) {
            show("default/13184-3.htm", player);
            return;
        }
        final Summon pet = player.getPet();
        if (pet != null) {
        }
    }

    public void locateSnowman() {
        final Player player = getSelf();
        if (!_active || player.isActionsDisabled() || player.isSitting() || player.getLastNpc() == null || player.getLastNpc().getDistance(player) > 300.0) {
            return;
        }
        if (_snowman != null) {
            player.sendPacket(new RadarControl(2, 2, _snowman.getLoc()), new RadarControl(0, 1, _snowman.getLoc()));
            player.sendPacket(new SystemMessage(2010).addZoneName(_snowman.getLoc()).addString("\u0421\u043d\u0435\u0433\u043e\u0432\u0438\u043a\u0430 \u0437\u0430\u0445\u0432\u0430\u0442\u0438\u043b\u0438 \u0432 "));
        } else {
            player.sendPacket(Msg.YOUR_TARGET_CANNOT_BE_FOUND);
        }
    }

    public void lotery() {
        final Player player = getSelf();
        if (!_active || player.isActionsDisabled() || player.isSitting() || player.getLastNpc() == null || player.getLastNpc().getDistance(player) > 300.0) {
            return;
        }
        if (!player.isQuestContinuationPossible(true)) {
            return;
        }
        if (getItemCount(player, Config.EVENT_SAVING_SNOWMAN_LOTERY_CURENCY) < Config.EVENT_SAVING_SNOWMAN_LOTERY_PRICE) {
            player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
            return;
        }
        removeItem(player, Config.EVENT_SAVING_SNOWMAN_LOTERY_CURENCY, Config.EVENT_SAVING_SNOWMAN_LOTERY_PRICE);
        if (REWARD_CHANCE_SUM != 100.0) {
            LOGGER.info("WARN: Sum != 100: " + REWARD_CHANCE_SUM);
        }
        final Pair<Integer, Long> reward = RandomUtils.pickRandomSortedGroup(REWARD, REWARD_CHANCE_SUM);
        final int rewardItemId = reward.getKey();
        final long rewardItemCount = reward.getValue();
        addItem(player, rewardItemId, rewardItemCount);
    }

    public void captureSnowman() {
        final Location spawnPoint = getRandomSpawnPoint();
        GameObjectsStorage.getPlayers().forEach(player -> {
            Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.SavingSnowman.AnnounceSnowmanCaptured", null, ChatType.ANNOUNCEMENT);
            player.sendPacket(new SystemMessage(2010).addZoneName(spawnPoint).addString("\u0418\u0449\u0438\u0442\u0435 \u0421\u043d\u0435\u0433\u043e\u0432\u0438\u043a\u0430 \u0432 "));
            player.sendPacket(new RadarControl(2, 2, spawnPoint), new RadarControl(0, 1, spawnPoint));
        });
        NpcTemplate template = NpcTemplateHolder.getInstance().getTemplate(13160);
        if (template == null) {
            LOGGER.info("WARNING! events.SavingSnowman.captureSnowman template is null for npc: 13160");
            Thread.dumpStack();
            return;
        }
        final SimpleSpawner sp = new SimpleSpawner(template);
        sp.setLoc(spawnPoint);
        sp.setAmount(1);
        sp.setRespawnDelay(0);
        _snowman = sp.doSpawn(true);
        if (_snowman == null) {
            return;
        }
        template = NpcTemplateHolder.getInstance().getTemplate(13183);
        if (template == null) {
            LOGGER.info("WARNING! events.SavingSnowman.captureSnowman template is null for npc: 13183");
            Thread.dumpStack();
            return;
        }
        final Location pos = Location.findPointToStay(_snowman.getX(), _snowman.getY(), _snowman.getZ(), 100, 120, _snowman.getReflection().getGeoIndex());
        final SimpleSpawner sp2 = new SimpleSpawner(template);
        sp2.setLoc(pos);
        sp2.setAmount(1);
        sp2.setRespawnDelay(0);
        _thomas = sp2.doSpawn(true);
        if (_thomas == null) {
            return;
        }
        _snowmanState = SnowmanState.CAPTURED;
        if (_snowmanShoutTask != null) {
            _snowmanShoutTask.cancel(true);
            _snowmanShoutTask = null;
        }
        _snowmanShoutTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new ShoutTask(), 1L, 60000L);
        if (_eatTask != null) {
            _eatTask.cancel(true);
            _eatTask = null;
        }
        _eatTask = executeTask("events.SavingSnowman.SavingSnowman", "eatSnowman", new Object[0], 600000L);
    }

    @Override
    public String getAppend(Player player, NpcInstance npc, int val) {
        if (val != 0) {
            return "";
        }
        return " (" + Util.formatAdena(Config.EVENT_SAVING_SNOWMAN_LOTERY_PRICE) + " Adena)";
    }

    @Override
    public List<Integer> getNpcIds() {
        return Collections.singletonList(13184);
    }

    private class ListenersImpl implements OnDeathListener, OnPlayerEnterListener {

        @Override
        public void onDeath(final Creature actor, final Creature killer) {
            if (_active && killer != null && _active && SimpleCheckDrop(actor, killer) && Rnd.chance(DROP_ITEM_CHANCE)) {
                final int item_idx = Rnd.get(DROP_ITEM_ID.length);
                ((NpcInstance) actor).dropItem(killer.getPlayer(), DROP_ITEM_ID[item_idx], DROP_ITEM_COUNT[item_idx]);
            }
        }

        @Override
        public void onPlayerEnter(final Player player) {
            if (_active) {
                Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.SavingSnowman.AnnounceEventStarted", null);
            }
        }
    }

    public enum SnowmanState {
        CAPTURED,
        KILLED,
        SAVED
    }

    public class SayTask implements Runnable {
        @Override
        public void run() {
            if (!_active) {
                return;
            }
            _spawns.stream()
                    .filter(s -> s.getCurrentNpcId() == 13184)
                    .forEach(s -> Functions.npcSayCustomMessage(s.getLastSpawn(), "scripts.events.SavingSnowman.SantaSay"));
        }
    }

    public class SantaTask implements Runnable {
        @Override
        public void run() {
            if (!_active) {
                return;
            }
            for (final Location loc : _santaSpawn) {
                final NpcInstance npc = NpcTemplateHolder.getInstance().getTemplate(13186).getNewInstance();
                npc.setAI(new FlyingSantaAI(npc));
                npc.spawnMe(loc);
                npc.setSpawnedLoc(loc);
            }
        }
    }

    public class ShoutTask implements Runnable {
        @Override
        public void run() {
            if (!_active || _snowman == null || _snowmanState != SnowmanState.CAPTURED) {
                return;
            }
            Functions.npcShoutCustomMessage(_snowman, "scripts.events.SavingSnowman.SnowmanShout");
        }
    }

    public class SaveTask implements Runnable {
        @Override
        public void run() {
            if (!_active || _snowmanState == SnowmanState.CAPTURED) {
                return;
            }
            captureSnowman();
        }
    }
}
