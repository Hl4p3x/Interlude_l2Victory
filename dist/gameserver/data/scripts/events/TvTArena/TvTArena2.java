package events.TvTArena;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.Announcements;
import ru.j2dev.gameserver.cache.HtmCache;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.handler.npcdialog.INpcDialogAppender;
import ru.j2dev.gameserver.handler.npcdialog.NpcDialogAppenderHolder;
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

public class TvTArena2 extends Functions implements OnInitScriptListener, INpcDialogAppender {
    private static final Logger LOGGER = LoggerFactory.getLogger(TvTArena2.class);
    private static TvTTemplate _instance;

    private final List<NpcInstance> _spawns;

    public TvTArena2() {
        _spawns = new ArrayList<>();
    }

    public static TvTTemplate getInstance() {
        if (_instance == null) {
            _instance = new TvTArena2Impl();
        }
        return _instance;
    }

    @Override
    public void onInit() {
        CharListenerList.addGlobal(new Listeners());
        NpcDialogAppenderHolder.getInstance().register(this);
        getInstance().onInit();
        if (isActive()) {
            spawnEventManagers();
            LOGGER.info("Loaded Event: TvT Arena 2 [state: activated]");
        } else {
            LOGGER.info("Loaded Event: TvT Arena 2 [state: deactivated]");
        }
    }

    private class Listeners implements OnDeathListener, OnPlayerExitListener, OnTeleportListener {
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
            return HtmCache.getInstance().getNotNull("scripts/events/TvTArena/31391.htm", player) + HtmCache.getInstance().getNotNull("scripts/events/TvTArena/31391-4.htm", player);
        }
        return HtmCache.getInstance().getNotNull("scripts/events/TvTArena/31391.htm", player);
    }

    @Override
    public List<Integer> getNpcIds() {
        return Collections.singletonList(31391);
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
        return IsActive("TvT Arena 2");
    }

    public void startEvent() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (SetActive("TvT Arena 2", true)) {
            spawnEventManagers();
            LOGGER.info("Event: TvT Arena 2 started.");
            Announcements.getInstance().announceToAll("\u041d\u0430\u0447\u0430\u043b\u0441\u044f TvT Arena 2 \u044d\u0432\u0435\u043d\u0442.");
        } else {
            player.sendMessage("TvT Arena 2 Event already started.");
        }
        show("admin/events/events.htm", player);
    }

    public void stopEvent() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (SetActive("TvT Arena 2", false)) {
            ServerVariables.unset("TvT Arena 2");
            unSpawnEventManagers();
            stop();
            LOGGER.info("TvT Arena 2 Event stopped.");
            Announcements.getInstance().announceToAll("TvT Arena 2 \u044d\u0432\u0435\u043d\u0442 \u043e\u043a\u043e\u043d\u0447\u0435\u043d.");
        } else {
            player.sendMessage("TvT Arena 2 Event not started.");
        }
        show("admin/events/events.htm", player);
    }

    private void spawnEventManagers() {
        final int[][] EVENT_MANAGERS = {{82840, 149048, -3472, 0}};
        final NpcTemplate template = NpcTemplateHolder.getInstance().getTemplate(31391);
        for (final int[] element : EVENT_MANAGERS) {
            final SimpleSpawner sp = new SimpleSpawner(template);
            sp.setLocx(element[0]);
            sp.setLocy(element[1]);
            sp.setLocz(element[2]);
            sp.setHeading(element[3]);
            final NpcInstance npc = sp.doSpawn(true);
            npc.setName("Arena 2");
            npc.setTitle("TvT Event");
            _spawns.add(npc);
        }
    }

    private void unSpawnEventManagers() {
        _spawns.forEach(GameObject::deleteMe);
        _spawns.clear();
    }

    private static class TvTArena2Impl extends TvTTemplate {
        @Override
        protected void onInit() {
            _managerId = 31391;
            _className = "TvTArena2";
            _status = 0;
            _team1list = new CopyOnWriteArrayList<>();
            _team2list = new CopyOnWriteArrayList<>();
            _team1live = new CopyOnWriteArrayList<>();
            _team2live = new CopyOnWriteArrayList<>();
            _zoneListener = new ZoneListener();
            (_zone = ReflectionUtils.getZone("[tvt_arena2]")).addListener(_zoneListener);
            _team1points = new ArrayList<>();
            _team2points = new ArrayList<>();
            _team1points.add(new Location(-77724, -47901, -11518, -11418));
            _team1points.add(new Location(-77718, -48080, -11518, -11418));
            _team1points.add(new Location(-77699, -48280, -11518, -11418));
            _team1points.add(new Location(-77777, -48442, -11518, -11418));
            _team1points.add(new Location(-77863, -48622, -11518, -11418));
            _team1points.add(new Location(-78002, -48714, -11518, -11418));
            _team1points.add(new Location(-78168, -48835, -11518, -11418));
            _team1points.add(new Location(-78353, -48851, -11518, -11418));
            _team1points.add(new Location(-78543, -48864, -11518, -11418));
            _team1points.add(new Location(-78709, -48784, -11518, -11418));
            _team1points.add(new Location(-78881, -48702, -11518, -11418));
            _team1points.add(new Location(-78981, -48555, -11518, -11418));
            _team2points.add(new Location(-79097, -48400, -11518, -11418));
            _team2points.add(new Location(-79107, -48214, -11518, -11418));
            _team2points.add(new Location(-79125, -48027, -11518, -11418));
            _team2points.add(new Location(-79047, -47861, -11518, -11418));
            _team2points.add(new Location(-78965, -47689, -11518, -11418));
            _team2points.add(new Location(-78824, -47594, -11518, -11418));
            _team2points.add(new Location(-78660, -47474, -11518, -11418));
            _team2points.add(new Location(-78483, -47456, -11518, -11418));
            _team2points.add(new Location(-78288, -47440, -11518, -11418));
            _team2points.add(new Location(-78125, -47515, -11518, -11418));
            _team2points.add(new Location(-77953, -47599, -11518, -11418));
            _team2points.add(new Location(-77844, -47747, -11518, -11418));
        }
    }
}
