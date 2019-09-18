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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TvTArena1 extends Functions implements OnInitScriptListener, INpcDialogAppender {
    private static final Logger LOGGER = LoggerFactory.getLogger(TvTArena1.class);
    private static TvTTemplate _instance;

    private final List<NpcInstance> _spawns;

    public TvTArena1() {
        _spawns = new ArrayList<>();
    }

    public static TvTTemplate getInstance() {
        if (_instance == null) {
            _instance = new TvTArena1Impl();
        }
        return _instance;
    }

    @Override
    public void onInit() {
        CharListenerList.addGlobal(new Listeners());
        getInstance().onInit();
        if (isActive()) {
            spawnEventManagers();
            LOGGER.info("Loaded Event: TvT Arena 1 [state: activated]");
        } else {
            LOGGER.info("Loaded Event: TvT Arena 1 [state: deactivated]");
        }
    }

    @Override
    public String getAppend(Player player, NpcInstance npc, int val) {
        if (val != 0) {
            return "";
        }
        if (player.isGM()) {
            return HtmCache.getInstance().getNotNull("scripts/events/TvTArena/31390.htm", player) + HtmCache.getInstance().getNotNull("scripts/events/TvTArena/31390-4.htm", player);
        }
        return HtmCache.getInstance().getNotNull("scripts/events/TvTArena/31390.htm", player);
    }

    @Override
    public List<Integer> getNpcIds() {
        return Collections.singletonList(31390);
    }

    private class Listeners implements OnDeathListener, OnTeleportListener, OnPlayerExitListener {

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
        return IsActive("TvT Arena 1");
    }

    public void startEvent() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (SetActive("TvT Arena 1", true)) {
            spawnEventManagers();
            LOGGER.info("Event: TvT Arena 1 started.");
            Announcements.getInstance().announceToAll("\u041d\u0430\u0447\u0430\u043b\u0441\u044f TvT Arena 1 \u044d\u0432\u0435\u043d\u0442.");
        } else {
            player.sendMessage("TvT Arena 1 Event already started.");
        }
        show("admin/events/events.htm", player);
    }

    public void stopEvent() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (SetActive("TvT Arena 1", false)) {
            ServerVariables.unset("TvT Arena 1");
            unSpawnEventManagers();
            stop();
            LOGGER.info("TvT Arena 1 Event stopped.");
            Announcements.getInstance().announceToAll("TvT Arena 1 \u044d\u0432\u0435\u043d\u0442 \u043e\u043a\u043e\u043d\u0447\u0435\u043d.");
        } else {
            player.sendMessage("TvT Arena 1 Event not started.");
        }
        show("admin/events/events.htm", player);
    }

    private void spawnEventManagers() {
        final int[][] EVENT_MANAGERS = {{82840, 149167, -3495, 0}};
        final NpcTemplate template = NpcTemplateHolder.getInstance().getTemplate(31390);
        Arrays.stream(EVENT_MANAGERS).forEach(element -> {
            final SimpleSpawner sp = new SimpleSpawner(template);
            sp.setLocx(element[0]);
            sp.setLocy(element[1]);
            sp.setLocz(element[2]);
            sp.setHeading(element[3]);
            final NpcInstance npc = sp.doSpawn(true);
            npc.setName("Arena 1");
            npc.setTitle("TvT Event");
            _spawns.add(npc);
        });
    }

    private void unSpawnEventManagers() {
        _spawns.forEach(GameObject::deleteMe);
        _spawns.clear();
    }

    private static class TvTArena1Impl extends TvTTemplate {
        @Override
        protected void onInit() {
            _managerId = 31390;
            _className = "TvTArena1";
            _status = 0;
            _team1list = new CopyOnWriteArrayList<>();
            _team2list = new CopyOnWriteArrayList<>();
            _team1live = new CopyOnWriteArrayList<>();
            _team2live = new CopyOnWriteArrayList<>();
            _zoneListener = new ZoneListener();
            (_zone = ReflectionUtils.getZone("[tvt_arena1]")).addListener(_zoneListener);
            _team1points = new ArrayList<>();
            _team2points = new ArrayList<>();
            _team1points.add(new Location(-81806, -44865, -11418));
            _team1points.add(new Location(-81617, -44893, -11418));
            _team1points.add(new Location(-81440, -44945, -11418));
            _team1points.add(new Location(-81301, -48066, -11418));
            _team1points.add(new Location(-81168, -45208, -11418));
            _team1points.add(new Location(-81114, -46379, -11418));
            _team1points.add(new Location(-81068, -45570, -11418));
            _team1points.add(new Location(-81114, -45728, -11418));
            _team1points.add(new Location(-81162, -45934, -11418));
            _team1points.add(new Location(-81280, -46045, -11418));
            _team1points.add(new Location(-81424, -46196, -11418));
            _team1points.add(new Location(-81578, -46238, -11418));
            _team2points.add(new Location(-81792, -46299, -11418));
            _team2points.add(new Location(-81959, -46247, -11418));
            _team2points.add(new Location(-82147, -46206, -11418));
            _team2points.add(new Location(-82256, -46093, -11418));
            _team2points.add(new Location(-82418, -45940, -11418));
            _team2points.add(new Location(-82455, -45779, -11418));
            _team2points.add(new Location(-82513, -45573, -11418));
            _team2points.add(new Location(-82464, -45499, -11418));
            _team2points.add(new Location(-82421, -45215, -11418));
            _team2points.add(new Location(-82308, -45106, -11418));
            _team2points.add(new Location(-82160, -44948, -11418));
            _team2points.add(new Location(-81978, -44904, -11418));
        }
    }
}
