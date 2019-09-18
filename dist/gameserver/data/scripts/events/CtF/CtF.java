package events.CtF;

import com.stringer.annotations.HideAccess;
import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.data.xml.holder.CtFArenaHolder;
import ru.j2dev.gameserver.handler.bypass.Bypass;
import ru.j2dev.gameserver.listener.actor.OnDeathListener;
import ru.j2dev.gameserver.listener.actor.npc.OnShowChatEventListener;
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
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ConfirmDlg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import ru.j2dev.gameserver.skills.EffectType;
import ru.j2dev.gameserver.templates.arenas.CtFArena;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.NpcUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

/**
 * Created by JunkyFunky
 * on 05.05.2018 08:58
 * group j2dev
 */
@HideAccess
public class CtF extends PvpEvent {
    private final int RED_FLAG_NPCID = 35423;
    private final int BLUE_FLAG_NPCID = 35426;
    private final HwidChecker hwid_check = new HwidChecker(Config.EVENT_CTF_CHECK_HWID);
    private final IpChecker ip_check = new IpChecker(Config.EVENT_CTF_CHECK_IP);
    private final AttackMagicListenerImpl _attackListener = new AttackMagicListenerImpl(Config.EVENT_CTF_DISSALOWED_SKILL, true);
    private final OnShowChatEventListenerImpl onShowChatEventListener = new OnShowChatEventListenerImpl();
    private final OnTeleportImpl onTeleportListener = new OnTeleportImpl();
    private List<Location> team1loc = new ArrayList<>();
    private List<Location> team2loc = new ArrayList<>();
    private PvpEventTeam teamBlue = new PvpEventTeam(BLUE_TEAM, "CTF_BLUE");
    private PvpEventTeam teamRed = new PvpEventTeam(RED_TEAM, "CTF_RED");
    private final ZoneListener zoneListener = new ZoneListener(teamBlue, teamRed);
    private NpcInstance redFlag;
    private NpcInstance blueFlag;
    private boolean _blueFlagCaptured;
    private boolean _redFlagCaptured;
    private List<CtFArena> arenas;
    private CtFArena arena;

    private boolean checkCountTeam(final int team) {
        if (team == 1 && teamBlue.getPlayersCount() < Config.EVENT_CTF_MAX_LENGTH_TEAM) {
            return true;
        } else return team == 2 && teamRed.getPlayersCount() < Config.EVENT_CTF_MAX_LENGTH_TEAM;

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
            player.sendMessage(new CustomMessage("scripts.events.CtF.Cancelled", player));
            return false;
        }

        if (player.getLevel() < _minLevel || player.getLevel() > _maxLevel) {
            player.sendMessage(new CustomMessage("scripts.events.CtF.CancelledLevel", player));
            return false;
        }

        if (player.isMounted()) {
            player.sendMessage(new CustomMessage("scripts.events.CtF.Cancelled", player));
            return false;
        }

        if (player.isInDuel()) {
            player.sendMessage(new CustomMessage("scripts.events.CtF.CancelledDuel", player));
            return false;
        }

        if (first && (player.getTeam() != TeamType.NONE)) {// || player.isRegistredEvent())) {
            player.sendMessage(new CustomMessage("scripts.events.CtF.CancelledOtherEvent", player));
            return false;
        }

        if (player.isOlyParticipant() || first && OlympiadPlayersManager.getInstance().isRegistred(player)) {
            player.sendMessage(new CustomMessage("scripts.events.CtF.CancelledOlympiad", player));
            return false;
        }

        if (player.isInParty() && player.getParty().isInDimensionalRift()) {
            player.sendMessage(new CustomMessage("scripts.events.CtF.CancelledOtherEvent", player));
            return false;
        }

        if (player.isTeleporting()) {
            player.sendMessage(new CustomMessage("scripts.events.CtF.CancelledTeleport", player));
            return false;
        }

        if (player.isCursedWeaponEquipped()) {
            player.sendMessage(new CustomMessage("scripts.events.CtF.Cancelled", player));
            return false;
        }

        // последним проверяем HardwareID
        if (first && !hwid_check.canParticipate(player)) {
            player.sendMessage(new CustomMessage("scripts.events.CtF.Cancelled", player));
            return false;
        }

        return true;
    }

    private void giveItemsToWinner(final PvpEventTeam team, final double rate) {
        team.getAllPlayers()
                .stream()
                .filter(Objects::nonNull)
                .forEach(player -> ItemFunctions.addItem(player, Config.EVENT_CTF_ITEM_ID, Math.round((Config.EVENT_CTF_RATE ? player.getLevel() : 1) * Config.EVENT_CTF_ITEM_COUNT * rate)));
    }

    private void removeAura() {
        teamRed.removeAura();
        teamBlue.removeAura();
    }

    private List<Integer> getFighterBuffs() {
        return Config.EVENT_CTF_BUFFS_FIGHTER;
    }

    private List<Integer> getMageBuffs() {
        return Config.EVENT_CTF_BUFFS_MAGE;
    }

    private void buffPlayers() {
        teamBlue.getAllPlayers().forEach(this::addBuffToPlayer);
        teamRed.getAllPlayers().forEach(this::addBuffToPlayer);
    }

    private void cleanPlayers() {
        teamBlue.getAllPlayers().forEach(player -> {
            if (!checkPlayer(player, false)) {
                removePlayer(player);
            } else {
                player.setTeam(TeamType.BLUE);
            }
        });
        teamRed.getAllPlayers().forEach(player -> {
            if (!checkPlayer(player, false)) {
                removePlayer(player);
            } else {
                player.setTeam(TeamType.RED);
            }
        });
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

    private void removePlayer(final Player player) {
        if (player != null) {
            teamRed.removePlayer(player);
            teamBlue.removePlayer(player);
            dropFlag(player);
        }
    }

    private void addFlag(final Player player) {
        final int flagId = player.getTeam() == TeamType.BLUE ? 13560 : 13561;
        final ItemInstance item = ItemFunctions.createItem(flagId);
        item.setCustomFlags(ItemInstance.FLAG_NO_DESTROY | ItemInstance.FLAG_NO_TRADE | ItemInstance.FLAG_NO_DROP | ItemInstance.FLAG_NO_TRANSFER);
        item.setCustomFlags(77);
        player.getInventory().addItem(item);
        player.getInventory().equipItem(item);
        player.sendChanges();
        player.setVar("CtFFlag", "1", Config.EVENT_CTF_TIME_FOR_FIGHT * 60 * 1000);
        player.startWeaponEquipBlocked();
        player.storeDisableSkills();
        if (player.isInvisible()) {
            player.getEffectList().stopAllSkillEffects(EffectType.Invisible);
        }
    }

    private void removeFlag(final Player player) {
        if (player != null && player.isEventFlagEquiped()) {
            final ItemInstance flag = player.getActiveWeaponInstance();
            if (flag != null && flag.getCustomFlags() == 77) { // 77 это эвентовый флаг
                flag.setCustomFlags(0);
                player.getInventory().destroyItem(flag, 1);
                player.broadcastUserInfo(true);
                player.getVars().remove("CtFFlag");
                player.stopWeaponEquipBlocked();
            }
        }
    }

    private void dropFlag(final Player player) {
        if (player != null && player.isEventFlagEquiped()) {
            final ItemInstance flag = player.getActiveWeaponInstance();
            if (flag != null && flag.getCustomFlags() == 77) {
                removeFlag(player);
                if (flag.getItemId() == 13561) {
                    _blueFlagCaptured = false;
                } else if (flag.getItemId() == 13560) {
                    _redFlagCaptured = false;
                }
            }
        }
    }

    private void flagToBase(final Player player) {
        dropFlag(player);
        player.getVars().remove("CtFFlag");
        if (player.getTeam() == TeamType.BLUE) {
            teamBlue.increaseTeamPoints();
        } else if (player.getTeam() == TeamType.RED) {
            teamRed.increaseTeamPoints();
        }

        sendFlagToBase(player, teamRed);
        sendFlagToBase(player, teamBlue);
    }

    private void sendFlagToBase(Player player, PvpEventTeam eventTeam) {
        eventTeam.getAllPlayers().forEach(player1 -> player.sendPacket(new ExShowScreenMessage(String.format(new CustomMessage("scripts.events.CtF.FlagToBase", player).toString(), player.getName(), player.getTeam() == TeamType.BLUE ? "Синих" : "Красных"), 5000, ScreenMessageAlign.MIDDLE_CENTER, true)));
    }

    private void removeFlags() {
        teamBlue.getAllPlayers().forEach(this::removeFlag);
        teamRed.getAllPlayers().forEach(this::removeFlag);
    }

    private void openColiseumDoors() {
        _reflection.getDoors().forEach(DoorInstance::openMe);
    }

    private void closeColiseumDoors() {
        _reflection.getDoors().forEach(DoorInstance::closeMe);
    }


    private void saveBackCoords(final List<Player> players) {
        players.forEach(player -> savePlayerBackCoord(player, "CaptureTheFlagBack"));
    }

    private void teleportToSavedCoords(final List<Player> players) {
        players.forEach(player -> {
            player.setIsInCtF(false);
            unlockItems(player, Config.EVENT_CTF_DISSALOWED_ITEMS);
            player.removeListener(_attackListener);
            player.removeListener(onTeleportListener);
            teleportPlayerToBack(player, true, Config.EVENT_CTF_BUFFS_ONSTART, "CaptureTheFlagBack");
        });
    }

    private void upParalyzePlayers() {
        teamRed.upParalyzePlayers();
        teamBlue.upParalyzePlayers();
    }

    /**
     * чистим арену от мусора) чистим только мусор который находиться в отражении для данного эвента
     */
    private void clearArena() {
        arena.getZones().keySet().stream().map(_reflection::getZone).forEach(_zone -> _zone.getInsidePlayables().stream().filter(obj -> obj.getReflection() == _reflection).forEach(obj -> {
            final Player player = obj.getPlayer();
            if (player != null && !teamRed.isLive(player.getObjectId()) && !teamBlue.isLive(player.getObjectId())) {
                player.sendMessage(player.isLangRus() ? "Вы не можете находиться в зоне проведения ПвП Ивента!" : "You not allow primission on enter PvP Event Zone now!");
                LOGGER.warn("Huligan on CtF : {}", player);
                player.teleToClosestTown();
            }
        }));
    }

    @Override
    public void onInit() {
        CharListenerList.addGlobal(new OnDeathListenerImpl());
        CharListenerList.addGlobal(new OnPlayerExitListenerImpl());
        arenas = CtFArenaHolder.getInstance().getArenas();

        // Если ивент активен, но пробуем зашедулить
        if (isActive()) {
            scheduleEventStart();
        }

        LOGGER.info("Loaded Event: "+getEventType()+" " + '[' + isActive() + ']' + " loaded arenas : " +arenas.size());
    }

    @Bypass("events.CtF:activateEvent")
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

    @Bypass("events.CtF:deactivateEvent")
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

    @Bypass("events.CtF:manualEnd")
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
    @Bypass("events.CtF:start")
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
        _time_to_start = Config.EVENT_CTF_TIME_TO_START;

        arena = Rnd.get(arenas);
        LOGGER.debug(getEventType() + " Event Start Debug : " + " arena id " + arena.getId() + " zones array " + arena.getZones().keySet());
        _reflection = new Reflection(getEventType().getRefId(), true);
        arena.initDoors();
        _reflection.init(arena.getArenaDoors(), arena.getZones());
        _reflection.startCollapseTimer(Config.EVENT_CTF_TIME_FOR_FIGHT * 2 * 60 * 1000);
        arena.getZones().keySet().forEach(s -> {
            _reflection.getZone(s).setType(ZoneType.peace_zone);
            _reflection.getZone(s).addListener(zoneListener);
        });
        team1loc = arena.getTeleportLocations(1);
        team2loc = arena.getTeleportLocations(2);
        if (redFlag != null) {
            redFlag.deleteMe();
        }
        if (blueFlag != null) {
            blueFlag.deleteMe();
        }

        redFlag = NpcUtils.spawnSingle(RED_FLAG_NPCID, arena.getRedBaseLocations(), _reflection);
        blueFlag = NpcUtils.spawnSingle(BLUE_FLAG_NPCID, arena.getBlueBaseLocations(), _reflection);
        redFlag.setHasChatWindow(false);
        blueFlag.setHasChatWindow(false);
        redFlag.addListener(onShowChatEventListener);
        blueFlag.addListener(onShowChatEventListener);
        redFlag.decayMe();
        blueFlag.decayMe();

        sayToAll("scripts.events.CtF.AnnouncePreStart", String.valueOf(_time_to_start), String.valueOf(_minLevel), String.valueOf(_maxLevel));
        if(isQuestionAllowed()) {
            scheduleRunnable(new question(), 10000);
        }
        scheduleRunnable(new announce(), 60000);
        //logEventStart(category, _autoContinue);
        onStart(this);
    }

    @Bypass("events.CtF:addPlayer")
    public void addPlayer(Player player, NpcInstance lastNpc, String[] args) {
        if (player == null || !checkPlayer(player, true)) {
            return;
        }
        if (!_isRegistrationActive) {
            player.sendMessage(player.isLangRus() ? "Регистрация не активна!" : "Registration is not active!");
            return;
        }
        if(isInTeam(player, teamBlue, teamRed)) {
            player.sendMessage(player.isLangRus() ? "Вы уже зарегистрированы в эвенте!" : "You already registred on event!");
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
            player.sendMessage(new CustomMessage("scripts.events.CtF.MaxCountTeam", player));
            return;
        }

        switch (team) {
            case 1:
                teamRed.addPlayer(player);
                player.sendMessage(new CustomMessage("scripts.events.CtF.Registered", player));
                break;
            case 2:
                teamBlue.addPlayer(player);
                player.sendMessage(new CustomMessage("scripts.events.CtF.Registered", player));
                break;
            default:
                LOGGER.info("WTF??? Command id 0 in CtF...");
                break;
        }
    }

    @Bypass("events.CtF:removePlayerCb")
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
            for (Config.EventInterval config : Config.EVENT_CTF_INTERVAL) {

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
        return PvpEventType.CaptureTheFlag;
    }

    @Override
    public boolean isAllowClanSkill() {
        return Config.EVENT_CTF_ALLOW_CLAN_SKILL;
    }

    @Override
    public boolean isAllowHeroSkill() {
        return Config.EVENT_CTF_ALLOW_HERO_SKILL;
    }

    @Override
    public boolean isAllowBuffs() {
        return Config.EVENT_CTF_ALLOW_BUFFS;
    }

    @Override
    public boolean isDispelTransformation() {
        return Config.EVENT_CTF_DISPEL_TRANSFORMATION;
    }

    @Override
    public boolean isAllowSummons() {
        return Config.EVENT_CTF_ALLOW_SUMMONS;
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
        return Config.EVENT_CTF_ALLOW_EQUIP;
    }

    @Override
    public boolean isQuestionAllowed() {
        return Config.EVENT_CTF_ALLOW_QUESTION;
    }

    @Override
    public int getBuffTime() {
        return Config.EVENT_CTF_TIME_FOR_FIGHT;
    }

    private void teleportPvpTeamToLocations(final PvpEventTeam team, final List<Location> locations) {
        team.getAllPlayers().forEach(player -> {
            preparePlayer(player);
            player.teleToLocation(Location.findPointToStay(Rnd.get(locations), 150, 500, _reflection.getGeoIndex()), _reflection);
            player.addListener(_attackListener);
            player.addListener(onTeleportListener);
            player.setIsInCtF(true);
            lockItems(player, Config.EVENT_CTF_DISSALOWED_ITEMS);
        });
    }

    private ExShowScreenMessage getTeamPointsPacket(PvpEventTeam team) {
        return new ExShowScreenMessage("Points: " + team.getTeamPoints(), 3000, ScreenMessageAlign.MIDDLE_RIGHT, true);
    }

    private class OnShowChatEventListenerImpl implements OnShowChatEventListener {
        @Override
        public void onShowChatEvent(final NpcInstance npc, final Player player) {
            if (npc == null)
                return;
            if (_status > 1 && player != null) {
                if (npc.getNpcId() == BLUE_FLAG_NPCID) {
                    if (player.isEventFlagEquiped() && teamBlue.isInTeam(player) && player.getTeam() == TeamType.BLUE) {
                        flagToBase(player);
                        _redFlagCaptured = false;
                        player.getVars().remove("CtfFlag");
                    } else if (!_blueFlagCaptured && !player.isEventFlagEquiped() && player.getTeam() == TeamType.RED && npc.isVisible()) {
                        addFlag(player);
                        _blueFlagCaptured = true;
                    }
                }

                if (npc.getNpcId() == RED_FLAG_NPCID) {
                    if (player.isEventFlagEquiped() && teamRed.isInTeam(player) && player.getTeam() == TeamType.RED) {
                        flagToBase(player);
                        _blueFlagCaptured = false;
                        player.getVars().remove("CtfFlag");
                    } else if (!_redFlagCaptured && !player.isEventFlagEquiped() && player.getTeam() == TeamType.BLUE && npc.isVisible()) {
                        addFlag(player);
                        _redFlagCaptured = true;
                    }
                }
            }
        }
    }

    private class OnPlayerExitListenerImpl implements OnPlayerExitListener {
        @Override
        public void onPlayerExit(final Player player) {
            if (player == null || player.getTeam() != TeamType.BLUE || player.getTeam() != TeamType.RED) {
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

    private class OnTeleportImpl implements OnTeleportListener {
        @Override
        public void onTeleport(Player player, int x, int y, int z, Reflection reflection) {
            if (arena.getZones().keySet().stream().map(_reflection::getZone).anyMatch(_zone -> _zone.checkIfInZone(x, y, z, reflection))) {
                return;
            }

            onEscape(player);
        }
    }

    public class question extends RunnableImpl {
        @Override
        public void runImpl() {
            GameObjectsStorage.getPlayers().stream().filter(player -> player != null && !player.isDead() && player.getLevel() >= _minLevel && player.getLevel() <= _maxLevel && player.getReflection().isDefault() && !player.isOlyParticipant() && !player.isInObserverMode() && !player.isOnSiegeField()).forEach(player -> {
                ConfirmDlg packet = new ConfirmDlg(SystemMsg.S1, 60000).addString(new CustomMessage("scripts.events.CtF.AskPlayer", player).toString());
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
            if ((player1 = _playerRef1.get()) == null || Config.EVENT_CTF_CHECK_HWID && !hwid_check.canParticipate(player1)) {
                return;
            }
            player1.sendMessage(new CustomMessage("scripts.events.CtF.AnswerYes", player));
            addPlayer(player1, lastNpc, args);
        }

        @Override
        public void sayNo() {
            Player player1;
            if ((player1 = _playerRef1.get()) == null) {
                return;
            }
            player1.sendMessage(new CustomMessage("scripts.events.CtF.AnswerNo", player));
        }

        @Override
        public long expireTime() {
            return _expireTime;
        }
    }

    public class announce extends RunnableImpl {

        @Override
        public void runImpl() {
            if (_time_to_start <= 1 && (teamBlue.isEmpty() || teamRed.isEmpty())) {
                sayToAll("scripts.events.CtF.AnnounceEventCancelled");
                _isRegistrationActive = false;
                _status = 0;
                scheduleRunnable(new autoContinue(), 5000);
                return;
            }

            if (_time_to_start > 1) {
                _time_to_start--;
                sayToAll("scripts.events.CtF.AnnouncePreStart", String.valueOf(_time_to_start), String.valueOf(_minLevel), String.valueOf(_maxLevel));
                scheduleRunnable(new announce(), 60000);
            } else {
                _status = 1;
                _isRegistrationActive = false;
                sayToAll("scripts.events.CtF.AnnounceEventStarting");
                scheduleRunnable(new prepare(), 5000);
            }
        }
    }

    public class prepare extends RunnableImpl {

        @Override
        public void runImpl() {
            closeColiseumDoors();

            cleanPlayers();
            clearArena();

            redFlag.spawnMe();
            blueFlag.spawnMe();
            scheduleRunnable(new ressurectPlayers(), 100);
            scheduleRunnable(new healPlayers(), 200);
            scheduleRunnable(new paralyzePlayers(), 300);
            scheduleRunnable(new saveBackCoords(), 400);
            scheduleRunnable(new checkLive(), 500);
            scheduleRunnable(new teleportPlayersToArena(), 600);
            scheduleRunnable(new go(), 60000);
            sayToAll("scripts.events.CtF.AnnounceFinalCountdown");

        }
    }

    public class endBattle extends RunnableImpl {

        @Override
        public void runImpl() {
            arena.getZones().keySet().forEach(s -> {
                _reflection.getZone(s).setType(ZoneType.peace_zone);
                _reflection.getZone(s).removeListener(zoneListener);
            });
            if (_endTask != null) {
                _endTask.cancel(false);
                _endTask = null;
            }

            removeFlags();

            if (redFlag != null) {
                redFlag.removeListener(onShowChatEventListener);
                redFlag.deleteMe();
                redFlag = null;
            }

            if (blueFlag != null) {
                blueFlag.removeListener(onShowChatEventListener);
                blueFlag.deleteMe();
                blueFlag = null;
            }

            _status = 0;
            removeAura();

            openColiseumDoors();
            if (teamBlue.getTeamPoints() > teamRed.getTeamPoints()) {
                sayToAll("scripts.events.CtF.AnnounceFinishedBlueWins");
                giveItemsToWinner(teamBlue, 1);
            }
            if (teamRed.getTeamPoints() > teamBlue.getTeamPoints()) {
                sayToAll("scripts.events.CtF.AnnounceFinishedRedWins");
                giveItemsToWinner(teamRed, 1);
            }
            if (teamRed.getTeamPoints() == teamBlue.getTeamPoints()) {
                sayToAll("scripts.events.CtF.AnnounceFinishedDraw");
                giveItemsToWinner(teamBlue, 0.5);
                giveItemsToWinner(teamRed, 0.5);
            }

            sayToAll("scripts.events.CtF.AnnounceScore", String.valueOf(teamRed.getTeamPoints()), String.valueOf(teamBlue.getTeamPoints()));
            sayToAll("scripts.events.CtF.AnnounceEnd");
            scheduleRunnable(new end(), 30000);
            _isRegistrationActive = false;
            onStop(CtF.this);
        }
    }

    public class end extends RunnableImpl {

        @Override
        public void runImpl() {
            scheduleRunnable(new ressurectPlayers(), 1000);
            scheduleRunnable(new healPlayers(), 2000);
            scheduleRunnable(new teleportPlayersToSavedCoords(), 3000);
            scheduleRunnable(new autoContinue(), 5000);
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
                    removePlayer(player);
                }
            });

            teamBlue.getLivePlayers().forEach(player -> {
                if (player.isConnected() && !player.isLogoutStarted()) {
                    player.setTeam(TeamType.BLUE);
                } else {
                    removePlayer(player);
                }
            });

            if (teamRed.getLivePlayersCount() < 1 || teamBlue.getLivePlayersCount() < 1) {
                ThreadPoolManager.getInstance().execute(new endBattle());
            }
        }
    }

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
            if (Config.EVENT_CTF_BUFFS_ONSTART) {
                addBuffToPlayer(player);
                healPlayer(player);
            }
            final Location pos;
            if (player.getTeam() == TeamType.BLUE) {
                pos = team1loc.get(Rnd.get(team1loc.size()));
            } else {
                pos = team2loc.get(Rnd.get(team2loc.size()));
            }
            player.teleToLocation(pos, _reflection);
        }
    }

    private class OnDeathListenerImpl implements OnDeathListener {

        @Override
        public void onDeath(final Creature self, final Creature killer) {
            if (_status > 1 && self != null && self.isPlayer() && self.getTeam() != TeamType.NONE && isInTeam(self, teamBlue, teamRed)) {
                if (Config.EVENT_CTF_ALLOW_KILL_BONUS) {
                    ItemFunctions.addItem(killer.getPlayer(), Config.EVENT_CTF_KILL_BONUS_ID, Config.EVENT_CTF_KILL_BONUS_COUNT);
                }
                if (Config.EVENT_CTF_ALLOW_PVP_COUNT_BONUS) {
                    killer.getPlayer().setPvpKills(killer.getPlayer().getPvpKills() + 1);
                }
                if (Config.EVENT_CTF_ALLOW_PVP_DECREASE_ONDIE) {
                    int pvpkills = self.getPlayer().getPvpKills() - 1;
                    if (pvpkills < 1) {
                        pvpkills = 0;
                    }
                    self.getPlayer().setPvpKills(pvpkills);
                }
                dropFlag(self.getPlayer());
                scheduleRunnable(new resurrectAtBase(self.getPlayer()), Config.EVENT_CTF_REVIVE_DELAY * 1000);
            }
        }
    }

    public class go extends RunnableImpl {

        @Override
        public void runImpl() {
            arena.getZones().keySet().stream().map(_reflection::getZone).forEach(_zone -> _zone.setType(ZoneType.battle_zone));
            _status = 2;
            upParalyzePlayers();
            if (Config.EVENT_CTF_BUFFS_ONSTART) {
                buffPlayers();
                scheduleRunnable(new healPlayers(), 1000);
            }
            clearArena();
            sayToAll("scripts.events.CtF.AnnounceFight");
            _endTask = scheduleRunnable(new endBattle(), Config.EVENT_CTF_TIME_FOR_FIGHT * 60 * 1000);
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

    public class teleportPlayersToArena extends RunnableImpl {

        @Override
        public void runImpl() {
            teleportPvpTeamToLocations(teamBlue, team1loc);
            teleportPvpTeamToLocations(teamRed, team2loc);
        }
    }
}
