package events.Finder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Announcements;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.data.xml.holder.ZoneHolder;
import ru.j2dev.gameserver.geodata.GeoEngine;
import ru.j2dev.gameserver.idfactory.IdFactory;
import ru.j2dev.gameserver.listener.actor.OnDeathListener;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.manager.SpawnManager;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.CharMoveToLocation;
import ru.j2dev.gameserver.network.lineage2.serverpackets.RadarControl;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.templates.ZoneTemplate;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.PositionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

public class Finder extends Functions implements OnInitScriptListener {
    private static final Logger LOGGER = LoggerFactory.getLogger((Class) Finder.class);
    private static final String EventName = "Finder";
    private static final long CaptureTime = 600000L;
    private static final long ShoutTime = 60000L;
    private static final int HostageNpcID = 40017;
    private static final int RaiderNpcID = 40016;
    private static final int RewarderNpcID = 40019;
    private static final String EVENT_FINDER_SPAWN_LIST = "[event_finder_list]";
    private static boolean _active;
    private static NpcInstance _hostage;
    private static NpcInstance _raider;
    private static Location _hostage_loc;
    private static ScheduledFuture<?> _captureTask;
    private static ScheduledFuture<?> _shoutTask;
    private static Map<String, ScheduledFuture<?>> _event_tasks;

    private static boolean isActive() {
        return IsActive("Finder");
    }

    private static Location getRandomSpawnPoint() {
        final ArrayList<ZoneTemplate> zones = ZoneHolder.getInstance().getZones().values().stream().filter(zt -> zt != null && zt.isDefault()).collect(Collectors.toCollection(ArrayList::new));
        final Territory terr = zones.get(Rnd.get(zones.size())).getTerritory();
        return terr.getRandomLoc(0);
    }

    private static void removeHostageAndRaider() {
        if (_hostage != null && _hostage.getSpawn() != null) {
            _hostage.deleteMe();
        }
        if (_raider != null && _raider.getSpawn() != null) {
            _raider.deleteMe();
        }
        _hostage = null;
        _raider = null;
        _hostage_loc = null;
        if (_shoutTask != null) {
            _shoutTask.cancel(true);
            _shoutTask = null;
        }
    }

    public static void spawnRewarder(final Player rewarded) {
        for (final NpcInstance npc : rewarded.getAroundNpc(1500, 300)) {
            if (npc.getNpcId() == 40019) {
                return;
            }
        }
        Location spawnLoc = Location.findPointToStay(rewarded, 300, 400);
        for (int i = 0; i < 20 && !GeoEngine.canSeeCoord(rewarded, spawnLoc.x, spawnLoc.y, spawnLoc.z, false); spawnLoc = Location.findPointToStay(rewarded, 300, 400), ++i) {
        }
        final NpcTemplate template = NpcTemplateHolder.getInstance().getTemplate(40019);
        if (template == null) {
            LOGGER.info("WARNING! events.SavingSnowman.spawnRewarder template is null for npc: 40019");
            Thread.dumpStack();
            return;
        }
        final NpcInstance rewarder = new NpcInstance(IdFactory.getInstance().getNextId(), template);
        rewarder.setLoc(spawnLoc);
        rewarder.setHeading((int) (Math.atan2(spawnLoc.y - rewarded.getY(), spawnLoc.x - rewarded.getX()) * 10430.378350470453) + 32768);
        rewarder.spawnMe();
        Functions.npcSayCustomMessage(rewarder, "scripts.events.Finder.RewarderPhrase1");
        final Location targetLoc = Location.findFrontPosition(rewarded, rewarded, 40, 50);
        rewarder.setSpawnedLoc(targetLoc);
        rewarder.broadcastPacket(new CharMoveToLocation(rewarder.getObjectId(), rewarder.getLoc(), targetLoc));
        executeTask("events.Finder.Finder", "reward", new Object[]{rewarder, rewarded}, 5000L);
    }

    public static void reward(final NpcInstance rewarder, final Player rewarded) {
        if (!_active || rewarder == null || rewarded == null) {
            return;
        }
        Functions.npcSayCustomMessage(rewarder, "scripts.events.Finder.RewarderPhrase2", rewarded.getName());
        Functions.addItem((Playable) rewarded, Config.EVENT_FINDER_REWARD_ID, (long) Config.EVENT_FINDER_ITEM_COUNT);
        executeTask("events.Finder.Finder", "removeRewarder", new Object[]{rewarder}, 5000L);
    }

    public static void removeRewarder(final NpcInstance rewarder) {
        if (!_active || rewarder == null) {
            return;
        }
        Functions.npcSayCustomMessage(rewarder, "scripts.events.Finder.RewarderPhrase3");
        final Location loc = rewarder.getSpawnedLoc();
        final double radian = PositionUtils.convertHeadingToRadian(rewarder.getHeading());
        final int x = loc.x - (int) (Math.sin(radian) * 300.0);
        final int y = loc.y + (int) (Math.cos(radian) * 300.0);
        final int z = loc.z;
        rewarder.broadcastPacket(new CharMoveToLocation(rewarder.getObjectId(), loc, new Location(x, y, z)));
        executeTask("events.Finder.Finder", "unspawnRewarder", new Object[]{rewarder}, 2000L);
    }

    public static void unspawnRewarder(final NpcInstance rewarder) {
        if (!_active || rewarder == null) {
            return;
        }
        rewarder.decayMe();
        rewarder.deleteMe();
        removeHostageAndRaider();
    }

    public static void OnDie(final Creature cha, final Creature killer) {
        if (!_active) {
            return;
        }
        if (killer.isPlayer()) {
            LOGGER.info("killed 0 " + cha.getName());
        }
        if (cha.isNpc() && cha == _raider && killer.isPlayer()) {
            LOGGER.info("killed 1 " + cha.getName());
            final Player saver = killer.getPlayer();
            if (_captureTask != null) {
                _captureTask.cancel(true);
                _captureTask = null;
            }
            if (_shoutTask != null) {
                _shoutTask.cancel(true);
                _shoutTask = null;
            }
            spawnRewarder(saver);
            LOGGER.info("killed 2 " + cha.getName());
            for (final Player player : GameObjectsStorage.getAllPlayersForIterate()) {
                Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.Finder.AnnounceHostageSavedByS1", new String[]{saver.getName()}, ChatType.ANNOUNCEMENT);
            }
            for (final NpcInstance npc : saver.getAroundNpc(1500, 300)) {
                if (npc.getNpcId() == 40017) {
                    LOGGER.info("killed 3 " + cha.getName());
                    Functions.npcSayCustomMessage(npc, "scripts.events.Finder.HostageThx");
                }
            }
        }
    }

    public void startEvent() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (SetActive("Finder", true)) {
            activate();
        } else {
            player.sendMessage("Event 'Finder' already active.");
        }
        _active = true;
        show("admin/events/events.htm", player);
    }

    public void stopEvent() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (SetActive("Finder", false)) {
            diactivate();
            Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.Finder.AnnounceEventStoped", null);
        } else {
            player.sendMessage("Event 'Finder' already diactivated.");
        }
        _active = false;
        show("admin/events/events.htm", player);
    }

    private void activate() {
        SpawnManager.getInstance().spawn("[event_finder_list]");
        if (_event_tasks != null) {
            _event_tasks.values().forEach(sf -> sf.cancel(true));
            _event_tasks.clear();
            _event_tasks = null;
        }
        _event_tasks = ScheduleTimeStarts(new EventTask(), Config.EVENT_FinderHostageStartTime);
        LOGGER.info("Event 'Finder' started on " + Arrays.toString(_event_tasks.keySet().toArray(new String[_event_tasks.keySet().size()])));
    }

    private void diactivate() {
        if (_event_tasks != null) {
            _event_tasks.values().forEach(sf -> sf.cancel(true));
            _event_tasks.clear();
            _event_tasks = null;
        }
        SpawnManager.getInstance().despawn("[event_finder_list]");
    }

    public void spawnHostageAndRaider() {
        if (!_active) {
            return;
        }
        final Location spawnPoint = getRandomSpawnPoint();
        NpcTemplate template = NpcTemplateHolder.getInstance().getTemplate(40017);
        if (template == null) {
            LOGGER.info("WARNING! events.Finder.Finder.spawnHostageAndRaider template is null for npc: 40017");
            Thread.dumpStack();
            return;
        }
        final SimpleSpawner sp = new SimpleSpawner(template);
        sp.setLoc(spawnPoint);
        sp.setAmount(1);
        sp.setRespawnDelay(0);
        sp.stopRespawn();
        _hostage = sp.doSpawn(true);
        if (_hostage == null) {
            return;
        }
        template = NpcTemplateHolder.getInstance().getTemplate(40016);
        if (template == null) {
            LOGGER.info("WARNING! events.Finder.Finder.spawnHostageAndRaider template is null for npc: 40016");
            Thread.dumpStack();
            return;
        }
        final Location pos = Location.findPointToStay(_hostage.getX(), _hostage.getY(), _hostage.getZ(), 100, 120, _hostage.getReflection().getGeoIndex());
        final SimpleSpawner sp2 = new SimpleSpawner(template);
        sp2.setLoc(pos);
        sp2.setAmount(1);
        sp2.setRespawnDelay(0);
        sp2.stopRespawn();
        (_raider = sp2.doSpawn(true)).addListener(new OnDeathListenerImpl());
        if (_captureTask != null) {
            _captureTask.cancel(true);
            _captureTask = null;
        }
        _captureTask = ThreadPoolManager.getInstance().schedule(new HostageKilledTask(), 600000L);
        if (_shoutTask != null) {
            _shoutTask.cancel(true);
            _shoutTask = null;
        }
        _shoutTask = ThreadPoolManager.getInstance().schedule(new ShoutTask(), 60000L);
        _hostage_loc = _hostage.getLoc();
        for (final Player player : GameObjectsStorage.getAllPlayersForIterate()) {
            Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.Finder.AnnounceHostageTaken", null, ChatType.ANNOUNCEMENT);
        }
    }

    public void GetPoint() {
        final Player player = getSelf();
        if (!_active || player.isActionsDisabled() || player.isSitting() || player.getLastNpc() == null || player.getLastNpc().getDistance(player) > 300.0) {
            return;
        }
        if (!player.isQuestContinuationPossible(true)) {
            return;
        }
        if (_hostage_loc != null) {
            player.sendPacket(new SystemMessage(2010).addZoneName(_hostage_loc).addString(new CustomMessage("scripts.events.Finder.HostageAtS1", player, new Object[0]).toString()));
            player.sendPacket(new RadarControl(2, 2, _hostage_loc), new RadarControl(0, 1, _hostage_loc));
        } else {
            player.sendMessage(new CustomMessage("scripts.events.Finder.NoHostage", player));
        }
    }

    @Override
    public void onInit() {
        if (isActive()) {
            _active = true;
            activate();
        } else {
            LOGGER.info("Loaded Event: 'Finder' [state: deactivated]");
        }
    }

    private class OnDeathListenerImpl implements OnDeathListener {

        @Override
        public void onDeath(final Creature actor, final Creature killer) {
            actor.removeListener(this);
            if (!_active || killer == null) {
                return;
            }
            if (killer.isPlayer()) {
                LOGGER.info("killed 0 " + actor.getName());
            }
            if (actor.isNpc() && actor == _raider && killer.isPlayer()) {
                LOGGER.info("killed 1 " + actor.getName());
                final Player saver = killer.getPlayer();
                if (_captureTask != null) {
                    _captureTask.cancel(true);
                    _captureTask = null;
                }
                if (_shoutTask != null) {
                    _shoutTask.cancel(true);
                    _shoutTask = null;
                }
                spawnRewarder(saver);
                LOGGER.info("killed 2 " + actor.getName());
                for (final Player player : GameObjectsStorage.getAllPlayersForIterate()) {
                    Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.Finder.AnnounceHostageSavedByS1", new String[]{saver.getName()}, ChatType.ANNOUNCEMENT);
                }
                saver.getAroundNpc(1500, 300).stream().filter(npc -> npc.getNpcId() == 40017).forEach(npc -> {
                    LOGGER.info("killed 3 " + actor.getName());
                    Functions.npcSayCustomMessage(npc, "scripts.events.Finder.HostageThx");
                });
            }
        }
    }

    private class EventTask implements Runnable {
        @Override
        public void run() {
            spawnHostageAndRaider();
        }
    }

    private class HostageKilledTask implements Runnable {
        @Override
        public void run() {
            if (!_active || _hostage == null || _raider == null) {
                return;
            }
            removeHostageAndRaider();
            GameObjectsStorage.getAllPlayersForIterate().forEach(player -> Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.Finder.AnnounceHostageKilled", null, ChatType.ANNOUNCEMENT));
        }
    }

    public class ShoutTask implements Runnable {
        @Override
        public void run() {
            if (!_active || _hostage == null || _hostage_loc == null) {
                return;
            }
            Functions.npcShoutCustomMessage(_hostage, "scripts.events.Finder.HostageShout");
        }
    }
}
