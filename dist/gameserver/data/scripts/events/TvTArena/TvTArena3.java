package events.TvTArena;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.Announcements;
import ru.j2dev.gameserver.cache.HtmCache;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.handler.npcdialog.INpcDialogAppender;
import ru.j2dev.gameserver.listener.actor.OnDeathListener;
import ru.j2dev.gameserver.listener.actor.player.OnPlayerExitListener;
import ru.j2dev.gameserver.listener.actor.player.OnTeleportListener;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.manager.ServerVariables;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.SimpleSpawner;
import ru.j2dev.gameserver.model.actor.listener.CharListenerList;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.ReflectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TvTArena3 extends Functions implements OnInitScriptListener, INpcDialogAppender {
    private static final Logger LOGGER = LoggerFactory.getLogger(TvTArena3.class);
    private static TvTTemplate _instance;

    private final List<NpcInstance> _spawns;

    public TvTArena3() {
        _spawns = new ArrayList<>();
    }

    public static TvTTemplate getInstance() {
        if (_instance == null) {
            _instance = new TvTArena3Impl();
        }
        return _instance;
    }

    @Override
    public void onInit() {
        CharListenerList.addGlobal(new ListenersImpl());
        getInstance().onInit();
        if (isActive()) {
            spawnEventManagers();
            LOGGER.info("Loaded Event: TvT Arena 3 [state: activated]");
        } else {
            LOGGER.info("Loaded Event: TvT Arena 3 [state: deactivated]");
        }
    }

    private class ListenersImpl implements OnDeathListener, OnTeleportListener, OnPlayerExitListener {

        @Override
        public void onDeath(final Creature cha, final Creature killer) {
            getInstance().onDeath(cha, killer);
        }

        @Override
        public void onPlayerExit(final Player player) {
            getInstance().onPlayerExit(player);
        }

        @Override
        public void onTeleport(final Player player, final int x, final int y, final int z, final Reflection reflection) {
            getInstance().onTeleport(player);
        }
    }

    @Override
    public String getAppend(Player player, NpcInstance npc, int val) {
        if (val != 0) {
            return "";
        }
        if (player.isGM()) {
            return HtmCache.getInstance().getNotNull("scripts/events/TvTArena/31392.htm", player) + HtmCache.getInstance().getNotNull("scripts/events/TvTArena/31392-4.htm", player);
        }
        return HtmCache.getInstance().getNotNull("scripts/events/TvTArena/31392.htm", player);
    }

    @Override
    public List<Integer> getNpcIds() {
        return Collections.singletonList(31392);
    }

    public void create1() {
        getInstance().template_create1(getSelf());
    }

    public void register() {
        getInstance().template_register(getSelf());
    }

    public void check1(final String[] var) {
        getInstance().template_check1(getSelf(), getNpc(), var);
    }

    public void register_check() {
        getInstance().template_register_check(getSelf());
    }

    public void stop() {
        getInstance().template_stop();
    }

    public void announce() {
        getInstance().template_announce();
    }

    public void prepare() {
        getInstance().template_prepare();
    }

    public void start() {
        getInstance().template_start();
    }

    public void timeOut() {
        getInstance().template_timeOut();
    }

    private boolean isActive() {
        return IsActive("TvT Arena 3");
    }

    public void startEvent() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (SetActive("TvT Arena 3", true)) {
            spawnEventManagers();
            LOGGER.info("Event: TvT Arena 3 started.");
            Announcements.getInstance().announceToAll("\u041d\u0430\u0447\u0430\u043b\u0441\u044f TvT Arena 3 \u044d\u0432\u0435\u043d\u0442.");
        } else {
            player.sendMessage("TvT Arena 3 Event already started.");
        }
        show("admin/events/events.htm", player);
    }

    public void stopEvent() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (SetActive("TvT Arena 3", false)) {
            ServerVariables.unset("TvT Arena 3");
            unSpawnEventManagers();
            stop();
            LOGGER.info("TvT Arena 3 Event stopped.");
            Announcements.getInstance().announceToAll("TvT Arena 3 \u044d\u0432\u0435\u043d\u0442 \u043e\u043a\u043e\u043d\u0447\u0435\u043d.");
        } else {
            player.sendMessage("TvT Arena 3 Event not started.");
        }
        show("admin/events/events.htm", player);
    }

    private void spawnEventManagers() {
        final int[][] EVENT_MANAGERS = {{82840, 148936, -3472, 0}};
        final NpcTemplate template = NpcTemplateHolder.getInstance().getTemplate(31392);
        for (final int[] element : EVENT_MANAGERS) {
            final SimpleSpawner sp = new SimpleSpawner(template);
            sp.setLocx(element[0]);
            sp.setLocy(element[1]);
            sp.setLocz(element[2]);
            sp.setHeading(element[3]);
            final NpcInstance npc = sp.doSpawn(true);
            npc.setName("Arena 3");
            npc.setTitle("TvT Event");
            _spawns.add(npc);
        }
    }

    private void unSpawnEventManagers() {
        _spawns.forEach(GameObject::deleteMe);
        _spawns.clear();
    }

    private static class TvTArena3Impl extends TvTTemplate {
        @Override
        protected void onInit() {
            _managerId = 31392;
            _className = "TvTArena3";
            _status = 0;
            _team1list = new CopyOnWriteArrayList<>();
            _team2list = new CopyOnWriteArrayList<>();
            _team1live = new CopyOnWriteArrayList<>();
            _team2live = new CopyOnWriteArrayList<>();
            _zoneListener = new ZoneListener();
            (_zone = ReflectionUtils.getZone("[tvt_arena3]")).addListener(_zoneListener);
            _team1points = new ArrayList<>();
            _team2points = new ArrayList<>();
            _team1points.add(new Location(-79383, -52724, -11518, -11418));
            _team1points.add(new Location(-79558, -52793, -11518, -11418));
            _team1points.add(new Location(-79726, -52867, -11518, -11418));
            _team1points.add(new Location(-79911, -52845, -11518, -11418));
            _team1points.add(new Location(-80098, -52822, -11518, -11418));
            _team1points.add(new Location(-80242, -52714, -11518, -11418));
            _team1points.add(new Location(-80396, -52597, -11518, -11418));
            _team1points.add(new Location(-80466, -52422, -11518, -11418));
            _team1points.add(new Location(-80544, -52250, -11518, -11418));
            _team1points.add(new Location(-80515, -52054, -11518, -11418));
            _team1points.add(new Location(-80496, -51878, -11518, -11418));
            _team1points.add(new Location(-80386, -51739, -11518, -11418));
            _team2points.add(new Location(-80270, -51582, -11518, -11418));
            _team2points.add(new Location(-80107, -51519, -11518, -11418));
            _team2points.add(new Location(-79926, -51435, -11518, -11418));
            _team2points.add(new Location(-79739, -51465, -11518, -11418));
            _team2points.add(new Location(-79554, -51482, -11518, -11418));
            _team2points.add(new Location(-79399, -51600, -11518, -11418));
            _team2points.add(new Location(-79254, -51711, -11518, -11418));
            _team2points.add(new Location(-79181, -51884, -11518, -11418));
            _team2points.add(new Location(-79114, -52057, -11518, -11418));
            _team2points.add(new Location(-79133, -52246, -11518, -11418));
            _team2points.add(new Location(-79156, -52427, -11518, -11418));
            _team2points.add(new Location(-79275, -52583, -11518, -11418));
        }
    }
}
