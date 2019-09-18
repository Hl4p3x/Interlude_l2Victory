package events.TvT2;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Announcements;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.ai.CtrlIntention;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.InstantZoneHolder;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.idfactory.IdFactory;
import ru.j2dev.gameserver.listener.actor.OnDeathListener;
import ru.j2dev.gameserver.listener.actor.player.OnPlayerExitListener;
import ru.j2dev.gameserver.listener.actor.player.OnTeleportListener;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.listener.zone.OnZoneEnterLeaveListener;
import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.manager.ServerVariables;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.model.Zone.ZoneType;
import ru.j2dev.gameserver.model.base.TeamType;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.model.entity.olympiad.OlympiadPlayersManager;
import ru.j2dev.gameserver.model.instances.MonsterInstance;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.items.attachment.FlagItemAttachment;
import ru.j2dev.gameserver.network.lineage2.GameClient;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.components.IStaticPacket;
import ru.j2dev.gameserver.network.lineage2.serverpackets.*;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExEventMatchMessage.MessageType;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.scripts.Scripts;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.templates.InstantZone;
import ru.j2dev.gameserver.templates.item.ItemTemplate;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.PositionUtils;
import ru.j2dev.gameserver.utils.TimeUtils;
import services.GlobalServices;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class PvPEvent extends Functions implements OnInitScriptListener {
    public static final String EVENT_NAME = "PvP";
    public static final String VAR_EVENT_ACTIVE = "PvP_is_active";
    public static final String VAR_START_TIME = "PvP_start_time";
    public static final String VAR_ANNOUNCE_TIME = "PvP_announce_time";
    public static final String VAR_ANNOUNCE_REDUCT = "PvP_announce_reduct";
    public static final String VAR_INSTANCES_IDS = "PvP_instances_ids";
    private static final Logger LOGGER = LoggerFactory.getLogger(PvPEvent.class);
    private static final Skill BUFF_PROTECTION_EFFECT = SkillTable.getInstance().getInfo(1323, 1);
    private static final long RANK_BROADCAST_TIME = 20000L;
    private static PvPEvent _instance;
    private DieListner _dieListner;
    private ZoneEnterLeaveListner _zoneListner;
    private ExitListner _exitListner;
    private boolean _event_active;
    private String _event_start_time;
    private int _event_announce_time;
    private int _event_announce_reductor;
    private int[] _event_instances_ids;
    private PvPEventState _state;
    private PvPEventRule _rule;
    private Pair<String, int[]> _lastProhibitedClassIds;
    private ScheduledFuture<?> _stateTask;
    private ScheduledFuture<?> _processTask;
    private Collection<Integer> _participants;
    private RegisrationState _regState;
    private Collection<Integer> _desireContainer;

    public PvPEvent() {
    }

    public static PvPEvent getInstance() {
        return _instance;
    }

    private static boolean isDesirePlayer(final Player player) {
        return player != null && player.getNetConnection() != null && player.isConnected() && !player.isDead() && !player.isBlocked() && !player.isInZone(ZoneType.epic) && !player.isInZone(ZoneType.SIEGE) && !player.isInZone(ZoneType.SIEGE) && player.getReflectionId() == 0 && !player.isFishing() && player.getTransformation() == 0 && !player.isCursedWeaponEquipped() && player.getLevel() >= getInstance().config_MinLevel() && player.getLevel() <= getInstance().config_MaxLevel() && !player.isOlyParticipant() && !OlympiadPlayersManager.getInstance().isRegistred(player) && (!getInstance().config_hwidRestrict() || !getInstance().isHWIDRegistred(player.getNetConnection().getHwid(), player)) && !ArrayUtils.contains(getInstance().config_ProhibitedClassIds(), player.getActiveClassId()) && player.getTeam() == TeamType.NONE && !player.isInDuel();
    }

    public void LoadVars() {
        _event_active = ServerVariables.getBool(VAR_EVENT_ACTIVE, false);
        _event_start_time = ServerVariables.getString(VAR_START_TIME, "");
        _event_announce_time = ServerVariables.getInt(VAR_ANNOUNCE_TIME, 5);
        _event_announce_reductor = ServerVariables.getInt(VAR_ANNOUNCE_REDUCT, 1);
        final String[] inst_ids = ServerVariables.getString(VAR_INSTANCES_IDS, "").split("\\s*;\\s*");
        final List<Integer> event_instances_ids = Stream.of(inst_ids).map(String::trim).filter(instIdStr -> !instIdStr.isEmpty()).map(Integer::parseInt).collect(Collectors.toList());
        _event_instances_ids = new int[event_instances_ids.size()];
        IntStream.range(0, event_instances_ids.size()).forEach(i -> _event_instances_ids[i] = event_instances_ids.get(i));
    }

    private PvPEventState getState() {
        return _state;
    }

    private synchronized void setState(final PvPEventState state) {
        LOGGER.info("PvPEventState changet to " + state.name());
        _state = state;
    }

    public PvPEventRule getRule() {
        return _rule;
    }

    public void setRule(final PvPEventRule rule) {
        _rule = rule;
    }

    public PvPEventRule getNextRule(final PvPEventRule rule) {
        if (rule != null) {
            switch (rule) {
                case TVT: {
                    if (config_isPvPEventRuleEnabled(PvPEventRule.CTF)) {
                        return PvPEventRule.CTF;
                    }
                    if (config_isPvPEventRuleEnabled(PvPEventRule.DM)) {
                        return PvPEventRule.DM;
                    }
                    if (config_isPvPEventRuleEnabled(PvPEventRule.TVT)) {
                        return PvPEventRule.TVT;
                    }
                    break;
                }
                case CTF: {
                    if (config_isPvPEventRuleEnabled(PvPEventRule.DM)) {
                        return PvPEventRule.DM;
                    }
                    if (config_isPvPEventRuleEnabled(PvPEventRule.TVT)) {
                        return PvPEventRule.TVT;
                    }
                    if (config_isPvPEventRuleEnabled(PvPEventRule.CTF)) {
                        return PvPEventRule.CTF;
                    }
                    break;
                }
                case DM: {
                    if (config_isPvPEventRuleEnabled(PvPEventRule.TVT)) {
                        return PvPEventRule.TVT;
                    }
                    if (config_isPvPEventRuleEnabled(PvPEventRule.CTF)) {
                        return PvPEventRule.CTF;
                    }
                    if (config_isPvPEventRuleEnabled(PvPEventRule.DM)) {
                        return PvPEventRule.DM;
                    }
                    break;
                }
            }
            return null;
        }
        if (config_isPvPEventRuleEnabled(PvPEventRule.TVT)) {
            return PvPEventRule.TVT;
        }
        if (config_isPvPEventRuleEnabled(PvPEventRule.CTF)) {
            return PvPEventRule.CTF;
        }
        if (config_isPvPEventRuleEnabled(PvPEventRule.DM)) {
            return PvPEventRule.DM;
        }
        return null;
    }

    private boolean config_isUseCapcha() {
        return ServerVariables.getBool("PvP_" + getRule().name() + "_use_capcha", true);
    }

    private boolean config_hwidRestrict() {
        return ServerVariables.getBool("PvP_" + getRule().name() + "_use_hwid_restrict", false);
    }

    private boolean config_hideIdentiti() {
        return ServerVariables.getBool("PvP_" + getRule().name() + "_hide_identiti", false);
    }

    private int config_MaxParticipants() {
        return ServerVariables.getInt("PvP_" + getRule().name() + "_max_parts", 100);
    }

    private int config_ItemPerKill() {
        return ServerVariables.getInt("PvP_" + getRule().name() + "_item_per_kill", 0);
    }

    private int config_ReviveDelay() {
        return ServerVariables.getInt("PvP_" + getRule().name() + "_revive_delay", 1);
    }

    private boolean config_isPvPEventRuleEnabled(final PvPEventRule rule) {
        return ServerVariables.getBool("PvP_" + rule.name() + "_enabled", false);
    }

    private boolean config_isBuffProtection() {
        return ServerVariables.getBool("PvP_" + getRule().name() + "_buff_protection", false);
    }

    private int config_ReqParticipants() {
        return ServerVariables.getInt("PvP_" + getRule().name() + "_req_parts", 50);
    }

    private int config_MinLevel() {
        return ServerVariables.getInt("PvP_" + getRule().name() + "_min_lvl", 1);
    }

    private int config_MaxLevel() {
        return ServerVariables.getInt("PvP_" + getRule().name() + "_max_lvl", 86);
    }

    private int config_RewardHeroHours() {
        return ServerVariables.getInt("PvP_" + getRule().name() + "_herorevhours", 0);
    }

    private int[] config_ProhibitedClassIds() {
        String pci = ServerVariables.getString("PvP_" + getRule().name() + "_prohibited_class_ids", "");
        if (pci.isEmpty()) {
            return ArrayUtils.EMPTY_INT_ARRAY;
        }
        final String key = getRule().name() + pci;
        if (_lastProhibitedClassIds != null && key.equals(_lastProhibitedClassIds.getLeft())) {
            return _lastProhibitedClassIds.getRight();
        }
        pci = pci.trim();
        final StringBuilder sb = new StringBuilder();
        int[] ids = ArrayUtils.EMPTY_INT_ARRAY;
        for (int pciIdx = 0; pciIdx < pci.length(); ++pciIdx) {
            final char c = pci.charAt(pciIdx);
            switch (c) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9': {
                    sb.append(c);
                    break;
                }
                case ',':
                case ';': {
                    ids = ArrayUtils.add(ids, Integer.parseInt(sb.toString()));
                    sb.setLength(0);
                    break;
                }
                default: {
                    LOGGER.error("Can't parse prohibited class ids \"" + pci + "\"");
                    return ids;
                }
            }
        }
        if (sb.length() > 0) {
            ids = ArrayUtils.add(ids, Integer.parseInt(sb.toString()));
        }
        Arrays.sort(ids);
        _lastProhibitedClassIds = Pair.of(key, ids);
        return ids;
    }

    private List<Pair<ItemTemplate, Long>> config_RewardTeamItemIdAndAmount() {
        final String teamRevListText = ServerVariables.getString("PvP_" + getRule().name() + "_rev_team", "");
        return Functions.parseItemIdAmountList(teamRevListText);
    }

    private List<Pair<ItemTemplate, Long>> config_RewardTopItemIdAndAmount() {
        final String teamRevListText = ServerVariables.getString("PvP_" + getRule().name() + "_rev_top", "");
        return Functions.parseItemIdAmountList(teamRevListText);
    }

    private boolean config_dispellEffects() {
        return ServerVariables.getBool("PvP_" + getRule().name() + "_dispell", true);
    }

    private boolean config_dispellEffectsAfter() {
        return ServerVariables.getBool("PvP_" + getRule().name() + "_dispell_after", true);
    }

    private int config_EventTime() {
        switch (getRule()) {
            case DM: {
                return ServerVariables.getInt("PvP_" + getRule().name() + "_time", 5);
            }
            case TVT:
            case CTF: {
                return ServerVariables.getInt("PvP_" + getRule().name() + "_time", 10);
            }
            default: {
                return 0;
            }
        }
    }

    private int getNewReflectionId() {
        return _event_instances_ids[Rnd.get(_event_instances_ids.length)];
    }

    private synchronized void scheduleStateChange(final PvPEventState to_state, final long delay) {
        _stateTask = ThreadPoolManager.getInstance().schedule(new PvPStateTask(to_state), delay);
    }

    private synchronized void cancelStateChange() {
        if (_stateTask != null) {
            _stateTask.cancel(false);
            _stateTask = null;
        }
    }

    private void goStandby() {
        setState(PvPEventState.STANDBY);
        final long mills = getMillsToNextActivation(_event_start_time);
        if (mills > 0L) {
            final PvPEventRule nextRule = getNextRule(getRule());
            if (nextRule != null) {
                setRule(nextRule);
                scheduleStateChange(PvPEventState.REGISTRATION, mills);
                LOGGER.info("PvPEvent: Next scheduled at " + TimeUtils.toSimpleFormat(System.currentTimeMillis() + mills));
            } else {
                LOGGER.info("PvPEvent: No active next event");
            }
        } else {
            LOGGER.warn("PvPEvent: Wrong event time: " + _event_start_time);
        }
    }

    public void goRegistration() {
        setState(PvPEventState.REGISTRATION);
        getInstance().scheduleProcessTask(new RegisrationTask(RegisrationState.ANNOUNCE, _event_announce_time), 1000L);
    }

    private void goPrepareTo() {
        setState(PvPEventState.PREPARE_TO);
        getRule().getParticipantController().initReflection();
        getRule().getParticipantController().prepareParticipantsTo();
        scheduleStateChange(PvPEventState.PORTING_TO, 2000L);
    }

    private void goPortingTo() {
        setState(PvPEventState.PORTING_TO);
        getRule().getParticipantController().portParticipantsTo();
        getInstance().scheduleProcessTask(new CompetitionRunTask(30), 1000L);
    }

    private void goCompetition() {
        setState(PvPEventState.COMPETITION);
        getRule().getParticipantController().initParticipant();
        scheduleStateChange(PvPEventState.WINNER, config_EventTime() * 60 * 1000);
    }

    private void goWinner() {
        setState(PvPEventState.WINNER);
        getRule().getParticipantController().MakeWinner();
        scheduleStateChange(PvPEventState.PREPARE_FROM, 1000L);
    }

    private void goPrepareFrom() {
        setState(PvPEventState.PREPARE_FROM);
        getRule().getParticipantController().prepareParticipantsFrom();
        scheduleStateChange(PvPEventState.PORTING_FROM, 10000L);
    }

    private void goPortingFrom() {
        setState(PvPEventState.PORTING_FROM);
        getRule().getParticipantController().portParticipantsBack();
        getRule().getParticipantController().doneParicipant();
        getRule().getParticipantController().doneReflection();
        _participants.clear();
        _participants = null;
        scheduleStateChange(PvPEventState.STANDBY, 5000L);
    }

    private synchronized void scheduleProcessTask(final Runnable r, final long delay) {
        _processTask = ThreadPoolManager.getInstance().schedule(r, delay);
    }

    private synchronized void cancelProcessTask() {
        if (_processTask != null) {
            _processTask.cancel(false);
            _processTask = null;
        }
    }

    private Collection<Player> getPlayers() {
        final List<Player> result = new ArrayList<>(_participants.size());
        GameObjectsStorage.getPlayers().stream().filter(Objects::nonNull).forEach(player -> _participants.stream().mapToInt(oid -> oid).filter(oid -> oid == player.getObjectId()).mapToObj(oid -> player).forEach(result::add));
        return result;
    }

    private void broadcastParticipationRequest() {
        final List<Player> players = GameObjectsStorage.getPlayers().stream().filter(PvPEvent::isDesirePlayer).collect(Collectors.toList());
        players.forEach(player -> player.scriptRequest(new CustomMessage("events.PvPEvent.AskToS1Participation", player, new Object[]{getRule().name()}).toString(), "events.TvT2.PvPEvent:addDesire", new Object[0]));
    }

    private void broadcastCapchaRequest() {
        if (_regState != RegisrationState.CAPCHA || _desireContainer == null) {
            return;
        }
        final List<Player> players = _desireContainer.stream().map(GameObjectsStorage::getPlayer).filter(PvPEvent::isDesirePlayer).collect(Collectors.toList());
        _desireContainer.clear();
        _desireContainer = null;
        getInstance()._desireContainer = new ConcurrentSkipListSet<>();
        players.forEach(player2 -> Scripts.getInstance().callScripts(player2, "Util", "RequestCapcha", new Object[]{"events.TvT2.PvPEvent:addDesire", player2.getObjectId(), 30}));
    }

    public boolean isHWIDRegistred(final String hwid, final Player player) {
        if (hwid == null || hwid.isEmpty() || _desireContainer == null) {
            return false;
        }
        for (final int oid : _desireContainer) {
            final Player p = GameObjectsStorage.getPlayer(oid);
            final GameClient gameClient;
            if (p != null && p != player && (gameClient = p.getNetConnection()) != null) {
                if (!gameClient.isConnected()) {
                    continue;
                }
                if (gameClient.getHwid() != null && hwid.equalsIgnoreCase(gameClient.getHwid())) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addDesire() {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (getInstance()._regState != RegisrationState.REQUEST && getInstance()._regState != RegisrationState.CAPCHA) {
            player.sendMessage(new CustomMessage("events.PvPEvent.ParticipationDesireInappropriateState", player));
            return;
        }
        if (!isDesirePlayer(player) || getInstance()._desireContainer == null) {
            player.sendMessage(new CustomMessage("events.PvPEvent.ParticipationDesireInsufficientConditions", player));
            return;
        }
        if (getInstance()._desireContainer.contains(player.getObjectId())) {
            player.sendMessage(new CustomMessage("events.PvPEvent.ParticipationDesireAlreadyAccepted", player));
            return;
        }
        player.sendMessage(new CustomMessage("events.PvPEvent.ParticipationDesireAccepted", player));
        getInstance()._desireContainer.add(player.getObjectId());
    }

    public void addDesireDuringAnnounce() {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (getInstance()._regState != RegisrationState.ANNOUNCE) {
            player.sendMessage(new CustomMessage("events.PvPEvent.ParticipationDesireInappropriateState", player));
            return;
        }
        if (!isDesirePlayer(player) || getInstance()._desireContainer == null) {
            player.sendMessage(new CustomMessage("events.PvPEvent.ParticipationDesireInsufficientConditions", player));
            return;
        }
        if (getInstance()._desireContainer.contains(player.getObjectId())) {
            player.sendMessage(new CustomMessage("events.PvPEvent.ParticipationDesireAlreadyAccepted", player));
            return;
        }
        player.sendMessage(new CustomMessage("events.PvPEvent.ParticipationDesireAccepted", player));
        getInstance()._desireContainer.add(player.getObjectId());
    }

    private void morphDesires() {
        if (_regState != RegisrationState.MORPH || _desireContainer == null) {
            return;
        }
        final List<Player> players = _desireContainer.stream().map(GameObjectsStorage::getPlayer).filter(PvPEvent::isDesirePlayer).collect(Collectors.toCollection(LinkedList::new));
        _desireContainer.clear();
        _desireContainer = null;
        final List<Player> participants = new ArrayList<>();
        final int max_part = config_MaxParticipants();
        while (participants.size() < max_part && !players.isEmpty()) {
            participants.add(players.remove(Rnd.get(players.size())));
        }
        if (participants.size() < config_ReqParticipants()) {
            Announcements.getInstance().announceByCustomMessage("events.PvPEvent.EventS1LackParticipants", new String[]{getRule().name()});
            goStandby();
            return;
        }
        _participants = new ConcurrentSkipListSet<>();
        players.forEach(player2 -> player2.sendMessage(new CustomMessage("events.PvPEvent.ParticipantAskLater", player2)));
        participants.forEach(participant -> {
            participant.sendMessage(new CustomMessage("events.PvPEvent.ParticipantAccepted", participant));
            _participants.add(participant.getObjectId());
        });
        players.clear();
        scheduleStateChange(PvPEventState.PREPARE_TO, 10000L);
    }

    public void Activate() {
        getInstance().scheduleStateChange(PvPEventState.STANDBY, 1000L);
        LOGGER.info("PvPEvent: [state: active]");
    }

    public void Deativate() {
        ServerVariables.set(VAR_EVENT_ACTIVE, false);
        getInstance().LoadVars();
        getInstance().cancelStateChange();
        LOGGER.info("PvPEvent: [state: inactive]");
    }

    @Override
    public void onInit() {
        (_instance = this).LoadVars();
        if (_event_active) {
            Activate();
        } else {
            LOGGER.info("PvPEvent: [state: inactive]");
        }
        _dieListner = new DieListner();
        _zoneListner = new ZoneEnterLeaveListner();
        _exitListner = new ExitListner();
    }

    private long getMillsToNextActivation(final String schedule) {
        final Matcher m = Pattern.compile("(\\d{2})\\:(\\d{2});*").matcher(schedule);
        final long now = System.currentTimeMillis();
        long ret_mills = Long.MAX_VALUE;
        while (m.find()) {
            final String hour_str = m.group(1);
            final String minute_str = m.group(2);
            final int hour = Integer.parseInt(hour_str);
            final int minute = Integer.parseInt(minute_str);
            final Calendar next_c = Calendar.getInstance();
            next_c.set(Calendar.HOUR_OF_DAY, hour);
            next_c.set(Calendar.MINUTE, minute);
            next_c.set(Calendar.SECOND, 0);
            next_c.set(Calendar.MILLISECOND, 0);
            if (next_c.getTimeInMillis() < now) {
                next_c.add(Calendar.DATE, 1);
            }
            final long mills_left = next_c.getTimeInMillis() - now;
            if (mills_left > 0L && mills_left < ret_mills) {
                ret_mills = mills_left;
            }
        }
        return (ret_mills < Long.MAX_VALUE) ? ret_mills : -1L;
    }

    private void broadcast(final L2GameServerPacket... gsp) {
        final Collection<Player> players = getPlayers();
        players.forEach(player -> player.sendPacket((IStaticPacket[]) gsp));
    }

    public boolean isEventPartisipant() {
        final Player p = getSelf();
        if (getInstance()._participants == null || p == null) {
            return false;
        }
        final int poid = p.getObjectId();
        return getInstance()._participants.stream().anyMatch(oid -> oid == poid);
    }

    protected enum PvPEventState {
        STANDBY,
        REGISTRATION,
        PORTING_TO,
        PREPARE_TO,
        COMPETITION,
        WINNER,
        PREPARE_FROM,
        PORTING_FROM
    }

    public enum PvPEventRule {
        TVT(new TvTParticipantController()),
        CTF(new CTFParticipantController()),
        DM(new DMParticipantController());

        public static PvPEventRule[] VALUES;

        static {
            PvPEventRule.VALUES = values();
        }

        private final IParticipantController _part_conteiner;

        PvPEventRule(final IParticipantController conteiner) {
            _part_conteiner = conteiner;
        }

        public IParticipantController getParticipantController() {
            return _part_conteiner;
        }
    }

    private enum RegisrationState {
        ANNOUNCE,
        REQUEST,
        MORPH,
        CAPCHA
    }

    private interface IParticipantController {
        void prepareParticipantsTo();

        void prepareParticipantsFrom();

        void initParticipant();

        void doneParicipant();

        void portParticipantsTo();

        void portParticipantsBack();

        void initReflection();

        void doneReflection();

        Reflection getReflection();

        void OnPlayerDied(final Player p0, final Player p1);

        void OnEnter(final Player p0, final Zone p1);

        void OnLeave(final Player p0, final Zone p1);

        void OnExit(final Player p0);

        void OnTeleport(final Player p0, final int p1, final int p2, final int p3, final Reflection p4);

        void MakeWinner();
    }

    private static class TvTParticipantController implements IParticipantController {
        private static final RankComparator _rankComparator = new RankComparator();
        private static final String TITLE_VAR = "pvp_tvt_title";
        private final String RET_LOC_VAR;
        private Map<Integer, ImmutablePair<AtomicInteger, AtomicInteger>> _red_team;
        private Map<Integer, ImmutablePair<AtomicInteger, AtomicInteger>> _blue_team;
        private int _instance_id;
        private String ZONE_DEFAULT;
        private String ZONE_BLUE;
        private String ZONE_RED;
        private Reflection _reflection;
        private Zone _default_zone;
        private AtomicInteger _red_points;
        private AtomicInteger _blue_points;
        private ScheduledFuture<?> _rankBroadcastTask;

        private TvTParticipantController() {
            _instance_id = 0;
            ZONE_DEFAULT = "[pvp_%d_tvt_default]";
            ZONE_BLUE = "[pvp_%d_tvt_spawn_blue]";
            ZONE_RED = "[pvp_%d_tvt_spawn_red]";
            RET_LOC_VAR = "backCoords";
            _reflection = null;
            _default_zone = null;
        }

        public int getKills(final TeamType team) {
            int result = 0;
            if (team == TeamType.RED) {
                result = _red_team.values().stream().mapToInt(entry -> entry.getLeft().get()).sum();
            }
            if (team == TeamType.BLUE) {
                result += _blue_team.values().stream().mapToInt(entry -> entry.getLeft().get()).sum();
            }
            return result;
        }

        @Override
        public void prepareParticipantsTo() {
            _red_team = new ConcurrentHashMap<>();
            _blue_team = new ConcurrentHashMap<>();
            _red_points = new AtomicInteger(0);
            _blue_points = new AtomicInteger(0);
            TeamType team_type = TeamType.BLUE;
            final boolean dispell = getInstance().config_dispellEffects();
            getInstance().getPlayers().stream().filter(player -> !isDesirePlayer(player)).forEach(player -> {
                getInstance()._participants.remove(player.getObjectId());
                OnExit(player);
            });
            for (final Player player : getInstance().getPlayers()) {
                player.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
                if (player.isAttackingNow()) {
                    player.abortAttack(true, false);
                }
                if (player.isCastingNow()) {
                    player.abortCast(true, false);
                }
                player.sendActionFailed();
                player.stopMove();
                player.sitDown(null);
                player.block();
                if (dispell) {
                    player.getEffectList().stopAllEffects();
                }
                player.setCurrentHpMp((double) player.getMaxHp(), (double) player.getMaxMp());
                player.setCurrentCp((double) player.getMaxCp());
                player.sendChanges();
                player.setVar("pvp_tvt_title", (player.getTitle() != null) ? player.getTitle() : "", -1L);
                if (team_type == TeamType.BLUE) {
                    player.setTeam(TeamType.BLUE);
                    _blue_team.put(player.getObjectId(), new ImmutablePair<>(new AtomicInteger(0), new AtomicInteger(0)));
                    team_type = TeamType.RED;
                } else {
                    player.setTeam(TeamType.RED);
                    _red_team.put(player.getObjectId(), new ImmutablePair<>(new AtomicInteger(0), new AtomicInteger(0)));
                    team_type = TeamType.BLUE;
                }
                updateTitle(player, 0);
            }
        }

        @Override
        public void prepareParticipantsFrom() {
            final boolean dispell_after = getInstance().config_dispellEffectsAfter();
            getInstance().getPlayers().forEach(player -> {
                player.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
                if (player.isAttackingNow()) {
                    player.abortAttack(true, false);
                }
                if (player.isCastingNow()) {
                    player.abortCast(true, false);
                }
                player.sendActionFailed();
                player.stopMove();
                player.sitDown(null);
                player.block();
                if (dispell_after) {
                    player.getEffectList().stopAllEffects();
                }
                player.setCurrentHpMp((double) player.getMaxHp(), (double) player.getMaxMp());
                player.setCurrentCp((double) player.getMaxCp());
                player.sendChanges();
                player.setTeam(TeamType.NONE);
                final String title = player.getVar("pvp_tvt_title");
                if (title != null) {
                    player.setTitle(title);
                    player.unsetVar("pvp_tvt_title");
                }
                if (getInstance().config_hideIdentiti() && player.getTransformation() == 0) {
                    player.setTransformationName(null);
                    player.setTransformationTitle(null);
                }
                player.sendUserInfo(true);
            });
            _red_team.clear();
            _blue_team.clear();
            _red_team = null;
            _blue_team = null;
            _red_points = null;
            _blue_points = null;
        }

        @Override
        public void initParticipant() {
            final boolean isBuffProtection = PvPEvent.getInstance().config_isBuffProtection();
            getInstance().getPlayers().forEach(player -> {
                player.addListener(getInstance()._dieListner);
                player.addListener(getInstance()._exitListner);
                player.setResurectProhibited(true);
                player.unblock();
                player.standUp();
                if (isBuffProtection) {
                    BUFF_PROTECTION_EFFECT.getEffects(player, player, false, false, false);
                }
            });
        }

        @Override
        public void doneParicipant() {
            if (_rankBroadcastTask != null) {
                _rankBroadcastTask.cancel(true);
                _rankBroadcastTask = null;
            }
            getInstance().getPlayers().forEach(player -> {
                player.removeListener(getInstance()._dieListner);
                player.removeListener(getInstance()._exitListner);
                player.setResurectProhibited(false);
                player.unblock();
                if (player.isDead()) {
                    player.doRevive(100.0);
                    player.setCurrentHpMp((double) player.getMaxHp(), (double) player.getMaxMp());
                    player.setCurrentCp((double) player.getMaxCp());
                }
                player.standUp();
            });
        }

        private void updateTitle(final Player player, final int kills) {
            player.setTransformationTitle(String.format("Kills: %d", kills));
            player.setTitle(player.getTransformationTitle());
            player.broadcastPacket(new NickNameChanged(player));
        }

        @Override
        public void OnPlayerDied(final Player target, final Player killer) {
            if (killer != null && killer.getTeam() != target.getTeam()) {
                if (killer.getTeam() == TeamType.RED && _red_team.containsKey(killer.getObjectId())) {
                    final ImmutablePair<AtomicInteger, AtomicInteger> entry = _red_team.get(killer.getObjectId());
                    final AtomicInteger cnt = entry.getLeft();
                    updateTitle(killer, cnt.incrementAndGet());
                    _red_points.incrementAndGet();
                } else if (killer.getTeam() == TeamType.BLUE && _blue_team.containsKey(killer.getObjectId())) {
                    final ImmutablePair<AtomicInteger, AtomicInteger> entry = _blue_team.get(killer.getObjectId());
                    final AtomicInteger cnt = entry.getLeft();
                    updateTitle(killer, cnt.incrementAndGet());
                    _blue_points.incrementAndGet();
                } else if (killer.getTeam() != TeamType.NONE) {
                    LOGGER.warn("PvPEvent.TVT: '" + killer.getName() + "' got color but not at list.");
                }
                killer.sendUserInfo(true);
            }
            if (target.getTeam() == TeamType.RED && _red_team.containsKey(target.getObjectId())) {
                final ImmutablePair<AtomicInteger, AtomicInteger> entry = _red_team.get(target.getObjectId());
                entry.getRight().incrementAndGet();
            } else if (target.getTeam() == TeamType.BLUE && _blue_team.containsKey(target.getObjectId())) {
                final ImmutablePair<AtomicInteger, AtomicInteger> entry = _blue_team.get(target.getObjectId());
                entry.getRight().incrementAndGet();
            }
            ThreadPoolManager.getInstance().schedule(getInstance().new TeleportAndReviveTask(target, getRandomTeamLoc(target.getTeam()), getReflection()), (long) (getInstance().config_ReviveDelay() * 1000));
        }

        @Override
        public void portParticipantsTo() {
            int redCnt = 0;
            int blueCnt = 0;
            for (final Player player : getInstance().getPlayers()) {
                final TeamType playerTeam = player.getTeam();
                if (playerTeam != TeamType.BLUE && playerTeam != TeamType.RED) {
                    getInstance()._participants.remove(player.getObjectId());
                    OnExit(player);
                } else {
                    player.setVar(RET_LOC_VAR, player.getLoc().toXYZString(), -1L);
                    if (player.getParty() != null) {
                        player.getParty().removePartyMember(player, false);
                    }
                    if (getInstance().config_hideIdentiti()) {
                        switch (playerTeam) {
                            case RED: {
                                player.setTransformationName(String.format("Red %d", ++redCnt));
                                break;
                            }
                            case BLUE: {
                                player.setTransformationName(String.format("Blue %d", ++blueCnt));
                                break;
                            }
                        }
                    }
                    player.teleToLocation(getRandomTeamLoc(playerTeam), getReflection());
                }
            }
        }

        @Override
        public void portParticipantsBack() {
            getInstance().getPlayers().forEach(player -> {
                if (player.getTransformation() == 0) {
                    player.setTransformationName(null);
                }
                final String sloc = player.getVar(RET_LOC_VAR);
                if (sloc != null) {
                    player.unsetVar(RET_LOC_VAR);
                    player.teleToLocation(Location.parseLoc(sloc), ReflectionManager.DEFAULT);
                } else {
                    player.teleToClosestTown();
                }
            });
        }

        @Override
        public void initReflection() {
            _instance_id = getInstance().getNewReflectionId();
            final InstantZone instantZone = InstantZoneHolder.getInstance().getInstantZone(_instance_id);
            ZONE_DEFAULT = String.format("[pvp_%d_tvt_default]", _instance_id);
            ZONE_BLUE = String.format("[pvp_%d_tvt_spawn_blue]", _instance_id);
            ZONE_RED = String.format("[pvp_%d_tvt_spawn_red]", _instance_id);
            (_reflection = new Reflection()).init(instantZone);
            (_default_zone = _reflection.getZone(ZONE_DEFAULT)).addListener(getInstance()._zoneListner);
        }

        @Override
        public void doneReflection() {
            _default_zone.removeListener(getInstance()._zoneListner);
            _reflection.collapse();
            _reflection = null;
        }

        @Override
        public Reflection getReflection() {
            return _reflection;
        }

        private Location getRandomTeamLoc(final TeamType tt) {
            if (tt == TeamType.BLUE) {
                return _reflection.getZone(ZONE_BLUE).getTerritory().getRandomLoc(_reflection.getGeoIndex());
            }
            if (tt == TeamType.RED) {
                return _reflection.getZone(ZONE_RED).getTerritory().getRandomLoc(_reflection.getGeoIndex());
            }
            return null;
        }

        @Override
        public void OnEnter(final Player player, final Zone zone) {
            if (player != null && player.getTeam() != TeamType.BLUE && player.getTeam() != TeamType.RED && zone == _default_zone) {
                player.teleToClosestTown();
            }
        }

        @Override
        public void OnLeave(final Player player, final Zone zone) {
            if (player != null && !_default_zone.checkIfInZone(player.getX(), player.getY(), player.getZ(), getReflection())) {
                if (player.getTeam() != TeamType.BLUE && player.getTeam() != TeamType.RED && zone == _default_zone) {
                    player.teleToClosestTown();
                    return;
                }
                final double radian = 6.283185307179586 - PositionUtils.convertHeadingToRadian(player.getHeading());
                final int x = (int) Math.floor(player.getX() - 50.0 * Math.cos(radian));
                final int y = (int) Math.floor(player.getY() + 50.0 * Math.sin(radian));
                final ThreadPoolManager instance = ThreadPoolManager.getInstance();
                final PvPEvent instance2 = getInstance();
                instance2.getClass();
                instance.schedule(instance2.new TeleportTask(player, new Location(x, y, player.getZ()).correctGeoZ(), getReflection()), 3000L);
            }
        }

        @Override
        public void OnExit(final Player player) {
            if (_blue_team.containsKey(player.getObjectId())) {
                _blue_team.remove(player.getObjectId());
            } else _red_team.remove(player.getObjectId());
            final String title = player.getVar("pvp_tvt_title");
            if (title != null) {
                player.setTitle(player.getVar("pvp_tvt_title"));
                player.unsetVar("pvp_tvt_title");
            }
        }

        @Override
        public void OnTeleport(final Player player, final int x, final int y, final int z, final Reflection r) {
            if (player != null && !_default_zone.checkIfInZone(x, y, z, getReflection())) {
                final Location loc = getRandomTeamLoc(player.getTeam());
                if (loc != null) {
                    ThreadPoolManager.getInstance().schedule(getInstance().new TeleportTask(player, loc, getReflection()), 3000L);
                }
            }
        }

        private void rewardPerKill(final Map<Integer, ImmutablePair<AtomicInteger, AtomicInteger>> team) {
            final int itemId = getInstance().config_ItemPerKill();
            if (itemId <= 0) {
                return;
            }
            team.forEach((key, p) -> {
                final int oid = key;
                final int kills = p.getLeft().get();
                final Player player = GameObjectsStorage.getPlayer(oid);
                if (kills > 0 && player != null) {
                    Functions.addItem((Playable) player, itemId, (long) kills);
                }
            });
        }

        private void reward(final Map<Integer, ImmutablePair<AtomicInteger, AtomicInteger>> team, final List<Pair<ItemTemplate, Long>> teamReward, final List<Pair<ItemTemplate, Long>> topReward) {
            int top_oid = -1;
            int top_cnt = Integer.MIN_VALUE;
            for (final Entry<Integer, ImmutablePair<AtomicInteger, AtomicInteger>> e : team.entrySet()) {
                final int oid = e.getKey();
                final ImmutablePair<AtomicInteger, AtomicInteger> p = e.getValue();
                final int kills = p.getLeft().get();
                final Player player = GameObjectsStorage.getPlayer(oid);
                if (player != null) {
                    teamReward.forEach(teamRewardItemInfo -> Functions.addItem((Playable) player, teamRewardItemInfo.getLeft().getItemId(), teamRewardItemInfo.getRight()));
                    if (top_cnt >= kills) {
                        continue;
                    }
                    top_cnt = kills;
                    top_oid = oid;
                }
            }
            if (top_oid > 0 && top_cnt > 0) {
                final Player player2 = GameObjectsStorage.getPlayer(top_oid);
                if (player2 != null) {
                    topReward.forEach(topRewardItemInfo -> Functions.addItem((Playable) player2, topRewardItemInfo.getLeft().getItemId(), topRewardItemInfo.getRight()));
                    Announcements.getInstance().announceByCustomMessage("events.PvPEvent.TheTvTGameTopPlayerIsS1", new String[]{player2.getName()});
                    if (getInstance().config_RewardHeroHours() > 0) {
                        GlobalServices.makeCustomHero(player2, getInstance().config_RewardHeroHours() * 60 * 60L);
                    }
                }
            }
        }

        @Override
        public void MakeWinner() {
            final int blue_pnt = _blue_points.get();
            final int red_pnt = _red_points.get();
            if (blue_pnt > red_pnt) {
                Announcements.getInstance().announceByCustomMessage("events.PvPEvent.TeamBlueWonTheTvTGameCountIsS1S2", new String[]{String.valueOf(blue_pnt), String.valueOf(red_pnt)});
                reward(_blue_team, getInstance().config_RewardTeamItemIdAndAmount(), getInstance().config_RewardTopItemIdAndAmount());
            } else if (blue_pnt < red_pnt) {
                Announcements.getInstance().announceByCustomMessage("events.PvPEvent.TeamRedWonTheTvTGameCountIsS1S2", new String[]{String.valueOf(red_pnt), String.valueOf(blue_pnt)});
                reward(_red_team, getInstance().config_RewardTeamItemIdAndAmount(), getInstance().config_RewardTopItemIdAndAmount());
            } else if (blue_pnt == red_pnt) {
                Announcements.getInstance().announceByCustomMessage("events.PvPEvent.TheTvTGameEndedInATie", ArrayUtils.EMPTY_STRING_ARRAY);
            }
            rewardPerKill(_red_team);
            rewardPerKill(_blue_team);
            getInstance().broadcast(new ExEventMatchMessage(MessageType.FINISH, null));
        }

        private static class RankComparator implements Comparator<Entry<Integer, ImmutablePair<AtomicInteger, AtomicInteger>>> {
            @Override
            public int compare(final Entry<Integer, ImmutablePair<AtomicInteger, AtomicInteger>> o1, final Entry<Integer, ImmutablePair<AtomicInteger, AtomicInteger>> o2) {
                try {
                    if (o1 == null && o2 == null) {
                        return 1;
                    }
                    if (o1 == null) {
                        return 1;
                    }
                    if (o2 == null) {
                        return -1;
                    }
                    final int i1 = o1.getKey();
                    final int i2 = o2.getKey();
                    final int k1 = o1.getValue().getLeft().get();
                    final int k2 = o2.getValue().getLeft().get();
                    return (k1 != k2) ? (k2 - k1) : (i2 - i1);
                } catch (Exception ex) {
                    return 0;
                }
            }
        }

        private class RankBroadcastTask implements Runnable {
            private final TvTParticipantController _controller;

            public RankBroadcastTask(final TvTParticipantController controller) {
                _controller = controller;
            }

            @Override
            public void run() {
                if (getInstance().getState() != PvPEventState.COMPETITION) {
                    return;
                }
                _controller._rankBroadcastTask = ThreadPoolManager.getInstance().schedule(this, RANK_BROADCAST_TIME);
            }
        }
    }

    private static class CTFParticipantController implements IParticipantController {
        private static final String TITLE_VAR = "pvp_ctf_title";
        private static final int BLUE_FLAG_NPC = 32027;
        private static final int RED_FLAG_NPC = 32027;
        private static final int BLUE_FLAG_ITEM = 6718;
        private static final int RED_FLAG_ITEM = 6718;
        private final String RET_LOC_VAR;
        private Map<Integer, AtomicInteger> _red_team;
        private Map<Integer, AtomicInteger> _blue_team;
        private AtomicInteger _red_points;
        private AtomicInteger _blue_points;
        private String ZONE_DEFAULT;
        private String ZONE_BLUE;
        private String ZONE_RED;
        private Reflection _reflection;
        private Zone _default_zone;
        private Zone _blue_zone;
        private Zone _red_zone;
        private int _instance_id;
        private WeakReference<CTFFlagInstance> _redFlag;
        private WeakReference<CTFFlagInstance> _blueFlag;
        private ScheduledFuture<?> _rankBroadcastTask;

        private CTFParticipantController() {
            ZONE_DEFAULT = "[pvp_%d_ctf_default]";
            ZONE_BLUE = "[pvp_%d_ctf_spawn_blue]";
            ZONE_RED = "[pvp_%d_ctf_spawn_red]";
            RET_LOC_VAR = "backCoords";
            _reflection = null;
            _default_zone = null;
            _blue_zone = null;
            _red_zone = null;
            _instance_id = 0;
        }

        @Override
        public void prepareParticipantsTo() {
            _red_team = new ConcurrentHashMap<>();
            _blue_team = new ConcurrentHashMap<>();
            _red_points = new AtomicInteger(0);
            _blue_points = new AtomicInteger(0);
            TeamType team_type = TeamType.BLUE;
            getInstance().getPlayers().stream().filter(player -> !isDesirePlayer(player)).forEach(player -> {
                getInstance()._participants.remove(player.getObjectId());
                OnExit(player);
            });
            final boolean dispell = getInstance().config_dispellEffects();
            for (final Player player2 : getInstance().getPlayers()) {
                player2.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
                if (player2.isAttackingNow()) {
                    player2.abortAttack(true, false);
                }
                if (player2.isCastingNow()) {
                    player2.abortCast(true, false);
                }
                player2.sendActionFailed();
                player2.stopMove();
                player2.sitDown(null);
                player2.block();
                if (dispell) {
                    player2.getEffectList().stopAllEffects();
                }
                player2.setCurrentHpMp((double) player2.getMaxHp(), (double) player2.getMaxMp());
                player2.setCurrentCp((double) player2.getMaxCp());
                player2.sendChanges();
                player2.setVar("pvp_ctf_title", (player2.getTitle() != null) ? player2.getTitle() : "", -1L);
                if (team_type == TeamType.BLUE) {
                    player2.setTeam(TeamType.BLUE);
                    _blue_team.put(player2.getObjectId(), new AtomicInteger(0));
                    team_type = TeamType.RED;
                } else {
                    player2.setTeam(TeamType.RED);
                    _red_team.put(player2.getObjectId(), new AtomicInteger(0));
                    team_type = TeamType.BLUE;
                }
            }
        }

        @Override
        public void prepareParticipantsFrom() {
            Objects.requireNonNull(_redFlag.get()).removeFlag(null);
            Objects.requireNonNull(_blueFlag.get()).removeFlag(null);
            final boolean dispell_after = getInstance().config_dispellEffectsAfter();
            getInstance().getPlayers().forEach(player -> {
                player.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
                if (player.isAttackingNow()) {
                    player.abortAttack(true, false);
                }
                if (player.isCastingNow()) {
                    player.abortCast(true, false);
                }
                player.sendActionFailed();
                player.stopMove();
                player.sitDown(null);
                player.block();
                if (dispell_after) {
                    player.getEffectList().stopAllEffects();
                }
                player.setCurrentHpMp((double) player.getMaxHp(), (double) player.getMaxMp());
                player.setCurrentCp((double) player.getMaxCp());
                player.sendChanges();
                if (getInstance().config_hideIdentiti() && player.getTransformation() == 0) {
                    player.setTransformationName(null);
                    player.setTransformationTitle(null);
                }
                player.setTeam(TeamType.NONE);
                final String title = player.getVar("pvp_ctf_title");
                if (title != null) {
                    player.setTitle(title);
                    player.unsetVar("pvp_ctf_title");
                }
                player.sendUserInfo(true);
            });
            _red_team.clear();
            _blue_team.clear();
            _red_team = null;
            _blue_team = null;
            _red_points = null;
            _blue_points = null;
        }

        @Override
        public void initParticipant() {
            final boolean isBuffProtection = PvPEvent.getInstance().config_isBuffProtection();
            getInstance().getPlayers().forEach(player -> {
                player.addListener(getInstance()._dieListner);
                player.addListener(getInstance()._exitListner);
                player.setResurectProhibited(true);
                player.unblock();
                player.standUp();
                if (isBuffProtection) {
                    BUFF_PROTECTION_EFFECT.getEffects(player, player, false, false, false);
                }
            });
        }

        @Override
        public void doneParicipant() {
            if (_rankBroadcastTask != null) {
                _rankBroadcastTask.cancel(true);
                _rankBroadcastTask = null;
            }
            getInstance().getPlayers().forEach(player -> {
                player.removeListener(getInstance()._dieListner);
                player.removeListener(getInstance()._exitListner);
                player.setResurectProhibited(false);
                player.unblock();
                if (player.isDead()) {
                    player.doRevive(100.0);
                    player.setCurrentHpMp((double) player.getMaxHp(), (double) player.getMaxMp());
                    player.setCurrentCp((double) player.getMaxCp());
                }
                player.standUp();
            });
        }

        @Override
        public void portParticipantsTo() {
            int redCnt = 0;
            int blueCnt = 0;
            for (final Player player : getInstance().getPlayers()) {
                final TeamType playerTeam = player.getTeam();
                if (playerTeam != TeamType.BLUE && playerTeam != TeamType.RED) {
                    getInstance()._participants.remove(player.getObjectId());
                    OnExit(player);
                } else {
                    player.setVar(RET_LOC_VAR, player.getLoc().toXYZString(), -1L);
                    if (player.getParty() != null) {
                        player.getParty().removePartyMember(player, false);
                    }
                    if (getInstance().config_hideIdentiti()) {
                        switch (playerTeam) {
                            case RED: {
                                player.setTransformationName(String.format("Red %d", ++redCnt));
                                break;
                            }
                            case BLUE: {
                                player.setTransformationName(String.format("Blue %d", ++blueCnt));
                                break;
                            }
                        }
                    }
                    player.teleToLocation(getRandomTeamLoc(playerTeam), getReflection());
                }
            }
        }

        @Override
        public void portParticipantsBack() {
            getInstance().getPlayers().forEach(player -> {
                final String sloc = player.getVar(RET_LOC_VAR);
                if (sloc != null) {
                    player.unsetVar(RET_LOC_VAR);
                    player.teleToLocation(Location.parseLoc(sloc), ReflectionManager.DEFAULT);
                } else {
                    player.teleToClosestTown();
                }
            });
        }

        @Override
        public void initReflection() {
            _instance_id = getInstance().getNewReflectionId();
            final InstantZone instantZone = InstantZoneHolder.getInstance().getInstantZone(_instance_id);
            ZONE_DEFAULT = String.format("[pvp_%d_ctf_default]", _instance_id);
            ZONE_BLUE = String.format("[pvp_%d_ctf_spawn_blue]", _instance_id);
            ZONE_RED = String.format("[pvp_%d_ctf_spawn_red]", _instance_id);
            (_reflection = new Reflection()).init(instantZone);
            _default_zone = _reflection.getZone(ZONE_DEFAULT);
            _blue_zone = _reflection.getZone(ZONE_BLUE);
            _red_zone = _reflection.getZone(ZONE_RED);
            _default_zone.addListener(getInstance()._zoneListner);
            _blue_zone.addListener(getInstance()._zoneListner);
            _red_zone.addListener(getInstance()._zoneListner);
            final CTFFlagInstance red_flag = new CTFFlagInstance(TeamType.RED, this);
            red_flag.setSpawnedLoc(getRandomTeamLoc(TeamType.RED));
            red_flag.setReflection(getReflection());
            red_flag.setCurrentHpMp((double) red_flag.getMaxHp(), (double) red_flag.getMaxMp(), true);
            red_flag.spawnMe(red_flag.getSpawnedLoc());
            _redFlag = new WeakReference<>(red_flag);
            final CTFFlagInstance blue_flag = new CTFFlagInstance(TeamType.BLUE, this);
            blue_flag.setSpawnedLoc(getRandomTeamLoc(TeamType.BLUE));
            blue_flag.setReflection(getReflection());
            blue_flag.setCurrentHpMp((double) blue_flag.getMaxHp(), (double) blue_flag.getMaxMp(), true);
            blue_flag.spawnMe(blue_flag.getSpawnedLoc());
            _blueFlag = new WeakReference<>(blue_flag);
        }

        @Override
        public void doneReflection() {
            if (_blueFlag.get() != null) {
                _blueFlag.get().destroy();
                _blueFlag.clear();
            }
            if (_redFlag.get() != null) {
                _redFlag.get().destroy();
                _redFlag.clear();
            }
            _redFlag = null;
            _blueFlag = null;
            _default_zone.removeListener(getInstance()._zoneListner);
            _blue_zone.removeListener(getInstance()._zoneListner);
            _red_zone.removeListener(getInstance()._zoneListner);
            _default_zone = null;
            _blue_zone = null;
            _red_zone = null;
            _reflection.collapse();
            _reflection = null;
        }

        @Override
        public Reflection getReflection() {
            return _reflection;
        }

        @Override
        public void OnPlayerDied(final Player target, final Player killer) {
            ThreadPoolManager.getInstance().schedule(getInstance().new TeleportAndReviveTask(target, getRandomTeamLoc(target.getTeam()), getReflection()), (long) (getInstance().config_ReviveDelay() * 1000));
        }

        @Override
        public void OnEnter(final Player player, final Zone zone) {
            if (player != null && !player.isDead()) {
                if (zone == _default_zone && player.getTeam() != TeamType.BLUE && player.getTeam() != TeamType.RED) {
                    player.teleToClosestTown();
                    LOGGER.warn("PvPEvent.CTF: '" + player.getName() + "' in zone.");
                } else if (zone == _blue_zone && player.getTeam() == TeamType.BLUE && player.getObjectId() == _redFlag.get().getOwnerOid()) {
                    _redFlag.get().removeFlag(null);
                    _blue_points.incrementAndGet();
                } else if (zone == _red_zone && player.getTeam() == TeamType.RED && player.getObjectId() == _blueFlag.get().getOwnerOid()) {
                    _blueFlag.get().removeFlag(null);
                    _red_points.incrementAndGet();
                }
            }
        }

        @Override
        public void OnLeave(final Player player, final Zone zone) {
            if (player != null && !_default_zone.checkIfInZone(player.getX(), player.getY(), player.getZ(), getReflection()) && zone == _default_zone) {
                if (player.getTeam() != TeamType.BLUE && player.getTeam() != TeamType.RED) {
                    if (getInstance().config_hideIdentiti() && player.getTransformation() == 0) {
                        player.setTransformationName(null);
                    }
                    player.teleToClosestTown();
                    return;
                }
                final double radian = 6.283185307179586 - PositionUtils.convertHeadingToRadian(player.getHeading());
                final int x = (int) Math.floor(player.getX() - 50.0 * Math.cos(radian));
                final int y = (int) Math.floor(player.getY() + 50.0 * Math.sin(radian));
                final ThreadPoolManager instance = ThreadPoolManager.getInstance();
                final PvPEvent instance2 = getInstance();
                instance2.getClass();
                instance.schedule(instance2.new TeleportTask(player, new Location(x, y, player.getZ()).correctGeoZ(), getReflection()), 3000L);
                if (player.getObjectId() == _blueFlag.get().getOwnerOid()) {
                    _blueFlag.get().removeFlag(player);
                } else if (player.getObjectId() == _redFlag.get().getOwnerOid()) {
                    _redFlag.get().removeFlag(player);
                }
            }
        }

        @Override
        public void OnExit(final Player player) {
            if (_blue_team.containsKey(player.getObjectId())) {
                _blue_team.remove(player.getObjectId());
            } else _red_team.remove(player.getObjectId());
            if (player.getTransformation() == 0) {
                player.setTransformationName(null);
                player.setTransformationTitle(null);
            }
            final String title = player.getVar("pvp_ctf_title");
            if (title != null) {
                player.setTitle(player.getVar("pvp_ctf_title"));
                player.unsetVar("pvp_ctf_title");
            }
        }

        @Override
        public void OnTeleport(final Player player, final int x, final int y, final int z, final Reflection r) {
            if (player != null && !_default_zone.checkIfInZone(x, y, z, r)) {
                final Location loc = getRandomTeamLoc(player.getTeam());
                if (loc != null) {
                    ThreadPoolManager.getInstance().schedule(getInstance().new TeleportTask(player, loc, getReflection()), 3000L);
                }
            }
        }

        private void reward(final Map<Integer, AtomicInteger> team, final List<Pair<ItemTemplate, Long>> rewardList) {
            team.forEach((key, value) -> {
                final int oid = key;
                final Player player = GameObjectsStorage.getPlayer(oid);
                if (player != null) {
                    rewardList.forEach(rewardInfo -> Functions.addItem((Playable) player, rewardInfo.getLeft().getItemId(), rewardInfo.getRight()));
                }
            });
        }

        @Override
        public void MakeWinner() {
            final int blue_pnt = _blue_points.get();
            final int red_pnt = _red_points.get();
            if (blue_pnt > red_pnt) {
                reward(_blue_team, getInstance().config_RewardTeamItemIdAndAmount());
                Announcements.getInstance().announceByCustomMessage("events.PvPEvent.TeamBlueWonTheCTFGameCountIsS1S2", new String[]{String.valueOf(blue_pnt), String.valueOf(red_pnt)});
            } else if (blue_pnt < red_pnt) {
                reward(_red_team, getInstance().config_RewardTeamItemIdAndAmount());
                Announcements.getInstance().announceByCustomMessage("events.PvPEvent.TeamRedWonTheCTFGameCountIsS1S2", new String[]{String.valueOf(red_pnt), String.valueOf(blue_pnt)});
            } else if (blue_pnt == red_pnt) {
                Announcements.getInstance().announceByCustomMessage("events.PvPEvent.TheCTFGameEndedInATie", ArrayUtils.EMPTY_STRING_ARRAY);
            }
            getInstance().broadcast(new ExEventMatchMessage(MessageType.FINISH, null));
        }

        private Location getRandomTeamLoc(final TeamType tt) {
            if (tt == TeamType.BLUE) {
                return _blue_zone.getTerritory().getRandomLoc(_reflection.getGeoIndex());
            }
            if (tt == TeamType.RED) {
                return _red_zone.getTerritory().getRandomLoc(_reflection.getGeoIndex());
            }
            return null;
        }

        private class CTFFlagInstance extends MonsterInstance implements FlagItemAttachment {
            private final TeamType _team;
            private final CTFParticipantController _controller;
            private ItemInstance _flag;

            public CTFFlagInstance(final TeamType team, final CTFParticipantController controller) {
                super(IdFactory.getInstance().getNextId(), NpcTemplateHolder.getInstance().getTemplate((team == TeamType.BLUE) ? 32027 : ((team == TeamType.RED) ? 32027 : -1)));
                _team = team;
                (_flag = ItemFunctions.createItem((team == TeamType.BLUE) ? 6718 : ((team == TeamType.RED) ? 6718 : -1))).setAttachment(this);
                _controller = controller;
            }

            public void destroy() {
                final Player owner = GameObjectsStorage.getPlayer(_flag.getOwnerId());
                if (owner != null) {
                    owner.getInventory().destroyItem(_flag);
                    owner.sendDisarmMessage(_flag);
                }
                _flag.setAttachment(null);
                _flag.deleteMe();
                _flag.delete();
                _flag = null;
                deleteMe();
            }

            @Override
            public boolean isAutoAttackable(final Creature attacker) {
                return isAttackable(attacker);
            }

            @Override
            public boolean isAttackable(final Creature attacker) {
                return attacker != null && attacker.getTeam() != null && attacker.getTeam() != TeamType.NONE && attacker.getTeam() != _team;
            }

            @Override
            protected void onDeath(final Creature killer) {
                if (isAttackable(killer)) {
                    final Player pkiller = killer.getPlayer();
                    if (pkiller != null && ((_team == TeamType.RED && killer.isInZone(_controller._red_zone)) || (_team == TeamType.BLUE && killer.isInZone(_controller._blue_zone)))) {
                        pkiller.getInventory().addItem(_flag);
                        pkiller.getInventory().equipItem(_flag);
                        pkiller.broadcastPacket(new SocialAction(pkiller.getObjectId(), 16));
                        decayMe();
                        if (_team == TeamType.RED) {
                            final ExShowScreenMessage essm = new ExShowScreenMessage("'" + pkiller.getName() + "' captured the Red flag!", 5000, ScreenMessageAlign.TOP_CENTER, false);
                            _controller._red_team.keySet().stream().map(GameObjectsStorage::getPlayer).filter(Objects::nonNull).forEach(player -> player.sendPacket(essm));
                        } else if (_team == TeamType.BLUE) {
                            final ExShowScreenMessage essm = new ExShowScreenMessage("'" + pkiller.getName() + "' captured the Blue flag!", 5000, ScreenMessageAlign.TOP_CENTER, false);
                            _controller._blue_team.keySet().stream().map(GameObjectsStorage::getPlayer).filter(Objects::nonNull).forEach(player -> player.sendPacket(essm));
                        }
                        return;
                    }
                }
                setCurrentHpMp((double) getMaxHp(), (double) getMaxMp(), true);
            }

            public int getOwnerOid() {
                return _flag.getOwnerId();
            }

            public void removeFlag(Player owner) {
                if (owner == null) {
                    owner = GameObjectsStorage.getPlayer(_flag.getOwnerId());
                }
                if (owner != null) {
                    owner.getInventory().removeItem(_flag);
                    owner.sendDisarmMessage(_flag);
                }
                _flag.setOwnerId(0);
                setCurrentHpMp((double) getMaxHp(), (double) getMaxMp(), true);
                spawnMe(_controller.getRandomTeamLoc(_team));
            }

            @Override
            public boolean canPickUp(final Player player) {
                return false;
            }

            @Override
            public void pickUp(final Player player) {
            }

            @Override
            public void setItem(final ItemInstance item) {
                if (item != null) {
                    item.setCustomFlags(39);
                }
            }

            @Override
            public void onLogout(final Player player) {
                player.getInventory().removeItem(_flag);
                _flag.setOwnerId(0);
                setCurrentHpMp((double) getMaxHp(), (double) getMaxMp(), true);
                spawnMe(_controller.getRandomTeamLoc(_team));
            }

            @Override
            public void onDeath(final Player owner, final Creature killer) {
                owner.getInventory().removeItem(_flag);
                owner.sendDisarmMessage(_flag);
                _flag.setOwnerId(0);
                setCurrentHpMp((double) getMaxHp(), (double) getMaxMp(), true);
                spawnMe(_controller.getRandomTeamLoc(_team));
            }

            @Override
            public void onEnterPeace(final Player owner) {
            }

            @Override
            public boolean canAttack(final Player player) {
                player.sendMessage(new CustomMessage("THAT_WEAPON_CANNOT_PERFORM_ANY_ATTACKS", player));
                return false;
            }

            @Override
            public boolean canCast(final Player player, final Skill skill) {
                player.sendMessage(new CustomMessage("THAT_WEAPON_CANNOT_USE_ANY_OTHER_SKILL_EXCEPT_THE_WEAPONS_SKILL", player));
                return false;
            }

            @Override
            public boolean isEffectImmune() {
                return true;
            }

            @Override
            public boolean isDebuffImmune() {
                return true;
            }
        }
    }

    private static class DMParticipantController implements IParticipantController {
        private static final String TITLE_VAR = "pvp_dm_title";
        private final String RET_LOC_VAR;
        private Map<Integer, AtomicInteger> _kills;
        private ScheduledFuture<?> _rankBroadcastTask;
        private Reflection _reflection;
        private int _instance_id;
        private String ZONE_DEFAULT;
        private String ZONE_SPAWN;
        private Zone _default_zone;
        private Zone _spawn_zone;

        private DMParticipantController() {
            _reflection = null;
            _instance_id = 0;
            ZONE_DEFAULT = "[pvp_%d_dm_default]";
            ZONE_SPAWN = "[pvp_%d_dm_spawn]";
            RET_LOC_VAR = "backCoords";
            _default_zone = null;
            _spawn_zone = null;
        }

        @Override
        public void prepareParticipantsTo() {
            _kills = new ConcurrentHashMap<>();
            final boolean dispell = getInstance().config_dispellEffects();
            getInstance().getPlayers().stream().filter(player -> !isDesirePlayer(player)).forEach(player -> {
                getInstance()._participants.remove(player.getObjectId());
                OnExit(player);
            });
            getInstance().getPlayers().forEach(player -> {
                player.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
                if (player.isAttackingNow()) {
                    player.abortAttack(true, false);
                }
                if (player.isCastingNow()) {
                    player.abortCast(true, false);
                }
                player.sendActionFailed();
                player.stopMove();
                player.sitDown(null);
                player.block();
                if (dispell) {
                    player.getEffectList().stopAllEffects();
                }
                player.setCurrentHpMp((double) player.getMaxHp(), (double) player.getMaxMp());
                player.setCurrentCp((double) player.getMaxCp());
                player.setVar("pvp_dm_title", (player.getTitle() != null) ? player.getTitle() : "", -1L);
                _kills.put(player.getObjectId(), new AtomicInteger(0));
                updateTitle(player, 0);
            });
        }

        private void updateTitle(final Player player, final int kills) {
            player.setTransformationTitle(String.format("Kills: %d", kills));
            player.setTitle(player.getTransformationTitle());
            player.broadcastPacket(new NickNameChanged(player));
        }

        @Override
        public void prepareParticipantsFrom() {
            final boolean dispell_after = getInstance().config_dispellEffectsAfter();
            getInstance().getPlayers().forEach(player -> {
                player.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
                if (player.isAttackingNow()) {
                    player.abortAttack(true, false);
                }
                if (player.isCastingNow()) {
                    player.abortCast(true, false);
                }
                player.sendActionFailed();
                player.stopMove();
                player.sitDown(null);
                player.block();
                if (dispell_after) {
                    player.getEffectList().stopAllEffects();
                }
                player.setCurrentHpMp((double) player.getMaxHp(), (double) player.getMaxMp(), true);
                player.setCurrentCp((double) player.getMaxCp());
                if (getInstance().config_hideIdentiti() && player.getTransformation() == 0) {
                    player.setTransformationName(null);
                    player.setTransformationTitle(null);
                }
                final String title = player.getVar("pvp_dm_title");
                if (title != null) {
                    player.setTitle(title);
                    player.unsetVar("pvp_dm_title");
                }
                player.sendUserInfo(true);
            });
            _kills.clear();
            _kills = null;
        }

        @Override
        public void initParticipant() {
            final boolean isBuffProtection = PvPEvent.getInstance().config_isBuffProtection();
            getInstance().getPlayers().forEach(player -> {
                player.addListener(getInstance()._dieListner);
                player.addListener(getInstance()._exitListner);
                player.setResurectProhibited(true);
                player.unblock();
                player.standUp();
                if (isBuffProtection) {
                    BUFF_PROTECTION_EFFECT.getEffects(player, player, false, false, false);
                }
            });
            _rankBroadcastTask = ThreadPoolManager.getInstance().schedule(new RankBroadcastTask(this), RANK_BROADCAST_TIME);
        }

        @Override
        public void doneParicipant() {
            if (_rankBroadcastTask != null) {
                _rankBroadcastTask.cancel(true);
                _rankBroadcastTask = null;
            }
            getInstance().getPlayers().forEach(player -> {
                player.removeListener(getInstance()._dieListner);
                player.removeListener(getInstance()._exitListner);
                player.setResurectProhibited(false);
                player.unblock();
                if (player.isDead()) {
                    player.doRevive(100.0);
                }
                player.setCurrentHpMp((double) player.getMaxHp(), (double) player.getMaxMp(), true);
                player.setCurrentCp((double) player.getMaxCp());
                player.standUp();
            });
        }

        @Override
        public void portParticipantsTo() {
            int playerCnt = 0;
            for (final Player player : getInstance().getPlayers()) {
                player.setVar(RET_LOC_VAR, player.getLoc().toXYZString(), -1L);
                if (player.getParty() != null) {
                    player.getParty().removePartyMember(player, false);
                }
                if (getInstance().config_hideIdentiti()) {
                    player.setTransformationName(String.format("Player %d", ++playerCnt));
                }
                player.teleToLocation(getRandomSpawnLoc(), getReflection());
            }
        }

        @Override
        public void portParticipantsBack() {
            for (final Player player : getInstance().getPlayers()) {
                if (getInstance().config_hideIdentiti() && player.getTransformation() == 0) {
                    player.setTransformationName(null);
                }
                final String sloc = player.getVar(RET_LOC_VAR);
                if (sloc != null) {
                    player.unsetVar(RET_LOC_VAR);
                    player.teleToLocation(Location.parseLoc(sloc), ReflectionManager.DEFAULT);
                } else {
                    player.teleToClosestTown();
                }
            }
        }

        @Override
        public void initReflection() {
            _instance_id = getInstance().getNewReflectionId();
            final InstantZone instantZone = InstantZoneHolder.getInstance().getInstantZone(_instance_id);
            ZONE_DEFAULT = String.format("[pvp_%d_dm_default]", _instance_id);
            ZONE_SPAWN = String.format("[pvp_%d_dm_spawn]", _instance_id);
            (_reflection = new Reflection()).init(instantZone);
            (_default_zone = _reflection.getZone(ZONE_DEFAULT)).addListener(getInstance()._zoneListner);
            _spawn_zone = _reflection.getZone(ZONE_SPAWN);
        }

        private Location getRandomSpawnLoc() {
            return _spawn_zone.getTerritory().getRandomLoc(_reflection.getGeoIndex());
        }

        @Override
        public void doneReflection() {
            _default_zone.removeListener(getInstance()._zoneListner);
            _default_zone = null;
            _spawn_zone = null;
            _reflection.collapse();
            _reflection = null;
        }

        @Override
        public Reflection getReflection() {
            return _reflection;
        }

        @Override
        public void OnPlayerDied(final Player target, final Player killer) {
            if (target != null && killer != null && _kills.containsKey(target.getObjectId()) && _kills.containsKey(killer.getObjectId())) {
                int kcntp = 0;
                final AtomicInteger tcnt = _kills.get(target.getObjectId());
                final AtomicInteger kcnt = _kills.get(killer.getObjectId());
                kcntp = kcnt.addAndGet(tcnt.getAndSet(0) + 1);
                updateTitle(target, 0);
                updateTitle(killer, kcntp);
            }
            ThreadPoolManager.getInstance().schedule(getInstance().new TeleportAndReviveTask(target, getRandomSpawnLoc(), getReflection()), (long) (getInstance().config_ReviveDelay() * 1000));
        }

        @Override
        public void OnEnter(final Player player, final Zone zone) {
            if (player != null && !_kills.containsKey(player.getObjectId())) {
                if (getInstance().config_hideIdentiti() && player.getTransformation() == 0) {
                    player.setTransformationName(null);
                }
                player.teleToClosestTown();
            }
        }

        @Override
        public void OnLeave(final Player player, final Zone zone) {
            if (player != null && !_default_zone.checkIfInZone(player.getX(), player.getY(), player.getZ(), getReflection()) && _kills.containsKey(player.getObjectId())) {
                final double radian = 6.283185307179586 - PositionUtils.convertHeadingToRadian(player.getHeading());
                final int x = (int) Math.floor(player.getX() - 50.0 * Math.cos(radian));
                final int y = (int) Math.floor(player.getY() + 50.0 * Math.sin(radian));
                final ThreadPoolManager instance = ThreadPoolManager.getInstance();
                final PvPEvent instance2 = getInstance();
                instance2.getClass();
                instance.schedule(instance2.new TeleportTask(player, new Location(x, y, player.getZ()).correctGeoZ(), getReflection()), 3000L);
            }
        }

        @Override
        public void OnExit(final Player player) {
            final String title = player.getVar("pvp_dm_title");
            if (title != null) {
                player.setTitle(player.getVar("pvp_dm_title"));
                player.unsetVar("pvp_dm_title");
            }
            _kills.remove(player.getObjectId());
        }

        @Override
        public void OnTeleport(final Player player, final int x, final int y, final int z, final Reflection r) {
            if (player != null && !_default_zone.checkIfInZone(x, y, z, getReflection())) {
                final Location loc = getRandomSpawnLoc();
                if (loc != null) {
                    ThreadPoolManager.getInstance().schedule(getInstance().new TeleportTask(player, loc, getReflection()), 3000L);
                }
            }
        }

        private void rewardPerKill() {
            final int itemId = getInstance().config_ItemPerKill();
            if (itemId <= 0) {
                return;
            }
            _kills.forEach((key, value) -> {
                final int kills = value.get();
                final int killerOid = key;
                final Player player = GameObjectsStorage.getPlayer(killerOid);
                if (kills > 0 && player != null) {
                    Functions.addItem((Playable) player, itemId, (long) kills);
                }
            });
        }

        @Override
        public void MakeWinner() {
            int max_oid = 0;
            int max = Integer.MIN_VALUE;
            for (final Entry<Integer, AtomicInteger> e : _kills.entrySet()) {
                final int val = e.getValue().get();
                if (val > max) {
                    max_oid = e.getKey();
                    max = val;
                }
            }
            if (max_oid != 0 && max > 0) {
                final Player winner = GameObjectsStorage.getPlayer(max_oid);
                getInstance().config_RewardTopItemIdAndAmount().forEach(rewardItemInfo -> Functions.addItem((Playable) winner, rewardItemInfo.getLeft().getItemId(), rewardItemInfo.getRight()));
                if (getInstance().config_RewardHeroHours() > 0) {
                    GlobalServices.makeCustomHero(winner, getInstance().config_RewardHeroHours() * 60 * 60L);
                }
                getInstance().broadcast(new ExEventMatchMessage(MessageType.TEXT, "'" + winner.getName() + "' winns!"), new SystemMessage(1497).addName(winner));
                Announcements.getInstance().announceByCustomMessage("events.PvPEvent.PlayerS1WonTheDMGame", new String[]{winner.getName()});
            } else {
                getInstance().broadcast(new ExEventMatchMessage(MessageType.TEXT, "Tie"), Msg.THE_GAME_ENDED_IN_A_TIE);
                Announcements.getInstance().announceByCustomMessage("events.PvPEvent.TheDMGameEndedInATie", ArrayUtils.EMPTY_STRING_ARRAY);
            }
            rewardPerKill();
        }

        private class RankBroadcastTask implements Runnable {
            private final DMParticipantController _controller;

            public RankBroadcastTask(final DMParticipantController controller) {
                _controller = controller;
            }

            @Override
            public void run() {
                if (getInstance().getState() != PvPEventState.COMPETITION) {
                    return;
                }
                _controller._rankBroadcastTask = ThreadPoolManager.getInstance().schedule(this, RANK_BROADCAST_TIME);
            }
        }
    }

    private class PvPStateTask implements Runnable {
        private final PvPEventState _to_state;

        public PvPStateTask(final PvPEventState to_state) {
            _to_state = to_state;
        }

        @Override
        public void run() {
            try {
                switch (_to_state) {
                    case STANDBY: {
                        getInstance().goStandby();
                        break;
                    }
                    case REGISTRATION: {
                        getInstance().goRegistration();
                        break;
                    }
                    case PORTING_TO: {
                        getInstance().goPortingTo();
                        break;
                    }
                    case PREPARE_TO: {
                        getInstance().goPrepareTo();
                        break;
                    }
                    case COMPETITION: {
                        getInstance().goCompetition();
                        break;
                    }
                    case WINNER: {
                        getInstance().goWinner();
                        break;
                    }
                    case PREPARE_FROM: {
                        getInstance().goPrepareFrom();
                        break;
                    }
                    case PORTING_FROM: {
                        getInstance().goPortingFrom();
                        break;
                    }
                }
            } catch (Exception ex) {
                LOGGER.warn("PvPEvent: Exception on changing state to " + _to_state + " state.", ex);
                ex.printStackTrace();
            }
        }
    }

    private class RegisrationTask implements Runnable {
        private final int _left;
        private final RegisrationState _to_reg_state;

        public RegisrationTask(final RegisrationState to_state, final int left) {
            _left = left;
            _to_reg_state = to_state;
        }

        @Override
        public void run() {
            if (getInstance()._regState != _to_reg_state && _to_reg_state == RegisrationState.ANNOUNCE) {
                if (getInstance()._desireContainer != null) {
                    getInstance()._desireContainer.clear();
                    getInstance()._desireContainer = null;
                }
                getInstance()._desireContainer = new ConcurrentSkipListSet<>();
            }
            getInstance()._regState = _to_reg_state;
            switch (_to_reg_state) {
                case ANNOUNCE: {
                    if (_left > 0) {
                        Announcements.getInstance().announceByCustomMessage("events.PvPEvent.EventS1StartAtS2Minutes", new String[]{getRule().name(), String.valueOf(_left)});
                        getInstance().scheduleProcessTask(new RegisrationTask(RegisrationState.ANNOUNCE, Math.max(0, _left - getInstance()._event_announce_reductor)), getInstance()._event_announce_reductor * 60 * 1000);
                        break;
                    }
                    getInstance().scheduleProcessTask(new RegisrationTask(RegisrationState.REQUEST, 0), 1000L);
                    break;
                }
                case REQUEST: {
                    getInstance().broadcastParticipationRequest();
                    getInstance().scheduleProcessTask(new RegisrationTask(getInstance().config_isUseCapcha() ? RegisrationState.CAPCHA : RegisrationState.MORPH, 0), 40000L);
                    break;
                }
                case CAPCHA: {
                    getInstance().broadcastCapchaRequest();
                    getInstance().scheduleProcessTask(new RegisrationTask(RegisrationState.MORPH, 0), 40000L);
                    break;
                }
                case MORPH: {
                    getInstance().morphDesires();
                    break;
                }
            }
        }
    }

    private class CompetitionRunTask implements Runnable {
        private final int _left;

        public CompetitionRunTask(final int left) {
            _left = left;
        }

        @Override
        public void run() {
            switch (_left) {
                case 30: {
                    getInstance().scheduleProcessTask(new CompetitionRunTask(_left - 25), 25000L);
                    return;
                }
                case 0: {
                    getInstance().scheduleStateChange(PvPEventState.COMPETITION, 100L);
                    getInstance().broadcast(new ExEventMatchMessage(MessageType.START, null));
                    return;
                }
                case 5: {
                    getInstance().broadcast(new ExEventMatchMessage(MessageType.NUMBER_1, null));
                    for (final Player player : getInstance().getPlayers()) {
                        player.broadcastUserInfo(true);
                    }
                    break;
                }
                case 4: {
                    getInstance().broadcast(new ExEventMatchMessage(MessageType.NUMBER_4, null));
                    break;
                }
                case 3: {
                    getInstance().broadcast(new ExEventMatchMessage(MessageType.NUMBER_3, null));
                    break;
                }
                case 2: {
                    getInstance().broadcast(new ExEventMatchMessage(MessageType.NUMBER_2, null));
                    break;
                }
                case 1: {
                    getInstance().broadcast(new ExEventMatchMessage(MessageType.NUMBER_1, null));
                    break;
                }
            }
            getInstance().scheduleProcessTask(new CompetitionRunTask(_left - 1), 1000L);
        }
    }

    private class DieListner implements OnDeathListener {
        @Override
        public void onDeath(final Creature actor, final Creature killer) {
            try {
                if (getInstance().getState() != PvPEventState.COMPETITION) {
                    return;
                }
                final Player ptarget = actor.getPlayer();
                final Player pkiller = killer.getPlayer();
                if (ptarget != null) {
                    getInstance().getRule().getParticipantController().OnPlayerDied(ptarget, pkiller);
                }
            } catch (Exception ex) {
                LOGGER.warn("PVPEvent.onDeath :", ex);
            }
        }
    }

    private class ZoneEnterLeaveListner implements OnZoneEnterLeaveListener {
        @Override
        public void onZoneEnter(final Zone zone, final Creature actor) {
            try {
                if (getInstance().getState() != PvPEventState.COMPETITION || !actor.isPlayer()) {
                    return;
                }
                getInstance().getRule().getParticipantController().OnEnter(actor.getPlayer(), zone);
            } catch (Exception ex) {
                LOGGER.warn("PVPEvent.onZoneEnter :", ex);
            }
        }

        @Override
        public void onZoneLeave(final Zone zone, final Creature actor) {
            try {
                if (getInstance().getState() != PvPEventState.COMPETITION || !actor.isPlayer()) {
                    return;
                }
                getInstance().getRule().getParticipantController().OnLeave(actor.getPlayer(), zone);
            } catch (Exception ex) {
                LOGGER.warn("PVPEvent.onZoneLeave :", ex);
            }
        }
    }

    private class TeleportListner implements OnTeleportListener {
        @Override
        public void onTeleport(final Player player, final int x, final int y, final int z, final Reflection r) {
            try {
                if (getInstance().getState() != PvPEventState.COMPETITION) {
                    return;
                }
                getInstance().getRule().getParticipantController().OnTeleport(player, x, y, z, r);
            } catch (Exception ex) {
                LOGGER.warn("PVPEvent.onTeleport :", ex);
            }
        }
    }

    private class ExitListner implements OnPlayerExitListener {
        @Override
        public void onPlayerExit(final Player player) {
            try {
                if (getInstance().getState() == PvPEventState.STANDBY) {
                    return;
                }
                getInstance().getRule().getParticipantController().OnExit(player);
                getInstance()._participants.remove(player.getObjectId());
            } catch (Exception ex) {
                LOGGER.warn("PVPEvent.onPlayerExit :", ex);
            }
        }
    }

    private class TeleportTask implements Runnable {
        private final Player _player;
        private final Location _loc;
        private final Reflection _ref;

        public TeleportTask(final Player player, final Location loc, final Reflection ref) {
            _player = player;
            _loc = loc;
            _ref = ref;
        }

        @Override
        public void run() {
            _player.teleToLocation(_loc, _ref);
        }
    }

    private class TeleportAndReviveTask implements Runnable {
        private final HardReference<Player> _playerRef;
        private final Location _loc;
        private final Reflection _ref;

        public TeleportAndReviveTask(final Player player, final Location loc, final Reflection ref) {
            _playerRef = player.getRef();
            _loc = loc;
            _ref = ref;
        }

        @Override
        public void run() {
            final Player player = _playerRef.get();
            if (player == null) {
                return;
            }
            synchronized (player) {
                player.teleToLocation(_loc, _ref);
                if (!player.isConnected()) {
                    player.onTeleported();
                }
                if (player.isDead()) {
                    player.setCurrentHp((double) player.getMaxHp(), true, true);
                    player.setCurrentCp((double) player.getMaxCp());
                    player.setCurrentMp((double) player.getMaxMp());
                    player.broadcastPacket(new Revive(player));
                    if (getInstance().config_isBuffProtection()) {
                        BUFF_PROTECTION_EFFECT.getEffects(player, player, false, false, false);
                    }
                }
            }
        }
    }
}
