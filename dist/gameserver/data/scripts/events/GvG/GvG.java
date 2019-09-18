package events.GvG;

import instances.GvGInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.lang.reference.HardReferences;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Announcements;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.data.xml.holder.InstantZoneHolder;
import ru.j2dev.gameserver.data.xml.holder.ResidenceHolder;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.manager.ServerVariables;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.base.TeamType;
import ru.j2dev.gameserver.model.entity.olympiad.OlympiadPlayersManager;
import ru.j2dev.gameserver.model.entity.residence.Castle;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.templates.InstantZone;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.TimeUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class GvG extends Functions implements OnInitScriptListener {
    public static final Location TEAM1_LOC = new Location(80296, 88504, -2880);
    public static final Location TEAM2_LOC = new Location(77704, 93400, -2880);
    public static final Location RETURN_LOC = new Location(43816, -48232, -822);
    private static final Logger LOGGER = LoggerFactory.getLogger(GvG.class);
    private static final String EVENT_NAME = "GvG";
    private static final long regActiveTime = 600000L;
    private static final List<HardReference<Player>> leaderList = new CopyOnWriteArrayList<>();
    private static boolean _active;
    private static boolean _isRegistrationActive;
    private static ScheduledFuture<?> _globalTask;
    private static ScheduledFuture<?> _regTask;
    private static ScheduledFuture<?> _countdownTask1;
    private static ScheduledFuture<?> _countdownTask2;
    private static ScheduledFuture<?> _countdownTask3;

    private static boolean isActive() {
        return IsActive("GvG");
    }

    private static void initTimer() {
        final long day = 86400000L;
        final Calendar ci = Calendar.getInstance();
        final String startTimeStr = Config.EVENT_GVG_START_TIME;
        final String[] startTimeStrParts = startTimeStr.split(":");
        ci.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startTimeStrParts[0]));
        ci.set(Calendar.MINUTE, (startTimeStrParts.length > 1) ? Integer.parseInt(startTimeStrParts[1]) : 0);
        ci.set(Calendar.SECOND, (startTimeStrParts.length > 2) ? Integer.parseInt(startTimeStrParts[2]) : 0);
        long delay = ci.getTimeInMillis() - System.currentTimeMillis();
        if (delay < 0L) {
            delay += day;
        }
        if (_globalTask != null) {
            _globalTask.cancel(true);
        }
        _globalTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Launch(), delay, day);
        LOGGER.info("Event 'GvG' will start at " + TimeUtils.toSimpleFormat(_globalTask.getDelay(TimeUnit.MILLISECONDS) + System.currentTimeMillis()) + ".");
    }

    private static boolean canBeStarted() {
        return ResidenceHolder.getInstance().getResidenceList(Castle.class).stream().noneMatch(c -> c.getSiegeEvent() != null && c.getSiegeEvent().isInProgress());
    }

    public static void activateEvent() {
        if (isActive() && canBeStarted()) {
            _regTask = ThreadPoolManager.getInstance().schedule(new RegTask(), Config.EVENT_GVG_REG_TIME);
            if (Config.EVENT_GVG_REG_TIME > 120000L) {
                if (Config.EVENT_GVG_REG_TIME > 300000L) {
                    _countdownTask3 = ThreadPoolManager.getInstance().schedule(new Countdown(5), Config.EVENT_GVG_REG_TIME - 300000L);
                }
                _countdownTask1 = ThreadPoolManager.getInstance().schedule(new Countdown(2), Config.EVENT_GVG_REG_TIME - 120000L);
                _countdownTask2 = ThreadPoolManager.getInstance().schedule(new Countdown(1), Config.EVENT_GVG_REG_TIME - 60000L);
            }
            ServerVariables.set("GvG", "on");
            LOGGER.info("Event 'GvG' activated.");
            Announcements.getInstance().announceByCustomMessage("scripts.events.gvg.AnnounceEventStartRegistration", null);
            Announcements.getInstance().announceByCustomMessage("scripts.events.gvg.regtime", new String[]{String.valueOf(Config.EVENT_GVG_REG_TIME / 60000L)});
            _active = true;
            _isRegistrationActive = true;
        }
        LOGGER.info("Event 'GvG' will start next at " + TimeUtils.toSimpleFormat(_globalTask.getDelay(TimeUnit.MILLISECONDS) + System.currentTimeMillis()) + ".");
    }

    public static void deactivateEvent() {
        if (isActive()) {
            stopTimers();
            ServerVariables.unset("GvG");
            LOGGER.info("Event 'GvG' canceled.");
            Announcements.getInstance().announceByCustomMessage("scripts.events.gvg.eventsiscanceled", null);
            _active = false;
            _isRegistrationActive = false;
            leaderList.clear();
        }
    }

    private static void stopTimers() {
        if (_regTask != null) {
            _regTask.cancel(false);
            _regTask = null;
        }
        if (_countdownTask1 != null) {
            _countdownTask1.cancel(false);
            _countdownTask1 = null;
        }
        if (_countdownTask2 != null) {
            _countdownTask2.cancel(false);
            _countdownTask2 = null;
        }
        if (_countdownTask3 != null) {
            _countdownTask3.cancel(false);
            _countdownTask3 = null;
        }
    }

    private static void prepare() {
        checkPlayers();
        shuffleGroups();
        if (isActive()) {
            stopTimers();
            _active = false;
            _isRegistrationActive = false;
        }
        if (leaderList.size() < 2) {
            leaderList.clear();
            Announcements.getInstance().announceByCustomMessage("scripts.events.gvg.tournamentcanceled", null);
            return;
        }
        Announcements.getInstance().announceByCustomMessage("scripts.events.gvg.tiurnamentstated", null);
        start();
    }

    private static int checkPlayer(final Player player, final boolean doCheckLeadership) {
        if (!player.isOnline()) {
            return 1;
        }
        if (!player.isInParty()) {
            return 2;
        }
        if (doCheckLeadership && (player.getParty() == null || !player.getParty().isLeader(player))) {
            return 4;
        }
        if (player.getParty() == null || player.getParty().getMemberCount() < Config.EVENT_GVG_MIN_PARTY_SIZE) {
            return 3;
        }
        if (player.getLevel() < Config.EVENT_GVG_MIN_LEVEL || player.getLevel() > Config.EVENT_GVG_MAX_LEVEL) {
            return 5;
        }
        if (player.isMounted()) {
            return 6;
        }
        if (player.isInDuel()) {
            return 7;
        }
        if (player.getTeam() != TeamType.NONE) {
            return 8;
        }
        if (player.isOlyParticipant() || OlympiadPlayersManager.getInstance().isRegistred(player)) {
            return 9;
        }
        if (player.isTeleporting()) {
            return 10;
        }
        if (player.getParty().isInDimensionalRift()) {
            return 11;
        }
        if (player.isCursedWeaponEquipped()) {
            return 12;
        }
        if (!player.isInPeaceZone()) {
            return 13;
        }
        if (player.isInObserverMode()) {
            return 14;
        }
        return 0;
    }

    private static void shuffleGroups() {
        if (leaderList.size() % 2 != 0) {
            final int rndindex = Rnd.get(leaderList.size());
            final Player expelled = leaderList.remove(rndindex).get();
            if (expelled != null) {
                expelled.sendMessage("\u041f\u0440\u0438 \u0444\u043e\u0440\u043c\u0438\u0440\u043e\u0432\u0430\u043d\u0438\u0438 \u0441\u043f\u0438\u0441\u043a\u0430 \u0443\u0447\u0430\u0441\u0442\u043d\u0438\u043a\u043e\u0432 \u0442\u0443\u0440\u043d\u0438\u0440\u0430 \u0432\u0430\u0448\u0430 \u0433\u0440\u0443\u043f\u043f\u0430 \u0431\u044b\u043b\u0430 \u043e\u0442\u0441\u0435\u044f\u043d\u0430. \u041f\u0440\u0438\u043d\u043e\u0441\u0438\u043c \u0438\u0437\u0432\u0438\u043d\u0435\u043d\u0438\u044f, \u043f\u043e\u043f\u0440\u043e\u0431\u0443\u0439\u0442\u0435 \u0432 \u0441\u043b\u0435\u0434\u0443\u044e\u0449\u0438\u0439 \u0440\u0430\u0437.");
            }
        }
        for (int i = 0; i < leaderList.size(); ++i) {
            final int rndindex2 = Rnd.get(leaderList.size());
            leaderList.set(i, leaderList.set(rndindex2, leaderList.get(i)));
        }
    }

    private static void checkPlayers() {
        for (final Player player : HardReferences.unwrap(leaderList)) {
            if (checkPlayer(player, true) != 0) {
                leaderList.remove(player.getRef());
            } else {
                for (final Player partymember : player.getParty().getPartyMembers()) {
                    if (checkPlayer(partymember, false) != 0) {
                        player.sendMessage("\u0412\u0430\u0448\u0430 \u0433\u0440\u0443\u043f\u043f\u0430 \u0431\u044b\u043b\u0430 \u0434\u0438\u0441\u043a\u0432\u0430\u043b\u0438\u0444\u0438\u0446\u0438\u0440\u043e\u0432\u0430\u043d\u0430 \u0438 \u0441\u043d\u044f\u0442\u0430 \u0441 \u0443\u0447\u0430\u0441\u0442\u0438\u044f \u0432 \u0442\u0443\u0440\u043d\u0438\u0440\u0435 \u0442\u0430\u043a \u043a\u0430\u043a \u043e\u0434\u0438\u043d \u0438\u043b\u0438 \u0431\u043e\u043b\u0435\u0435 \u0447\u043b\u0435\u043d\u043e\u0432 \u0433\u0440\u0443\u043f\u043f\u044b \u043d\u0430\u0440\u0443\u0448\u0438\u043b \u0443\u0441\u043b\u043e\u0432\u0438\u044f \u0443\u0447\u0430\u0441\u0442\u0438\u044f");
                        leaderList.remove(player.getRef());
                        break;
                    }
                }
            }
        }
    }

    public static void updateWinner(final Player winner) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("INSERT INTO event_data(charId, score) VALUES (?,1) ON DUPLICATE KEY UPDATE score=score+1");
            statement.setInt(1, winner.getObjectId());
            statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    private static void start() {
        final int instancedZoneId = 504;
        final InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(instancedZoneId);
        if (iz == null) {
            LOGGER.warn("GvG: InstanceZone : " + instancedZoneId + " not found!");
            return;
        }
        for (int i = 0; i < leaderList.size(); i += 2) {
            final Player team1Leader = leaderList.get(i).get();
            final Player team2Leader = leaderList.get(i + 1).get();
            final GvGInstance r = new GvGInstance();
            r.setTeam1(team1Leader.getParty());
            r.setTeam2(team2Leader.getParty());
            r.init(iz);
            r.setReturnLoc(RETURN_LOC);
            team1Leader.getParty().getPartyMembers().forEach(member -> {
                Functions.unRide(member);
                Functions.unSummonPet(member, true);
                member.setTransformation(0);
                member.setInstanceReuse(instancedZoneId, System.currentTimeMillis());
                member.dispelBuffs();
                member.teleToLocation(Location.findPointToStay(TEAM1_LOC, 0, 150, r.getGeoIndex()), r);
            });
            team2Leader.getParty().getPartyMembers().forEach(member -> {
                Functions.unRide(member);
                Functions.unSummonPet(member, true);
                member.setTransformation(0);
                member.setInstanceReuse(instancedZoneId, System.currentTimeMillis());
                member.dispelBuffs();
                member.teleToLocation(Location.findPointToStay(TEAM2_LOC, 0, 150, r.getGeoIndex()), r);
            });
            r.start();
        }
        leaderList.clear();
        LOGGER.info("GvG: Event started successfuly.");
    }

    public void startEvent() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (SetActive("GvG", true)) {
            System.out.println("Event: GvG started.");
            Announcements.getInstance().announceByCustomMessage("scripts.events.gvg.AnnounceEventStarted", null);
            LOGGER.info("Loaded Event: GvG");
            initTimer();
        } else {
            player.sendMessage("Event 'Groupe vs Groupe' already started.");
        }
        _active = true;
        show("admin/events/events.htm", player);
    }

    public void stopEvent() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (SetActive("GvG", false)) {
            System.out.println("Event: GvG stopped.");
            Announcements.getInstance().announceByCustomMessage("scripts.events.gvg.AnnounceEventStoped", null);
        } else {
            player.sendMessage("Event 'Groupe vs Groupe' not started.");
        }
        _active = false;
        show("admin/events/events.htm", player);
    }

    @Override
    public void onInit() {
        if (isActive()) {
            _active = true;
            initTimer();
            LOGGER.info("Loaded Event: GvG [state: activated]");
        } else {
            LOGGER.info("Loaded Event: GvG [state: deactivated]");
        }
    }

    public void showStats() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (!isActive()) {
            player.sendMessage("Groupe vs Groupe event is not launched");
            return;
        }
        final StringBuilder string = new StringBuilder();
        final String refresh = "<button value=\"Refresh\" action=\"bypass -h scripts_events.GvG.GvG:showStats\" width=60 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
        final String start = "<button value=\"Start Now\" action=\"bypass -h scripts_events.GvG.GvG:startNow\" width=60 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
        int i = 0;
        if (!leaderList.isEmpty()) {
            for (final Player leader : HardReferences.unwrap(leaderList)) {
                if (!leader.isInParty()) {
                    continue;
                }
                string.append("*").append(leader.getName()).append("*").append(" | group members: ").append(leader.getParty().getMemberCount()).append("\n\n");
                ++i;
            }
            show("There are " + i + " group leaders who registered for the event:\n\n" + string + "\n\n" + refresh + "\n\n" + start, player, null);
        } else {
            show("There are no participants at the time\n\n" + refresh, player, null);
        }
    }

    public void startNow() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (!isActive() || !canBeStarted()) {
            player.sendMessage("Groupe vs Groupe event is not launched");
            return;
        }
        prepare();
    }

    public void addGroup() {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (!_isRegistrationActive) {
            player.sendMessage(new CustomMessage("scripts.event.gvg.notactived", player));
            return;
        }
        if (leaderList.contains(player.getRef())) {
            player.sendMessage(new CustomMessage("scripts.event.gvg.registred", player));
            return;
        }
        if (!player.isInParty()) {
            player.sendMessage(new CustomMessage("scripts.event.gvg.notinparty", player));
            return;
        }
        if (!player.getParty().isLeader(player)) {
            player.sendMessage(new CustomMessage("scripts.event.gvg.onlypartyleader", player));
            return;
        }
        if (player.getParty().isInCommandChannel()) {
            player.sendMessage(new CustomMessage("scripts.event.gvg.removecommandchannel", player));
            return;
        }
        if (leaderList.size() >= Config.EVENT_GVG_GROUPS_LIMIT) {
            player.sendMessage(new CustomMessage("scripts.event.gvg.limitpartycount", player));
            return;
        }
        final List<Player> party = player.getParty().getPartyMembers();
        final String[] abuseReason = {"\u043d\u0435 \u043d\u0430\u0445\u043e\u0434\u0438\u0442\u0441\u044f \u0432 \u0438\u0433\u0440\u0435", "\u043d\u0435 \u043d\u0430\u0445\u043e\u0434\u0438\u0442\u0441\u044f \u0432 \u0433\u0440\u0443\u043f\u043f\u0435", "\u0441\u043e\u0441\u0442\u043e\u0438\u0442 \u0432 \u043d\u0435\u043f\u043e\u043b\u043d\u043e\u0439 \u0433\u0440\u0443\u043f\u043f\u0435. \u041c\u0438\u043d\u0438\u043c\u0430\u043b\u044c\u043d\u043e\u0435 \u043a\u043e\u043b-\u0432\u043e \u0447\u043b\u0435\u043d\u043e\u0432 \u0433\u0440\u0443\u043f\u043f\u044b - 6.", "\u043d\u0435 \u044f\u0432\u043b\u044f\u0435\u0442\u0441\u044f \u043b\u0438\u0434\u0435\u0440\u043e\u043c \u0433\u0440\u0443\u043f\u043f\u044b, \u043f\u043e\u0434\u0430\u0432\u0430\u0432\u0448\u0435\u0439 \u0437\u0430\u044f\u0432\u043a\u0443", "\u043d\u0435 \u0441\u043e\u043e\u0442\u0432\u0435\u0442\u0441\u0442\u0432\u0443\u0435\u0442 \u0442\u0440\u0435\u0431\u043e\u0432\u0430\u043d\u0438\u044f\u043c \u0443\u0440\u043e\u0432\u043d\u0435\u0439 \u0434\u043b\u044f \u0442\u0443\u0440\u043d\u0438\u0440\u0430", "\u0438\u0441\u043f\u043e\u043b\u044c\u0437\u0443\u0435\u0442 \u0435\u0437\u0434\u043e\u0432\u043e\u0435 \u0436\u0438\u0432\u043e\u0442\u043d\u043e\u0435, \u0447\u0442\u043e \u043f\u0440\u043e\u0442\u0438\u0432\u043e\u0440\u0435\u0447\u0438\u0442 \u0442\u0440\u0435\u0431\u043e\u0432\u0430\u043d\u0438\u044f\u043c \u0442\u0443\u0440\u043d\u0438\u0440\u0430", "\u043d\u0430\u0445\u043e\u0434\u0438\u0442\u0441\u044f \u0432 \u0434\u0443\u044d\u043b\u0438, \u0447\u0442\u043e \u043f\u0440\u043e\u0442\u0438\u0432\u043e\u0440\u0435\u0447\u0438\u0442 \u0442\u0440\u0435\u0431\u043e\u0432\u0430\u043d\u0438\u044f\u043c \u0442\u0443\u0440\u043d\u0438\u0440\u0430", "\u043f\u0440\u0438\u043d\u0438\u043c\u0430\u0435\u0442 \u0443\u0447\u0430\u0441\u0442\u0438\u0435 \u0432 \u0434\u0440\u0443\u0433\u043e\u043c \u044d\u0432\u0435\u043d\u0442\u0435, \u0447\u0442\u043e \u043f\u0440\u043e\u0442\u0438\u0432\u043e\u0440\u0435\u0447\u0438\u0442 \u0442\u0440\u0435\u0431\u043e\u0432\u0430\u043d\u0438\u044f\u043c \u0442\u0443\u0440\u043d\u0438\u0440\u0430", "\u043d\u0430\u0445\u043e\u0434\u0438\u0442\u0441\u044f \u0432 \u0441\u043f\u0438\u0441\u043a\u0435 \u043e\u0436\u0438\u0434\u0430\u043d\u0438\u044f \u041e\u043b\u0438\u043c\u043f\u0438\u0430\u0434\u044b \u0438\u043b\u0438 \u043f\u0440\u0438\u043d\u0438\u043c\u0430\u0435\u0442 \u0443\u0447\u0430\u0441\u0442\u0438\u0435 \u0432 \u043d\u0435\u0439", "\u043d\u0430\u0445\u043e\u0434\u0438\u0442\u0441\u044f \u0432 \u0441\u043e\u0441\u0442\u043e\u044f\u043d\u0438\u0438 \u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0430\u0446\u0438\u0438, \u0447\u0442\u043e \u043f\u0440\u043e\u0442\u0438\u0432\u043e\u0440\u0435\u0447\u0438\u0442 \u0442\u0440\u0435\u0431\u043e\u0432\u0430\u043d\u0438\u044f\u043c \u0442\u0443\u0440\u043d\u0438\u0440\u0430", "\u043d\u0430\u0445\u043e\u0434\u0438\u0442\u0441\u044f \u0432 Dimensional Rift, \u0447\u0442\u043e \u043f\u0440\u043e\u0442\u0438\u0432\u043e\u0440\u0435\u0447\u0438\u0442 \u0442\u0440\u0435\u0431\u043e\u0432\u0430\u043d\u0438\u044f\u043c \u0442\u0443\u0440\u043d\u0438\u0440\u0430", "\u043e\u0431\u043b\u0430\u0434\u0430\u0435\u0442 \u041f\u0440\u043e\u043a\u043b\u044f\u0442\u044b\u043c \u041e\u0440\u0443\u0436\u0438\u0435\u043c, \u0447\u0442\u043e \u043f\u0440\u043e\u0442\u0438\u0432\u043e\u0440\u0435\u0447\u0438\u0442 \u0442\u0440\u0435\u0431\u043e\u0432\u0430\u043d\u0438\u044f\u043c \u0442\u0443\u0440\u043d\u0438\u0440\u0430", "\u043d\u0435 \u043d\u0430\u0445\u043e\u0434\u0438\u0442\u0441\u044f \u0432 \u043c\u0438\u0440\u043d\u043e\u0439 \u0437\u043e\u043d\u0435", "\u043d\u0430\u0445\u043e\u0434\u0438\u0442\u0441\u044f \u0432 \u0440\u0435\u0436\u0438\u043c\u0435 \u043e\u0431\u043e\u0437\u0440\u0435\u0432\u0430\u043d\u0438\u044f"};
        for (final Player eachmember : party) {
            final int abuseId = checkPlayer(eachmember, false);
            if (abuseId != 0) {
                player.sendMessage("Player " + eachmember.getName() + " " + abuseReason[abuseId - 1]);
                return;
            }
        }
        leaderList.add(player.getRef());
        player.getParty().broadcastMessageToPartyMembers("\u0412\u0430\u0448\u0430 \u0433\u0440\u0443\u043f\u043f\u0430 \u0432\u043d\u0435\u0441\u0435\u043d\u0430 \u0432 \u0441\u043f\u0438\u0441\u043e\u043a \u043e\u0436\u0438\u0434\u0430\u043d\u0438\u044f. \u041f\u043e\u0436\u0430\u043b\u0443\u0439\u0441\u0442\u0430, \u043d\u0435 \u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u0443\u0439\u0442\u0435\u0441\u044c \u0432 \u0434\u0440\u0443\u0433\u0438\u0445 \u0438\u0432\u0435\u043d\u0442\u0430\u0445 \u0438 \u043d\u0435 \u0443\u0447\u0430\u0441\u0442\u0432\u0443\u0439\u0442\u0435 \u0432 \u0434\u0443\u044d\u043b\u044f\u0445 \u0434\u043e \u043d\u0430\u0447\u0430\u043b\u0430 \u0442\u0443\u0440\u043d\u0438\u0440\u0430.");
    }

    public static class RegTask extends RunnableImpl {
        @Override
        public void runImpl() {
            prepare();
        }
    }

    public static class Countdown extends RunnableImpl {
        int _timer;

        public Countdown(final int timer) {
            _timer = timer;
        }

        @Override
        public void runImpl() {
            Announcements.getInstance().announceByCustomMessage("scripts.events.gvg.timeexpend", new String[]{Integer.toString(_timer)});
        }
    }

    public static class Launch extends RunnableImpl {
        @Override
        public void runImpl() {
            activateEvent();
        }
    }
}
