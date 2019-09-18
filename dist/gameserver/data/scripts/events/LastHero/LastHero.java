package events.LastHero;

import com.stringer.annotations.HideAccess;
import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.data.xml.holder.LastHeroArenaHolder;
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
import ru.j2dev.gameserver.model.entity.olympiad.HeroManager;
import ru.j2dev.gameserver.model.entity.olympiad.OlympiadPlayersManager;
import ru.j2dev.gameserver.model.event.*;
import ru.j2dev.gameserver.model.instances.DoorInstance;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ConfirmDlg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExEventMatchMessage.MessageType;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SocialAction;
import ru.j2dev.gameserver.templates.arenas.LastHeroArena;
import ru.j2dev.gameserver.utils.ItemFunctions;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

/**
 * Created by JunkyFunky
 * on 06.05.2018 11:42
 * group j2dev
 */
@HideAccess
public class LastHero extends PvpEvent {
    private final HwidChecker hwid_check = new HwidChecker(Config.EVENT_LASTHERO_CHECK_HWID);
    private final IpChecker ip_check = new IpChecker(Config.EVENT_LASTHERO_CHECK_IP);
    private final ZoneListener _zoneListener = new ZoneListener();
    private final AttackMagicListenerImpl _attackListener = new AttackMagicListenerImpl(Config.EVENT_LASTHERO_DISSALOWED_SKILL);
    private final OnTeleportImpl onTeleportListener = new OnTeleportImpl();
    private Reflection _reflection;
    private ScheduledFuture<?> _startTask;
    private ScheduledFuture<?> _endTask;
    private PvpEventTeam participants = new PvpEventTeam(RED_TEAM, "LastHero");
    private final ZoneListener zoneListener = new ZoneListener(participants);
    private boolean _isRegistrationActive;
    private int _status;
    private int _time_to_start;
    private int _minLevel;
    private int _maxLevel;
    private int _autoContinue;
    private List<LastHeroArena> arenas;
    private LastHeroArena arena;

    private static List<Integer> getFighterBuffs() {
        return Config.EVENT_LASTHERO_BUFFS_FIGHTER;
    }

    private static List<Integer> getMageBuffs() {
        return Config.EVENT_LASTHERO_BUFFS_MAGE;
    }

    public boolean checkPlayer(final Player player, final boolean first) {
        if (first && !_isRegistrationActive) {
            player.sendMessage(new CustomMessage("scripts.events.Late", player));
            return false;
        }

        if (first && player.isDead()) {
            return false;
        }

        if (first && participants.isInTeam(player)) {
            player.sendMessage(new CustomMessage("scripts.events.LastHero.Cancelled", player));
            return false;
        }

        if (player.getLevel() < _minLevel || player.getLevel() > _maxLevel) {
            player.sendMessage(new CustomMessage("scripts.events.LastHero.CancelledLevel", player));
            return false;
        }

        if (player.isInDuel()) {
            player.sendMessage(new CustomMessage("scripts.events.LastHero.CancelledDuel", player));
            return false;
        }

        if (first && (player.getTeam() != TeamType.NONE)) {
            player.sendMessage(new CustomMessage("scripts.events.LastHero.CancelledOtherEvent", player));
            return false;
        }

        if (player.isOlyParticipant() || first && OlympiadPlayersManager.getInstance().isRegistred(player)) {
            player.sendMessage(new CustomMessage("scripts.events.LastHero.CancelledOlympiad", player));
            return false;
        }

        if (player.isInParty() && player.getParty().isInDimensionalRift()) {
            player.sendMessage(new CustomMessage("scripts.events.LastHero.CancelledOtherEvent", player));
            return false;
        }

        if (player.isTeleporting()) {
            player.sendMessage(new CustomMessage("scripts.events.LastHero.CancelledTeleport", player));
            return false;
        }

        if (player.isCursedWeaponEquipped()) {
            player.sendMessage(new CustomMessage("scripts.events.LastHero.Cancelled", player));
            return false;
        }

        // последним проверяем HardwareID
        if (first && !hwid_check.canParticipate(player)) {
            player.sendMessage(new CustomMessage("scripts.events.LastHero.Cancelled", player));
            return false;
        }
        if (first && !ip_check.canParticipate(player)) {
            player.sendMessage(new CustomMessage("scripts.events.LastHero.Cancelled", player));
            return false;
        }

        return true;
    }

    private void saveBackCoords(final List<Player> players) {
        players.forEach(player -> savePlayerBackCoord(player, "LastHeroBack"));
    }

    private void buffPlayers() {
        participants.getAllPlayers().forEach(this::addBuffToPlayer);
    }

    private void teleportToSavedCoords(final List<Player> players) {
        players.forEach(player -> {
            player.setIsInLastHero(false);
            unlockItems(player, Config.EVENT_LASTHERO_DISSALOWED_ITEMS);
            player.removeListener(_attackListener);
            player.removeListener(onTeleportListener);
            teleportPlayerToBack(player, true, Config.EVENT_TVT_BUFFS_ONSTART, "LastHeroBack");
        });
    }

    private void upParalyzePlayers() {
        participants.upParalyzePlayers();
    }

    private void cleanPlayers() {
        participants.getAllPlayers().stream().filter(player -> !checkPlayer(player, false)).forEach(this::removePlayer);
    }

    private void checkLive() {
        participants.updateLivePlayers();
        participants.getLivePlayers().forEach(player -> {
            if (!player.isDead() && player.isConnected() && !player.isLogoutStarted()) {
                player.setTeam(TeamType.RED);
            } else {
                loosePlayer(player);
            }
        });

        if (participants.getLivePlayersCount() <= 1) {
            ThreadPoolManager.getInstance().execute(new endBattle());
        }
    }

    private void loosePlayer(final Player player) {
        if (player != null) {
            participants.removeFromLive(player);
            player.setTeam(TeamType.NONE);
            if (player.isDead()) {
                player.doDie(player);
            }
            player.sendMessage(new CustomMessage("scripts.events.lasthero.YouLose", player));
        }
    }

    private void removeAura() {
        participants.removeAura();
    }

    /**
     * чистим арену от чужих)
     */
    private void clearArena() {
        arena.getZones().keySet().stream().map(_reflection::getZone).forEach(_zone -> _zone.getInsidePlayables().stream().filter(obj -> obj.getReflection() == _reflection).forEach(obj -> {
            final Player player = obj.getPlayer();
            if (player != null && !participants.isLive(player)) {
                player.sendMessage(player.isLangRus() ? "Вы не можете находиться в зоне проведения ПвП Ивента!" : "You not allow primission on enter PvP Event Zone now!");
                LOGGER.warn("Huligan on LastHero : {}", player);
                player.teleToClosestTown();
            }
        }));
    }

    @Override
    public void onEscape(final Player player) {
        if (_status > 1 && player != null && player.getTeam() != TeamType.NONE && participants.isLive(player)) {
            removePlayer(player);
            checkLive();
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

    private void removePlayer(final Player player) {
        participants.removePlayer(player);
        player.removeListener(_attackListener);
        player.getInventory().unlock();
    }

    private void openArenaDoors() {
        _reflection.getDoors().forEach(DoorInstance::openMe);
    }

    private void closeArenaDoors() {
        _reflection.getDoors().forEach(DoorInstance::closeMe);
    }

    @Override
    public void onInit() {
        arenas = LastHeroArenaHolder.getInstance().getArenas();
        CharListenerList.addGlobal(new OnDeathListenerImpl());
        CharListenerList.addGlobal(new OnPlayerExitListenerImpl());
        // Если ивент активен, но пробуем зашедулить
        if (isActive()) {
            scheduleEventStart();
        }

        LOGGER.info("Loaded Event: " + getEventType() + '[' + isActive() + ']' + " loaded arenas :" + arenas.size());
    }

    @Bypass("events.LastHero:activateEvent")
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

    @Bypass("events.LastHero:deactivateEvent")
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

    @Bypass("events.LastHero:manualEnd")
    public void manualEnd(Player player, NpcInstance lastNpc, String[] args) {
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (_status > 1) {
            ThreadPoolManager.getInstance().execute(new endBattle());
        }
    }

    public boolean isRunned() {
        return _isRegistrationActive || _status > 0;
    }

    /**
     * 0 - категория уровней<br>
     * 1 - если больше 0 то автоматически продолжается
     */
    @Override
    @Bypass("events.LastHero:start")
    public void start(Player player, NpcInstance lastNpc, String[] args) {
        if (isRunned()) {
            return;
        }

        if (args.length != 2) {
            player.sendMessage(new CustomMessage("common.Error", player));
            return;
        }
        if (player != null && !player.getPlayerAccess().IsEventGm) {
            return;
        }

        final int category;
        final int autoContinue;
        try {
            category = Integer.parseInt(args[0]);
            autoContinue = Integer.parseInt(args[1]);
        } catch (final Exception e) {
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
        _time_to_start = Config.EVENT_LASTHERO_TIME_TO_START;

        arenas = LastHeroArenaHolder.getInstance().getArenas();
        arena = Rnd.get(arenas);
        LOGGER.debug(getEventType() + " Event Start Debug : " + " arena id " + arena.getId() + " zones array " + arena.getZones().keySet());
        _reflection = new Reflection(getEventType().getRefId(), true);
        arena.initDoors();
        _reflection.init(arena.getArenaDoors(), arena.getZones());
        _reflection.startCollapseTimer(Config.EVENT_LASTHERO_TIME_FOR_FIGHT * 2 * 60 * 1000);
        arena.getZones().keySet().forEach(s -> {
            _reflection.getZone(s).setType(ZoneType.peace_zone);
            _reflection.getZone(s).addListener(zoneListener);
        });

        sayToAll("scripts.events.LastHero.AnnouncePreStart", String.valueOf(_time_to_start), String.valueOf(_minLevel), String.valueOf(_maxLevel));

        if(isQuestionAllowed()) {
            scheduleRunnable(new question(), 10000);
        }
        scheduleRunnable(new announce(), 60000);
        //logEventStart(category, _autoContinue);
        onStart(this);
    }

    @Bypass("events.LastHero:addPlayer")
    public void addPlayer(Player player, NpcInstance lastNpc, String[] args) {
        if (player == null || !checkPlayer(player, true)) {
            return;
        }
        if (!_isRegistrationActive) {
            sendNoActiveRegister(player);
            return;
        }
        if(isInTeam(player, participants)) {
            sendAlreadyRegistred(player);
            return;
        }
        participants.addPlayer(player);

        player.sendMessage(new CustomMessage("scripts.events.LastHero.Registered", player));
    }

    @Bypass("events.LastHero:removePlayerCb")
    public void removePlayerCb(Player player, NpcInstance lastNpc, String[] args) {
        if (player == null) {
            return;
        }
        if (checkRegisterCondition(player, participants)) {
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
            for (Config.EventInterval config : Config.EVENT_LASTHERO_INTERVAL) {

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
        return PvpEventType.LastHero;
    }

    @Override
    public boolean isAllowClanSkill() {
        return Config.EVENT_LASTHERO_ALLOW_CLAN_SKILL;
    }

    @Override
    public boolean isAllowHeroSkill() {
        return Config.EVENT_LASTHERO_ALLOW_HERO_SKILL;
    }

    @Override
    public boolean isAllowBuffs() {
        return Config.EVENT_LASTHERO_ALLOW_BUFFS;
    }

    @Override
    public boolean isDispelTransformation() {
        return Config.EVENT_LASTHERO_DISPEL_TRANSFORMATION;
    }

    @Override
    public boolean isAllowSummons() {
        return Config.EVENT_LASTHERO_ALLOW_SUMMONS;
    }

    @Override
    public boolean isPartyDisable() {
        return Config.EVENT_LASTHERO_PARTY_DISABLE;
    }

    @Override
    public boolean isEquipedByCustomArmorWeapon() {
        return Config.EVENT_LASTHERO_ALLOW_EQUIP;
    }

    @Override
    public boolean isQuestionAllowed() {
        return Config.EVENT_LASTHERO_ALLOW_QUESTION;
    }

    public class question extends RunnableImpl {

        @Override
        public void runImpl() {
            GameObjectsStorage.getPlayers().stream().filter(player -> player != null && !player.isDead() && player.getLevel() >= _minLevel && player.getLevel() <= _maxLevel && player.getReflection().isDefault() && !player.isOlyParticipant() && !player.isInObserverMode() && !player.isOnSiegeField()).forEach(player -> {
                ConfirmDlg packet = new ConfirmDlg(SystemMsg.S1, 60000).addString(new CustomMessage("scripts.events.LastHero.AskPlayer", player).toString());
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
            if ((player1 = _playerRef1.get()) == null || Config.EVENT_LASTHERO_CHECK_HWID && !hwid_check.canParticipate(player1)) {
                return;
            }
            player1.sendMessage(new CustomMessage("scripts.events.LastHero.AnswerYes", player));
            addPlayer(player1, lastNpc, args);
        }

        @Override
        public void sayNo() {
            Player player1;
            if ((player1 = _playerRef1.get()) == null) {
                return;
            }
            player1.sendMessage(new CustomMessage("scripts.events.LastHero.AnswerNo", player));
        }

        @Override
        public long expireTime() {
            return _expireTime;
        }
    }

    public class announce extends RunnableImpl {

        @Override
        public void runImpl() {
            if (_time_to_start <= 1 && participants.getPlayersCount() < 2) {
                sayToAll("scripts.events.LastHero.AnnounceEventCancelled");
                _isRegistrationActive = false;
                _status = 0;
                scheduleRunnable(new autoContinue(), 5000);
                return;
            }

            if (_time_to_start > 1) {
                _time_to_start--;
                sayToAll("scripts.events.LastHero.AnnouncePreStart", String.valueOf(_time_to_start), String.valueOf(_minLevel), String.valueOf(_maxLevel));
                scheduleRunnable(new announce(), 60000);
            } else {
                _status = 1;
                _isRegistrationActive = false;
                sayToAll("scripts.events.LastHero.AnnounceEventStarting");
                scheduleRunnable(new prepare(), 5000);
            }
        }
    }

    public class prepare extends RunnableImpl {

        @Override
        public void runImpl() {
            cleanPlayers();
            clearArena();

            scheduleRunnable(new ressurectPlayers(), 1000);
            scheduleRunnable(new healPlayers(), 2000);
            scheduleRunnable(new saveBackCoords(), 2000);
            scheduleRunnable(new teleportPlayersToArena(), 3000);
            scheduleRunnable(new paralyzePlayers(), 5000);
            scheduleRunnable(new go(), 60000);

            sayToAll("scripts.events.LastHero.AnnounceFinalCountdown");
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

            // Остался только один)
            if (participants.getLivePlayersCount() == 1) {
                // FIXME нахрена так то?) переделать
                for (final Player player : participants.getLivePlayers()) {
                    if (player != null) {
                        player.broadcastPacket(new SocialAction(player.getObjectId(), SocialAction.VICTORY));
                        sayToAll("scripts.events.LastHero.AnnounceWiner", player.getName());

                        if (Config.EVENT_LASTHERO_GIVE_HERO) {
                            player.setVar("LastHeroPeriod", (System.currentTimeMillis() + Config.EVENT_LASTHERO_GIVE_HERO_INTERVAL * 60 * 1000), -1);
                            player.setHero(true);
                            HeroManager.addSkills(player);
                            player.updatePledgeClass();
                            player.broadcastUserInfo(true);
                        }

                        if (Config.EVENT_LASTHERO_RATE) {
                            ItemFunctions.addItem(player, Config.EVENT_LASTHERO_ITEM_ID, player.getLevel() * Config.EVENT_LASTHERO_ITEM_COUNT);
                        } else {
                            ItemFunctions.addItem(player, Config.EVENT_LASTHERO_ITEM_ID, Config.EVENT_LASTHERO_ITEM_COUNT);
                        }
                        break;
                    }
                }
            } else {
                // иначе считаем количество убийств(
                final Player player = participants.getTopPlayer();
                if (player != null) {
                    player.broadcastPacket(new SocialAction(player.getObjectId(), SocialAction.VICTORY));
                    sayToAll("scripts.events.LastHero.AnnounceWiner", player.getName());

                    if (Config.EVENT_LASTHERO_GIVE_HERO) {
                        player.setVar("LastHeroPeriod", (System.currentTimeMillis() + Config.EVENT_LASTHERO_GIVE_HERO_INTERVAL * 60 * 1000), -1);
                        player.setHero(true);
                        HeroManager.addSkills(player);
                        player.updatePledgeClass();
                        player.broadcastUserInfo(true);
                    }

                    if (Config.EVENT_LASTHERO_RATE) {
                        ItemFunctions.addItem(player, Config.EVENT_LASTHERO_ITEM_ID, player.getLevel() * Config.EVENT_LASTHERO_ITEM_COUNT * 5);
                    } else {
                        ItemFunctions.addItem(player, Config.EVENT_LASTHERO_ITEM_ID, Config.EVENT_LASTHERO_ITEM_COUNT * 5);
                    }
                }
            }

            // отправляем всем статискику
            participants.sendPacketToAll(getFinalStatistics(participants, "Last Hero TOP"));

            sayToAll("scripts.events.LastHero.AnnounceEnd");
            sendEventMessage(participants, MessageType.FINISH, "");
            scheduleRunnable(new end(), 60000);
            _isRegistrationActive = false;
            if (_endTask != null) {
                _endTask.cancel(false);
                _endTask = null;
            }
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
            _reflection.collapse();
        }
    }

    public class saveBackCoords extends RunnableImpl {

        @Override
        public void runImpl() {
            saveBackCoords(participants.getAllPlayers());
        }
    }

    public class teleportPlayersToSavedCoords extends RunnableImpl {
        @Override
        public void runImpl() {
            teleportToSavedCoords(participants.getAllPlayers());
        }
    }

    public class paralyzePlayers extends RunnableImpl {
        @Override
        public void runImpl() {
            participants.paralyzePlayers();
        }
    }

    public class ressurectPlayers extends RunnableImpl {

        @Override
        public void runImpl() {
            participants.ressurectPlayers();
        }
    }

    public class healPlayers extends RunnableImpl {
        @Override
        public void runImpl() {
            participants.healPlayers();
        }
    }

    private class OnDeathListenerImpl implements OnDeathListener {

        @Override
        public void onDeath(final Creature victim, final Creature killer) {
            if (_status > 1 && victim != null && victim.isPlayer() && participants.isLive(victim.getObjectId())) {

                // Выдаём очки убийце и выдаём награду
                if (killer != null && killer.isPlayer()) {
                    if (participants.isInTeam(killer.getObjectId())) {
                        // Выдаём очки
                        participants.getPlayerInfo(killer.getObjectId()).incrementKills();
                        if (Config.EVENT_LASTHERO_ALLOW_KILL_BONUS) {
                            ItemFunctions.addItem(killer.getPlayer(), Config.EVENT_LASTHERO_KILL_BONUS_ID, Config.EVENT_LASTHERO_KILL_BONUS_COUNT);
                        }
                        if (Config.EVENT_LASTHERO_ALLOW_PVP_COUNT_BONUS) {
                            killer.getPlayer().setPvpKills(killer.getPlayer().getPvpKills() + 1);
                        }
                        if (Config.EVENT_LASTHERO_ALLOW_PVP_DECREASE_ONDIE) {
                            int pvpkills = victim.getPlayer().getPvpKills() - 1;
                            if (pvpkills < 1) {
                                pvpkills = 0;
                            }
                            victim.getPlayer().setPvpKills(pvpkills);
                        }
                    }
                }

                final Player player = victim.getPlayer();
                loosePlayer(player);
                checkLive();
            }
        }
    }

    public class go extends RunnableImpl {

        @Override
        public void runImpl() {
            arena.getZones().keySet().stream().map(_reflection::getZone).forEach(_zone -> {
                _zone.addListener(_zoneListener);
                _zone.setType(ZoneType.battle_zone);
            });
            _status = 2;
            upParalyzePlayers();
            // бафаем игроков перед стартом
            if (Config.EVENT_LASTHERO_BUFFS_ONSTART) {
                buffPlayers();
            }

            //checkLive();
            sayToAll("scripts.events.LastHero.AnnounceFight");
            _endTask = scheduleRunnable(new endBattle(), Config.EVENT_LASTHERO_TIME_FOR_FIGHT * 60 * 1000);
        }
    }

    public class autoContinue extends RunnableImpl {

        @Override
        public void runImpl() {
            participants.clear();
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

    public class teleportPlayersToArena extends RunnableImpl {

        @Override
        public void runImpl() {
            closeArenaDoors();

            participants.getAllPlayers().forEach(player -> {
                preparePlayer(player);
                player.teleToLocation(Rnd.get(arena.getTeleportLocations()), _reflection);
                player.addListener(_attackListener);
                lockItems(player, Config.EVENT_LASTHERO_DISSALOWED_ITEMS);
                player.setIsInLastHero(true);
            });
        }
    }

    private class OnPlayerExitListenerImpl implements OnPlayerExitListener {
        @Override
        public void onPlayerExit(final Player player) {
            if (player == null || player.getTeam() != TeamType.RED) {
                return;
            }

            // Вышел или вылетел во время регистрации
            if (_status == 0 && _isRegistrationActive && participants.isLive(player)) {
                removePlayer(player);
                return;
            }

            // Вышел или вылетел во время телепортации
            if (_status == 1 && participants.isLive(player)) {
                removePlayer(player);
                player.teleToClosestTown();
                return;
            }

            // Вышел или вылетел во время эвента
            onEscape(player);
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
