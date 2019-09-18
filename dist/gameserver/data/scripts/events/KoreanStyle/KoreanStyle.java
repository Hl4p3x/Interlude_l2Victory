package events.KoreanStyle;

import com.stringer.annotations.HideAccess;
import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.data.xml.holder.KoreanStyleArenaHolder;
import ru.j2dev.gameserver.handler.bypass.Bypass;
import ru.j2dev.gameserver.listener.actor.OnDeathListener;
import ru.j2dev.gameserver.listener.actor.player.OnAnswerListener;
import ru.j2dev.gameserver.listener.actor.player.OnPlayerExitListener;
import ru.j2dev.gameserver.listener.actor.player.OnTeleportListener;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Zone.ZoneType;
import ru.j2dev.gameserver.model.actor.listener.CharListenerList;
import ru.j2dev.gameserver.model.base.TeamType;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.model.entity.olympiad.OlympiadPlayersManager;
import ru.j2dev.gameserver.model.event.*;
import ru.j2dev.gameserver.model.instances.DoorInstance;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ConfirmDlg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExEventMatchMessage.MessageType;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import ru.j2dev.gameserver.skills.AbnormalEffect;
import ru.j2dev.gameserver.templates.arenas.KoreanStyleArena;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.Location;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by JunkyFunky
 * on 09.05.2018 18:58
 * group j2dev
 */
@HideAccess
public class KoreanStyle extends PvpEvent {
    private final HwidChecker hwid_check = new HwidChecker(Config.EVENT_KOREAN_STYLE_CHECK_HWID);
    private final IpChecker ip_check = new IpChecker(Config.EVENT_KOREAN_STYLE_CHECK_IP);
    private final AttackMagicListenerImpl _attackListener = new AttackMagicListenerImpl(Config.EVENT_KOREAN_STYLE_DISSALOWED_SKILL, true);
    private final OnTeleportImpl onTeleportListener = new OnTeleportImpl();
    private Location teamBlueLoc1;
    private Location teamRedLoc1;
    private Location teamBlueLoc2;
    private Location teamRedLoc2;
    private Location team1Fightloc;
    private Location team2Fightloc;
    private Player _redTeamFighter;
    private Player _blueTeamFighter;
    private PvpEventTeam teamBlue = new PvpEventTeam(TeamType.BLUE, "KOREAN_BLUE");
    private PvpEventTeam teamRed = new PvpEventTeam(TeamType.RED, "KOREAN_RED");
    private final ZoneListener zoneListener = new ZoneListener(teamBlue, teamRed);
    private List<KoreanStyleArena> arenas;
    private KoreanStyleArena arena;

    private List<Integer> getFighterBuffs() {
        return Config.EVENT_KOREAN_STYLE_BUFFS_FIGHTER;
    }

    private List<Integer> getMageBuffs() {
        return Config.EVENT_KOREAN_STYLE_BUFFS_MAGE;
    }

    private void upParalyzePlayers() {
        teamRed.upParalyzePlayers();
        teamBlue.upParalyzePlayers();
    }

    private void giveItemsToWinner(final PvpEventTeam team, final double rate) {
        team.getAllPlayers()
                .stream()
                .filter(Objects::nonNull)
                .forEach(player -> ItemFunctions.addItem(player, Config.EVENT_KOREAN_STYLE_ITEM_ID, Math.round((Config.EVENT_KOREAN_STYLE_RATE ? player.getLevel() : 1) * Config.EVENT_KOREAN_STYLE_ITEM_COUNT * rate)));
    }

    private void loosePlayer(final Player player, final boolean inc) {
        if (player != null) {
            if (inc && player.getTeam() != TeamType.NONE) {
                teamRed.removeFromLive(player);
                teamBlue.removeFromLive(player);
            } else {
                teamRed.removeFromLive(player);
                teamBlue.removeFromLive(player);
                player.setTeam(TeamType.NONE);
                player.sendMessage(new CustomMessage("scripts.events.KoreanStyle.YouLose", player));
            }
        }
    }

    private void showPointsToScreen(boolean end) {
        showPoints(teamBlue, teamBlue, "Blue Team :", ScreenMessageAlign.MIDDLE_LEFT, end);
        showPoints(teamRed, teamBlue, "Blue Team :", ScreenMessageAlign.MIDDLE_LEFT, end);
        showPoints(teamBlue, teamRed, "Red Team :", ScreenMessageAlign.MIDDLE_RIGHT, end);
        showPoints(teamRed, teamRed, "Red Team :", ScreenMessageAlign.MIDDLE_RIGHT, end);
    }

    private void showPoints(final PvpEventTeam team, final PvpEventTeam teamToShow, final String commandName, final ScreenMessageAlign align, boolean end) {
        String message = teamToShow.getPlayerInfos().stream().
                filter(playerInfo -> playerInfo.getKillsCount() > 0).
                map(playerInfo -> '\n' + playerInfo.getPlayer().getName() + " : " + playerInfo.getKillsCount()).
                collect(Collectors.joining("", commandName, ""));
        team.sendPacketToAll(new ExShowScreenMessage(message, end ? 5000 : 600000, align, true));
    }

    private boolean checkCountTeam(final int team) {
        if (team == 1 && teamBlue.getPlayersCount() < Config.EVENT_KOREAN_STYLE_MAX_LENGTH_TEAM) {
            return true;
        } else return team == 2 && teamRed.getPlayersCount() < Config.EVENT_KOREAN_STYLE_MAX_LENGTH_TEAM;

    }

    private void removeAura() {
        teamRed.removeAura();
        teamBlue.removeAura();
    }

    private void clearArena() {
        arena.getZones().keySet().stream().map(_reflection::getZone).forEach(_zone -> _zone.getInsidePlayables().stream().filter(obj -> obj.getReflection() == _reflection).forEach(obj -> {
            final Player player = obj.getPlayer();
            if (player != null && !teamRed.isLive(player.getObjectId()) && !teamBlue.isLive(player.getObjectId())) {
                player.sendMessage(player.isLangRus() ? "Вы не можете находиться в зоне проведения ПвП Ивента!" : "You not allow primission on enter PvP Event Zone now!");
                LOGGER.warn("Huligan on KoreanStyle : {}", player);
                player.teleToClosestTown();
            }
        }));
    }

    private void removePlayer(final Player player) {
        if (player != null) {
            teamRed.removePlayer(player);
            teamBlue.removePlayer(player);
        }
    }

    private void openArenaDoors() {
        _reflection.getDoors().forEach(DoorInstance::openMe);
    }

    private void closeArenaDoors() {
        _reflection.getDoors().forEach(DoorInstance::closeMe);
    }

    private void saveBackCoords(final List<Player> players) {
        players.forEach(player -> savePlayerBackCoord(player, "KoreanStyleBack"));
    }

    private void teleportToSavedCoords(final List<Player> players) {
        players.forEach(player -> {
            player.setIsInKoreanStyle(false);
            removePlayer(player);
            unlockItems(player, Config.EVENT_KOREAN_STYLE_DISSALOWED_ITEMS);
            player.removeListener(_attackListener);
            player.removeListener(onTeleportListener);
            teleportPlayerToBack(player, true, Config.EVENT_KOREAN_STYLE_BUFFS_ONSTART, "KoreanStyleBack");
        });
    }

    private void buffPlayers() {
        teamBlue.getAllPlayers().forEach(this::addBuffToPlayer);
        teamRed.getAllPlayers().forEach(this::addBuffToPlayer);
    }

    private void cleanPlayers() {
        teamRed.getAllPlayers().stream().filter(player -> !checkPlayer(player, false)).forEach(this::removePlayer);
        teamBlue.getAllPlayers().stream().filter(player -> !checkPlayer(player, false)).forEach(this::removePlayer);
    }

    @Override
    public void onEscape(final Player player) {
        if (_status > 1 && player != null && player.getTeam() != TeamType.NONE && (teamRed.isLive(player.getObjectId()) || teamBlue.isLive(player.getObjectId()))) {
            removePlayer(player);
            ThreadPoolManager.getInstance().execute(new checkLive());
        }
    }

    private void addBuffToPlayer(final Player player) {
        if (player != null) {
            if (player.isMageClass()) {
                playerBuff(player, getMageBuffs());
            } else {
                playerBuff(player, getFighterBuffs());
            }
        }
    }

    private void prepareFighter(final Player player) {
        addBuffToPlayer(player);
        //player.standUp();
        //player.broadcastUserInfo(true);
        player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
        player.setCurrentCp(player.getMaxCp());
        unParalizePlayer(player);
        player.resetReuse();
        final Location pos;
        if (player.getTeam() == TeamType.BLUE) {
            pos = team1Fightloc;
        } else {
            pos = team2Fightloc;
        }
        player.teleToLocation(pos.x, pos.y, pos.z, _reflection);
        player.setHeading(pos.h);
    }

    private void unParalizePlayer(Player player) {
        if (player.isFrozen()) {
            player.stopFrozen();
            player.stopDamageBlocked();
        }
        if (player.getPet() != null && player.getPet().isFrozen()) {
            player.getPet().stopFrozen();
            player.getPet().stopDamageBlocked();
        }
    }

    private boolean checkPlayer(final Player player, final boolean first) {
        if (first && !_isRegistrationActive) {
            player.sendMessage(new CustomMessage("scripts.events.Late", player));
            return false;
        }

        if (first && player.isDead()) {
            return false;
        }

        if (first && (teamRed.isInTeam(player.getObjectId()) || teamBlue.isInTeam(player.getObjectId()))) {
            player.sendMessage(new CustomMessage("scripts.events.KoreanStyle.Cancelled", player));
            return false;
        }

        if (player.getLevel() < _minLevel || player.getLevel() > _maxLevel) {
            player.sendMessage(new CustomMessage("scripts.events.KoreanStyle.CancelledLevel", player));
            return false;
        }

        if (player.isMounted()) {
            player.sendMessage(new CustomMessage("scripts.events.KoreanStyle.Cancelled", player));
            return false;
        }

        if (player.isInDuel()) {
            player.sendMessage(new CustomMessage("scripts.events.KoreanStyle.CancelledDuel", player));
            return false;
        }

        if (first && (player.getTeam() != TeamType.NONE)) {
            player.sendMessage(new CustomMessage("scripts.events.KoreanStyle.CancelledOtherEvent", player));
            return false;
        }

        if (player.isOlyParticipant() || first && OlympiadPlayersManager.getInstance().isRegistred(player)) {
            player.sendMessage(new CustomMessage("scripts.events.KoreanStyle.CancelledOlympiad", player));
            return false;
        }

        if (player.isInParty() && player.getParty().isInDimensionalRift()) {
            player.sendMessage(new CustomMessage("scripts.events.KoreanStyle.CancelledOtherEvent", player));
            return false;
        }

        if (player.isTeleporting()) {
            player.sendMessage(new CustomMessage("scripts.events.KoreanStyle.CancelledTeleport", player));
            return false;
        }

        if (player.isCursedWeaponEquipped()) {
            player.sendMessage(new CustomMessage("scripts.events.KoreanStyle.Cancelled", player));
            return false;
        }

        // последним проверяем HardwareID
        if (first && !hwid_check.canParticipate(player)) {
            player.sendMessage(new CustomMessage("scripts.events.KoreanStyle.Cancelled", player));
            return false;
        }

        if (first && !ip_check.canParticipate(player)) {
            player.sendMessage(new CustomMessage("scripts.events.KoreanStyle.Cancelled", player));
            return false;
        }

        return true;
    }

    @Override
    public void onInit() {
        arenas = KoreanStyleArenaHolder.getInstance().getArenas();
        CharListenerList.addGlobal(new onDeathListenerImpl());
        CharListenerList.addGlobal(new onPlayerExitListenerImpl());

        // Если ивент активен, но пробуем зашедулить
        if (isActive()) {
            scheduleEventStart();
        }

        LOGGER.info("Loaded Event: " + getEventType() + '[' + isActive() + ']' + " loaded arenas :" + arenas.size());

    }

    @Bypass("events.KoreanStyle:activateEvent")
    public void activateEvent(Player player, NpcInstance lastNpc, String[] args) {
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }

        if (setStatus(true)) {
            // при активации ивента, если он не был активирован, то пробуем стартовать. Так как как таск стартует только при загрузке
            if (_startTask == null) {
                scheduleEventStart();
            }
            LOGGER.info("Event '{}' activated, by: {}", getEventType(), player.toString());
            sayToAll("scripts.events.AnnounceEventStarted", getEventType().getName(false));
        } else {
            player.sendMessage("Event '" + getEventType() + "' already active.");
        }
    }

    @Bypass("events.KoreanStyle:deactivateEvent")
    public void deactivateEvent(Player player, NpcInstance lastNpc, String[] args) {
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }

        if (setStatus(false)) {
            // отменяем таск
            if (_startTask != null) {
                _startTask.cancel(true);
                _startTask = null;
            }
            LOGGER.info("Event '{}' deactivated, by: {}", getEventType(), player.toString());
            sayToAll("scripts.events.AnnounceEventStoped", getEventType().getName(false));
        } else {
            player.sendMessage("Event '" + getEventType() + "' not active.");
        }
    }

    @Bypass("events.KoreanStyle:manualEnd")
    public void manualEnd(Player player, NpcInstance lastNpc, String[] args) {
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (_status > 1) {
            ThreadPoolManager.getInstance().execute(new endBattle());
        }
    }

    /**
     * 0 - категория уровней<br>
     * 1 - если больше 0 то автоматически продолжается
     */
    @Override
    @Bypass("events.KoreanStyle:start")
    public void start(Player player, NpcInstance lastNpc, String[] args) {
        if (isRunned()) {
            return;
        }

        if (args.length != 2) {
            return;
        }
        if (player != null && !player.getPlayerAccess().IsEventGm) {
            LOGGER.info(player.toString() + " : possible hack event {} to start", getEventType());
            return;
        }

        final int category;
        final int autoContinue;
        try {
            category = Integer.parseInt(args[0]);
            autoContinue = Integer.parseInt(args[1]);
        } catch (final NumberFormatException e) {
            return;
        }

        _autoContinue = autoContinue;

        if (category == -1) {
            _minLevel = 1;
            _maxLevel = 80;
        } else {
            _minLevel = getMinLevelForCategory(category);
            _maxLevel = getMaxLevelForCategory(category);
        }

        if (_endTask != null) {
            return;
        }

        _status = 0;
        _isRegistrationActive = true;

        arena = Rnd.get(arenas);
        LOGGER.debug(getEventType() + " Event Start Debug : " + " arena id " + arena.getId() + " zones array " + arena.getZones().keySet());
        _reflection = new Reflection(getEventType().getRefId(), true);
        arena.initDoors();
        _reflection.init(arena.getArenaDoors(), arena.getZones());
        arena.getZones().keySet().forEach(s -> {
            _reflection.getZone(s).setType(ZoneType.peace_zone);
            _reflection.getZone(s).addListener(zoneListener);
        });

        teamBlueLoc1 = arena.getTeamLeftCorner(1);
        teamBlueLoc2 = arena.getTeamRightCorner(1);
        teamRedLoc1 = arena.getTeamLeftCorner(2);
        teamRedLoc2 = arena.getTeamRightCorner(2);
        team1Fightloc = arena.getFightLocation(1);
        team2Fightloc = arena.getFightLocation(2);
        _time_to_start = Config.EVENT_KOREAN_STYLE_TIME_TO_START;
        sayToAll("scripts.events.KoreanStyle.AnnouncePreStart", String.valueOf(_time_to_start), String.valueOf(_minLevel), String.valueOf(_maxLevel));

        if(isQuestionAllowed()) {
            scheduleRunnable(new question(), 10000);
        }
        scheduleRunnable(new announce(), 60000);
        //logEventStart(category, _autoContinue);
        onStart(this);
    }

    @Bypass("events.KoreanStyle:addPlayer")
    public void addPlayer(Player player, NpcInstance lastNpc, String[] args) {
        if (player == null || !checkPlayer(player, true)) {
            return;
        }
        if (!_isRegistrationActive) {
            sendNoActiveRegister(player);
            return;
        }
        if(isInTeam(player, teamBlue, teamRed)) {
            sendAlreadyRegistred(player);
            return;
        }

        int team;
        final int size1 = teamRed.getPlayersCount();
        final int size2 = teamBlue.getPlayersCount();

        if (size1 > size2) {
            team = 2;
        } else if (size1 < size2) {
            team = 1;
        } else {
            team = Rnd.get(1, 2);
        }

        if (!checkCountTeam(team)) {
            player.sendMessage(new CustomMessage("scripts.events.MaxCountTeam", player));
            return;
        }

        switch (team) {
            case 1:
                teamRed.addPlayer(player);
                player.sendMessage(new CustomMessage("scripts.events.KoreanStyle.Registered", player));
                break;
            case 2:
                teamBlue.addPlayer(player);
                player.sendMessage(new CustomMessage("scripts.events.KoreanStyle.Registered", player));
                break;
            default:
                LOGGER.info("WTF??? Command id 0 in KoreanStyle...");
                break;
        }
    }

    @Bypass("events.KoreanStyle:removePlayerCb")
    public void removePlayerCb(Player player, NpcInstance lastNpc, String[] args) {
        if (player == null) {
            return;
        }
        if (checkRegisterCondition(player, teamRed, teamBlue)) {
            return;
        }
        removePlayer(player);
        sendCancelRegister(player);
    }

    /**
     * проверяет возможность запуска ивента и стартует такс в указанное в конфигах время
     */
    private void scheduleEventStart() {
        try {
            long day = 24 * 60 * 60 * 1000L;
            Calendar ci = Calendar.getInstance();
            for (Config.EventInterval config : Config.EVENT_KOREAN_STYLE_INTERVAL) {

                ci.set(Calendar.HOUR_OF_DAY, config.hour);
                ci.set(Calendar.MINUTE, config.minute);
                ci.set(Calendar.SECOND, 0);

                long delay = ci.getTimeInMillis() - System.currentTimeMillis();
                if (delay < 0) {
                    delay = delay + day;
                }

                _startTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new StartTask(String.valueOf(config.category), "0"), delay, day);
            }
        } catch (Exception e) {
            LOGGER.info(getEventType()+": Error figuring out a start time. Check EventTvtInterval in config file.");
        }
    }

    private void teleportTeamPlayersToArenaLocations(final List<Player> players, final Location startLoc, final Location endLoc) {
        final AtomicInteger index = new AtomicInteger(0);
        players.forEach(player -> {
            player.setMount(0,0,0);
            final Location loc = new Location(startLoc.x, startLoc.y, startLoc.z, startLoc.h);
            loc.x += ((endLoc.x - startLoc.x) / players.size()) * index.get();
            loc.y += ((endLoc.y - startLoc.y) / players.size()) * index.getAndIncrement();
            player.addListener(onTeleportListener);
            player.teleToLocation(loc, _reflection);
            player.setHeading(loc.h);
            player.addListener(_attackListener);
            player.startAbnormalEffect(AbnormalEffect.DANCE_STUNNED);
            //player.sitDown(null);
            //player.broadcastUserInfo(true);
            player.setIsInKoreanStyle(true);
            lockItems(player, Config.EVENT_KOREAN_STYLE_DISSALOWED_ITEMS);
        });
    }

    private boolean isRunned() {
        return _isRegistrationActive || _status > 0;
    }

    @Override
    public PvpEventType getEventType() {
        return PvpEventType.KoreanStyle;
    }

    @Override
    public boolean isAllowClanSkill() {
        return Config.EVENT_KOREAN_STYLE_ALLOW_CLAN_SKILL;
    }

    @Override
    public boolean isAllowHeroSkill() {
        return Config.EVENT_KOREAN_STYLE_ALLOW_HERO_SKILL;
    }

    @Override
    public boolean isAllowBuffs() {
        return Config.EVENT_KOREAN_STYLE_ALLOW_BUFFS;
    }

    @Override
    public boolean isDispelTransformation() {
        return Config.EVENT_KOREAN_STYLE_DISPEL_TRANSFORMATION;
    }

    @Override
    public boolean isAllowSummons() {
        return Config.EVENT_KOREAN_STYLE_ALLOW_SUMMONS;
    }

    @Override
    public boolean isPartyDisable() {
        return false;
    }

    @Override
    public boolean isPartyDisableMassEvent() {
        return true;
    }

    @Override
    public boolean isEquipedByCustomArmorWeapon() {
        return Config.EVENT_KOREAN_STYLE_ALLOW_EQUIP;
    }

    @Override
    public boolean isQuestionAllowed() {
        return Config.EVENT_KOREAN_STYLE_ALLOW_QUESTION;
    }

    @Override
    public int getBuffTime() {
        return Config.EVENT_KOREAN_STYLE_TIME_FOR_FIGHT;
    }

    /**
     * Задача заморозки игроков
     */
    public class paralyzePlayers extends RunnableImpl {
        @Override
        public void runImpl() {
            teamBlue.paralyzePlayers();
            teamRed.paralyzePlayers();
        }
    }

    public class prepare extends RunnableImpl {

        @Override
        public void runImpl() {
            cleanPlayers();
            clearArena();
            closeArenaDoors();


            scheduleRunnable(new ressurectPlayers(), 100);
            scheduleRunnable(new healPlayers(), 200);
            scheduleRunnable(new paralyzePlayers(), 300);
            scheduleRunnable(new saveBackCoords(), 400);
            scheduleRunnable(new checkLive(), 500);
            scheduleRunnable(new teleportPlayersToArena(), 600);
            scheduleRunnable(new go(), 60000);

            sayToAll("scripts.events.KoreanStyle.AnnounceFinalCountdown");
        }
    }

    public class endBattle extends RunnableImpl {

        @Override
        public void runImpl() {
            arena.getZones().keySet().stream().map(_reflection::getZone).forEach(_zone -> {
                _zone.removeListener(zoneListener);
                _zone.setType(ZoneType.peace_zone);
            });
            _status = 0;
            showPointsToScreen(true);
            removeAura();
            upParalyzePlayers();

            if (teamBlue.getTeamPoints() > teamRed.getTeamPoints()) {
                sayToAll("scripts.events.KoreanStyle.AnnounceFinishedBlueWins");
                giveItemsToWinner(teamBlue, 1);
            } else if (teamRed.getTeamPoints() > teamBlue.getTeamPoints()) {
                sayToAll("scripts.events.KoreanStyle.AnnounceFinishedRedWins");
                giveItemsToWinner(teamRed, 1);
            } else if (teamRed.getTeamPoints() == teamBlue.getTeamPoints()) {
                sayToAll("scripts.events.KoreanStyle.AnnounceFinishedDraw");
                giveItemsToWinner(teamBlue, 0.5);
                giveItemsToWinner(teamRed, 0.5);
            }

            sayToAll("scripts.events.KoreanStyle.AnnounceScore", String.valueOf(teamRed.getTeamPoints()), String.valueOf(teamBlue.getTeamPoints()));

            sayToAll("scripts.events.KoreanStyle.AnnounceEnd");
            scheduleRunnable(new end(), 30000);

            _isRegistrationActive = false;
            if (_endTask != null) {
                _endTask.cancel(false);
                _endTask = null;
            }
        }
    }

    private class onDeathListenerImpl implements OnDeathListener {

        @Override
        public void onDeath(final Creature victim, final Creature killer) {
            if (_status > 1 && victim != null && victim.isPlayer() && (teamRed.isLive(victim.getObjectId()) || teamBlue.isLive(victim.getObjectId()))) {

                if (Config.EVENT_KOREAN_STYLE_ALLOW_KILL_BONUS) {
                    ItemFunctions.addItem(killer.getPlayer(), Config.EVENT_KOREAN_STYLE_KILL_BONUS_ID, Config.EVENT_KOREAN_STYLE_KILL_BONUS_COUNT);
                }
                if (Config.EVENT_KOREAN_STYLE_ALLOW_PVP_COUNT_BONUS) {
                    killer.getPlayer().setPvpKills(killer.getPlayer().getPvpKills() + 1);
                }
                if (Config.EVENT_KOREAN_STYLE_ALLOW_PVP_DECREASE_ONDIE) {
                    int pvpkills = victim.getPlayer().getPvpKills() - 1;
                    if (pvpkills < 1) {
                        pvpkills = 0;
                    }
                    victim.getPlayer().setPvpKills(pvpkills);
                }
                // если жертва находиться в одной из команд увеличиваем счётчик смертей игроку
                if (victim.getTeam() == TeamType.RED) {
                    teamRed.getPlayerInfo(victim.getObjectId()).incrementDeaths(); // Добавляем очки противоположной команде
                    teamBlue.increaseTeamPoints();
                } else {
                    teamBlue.getPlayerInfo(victim.getObjectId()).incrementDeaths(); // Добавляем очки противоположной команде
                    teamRed.increaseTeamPoints();
                }
                // увеличиваем счётчик убийств
                if (killer.getPlayer() != null) { // вдруг суммоном убил
                    final Player player = killer.getPlayer();
                    if (player.getTeam() == TeamType.RED) { // если убийца находиться в одной из команд
                        PvpEventPlayerInfo teamRedPlayerInfo = teamRed.getPlayerInfo(player.getObjectId());
                        teamRedPlayerInfo.incrementKills();
                        checkKillsAndAnnounce(teamRedPlayerInfo, teamBlue, teamRed);
                    } else {
                        PvpEventPlayerInfo teamBluePlayerInfo = teamBlue.getPlayerInfo(player.getObjectId());
                        teamBluePlayerInfo.incrementKills();
                        checkKillsAndAnnounce(teamBluePlayerInfo, teamRed, teamBlue);
                    }
                }
                showPointsToScreen(false);
                scheduleRunnable(new getNewFighters(), 5000);
                loosePlayer(victim.getPlayer(), true);
                ThreadPoolManager.getInstance().execute(new checkLive());
            }
        }
    }

    private class onPlayerExitListenerImpl implements OnPlayerExitListener {

        @Override
        public void onPlayerExit(final Player player) {
            if (player == null || player.getTeam() == TeamType.NONE) {
                return;
            }

            // Вышел или вылетел во время регистрации
            if (_status == 0 && _isRegistrationActive && player.getTeam() != TeamType.NONE && (teamRed.isLive(player.getObjectId()) || teamBlue.isLive(player.getObjectId()))) {
                removePlayer(player);
                return;
            }

            // Вышел или вылетел во время телепортации
            if (_status == 1 && (teamRed.isLive(player.getObjectId()) || teamBlue.isLive(player.getObjectId()))) {
                removePlayer(player);
                teleportToSavedCoords(Collections.singletonList(player));
                return;
            }

            // Вышел или вылетел во время эвента
            onEscape(player);
        }
    }

    public class end extends RunnableImpl {

        @Override
        public void runImpl() {
            openArenaDoors();
            scheduleRunnable(new ressurectPlayers(), 1000);
            scheduleRunnable(new healPlayers(), 2000);
            scheduleRunnable(new teleportPlayersToSavedCoords(), 3000);
            scheduleRunnable(new autoContinue(), 5000);
        }
    }

    public class teleportPlayersToSavedCoords extends RunnableImpl {

        @Override
        public void runImpl() {
            teleportToSavedCoords(teamRed.getAllPlayers());
            teleportToSavedCoords(teamBlue.getAllPlayers());
        }
    }

    public class saveBackCoords extends RunnableImpl {
        @Override
        public void runImpl() {
            saveBackCoords(teamRed.getAllPlayers());
            saveBackCoords(teamBlue.getAllPlayers());
        }
    }

    public class ressurectPlayers extends RunnableImpl {
        @Override
        public void runImpl() {
            teamRed.ressurectPlayers();
            teamBlue.ressurectPlayers();
        }
    }

    public class healPlayers extends RunnableImpl {
        @Override
        public void runImpl() {
            teamRed.healPlayers();
            teamBlue.healPlayers();
        }
    }

    public class checkLive extends RunnableImpl {

        @Override
        public void runImpl() {
            teamRed.updateLivePlayers();
            teamBlue.updateLivePlayers();

            teamRed.getLivePlayers().forEach(player -> {
                if (player.isConnected() && !player.isLogoutStarted()) {
                    player.setTeam(TeamType.RED);
                } else {
                    loosePlayer(player, false);
                }
            });

            teamBlue.getLivePlayers().forEach(player -> {
                if (player.isConnected() && !player.isLogoutStarted()) {
                    player.setTeam(TeamType.BLUE);
                } else {
                    loosePlayer(player, false);
                }
            });

            if (teamRed.getLivePlayersCount() < 1 || teamBlue.getLivePlayersCount() < 1) {
                ThreadPoolManager.getInstance().execute(new endBattle());
            }
        }
    }

    public class getNewFighters extends RunnableImpl {

        @Override
        public void runImpl() {
            if (teamBlue.getLivePlayersCount() >= 1 && teamRed.getLivePlayersCount() >= 1) {

                teamBlue.getLivePlayers().stream().filter(bluePlayer -> !bluePlayer.isDead() && bluePlayer != _blueTeamFighter).findFirst().ifPresent(player1 -> {
                    _blueTeamFighter = player1;
                    prepareFighter(_blueTeamFighter);
                });
                teamRed.getLivePlayers().stream().filter(bluePlayer -> !bluePlayer.isDead() && bluePlayer != _blueTeamFighter).findFirst().ifPresent(player1 -> {
                    _redTeamFighter = player1;
                    prepareFighter(_redTeamFighter);
                });
                final String param = _redTeamFighter.getName() + " VS " + _blueTeamFighter.getName();
                sendEventMessage(teamBlue, MessageType.TEXT, param);
                sendEventMessage(teamRed, MessageType.TEXT, param);
            }
        }
    }

    public class teleportPlayersToArena extends RunnableImpl {

        @Override
        public void runImpl() {
            teleportTeamPlayersToArenaLocations(teamBlue.getAllPlayers(), teamBlueLoc1, teamBlueLoc2);
            teleportTeamPlayersToArenaLocations(teamRed.getAllPlayers(), teamRedLoc1, teamRedLoc2);
        }
    }

    public class question extends RunnableImpl {
        @Override
        public void runImpl() {
            GameObjectsStorage.getPlayers().stream().filter(player -> player != null && !player.isDead() && player.getLevel() >= _minLevel && player.getLevel() <= _maxLevel && player.getReflection().isDefault() && !player.isOlyParticipant() && !player.isInObserverMode() && !player.isOnSiegeField()).forEach(player -> {
                ConfirmDlg packet = new ConfirmDlg(SystemMsg.S1, 60000).addString(new CustomMessage("scripts.events.KoreanStyle.AskPlayer", player).toString());
                player.ask(packet, new AnswerListener(player, 60000));
            });
        }
    }

    private class AnswerListener implements OnAnswerListener {
        private final long _expireTime;
        private final HardReference<Player> _playerRef1;

        private AnswerListener(final Player player1, final long expireTime) {
            _playerRef1 = player1.getRef();
            _expireTime = expireTime > 0L ? System.currentTimeMillis() + expireTime : 0L;
        }

        @Override
        public void sayYes() {
            Player player1;
            if ((player1 = _playerRef1.get()) == null || Config.EVENT_KOREAN_STYLE_CHECK_HWID && !hwid_check.canParticipate(player1)) {
                return;
            }
            player1.sendMessage(new CustomMessage("scripts.events.KoreanStyle.AnswerYes", player1));
            addPlayer(player1, lastNpc, args);
        }

        @Override
        public void sayNo() {
            Player player1;
            if ((player1 = _playerRef1.get()) == null) {
                return;
            }
            player1.sendMessage(new CustomMessage("scripts.events.KoreanStyle.AnswerNo", player1));
        }

        @Override
        public long expireTime() {
            return _expireTime;
        }
    }

    public class announce extends RunnableImpl {

        @Override
        public void runImpl() {
            // Ивент отменён так как не участников ;(
            if (_time_to_start <= 1 && (teamRed.isEmpty() || teamBlue.isEmpty())) {
                sayToAll("scripts.events.KoreanStyle.AnnounceEventCancelled");
                _isRegistrationActive = false;
                _status = 0;
                scheduleRunnable(new autoContinue(), 5000);
                return;
            }

            if (_time_to_start > 1) {
                _time_to_start--;
                sayToAll("scripts.events.KoreanStyle.AnnouncePreStart", String.valueOf(_time_to_start), String.valueOf(_minLevel), String.valueOf(_maxLevel));
                scheduleRunnable(new announce(), 60000);
            } else {
                _status = 1;
                _isRegistrationActive = false;
                sayToAll("scripts.events.KoreanStyle.AnnounceEventStarting");
                scheduleRunnable(new prepare(), 5000);
            }

        }
    }

    public class go extends RunnableImpl {

        @Override
        public void runImpl() {
            arena.getZones().keySet().forEach(s -> _reflection.getZone(s).setType(ZoneType.battle_zone));
            _status = 2;
            ThreadPoolManager.getInstance().execute(new checkLive());
            ThreadPoolManager.getInstance().execute(new getNewFighters());
            showPointsToScreen(false);
            clearArena();
            sayToAll("scripts.events.KoreanStyle.AnnounceFight");
            if (Config.EVENT_KOREAN_STYLE_BUFFS_ONSTART) {
                buffPlayers();
            }

            _endTask = scheduleRunnable(new endBattle(), Config.EVENT_KOREAN_STYLE_TIME_FOR_FIGHT * 60 * 1000);

        }
    }

    public class autoContinue extends RunnableImpl {

        @Override
        public void runImpl() {
            teamBlue.clear();
            teamRed.clear();
            hwid_check.clear();
            ip_check.clear();
            // если больше, то стартуем автоматом следующие уровни
            if (_autoContinue > 0) {
                if (_autoContinue >= 6) {
                    _autoContinue = 0;
                    return;
                }
                start(player, lastNpc, new String[]{
                        String.valueOf(_autoContinue + 1),
                        String.valueOf(_autoContinue + 1)
                });
            } else if (isActive()) {
                // пробуем зашедулить по времени из конфигов
                scheduleEventStart();
            }
        }
    }

    private class OnTeleportImpl implements OnTeleportListener {
        @Override
        public void onTeleport(Player player, int x, int y, int z, Reflection reflection) {
            if (arena.getZones().keySet().stream().map(_reflection::getZone).anyMatch(_zone -> _zone.checkIfInZone(x, y, z, reflection))) {
                return;
            }

            onEscape(player);
        }
    }
}

