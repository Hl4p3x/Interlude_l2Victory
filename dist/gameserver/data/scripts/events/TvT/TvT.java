package events.TvT;

import com.stringer.annotations.HideAccess;
import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.data.xml.holder.TvTArenaHolder;
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
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import ru.j2dev.gameserver.templates.arenas.TvTArena;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Created by JunkyFunky
 * on 05.05.2018 16:32
 * group j2dev
 */
@HideAccess
public class TvT extends PvpEvent {
    private final HwidChecker hwid_check = new HwidChecker(Config.EVENT_TVT_CHECK_HWID);
    private final IpChecker ip_check = new IpChecker(Config.EVENT_TVT_CHECK_IP);
    private final AttackMagicListenerImpl _attackListener = new AttackMagicListenerImpl(Config.EVENT_TVT_DISSALOWED_SKILL, true);
    private final OnTeleportImpl onTeleportListener = new OnTeleportImpl();
    private List<Location> team1loc = new ArrayList<>();
    private List<Location> team2loc = new ArrayList<>();
    private PvpEventTeam teamBlue = new PvpEventTeam(BLUE_TEAM, "TVT_BLUE");
    private PvpEventTeam teamRed = new PvpEventTeam(RED_TEAM, "TVT_RED");
    private final ZoneListener zoneListener = new ZoneListener(teamBlue, teamRed);
    private List<TvTArena> arenas;
    private TvTArena arena;

    private List<Integer> getFighterBuffs() {
        return Config.EVENT_TVT_BUFFS_FIGHTER;
    }

    private List<Integer> getMageBuffs() {
        return Config.EVENT_TVT_BUFFS_MAGE;
    }

    private boolean checkCountTeam(final int team) {
        if (team == 1 && teamRed.getPlayersCount() < Config.EVENT_TVT_MAX_LENGTH_TEAM) {
            return true;
        } else return team == 2 && teamBlue.getPlayersCount() < Config.EVENT_TVT_MAX_LENGTH_TEAM;
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
            player.sendMessage(new CustomMessage("scripts.events.TvT.Cancelled", player));
            return false;
        }

        if (player.getLevel() < _minLevel || player.getLevel() > _maxLevel) {
            player.sendMessage(new CustomMessage("scripts.events.TvT.CancelledLevel", player));
            return false;
        }

        if (player.isMounted()) {
            player.sendMessage(new CustomMessage("scripts.events.TvT.Cancelled", player));
            return false;
        }

        if (player.isInDuel()) {
            player.sendMessage(new CustomMessage("scripts.events.TvT.CancelledDuel", player));
            return false;
        }

        if (first && (player.getTeam() != TeamType.NONE)) {// || player.isRegistredEvent())) { todo[j]
            player.sendMessage(new CustomMessage("scripts.events.TvT.CancelledOtherEvent", player));
            return false;
        }


        if (player.isOlyParticipant() || first && OlympiadPlayersManager.getInstance().isRegistred(player)) {
            player.sendMessage(new CustomMessage("scripts.events.TvT.CancelledOlympiad", player));
            return false;
        }

        if (player.isInParty() && player.getParty().isInDimensionalRift()) {
            player.sendMessage(new CustomMessage("scripts.events.TvT.CancelledOtherEvent", player));
            return false;
        }

        if (player.isTeleporting()) {
            player.sendMessage(new CustomMessage("scripts.events.TvT.CancelledTeleport", player));
            return false;
        }

        if (player.isCursedWeaponEquipped()) {
            player.sendMessage(new CustomMessage("scripts.events.TvT.Cancelled", player));
            return false;
        }

        // последним проверяем HardwareID
        if (first && !hwid_check.canParticipate(player)) {
            player.sendMessage(new CustomMessage("scripts.events.TvT.Cancelled", player));
            return false;
        }

        if (first && !ip_check.canParticipate(player)) {
            player.sendMessage(new CustomMessage("scripts.events.TvT.Cancelled", player));
            return false;
        }

        return true;
    }

    private void removePlayer(final Player player) {
        if (player != null) {
            teamRed.removePlayer(player);
            teamBlue.removePlayer(player);
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

    private void openArenaDoors() {
        _reflection.getDoors().forEach(DoorInstance::openMe);
    }

    private void closeArenaDoors() {
        _reflection.getDoors().forEach(DoorInstance::closeMe);
    }

    private void giveItemsToWinner(final PvpEventTeam team, final double rate) {
        team.getAllPlayers()
                .stream()
                .filter(Objects::nonNull)
                .forEach(player -> ItemFunctions.addItem(player, Config.EVENT_TVT_ITEM_ID, Math.round((Config.EVENT_TVT_RATE ? player.getLevel() : 1) * Config.EVENT_TVT_ITEM_COUNT * rate)));
    }

    private void upParalyzePlayers() {
        teamRed.upParalyzePlayers();
        teamBlue.upParalyzePlayers();
    }

    private void cleanPlayers() {
        teamRed.getAllPlayers().stream().filter(player -> !checkPlayer(player, false)).forEach(this::removePlayer);
        teamBlue.getAllPlayers().stream().filter(player -> !checkPlayer(player, false)).forEach(this::removePlayer);
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
                LOGGER.warn("Huligan on TvT : {}", player);
                player.teleToClosestTown();
            }
        }));
    }

    @Override
    public void onEscape(final Player player) {
        if (_status > 1 && player != null && player.getTeam() != TeamType.NONE && (teamRed.isLive(player.getObjectId()) || teamBlue.isLive(player.getObjectId()))) {
            removePlayer(player);
            ThreadPoolManager.getInstance().execute(new checkLive());
        }
    }

    private void loosePlayer(final Player player, final boolean inc) {
        if (player != null) {
            if (inc && player.getTeam() != TeamType.NONE) {
                player.sendMessage(new CustomMessage("scripts.events.Revive", player).addNumber(Config.EVENT_TVT_REVIVE_DELAY));
                scheduleRunnable(new resurrectAtBase(player), Config.EVENT_TVT_REVIVE_DELAY * 1000);
                showScore(player);
            } else {
                teamRed.removeFromLive(player);
                teamBlue.removeFromLive(player);
                player.setTeam(TeamType.NONE);
                player.sendMessage(new CustomMessage("scripts.events.TvT.YouLose", player));
            }
        }
    }

    @Bypass("events.TvT:activateEvent")
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

    @Bypass("events.TvT:deactivateEvent")
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

    @Bypass("events.TvT:manualEnd")
    public void manualEnd(Player player, NpcInstance lastNpc, String[] args) {
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (_status > 1) {
            ThreadPoolManager.getInstance().execute(new endBattle());
        }
    }

    private boolean isRunned() {
        return _isRegistrationActive || _status > 0;
    }

    /**
     * 0 - категория уровней<br>
     * 1 - если больше 0 то автоматически продолжается
     */
    @Override
    @Bypass("events.TvT:start")
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
        _reflection.startCollapseTimer(Config.EVENT_TVT_TIME_FOR_FIGHT * 2 * 60 * 1000);
        arena.getZones().keySet().forEach(s -> {
            _reflection.getZone(s).setType(ZoneType.peace_zone);
            _reflection.getZone(s).addListener(zoneListener);
        });
        team1loc = arena.getTeleportLocations(1);
        team2loc = arena.getTeleportLocations(2);
        _time_to_start = Config.EVENT_TVT_TIME_TO_START;
        sayToAll("scripts.events.TvT.AnnouncePreStart", String.valueOf(_time_to_start), String.valueOf(_minLevel), String.valueOf(_maxLevel));

        if(isQuestionAllowed()) {
            scheduleRunnable(new question(), 10000);
        }
        scheduleRunnable(new announce(), 60000);
        //logEventStart(category, _autoContinue);
        onStart(this);
    }

    @Bypass("events.TvT:addPlayer")
    public void addPlayer(Player player, NpcInstance lastNpc, String[] args) {
        if (player == null) {
            return;
        }

        if (!checkPlayer(player, true)) {
            removePlayer(player);
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
                player.sendMessage(new CustomMessage("scripts.events.TvT.Registered", player));
                break;
            case 2:
                teamBlue.addPlayer(player);
                player.sendMessage(new CustomMessage("scripts.events.TvT.Registered", player));
                break;
            default:
                LOGGER.info("WTF??? Command id 0 in TvT...");
                break;
        }
    }

    @Bypass("events.TvT:removePlayerCb")
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
            for (Config.EventInterval config : Config.EVENT_TVT_INTERVAL) {

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

    @Override
    public PvpEventType getEventType() {
        return PvpEventType.TeamVsTeam;
    }

    @Override
    public boolean isAllowClanSkill() {
        return Config.EVENT_TVT_ALLOW_CLAN_SKILL;
    }

    @Override
    public boolean isAllowHeroSkill() {
        return Config.EVENT_TVT_ALLOW_HERO_SKILL;
    }

    @Override
    public boolean isAllowBuffs() {
        return Config.EVENT_TVT_ALLOW_BUFFS;
    }

    @Override
    public boolean isDispelTransformation() {
        return Config.EVENT_TVT_DISPEL_TRANSFORMATION;
    }

    @Override
    public boolean isAllowSummons() {
        return Config.EVENT_TVT_ALLOW_SUMMONS;
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
        return Config.EVENT_TVT_ALLOW_EQUIP;
    }

    @Override
    public boolean isQuestionAllowed() {
        return Config.EVENT_TVT_ALLOW_QUESTION;
    }

    @Override
    public int getBuffTime() {
        return Config.EVENT_TVT_TIME_FOR_FIGHT;
    }

    @Override
    public void onInit() {
        CharListenerList.addGlobal(new OnDeathListenerImpl());
        CharListenerList.addGlobal(new OnPlayerExitListenerImpl());
        arenas = TvTArenaHolder.getInstance().getArenas();

        // Если ивент активен, но пробуем зашедулить
        if (isActive()) {
            scheduleEventStart();
        }

        LOGGER.info("Loaded Event: " + getEventType() + '[' + isActive() + ']' + " loaded arenas :" + arenas.size());
    }

    private void saveBackCoords(final List<Player> players) {
        players.forEach(player -> savePlayerBackCoord(player, "TvTBack"));
    }

    private void teleportToSavedCoords(final List<Player> players) {
        players.forEach(player -> {
            player.setIsInTvT(false);
            unlockItems(player, Config.EVENT_TVT_DISSALOWED_ITEMS);
            player.removeListener(_attackListener);
            player.removeListener(onTeleportListener);
            teleportPlayerToBack(player, true, Config.EVENT_TVT_BUFFS_ONSTART, "TvTBack");
        });
    }

    private ExShowScreenMessage getTeamPointsPacket(PvpEventTeam team) {
        return new ExShowScreenMessage("Kills: " + team.getTeamPoints(), 3000, ScreenMessageAlign.MIDDLE_RIGHT, true);
    }

    private void teleportPvpTeamToLocations(final PvpEventTeam team, final List<Location> locations) {
        team.getAllPlayers().forEach(player -> {
            preparePlayer(player);
            player.teleToLocation(Location.findPointToStay(Rnd.get(locations), 150, 500, _reflection.getGeoIndex()), _reflection);
            player.addListener(_attackListener);
            player.addListener(onTeleportListener);
            player.setIsInTvT(true);
            lockItems(player, Config.EVENT_TVT_DISSALOWED_ITEMS);
        });
    }

    /**
     * Задача наложения баффов на игроков
     */
    private void buffPlayers() {
        teamBlue.getAllPlayers().forEach(this::addBuffToPlayer);
        teamRed.getAllPlayers().forEach(this::addBuffToPlayer);
    }

    /**
     * Отправляет сообщение игрокам о количестве очков
     */
    private void showScore(final Player player) {
        if (player == null || player.getTeam() == TeamType.NONE) {
            return;
        }

        if (player.getTeam() == TeamType.BLUE) {
            teamRed.sendPacketToLive(getTeamPointsPacket(teamRed));
            // если умер 'красный' отправляем сообщения синим с количеством очков
        } else if (player.getTeam() == TeamType.RED) {
            teamBlue.sendPacketToLive(getTeamPointsPacket(teamBlue));
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

    public class end extends RunnableImpl {

        @Override
        public void runImpl() {
            openArenaDoors();

            scheduleRunnable(new ressurectPlayers(), 100);
            scheduleRunnable(new healPlayers(), 200);
            scheduleRunnable(new teleportPlayersToSavedCoords(), 300);
            scheduleRunnable(new autoContinue(), 400);

        }
    }

    public class ressurectPlayers extends RunnableImpl {

        @Override
        public void runImpl() {
            teamRed.ressurectPlayers();
            teamBlue.ressurectPlayers();
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

    private class OnDeathListenerImpl implements OnDeathListener {

        @Override
        public void onDeath(final Creature victim, final Creature killer) {
            if (_status > 1 && victim != null && victim.isPlayer() && (teamRed.isLive(victim.getObjectId()) || teamBlue.isLive(victim.getObjectId()))) {

                if (Config.EVENT_TVT_ALLOW_KILL_BONUS) {
                    ItemFunctions.addItem(killer.getPlayer(), Config.EVENT_TVT_KILL_BONUS_ID, Config.EVENT_TVT_KILL_BONUS_COUNT);
                }
                if (Config.EVENT_TVT_ALLOW_PVP_COUNT_BONUS) {
                    killer.getPlayer().setPvpKills(killer.getPlayer().getPvpKills() + 1);
                }
                if (Config.EVENT_TVT_ALLOW_PVP_DECREASE_ONDIE) {
                    int pvpkills = victim.getPlayer().getPvpKills() - 1;
                    if (pvpkills < 1) {
                        pvpkills = 0;
                    }
                    victim.getPlayer().setPvpKills(pvpkills);
                }
                // если жертва находиться в одной из команд увеличиваем счётчик смертей игроку
                if (victim.getTeam() == TeamType.RED) {
                    teamRed.getPlayerInfo(victim.getObjectId()).incrementDeaths();
                    // Добавляем очки противоположной команде
                    teamBlue.increaseTeamPoints();
                } else {
                    teamBlue.getPlayerInfo(victim.getObjectId()).incrementDeaths();
                    // Добавляем очки противоположной команде
                    teamRed.increaseTeamPoints();
                }
                // увеличиваем счётчик убийств
                if (killer.getPlayer() != null) { // вдруг суммоном убил
                    final Player player = killer.getPlayer();
                    // если убийца находиться в одной из команд
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

                loosePlayer(victim.getPlayer(), true);
                ThreadPoolManager.getInstance().execute(new checkLive());
            }
        }
    }

    public class teleportPlayersToArena extends RunnableImpl {

        @Override
        public void runImpl() {
            teleportPvpTeamToLocations(teamBlue, team1loc);
            teleportPvpTeamToLocations(teamRed, team2loc);
        }
    }

    private class OnPlayerExitListenerImpl implements OnPlayerExitListener {

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
                player.teleToClosestTown();
                return;
            }

            // Вышел или вылетел во время эвента
            onEscape(player);
        }
    }

    public class question extends RunnableImpl {

        @Override
        public void runImpl() {
            GameObjectsStorage.getPlayers().stream().filter(player -> player != null && !player.isDead() && player.getLevel() >= _minLevel && player.getLevel() <= _maxLevel && player.getReflection().isDefault() && !player.isOlyParticipant() && !player.isInObserverMode() && !player.isOnSiegeField()).forEach(player -> {
                ConfirmDlg packet = new ConfirmDlg(SystemMsg.S1, 60000).addString(new CustomMessage("scripts.events.TvT.AskPlayer", player).toString());
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
            if ((player1 = _playerRef1.get()) == null || Config.EVENT_TVT_CHECK_HWID && !hwid_check.canParticipate(player1)) {
                return;
            }
            player1.sendMessage(new CustomMessage("scripts.events.TvT.AnswerYes", player));
            addPlayer(player1, lastNpc, args);
        }

        @Override
        public void sayNo() {
            Player player1;
            if ((player1 = _playerRef1.get()) == null) {
                return;
            }
            player1.sendMessage(new CustomMessage("scripts.events.TvT.AnswerNo", player));
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
                sayToAll("scripts.events.TvT.AnnounceEventCancelled");
                _isRegistrationActive = false;
                _status = 0;
                scheduleRunnable(new autoContinue(), 5000);
                return;
            }

            if (_time_to_start > 1) {
                _time_to_start--;
                sayToAll("scripts.events.TvT.AnnouncePreStart", String.valueOf(_time_to_start), String.valueOf(_minLevel), String.valueOf(_maxLevel));
                scheduleRunnable(new announce(), 60000);
            } else {
                _status = 1;
                _isRegistrationActive = false;
                sayToAll("scripts.events.TvT.AnnounceEventStarting");
                scheduleRunnable(new prepare(), 5000);
            }
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

            sayToAll("scripts.events.TvT.AnnounceFinalCountdown");
        }
    }

    public class endBattle extends RunnableImpl {

        @Override
        public void runImpl() {
            arena.getZones().keySet().forEach(s -> {
                _reflection.getZone(s).setType(ZoneType.peace_zone);
                _reflection.getZone(s).removeListener(zoneListener);
            });
            _status = 0;
            removeAura();

            final int blue_score = teamBlue.getTeamPoints();
            final int red_score = teamRed.getTeamPoints();
            final boolean drawWinner = blue_score == red_score;
            if (blue_score > red_score) {
                sayToAll("scripts.events.TvT.AnnounceFinishedBlueWins");
                giveItemsToWinner(teamBlue, 1);
            } else if (red_score > blue_score) {
                sayToAll("scripts.events.TvT.AnnounceFinishedRedWins");
                giveItemsToWinner(teamRed, 1);
            } else if (drawWinner && blue_score > 0) {
                sayToAll("scripts.events.TvT.AnnounceFinishedDraw"); // если убийства есть)
                giveItemsToWinner(teamRed, 0.5);
            } else if (drawWinner && blue_score == 0) {
                sayToAll("scripts.events.TvT.AnnounceFinishedDraw"); // если вдруг никто никого не убил
            }
            // отправляем всем статискику
            teamBlue.sendPacketToAll(getFinalStatistics(teamBlue, "Team vs Team Blue Team TOP"));
            teamRed.sendPacketToAll(getFinalStatistics(teamRed, "Team vs Team Red Team TOP"));

            sayToAll("scripts.events.TvT.AnnounceScore", String.valueOf(red_score), String.valueOf(blue_score));

            sayToAll("scripts.events.TvT.AnnounceEnd");
            scheduleRunnable(new end(), 30000);

            _isRegistrationActive = false;
            if (_endTask != null) {
                _endTask.cancel(false);
                _endTask = null;
            }
        }
    }

    public class saveBackCoords extends RunnableImpl {

        @Override
        public void runImpl() {
            saveBackCoords(teamRed.getAllPlayers());
            saveBackCoords(teamBlue.getAllPlayers());
        }
    }

    public class teleportPlayersToSavedCoords extends RunnableImpl {

        @Override
        public void runImpl() {
            teleportToSavedCoords(teamRed.getAllPlayers());
            teleportToSavedCoords(teamBlue.getAllPlayers());
        }
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

    /**
     * Задача восстановления фулл хп\мп\цп у игроков
     */
    public class healPlayers extends RunnableImpl {

        @Override
        public void runImpl() {
            teamRed.healPlayers();
            teamBlue.healPlayers();
        }
    }

    /**
     * Задача воскрешения игрока, вызывает наложение баффа и фулл хил
     * телепортирует в рандомную точку для команды
     */
    public class resurrectAtBase extends RunnableImpl {
        Player player;

        private resurrectAtBase(final Player plr) {
            player = plr;
        }

        @Override
        public void runImpl() {
            if (player.getTeam() == TeamType.NONE) {
                return;
            }
            revivePlayer(player);
            if (Config.EVENT_TVT_BUFFS_ONSTART) {
                addBuffToPlayer(player);
                healPlayer(player);
            }
            final Location pos;
            if (player.getTeam() == TeamType.BLUE) {
                pos = Rnd.get(team1loc);
            } else {
                pos = Rnd.get(team2loc);
            }
            player.teleToLocation(pos.x, pos.y, pos.z, _reflection);
        }
    }

    public class go extends RunnableImpl {

        @Override
        public void runImpl() {
            arena.getZones().keySet().stream().map(_reflection::getZone).forEach(_zone -> _zone.setType(ZoneType.battle_zone));
            _status = 2;
            upParalyzePlayers();
            ThreadPoolManager.getInstance().execute(new checkLive());
            clearArena();
            sayToAll("scripts.events.TvT.AnnounceFight");
            if (Config.EVENT_TVT_BUFFS_ONSTART) {
                buffPlayers();
                scheduleRunnable(new healPlayers(), 1000);
            }

            _endTask = scheduleRunnable(new endBattle(), TimeUnit.MINUTES.toMillis(Config.EVENT_TVT_TIME_FOR_FIGHT));
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
}
