package events.Halloween;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Announcements;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.ai.CtrlIntention;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.handler.npcdialog.INpcDialogAppender;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Halloween extends Functions implements OnInitScriptListener, INpcDialogAppender {
    private static final Logger LOGGER = LoggerFactory.getLogger(Halloween.class);
    private static final String EVENT_NAME = "HalloweenEvent";
    private static final int[][] GHOST_SPAWN = {{81945, 148597, -3472, 600, 15}, {147456, 27480, -2229, 500, 12}, {82610, 55643, -1550, 200, 6}, {18650, 145436, -3153, 350, 9}, {111389, 220161, -3700, 420, 10}, {-14225, 123540, -3121, 200, 7}, {147723, -56388, -2807, 150, 5}, {87356, -141767, -1344, 220, 7}};
    private static final String[] SKILLDIE_SPAWN = {"Hot Springs:147607,-114850,-2002", "Rainbow Springs Chateau:152587,-126642,-2315", "Forge of the Gods:167693,-113024,-2844", "Wall of Argos:176338,-49702,-3334", "Swamp of Screams:94637,-60140,-2475", "Restless Forest:65725,-48176,-2823", "Valley of Saints:66767,-73106,-3718", "Windtail Waterfall:41303,-92076,-3703", "Cursed Village:57129,-41255,-3177", "Stakato Nest:88413,-43714,-2193", "Beast Farm:44735,-87574,-2578", "Grave Robber Hideout:46146,-106761,-1516", "Crypts of Disgrace:43242,-120040,-3408", "Den of Evil:67288,-112072,-2176", "Archaic Laboratory:92168,-115144,-3344", "Plunderous Plains:127736,-149416,-3736", "Brigand Stronghold:124520,-161496,-1168", "Deamon Fortress:100006,-52612,-673", "Borderland Fortress:155951,-70319,-2804", "Lost Nest:24044,-10452,-2589", "Primeval Plains Waterfall:6810,-12014,-3674", "Mimir's Forest:-82051,51084,-3339", "Chromatic Highlands:154735,152587,-3684"};
    private static final String en = "<br1>[scripts_events.Halloween.Halloween:show|\"Halloween Event\"]<br1>";
    private static final String ru = "<br1>[scripts_events.Halloween.Halloween:show|\"\u0418\u0432\u0435\u043d\u0442 \u0425\u044d\u043b\u043b\u043e\u0443\u0438\u043d\"]<br1>";
    private static final ArrayList<NpcInstance> _ghostSpawn = new ArrayList<>();
    private static boolean _active;
    private static ScheduledFuture<?> _eventTask;
    private static NpcInstance SkooldieInstance;

    private static boolean isActive() {
        return IsActive("HalloweenEvent");
    }

    public static void OnPlayerEnter(final Player player) {
        if (_active) {
            Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.Halloween.EventActive", null);
        }
    }

    public static void SpawnGhosts(final int idx) {
        final ArrayList<Location> loc_list;
        final double step = 6.283185307179586 / GHOST_SPAWN[idx][4];
        loc_list = IntStream.range(0, GHOST_SPAWN[idx][4]).mapToDouble(i -> step * i).mapToObj(rad -> new Location(GHOST_SPAWN[idx][0] + (int) (GHOST_SPAWN[idx][3] * Math.cos(rad)), GHOST_SPAWN[idx][1] + (int) (GHOST_SPAWN[idx][3] * Math.sin(rad)), GHOST_SPAWN[idx][2])).collect(Collectors.toCollection(ArrayList::new));
        final Location[] locs = loc_list.toArray(new Location[0]);
        loc_list.clear();
        IntStream.range(0, locs.length).forEach(j -> {
            final NpcInstance npc = NpcTemplateHolder.getInstance().getTemplate(Config.EVENT_PUMPKIN_GHOST_ID).getNewInstance();
            npc.setAI(new PumpkinGhostAI(npc, locs, j, Config.EVENT_PUMPKIN_DROP_CHANCE, Config.EVENT_PUMPKIN_DROP_ITEMS));
            npc.setFlying(false);
            npc.setTargetable(false);
            npc.setShowName(false);
            npc.spawnMe(locs[j]);
            npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
            _ghostSpawn.add(npc);
        });
        Announcements.getInstance().announceByCustomMessage("scripts.events.Halloween.PumpkinGhost.spawn." + idx, null);
    }

    public static void DespawnGhost() {
        if (_ghostSpawn.isEmpty()) {
            return;
        }
        for (final NpcInstance npc : _ghostSpawn) {
            npc.deleteMe();
        }
        _ghostSpawn.clear();
    }

    public static void RunEvent() {
        SkooldieDespawn();
        DespawnGhost();
        SpawnGhosts(Rnd.get(GHOST_SPAWN.length));
        executeTask("events.Halloween.Halloween", "DespawnGhost", new Object[0], (long) Config.EVENT_PUMPKIN_GHOST_SHOW_TIME);
        executeTask("events.Halloween.Halloween", "SkooldieSpawn", new Object[0], (long) (Config.EVENT_PUMPKIN_GHOST_SHOW_TIME - 30000));
        executeTask("events.Halloween.Halloween", "SkooldieDespawn", new Object[0], (long) (Config.EVENT_PUMPKIN_GHOST_SHOW_TIME + Config.EVENT_SKOOLDIE_TIME));
    }

    public static void SkooldieSpawn() {
        if (SkooldieInstance != null) {
            SkooldieInstance.deleteMe();
        }
        final int point = Rnd.get(SKILLDIE_SPAWN.length);
        final String[] spwn = SKILLDIE_SPAWN[point].split(":");
        SkooldieInstance = Functions.spawn(Location.parseLoc(spwn[1]), Config.EVENT_SKOOLDIE_REWARDER);
        Announcements.getInstance().announceByCustomMessage("scripts.events.Halloween.SkooldieSpawned", new String[]{spwn[0]});
    }

    public static void SkooldieDespawn() {
        if (SkooldieInstance != null) {
            SkooldieInstance.deleteMe();
        }
        SkooldieInstance = null;
    }

    public static void start() {
        stop();
        _eventTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new EventRunner(), 120000L, (long) Config.EVENT_PUMPKIN_DELAY);
    }

    public static void stop() {
        if (_eventTask != null) {
            _eventTask.cancel(true);
            _eventTask = null;
        }
        DespawnGhost();
        SkooldieDespawn();
    }

    public void startEvent() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (SetActive("HalloweenEvent", true)) {
            start();
            LOGGER.info("Event: 'HalloweenEvent' started.");
            Announcements.getInstance().announceByCustomMessage("scripts.events.Halloween.AnnounceEventStarted", null);
        } else {
            player.sendMessage("Event 'HalloweenEvent' already started.");
        }
        _active = true;
        show("admin/events/events.htm", player);
    }

    public void stopEvent() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        DespawnGhost();
        SkooldieDespawn();
        if (SetActive("HalloweenEvent", false)) {
            stop();
            LOGGER.info("Event: 'HalloweenEvent' stopped.");
            Announcements.getInstance().announceByCustomMessage("scripts.events.Halloween.AnnounceEventStoped", null);
        } else {
            player.sendMessage("Event 'HalloweenEvent' not started.");
        }
        _active = false;
        show("admin/events/events.htm", player);
    }


    @Override
    public String getAppend(Player player, NpcInstance npc, int val) {
        if (!_active || val != 0) {
            return "";
        }
        if (player == null) {
            return "";
        }
        return player.isLangRus() ? "<br1>[scripts_events.Halloween.Halloween:show|\"\u0418\u0432\u0435\u043d\u0442 \u0425\u044d\u043b\u043b\u043e\u0443\u0438\u043d\"]<br1>" : "<br1>[scripts_events.Halloween.Halloween:show|\"Halloween Event\"]<br1>";
    }

    @Override
    public List<Integer> getNpcIds() {
        return Collections.singletonList(40029);
    }

    public void show() {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (player == null || npc == null) {
            return;
        }
        show("scripts/events/halloween/exchange.htm", player);
    }

    public void exchange() {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (player == null || npc == null) {
            return;
        }
        final long cnt = Functions.getItemCount(player, Config.EVENT_HALLOWEEN_CANDY);
        if (cnt > 0L) {
            Functions.removeItem(player, Config.EVENT_HALLOWEEN_CANDY, (long) Config.EVENT_HALLOWEEN_CANDY_ITEM_COUNT_NEEDED);
            Functions.addItem(player, Config.EVENT_HALLOWEEN_TOY_CHEST, (long) Config.EVENT_HALLOWEEN_TOY_CHEST_REWARD_AMOUNT);
            Announcements.getInstance().announceByCustomMessage("scripts.events.Halloween.SkooldieFind", new String[]{player.getName()});
            SkooldieDespawn();
            DespawnGhost();
        } else {
            show("scripts/events/halloween/noitem.htm", player);
        }
    }

    @Override
    public void onInit() {
        if (isActive()) {
            _active = true;
            start();
            LOGGER.info("Loaded Event: 'HalloweenEvent' [state: activated]");
        } else {
            LOGGER.info("Loaded Event: 'HalloweenEvent' [state: deactivated]");
        }
    }

    public static class EventRunner implements Runnable {
        @Override
        public void run() {
            RunEvent();
        }
    }
}
