package ru.j2dev.gameserver.model;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.collections.MultiValueSet;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.commons.dbutils.SqlBatch;
import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.lang.reference.HardReferences;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Announcements;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.GameTimeController;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.ai.CtrlEvent;
import ru.j2dev.gameserver.ai.CtrlIntention;
import ru.j2dev.gameserver.ai.NextAction;
import ru.j2dev.gameserver.ai.PlayerAI;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.dao.*;
import ru.j2dev.gameserver.data.xml.holder.*;
import ru.j2dev.gameserver.data.xml.holder.MultiSellHolder.MultiSellListContainer;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.database.mysql;
import ru.j2dev.gameserver.handler.items.IItemHandler;
import ru.j2dev.gameserver.handler.items.IRefineryHandler;
import ru.j2dev.gameserver.idfactory.IdFactory;
import ru.j2dev.gameserver.listener.actor.player.OnAnswerListener;
import ru.j2dev.gameserver.listener.actor.player.impl.ReviveAnswerListener;
import ru.j2dev.gameserver.listener.actor.player.impl.ScriptAnswerListener;
import ru.j2dev.gameserver.listener.actor.player.impl.SummonAnswerListener;
import ru.j2dev.gameserver.listener.hooks.ListenerHook;
import ru.j2dev.gameserver.listener.hooks.ListenerHookType;
import ru.j2dev.gameserver.manager.*;
import ru.j2dev.gameserver.model.Effect.EEffectSlot;
import ru.j2dev.gameserver.model.Request.L2RequestType;
import ru.j2dev.gameserver.model.Skill.AddedSkill;
import ru.j2dev.gameserver.model.Zone.ZoneType;
import ru.j2dev.gameserver.model.actor.instances.player.*;
import ru.j2dev.gameserver.model.actor.instances.player.FriendList;
import ru.j2dev.gameserver.model.actor.listener.PlayerListenerList;
import ru.j2dev.gameserver.model.actor.recorder.PlayerStatsChangeRecorder;
import ru.j2dev.gameserver.model.base.*;
import ru.j2dev.gameserver.model.chat.chatfilter.ChatMsg;
import ru.j2dev.gameserver.model.entity.DimensionalRift;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.model.entity.SevenSignsFestival.DarknessFestival;
import ru.j2dev.gameserver.model.entity.boat.Boat;
import ru.j2dev.gameserver.model.entity.events.GlobalEvent;
import ru.j2dev.gameserver.model.entity.events.impl.DuelEvent;
import ru.j2dev.gameserver.model.entity.events.impl.SiegeEvent;
import ru.j2dev.gameserver.model.entity.olympiad.*;
import ru.j2dev.gameserver.model.entity.residence.Castle;
import ru.j2dev.gameserver.model.entity.residence.ClanHall;
import ru.j2dev.gameserver.model.entity.residence.Residence;
import ru.j2dev.gameserver.model.instances.*;
import ru.j2dev.gameserver.model.items.*;
import ru.j2dev.gameserver.model.items.Warehouse.WarehouseType;
import ru.j2dev.gameserver.model.items.attachment.FlagItemAttachment;
import ru.j2dev.gameserver.model.items.attachment.PickableAttachment;
import ru.j2dev.gameserver.model.matching.MatchingRoom;
import ru.j2dev.gameserver.model.pledge.*;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestEventType;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.GameClient;
import ru.j2dev.gameserver.network.lineage2.components.*;
import ru.j2dev.gameserver.network.lineage2.serverpackets.*;
import ru.j2dev.gameserver.scripts.Events;
import ru.j2dev.gameserver.skills.AbnormalEffect;
import ru.j2dev.gameserver.skills.EffectType;
import ru.j2dev.gameserver.skills.TimeStamp;
import ru.j2dev.gameserver.skills.effects.EffectCubic;
import ru.j2dev.gameserver.skills.skillclasses.Transformation;
import ru.j2dev.gameserver.stats.Formulas;
import ru.j2dev.gameserver.stats.Stats;
import ru.j2dev.gameserver.tables.*;
import ru.j2dev.gameserver.taskmanager.AutoSaveManager;
import ru.j2dev.gameserver.taskmanager.LazyPrecisionTaskManager;
import ru.j2dev.gameserver.taskmanager.tasks.objecttasks.*;
import ru.j2dev.gameserver.templates.FishTemplate;
import ru.j2dev.gameserver.templates.Henna;
import ru.j2dev.gameserver.templates.InstantZone;
import ru.j2dev.gameserver.templates.PlayerTemplate;
import ru.j2dev.gameserver.templates.item.ArmorTemplate;
import ru.j2dev.gameserver.templates.item.ArmorTemplate.ArmorType;
import ru.j2dev.gameserver.templates.item.ItemTemplate;
import ru.j2dev.gameserver.templates.item.WeaponTemplate;
import ru.j2dev.gameserver.templates.item.WeaponTemplate.WeaponType;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.*;
import ru.j2dev.gameserver.utils.Log.ItemLog;

import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class Player extends Playable implements PlayerGroup {
    public static final int DEFAULT_TITLE_COLOR = 0xffff77;
    public static final int MAX_POST_FRIEND_SIZE = 100;
    public static final int MAX_FRIEND_SIZE = 128;
    public static final String NO_TRADERS_VAR = "notraders";
    public static final String CUSTOM_HERO_END_TIME_VAR = "CustomHeroEndTime";
    public static final String ANIMATION_OF_CAST_RANGE_VAR = "buffAnimRange";
    public static final String LAST_PVP_PK_KILL_VAR_NAME = "LastPVPPKKill";
    public static final int OBSERVER_NONE = 0;
    public static final int OBSERVER_STARTING = 1;
    public static final int OBSERVER_STARTED = 3;
    public static final int OBSERVER_LEAVING = 2;
    public static final int STORE_PRIVATE_NONE = 0;
    public static final int STORE_PRIVATE_SELL = 1;
    public static final int STORE_PRIVATE_BUY = 3;
    public static final int STORE_PRIVATE_MANUFACTURE = 5;
    public static final int STORE_OBSERVING_GAMES = 7;
    public static final int STORE_PRIVATE_SELL_PACKAGE = 8;
    public static final int RANK_VAGABOND = 0;
    public static final int RANK_VASSAL = 1;
    public static final int RANK_HEIR = 2;
    public static final int RANK_KNIGHT = 3;
    public static final int RANK_WISEMAN = 4;
    public static final int RANK_BARON = 5;
    public static final int RANK_VISCOUNT = 6;
    public static final int RANK_COUNT = 7;
    public static final int RANK_MARQUIS = 8;
    public static final int LANG_ENG = 0;
    public static final int LANG_RUS = 1;
    public static final int LANG_UNK = -1;
    public static final int PLAYER_SEX_MALE = 0;
    public static final int PLAYER_SEX_FEMALE = 1;
    public static final int[] EXPERTISE_LEVELS = {0, 20, 40, 52, 61, 76, Integer.MAX_VALUE};
    private static final Logger LOGGER = LoggerFactory.getLogger(Player.class);
    private static final String NOT_CONNECTED = "<not connected>";

    public final Map<Integer, SubClass> _classlist = new HashMap<>(4);
    public final int[] _loto = new int[5];
    public final int[] _race = new int[2];
    final Map<Integer, Skill> _transformationSkills = new HashMap<>();
    private final AntiFlood antiFlood = new AntiFlood();
    private final PcInventory _inventory = new PcInventory(this);
    private final Warehouse _warehouse = new PcWarehouse(this);
    private final ItemContainer _refund = new PcRefund(this);
    private final PcFreight _freight = new PcFreight(this);
    private final Deque<ChatMsg> _msgBucket = new LinkedList<>();
    private final Map<Integer, Recipe> _recipebook = new TreeMap<>();
    private final Map<Integer, Recipe> _commonrecipebook = new TreeMap<>();
    private final Map<String, QuestState> _quests = new HashMap<>();
    private final ShortCutList _shortCuts = new ShortCutList(this);
    private final MacroList _macroses = new MacroList(this);
    private final Henna[] _henna = new Henna[3];
    private final Map<Integer, String> _blockList = new ConcurrentSkipListMap<>();
    private final FriendList _friendList = new FriendList(this);
    private final Fishing _fishing = new Fishing(this);
    private final Lock _storeLock = new ReentrantLock();
    private final Map<Integer, Long> _instancesReuses = new ConcurrentHashMap<>();
    private final AtomicReference<MoveToLocationOffloadData> _mtlOffloadData = new AtomicReference<>(null);
    private final MultiValueSet<String> _vars = new MultiValueSet<>();
    private final Map<Integer, PremiumItem> _premiumItems = new TreeMap<>();
    private final AtomicBoolean _isLogout = new AtomicBoolean();
    private final Set<Integer> _activeSoulShots = new CopyOnWriteArraySet<>();
    private final AtomicInteger _observerMode = new AtomicInteger(0);
    private final Bonus _bonus = new Bonus();
    private final Set<String> _blockedActions = new HashSet<>();
    private final ConcurrentMap<Integer, TimeStamp> _sharedGroupReuses = new ConcurrentHashMap<>();
    private final TIntSet _recommendedCharIds = new TIntHashSet();
    private final AtomicBoolean isActive = new AtomicBoolean();
    public int expertiseIndex;
    public int _telemode;
    public boolean entering = true;
    public Location _stablePoint;
    public volatile boolean sittingTaskLaunched;
    protected int _baseClass;
    protected SubClass _activeClass;
    protected int _pvpFlag;
    private GameClient _connection;
    private String _login;
    private int _karma;
    private int _pkKills;
    private int _pvpKills;
    private int _face;
    private int _hairStyle;
    private int _hairColor;
    private boolean _isUndying;
    private int _deleteTimer;
    private OlympiadStadium _olyObserveOlympiadStadium;
    private OlympiadPlayer _olyOlympiadPlayer;
    private long _createTime;
    private long _onlineTime;
    private long _onlineBeginTime;
    private long _leaveClanTime;
    private long _deleteClanTime;
    private long _NoChannel;
    private long _NoChannelBegin;
    private long _uptime;
    private long _lastAccess;
    private int _nameColor = 0xffffff;
    private int _titlecolor = DEFAULT_TITLE_COLOR;
    private String _disconnectedTitle = Config.DISCONNECTED_PLAYER_TITLE;
    private int _disconnectedTitleColor = Config.DISCONNECTED_PLAYER_TITLE_COLOR;
    private boolean _overloaded;
    private int _fakeDeath;
    private int _waitTimeWhenSit;
    private boolean _autoLoot = Config.AUTO_LOOT;
    private boolean AutoLootHerbs = Config.AUTO_LOOT_HERBS;
    private boolean AutoLootAdena = Config.AUTO_LOOT_ADENA;
    private long _lastNpcInteractionTime;
    private int _privatestore;
    private String _manufactureName;
    private List<ManufactureItem> _createList = Collections.emptyList();
    private String _sellStoreName;
    private List<TradeItem> _sellList = Collections.emptyList();
    private List<TradeItem> _packageSellList = Collections.emptyList();
    private String _buyStoreName;
    private List<TradeItem> _buyList = Collections.emptyList();
    private List<TradeItem> _tradeList = Collections.emptyList();
    private int _hennaSTR;
    private int _hennaINT;
    private int _hennaDEX;
    private int _hennaMEN;
    private int _hennaWIT;
    private int _hennaCON;
    private Party _party;
    private Location _lastPartyPosition;
    private Clan _clan;
    private int _pledgeClass;
    private int _pledgeType = -128;
    private int _powerGrade;
    private int _lvlJoinedAcademy;
    private int _apprentice;
    private int _accessLevel;
    private PlayerAccess _playerAccess = new PlayerAccess();
    private boolean _messageRefusal;
    private boolean _tradeRefusal;
    private boolean _blockAll;
    private Summon _summon;
    private boolean _riding;
    private Map<Integer, EffectCubic> _cubics;
    private Request _request;
    private ItemInstance _arrowItem;
    private WeaponTemplate _fistsWeaponItem;
    private Map<Integer, String> _chars = new HashMap<>(8);
    private ItemInstance _enchantScroll;
    private IRefineryHandler _refineryHandler;
    private WarehouseType _usingWHType;
    private boolean _isOnline;
    private HardReference<NpcInstance> _lastNpc = HardReferences.emptyRef();
    private MultiSellListContainer _multisell;
    private WorldRegion _observerRegion;
    private boolean _hero;
    private Boat _boat;
    private Location _inBoatPosition;
    private Future<?> _bonusExpiration;
    private boolean _isSitting;
    private StaticObjectInstance _sittingObject;
    private boolean _noble;
    private int _varka;
    private int _ketra;
    private int _ram;
    private byte[] _keyBindings = ArrayUtils.EMPTY_BYTE_ARRAY;
    private int _cursedWeaponEquippedId;
    private boolean _isFishing;
    private Future<?> _taskWater;
    private Future<?> _autoSaveTask;
    private Future<?> _kickTask;
    private Future<?> _pcCafePointsTask;
    private Future<?> _unjailTask;
    private Future<?> _customHeroRemoveTask;
    private int _zoneMask;
    private boolean _offline;
    private int _transformationId;
    private int _transformationTemplate;
    private String _transformationName;
    private String _transformationTitle;
    private int _pcBangPoints;
    private int _expandInventory;
    private int _expandWarehouse;
    private int _buffAnimRange = 1500;
    private int _battlefieldChatId;
    private InvisibleType _invisibleType = InvisibleType.NONE;
    private Map<Integer, String> _postFriends = Collections.emptyMap();
    private boolean _notShowTraders;
    private boolean _debug;
    private long _dropDisabled;
    private long _lastItemAuctionInfoRequest;
    private Pair<Integer, OnAnswerListener> _askDialog;
    private MatchingRoom _matchingRoom;
    private int _receivedRec;
    private int _givableRec;
    private Future<?> _updateEffectIconsTask;
    private ScheduledFuture<?> _broadcastCharInfoTask;
    private int _polyNpcId;
    private Future<?> _userInfoTask;
    private int _mountNpcId;
    private int _mountObjId;
    private int _mountLevel;
    private boolean _resurect_prohibited;
    private boolean _maried;
    private int _partnerId;
    private int _coupleId;
    private boolean _maryrequest;
    private boolean _maryaccepted;
    private boolean _charmOfCourage;
    private int _increasedForce;
    private long _increasedForceLastUpdateTimeStamp;
    private Future<?> _increasedForceCleanupTask;
    private long _lastFalling;
    private Location _lastClientPosition;
    private Location _lastServerPosition;
    private int _useSeed;
    private Future<?> _PvPRegTask;
    private long _lastPvpAttack;
    private TamedBeastInstance _tamedBeast;
    private AgathionInstance _agathion;
    private long _lastAttackPacket;
    private Location _groundSkillLoc;
    private int _buyListId;
    private int _incorrectValidateCount;
    private int _movieId;
    private boolean _isInMovie;
    private ItemInstance _petControlItem;
    private Map<Integer, Integer> _traps;
    private Future<?> _hourlyTask;
    private int _hoursInGame;
    private Map<String, String> _userSession;
    private long _afterTeleportPortectionTime;
    // Для будущих проверок в других ивентах и везде где потребуется.
    private AtomicBoolean _inCtF = new AtomicBoolean();
    private AtomicBoolean _InTvT = new AtomicBoolean();
    private AtomicBoolean _InDeathMatch = new AtomicBoolean();
    private AtomicBoolean _inFightClub = new AtomicBoolean();
    private AtomicBoolean _inLastHero = new AtomicBoolean();
    private AtomicBoolean _inKoreanStyle = new AtomicBoolean();
    private AtomicBoolean _inTVTArena = new AtomicBoolean();
    private AtomicBoolean _inDuelEvent = new AtomicBoolean();
    private AtomicBoolean _inRegistredEvent = new AtomicBoolean();
    private AtomicBoolean _noClanAlyCrest = new AtomicBoolean();

    public Player(final int objectId, final PlayerTemplate template, final String accountName) {
        super(objectId, template);
        _baseClass = -1;
        _login = accountName;
        _baseClass = getClassId().getId();
    }

    private Player(final int objectId, final PlayerTemplate template) {
        this(objectId, template, null);
        _ai = new PlayerAI(this);
        if (!Config.EVERYBODY_HAS_ADMIN_RIGHTS) {
            setPlayerAccess(Config.gmlist.get(objectId));
        } else {
            setPlayerAccess(Config.gmlist.get(0));
        }
    }

    public static Player create(final int classId, final int sex, final String accountName, final String name, final int hairStyle, final int hairColor, final int face) {
        final PlayerTemplate template = CharTemplateTable.getInstance().getTemplate(classId, sex != 0);
        final Player player = new Player(IdFactory.getInstance().getNextId(), template, accountName);
        player.setName(name);
        player.setTitle("");
        player.setHairStyle(hairStyle);
        player.setHairColor(hairColor);
        player.setFace(face);
        player.setCreateTime(System.currentTimeMillis());
        if (!CharacterDAO.getInstance().insert(player)) {
            return null;
        }
        return player;
    }

    public static Player restore(final int objectId) {
        Player player = null;
        Connection con = null;
        Statement statement = null;
        Statement statement2 = null;
        PreparedStatement statement3 = null;
        ResultSet rset = null;
        ResultSet rset2 = null;
        ResultSet rset3 = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.createStatement();
            statement2 = con.createStatement();
            rset = statement.executeQuery("SELECT * FROM `characters` WHERE `obj_Id`=" + objectId + " LIMIT 1");
            rset2 = statement2.executeQuery("SELECT `class_id` FROM `character_subclasses` WHERE `char_obj_id`=" + objectId + " AND `isBase`=1 LIMIT 1");
            if (rset.next() && rset2.next()) {
                final int classId = rset2.getInt("class_id");
                final boolean female = rset.getInt("sex") == 1;
                final PlayerTemplate template = CharTemplateTable.getInstance().getTemplate(classId, female);
                player = new Player(objectId, template);
                CharacterVariablesDAO.getInstance().loadVariables(objectId, player.getVars());
                player.loadInstanceReuses();
                player.loadPremiumItemList();
                player._friendList.restore();
                player._postFriends = CharacterPostFriendDAO.getInstance().select(player);
                CharacterGroupReuseDAO.getInstance().select(player);
                player.setBaseClass(classId);
                player._login = rset.getString("account_name");
                final String name = rset.getString("char_name");
                player.setName(name);
                player.setFace(rset.getInt("face"));
                player.setHairStyle(rset.getInt("hairStyle"));
                player.setHairColor(rset.getInt("hairColor"));
                player.setHeading(0);
                player.setKarma(rset.getInt("karma"));
                player.setPvpKills(rset.getInt("pvpkills"));
                player.setPkKills(rset.getInt("pkkills"));
                player.setLeaveClanTime(rset.getLong("leaveclan") * 1000L);
                if (player.getLeaveClanTime() > 0L && player.canJoinClan()) {
                    player.setLeaveClanTime(0L);
                }
                player.setDeleteClanTime(rset.getLong("deleteclan") * 1000L);
                if (player.getDeleteClanTime() > 0L && player.canCreateClan()) {
                    player.setDeleteClanTime(0L);
                }
                player.setNoChannel(rset.getLong("nochannel") * 1000L);
                if (player.getNoChannel() > 0L && player.getNoChannelRemained() < 0L) {
                    player.setNoChannel(0L);
                }
                player.setOnlineTime(rset.getLong("onlinetime") * 1000L);
                final int clanId = rset.getInt("clanid");
                if (clanId > 0) {
                    player.setClan(ClanTable.getInstance().getClan(clanId));
                    player.setPledgeType(rset.getInt("pledge_type"));
                    player.setPowerGrade(rset.getInt("pledge_rank"));
                    player.setLvlJoinedAcademy(rset.getInt("lvl_joined_academy"));
                    player.setApprentice(rset.getInt("apprentice"));
                }
                player.setCreateTime(rset.getLong("createtime") * 1000L);
                player.setDeleteTimer(rset.getInt("deletetime"));
                player.setTitle(rset.getString("title"));
                if (player.getVar("titlecolor") != null) {
                    player.setTitleColor(Integer.decode("0x" + player.getVar("titlecolor")));
                }
                if (player.getVar("namecolor") == null) {
                    if (player.isGM()) {
                        player.setNameColor(Config.GM_NAME_COLOUR);
                    } else if (player.getClan() != null && player.getClan().getLeaderId() == player.getObjectId()) {
                        player.setNameColor(Config.CLANLEADER_NAME_COLOUR);
                    } else {
                        player.setNameColor(Config.NORMAL_NAME_COLOUR);
                    }
                } else {
                    player.setNameColor(Integer.decode("0x" + player.getVar("namecolor")));
                }
                if (Config.AUTO_LOOT_INDIVIDUAL) {
                    player._autoLoot = player.getVarB("AutoLoot", Config.AUTO_LOOT);
                    player.AutoLootHerbs = player.getVarB("AutoLootHerbs", Config.AUTO_LOOT_HERBS);
                }
                player.setFistsWeaponItem(player.findFistsWeaponItem(classId));
                player.setUptime(System.currentTimeMillis());
                player.setLastAccess(rset.getLong("lastAccess"));
                final int givableRecs = rset.getInt("rec_left");
                final int receibedRecs = rset.getInt("rec_have");
                player.setKeyBindings(rset.getBytes("key_bindings"));
                player.setPcBangPoints(rset.getInt("pcBangPoints"));
                player.restoreRecipeBook();
                boolean removeHeroSkills = false;
                player.setNoble(NoblessManager.getInstance().isNobles(player));
                if (Config.OLY_ENABLED) {
                    player.setHero(HeroManager.getInstance().isCurrentHero(player));
                    if (player.isHero()) {
                        HeroManager.getInstance().loadDiary(player.getObjectId());
                    }
                    if (!player.isHero() && player.getVar(CUSTOM_HERO_END_TIME_VAR) != null) {
                        final long customHeroEndTime = player.getVarLong(CUSTOM_HERO_END_TIME_VAR, 0L);
                        final long customHeroLeftTimeSec = customHeroEndTime - System.currentTimeMillis() / 1000L;
                        if (customHeroLeftTimeSec > 0L) {
                            player.setCustomHero(true, customHeroLeftTimeSec, false);
                        } else {
                            player.setCustomHero(false, 0L, false);
                            removeHeroSkills = true;
                        }
                    }
                }
                player.updatePledgeClass();
                int reflection = 0;
                if (player.getVar("jailed") != null && System.currentTimeMillis() / 1000L < Integer.parseInt(player.getVar("jailed")) + 60) {
                    player.setXYZ(-114648, -249384, -2984);
                    player.sitDown(null);
                    player.block();
                    player._unjailTask = ThreadPoolManager.getInstance().schedule(new UnJailTask(player), Integer.parseInt(player.getVar("jailed")) * 1000L);
                } else {
                    player.setXYZ(rset.getInt("x"), rset.getInt("y"), rset.getInt("z"));
                    final String ref = player.getVar("reflection");
                    if (ref != null) {
                        reflection = Integer.parseInt(ref);
                        if (reflection > 0) {
                            final String back = player.getVar("backCoords");
                            if (back != null) {
                                player.setLoc(Location.parseLoc(back));
                                player.unsetVar("backCoords");
                            }
                            reflection = 0;
                        }
                    }
                }
                player.setReflection(reflection);
                EventHolder.getInstance().findEvent(player);
                Quest.restoreQuestStates(player);
                player.getInventory().restore();
                restoreCharSubClasses(player);
                player.restoreRecommendedCharacters();
                player.restoreGivableAndReceivedRec(givableRecs, receibedRecs);
                try {
                    final String var = player.getVar("ExpandInventory");
                    if (var != null) {
                        player.setExpandInventory(Integer.parseInt(var));
                    }
                } catch (Exception e) {
                    LOGGER.error("", e);
                }
                try {
                    final String var = player.getVar("ExpandWarehouse");
                    if (var != null) {
                        player.setExpandWarehouse(Integer.parseInt(var));
                    }
                } catch (Exception e) {
                    LOGGER.error("", e);
                }
                try {
                    final String var = player.getVar(ANIMATION_OF_CAST_RANGE_VAR);
                    if (var != null) {
                        player.setBuffAnimRange(Integer.parseInt(var));
                    }
                } catch (Exception e) {
                    LOGGER.error("", e);
                }
                try {
                    final String var = player.getVar(NO_TRADERS_VAR);
                    if (var != null) {
                        player.setNotShowTraders(Boolean.parseBoolean(var));
                    }
                } catch (Exception e) {
                    LOGGER.error("", e);
                }
                try {
                    final String var = player.getVar("pet");
                    if (var != null) {
                        player.setPetControlItem(Integer.parseInt(var));
                    }
                } catch (Exception e) {
                    LOGGER.error("", e);
                }
                statement3 = con.prepareStatement("SELECT obj_Id, char_name FROM characters WHERE account_name=? AND obj_Id!=?");
                statement3.setString(1, player._login);
                statement3.setInt(2, objectId);
                rset3 = statement3.executeQuery();
                while (rset3.next()) {
                    final Integer charId = rset3.getInt("obj_Id");
                    final String charName = rset3.getString("char_name");
                    player._chars.put(charId, charName);
                }
                DbUtils.close(statement3, rset3);
                if (removeHeroSkills) {
                    HeroManager.removeSkills(player);
                }
                final List<Zone> zones = new ArrayList<>();
                World.getZones(zones, player.getLoc(), player.getReflection());
                if (!zones.isEmpty()) {
                    for (final Zone zone : zones) {
                        if (zone.getType() == ZoneType.no_restart) {
                            if (System.currentTimeMillis() / 1000L - player.getLastAccess() <= zone.getRestartTime()) {
                                continue;
                            }
                            player.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.EnterWorld.TeleportedReasonNoRestart", player));
                            player.setLoc(TeleportUtils.getRestartLocation(player, RestartType.TO_VILLAGE));
                        } else {
                            if (zone.getType() != ZoneType.SIEGE) {
                                continue;
                            }
                            final SiegeEvent<?, ?> siegeEvent = player.getEvent(SiegeEvent.class);
                            if (siegeEvent != null) {
                                player.setLoc(siegeEvent.getEnterLoc(player));
                            } else {
                                final Residence r = ResidenceHolder.getInstance().getResidence(zone.getParams().getInteger("residence"));
                                player.setLoc(r.getNotOwnerRestartPoint(player));
                            }
                        }
                    }
                }
                zones.clear();
                if (DimensionalRiftManager.getInstance().checkIfInRiftZone(player.getLoc(), false)) {
                    player.setLoc(DimensionalRiftManager.getInstance().getRoom(0, 0).getTeleportCoords());
                }
                player.restoreBlockList();
                player._macroses.restore();
                player.refreshExpertisePenalty();
                player.refreshOverloaded();
                player.getWarehouse().restore();
                player.getFreight().restore();
                player.restoreTradeList();
                if (player.getVar("storemode") != null) {
                    player.setPrivateStoreType(Integer.parseInt(player.getVar("storemode")));
                    player.setSitting(true);
                }
                player.updateKetraVarka();
                player.updateRam();
                if (player.getVar("lang@") == null) {
                    player.setVar("lang@", Config.DEFAULT_LANG, -1L);
                }
                if (Config.SERVICES_ENABLE_NO_CARRIER && player.getVar("noCarrier") == null) {
                    player.setVar("noCarrier", Config.SERVICES_NO_CARRIER_DEFAULT_TIME, -1L);
                }
            }
        } catch (Exception e2) {
            LOGGER.error("Could not restore char data!", e2);
        } finally {
            DbUtils.closeQuietly(statement2, rset2);
            DbUtils.closeQuietly(statement3, rset3);
            DbUtils.closeQuietly(con, statement, rset);
        }
        return player;
    }

    public static void restoreCharSubClasses(final Player player) {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT class_id,exp,sp,curHp,curCp,curMp,active,isBase,death_penalty FROM character_subclasses WHERE char_obj_id=?");
            statement.setInt(1, player.getObjectId());
            rset = statement.executeQuery();
            SubClass activeSubclass = null;
            while (rset.next()) {
                final SubClass subClass = new SubClass();
                subClass.setBase(rset.getInt("isBase") != 0);
                subClass.setClassId(rset.getInt("class_id"));
                subClass.setExp(rset.getLong("exp"));
                subClass.setSp(rset.getInt("sp"));
                subClass.setHp(rset.getDouble("curHp"));
                subClass.setMp(rset.getDouble("curMp"));
                subClass.setCp(rset.getDouble("curCp"));
                subClass.setDeathPenalty(new DeathPenalty(player, rset.getInt("death_penalty")));
                final boolean active = rset.getInt("active") != 0;
                if (active) {
                    activeSubclass = subClass;
                }
                player.getSubClasses().put(subClass.getClassId(), subClass);
            }
            if (player.getSubClasses().size() == 0) {
                throw new Exception("There are no one subclass for player: " + player);
            }
            final int BaseClassId = player.getBaseClassId();
            if (BaseClassId == -1) {
                throw new Exception("There are no base subclass for player: " + player);
            }
            if (activeSubclass != null) {
                player.setActiveSubClass(activeSubclass.getClassId(), false);
            }
            if (player.getActiveClass() == null) {
                final SubClass subClass2 = player.getSubClasses().get(BaseClassId);
                subClass2.setActive(true);
                player.setActiveSubClass(subClass2.getClassId(), false);
            }
        } catch (Exception e) {
            LOGGER.warn("Could not restore char sub-classes: " + e);
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
    }

    private static boolean hwidIpCheck(String val1, String val2) {
        return (val1 == null && val2 != null) || (val1 != null && !val1.equals(val2));
    }

    public int buffAnimRange() {
        return _buffAnimRange;
    }

    public void setBuffAnimRange(final int value) {
        _buffAnimRange = value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public HardReference<Player> getRef() {
        return (HardReference<Player>) super.getRef();
    }

    public String getAccountName() {
        if (_connection == null) {
            return _login;
        }
        return _connection.getLogin();
    }

    public String getIP() {
        if (_connection == null) {
            return NOT_CONNECTED;
        }
        return _connection.getIpAddr();
    }

    public Map<Integer, String> getAccountChars() {
        return _chars;
    }

    @Override
    public final PlayerTemplate getTemplate() {
        return (PlayerTemplate) _template;
    }

    @Override
    public PlayerTemplate getBaseTemplate() {
        return (PlayerTemplate) _baseTemplate;
    }

    public void changeSex() {
        boolean male = true;
        if (getSex() == 1) {
            male = false;
        }
        _template = CharTemplateTable.getInstance().getTemplate(getClassId(), !male);
    }

    @Override
    public PlayerAI getAI() {
        return (PlayerAI) _ai;
    }

    @Override
    public void doCast(final Skill skill, final Creature target, final boolean forceUse) {
        if (skill == null) {
            return;
        }
        super.doCast(skill, target, forceUse);
        if (Config.ALT_TELEPORT_PROTECTION && getAfterTeleportPortectionTime() > System.currentTimeMillis()) {
            setAfterTeleportPortectionTime(0L);
            sendMessage(new CustomMessage("alt.teleport_protect_gonna", this));
        }
    }

    @Override
    public void altUseSkill(final Skill skill, final Creature target) {
        super.altUseSkill(skill, target);
        if (Config.ALT_TELEPORT_PROTECTION && isPlayer()) {
            if (getAfterTeleportPortectionTime() > System.currentTimeMillis()) {
                setAfterTeleportPortectionTime(0L);
                sendMessage(new CustomMessage("alt.teleport_protect_gonna", this));
            }
        }
    }

    @Override
    public void sendReuseMessage(final Skill skill) {
        if (isCastingNow()) {
            return;
        }
        final TimeStamp sts = getSkillReuse(skill);
        if (sts == null || !sts.hasNotPassed()) {
            return;
        }
        final long timeleft = sts.getReuseCurrent();
        if ((!Config.ALT_SHOW_REUSE_MSG && timeleft < 10000L) || timeleft < 500L) {
            return;
        }
        sendPacket(new SystemMessage(48).addSkillName(skill.getDisplayId(), skill.getDisplayLevel()));
    }

    @Override
    public final int getLevel() {
        return (_activeClass == null) ? 1 : _activeClass.getLevel();
    }

    public int getSex() {
        return getTemplate().isMale ? 0 : 1;
    }

    public int getFace() {
        return _face;
    }

    public void setFace(final int face) {
        _face = face;
    }

    public int getHairColor() {
        return _hairColor;
    }

    public void setHairColor(final int hairColor) {
        _hairColor = hairColor;
    }

    public int getHairStyle() {
        return _hairStyle;
    }

    public void setHairStyle(final int hairStyle) {
        _hairStyle = hairStyle;
    }

    public void offline() {
        if (_connection != null) {
            _connection.setActiveChar(null);
            _connection.close(ServerClose.STATIC);
            setNetConnection(null);
        }
        if (Config.SERVICES_OFFLINE_TRADE_NAME_COLOR_CHANGE) {
            setNameColor(Config.SERVICES_OFFLINE_TRADE_NAME_COLOR);
        }
        if (Config.SERVICES_OFFLINE_TRADE_ABNORMAL != AbnormalEffect.NULL) {
            startAbnormalEffect(Config.SERVICES_OFFLINE_TRADE_ABNORMAL);
        }
        setOfflineMode(true);
        setVar("offline", String.valueOf(System.currentTimeMillis() / 1000L), -1L);
        if (Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK > 0L) {
            startKickTask(Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK * 1000L);
        }
        final Party party = getParty();
        if (party != null) {
            if (isFestivalParticipant()) {
                party.broadcastMessageToPartyMembers(getName() + " has been removed from the upcoming festival.");
            }
            leaveParty();
        }
        if (getPet() != null) {
            getPet().unSummon();
        }
        CursedWeaponsManager.getInstance().doLogout(this);
        if (isOlyParticipant()) {
            getOlyParticipant().onDisconnect(this);
        }
        broadcastCharInfo();
        stopWaterTask();
        stopBonusTask();
        stopHourlyTask();
        stopPcBangPointsTask();
        stopAutoSaveTask();
        stopQuestTimers();
        try {
            getInventory().store();
        } catch (Throwable t) {
            LOGGER.error("", t);
        }
        try {
            store(false);
        } catch (Throwable t) {
            LOGGER.error("", t);
        }
    }

    public void kick() {
        if (_connection != null) {
            _connection.close(LeaveWorld.STATIC);
            setNetConnection(null);
        }
        prepareToLogout();
        deleteMe();
    }

    public void restart() {
        if (_connection != null) {
            _connection.setActiveChar(null);
            setNetConnection(null);
        }
        prepareToLogout();
        deleteMe();
    }

    public void logout() {
        if (_connection != null) {
            _connection.close(ServerClose.STATIC);
            setNetConnection(null);
        }
        prepareToLogout();
        deleteMe();
    }

    private void prepareToLogout() {
        if (_isLogout.getAndSet(true)) {
            return;
        }
        setNetConnection(null);
        setIsOnline(false);
        getListeners().onExit();
        if (isFlying() && !checkLandingState()) {
            _stablePoint = TeleportUtils.getRestartLocation(this, RestartType.TO_VILLAGE);
        }
        if (isCastingNow()) {
            abortCast(true, true);
        }
        final Party party = getParty();
        if (party != null) {
            if (isFestivalParticipant()) {
                party.broadcastMessageToPartyMembers(getName() + " has been removed from the upcoming festival.");
            }
            leaveParty();
        }
        if (Config.OLY_ENABLED && OlympiadSystemManager.getInstance().isCompetitionsActive()) {
            if (isOlyParticipant()) {
                getOlyParticipant().onDisconnect(this);
            }
            if (OlympiadPlayersManager.getInstance().isRegistred(this)) {
                OlympiadPlayersManager.getInstance().onLogout(this);
            }
        }
        CursedWeaponsManager.getInstance().doLogout(this);
        if (isOlyObserver()) {
            leaveOlympiadObserverMode();
        }
        if (isInObserverMode()) {
            leaveObserverMode();
        }
        stopFishing();
        if (_stablePoint != null) {
            teleToLocation(_stablePoint);
        }
        final Summon pet = getPet();
        if (pet != null) {
            pet.saveEffects();
            pet.unSummon();
        }
        _friendList.notifyFriends(false);
        if (isProcessingRequest()) {
            getRequest().cancel();
        }
        stopAllTimers();
        if (isInBoat()) {
            getBoat().removePlayer(this);
        }
        final SubUnit unit = getSubUnit();
        final UnitMember member = (unit == null) ? null : unit.getUnitMember(getObjectId());
        if (member != null) {
            final int sponsor = member.getSponsor();
            final int apprentice = getApprentice();
            final PledgeShowMemberListUpdate memberUpdate = new PledgeShowMemberListUpdate(this);
            _clan.getOnlineMembers(getObjectId()).forEach(clanMember -> {
                clanMember.sendPacket(memberUpdate);
                if (clanMember.getObjectId() == sponsor) {
                    clanMember.sendPacket(new SystemMessage(1757).addString(_name));
                } else {
                    if (clanMember.getObjectId() != apprentice) {
                        return;
                    }
                    clanMember.sendPacket(new SystemMessage(1759).addString(_name));
                }
            });
            member.setPlayerInstance(this, true);
        }
        final FlagItemAttachment attachment = getActiveWeaponFlagAttachment();
        if (attachment != null) {
            attachment.onLogout(this);
        }
        if (CursedWeaponsManager.getInstance().getCursedWeapon(getCursedWeaponEquippedId()) != null) {
            CursedWeaponsManager.getInstance().getCursedWeapon(getCursedWeaponEquippedId()).setPlayer(null);
        }
        final MatchingRoom room = getMatchingRoom();
        if (room != null) {
            if (room.getLeader() == this) {
                room.disband();
            } else {
                room.removeMember(this, false);
            }
        }
        setMatchingRoom(null);
        MatchingRoomManager.getInstance().removeFromWaitingList(this);
        destroyAllTraps();
        stopPvPFlag();
        final Reflection ref = getReflection();
        if (ref != ReflectionManager.DEFAULT) {
            if (ref.getReturnLoc() != null) {
                _stablePoint = ref.getReturnLoc();
            }
            ref.removeObject(this);
        }

        if (!isPhantom()) {
            try {
                getInventory().store();
                getRefund().clear();
            } catch (Throwable t) {
                LOGGER.error("", t);
            }
            try {
                store(false);
            } catch (Throwable t) {
                LOGGER.error("", t);
            }
        }
    }

    public Collection<Recipe> getDwarvenRecipeBook() {
        return _recipebook.values();
    }

    public Collection<Recipe> getCommonRecipeBook() {
        return _commonrecipebook.values();
    }

    public int recipesCount() {
        return _commonrecipebook.size() + _recipebook.size();
    }

    public boolean hasRecipe(final Recipe id) {
        return _recipebook.containsValue(id) || _commonrecipebook.containsValue(id);
    }

    public boolean findRecipe(final int id) {
        return _recipebook.containsKey(id) || _commonrecipebook.containsKey(id);
    }

    public void registerRecipe(final Recipe recipe, final boolean saveDB) {
        if (recipe == null) {
            return;
        }
        switch (recipe.getType()) {
            case ERT_COMMON: {
                _commonrecipebook.put(recipe.getId(), recipe);
                break;
            }
            case ERT_DWARF: {
                _recipebook.put(recipe.getId(), recipe);
                break;
            }
            default: {
                return;
            }
        }
        if (saveDB) {
            mysql.set("REPLACE INTO character_recipebook (char_id, id) VALUES(?,?)", getObjectId(), recipe.getId());
        }
    }

    public void unregisterRecipe(final int RecipeID) {
        if (_recipebook.containsKey(RecipeID)) {
            mysql.set("DELETE FROM `character_recipebook` WHERE `char_id`=? AND `id`=? LIMIT 1", getObjectId(), RecipeID);
            _recipebook.remove(RecipeID);
        } else if (_commonrecipebook.containsKey(RecipeID)) {
            mysql.set("DELETE FROM `character_recipebook` WHERE `char_id`=? AND `id`=? LIMIT 1", getObjectId(), RecipeID);
            _commonrecipebook.remove(RecipeID);
        } else {
            LOGGER.warn("Attempted to remove unknown RecipeList" + RecipeID);
        }
    }

    public QuestState getQuestState(final Quest quest) {
        return getQuestState(quest.getName());
    }

    public QuestState getQuestState(final String quest) {
        questRead.lock();
        try {
            return _quests.get(quest);
        } finally {
            questRead.unlock();
        }
    }

    public QuestState getQuestState(final Class<?> quest) {
        return getQuestState(quest.getSimpleName());
    }

    public boolean isQuestCompleted(final String quest) {
        final QuestState q = getQuestState(quest);
        return q != null && q.isCompleted();
    }

    public boolean isQuestCompleted(final Class<?> quest) {
        final QuestState q = getQuestState(quest);
        return q != null && q.isCompleted();
    }

    public void setQuestState(final QuestState qs) {
        questWrite.lock();
        try {
            _quests.put(qs.getQuest().getName(), qs);
        } finally {
            questWrite.unlock();
        }
    }

    public void removeQuestState(final String quest) {
        questWrite.lock();
        try {
            _quests.remove(quest);
        } finally {
            questWrite.unlock();
        }
    }

    public Quest[] getAllActiveQuests() {
        final List<Quest> quests;
        questRead.lock();
        try {
            quests = _quests.values().stream().filter(QuestState::isStarted).map(QuestState::getQuest).collect(Collectors.toList());
        } finally {
            questRead.unlock();
        }
        return quests.toArray(new Quest[0]);
    }

    public QuestState[] getAllQuestsStates() {
        questRead.lock();
        try {
            return _quests.values().toArray(QuestState.EMPTY_ARRAY);
        } finally {
            questRead.unlock();
        }
    }

    public List<QuestState> getQuestsForEvent(final NpcInstance npc, final QuestEventType event) {
        final List<QuestState> states = new ArrayList<>();
        final Quest[] quests = npc.getTemplate().getEventQuests(event);
        if (quests != null) {
            Arrays.stream(quests).forEach(quest -> {
                final QuestState qs = getQuestState(quest.getName());
                if (qs != null && !qs.isCompleted()) {
                    states.add(getQuestState(quest.getName()));
                }
            });
        }
        return states;
    }

    public void processQuestEvent(final String quest, String event, final NpcInstance npc) {
        if (event == null) {
            event = "";
        }
        QuestState qs = getQuestState(quest);
        if (qs == null) {
            final Quest q = QuestManager.getQuest(quest);
            if (q == null) {
                LOGGER.warn("Quest " + quest + " not found!");
                return;
            }
            qs = q.newQuestState(this, 1);
        }
        if (qs == null || qs.isCompleted()) {
            return;
        }
        qs.getQuest().notifyEvent(event, qs, npc);
        sendPacket(new QuestList(this));
    }

    public boolean isQuestContinuationPossible(final boolean msg) {
        if (getWeightPenalty() >= 3 || getInventoryLimit() * 0.9 < getInventory().getSize() || Config.QUEST_INVENTORY_MAXIMUM * 0.9 < getInventory().getQuestSize()) {
            if (msg) {
                sendPacket(Msg.PROGRESS_IN_A_QUEST_IS_POSSIBLE_ONLY_WHEN_YOUR_INVENTORYS_WEIGHT_AND_VOLUME_ARE_LESS_THAN_80_PERCENT_OF_CAPACITY);
            }
            return false;
        }
        return true;
    }

    public void stopQuestTimers() {
        Arrays.stream(getAllQuestsStates()).forEach(qs -> {
            if (qs.isStarted()) {
                qs.pauseQuestTimers();
            } else {
                qs.stopQuestTimers();
            }
        });
    }

    public void resumeQuestTimers() {
        Arrays.stream(getAllQuestsStates()).forEach(QuestState::resumeQuestTimers);
    }

    public Collection<ShortCut> getAllShortCuts() {
        return _shortCuts.getAllShortCuts();
    }

    public ShortCut getShortCut(final int slot, final int page) {
        return _shortCuts.getShortCut(slot, page);
    }

    public void registerShortCut(final ShortCut shortcut) {
        _shortCuts.registerShortCut(shortcut);
    }

    public void deleteShortCut(final int slot, final int page) {
        _shortCuts.deleteShortCut(slot, page);
    }

    public void registerMacro(final Macro macro) {
        _macroses.registerMacro(macro);
    }

    public void deleteMacro(final int id) {
        _macroses.deleteMacro(id);
    }

    public MacroList getMacroses() {
        return _macroses;
    }

    public boolean isCastleLord(final int castleId) {
        return _clan != null && isClanLeader() && _clan.getCastle() == castleId;
    }

    public int getPkKills() {
        return _pkKills;
    }

    public void setPkKills(final int pkKills) {
        _pkKills = pkKills;
    }

    public long getCreateTime() {
        return _createTime;
    }

    public void setCreateTime(final long createTime) {
        _createTime = createTime;
    }

    public int getDeleteTimer() {
        return _deleteTimer;
    }

    public void setDeleteTimer(final int deleteTimer) {
        _deleteTimer = deleteTimer;
    }

    public int getCurrentLoad() {
        return getInventory().getTotalWeight();
    }

    public long getLastAccess() {
        return _lastAccess;
    }

    public void setLastAccess(final long value) {
        _lastAccess = value;
    }

    public boolean isRecommended(final Player target) {
        return _recommendedCharIds.contains(target.getObjectId());
    }

    public int getReceivedRec() {
        return _receivedRec;
    }

    public void setReceivedRec(final int value) {
        _receivedRec = value;
    }

    public int getGivableRec() {
        return _givableRec;
    }

    public void setGivableRec(final int value) {
        _givableRec = value;
    }

    public void updateRecommends() {
        _recommendedCharIds.clear();
        if (getLevel() >= 40) {
            _givableRec = 9;
            _receivedRec = Math.max(0, _receivedRec - 3);
        } else if (getLevel() >= 20) {
            _givableRec = 6;
            _receivedRec = Math.max(0, _receivedRec - 2);
        } else if (getLevel() >= 10) {
            _givableRec = 3;
            _receivedRec = Math.max(0, _receivedRec - 1);
        } else {
            _givableRec = 0;
            _receivedRec = 0;
        }
    }

    public void restoreGivableAndReceivedRec(final int givableRecs, final int receivedRecs) {
        _givableRec = givableRecs;
        _receivedRec = receivedRecs;
        final Calendar temp = Calendar.getInstance();
        temp.set(Calendar.HOUR_OF_DAY, Config.REC_FLUSH_HOUR);
        temp.set(Calendar.MINUTE, Config.REC_FLUSH_MINUTE);
        temp.set(Calendar.SECOND, 0);
        temp.set(Calendar.MILLISECOND, 0);
        long daysElapsed = Math.round((System.currentTimeMillis() / 1000L - getLastAccess()) / 86400L);
        if (daysElapsed == 0L && getLastAccess() < temp.getTimeInMillis() / 1000L && System.currentTimeMillis() > temp.getTimeInMillis()) {
            daysElapsed++;
        }
        for (int i = 1; i < daysElapsed; i++) {
            updateRecommends();
        }
    }

    public void giveRecommendation(final Player target) {
        if (target == null) {
            return;
        }
        if (getGivableRec() <= 0 || target.getReceivedRec() >= 255) {
            return;
        }
        if (_recommendedCharIds.contains(target.getObjectId())) {
            return;
        }
        _recommendedCharIds.add(target.getObjectId());
        setGivableRec(getGivableRec() - 1);
        sendUserInfo();
        target.setReceivedRec(target.getReceivedRec() + 1);
        target.broadcastUserInfo(true);
    }

    private void restoreRecommendedCharacters() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rset = null;
        try {
            conn = DatabaseFactory.getInstance().getConnection();
            pstmt = conn.prepareStatement("SELECT `targetId` AS `recommendedObjId` FROM `character_recommends` WHERE `objId` = ?");
            pstmt.setInt(1, getObjectId());
            rset = pstmt.executeQuery();
            _recommendedCharIds.clear();
            while (rset.next()) {
                final int recommendedCharId = rset.getInt("recommendedObjId");
                _recommendedCharIds.add(recommendedCharId);
            }
        } catch (SQLException se) {
            LOGGER.error("Can't load recommended characters", se);
        } finally {
            DbUtils.closeQuietly(conn, pstmt, rset);
        }
    }

    private void storeRecommendedCharacters() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseFactory.getInstance().getConnection();
            pstmt = conn.prepareStatement("DELETE FROM `character_recommends` WHERE `objId` = ?");
            pstmt.setInt(1, getObjectId());
            pstmt.executeUpdate();
            DbUtils.close(pstmt);
            if (!_recommendedCharIds.isEmpty()) {
                pstmt = conn.prepareStatement("INSERT INTO `character_recommends` (`objId`, `targetId`) VALUES (?, ?)");
                for (int charId : _recommendedCharIds.toArray()) {
                    pstmt.setInt(1, getObjectId());
                    pstmt.setInt(2, charId);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException se) {
            LOGGER.error("Can't store recommended characters", se);
        } finally {
            DbUtils.closeQuietly(conn, pstmt);
        }
    }

    @Override
    public int getKarma() {
        return _karma;
    }

    public void setKarma(int karma) {
        if (karma < 0) {
            karma = 0;
        }
        if (_karma == karma) {
            return;
        }
        _karma = karma;
        sendChanges();
        if (getPet() != null) {
            getPet().broadcastCharInfo();
        }
    }

    @Override
    public int getMaxLoad() {
        final int con = getCON();
        if (con < 1) {
            return (int) (31000.0 * Config.MAXLOAD_MODIFIER);
        }
        if (con > 59) {
            return (int) (176000.0 * Config.MAXLOAD_MODIFIER);
        }
        return (int) calcStat(Stats.MAX_LOAD, Math.pow(1.029993928, con) * 30495.627366 * Config.MAXLOAD_MODIFIER, this, null);
    }

    @Override
    public void updateEffectIcons() {
        if (entering || isLogoutStarted()) {
            return;
        }
        if (Config.USER_INFO_INTERVAL == 0L) {
            if (_updateEffectIconsTask != null) {
                _updateEffectIconsTask.cancel(false);
                _updateEffectIconsTask = null;
            }
            updateEffectIconsImpl();
            return;
        }
        if (_updateEffectIconsTask != null) {
            return;
        }
        _updateEffectIconsTask = ThreadPoolManager.getInstance().schedule(new UpdateEffectIcons(), Config.USER_INFO_INTERVAL);
    }

    private void updateEffectIconsImpl() {
        final Effect[] effects = getEffectList().getAllFirstEffects();
        final PartySpelled ps = new PartySpelled(this, false);
        final AbnormalStatusUpdate mi = new AbnormalStatusUpdate();
        Arrays.stream(EEffectSlot.VALUES).forEach(ees -> Arrays.stream(effects).filter(eff -> eff.isInUse() && eff.getEffectSlot() == ees).forEach(eff -> {
            if (eff.isStackTypeMatch("HpRecoverCast")) {
                sendPacket(new ShortBuffStatusUpdate(eff));
            } else {
                eff.addIcon(mi);
            }
            if (_party != null) {
                eff.addPartySpelledIcon(ps);
            }
        }));
        sendPacket(mi);
        if (_party != null) {
            _party.broadCast(ps);
        }
        if (isOlyParticipant()) {
            getOlyParticipant().getCompetition().broadcastEffectIcons(this, effects);
        }
    }

    public int getWeightPenalty() {
        return getSkillLevel(4270, 0);
    }

    public void refreshOverloaded() {
        if (isLogoutStarted() || getMaxLoad() <= 0) {
            return;
        }
        setOverloaded(getCurrentLoad() > getMaxLoad());
        final double weightproc = 100.0 * (getCurrentLoad() - calcStat(Stats.MAX_NO_PENALTY_LOAD, 0.0, this, null)) / getMaxLoad();
        int newWeightPenalty;
        if (weightproc < 50.0) {
            newWeightPenalty = 0;
        } else if (weightproc < 66.6) {
            newWeightPenalty = 1;
        } else if (weightproc < 80.0) {
            newWeightPenalty = 2;
        } else if (weightproc < 100.0) {
            newWeightPenalty = 3;
        } else {
            newWeightPenalty = 4;
        }
        final int current = getWeightPenalty();
        if (current == newWeightPenalty) {
            return;
        }
        if (newWeightPenalty > 0) {
            super.addSkill(SkillTable.getInstance().getInfo(4270, newWeightPenalty));
        } else {
            super.removeSkill(getKnownSkill(4270));
        }
        sendPacket(new SkillList(this));
        sendEtcStatusUpdate();
        updateStats();
    }

    public int getGradePenalty() {
        return getSkillLevel(4267, 0);
    }

    public int getExpertisePenalty(final ItemInstance item) {
        if (item.getTemplate().getType2() == 0 || item.getTemplate().getType2() == 1 || item.getTemplate().getType2() == 2) {
            return getGradePenalty();
        }
        return 0;
    }

    public void refreshExpertisePenalty() {
        if (isLogoutStarted()) {
            return;
        }
        int level;
        int i;
        for (level = (int) calcStat(Stats.GRADE_EXPERTISE_LEVEL, getLevel(), null, null), i = 0, i = 0; i < EXPERTISE_LEVELS.length && level >= EXPERTISE_LEVELS[i + 1]; ++i) {
        }
        boolean skillUpdate = false;
        if (expertiseIndex != i) {
            expertiseIndex = i;
            if (expertiseIndex > 0) {
                addSkill(SkillTable.getInstance().getInfo(239, expertiseIndex), false);
                skillUpdate = true;
            }
        }
        int newGradePenalty = 0;
        final ItemInstance[] items = getInventory().getPaperdollItems();
        for (final ItemInstance item : items) {
            if (item != null) {
                final int crystaltype = item.getTemplate().getCrystalType().ordinal();
                if ((item.getTemplate().getType2() == 0 || item.getTemplate().getType2() == 1 || item.getTemplate().getType2() == 2) && crystaltype > newGradePenalty) {
                    newGradePenalty = crystaltype;
                }
            }
        }
        newGradePenalty -= expertiseIndex;
        if (newGradePenalty <= 0) {
            newGradePenalty = 0;
        } else if (newGradePenalty >= 4) {
            newGradePenalty = 4;
        }
        int PenaltyExpertise = getGradePenalty();
        if (PenaltyExpertise != newGradePenalty) {
            if ((PenaltyExpertise = newGradePenalty) > 0) {
                super.addSkill(SkillTable.getInstance().getInfo(4267, PenaltyExpertise));
            } else {
                super.removeSkill(getKnownSkill(4267));
            }
            skillUpdate = true;
        }
        if (skillUpdate) {
            getInventory().validateItemsSkills();
            sendPacket(new SkillList(this));
            sendEtcStatusUpdate();
            updateStats();
        }
    }

    public int getPvpKills() {
        return _pvpKills;
    }

    public void setPvpKills(final int pvpKills) {
        _pvpKills = pvpKills;
    }

    public ClassId getClassId() {
        return getTemplate().classId;
    }

    public void addClanPointsOnProfession(final int id) {
        if (getLvlJoinedAcademy() != 0 && _clan != null && _clan.getLevel() >= 5 && ClassId.VALUES[id].getLevel() == 2) {
            _clan.incReputation(100, true, "Academy");
        } else if (getLvlJoinedAcademy() != 0 && _clan != null && _clan.getLevel() >= 5 && ClassId.VALUES[id].getLevel() == 3) {
            int earnedPoints;
            if (getLvlJoinedAcademy() > 39) {
                earnedPoints = 160;
            } else if (getLvlJoinedAcademy() > 16) {
                earnedPoints = 400 - (getLvlJoinedAcademy() - 16) * 10;
            } else {
                earnedPoints = 400;
            }
            _clan.removeClanMember(getObjectId());
            final SystemMessage sm = new SystemMessage(1748);
            sm.addString(getName());
            sm.addNumber(_clan.incReputation(earnedPoints, true, "Academy"));
            _clan.broadcastToOnlineMembers(sm);
            _clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListDelete(getName()), this);
            setClan(null);
            setTitle("");
            sendPacket(Msg.CONGRATULATIONS_YOU_WILL_NOW_GRADUATE_FROM_THE_CLAN_ACADEMY_AND_LEAVE_YOUR_CURRENT_CLAN_AS_A_GRADUATE_OF_THE_ACADEMY_YOU_CAN_IMMEDIATELY_JOIN_A_CLAN_AS_A_REGULAR_MEMBER_WITHOUT_BEING_SUBJECT_TO_ANY_PENALTIES);
            setLeaveClanTime(0L);
            broadcastCharInfo();
            sendPacket(PledgeShowMemberListDeleteAll.STATIC);
            ItemFunctions.addItem(this, 8181, 1L, true);
        }
    }

    public synchronized void setClassId(final int id, final boolean noban, final boolean fromQuest) {
        if (!noban && !ClassId.VALUES[id].equalsOrChildOf(ClassId.VALUES[getActiveClassId()]) && !getPlayerAccess().CanChangeClass && !Config.EVERYBODY_HAS_ADMIN_RIGHTS) {
            Thread.dumpStack();
            return;
        }
        final boolean isNewSub = !getSubClasses().containsKey(id);
        if (isNewSub) {
            final SubClass cclass = getActiveClass();
            getSubClasses().remove(getActiveClassId());
            changeClassInDb(cclass.getClassId(), id);
            if (cclass.isBase()) {
                setBaseClass(id);
                addClanPointsOnProfession(id);
                ItemInstance coupons = null;
                if (ClassId.VALUES[id].getLevel() == 2) {
                    if (fromQuest && Config.ALT_ALLOW_SHADOW_WEAPONS) {
                        coupons = ItemFunctions.createItem(8869);
                    }
                    unsetVar("newbieweapon");
                    unsetVar("p1q2");
                    unsetVar("p1q3");
                    unsetVar("p1q4");
                    unsetVar("prof1");
                    unsetVar("ng1");
                    unsetVar("ng2");
                    unsetVar("ng3");
                    unsetVar("ng4");
                } else if (ClassId.VALUES[id].getLevel() == 3) {
                    if (fromQuest && Config.ALT_ALLOW_SHADOW_WEAPONS) {
                        coupons = ItemFunctions.createItem(8870);
                    }
                    unsetVar("newbiearmor");
                    unsetVar("dd1");
                    unsetVar("dd2");
                    unsetVar("dd3");
                    unsetVar("prof2.1");
                    unsetVar("prof2.2");
                    unsetVar("prof2.3");
                }
                if (coupons != null) {
                    coupons.setCount(15L);
                    sendPacket(SystemMessage2.obtainItems(coupons));
                    getInventory().addItem(coupons);
                }
            }
            cclass.setClassId(id);
            getSubClasses().put(id, cclass);
            rewardSkills(true);
            storeCharSubClasses();
            if (fromQuest) {
                broadcastPacket(new SocialAction(getObjectId(), 16));
                sendPacket(new PlaySound("ItemSound.quest_fanfare_2"));
            }
            broadcastCharInfo();
        }
        final PlayerTemplate t = CharTemplateTable.getInstance().getTemplate(id, getSex() == 1);
        if (t == null) {
            LOGGER.error("Missing template for classId: " + id);
            return;
        }
        _template = t;
        if (isInParty()) {
            getParty().broadCast(new PartySmallWindowUpdate(this));
        }
        if (getClan() != null) {
            getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdate(this));
        }
        if (_matchingRoom != null) {
            _matchingRoom.broadcastPlayerUpdate(this);
        }
        sendPacket(new SkillList(this));
    }

    public long getExp() {
        return (_activeClass == null) ? 0L : _activeClass.getExp();
    }

    public long getMaxExp() {
        return (_activeClass == null) ? Experience.LEVEL[Experience.getMaxLevel() + 1] : _activeClass.getMaxExp();
    }

    public ItemInstance getEnchantScroll() {
        return _enchantScroll;
    }

    public void setEnchantScroll(final ItemInstance scroll) {
        _enchantScroll = scroll;
    }

    public IRefineryHandler getRefineryHandler() {
        return _refineryHandler;
    }

    public void setRefineryHandler(final IRefineryHandler refineryHandler) {
        _refineryHandler = refineryHandler;
    }

    public WeaponTemplate getFistsWeaponItem() {
        return _fistsWeaponItem;
    }

    public void setFistsWeaponItem(final WeaponTemplate weaponItem) {
        _fistsWeaponItem = weaponItem;
    }

    public WeaponTemplate findFistsWeaponItem(final int classId) {
        if (classId >= 0 && classId <= 9) {
            return (WeaponTemplate) ItemTemplateHolder.getInstance().getTemplate(246);
        }
        if (classId >= 10 && classId <= 17) {
            return (WeaponTemplate) ItemTemplateHolder.getInstance().getTemplate(251);
        }
        if (classId >= 18 && classId <= 24) {
            return (WeaponTemplate) ItemTemplateHolder.getInstance().getTemplate(244);
        }
        if (classId >= 25 && classId <= 30) {
            return (WeaponTemplate) ItemTemplateHolder.getInstance().getTemplate(249);
        }
        if (classId >= 31 && classId <= 37) {
            return (WeaponTemplate) ItemTemplateHolder.getInstance().getTemplate(245);
        }
        if (classId >= 38 && classId <= 43) {
            return (WeaponTemplate) ItemTemplateHolder.getInstance().getTemplate(250);
        }
        if (classId >= 44 && classId <= 48) {
            return (WeaponTemplate) ItemTemplateHolder.getInstance().getTemplate(248);
        }
        if (classId >= 49 && classId <= 52) {
            return (WeaponTemplate) ItemTemplateHolder.getInstance().getTemplate(252);
        }
        if (classId >= 53 && classId <= 57) {
            return (WeaponTemplate) ItemTemplateHolder.getInstance().getTemplate(247);
        }
        return null;
    }

    public void addExpAndCheckBonus(final MonsterInstance mob, final double noRateExp, final double noRateSp) {
        if (_activeClass == null) {
            return;
        }
        double expRate = Config.RATE_XP;
        double spRate = Config.RATE_SP;
        if (mob.isRaid()) {
            expRate = Config.RATE_RAIDBOSS_XP;
            spRate = Config.RATE_RAIDBOSS_SP;
        }
        final long normalExp = (long) (noRateExp * (expRate * getRateExp()));
        final long normalSp = (long) (noRateSp * (spRate * getRateSp()));
        addExpAndSp(normalExp, normalSp, false, true);
    }

    @Override
    public void addExpAndSp(final long exp, final long sp) {
        addExpAndSp(exp, sp, false, false);
    }

    public void addExpAndSp(long addToExp, long addToSp, final boolean applyRate, final boolean applyToPet) {
        if (_activeClass == null) {
            return;
        }
        if (applyRate) {
            addToExp *= (long) (Config.RATE_XP * getRateExp());
            addToSp *= (long) (Config.RATE_SP * getRateSp());
        }
        final Summon pet = getPet();
        boolean updatePetInfo = false;
        if (addToExp > 0L) {
            if (!isCursedWeaponEquipped() && addToSp > 0L && _karma > 0) {
                _karma -= (int) (addToSp / (Config.KARMA_SP_DIVIDER * Config.RATE_SP));
                updatePetInfo = true;
            }
            if (_karma < 0) {
                _karma = 0;
            }
            if (applyToPet && pet != null && !pet.isDead()) {
                if (pet.getNpcId() == 12564) {
                    pet.addExpAndSp(addToExp, 0L);
                    addToExp = 0L;
                } else if (pet.isPet() && pet.getExpPenalty() > 0.0) {
                    if (pet.getLevel() > getLevel() - 20 && pet.getLevel() < getLevel() + 5) {
                        pet.addExpAndSp((long) (addToExp * pet.getExpPenalty()), 0L);
                        addToExp *= (long) (1.0 - pet.getExpPenalty());
                    } else {
                        pet.addExpAndSp((long) (addToExp * pet.getExpPenalty() / 5.0), 0L);
                        addToExp *= (long) (1.0 - pet.getExpPenalty() / 5.0);
                    }
                } else if (pet.isSummon()) {
                    addToExp *= (long) (1.0 - pet.getExpPenalty());
                }
            }
            final long max_xp = getVarB("NoExp") ? (Experience.LEVEL[getLevel() + 1] - 1L) : getMaxExp();
            addToExp = Math.min(addToExp, max_xp - getExp());
        }
        final int oldLvl = _activeClass.getLevel();
        _activeClass.addExp(addToExp);
        _activeClass.addSp(addToSp);
        if (addToSp > 0L && addToExp == 0L) {
            sendPacket(new SystemMessage(331).addNumber(addToSp));
        } else if (addToSp > 0L && addToExp > 0L) {
            sendPacket(new SystemMessage(95).addNumber(addToExp).addNumber(addToSp));
        } else if (addToSp == 0L && addToExp > 0L) {
            sendPacket(new SystemMessage(45).addNumber(addToExp));
        }
        final int level = _activeClass.getLevel();
        if (level != oldLvl) {
            final int levels = level - oldLvl;
            levelSet(levels);
        }
        updateStats();
        if (pet != null && updatePetInfo) {
            pet.broadcastCharInfo();
        }
        getListeners().onGainExpSp(addToExp, addToSp);
    }

    public void rewardSkills() {
        rewardSkills(false);
    }

    public void rewardSkillsAltSrart() {
        int unLearnable = 0;
        for (Collection<SkillLearn> skills = SkillAcquireHolder.getInstance().getAvailableSkills(this, ClassId.VALUES[getActiveClassId()], AcquireType.NORMAL, null); skills.size() > unLearnable; skills = SkillAcquireHolder.getInstance().getAvailableSkills(this, AcquireType.NORMAL)) {
            unLearnable = 0;
            for (final SkillLearn s : skills) {
                final Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
                if (sk == null || !sk.getCanLearn(getClassId()) || !s.canAutoLearn()) {
                    ++unLearnable;
                } else {
                    addSkill(sk, true);
                }
            }
        }
        sendPacket(new SkillList(this));
        updateStats();
    }

    private void rewardSkills(final boolean send) {
        boolean update = false;
        if (Config.AUTO_LEARN_SKILLS) {
            int unLearnable = 0;
            for (Collection<SkillLearn> skills = SkillAcquireHolder.getInstance().getAvailableSkills(this, ClassId.VALUES[getActiveClassId()], AcquireType.NORMAL, null); skills.size() > unLearnable; skills = SkillAcquireHolder.getInstance().getAvailableSkills(this, AcquireType.NORMAL)) {
                unLearnable = 0;
                for (final SkillLearn s : skills) {
                    final Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
                    if (sk == null || !sk.getCanLearn(getClassId()) || !s.canAutoLearn()) {
                        ++unLearnable;
                    } else {
                        addSkill(sk, true);
                    }
                }
            }
            update = true;
        } else {
            for (final SkillLearn skill : SkillAcquireHolder.getInstance().getAvailableSkills(this, AcquireType.NORMAL)) {
                if (skill.getCost() == 0 && skill.getItemId() == 0) {
                    final Skill sk2 = SkillTable.getInstance().getInfo(skill.getId(), skill.getLevel());
                    addSkill(sk2, true);
                    if (getAllShortCuts().size() > 0 && sk2.getLevel() > 1) {
                        getAllShortCuts().stream().filter(sc -> sc.getId() == sk2.getId() && sc.getType() == 2).map(sc -> new ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), sk2.getLevel(), 1)).forEach(newsc -> {
                            sendPacket(new ShortCutRegister(this, newsc));
                            registerShortCut(newsc);
                        });
                    }
                    update = true;
                }
            }
        }
        if (send && update) {
            sendPacket(new SkillList(this));
        }
        updateStats();
    }

    public Race getRace() {
        return getBaseTemplate().race;
    }

    public int getIntSp() {
        return (int) getSp();
    }

    public long getSp() {
        return (_activeClass == null) ? 0L : _activeClass.getSp();
    }

    public void setSp(final long sp) {
        if (_activeClass != null) {
            _activeClass.setSp(sp);
        }
    }

    public int getClanId() {
        return (_clan == null) ? 0 : _clan.getClanId();
    }

    public long getLeaveClanTime() {
        return _leaveClanTime;
    }

    public void setLeaveClanTime(final long time) {
        _leaveClanTime = time;
    }

    public long getDeleteClanTime() {
        return _deleteClanTime;
    }

    public void setDeleteClanTime(final long time) {
        _deleteClanTime = time;
    }

    public long getOnlineBeginTime() {
        return _onlineBeginTime;
    }

    public long getOnlineTime() {
        return _onlineTime;
    }

    public void setOnlineTime(final long time) {
        _onlineTime = time;
        _onlineBeginTime = System.currentTimeMillis();
    }

    public long getNoChannel() {
        return _NoChannel;
    }

    public void setNoChannel(final long time) {
        _NoChannel = time;
        if (_NoChannel > 2145909600000L || _NoChannel < 0L) {
            _NoChannel = -1L;
        }
        if (_NoChannel > 0L) {
            _NoChannelBegin = System.currentTimeMillis();
        } else {
            _NoChannelBegin = 0L;
        }
    }

    public long getNoChannelRemained() {
        if (_NoChannel == 0L) {
            return 0L;
        }
        if (_NoChannel < 0L) {
            return -1L;
        }
        final long remained = _NoChannel - System.currentTimeMillis() + _NoChannelBegin;
        if (remained < 0L) {
            return 0L;
        }
        return remained;
    }

    public void setLeaveClanCurTime() {
        _leaveClanTime = System.currentTimeMillis();
    }

    public void setDeleteClanCurTime() {
        _deleteClanTime = System.currentTimeMillis();
    }

    public boolean canJoinClan() {
        if (_leaveClanTime == 0L) {
            return true;
        }
        if (System.currentTimeMillis() - _leaveClanTime >= Config.CLAN_LEAVE_TIME_PERNALTY) {
            _leaveClanTime = 0L;
            return true;
        }
        return false;
    }

    public boolean canCreateClan() {
        if (_deleteClanTime == 0L) {
            return true;
        }
        if (System.currentTimeMillis() - _deleteClanTime >= Config.NEW_CLAN_CREATE_PENALTY) {
            _deleteClanTime = 0L;
            return true;
        }
        return false;
    }

    public IStaticPacket canJoinParty(final Player inviter) {
        final Request request = getRequest();
        if (request != null && request.isInProgress() && request.getOtherPlayer(this) != inviter) {
            return SystemMsg.WAITING_FOR_ANOTHER_REPLY.packet(inviter);
        }
        if (isBlockAll() || getMessageRefusal()) {
            return SystemMsg.THAT_PERSON_IS_IN_MESSAGE_REFUSAL_MODE.packet(inviter);
        }
        if (isInParty()) {
            return new SystemMessage2(SystemMsg.C1_IS_A_MEMBER_OF_ANOTHER_PARTY_AND_CANNOT_BE_INVITED).addName(this);
        }
        if (inviter.getReflection() != getReflection() && inviter.getReflection() != ReflectionManager.DEFAULT && getReflection() != ReflectionManager.DEFAULT) {
            return SystemMsg.INVALID_TARGET.packet(inviter);
        }
        if (isCursedWeaponEquipped() || inviter.isCursedWeaponEquipped()) {
            return SystemMsg.INVALID_TARGET.packet(inviter);
        }
        if (inviter.isOlyParticipant() || isOlyParticipant()) {
            return SystemMsg.A_USER_CURRENTLY_PARTICIPATING_IN_THE_OLYMPIAD_CANNOT_SEND_PARTY_AND_FRIEND_INVITATIONS.packet(inviter);
        }
        if (!inviter.getPlayerAccess().CanJoinParty || !getPlayerAccess().CanJoinParty) {
            return SystemMsg.INVALID_TARGET.packet(inviter);
        }
        if (getTeam() != TeamType.NONE) {
            return SystemMsg.INVALID_TARGET.packet(inviter);
        }
        return null;
    }

    @Override
    public PcInventory getInventory() {
        return _inventory;
    }

    @Override
    public long getWearedMask() {
        return _inventory.getWearedMask();
    }

    public PcFreight getFreight() {
        return _freight;
    }

    public void removeItemFromShortCut(final int objectId) {
        _shortCuts.deleteShortCutByObjectId(objectId);
    }

    public void removeSkillFromShortCut(final int skillId) {
        _shortCuts.deleteShortCutBySkillId(skillId);
    }

    public boolean isSitting() {
        return _isSitting;
    }

    public void setSitting(final boolean val) {
        _isSitting = val;
    }

    public boolean getSittingTask() {
        return sittingTaskLaunched;
    }

    @Override
    public void sitDown(final StaticObjectInstance throne) {
        if (isSitting() || sittingTaskLaunched || isAlikeDead()) {
            return;
        }
        if (isStunned() || isSleeping() || isParalyzed() || isAttackingNow() || isCastingNow() || isMoving()) {
            getAI().setNextAction(NextAction.REST, null, null, false, false);
            return;
        }
        resetWaitSitTime();
        getAI().setIntention(CtrlIntention.AI_INTENTION_REST, null, null);
        if (throne == null) {
            broadcastPacket(new ChangeWaitType(this, 0));
        } else {
            broadcastPacket(new ChairSit(this, throne));
        }
        _sittingObject = throne;
        setSitting(true);
        sittingTaskLaunched = true;
        ThreadPoolManager.getInstance().schedule(new EndSitDownTask(this), 2500L);
    }

    @Override
    public void standUp() {
        if (!isSitting() || sittingTaskLaunched || isInStoreMode() || isAlikeDead()) {
            return;
        }
        getAI().clearNextAction();
        broadcastPacket(new ChangeWaitType(this, 1));
        _sittingObject = null;
        sittingTaskLaunched = true;
        ThreadPoolManager.getInstance().schedule(new EndStandUpTask(this), 2500L);
    }

    @Override
    protected MoveActionBase createMoveToLocation(final Location dest, int indent, final boolean pathFind) {
        boolean ignoreGeo = !Config.ALLOW_GEODATA;
        final Location from = getLoc();
        final Location to = dest.clone();
        if (isInBoat()) {
            indent += (int) (from.distance(to) - 3 * getBoat().getActingRange());
            ignoreGeo = true;
        }
        if (Config.MOVE_OFFLOAD_MTL_PC) {
            return new MoveToLocationActionForOffload(this, from, to, ignoreGeo, indent, pathFind);
        }
        return new MoveToLocationAction(this, from, to, ignoreGeo, indent, pathFind);
    }

    public void moveBackwardToLocationForPacket(final Location loc, final boolean pathfinding) {
        if (isMoving() && Config.MOVE_OFFLOAD_MTL_PC) {
            _mtlOffloadData.set(new MoveToLocationOffloadData(loc, 0, pathfinding));
            return;
        }
        moveToLocation(loc, 0, pathfinding);
    }

    public void updateWaitSitTime() {
        if (_waitTimeWhenSit < 200) {
            _waitTimeWhenSit += 2;
        }
    }

    public int getWaitSitTime() {
        return _waitTimeWhenSit;
    }

    public void resetWaitSitTime() {
        _waitTimeWhenSit = 0;
    }

    public Warehouse getWarehouse() {
        return _warehouse;
    }

    public ItemContainer getRefund() {
        return _refund;
    }

    public long getAdena() {
        return getInventory().getAdena();
    }

    public boolean reduceAdena(final long adena) {
        return reduceAdena(adena, false);
    }

    public boolean reduceAdena(final long adena, final boolean notify) {
        if (adena < 0L) {
            return false;
        }
        if (adena == 0L) {
            return true;
        }
        final boolean result = getInventory().reduceAdena(adena);
        if (notify && result) {
            sendPacket(SystemMessage2.removeItems(57, adena));
        }
        return result;
    }

    public ItemInstance addAdena(final long adena) {
        return addAdena(adena, false);
    }

    public ItemInstance addAdena(final long adena, final boolean notify) {
        if (adena < 1L) {
            return null;
        }
        final ItemInstance item = getInventory().addAdena(adena);
        if (item != null && notify) {
            sendPacket(SystemMessage2.obtainItems(57, adena, 0));
        }
        return item;
    }

    public GameClient getNetConnection() {
        return _connection;
    }

    public void setNetConnection(final GameClient connection) {
        _connection = connection;
    }

    public int getRevision() {
        return (_connection == null) ? 0 : _connection.getRevision();
    }

    public boolean isConnected() {
        return _connection != null && _connection.isConnected();
    }

    @Override
    public void onAction(final Player player, final boolean shift) {
        if (isFrozen()) {
            player.sendActionFailed();
            return;
        }
        if (Events.onAction(player, this, shift)) {
            player.sendActionFailed();
            return;
        }
        if (player.getTarget() != this) {
            player.setTarget(this);
            if (player.getTarget() == this) {
                player.sendPacket(new MyTargetSelected(getObjectId(), 0));
                player.sendPacket(makeStatusUpdate(9, 10, 11, 12));
            } else {
                player.sendActionFailed();
            }
        } else if (getPrivateStoreType() != STORE_PRIVATE_NONE) {
            if (getRealDistance(player) > getActingRange() && player.getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT) {
                if (!shift) {
                    player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
                } else {
                    player.sendActionFailed();
                }
            } else {
                player.doInteract(this);
            }
        } else if (isAutoAttackable(player)) {
            player.getAI().Attack(this, false, shift);
        } else if (player != this) {
            if (player.getAI().getIntention() != CtrlIntention.AI_INTENTION_FOLLOW) {
                if (!shift) {
                    player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
                } else {
                    player.sendActionFailed();
                }
            } else {
                player.sendActionFailed();
            }
        } else {
            player.sendActionFailed();
        }
    }

    @Override
    public void broadcastStatusUpdate() {
        if (!needStatusUpdate()) {
            return;
        }
        final StatusUpdate su = makeStatusUpdate(10, 12, 34, 9, 11, 33);
        sendPacket(su);
        if (isInParty()) {
            getParty().broadcastToPartyMembers(this, new PartySmallWindowUpdate(this));
        }
        final DuelEvent duelEvent = getEvent(DuelEvent.class);
        if (duelEvent != null) {
            duelEvent.sendPacket(new ExDuelUpdateUserInfo(this), getTeam().revert().name());
        }
        if (isOlyCompetitionStarted()) {
            broadcastPacket(new ExOlympiadUserInfo(this));
        }
    }

    @Override
    public void broadcastCharInfo() {
        broadcastUserInfo(false);
    }

    public void broadcastUserInfo(boolean force) {
        sendUserInfo(force);
        if (!isVisible() || isInvisible()) {
            return;
        }
        if (Config.BROADCAST_CHAR_INFO_INTERVAL == 0L) {
            force = true;
        }
        if (force) {
            if (_broadcastCharInfoTask != null) {
                _broadcastCharInfoTask.cancel(false);
                _broadcastCharInfoTask = null;
            }
            broadcastCharInfoImpl();
            return;
        }
        if (_broadcastCharInfoTask != null) {
            return;
        }
        _broadcastCharInfoTask = ThreadPoolManager.getInstance().schedule(new BroadcastCharInfoTask(), Config.BROADCAST_CHAR_INFO_INTERVAL);
    }

    public boolean isPolymorphed() {
        return _polyNpcId != 0;
    }

    public int getPolyId() {
        return _polyNpcId;
    }

    public void setPolyId(final int polyid) {
        _polyNpcId = polyid;
        teleToLocation(getLoc());
        broadcastUserInfo(true);
    }

    private void broadcastCharInfoImpl() {
        if (!isVisible() || isInvisible()) {
            return;
        }
        final L2GameServerPacket ci = isPolymorphed() ? new NpcInfoPoly(this) : new CharInfo(this);
        World.getAroundPlayers(this).forEach(player -> {
            player.sendPacket(ci);
            player.sendPacket(RelationChanged.create(player, this, player));
        });
    }

    public void setLastNpcInteractionTime() {
        _lastNpcInteractionTime = System.currentTimeMillis();
    }

    public boolean canMoveAfterInteraction() {
        return _lastNpcInteractionTime + 1000L < System.currentTimeMillis();
    }

    public void broadcastRelationChanged() {
        if (!isVisible() || isInvisible()) {
            return;
        }
        World.getAroundPlayers(this).forEach(player -> player.sendPacket(RelationChanged.create(player, this, player)));
    }

    public void sendEtcStatusUpdate() {
        if (!isVisible()) {
            return;
        }
        sendPacket(new EtcStatusUpdate(this));
    }

    private void sendUserInfoImpl() {
        sendPacket(new UserInfo(this));
    }

    public void sendUserInfo() {
        sendUserInfo(false);
    }

    public void sendUserInfo(final boolean force) {
        if (!isVisible() || entering || isLogoutStarted()) {
            return;
        }
        if (Config.USER_INFO_INTERVAL == 0L || force) {
            if (_userInfoTask != null) {
                _userInfoTask.cancel(false);
                _userInfoTask = null;
            }
            sendUserInfoImpl();
            return;
        }
        if (_userInfoTask != null) {
            return;
        }
        _userInfoTask = ThreadPoolManager.getInstance().schedule(new UserInfoTask(), Config.USER_INFO_INTERVAL);
    }

    @Override
    public StatusUpdate makeStatusUpdate(final int... fields) {
        final StatusUpdate su = new StatusUpdate(this);
        for (final int field : fields) {
            switch (field) {
                case 9: {
                    su.addAttribute(field, (int) getCurrentHp());
                    break;
                }
                case 10: {
                    su.addAttribute(field, getMaxHp());
                    break;
                }
                case 11: {
                    su.addAttribute(field, (int) getCurrentMp());
                    break;
                }
                case 12: {
                    su.addAttribute(field, getMaxMp());
                    break;
                }
                case 14: {
                    su.addAttribute(field, getCurrentLoad());
                    break;
                }
                case 15: {
                    su.addAttribute(field, getMaxLoad());
                    break;
                }
                case 26: {
                    su.addAttribute(field, _pvpFlag);
                    break;
                }
                case 27: {
                    su.addAttribute(field, getKarma());
                    break;
                }
                case 33: {
                    su.addAttribute(field, (int) getCurrentCp());
                    break;
                }
                case 34: {
                    su.addAttribute(field, getMaxCp());
                    break;
                }
            }
        }
        return su;
    }

    public void sendStatusUpdate(final boolean broadCast, final boolean withPet, final int... fields) {
        if (fields.length == 0 || (entering && !broadCast)) {
            return;
        }
        final StatusUpdate su = makeStatusUpdate(fields);
        if (!su.hasAttributes()) {
            return;
        }
        final List<L2GameServerPacket> packets = new ArrayList<>(withPet ? 2 : 1);
        if (withPet && getPet() != null) {
            packets.add(getPet().makeStatusUpdate(fields));
        }
        packets.add(su);
        if (!broadCast) {
            sendPacket(packets);
        } else if (entering) {
            broadcastPacketToOthers(packets);
        } else {
            broadcastPacket(packets);
        }
    }

    public int getAllyId() {
        return (_clan == null) ? 0 : _clan.getAllyId();
    }

    @Override
    public void sendPacket(final IStaticPacket p) {
        if (!isConnected()) {
            return;
        }
        if (isPacketIgnored(p.packet(this))) {
            return;
        }
        _connection.sendPacket(p.packet(this));
    }

    @Override
    public void sendPacket(final IStaticPacket... packets) {
        if (!isConnected()) {
            return;
        }
        for (final IStaticPacket p : packets) {
            if (!isPacketIgnored(p)) {
                _connection.sendPacket(p.packet(this));
            }
        }
    }

    private boolean isPacketIgnored(final IStaticPacket p) {
        return p == null;
    }

    @Override
    public void sendPacket(final List<? extends IStaticPacket> packets) {
        if (!isConnected()) {
            return;
        }
        for (final IStaticPacket p : packets) {
            _connection.sendPacket(p.packet(this));
        }
    }

    public void doInteract(final GameObject target) {
        if (target == null || isActionsDisabled()) {
            sendActionFailed();
            return;
        }
        if (target.isPlayer()) {
            final Player temp = (Player) target;
            if (getRealDistance(target) <= target.getActingRange()) {
                switch (temp.getPrivateStoreType()) {
                    case 1:
                    case 8:
                        sendPacket(new PrivateStoreListSell(this, temp));
                        sendActionFailed();
                        break;
                    case 3:
                        sendPacket(new PrivateStoreListBuy(this, temp));
                        sendActionFailed();
                        break;
                    case 5:
                        sendPacket(new RecipeShopSellList(this, temp));
                        sendActionFailed();
                        break;
                }
                sendActionFailed();
            } else if (!getAI().isIntendingInteract(temp)) {
                getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, temp);
            }
        } else {
            target.onAction(this, false);
        }
    }

    public void doAutoLootOrDrop(final ItemInstance item, final NpcInstance fromNpc) {
        final boolean forceAutoloot = fromNpc.isFlying() || getReflection().isAutolootForced();
        if ((fromNpc.isRaid() || fromNpc instanceof ReflectionBossInstance) && !Config.AUTO_LOOT_FROM_RAIDS && !item.isHerb() && !forceAutoloot) {
            item.dropToTheGround(this, fromNpc);
            return;
        }
        if (item.isHerb()) {
            if (!AutoLootHerbs && !forceAutoloot) {
                item.dropToTheGround(this, fromNpc);
                return;
            }
            final Skill[] skills = item.getTemplate().getAttachedSkills();
            if (skills.length > 0) {
                Arrays.stream(skills).forEach(skill -> {
                    altUseSkill(skill, this);
                    if (getPet() != null && getPet().isSummon() && !getPet().isDead()) {
                        getPet().altUseSkill(skill, getPet());
                    }
                });
            }
            item.deleteMe();
        } else {
            if (forceAutoloot || _autoLoot || (item.getItemId() == 57 && AutoLootAdena)) {
                if (!isInParty()) {
                    if (!pickupItem(item, ItemLog.Pickup)) {
                        item.dropToTheGround(this, fromNpc);
                        return;
                    }
                } else {
                    getParty().distributeItem(this, item, fromNpc);
                }
                broadcastPickUpMsg(item);
                return;
            }
            item.dropToTheGround(this, fromNpc);
        }
    }

    @Override
    public void doPickupItem(final GameObject object) {
        if (!object.isItem()) {
            LOGGER.warn("trying to pickup wrong target." + getTarget());
            return;
        }
        sendActionFailed();
        stopMove();
        final ItemInstance item = (ItemInstance) object;
        synchronized (item) {
            if (!item.isVisible()) {
                return;
            }
            if (!ItemFunctions.checkIfCanPickup(this, item)) {
                SystemMessage sm;
                if (item.getItemId() == 57) {
                    sm = new SystemMessage(55);
                    sm.addNumber(item.getCount());
                } else {
                    sm = new SystemMessage(56);
                    sm.addItemName(item.getItemId());
                }
                sendPacket(sm);
                return;
            }
            if (item.isHerb()) {
                final Skill[] skills = item.getTemplate().getAttachedSkills();
                if (skills.length > 0) {
                    Arrays.stream(skills).forEach(skill -> altUseSkill(skill, this));
                }
                broadcastPacket(new GetItem(item, getObjectId()));
                item.deleteMe();
                return;
            }
            final FlagItemAttachment attachment = (item.getAttachment() instanceof FlagItemAttachment) ? ((FlagItemAttachment) item.getAttachment()) : null;
            if (!isInParty() || attachment != null) {
                if (pickupItem(item, ItemLog.Pickup)) {
                    broadcastPacket(new GetItem(item, getObjectId()));
                    broadcastPickUpMsg(item);
                    item.pickupMe();
                }
            } else {
                getParty().distributeItem(this, item, null);
            }
        }
    }

    public boolean pickupItem(final ItemInstance item, final ItemLog log) {
        final PickableAttachment attachment = (item.getAttachment() instanceof PickableAttachment) ? ((PickableAttachment) item.getAttachment()) : null;
        if (!ItemFunctions.canAddItem(this, item)) {
            return false;
        }
        if (item.getItemId() == 57 || item.getItemId() == 6353) {
            final Quest q = QuestManager.getQuest(255);
            if (q != null) {
                processQuestEvent(q.getName(), "CE" + item.getItemId(), null);
            }
        }
        Log.LogItem(this, log, item);
        sendPacket(SystemMessage2.obtainItems(item));
        getInventory().addItem(item);
        if (attachment != null) {
            attachment.pickUp(this);
        }
        sendChanges();
        return true;
    }

    public void setObjectTarget(final GameObject target) {
        setTarget(target);
        if (target == null) {
            return;
        }
        if (target == getTarget()) {
            if (target.isNpc()) {
                final NpcInstance npc = (NpcInstance) target;
                sendPacket(new MyTargetSelected(npc.getObjectId(), getLevel() - npc.getLevel()));
                sendPacket(npc.makeStatusUpdate(9, 10));
                sendPacket(new ValidateLocation(npc), ActionFail.STATIC);
            } else {
                sendPacket(new MyTargetSelected(target.getObjectId(), 0));
            }
        }
    }

    @Override
    public void setTarget(GameObject newTarget) {
        if (newTarget != null && !newTarget.isVisible()) {
            newTarget = null;
        }
        if (newTarget instanceof FestivalMonsterInstance && !isFestivalParticipant()) {
            newTarget = null;
        }
        final Party party = getParty();
        if (party != null && party.isInDimensionalRift()) {
            final int riftType = party.getDimensionalRift().getType();
            final int riftRoom = party.getDimensionalRift().getCurrentRoom();
            if (newTarget != null && !DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(newTarget.getX(), newTarget.getY(), newTarget.getZ())) {
                newTarget = null;
            }
        }
        final GameObject oldTarget = getTarget();
        if (oldTarget != null) {
            if (oldTarget.equals(newTarget)) {
                return;
            }
            if (oldTarget.isCreature()) {
                ((Creature) oldTarget).removeStatusListener(this);
            }
            broadcastPacket(new TargetUnselected(this));
        }
        if (newTarget != null) {
            if (newTarget.isCreature()) {
                ((Creature) newTarget).addStatusListener(this);
            }
            broadcastPacket(new TargetSelected(getObjectId(), newTarget.getObjectId(), getLoc()));
        }
        super.setTarget(newTarget);
    }

    @Override
    public ItemInstance getActiveWeaponInstance() {
        return getInventory().getPaperdollItem(7);
    }

    @Override
    public WeaponTemplate getActiveWeaponItem() {
        final ItemInstance weapon = getActiveWeaponInstance();
        if (weapon == null) {
            return getFistsWeaponItem();
        }
        return (WeaponTemplate) weapon.getTemplate();
    }

    @Override
    public ItemInstance getSecondaryWeaponInstance() {
        return getInventory().getPaperdollItem(8);
    }

    @Override
    public WeaponTemplate getSecondaryWeaponItem() {
        final ItemInstance weapon = getSecondaryWeaponInstance();
        if (weapon == null) {
            return getFistsWeaponItem();
        }
        final ItemTemplate item = weapon.getTemplate();
        if (item instanceof WeaponTemplate) {
            return (WeaponTemplate) item;
        }
        return null;
    }

    public boolean isWearingArmor(final ArmorType armorType) {
        final ItemInstance chest = getInventory().getPaperdollItem(10);
        if (chest == null) {
            return armorType == ArmorType.NONE;
        }
        if (chest.getItemType() != armorType) {
            return false;
        }
        if (chest.getBodyPart() == 32768) {
            return true;
        }
        if (chest.getBodyPart() == 131072) {
            return true;
        }
        final ItemInstance legs = getInventory().getPaperdollItem(11);
        return (legs == null) ? (armorType == ArmorType.NONE) : (legs.getItemType() == armorType);
    }

    @Override
    public void reduceCurrentHp(final double damage, final Creature attacker, final Skill skill, final boolean awake, final boolean standUp, final boolean directHp, final boolean canReflect, final boolean transferDamage, final boolean isDot, final boolean sendMessage) {
        if (attacker == null || isDead() || (attacker.isDead() && !isDot)) {
            return;
        }
        if (attacker.isPlayer() && Math.abs(attacker.getLevel() - getLevel()) > 10) {
            if (attacker.getKarma() > 0 && getEffectList().getEffectsBySkillId(5182) != null && !isInZone(ZoneType.SIEGE)) {
                return;
            }
            if (getKarma() > 0 && attacker.getEffectList().getEffectsBySkillId(5182) != null && !attacker.isInZone(ZoneType.SIEGE)) {
                return;
            }
        }
        super.reduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, canReflect, transferDamage, isDot, sendMessage);
    }

    @Override
    protected void onReduceCurrentHp(double damage, final Creature attacker, final Skill skill, final boolean awake, final boolean standUp, final boolean directHp) {
        if (standUp) {
            standUp();
            if (isFakeDeath()) {
                breakFakeDeath();
            }
        }
        if (isOlyParticipant()) {
            if (isOlyCompetitionStarted()) {
                getOlyParticipant().onDamaged(this, attacker, damage, directHp ? getCurrentHp() : (getCurrentHp() + getCurrentCp()));
            }
            if (!getOlyParticipant().isAlive()) {
                return;
            }
        }
        if (attacker.isPlayable() && !directHp && getCurrentCp() > 0.0) {
            double cp = getCurrentCp();
            if (cp >= damage) {
                cp -= damage;
                damage = 0.0;
            } else {
                damage -= cp;
                cp = 0.0;
            }
            setCurrentCp(cp);
        }
        final double hp = getCurrentHp();
        final DuelEvent duelEvent = getEvent(DuelEvent.class);
        if (duelEvent != null && hp - damage <= 1.0) {
            setCurrentHp(1.0, false);
            duelEvent.onDie(this);
            return;
        }
        super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
    }

    @Override
    public boolean isAlikeDead() {
        return _fakeDeath == 1 || super.isAlikeDead();
    }

    @Override
    public boolean isMovementDisabled() {
        return isFakeDeath() || super.isMovementDisabled();
    }

    @Override
    public boolean isActionsDisabled() {
        return isFakeDeath() || super.isActionsDisabled();
    }

    @Override
    public void doAttack(final Creature target) {
        if (isFakeDeath() || isInMountTransform()) {
            return;
        }
        super.doAttack(target);
    }

    @Override
    public void onHitTimer(final Creature target, final int damage, final boolean crit, final boolean miss, final boolean soulshot, final boolean shld, final boolean unchargeSS) {
        if (isFakeDeath()) {
            sendActionFailed();
            return;
        }
        super.onHitTimer(target, damage, crit, miss, soulshot, shld, unchargeSS);
    }

    public boolean isFakeDeath() {
        return _fakeDeath != 0;
    }

    public void setFakeDeath(final int value) {
        _fakeDeath = value;
    }

    public void breakFakeDeath() {
        getEffectList().stopAllSkillEffects(EffectType.FakeDeath);
    }

    private void altDeathPenalty(final Creature killer) {
        if (!Config.ALT_GAME_DELEVEL) {
            return;
        }
        if (isInZoneBattle() || isInZone(ZoneType.fun)) {
            return;
        }
        deathPenalty(killer);
    }

    public final boolean atWarWith(final Player player) {
        return _clan != null && player.getClan() != null && getPledgeType() != -1 && player.getPledgeType() != -1 && _clan.isAtWarWith(player.getClan().getClanId());
    }

    public boolean atMutualWarWith(final Player player) {
        return _clan != null && player.getClan() != null && getPledgeType() != -1 && player.getPledgeType() != -1 && _clan.isAtWarWith(player.getClan().getClanId()) && player.getClan().isAtWarWith(_clan.getClanId());
    }

    @Override
    public void doPurePk(final Player killer) {
        super.doPurePk(killer);
        killer.setPkKills(killer.getPkKills() + 1);
        if (Config.SERVICES_PK_ANNOUNCE) {
            Announcements.getInstance().announceByCustomMessage("player.pkannounce", new String[]{killer.getName(), getName()});
        }
    }

    private void processRewardPvpPkKill(final Player killer, final boolean isThisPlayerKiller) {
        if (isThisPlayerKiller) {
            doPurePk(killer);
            killer.getListeners().onPvpPkKill(this, true);
        } else {
            killer.setPvpKills(killer.getPvpKills() + 1);
            killer.getListeners().onPvpPkKill(this, false);
        }
        if (Config.SERVICES_PK_KILL_BONUS_ENABLE || Config.SERVICES_PVP_KILL_BONUS_ENABLE) {
            boolean ipCheckSuccess = true;
            boolean hwidCheckSuccess = true;
            if (Config.SERVICES_PK_PVP_BONUS_TIE_IF_SAME_IP) {
                ipCheckSuccess = hwidIpCheck(getIP(), killer.getIP());
            }
            if (Config.SERVICES_PK_PVP_BONUS_TIE_IF_SAME_HWID) {
                hwidCheckSuccess = hwidIpCheck(getNetConnection().getHwid(), killer.getNetConnection().getHwid());
            }
            final long now = System.currentTimeMillis();
            final long lastKillTime = killer.getVarLong(LAST_PVP_PK_KILL_VAR_NAME, 0L);
            if ((ipCheckSuccess || hwidCheckSuccess) && now - lastKillTime > Config.SERVICES_PK_KILL_BONUS_INTERVAL) {
                if (isThisPlayerKiller) {
                    ItemFunctions.addItem(killer, Config.SERVICES_PK_KILL_BONUS_REWARD_ITEM, Config.SERVICES_PK_KILL_BONUS_REWARD_COUNT, true);
                } else {
                    ItemFunctions.addItem(killer, Config.SERVICES_PVP_KILL_BONUS_REWARD_ITEM, Config.SERVICES_PVP_KILL_BONUS_REWARD_COUNT, true);
                }
                killer.setVar(LAST_PVP_PK_KILL_VAR_NAME, now, -1L);
            }
        }
    }

    public void checkAddItemToDrop(final List<ItemInstance> array, final List<ItemInstance> items, final int maxCount) {
        for (int i = 0; i < maxCount && !items.isEmpty(); ++i) {
            array.add(items.remove(Rnd.get(items.size())));
        }
    }

    public FlagItemAttachment getActiveWeaponFlagAttachment() {
        final ItemInstance item = getActiveWeaponInstance();
        if (item == null || !(item.getAttachment() instanceof FlagItemAttachment)) {
            return null;
        }
        return (FlagItemAttachment) item.getAttachment();
    }

    protected void doPKPVPManage(Creature killer) {
        final FlagItemAttachment attachment = getActiveWeaponFlagAttachment();
        if (attachment != null) {
            attachment.onDeath(this, killer);
        }
        if (killer == null || killer == _summon || killer == this) {
            return;
        }
        if (isInZoneBattle() || killer.isInZoneBattle()) {
            return;
        }
        final boolean inFunZone = isInZone(ZoneType.fun);
        if (!Config.FUN_ZONE_PVP_COUNT && inFunZone) {
            return;
        }
        if (killer instanceof Summon && (killer = killer.getPlayer()) == null) {
            return;
        }
        if (killer.isPlayer()) {
            final Player pk = (Player) killer;
            final int repValue = (getLevel() - pk.getLevel() >= 20) ? Config.CRP_REWARD_ON_WAR_KILL_OVER_LEVEL : Config.CRP_REWARD_ON_WAR_KILL;
            final boolean war = atMutualWarWith(pk);
            if (war && pk.getClan().getReputationScore() > 0 && _clan.getLevel() >= 5 && _clan.getReputationScore() > 0 && pk.getClan().getLevel() >= 5) {
                _clan.broadcastToOtherOnlineMembers(new SystemMessage(1782).addString(getName()).addNumber(-_clan.incReputation(-repValue, true, "ClanWar")), this);
                pk.getClan().broadcastToOtherOnlineMembers(new SystemMessage(1783).addNumber(pk.getClan().incReputation(repValue, true, "ClanWar")), pk);
            }
            if (isOnSiegeField()) {
                return;
            }
            if (Config.FUN_ZONE_PVP_COUNT && inFunZone) {
                processRewardPvpPkKill(pk, false);
                pk.sendChanges();
                return;
            }
            if (_pvpFlag > 0 || war) {
                processRewardPvpPkKill(pk, false);
            } else {
                processRewardPvpPkKill(pk, _karma <= 0);
            }
            pk.sendChanges();
        }
        final int karma = _karma;
        decreaseKarma(Config.KARMA_LOST_BASE);
        final boolean isPvP = killer.isPlayable() || killer instanceof GuardInstance;
        if ((killer.isMonster() && !Config.DROP_ITEMS_ON_DIE) || (isPvP && (_pkKills < Config.MIN_PK_TO_ITEMS_DROP || (karma == 0 && Config.KARMA_NEEDED_TO_DROP))) || isFestivalParticipant() || (!killer.isMonster() && !isPvP)) {
            return;
        }
        if (!Config.KARMA_DROP_GM && isGM()) {
            return;
        }
        if (Config.ITEM_ANTIDROP_FROM_PK > 0 && getInventory().getItemByItemId(Config.ITEM_ANTIDROP_FROM_PK) != null) {
            return;
        }
        final int max_drop_count = isPvP ? Config.KARMA_DROP_ITEM_LIMIT : 1;
        double dropRate;
        if (isPvP) {
            dropRate = _pkKills * Config.KARMA_DROPCHANCE_MOD + Config.KARMA_DROPCHANCE_BASE;
        } else {
            dropRate = Config.NORMAL_DROPCHANCE_BASE;
        }
        int dropEquipCount = 0;
        int dropWeaponCount = 0;
        int dropItemCount = 0;
        for (int i = 0; i < Math.ceil(dropRate / 100.0) && i < max_drop_count; ++i) {
            if (Rnd.chance(dropRate)) {
                final int rand = Rnd.get(Config.DROPCHANCE_EQUIPPED_WEAPON + Config.DROPCHANCE_EQUIPMENT + Config.DROPCHANCE_ITEM) + 1;
                if (rand > Config.DROPCHANCE_EQUIPPED_WEAPON + Config.DROPCHANCE_EQUIPMENT) {
                    ++dropItemCount;
                } else if (rand > Config.DROPCHANCE_EQUIPPED_WEAPON) {
                    ++dropEquipCount;
                } else {
                    ++dropWeaponCount;
                }
            }
        }
        final List<ItemInstance> drop = new ArrayList<>();
        final List<ItemInstance> dropItem = new ArrayList<>();
        final List<ItemInstance> dropEquip = new ArrayList<>();
        final List<ItemInstance> dropWeapon = new ArrayList<>();
        getInventory().writeLock();
        try {
            for (final ItemInstance item : getInventory().getItems()) {
                if (item.canBeDropped(this, true)) {
                    if (!Config.KARMA_LIST_NONDROPPABLE_ITEMS.contains(item.getItemId())) {
                        switch (item.getTemplate().getType2()) {
                            case 0:
                                dropWeapon.add(item);
                                break;
                            case 1:
                            case 2:
                                dropEquip.add(item);
                                break;
                            case 5:
                                dropItem.add(item);
                                break;
                        }
                    }
                }
            }
            checkAddItemToDrop(drop, dropWeapon, dropWeaponCount);
            checkAddItemToDrop(drop, dropEquip, dropEquipCount);
            checkAddItemToDrop(drop, dropItem, dropItemCount);
            if (drop.isEmpty()) {
                return;
            }
            for (ItemInstance item2 : drop) {
                if (item2.isAugmented() && !Config.ALT_ALLOW_DROP_AUGMENTED) {
                    item2.setVariationStat1(0);
                    item2.setVariationStat2(0);
                }
                item2 = getInventory().removeItem(item2);
                Log.LogItem(this, ItemLog.PvPDrop, item2);
                if (item2.getEnchantLevel() > 0) {
                    sendPacket(new SystemMessage(375).addNumber(item2.getEnchantLevel()).addItemName(item2.getItemId()));
                } else {
                    sendPacket(new SystemMessage(298).addItemName(item2.getItemId()));
                }
                if (killer.isPlayable() && ((Config.AUTO_LOOT && Config.AUTO_LOOT_PK) || isInFlyingTransform())) {
                    killer.getPlayer().getInventory().addItem(item2);
                    Log.LogItem(this, ItemLog.Pickup, item2);
                    killer.getPlayer().sendPacket(SystemMessage2.obtainItems(item2));
                } else {
                    item2.dropToTheGround(this, Location.findAroundPosition(this, Config.KARMA_RANDOM_DROP_LOCATION_LIMIT));
                }
            }
        } finally {
            getInventory().writeUnlock();
        }
    }

    @Override
    protected void onDeath(final Creature killer) {
        getDeathPenalty().checkCharmOfLuck();
        if (isInStoreMode()) {
            setPrivateStoreType(Player.STORE_PRIVATE_NONE);
        }
        if (isProcessingRequest()) {
            final Request request = getRequest();
            if (isInTrade()) {
                final Player parthner = request.getOtherPlayer(this);
                sendPacket(SendTradeDone.FAIL);
                parthner.sendPacket(SendTradeDone.FAIL);
            }
            request.cancel();
        }

        boolean checkPvp = true;
        if (Config.ALLOW_CURSED_WEAPONS) {
            if (isCursedWeaponEquipped()) {
                CursedWeaponsManager.getInstance().dropPlayer(this);
                checkPvp = false;
            } else if (killer != null && killer.isPlayer() && killer.isCursedWeaponEquipped()) {
                CursedWeaponsManager.getInstance().increaseKills(((Player) killer).getCursedWeaponEquippedId());
                checkPvp = false;
            }
        }
        if (checkPvp) {
            doPKPVPManage(killer);
            altDeathPenalty(killer);
        }
        getDeathPenalty().notifyDead(killer);
        setIncreasedForce(0);
        if (isInParty() && getParty().isInReflection() && getParty().getReflection() instanceof DimensionalRift) {
            ((DimensionalRift) getParty().getReflection()).memberDead(this);
        }
        stopWaterTask();
        if (!isSalvation() && isOnSiegeField() && isCharmOfCourage()) {
            setCharmOfCourage(false);
        }
        if (getLevel() < 6) {
            final Quest q = QuestManager.getQuest(255);
            if (q != null) {
                processQuestEvent(q.getName(), "CE30", null);
            }
        }
        super.onDeath(killer);
    }

    public void restoreExp() {
        restoreExp(100.0);
    }

    public void restoreExp(final double percent) {
        if (percent == 0.0) {
            return;
        }
        int lostexp = 0;
        final String lostexps = getVar("lostexp");
        if (lostexps != null) {
            lostexp = Integer.parseInt(lostexps);
            unsetVar("lostexp");
        }
        if (lostexp != 0) {
            addExpAndSp((long) (lostexp * percent / 100.0), 0L);
        }
    }

    public void deathPenalty(final Creature killer) {
        if (killer == null) {
            return;
        }
        final boolean atwar = killer.getPlayer() != null && atWarWith(killer.getPlayer());
        double deathPenaltyBonus = getDeathPenalty().getLevel() * Config.ALT_DEATH_PENALTY_C5_EXPERIENCE_PENALTY;
        if (deathPenaltyBonus < 2.0) {
            deathPenaltyBonus = 1.0;
        } else {
            deathPenaltyBonus /= 2.0;
        }
        double percentLost = 8.0;
        final int level = getLevel();
        if (level >= 79) {
            percentLost = 1.0;
        } else if (level >= 78) {
            percentLost = 1.5;
        } else if (level >= 76) {
            percentLost = 2.0;
        } else if (level >= 40) {
            percentLost = 4.0;
        }
        if (Config.ALT_DEATH_PENALTY) {
            percentLost = percentLost * Config.RATE_XP + _pkKills * Config.ALT_PK_DEATH_RATE;
        }
        if (isFestivalParticipant() || atwar) {
            percentLost /= 4.0;
        }
        int lostexp = (int) Math.round((Experience.LEVEL[level + 1] - Experience.LEVEL[level]) * percentLost / 100.0);
        lostexp *= (int) deathPenaltyBonus;
        lostexp = (int) calcStat(Stats.EXP_LOST, lostexp, killer, null);
        if (isOnSiegeField()) {
            final SiegeEvent<?, ?> siegeEvent = getEvent(SiegeEvent.class);
            if (siegeEvent != null) {
                lostexp = 0;
            }
        }
        final long before = getExp();
        addExpAndSp(-lostexp, 0L);
        final long lost = before - getExp();
        if (lost > 0L) {
            setVar("lostexp", String.valueOf(lost), -1L);
        }
    }

    public Request getRequest() {
        return _request;
    }

    public void setRequest(final Request transaction) {
        _request = transaction;
    }

    public boolean isBusy() {
        return isProcessingRequest() || isOutOfControl() || isOlyParticipant() || getTeam() != TeamType.NONE || isInStoreMode() || isInDuel() || getMessageRefusal() || isBlockAll() || isInvisible();
    }

    public boolean isProcessingRequest() {
        return _request != null && _request.isInProgress();
    }

    public boolean isInTrade() {
        return isProcessingRequest() && getRequest().isTypeOf(L2RequestType.TRADE);
    }

    public List<L2GameServerPacket> addVisibleObject(final GameObject object, final Creature dropper) {
        if (isLogoutStarted() || object == null || object.getObjectId() == getObjectId() || !object.isVisible()) {
            return Collections.emptyList();
        }
        return object.addPacketList(this, dropper);
    }

    @Override
    public List<L2GameServerPacket> addPacketList(final Player forPlayer, final Creature dropper) {
        if (isInvisible() && forPlayer.getObjectId() != getObjectId()) {
            return Collections.emptyList();
        }
        if (getPrivateStoreType() != STORE_PRIVATE_NONE && forPlayer.getVarB("notraders")) {
            return Collections.emptyList();
        }
        if (isInObserverMode() && getCurrentRegion() != getObserverRegion() && getObserverRegion() == forPlayer.getCurrentRegion()) {
            return Collections.emptyList();
        }
        final List<L2GameServerPacket> list = new ArrayList<>();
        if (forPlayer.getObjectId() != getObjectId()) {
            list.add(isPolymorphed() ? new NpcInfoPoly(this) : new CharInfo(this));
        }
        if (isSitting() && _sittingObject != null) {
            list.add(new ChairSit(this, _sittingObject));
        }
        if (getPrivateStoreType() != STORE_PRIVATE_NONE) {
            switch (getPrivateStoreType()) {
                case STORE_PRIVATE_BUY:
                    list.add(new PrivateStoreMsgBuy(this));
                    break;
                case STORE_PRIVATE_SELL:
                case STORE_PRIVATE_SELL_PACKAGE:
                    list.add(new PrivateStoreMsgSell(this));
                    break;
                case STORE_PRIVATE_MANUFACTURE:
                    list.add(new RecipeShopMsg(this));
                    break;
            }
            if (forPlayer.isInZonePeace()) {
                return list;
            }
        }
        if (isCastingNow()) {
            final Creature castingTarget = getCastingTarget();
            final Skill castingSkill = getCastingSkill();
            final long animationEndTime = getAnimationEndTime();
            if (castingSkill != null && castingTarget != null && castingTarget.isCreature() && getAnimationEndTime() > 0L) {
                list.add(new MagicSkillUse(this, castingTarget, castingSkill, (int) (animationEndTime - System.currentTimeMillis()), 0L));
            }
        }
        if (isInCombat()) {
            list.add(new AutoAttackStart(getObjectId()));
        }
        list.add(RelationChanged.create(forPlayer, this, forPlayer));
        if (isInBoat()) {
            list.add(getBoat().getOnPacket(this, getInBoatPosition()));
        } else if (isMoving() || isFollowing()) {
            list.add(movePacket());
        }
        return list;
    }

    public List<L2GameServerPacket> removeVisibleObject(final GameObject object, final List<L2GameServerPacket> list) {
        if (isLogoutStarted() || object == null || object.getObjectId() == getObjectId()) {
            return null;
        }
        final List<L2GameServerPacket> result = (list == null) ? object.deletePacketList() : list;
        if (isFollowing() && getFollowTarget() == object) {
            stopMove();
        }
        getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, object);
        return result;
    }

    private void levelSet(final int levels) {
        if (levels > 0) {
            sendPacket(Msg.YOU_HAVE_INCREASED_YOUR_LEVEL);
            broadcastPacket(new SocialAction(getObjectId(), 15));
            setCurrentHpMp(getMaxHp(), getMaxMp());
            setCurrentCp(getMaxCp());
            final Quest q = QuestManager.getQuest(255);
            if (q != null) {
                processQuestEvent(q.getName(), "CE40", null);
            }
        } else if (levels < 0) {
            checkSkills();
        }
        if (isInParty()) {
            getParty().recalculatePartyData();
        }
        if (_clan != null) {
            _clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(this));
        }
        if (_matchingRoom != null) {
            _matchingRoom.broadcastPlayerUpdate(this);
        }
        rewardSkills(true);
    }

    public void checkSkills() {
        if (Config.ALT_WEAK_SKILL_LEARN) {
            return;
        }
        Arrays.stream(getAllSkillsArray()).forEach(sk -> SkillTreeTable.checkSkill(this, sk));
        sendPacket(new SkillList(this));
    }

    public void startTimers() {
        startAutoSaveTask();
        startPcBangPointsTask();
        startBonusTask();
        getInventory().startTimers();
        resumeQuestTimers();
    }

    public void stopAllTimers() {

        stopWaterTask();
        stopBonusTask();
        stopHourlyTask();
        stopKickTask();
        stopPcBangPointsTask();
        stopAutoSaveTask();
        getInventory().stopAllTimers();
        stopQuestTimers();
        stopCustomHeroEndTask();
    }

    @Override
    public Summon getPet() {
        return _summon;
    }

    public void setPet(final Summon summon) {
        boolean isPet = false;
        if (_summon != null && _summon.isPet()) {
            isPet = true;
        }
        unsetVar("pet");
        _summon = summon;
        autoShot();
        if (summon == null) {
            if (isPet) {
                if (isLogoutStarted() && getPetControlItem() != null) {
                    setVar("pet", String.valueOf(getPetControlItem().getObjectId()), -1L);
                }
                setPetControlItem(null);
            }
            getEffectList().stopEffect(4140);
        }
    }

    public void scheduleDelete() {
        long time = 0L;
        if (Config.SERVICES_ENABLE_NO_CARRIER) {
            time = NumberUtils.toInt(getVar("noCarrier"), Config.SERVICES_NO_CARRIER_DEFAULT_TIME);
        }
        scheduleDelete(time * 1000L);
    }

    public void scheduleDelete(final long time) {
        if (isLogoutStarted() || isInOfflineMode()) {
            return;
        }
        broadcastCharInfo();
        ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
            @Override
            public void runImpl() {
                if (!isConnected()) {
                    prepareToLogout();
                    deleteMe();
                }
            }
        }, time);
    }

    @Override
    protected void onDelete() {
        super.onDelete();
        final WorldRegion observerRegion = getObserverRegion();
        if (observerRegion != null) {
            observerRegion.removeObject(this);
        }
        _friendList.notifyFriends(false);
        _inventory.clear();
        _warehouse.clear();
        _summon = null;
        _arrowItem = null;
        _fistsWeaponItem = null;
        _chars = null;
        _enchantScroll = null;
        _lastNpc = HardReferences.emptyRef();
        _observerRegion = null;
    }

    public List<TradeItem> getTradeList() {
        return _tradeList;
    }

    public void setTradeList(final List<TradeItem> list) {
        _tradeList = list;
    }

    public String getSellStoreName() {
        return _sellStoreName;
    }

    public void setSellStoreName(final String name) {
        _sellStoreName = Strings.stripToSingleLine(name);
    }

    public void setSellList(final boolean packageSell, final List<TradeItem> list) {
        if (packageSell) {
            _packageSellList = list;
        } else {
            _sellList = list;
        }
    }

    public List<TradeItem> getSellList() {
        return getSellList(_privatestore == 8);
    }

    public List<TradeItem> getSellList(final boolean packageSell) {
        return packageSell ? _packageSellList : _sellList;
    }

    public String getBuyStoreName() {
        return _buyStoreName;
    }

    public void setBuyStoreName(final String name) {
        _buyStoreName = Strings.stripToSingleLine(name);
    }

    public List<TradeItem> getBuyList() {
        return _buyList;
    }

    public void setBuyList(final List<TradeItem> list) {
        _buyList = list;
    }

    public String getManufactureName() {
        return _manufactureName;
    }

    public void setManufactureName(final String name) {
        _manufactureName = Strings.stripToSingleLine(name);
    }

    public List<ManufactureItem> getCreateList() {
        return _createList;
    }

    public void setCreateList(final List<ManufactureItem> list) {
        _createList = list;
    }

    public boolean isInStoreMode() {
        return _privatestore != 0;
    }

    public int getPrivateStoreType() {
        return _privatestore;
    }

    public void setPrivateStoreType(final int type) {
        _privatestore = type;
        if (type != 0) {
            setVar("storemode", String.valueOf(type), -1L);
        } else {
            unsetVar("storemode");
        }
    }

    @Override
    public Clan getClan() {
        return _clan;
    }

    public void setClan(final Clan clan) {
        if (_clan != clan && _clan != null) {
            unsetVar("canWhWithdraw");
        }
        final Clan oldClan = _clan;
        if (oldClan != null && clan == null) {
            for (final Skill skill : oldClan.getAllSkills()) {
                removeSkill(skill, false);
            }
        }
        if ((_clan = clan) == null) {
            _pledgeType = -128;
            _pledgeClass = 0;
            _powerGrade = 0;
            _apprentice = 0;
            getInventory().validateItems();
            return;
        }
        if (!clan.isAnyMember(getObjectId())) {
            setClan(null);
            if (!isNoble()) {
                setTitle("");
            }
        }
    }

    public SubUnit getSubUnit() {
        return (_clan == null) ? null : _clan.getSubUnit(_pledgeType);
    }

    public ClanHall getClanHall() {
        final int id = (_clan != null) ? _clan.getHasHideout() : 0;
        return ResidenceHolder.getInstance().getResidence(ClanHall.class, id);
    }

    public Castle getCastle() {
        final int id = (_clan != null) ? _clan.getCastle() : 0;
        return ResidenceHolder.getInstance().getResidence(Castle.class, id);
    }

    public Alliance getAlliance() {
        return (_clan == null) ? null : _clan.getAlliance();
    }

    public boolean isClanLeader() {
        return _clan != null && getObjectId() == _clan.getLeaderId();
    }

    public boolean isAllyLeader() {
        return getAlliance() != null && getAlliance().getLeader().getLeaderId() == getObjectId();
    }

    @Override
    public void reduceArrowCount() {
        sendPacket(SystemMsg.YOU_CAREFULLY_NOCK_AN_ARROW);
        if (!getInventory().destroyItemByObjectId(getInventory().getPaperdollObjectId(8), 1L)) {
            getInventory().setPaperdollItem(8, null);
            _arrowItem = null;
        }
    }

    protected boolean checkAndEquipArrows() {
        if (getInventory().getPaperdollItem(8) == null) {
            final ItemInstance activeWeapon = getActiveWeaponInstance();
            if (activeWeapon != null && activeWeapon.getItemType() == WeaponType.BOW) {
                _arrowItem = getInventory().findArrowForBow(activeWeapon.getTemplate());
            }
            if (_arrowItem != null) {
                getInventory().setPaperdollItem(8, _arrowItem);
            }
        } else {
            _arrowItem = getInventory().getPaperdollItem(8);
        }
        return _arrowItem != null;
    }

    public long getUptime() {
        return System.currentTimeMillis() - _uptime;
    }

    public void setUptime(final long time) {
        _uptime = time;
    }

    public boolean isInParty() {
        return _party != null;
    }

    public void joinParty(final Party party) {
        if (party != null) {
            party.addPartyMember(this);
        }
    }

    public void leaveParty() {
        if (isInParty()) {
            _party.removePartyMember(this, false);
        }
    }

    public Party getParty() {
        return _party;
    }

    public void setParty(final Party party) {
        _party = party;
    }

    public Location getLastPartyPosition() {
        return _lastPartyPosition;
    }

    public void setLastPartyPosition(final Location loc) {
        _lastPartyPosition = loc;
    }

    public boolean isGM() {
        return _playerAccess != null && _playerAccess.IsGM;
    }

    @Override
    public int getAccessLevel() {
        return _accessLevel;
    }

    public void setAccessLevel(final int level) {
        _accessLevel = level;
    }

    public PlayerAccess getPlayerAccess() {
        return _playerAccess;
    }

    public void setPlayerAccess(final PlayerAccess pa) {
        if (pa != null) {
            _playerAccess = pa;
        } else {
            _playerAccess = new PlayerAccess();
        }
        setAccessLevel((isGM() || _playerAccess.Menu) ? 100 : 0);
    }

    @Override
    public double getLevelMod() {
        return (89.0 + getLevel()) / 100.0;
    }

    @Override
    public void updateStats() {
        if (entering || isLogoutStarted()) {
            return;
        }
        refreshOverloaded();
        refreshExpertisePenalty();
        super.updateStats();
    }

    @Override
    public void sendChanges() {
        if (entering || isLogoutStarted()) {
            return;
        }
        super.sendChanges();
    }

    public void updateKarma(final boolean flagChanged) {
        sendStatusUpdate(true, true, 27);
        if (flagChanged) {
            broadcastRelationChanged();
        }
    }

    public boolean isOnline() {
        return _isOnline;
    }

    public void setIsOnline(final boolean isOnline) {
        _isOnline = isOnline;
    }

    public void setOnlineStatus(final boolean isOnline) {
        _isOnline = isOnline;
        updateOnlineStatus();
    }

    private void updateOnlineStatus() {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("UPDATE characters SET online=?, lastAccess=? WHERE obj_id=?");
            statement.setInt(1, (isOnline() && !isInOfflineMode()) ? 1 : 0);
            statement.setLong(2, System.currentTimeMillis() / 1000L);
            statement.setInt(3, getObjectId());
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void increaseKarma(final long add_karma) {
        final boolean flagChanged = _karma == 0;
        long new_karma = _karma + add_karma;
        if (new_karma > 2147483647L) {
            new_karma = 2147483647L;
        }
        if (_karma == 0 && new_karma > 0L) {
            if (_pvpFlag > 0) {
                _pvpFlag = 0;
                if (_PvPRegTask != null) {
                    _PvPRegTask.cancel(true);
                    _PvPRegTask = null;
                }
                sendStatusUpdate(true, true, 26);
            }
            _karma = (int) new_karma;
        } else {
            _karma = (int) new_karma;
        }
        updateKarma(flagChanged);
    }

    public void decreaseKarma(final int i) {
        final boolean flagChanged = _karma > 0;
        _karma -= i;
        if (_karma <= 0) {
            _karma = 0;
            updateKarma(flagChanged);
        } else {
            updateKarma(false);
        }
    }

    private void loadPremiumItemList() {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT itemNum, itemId, itemCount, itemSender FROM character_premium_items WHERE charId=?");
            statement.setInt(1, getObjectId());
            rs = statement.executeQuery();
            while (rs.next()) {
                final int itemNum = rs.getInt("itemNum");
                final int itemId = rs.getInt("itemId");
                final long itemCount = rs.getLong("itemCount");
                final String itemSender = rs.getString("itemSender");
                final PremiumItem item = new PremiumItem(itemId, itemCount, itemSender);
                _premiumItems.put(itemNum, item);
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement, rs);
        }
    }

    public void updatePremiumItem(final int itemNum, final long newcount) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("UPDATE character_premium_items SET itemCount=? WHERE charId=? AND itemNum=?");
            statement.setLong(1, newcount);
            statement.setInt(2, getObjectId());
            statement.setInt(3, itemNum);
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void deletePremiumItem(final int itemNum) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("DELETE FROM character_premium_items WHERE charId=? AND itemNum=?");
            statement.setInt(1, getObjectId());
            statement.setInt(2, itemNum);
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public Map<Integer, PremiumItem> getPremiumItemList() {
        return _premiumItems;
    }

    public void store(final boolean fast) {
        if (!_storeLock.tryLock()) {
            return;
        }
        try {
            Connection con = null;
            PreparedStatement statement = null;
            try {
                con = DatabaseFactory.getInstance().getConnection();
                statement = con.prepareStatement("UPDATE characters SET face=?,hairStyle=?,hairColor=?,x=?,y=?,z=?,karma=?,pvpkills=?,pkkills=?,rec_have=?,rec_left=?,rec_bonus_time=?,hunting_bonus_time=?,rec_tick_cnt=?,hunting_bonus=?,clanid=?,deletetime=?,title=?,accesslevel=?,online=?,leaveclan=?,deleteclan=?,nochannel=?,onlinetime=?,pledge_type=?,pledge_rank=?,lvl_joined_academy=?,apprentice=?,key_bindings=?,pcBangPoints=?,char_name=?,bookmarks=? WHERE obj_Id=? LIMIT 1");
                statement.setInt(1, getFace());
                statement.setInt(2, getHairStyle());
                statement.setInt(3, getHairColor());
                if (_stablePoint == null) {
                    statement.setInt(4, getX());
                    statement.setInt(5, getY());
                    statement.setInt(6, getZ());
                } else {
                    statement.setInt(4, _stablePoint.x);
                    statement.setInt(5, _stablePoint.y);
                    statement.setInt(6, _stablePoint.z);
                }
                statement.setInt(7, getKarma());
                statement.setInt(8, getPvpKills());
                statement.setInt(9, getPkKills());
                statement.setInt(10, getReceivedRec());
                statement.setInt(11, getGivableRec());
                statement.setInt(12, 0);
                statement.setInt(13, 0);
                statement.setInt(14, 0);
                statement.setInt(15, 0);
                statement.setInt(16, getClanId());
                statement.setInt(17, getDeleteTimer());
                statement.setString(18, _title);
                statement.setInt(19, _accessLevel);
                statement.setInt(20, (isOnline() && !isInOfflineMode()) ? 1 : 0);
                statement.setLong(21, getLeaveClanTime() / 1000L);
                statement.setLong(22, getDeleteClanTime() / 1000L);
                statement.setLong(23, (_NoChannel > 0L) ? (getNoChannelRemained() / 1000L) : _NoChannel);
                statement.setInt(24, (int) ((_onlineBeginTime > 0L) ? ((_onlineTime + System.currentTimeMillis() - _onlineBeginTime) / 1000L) : (_onlineTime / 1000L)));
                statement.setInt(25, getPledgeType());
                statement.setInt(26, getPowerGrade());
                statement.setInt(27, getLvlJoinedAcademy());
                statement.setInt(28, getApprentice());
                statement.setBytes(29, getKeyBindings());
                statement.setInt(30, getPcBangPoints());
                statement.setString(31, getName());
                statement.setInt(32, 0);
                statement.setInt(33, getObjectId());
                statement.executeUpdate();
                GameStats.increaseUpdatePlayerBase();
                if (!fast) {
                    EffectsDAO.getInstance().insert(this);
                    CharacterGroupReuseDAO.getInstance().insert(this);
                    storeDisableSkills();
                    storeBlockList();
                }
                storeCharSubClasses();
                storeRecommendedCharacters();
            } catch (Exception e) {
                LOGGER.error("Could not store char data: " + this + "!", e);
            } finally {
                DbUtils.closeQuietly(con, statement);
            }
        } finally {
            _storeLock.unlock();
        }
    }

    public Skill addSkill(final Skill newSkill, final boolean store) {
        if (newSkill == null) {
            return null;
        }
        final Skill oldSkill = super.addSkill(newSkill);
        if (newSkill.equals(oldSkill)) {
            return oldSkill;
        }
        if (store && !isPhantom()) {
            storeSkill(newSkill, oldSkill);
        }
        getListeners().onPlayerSkillAdd(this, newSkill, oldSkill);
        return oldSkill;
    }

    public Skill removeSkill(final Skill skill, final boolean fromDB) {
        if (skill == null) {
            return null;
        }
        return removeSkill(skill.getId(), fromDB);
    }

    public Skill removeSkill(final int id, final boolean fromDB) {
        final Skill oldSkill = super.removeSkillById(id);
        if (!fromDB) {
            return oldSkill;
        }
        if (oldSkill != null) {
            Connection con = null;
            PreparedStatement statement = null;
            try {
                con = DatabaseFactory.getInstance().getConnection();
                statement = con.prepareStatement("DELETE FROM character_skills WHERE skill_id=? AND char_obj_id=? AND class_index=?");
                statement.setInt(1, oldSkill.getId());
                statement.setInt(2, getObjectId());
                statement.setInt(3, getActiveClassId());
                statement.execute();
            } catch (Exception e) {
                LOGGER.error("Could not delete skill!", e);
            } finally {
                DbUtils.closeQuietly(con, statement);
            }
        }
        return oldSkill;
    }

    private void storeSkill(final Skill newSkill, final Skill oldSkill) {
        if (newSkill == null) {
            LOGGER.warn("could not store new skill. its NULL");
            return;
        }
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("REPLACE INTO character_skills (char_obj_id,skill_id,skill_level,class_index) values(?,?,?,?)");
            statement.setInt(1, getObjectId());
            statement.setInt(2, newSkill.getId());
            statement.setInt(3, newSkill.getLevel());
            statement.setInt(4, getActiveClassId());
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("Error could not store skills!", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    private void restoreSkills() {
        restoreSkills(getActiveClassId());
    }

    private void restoreSkills(final int classId) {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT skill_id,skill_level FROM character_skills WHERE char_obj_id=? AND class_index=?");
            statement.setInt(1, getObjectId());
            statement.setInt(2, classId);
            rset = statement.executeQuery();
            while (rset.next()) {
                final int id = rset.getInt("skill_id");
                final int level = rset.getInt("skill_level");
                final Skill skill = SkillTable.getInstance().getInfo(id, level);
                if (skill == null) {
                    continue;
                }
                if (!isGM() && !Config.ALT_WEAK_SKILL_LEARN && !SkillAcquireHolder.getInstance().isSkillPossible(this, skill)) {
                    LOGGER.warn("Skill " + skill + " not possible for player " + toString() + " with classId " + getActiveClassId());
                    removeSkill(skill, true);
                    removeSkillFromShortCut(skill.getId());
                } else {
                    super.addSkill(skill);
                }
            }
            if (getActiveClassId() != classId) {
                return;
            }
            if (isNoble()) {
                updateNobleSkills();
            }
            if (_hero && (getBaseClassId() == getActiveClassId() || Config.ALT_ALLOW_HERO_SKILLS_ON_SUB_CLASS)) {
                HeroManager.addSkills(this);
            }
            if (_clan != null) {
                _clan.addSkillsQuietly(this);
                if (_clan.getLeaderId() == getObjectId() && _clan.getLevel() >= 5) {
                    SiegeUtils.addSiegeSkills(this);
                }
            }
            ClassId activeClassId = null;
            for (final ClassId clsId : ClassId.VALUES) {
                if (clsId.getId() == getActiveClassId()) {
                    activeClassId = clsId;
                }
            }
            switch (activeClassId) {
                case dwarvenFighter:
                case scavenger:
                case bountyHunter:
                case artisan:
                case warsmith:
                case fortuneSeeker:
                case maestro: {
                    addSkill(SkillTable.getInstance().getInfo(1321, 1));
                    break;
                }
            }
            addSkill(SkillTable.getInstance().getInfo(1322, 1));
            if (Config.UNSTUCK_SKILL && getSkillLevel(1050) < 0) {
                addSkill(SkillTable.getInstance().getInfo(2099, 1));
            }
            if (Config.BLOCK_BUFF_SKILL) {
                addSkill(SkillTable.getInstance().getInfo(5088, 1));
            }
            if (Config.NOBLES_BUFF_SKILL) {
                addSkill(SkillTable.getInstance().getInfo(1323, 1));
            }
            getListeners().onPlayerSkillsRestored(this);
        } catch (Exception e) {
            LOGGER.warn("Could not restore skills for player objId: " + getObjectId());
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
    }

    public void storeDisableSkills() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseFactory.getInstance().getConnection();
            pstmt = conn.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id = ? AND (class_index=? OR class_index=-1) AND `end_time` < ?");
            pstmt.setInt(1, getObjectId());
            pstmt.setInt(2, getActiveClassId());
            pstmt.setLong(3, System.currentTimeMillis());
            pstmt.executeUpdate();
            DbUtils.close(pstmt);
            if (_skillReuses.isEmpty()) {
                return;
            }
            pstmt = conn.prepareStatement("REPLACE INTO `character_skills_save`(`char_obj_id`, `skill_id`, `skill_level`, `class_index`, `end_time`, `reuse_delay_org`) VALUES\t(?,?,?,?,?,?)");
            final ConcurrentMap<Integer, TimeStamp> skillReuses;
            synchronized (_skillReuses) {
                skillReuses = new ConcurrentHashMap<>(_skillReuses);
            }
            for (final TimeStamp timeStamp : skillReuses.values()) {
                final Skill skill = SkillTable.getInstance().getInfo(timeStamp.getId(), timeStamp.getLevel());
                if (skill == null) {
                    continue;
                }
                pstmt.setInt(1, getObjectId());
                pstmt.setInt(2, skill.getId());
                pstmt.setInt(3, skill.getLevel());
                pstmt.setInt(4, skill.isSharedClassReuse() ? -1 : getActiveClassId());
                pstmt.setLong(5, timeStamp.getEndTime());
                pstmt.setLong(6, timeStamp.getReuseBasic());
                pstmt.executeUpdate();
            }
        } catch (Exception e) {
            LOGGER.warn("Could not store disable skills data: " + e);
        } finally {
            DbUtils.closeQuietly(conn, pstmt);
        }
    }

    public void restoreDisableSkills() {
        _skillReuses.clear();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rset = null;
        try {
            conn = DatabaseFactory.getInstance().getConnection();
            pstmt = conn.prepareStatement("SELECT skill_id, skill_level, end_time, reuse_delay_org FROM character_skills_save WHERE char_obj_id=? AND (class_index=? OR class_index=-1)");
            pstmt.setInt(1, getObjectId());
            pstmt.setInt(2, getActiveClassId());
            rset = pstmt.executeQuery();
            while (rset.next()) {
                final int skillId = rset.getInt("skill_id");
                final int skillLevel = rset.getInt("skill_level");
                final long endTime = rset.getLong("end_time");
                final long rDelayOrg = rset.getLong("reuse_delay_org");
                final long curTime = System.currentTimeMillis();
                final Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
                if (skill != null && endTime - curTime > 500L) {
                    _skillReuses.put(skill.hashCode(), new TimeStamp(skill, endTime, rDelayOrg));
                }
            }
            DbUtils.close(pstmt);
            pstmt = conn.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id = ? AND (class_index=? OR class_index=-1) AND `end_time` < ?");
            pstmt.setInt(1, getObjectId());
            pstmt.setInt(2, getActiveClassId());
            pstmt.setLong(3, System.currentTimeMillis());
            pstmt.executeUpdate();
        } catch (Exception e) {
            LOGGER.error("Could not restore active skills data!", e);
        } finally {
            DbUtils.closeQuietly(conn, pstmt, rset);
        }
    }

    private void restoreHenna() {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("select slot, symbol_id from character_hennas where char_obj_id=? AND class_index=?");
            statement.setInt(1, getObjectId());
            statement.setInt(2, getActiveClassId());
            rset = statement.executeQuery();
            for (int i = 0; i < 3; ++i) {
                _henna[i] = null;
            }
            while (rset.next()) {
                final int slot = rset.getInt("slot");
                if (slot >= 1) {
                    if (slot > 3) {
                        continue;
                    }
                    final int symbol_id = rset.getInt("symbol_id");
                    if (symbol_id == 0) {
                        continue;
                    }
                    final Henna tpl = HennaHolder.getInstance().getHenna(symbol_id);
                    if (tpl == null) {
                        continue;
                    }
                    _henna[slot - 1] = tpl;
                }
            }
        } catch (Exception e) {
            LOGGER.warn("could not restore henna: " + e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
        recalcHennaStats();
    }

    public int getHennaEmptySlots() {
        int totalSlots = 1 + getClassId().level();
        for (int i = 0; i < 3; ++i) {
            if (_henna[i] != null) {
                totalSlots--;
            }
        }
        if (totalSlots <= 0) {
            return 0;
        }
        return totalSlots;
    }

    public boolean removeHenna(int slot) {
        if (slot < 1 || slot > 3) {
            return false;
        }
        --slot;
        if (_henna[slot] == null) {
            return false;
        }
        final Henna henna = _henna[slot];
        final int dyeID = henna.getDyeId();
        _henna[slot] = null;
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("DELETE FROM character_hennas where char_obj_id=? and slot=? and class_index=?");
            statement.setInt(1, getObjectId());
            statement.setInt(2, slot + 1);
            statement.setInt(3, getActiveClassId());
            statement.execute();
        } catch (Exception e) {
            LOGGER.warn("could not remove char henna: " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
        recalcHennaStats();
        sendPacket(new HennaInfo(this));
        sendUserInfo(true);
        ItemFunctions.addItem(this, dyeID, henna.getDrawCount() / 2L, true);
        return true;
    }

    public boolean addHenna(final Henna henna) {
        if (getHennaEmptySlots() == 0) {
            sendPacket(SystemMsg.NO_SLOT_EXISTS_TO_DRAW_THE_SYMBOL);
            return false;
        }
        for (int i = 0; i < 3; ++i) {
            if (_henna[i] == null) {
                _henna[i] = henna;
                recalcHennaStats();
                Connection con = null;
                PreparedStatement statement = null;
                try {
                    con = DatabaseFactory.getInstance().getConnection();
                    statement = con.prepareStatement("INSERT INTO `character_hennas` (char_obj_id, symbol_id, slot, class_index) VALUES (?,?,?,?)");
                    statement.setInt(1, getObjectId());
                    statement.setInt(2, henna.getSymbolId());
                    statement.setInt(3, i + 1);
                    statement.setInt(4, getActiveClassId());
                    statement.execute();
                } catch (Exception e) {
                    LOGGER.warn("could not save char henna: " + e);
                } finally {
                    DbUtils.closeQuietly(con, statement);
                }
                sendPacket(new HennaInfo(this));
                sendUserInfo(true);
                return true;
            }
        }
        return false;
    }

    private void recalcHennaStats() {
        _hennaINT = 0;
        _hennaSTR = 0;
        _hennaCON = 0;
        _hennaMEN = 0;
        _hennaWIT = 0;
        _hennaDEX = 0;
        for (int i = 0; i < 3; ++i) {
            final Henna henna = _henna[i];
            if (henna != null) {
                if (henna.isForThisClass(this)) {
                    _hennaINT += henna.getStatINT();
                    _hennaSTR += henna.getStatSTR();
                    _hennaMEN += henna.getStatMEN();
                    _hennaCON += henna.getStatCON();
                    _hennaWIT += henna.getStatWIT();
                    _hennaDEX += henna.getStatDEX();
                }
            }
        }
        if (_hennaINT > 5) {
            _hennaINT = 5;
        }
        if (_hennaSTR > 5) {
            _hennaSTR = 5;
        }
        if (_hennaMEN > 5) {
            _hennaMEN = 5;
        }
        if (_hennaCON > 5) {
            _hennaCON = 5;
        }
        if (_hennaWIT > 5) {
            _hennaWIT = 5;
        }
        if (_hennaDEX > 5) {
            _hennaDEX = 5;
        }
    }

    public Henna getHenna(final int slot) {
        if (slot < 1 || slot > 3) {
            return null;
        }
        return _henna[slot - 1];
    }

    public int getHennaStatINT() {
        return _hennaINT;
    }

    public int getHennaStatSTR() {
        return _hennaSTR;
    }

    public int getHennaStatCON() {
        return _hennaCON;
    }

    public int getHennaStatMEN() {
        return _hennaMEN;
    }

    public int getHennaStatWIT() {
        return _hennaWIT;
    }

    public int getHennaStatDEX() {
        return _hennaDEX;
    }

    @Override
    public boolean consumeItem(final int itemConsumeId, final long itemCount) {
        if (getInventory().destroyItemByItemId(itemConsumeId, itemCount)) {
            sendPacket(SystemMessage2.removeItems(itemConsumeId, itemCount));
            return true;
        }
        return false;
    }

    @Override
    public boolean consumeItemMp(final int itemId, final int mp) {
        final ItemInstance[] paperdollItems = getInventory().getPaperdollItems();
        final int length = paperdollItems.length;
        int i = 0;
        while (i < length) {
            final ItemInstance item = paperdollItems[i];
            if (item != null && item.getItemId() == itemId) {
                final int newMp = item.getDuration() - mp;
                if (newMp >= 0) {
                    item.setDuration(newMp);
                    sendPacket(new InventoryUpdate().addModifiedItem(item));
                    return true;
                }
                break;
            } else {
                ++i;
            }
        }
        return false;
    }

    @Override
    public boolean isMageClass() {
        final ClassId classId = getClassId();
        return classId.isMage();
    }

    public boolean isMounted() {
        return _mountNpcId > 0;
    }

    public final boolean isRiding() {
        return _riding;
    }

    public final void setRiding(final boolean mode) {
        _riding = mode;
    }

    public boolean checkLandingState() {
        if (isInZone(ZoneType.no_landing)) {
            return false;
        }
        final SiegeEvent<?, ?> siege = getEvent(SiegeEvent.class);
        if (siege != null) {
            final Residence unit = siege.getResidence();
            return unit != null && getClan() != null && isClanLeader() && getClan().getCastle() == unit.getId();
        }
        return true;
    }

    public void setMount(final int npcId, final int obj_id, final int level) {
        if (isCursedWeaponEquipped()) {
            return;
        }
        switch (npcId) {
            case 0: {
                setFlying(false);
                setRiding(false);
                if (getTransformation() > 0) {
                    setTransformation(0);
                }
                removeSkillById(4289);
                getEffectList().stopEffect(4258);
                break;
            }
            case 12526:
            case 12527:
            case 12528:
            case 16038:
            case 16039:
            case 16040:
            case 16068: {
                setRiding(true);
                break;
            }
            case 12621: {
                setFlying(true);
                setLoc(getLoc().changeZ(32));
                addSkill(SkillTable.getInstance().getInfo(4289, 1), false);
                break;
            }
            case 16037:
            case 16041:
            case 16042: {
                setRiding(true);
                break;
            }
        }
        if (npcId > 0) {
            unEquipWeapon();
        }
        _mountNpcId = npcId;
        _mountObjId = obj_id;
        _mountLevel = level;
        broadcastUserInfo(true);
        broadcastPacket(new Ride(this));
        broadcastUserInfo(true);
        sendPacket(new SkillList(this));
    }

    public void unEquipWeapon() {
        ItemInstance wpn = getSecondaryWeaponInstance();
        if (wpn != null) {
            sendDisarmMessage(wpn);
            getInventory().unEquipItem(wpn);
        }
        wpn = getActiveWeaponInstance();
        if (wpn != null) {
            sendDisarmMessage(wpn);
            getInventory().unEquipItem(wpn);
        }
        abortAttack(true, true);
        abortCast(true, true);
    }

    @Override
    public int getSpeed(int baseSpeed) {
        if (isMounted()) {
            final PetData petData = PetDataTable.getInstance().getInfo(_mountNpcId, _mountLevel);
            int speed = 187;
            if (petData != null) {
                speed = petData.getSpeed();
            }
            double mod = 1.0;
            final int level = getLevel();
            if (_mountLevel > level && level - _mountLevel > 10) {
                mod = 0.5;
            }
            baseSpeed = (int) (mod * speed);
        }
        return super.getSpeed(baseSpeed);
    }

    public int getMountNpcId() {
        return _mountNpcId;
    }

    public int getMountObjId() {
        return _mountObjId;
    }

    public int getMountLevel() {
        return _mountLevel;
    }

    public void sendDisarmMessage(final ItemInstance wpn) {
        if (wpn.getEnchantLevel() > 0) {
            final SystemMessage sm = new SystemMessage(1064);
            sm.addNumber(wpn.getEnchantLevel());
            sm.addItemName(wpn.getItemId());
            sendPacket(sm);
        } else {
            final SystemMessage sm = new SystemMessage(417);
            sm.addItemName(wpn.getItemId());
            sendPacket(sm);
        }
    }

    public WarehouseType getUsingWarehouseType() {
        return _usingWHType;
    }

    public void setUsingWarehouseType(final WarehouseType type) {
        _usingWHType = type;
    }

    public Collection<EffectCubic> getCubics() {
        return _cubics == null ? Collections.emptyList() : _cubics.values();
    }

    public void addCubic(final EffectCubic cubic) {
        if (_cubics == null) {
            _cubics = new ConcurrentHashMap<>(3);
        }
        _cubics.put(cubic.getId(), cubic);
    }

    public void removeCubic(final int id) {
        if (_cubics != null) {
            _cubics.remove(id);
        }
    }

    public EffectCubic getCubic(final int id) {
        return (_cubics == null) ? null : _cubics.get(id);
    }

    @Override
    public String toString() {
        return getName() + "[" + getObjectId() + "]";
    }

    public int getEnchantEffect() {
        final ItemInstance wpn = getActiveWeaponInstance();
        if (wpn == null) {
            return 0;
        }
        return Math.min(127, wpn.getEnchantLevel());
    }

    public NpcInstance getLastNpc() {
        return _lastNpc.get();
    }

    public void setLastNpc(final NpcInstance npc) {
        if (npc == null) {
            _lastNpc = HardReferences.emptyRef();
        } else {
            _lastNpc = npc.getRef();
        }
    }

    public MultiSellListContainer getMultisell() {
        return _multisell;
    }

    public void setMultisell(final MultiSellListContainer multisell) {
        _multisell = multisell;
    }

    public boolean isFestivalParticipant() {
        return getReflection() instanceof DarknessFestival;
    }

    @Override
    public boolean unChargeShots(final boolean spirit) {
        final ItemInstance weapon = getActiveWeaponInstance();
        if (weapon == null) {
            return false;
        }
        if (spirit) {
            weapon.setChargedSpiritshot(0);
        } else {
            weapon.setChargedSoulshot(0);
        }
        autoShot();
        return true;
    }

    public boolean unChargeFishShot() {
        final ItemInstance weapon = getActiveWeaponInstance();
        if (weapon == null) {
            return false;
        }
        weapon.setChargedFishshot(false);
        autoShot();
        return true;
    }

    public void autoShot() {
        for (final Integer shotId : _activeSoulShots) {
            final ItemInstance item = getInventory().getItemByItemId(shotId);
            if (item == null) {
                removeAutoSoulShot(shotId);
            } else {
                if (!item.getTemplate().testCondition(this, item, false)) {
                    continue;
                }
                final IItemHandler handler = item.getTemplate().getHandler();
                if (handler == null) {
                    continue;
                }
                handler.useItem(this, item, false);
            }
        }
    }

    public boolean getChargedFishShot() {
        final ItemInstance weapon = getActiveWeaponInstance();
        return weapon != null && weapon.getChargedFishshot();
    }

    @Override
    public boolean getChargedSoulShot() {
        final ItemInstance weapon = getActiveWeaponInstance();
        return weapon != null && weapon.getChargedSoulshot() == 1;
    }

    @Override
    public int getChargedSpiritShot() {
        final ItemInstance weapon = getActiveWeaponInstance();
        if (weapon == null) {
            return 0;
        }
        return weapon.getChargedSpiritshot();
    }

    public void addAutoSoulShot(final Integer itemId) {
        _activeSoulShots.add(itemId);
    }

    public void removeAutoSoulShot(final Integer itemId) {
        _activeSoulShots.remove(itemId);
    }

    public Set<Integer> getAutoSoulShot() {
        return _activeSoulShots;
    }

    @Override
    public InvisibleType getInvisibleType() {
        return _invisibleType;
    }

    public void setInvisibleType(final InvisibleType vis) {
        _invisibleType = vis;
    }

    public int getClanPrivileges() {
        if (_clan == null) {
            return 0;
        }
        if (isClanLeader()) {
            return 0x7ffffe;
        }
        if (_powerGrade < 1 || _powerGrade > 9) {
            return 0;
        }
        final RankPrivs privs = _clan.getRankPrivs(_powerGrade);
        if (privs != null) {
            return privs.getPrivs();
        }
        return 0;
    }

    public void teleToClosestTown() {
        teleToLocation(TeleportUtils.getRestartLocation(this, RestartType.TO_VILLAGE), ReflectionManager.DEFAULT);
    }

    public void teleToCastle() {
        teleToLocation(TeleportUtils.getRestartLocation(this, RestartType.TO_CASTLE), ReflectionManager.DEFAULT);
    }

    public void teleToClanhall() {
        teleToLocation(TeleportUtils.getRestartLocation(this, RestartType.TO_CLANHALL), ReflectionManager.DEFAULT);
    }

    @Override
    public void sendMessage(final CustomMessage message) {
        sendMessage(message.toString());
    }

    @Override
    public void teleToLocation(final int x, final int y, final int z, final int refId) {
        if (isDeleted()) {
            return;
        }
        super.teleToLocation(x, y, z, refId);
    }

    @Override
    public boolean onTeleported() {
        if (!super.onTeleported()) {
            return false;
        }
        if (isFakeDeath()) {
            breakFakeDeath();
        }
        if (isInBoat()) {
            setLoc(getBoat().getLoc());
        }
        setNonAggroTime(System.currentTimeMillis() + Config.NONAGGRO_TIME_ONTELEPORT);
        spawnMe();
        setLastClientPosition(getLoc());
        setLastServerPosition(getLoc());
        if (isPendingRevive()) {
            doRevive();
        }
        sendActionFailed();
        getAI().notifyEvent(CtrlEvent.EVT_TELEPORTED);
        if (isLockedTarget() && getTarget() != null) {
            sendPacket(new MyTargetSelected(getTarget().getObjectId(), 0));
        }
        sendUserInfo(true);
        if (getPet() != null) {
            getPet().teleportToOwner();
        }
        if (Config.ALT_TELEPORT_PROTECTION && !isInZone(ZoneType.peace_zone) && !isInZone(ZoneType.SIEGE) && !isInZone(ZoneType.offshore) && !isOlyParticipant()) {
            setAfterTeleportPortectionTime(System.currentTimeMillis() + 1000L * Config.ALT_TELEPORT_PROTECTION_TIME);
            sendMessage(new CustomMessage("alt.teleport_protect", this, Config.ALT_TELEPORT_PROTECTION_TIME));
        }
        return true;
    }

    public boolean enterObserverMode(final Location loc) {
        final WorldRegion observerRegion = World.getRegion(loc);
        if (observerRegion == null) {
            return false;
        }
        if (!_observerMode.compareAndSet(0, 1)) {
            return false;
        }
        setTarget(null);
        stopMove();
        sitDown(null);
        setFlying(true);
        World.removeObjectsFromPlayer(this);
        setObserverRegion(observerRegion);
        broadcastCharInfo();
        sendPacket(new ObserverStart(loc));
        return true;
    }

    public void appearObserverMode() {
        if (!_observerMode.compareAndSet(1, 3)) {
            return;
        }
        final WorldRegion currentRegion = getCurrentRegion();
        final WorldRegion observerRegion = getObserverRegion();
        if (!observerRegion.equals(currentRegion)) {
            observerRegion.addObject(this);
        }
        World.showObjectsToPlayer(this);
        if (isOlyObserver()) {
            for (final Player p : getOlyObservingStadium().getPlayers()) {
                if (p.isOlyCompetitionStarted()) {
                    sendPacket(new ExOlympiadUserInfo(p));
                }
            }
        }
    }

    public void leaveObserverMode() {
        if (!_observerMode.compareAndSet(3, 2)) {
            return;
        }
        final WorldRegion currentRegion = getCurrentRegion();
        final WorldRegion observerRegion = getObserverRegion();
        if (!observerRegion.equals(currentRegion)) {
            observerRegion.removeObject(this);
        }
        World.removeObjectsFromPlayer(this);
        setObserverRegion(null);
        setTarget(null);
        stopMove();
        sendPacket(new ObserverEnd(getLoc()));
    }

    public void returnFromObserverMode() {
        if (!_observerMode.compareAndSet(2, 0)) {
            return;
        }
        setLastClientPosition(null);
        setLastServerPosition(null);
        unblock();
        standUp();
        setFlying(false);
        broadcastCharInfo();
        World.showObjectsToPlayer(this);
    }

    public void enterOlympiadObserverMode(final OlympiadStadium olympiadStadium) {
        final WorldRegion observerRegion = World.getRegion(olympiadStadium.getObservingLoc());
        if (observerRegion == null || _olyObserveOlympiadStadium != null) {
            return;
        }
        if (!_observerMode.compareAndSet(0, 1)) {
            return;
        }
        setTarget(null);
        setLastNpc(null);
        stopMove();
        _olyObserveOlympiadStadium = olympiadStadium;
        World.removeObjectsFromPlayer(this);
        setObserverRegion(observerRegion);
        block();
        broadcastCharInfo();
        setReflection(olympiadStadium);
        setLastClientPosition(null);
        setLastServerPosition(null);
        sendPacket(new ExOlympiadMode(3), new TeleportToLocation(this, olympiadStadium.getObservingLoc()));
    }

    public void switchOlympiadObserverArena(final OlympiadStadium olympiadStadium) {
        if (_olyObserveOlympiadStadium == null || olympiadStadium == _olyObserveOlympiadStadium) {
            return;
        }
        final WorldRegion oldObserverRegion = World.getRegion(_olyObserveOlympiadStadium.getObservingLoc());
        if (!_observerMode.compareAndSet(3, 0)) {
            return;
        }
        if (oldObserverRegion != null) {
            oldObserverRegion.removeObject(this);
            oldObserverRegion.removeFromPlayers(this);
        }
        _olyObserveOlympiadStadium = null;
        World.removeObjectsFromPlayer(this);
        sendPacket(new ExOlympiadMode(0));
        enterOlympiadObserverMode(olympiadStadium);
    }

    public void leaveOlympiadObserverMode() {
        if (_olyObserveOlympiadStadium == null) {
            return;
        }
        if (!_observerMode.compareAndSet(3, 2)) {
            return;
        }
        final WorldRegion currentRegion = getCurrentRegion();
        final WorldRegion observerRegion = getObserverRegion();
        if (observerRegion != null && currentRegion != null && !observerRegion.equals(currentRegion)) {
            observerRegion.removeObject(this);
        }
        World.removeObjectsFromPlayer(this);
        setObserverRegion(null);
        _olyObserveOlympiadStadium = null;
        setTarget(null);
        stopMove();
        sendPacket(new ExOlympiadMode(0));
        setReflection(ReflectionManager.DEFAULT);
        sendPacket(new TeleportToLocation(this, getLoc()));
    }

    public boolean isOlyObserver() {
        return _olyObserveOlympiadStadium != null;
    }

    public OlympiadStadium getOlyObservingStadium() {
        return _olyObserveOlympiadStadium;
    }

    @Override
    public boolean isInObserverMode() {
        return _observerMode.get() > 0;
    }

    public int getObserverMode() {
        return _observerMode.get();
    }

    public OlympiadPlayer getOlyParticipant() {
        return _olyOlympiadPlayer;
    }

    @Override
    public boolean isOlyParticipant() {
        return _olyOlympiadPlayer != null;
    }

    public void setOlyParticipant(final OlympiadPlayer olympiadPlayer) {
        _olyOlympiadPlayer = olympiadPlayer;
    }

    public boolean isOlyCompetitionStarted() {
        return isOlyParticipant() && _olyOlympiadPlayer.getCompetition().getState() == OlympiadGameState.PLAYING;
    }

    public boolean isOlyCompetitionStandby() {
        return isOlyParticipant() && _olyOlympiadPlayer.getCompetition().getState() == OlympiadGameState.STAND_BY;
    }

    public boolean isOlyCompetitionPreparing() {
        return isOlyParticipant() && (_olyOlympiadPlayer.getCompetition().getState() == OlympiadGameState.INIT || _olyOlympiadPlayer.getCompetition().getState() == OlympiadGameState.STAND_BY);
    }

    public boolean isOlyCompetitionFinished() {
        return isOlyParticipant() && _olyOlympiadPlayer.getCompetition().getState() == OlympiadGameState.FINISH;
    }

    public boolean isLooseOlyCompetition() {
        if (!isOlyParticipant()) {
            return false;
        }
        if (isOlyCompetitionFinished()) {
            return !_olyOlympiadPlayer.isAlive();
        }
        return _olyOlympiadPlayer.isPlayerLoose(this);
    }

    public WorldRegion getObserverRegion() {
        return _observerRegion;
    }

    public void setObserverRegion(final WorldRegion region) {
        _observerRegion = region;
    }

    public int getTeleMode() {
        return _telemode;
    }

    public void setTeleMode(final int mode) {
        _telemode = mode;
    }

    public void setLoto(final int i, final int val) {
        _loto[i] = val;
    }

    public int getLoto(final int i) {
        return _loto[i];
    }

    public void setRace(final int i, final int val) {
        _race[i] = val;
    }

    public int getRace(final int i) {
        return _race[i];
    }

    public boolean getMessageRefusal() {
        return _messageRefusal;
    }

    public void setMessageRefusal(final boolean mode) {
        _messageRefusal = mode;
    }

    public boolean getTradeRefusal() {
        return _tradeRefusal;
    }

    public void setTradeRefusal(final boolean mode) {
        _tradeRefusal = mode;
    }

    public void addToBlockList(final String charName) {
        if (charName == null || charName.equalsIgnoreCase(getName()) || isInBlockList(charName)) {
            sendPacket(Msg.YOU_HAVE_FAILED_TO_REGISTER_THE_USER_TO_YOUR_IGNORE_LIST);
            return;
        }
        final Player block_target = World.getPlayer(charName);
        if (block_target != null) {
            if (block_target.isGM()) {
                sendPacket(Msg.YOU_MAY_NOT_IMPOSE_A_BLOCK_ON_A_GM);
                return;
            }
            _blockList.put(block_target.getObjectId(), block_target.getName());
            sendPacket(new SystemMessage(617).addString(block_target.getName()));
            block_target.sendPacket(new SystemMessage(619).addString(getName()));
        } else {
            final int charId = CharacterDAO.getInstance().getObjectIdByName(charName);
            if (charId == 0) {
                sendPacket(Msg.YOU_HAVE_FAILED_TO_REGISTER_THE_USER_TO_YOUR_IGNORE_LIST);
                return;
            }
            if (Config.gmlist.containsKey(charId) && Config.gmlist.get(charId).IsGM) {
                sendPacket(Msg.YOU_MAY_NOT_IMPOSE_A_BLOCK_ON_A_GM);
                return;
            }
            _blockList.put(charId, charName);
            sendPacket(new SystemMessage(617).addString(charName));
        }
    }

    public void removeFromBlockList(final String charName) {
        int charId = 0;
        for (final Map.Entry<Integer, String> entry : _blockList.entrySet()) {
            if (charName.equalsIgnoreCase(entry.getValue())) {
                charId = entry.getKey();
                break;
            }
        }
        if (charId == 0) {
            sendPacket(Msg.YOU_HAVE_FAILED_TO_DELETE_THE_CHARACTER_FROM_IGNORE_LIST);
            return;
        }
        sendPacket(new SystemMessage(618).addString(_blockList.remove(charId)));
        final Player block_target = GameObjectsStorage.getPlayer(charId);
        if (block_target != null) {
            block_target.sendMessage(getName() + " has removed you from his/her Ignore List.");
        }
    }

    public boolean isInBlockList(final Player player) {
        return isInBlockList(player.getObjectId());
    }

    public boolean isInBlockList(final int charId) {
        return _blockList != null && _blockList.containsKey(charId);
    }

    public boolean isInBlockList(final String charName) {
        return _blockList.values().stream().anyMatch(charName::equalsIgnoreCase);
    }

    private void restoreBlockList() {
        _blockList.clear();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT target_Id, char_name FROM character_blocklist LEFT JOIN characters ON ( character_blocklist.target_Id = characters.obj_Id ) WHERE character_blocklist.obj_Id = ?");
            statement.setInt(1, getObjectId());
            rs = statement.executeQuery();
            while (rs.next()) {
                final int targetId = rs.getInt("target_Id");
                final String name = rs.getString("char_name");
                if (name == null) {
                    continue;
                }
                _blockList.put(targetId, name);
            }
        } catch (SQLException e) {
            LOGGER.warn("Can't restore player blocklist " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement, rs);
        }
    }

    private void storeBlockList() {
        Connection con = null;
        Statement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.createStatement();
            statement.executeUpdate("DELETE FROM character_blocklist WHERE obj_Id=" + getObjectId());
            if (_blockList.isEmpty()) {
                return;
            }
            final SqlBatch b = new SqlBatch("INSERT IGNORE INTO `character_blocklist` (`obj_Id`,`target_Id`) VALUES");
            synchronized (_blockList) {
                for (final Map.Entry<Integer, String> e : _blockList.entrySet()) {
                    String sb = "(" + getObjectId() + "," +
                            e.getKey() + ")";
                    b.write(sb);
                }
            }
            if (!b.isEmpty()) {
                statement.executeUpdate(b.close());
            }
        } catch (Exception e2) {
            LOGGER.warn("Can't store player blocklist " + e2);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public boolean isBlockAll() {
        return _blockAll;
    }

    public void setBlockAll(final boolean state) {
        _blockAll = state;
    }

    public Collection<String> getBlockList() {
        return _blockList.values();
    }

    public Map<Integer, String> getBlockListMap() {
        return _blockList;
    }

    private void stopCustomHeroEndTask() {
        if (_customHeroRemoveTask != null) {
            _customHeroRemoveTask.cancel(true);
            _customHeroRemoveTask = null;
        }
    }

    public void setCustomHero(final boolean customHero, final long customHeroStatusDuration, final boolean processSkills) {
        if (!isHero() && customHero && customHeroStatusDuration > 0L) {
            setVar("CustomHeroEndTime", System.currentTimeMillis() / 1000L + customHeroStatusDuration, -1L);
            setHero(true);
            if (processSkills) {
                HeroManager.addSkills(this);
            }
            _customHeroRemoveTask = ThreadPoolManager.getInstance().schedule(new EndCustomHeroTask(this), customHeroStatusDuration * 1000L);
        } else if (!customHero) {
            unsetVar("CustomHeroEndTime");
            stopCustomHeroEndTask();
            if (HeroManager.getInstance().isCurrentHero(this)) {
                return;
            }
            setHero(false);
            if (processSkills) {
                HeroManager.removeSkills(this);
            }
        }
    }

    @Override
    public boolean isHero() {
        return _hero;
    }

    public void setHero(final boolean hero) {
        _hero = hero;
    }

    public void updateNobleSkills() {
        if (isNoble()) {
            if (isClanLeader() && getClan().getCastle() > 0) {
                super.addSkill(SkillTable.getInstance().getInfo(327, 1));
            }
            super.addSkill(SkillTable.getInstance().getInfo(325, 1));
            super.addSkill(SkillTable.getInstance().getInfo(1323, 1));
            super.addSkill(SkillTable.getInstance().getInfo(1324, 1));
            super.addSkill(SkillTable.getInstance().getInfo(1325, 1));
            super.addSkill(SkillTable.getInstance().getInfo(1326, 1));
            super.addSkill(SkillTable.getInstance().getInfo(1327, 1));
        } else {
            super.removeSkillById(327);
            super.removeSkillById(325);
            super.removeSkillById(1323);
            super.removeSkillById(1324);
            super.removeSkillById(1325);
            super.removeSkillById(1326);
            super.removeSkillById(1327);
        }
    }

    public boolean isNoble() {
        return _noble;
    }

    public void setNoble(final boolean noble) {
        _noble = noble;
        if (noble) {
            broadcastPacket(new SocialAction(getObjectId(), 16));
        }
    }

    public int getSubLevel() {
        return isSubClassActive() ? getLevel() : 0;
    }

    public void updateKetraVarka() {
        if (ItemFunctions.getItemCount(this, 7215) > 0L) {
            _ketra = 5;
        } else if (ItemFunctions.getItemCount(this, 7214) > 0L) {
            _ketra = 4;
        } else if (ItemFunctions.getItemCount(this, 7213) > 0L) {
            _ketra = 3;
        } else if (ItemFunctions.getItemCount(this, 7212) > 0L) {
            _ketra = 2;
        } else if (ItemFunctions.getItemCount(this, 7211) > 0L) {
            _ketra = 1;
        } else if (ItemFunctions.getItemCount(this, 7225) > 0L) {
            _varka = 5;
        } else if (ItemFunctions.getItemCount(this, 7224) > 0L) {
            _varka = 4;
        } else if (ItemFunctions.getItemCount(this, 7223) > 0L) {
            _varka = 3;
        } else if (ItemFunctions.getItemCount(this, 7222) > 0L) {
            _varka = 2;
        } else if (ItemFunctions.getItemCount(this, 7221) > 0L) {
            _varka = 1;
        } else {
            _varka = 0;
            _ketra = 0;
        }
    }

    public int getVarka() {
        return _varka;
    }

    public int getKetra() {
        return _ketra;
    }

    public void updateRam() {
        if (ItemFunctions.getItemCount(this, 7247) > 0L) {
            _ram = 2;
        } else if (ItemFunctions.getItemCount(this, 7246) > 0L) {
            _ram = 1;
        } else {
            _ram = 0;
        }
    }

    public int getRam() {
        return _ram;
    }

    public int getPledgeType() {
        return _pledgeType;
    }

    public void setPledgeType(final int typeId) {
        _pledgeType = typeId;
    }

    public int getLvlJoinedAcademy() {
        return _lvlJoinedAcademy;
    }

    public void setLvlJoinedAcademy(final int lvl) {
        _lvlJoinedAcademy = lvl;
    }

    public int getPledgeClass() {
        return _pledgeClass;
    }

    public EPledgeRank getPledgeRank() {
        return EPledgeRank.getPledgeRank(getPledgeClass());
    }

    public void updatePledgeClass() {
        final int CLAN_LEVEL = (_clan == null) ? -1 : _clan.getLevel();
        final boolean IN_ACADEMY = _clan != null && Clan.isAcademy(_pledgeType);
        final boolean IS_GUARD = _clan != null && Clan.isRoyalGuard(_pledgeType);
        final boolean IS_KNIGHT = _clan != null && Clan.isOrderOfKnights(_pledgeType);
        boolean IS_GUARD_CAPTAIN = false;
        boolean IS_KNIGHT_COMMANDER = false;
        boolean IS_LEADER = false;
        final SubUnit unit = getSubUnit();
        if (unit != null) {
            final UnitMember unitMember = unit.getUnitMember(getObjectId());
            if (unitMember == null) {
                LOGGER.warn("Player: unitMember null, clan: " + _clan.getClanId() + "; pledgeType: " + unit.getType());
                return;
            }
            IS_GUARD_CAPTAIN = Clan.isRoyalGuard(unitMember.getLeaderOf());
            IS_KNIGHT_COMMANDER = Clan.isOrderOfKnights(unitMember.getLeaderOf());
            IS_LEADER = (unitMember.getLeaderOf() == 0);
        }
        switch (CLAN_LEVEL) {
            case -1: {
                _pledgeClass = 0;
                break;
            }
            case 0:
            case 1:
            case 2:
            case 3: {
                if (IS_LEADER) {
                    _pledgeClass = 2;
                    break;
                }
                _pledgeClass = 1;
                break;
            }
            case 4: {
                if (IS_LEADER) {
                    _pledgeClass = 3;
                    break;
                }
                _pledgeClass = 2;
                break;
            }
            case 5: {
                if (IS_LEADER) {
                    _pledgeClass = 4;
                    break;
                }
                if (IN_ACADEMY) {
                    _pledgeClass = 1;
                    break;
                }
                _pledgeClass = 2;
                break;
            }
            case 6: {
                if (IS_LEADER) {
                    _pledgeClass = 5;
                    break;
                }
                if (IN_ACADEMY) {
                    _pledgeClass = 1;
                    break;
                }
                if (IS_GUARD_CAPTAIN) {
                    _pledgeClass = 4;
                    break;
                }
                if (IS_GUARD) {
                    _pledgeClass = 2;
                    break;
                }
                _pledgeClass = 3;
                break;
            }
            case 7: {
                if (IS_LEADER) {
                    _pledgeClass = 7;
                    break;
                }
                if (IN_ACADEMY) {
                    _pledgeClass = 1;
                    break;
                }
                if (IS_GUARD_CAPTAIN) {
                    _pledgeClass = 6;
                    break;
                }
                if (IS_GUARD) {
                    _pledgeClass = 3;
                    break;
                }
                if (IS_KNIGHT_COMMANDER) {
                    _pledgeClass = 5;
                    break;
                }
                if (IS_KNIGHT) {
                    _pledgeClass = 2;
                    break;
                }
                _pledgeClass = 4;
                break;
            }
            case 8: {
                if (IS_LEADER) {
                    _pledgeClass = 8;
                    break;
                }
                if (IN_ACADEMY) {
                    _pledgeClass = 1;
                    break;
                }
                if (IS_GUARD_CAPTAIN) {
                    _pledgeClass = 7;
                    break;
                }
                if (IS_GUARD) {
                    _pledgeClass = 4;
                    break;
                }
                if (IS_KNIGHT_COMMANDER) {
                    _pledgeClass = 6;
                    break;
                }
                if (IS_KNIGHT) {
                    _pledgeClass = 3;
                    break;
                }
                _pledgeClass = 5;
                break;
            }
        }
        if (_hero && _pledgeClass < 8) {
            _pledgeClass = 8;
        } else if (_noble && _pledgeClass < 5) {
            _pledgeClass = 5;
        }
    }

    public int getPowerGrade() {
        return _powerGrade;
    }

    public void setPowerGrade(final int grade) {
        _powerGrade = grade;
    }

    public int getApprentice() {
        return _apprentice;
    }

    public void setApprentice(final int apprentice) {
        _apprentice = apprentice;
    }

    public int getSponsor() {
        return (_clan == null) ? 0 : _clan.getAnyMember(getObjectId()).getSponsor();
    }

    public int getNameColor() {
        if (isInObserverMode()) {
            return Color.black.getRGB();
        }
        return _nameColor;
    }

    public void setNameColor(final int nameColor) {
        if (nameColor != Config.NORMAL_NAME_COLOUR && nameColor != Config.CLANLEADER_NAME_COLOUR && nameColor != Config.GM_NAME_COLOUR && nameColor != Config.SERVICES_OFFLINE_TRADE_NAME_COLOR) {
            setVar("namecolor", Integer.toHexString(nameColor), -1L);
        } else if (nameColor == Config.NORMAL_NAME_COLOUR) {
            unsetVar("namecolor");
        }
        _nameColor = nameColor;
    }

    public void setVar(final String name, final String value, final long expirationTime) {
        _vars.put(name, value);
        CharacterVariablesDAO.getInstance().setVar(getObjectId(), name, value, expirationTime);
    }

    public void setVar(final String name, final int value, final long expirationTime) {
        setVar(name, String.valueOf(value), expirationTime);
    }

    public void setVar(final String name, final long value, final long expirationTime) {
        setVar(name, String.valueOf(value), expirationTime);
    }

    public void unsetVar(final String name) {
        if (name == null) {
            return;
        }
        if (_vars.remove(name) != null) {
            CharacterVariablesDAO.getInstance().deleteVar(getObjectId(), name);
        }
    }

    public String getVar(final String name) {
        return _vars.getString(name, null);
    }

    public boolean getVarB(final String name, final boolean defaultVal) {
        final String var = _vars.getString(name, null);
        if (var == null) {
            return defaultVal;
        }
        return !"0".equals(var) && !"false".equalsIgnoreCase(var);
    }

    public boolean getVarB(final String name) {
        final String var = _vars.getString(name, null);
        return var != null && !"0".equals(var) && !"false".equalsIgnoreCase(var);
    }

    public long getVarLong(final String name) {
        return getVarLong(name, 0L);
    }

    public long getVarLong(final String name, final long defaultVal) {
        long result = defaultVal;
        final String var = getVar(name);
        if (var != null) {
            result = Long.parseLong(var);
        }
        return result;
    }

    public int getVarInt(final String name) {
        return getVarInt(name, 0);
    }

    public int getVarInt(final String name, final int defaultVal) {
        int result = defaultVal;
        final String var = getVar(name);
        if (var != null) {
            result = Integer.parseInt(var);
        }
        return result;
    }

    public MultiValueSet<String> getVars() {
        return _vars;
    }

    public String getLang() {
        return getVar("lang@");
    }

    public String getHWIDLock() {
        return getVar("hwidlock@");
    }

    public void setHWIDLock(final String HWIDLock) {
        if (HWIDLock == null) {
            unsetVar("hwidlock@");
        } else {
            setVar("hwidlock@", HWIDLock, -1L);
        }
    }

    public String getIPLock() {
        return getVar("iplock@");
    }

    public void setIPLock(final String IPLock) {
        if (IPLock == null) {
            unsetVar("iplock@");
        } else {
            setVar("iplock@", IPLock, -1L);
        }
    }

    public int getLangId() {
        final String lang = getLang();
        if ("en".equalsIgnoreCase(lang) || "e".equalsIgnoreCase(lang) || "eng".equalsIgnoreCase(lang)) {
            return 0;
        }
        if ("ru".equalsIgnoreCase(lang) || "r".equalsIgnoreCase(lang) || "rus".equalsIgnoreCase(lang)) {
            return 1;
        }
        return -1;
    }

    public Language getLanguage() {
        final String lang = getLang();
        if (lang == null || "en".equalsIgnoreCase(lang) || "e".equalsIgnoreCase(lang) || "eng".equalsIgnoreCase(lang)) {
            return Language.ENGLISH;
        }
        if ("ru".equalsIgnoreCase(lang) || "r".equalsIgnoreCase(lang) || "rus".equalsIgnoreCase(lang)) {
            return Language.RUSSIAN;
        }
        return Language.ENGLISH;
    }

    public boolean isLangRus() {
        return getLangId() == 1;
    }

    public int isAtWarWith(final Integer id) {
        return (_clan != null && _clan.isAtWarWith(id)) ? 1 : 0;
    }

    public int isAtWar() {
        return (_clan != null && _clan.isAtWarOrUnderAttack() > 0) ? 1 : 0;
    }

    public void stopWaterTask() {
        if (_taskWater != null) {
            _taskWater.cancel(false);
            _taskWater = null;
            sendPacket(new SetupGauge(this, 2, 0));
            sendChanges();
        }
    }

    public void startWaterTask() {
        if (isDead()) {
            stopWaterTask();
        } else if (Config.ALLOW_WATER && _taskWater == null) {
            final int timeinwater = (int) (calcStat(Stats.BREATH, 86.0, null, null) * 1000.0);
            sendPacket(new SetupGauge(this, 2, timeinwater));
            if (getTransformation() > 0 && getTransformationTemplate() > 0 && !isCursedWeaponEquipped()) {
                setTransformation(0);
            }
            _taskWater = ThreadPoolManager.getInstance().scheduleAtFixedRate(new WaterTask(this), timeinwater, 1000L);
            sendChanges();
        }
    }

    public void doRevive(final double percent) {
        restoreExp(percent);
        doRevive();
    }

    @Override
    public void doRevive() {
        super.doRevive();
        unsetVar("lostexp");
        updateEffectIcons();
        autoShot();
    }

    public void reviveRequest(final Player reviver, final double percent, final boolean pet) {
        final ReviveAnswerListener reviveAsk = (_askDialog != null && _askDialog.getValue() instanceof ReviveAnswerListener) ? ((ReviveAnswerListener) _askDialog.getValue()) : null;
        if (reviveAsk != null) {
            if (reviveAsk.isForPet() == pet && reviveAsk.getPower() >= percent) {
                reviver.sendPacket(Msg.BETTER_RESURRECTION_HAS_BEEN_ALREADY_PROPOSED);
                return;
            }
            if (pet && !reviveAsk.isForPet()) {
                reviver.sendPacket(Msg.SINCE_THE_MASTER_WAS_IN_THE_PROCESS_OF_BEING_RESURRECTED_THE_ATTEMPT_TO_RESURRECT_THE_PET_HAS_BEEN_CANCELLED);
                return;
            }
            if (pet && isDead()) {
                reviver.sendPacket(Msg.WHILE_A_PET_IS_ATTEMPTING_TO_RESURRECT_IT_CANNOT_HELP_IN_RESURRECTING_ITS_MASTER);
                return;
            }
        }
        if ((pet && getPet() != null && getPet().isDead()) || (!pet && isDead())) {
            final ConfirmDlg pkt = new ConfirmDlg(SystemMsg.C1_IS_MAKING_AN_ATTEMPT_TO_RESURRECT_YOU_IF_YOU_CHOOSE_THIS_PATH_S2_EXPERIENCE_WILL_BE_RETURNED_FOR_YOU, 0);
            pkt.addName(reviver).addString(Math.round(percent) + " percent");
            ask(pkt, new ReviveAnswerListener(this, percent, pet));
        }
    }

    public void summonCharacterRequest(final Creature summoner, final Location loc, final int summonConsumeCrystal) {
        final ConfirmDlg cd = new ConfirmDlg(SystemMsg.C1_WISHES_TO_SUMMON_YOU_FROM_S2, 60000);
        cd.addName(summoner).addZoneName(loc);
        ask(cd, new SummonAnswerListener(this, loc, summonConsumeCrystal, 60000L));
    }

    public void scriptRequest(final String text, final String scriptName, final Object[] args) {
        ask(new ConfirmDlg(SystemMsg.S1, 30000).addString(text), new ScriptAnswerListener(this, scriptName, args, 30000L));
    }

    public void updateNoChannel(final long time) {
        setNoChannel(time);
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            final String stmt = "UPDATE characters SET nochannel = ? WHERE obj_Id=?";
            statement = con.prepareStatement("UPDATE characters SET nochannel = ? WHERE obj_Id=?");
            statement.setLong(1, (_NoChannel > 0L) ? (_NoChannel / 1000L) : _NoChannel);
            statement.setInt(2, getObjectId());
            statement.executeUpdate();
        } catch (Exception e) {
            LOGGER.warn("Could not activate nochannel:" + e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
        sendPacket(new EtcStatusUpdate(this));
    }

    public boolean canTalkWith(final Player player) {
        return _NoChannel >= 0L || player == this;
    }

    public Deque<ChatMsg> getMessageBucket() {
        return _msgBucket;
    }

    @Override
    public boolean isInBoat() {
        return _boat != null;
    }

    public Boat getBoat() {
        return _boat;
    }

    public void setBoat(final Boat boat) {
        _boat = boat;
    }

    @Override
    protected L2GameServerPacket stopMovePacket() {
        if (isInBoat()) {
            getBoat().inStopMovePacket(this);
        }
        return super.stopMovePacket();
    }

    public Location getInBoatPosition() {
        return _inBoatPosition;
    }

    public void setInBoatPosition(final Location loc) {
        _inBoatPosition = loc;
    }

    public Map<Integer, SubClass> getSubClasses() {
        return _classlist;
    }

    public void setBaseClass(final int baseClass) {
        _baseClass = baseClass;
    }

    public int getBaseClassId() {
        return _baseClass;
    }

    public SubClass getActiveClass() {
        return _activeClass;
    }

    public void setActiveClass(final SubClass activeClass) {
        _activeClass = activeClass;
    }

    public int getActiveClassId() {
        return getActiveClass().getClassId();
    }

    public synchronized void changeClassInDb(final int oldclass, final int newclass) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("UPDATE character_subclasses SET class_id=? WHERE char_obj_id=? AND class_id=?");
            statement.setInt(1, newclass);
            statement.setInt(2, getObjectId());
            statement.setInt(3, oldclass);
            statement.executeUpdate();
            DbUtils.close(statement);
            statement = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=?");
            statement.setInt(1, getObjectId());
            statement.setInt(2, newclass);
            statement.executeUpdate();
            DbUtils.close(statement);
            statement = con.prepareStatement("UPDATE character_hennas SET class_index=? WHERE char_obj_id=? AND class_index=?");
            statement.setInt(1, newclass);
            statement.setInt(2, getObjectId());
            statement.setInt(3, oldclass);
            statement.executeUpdate();
            DbUtils.close(statement);
            statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE object_id=? AND class_index=?");
            statement.setInt(1, getObjectId());
            statement.setInt(2, newclass);
            statement.executeUpdate();
            DbUtils.close(statement);
            statement = con.prepareStatement("UPDATE character_shortcuts SET class_index=? WHERE object_id=? AND class_index=?");
            statement.setInt(1, newclass);
            statement.setInt(2, getObjectId());
            statement.setInt(3, oldclass);
            statement.executeUpdate();
            DbUtils.close(statement);
            statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=? AND class_index=?");
            statement.setInt(1, getObjectId());
            statement.setInt(2, newclass);
            statement.executeUpdate();
            DbUtils.close(statement);
            statement = con.prepareStatement("UPDATE character_skills SET class_index=? WHERE char_obj_id=? AND class_index=?");
            statement.setInt(1, newclass);
            statement.setInt(2, getObjectId());
            statement.setInt(3, oldclass);
            statement.executeUpdate();
            DbUtils.close(statement);
            statement = con.prepareStatement("DELETE FROM character_effects_save WHERE object_id=? AND id=?");
            statement.setInt(1, getObjectId());
            statement.setInt(2, newclass);
            statement.executeUpdate();
            DbUtils.close(statement);
            statement = con.prepareStatement("UPDATE character_effects_save SET id=? WHERE object_id=? AND id=?");
            statement.setInt(1, newclass);
            statement.setInt(2, getObjectId());
            statement.setInt(3, oldclass);
            statement.executeUpdate();
            DbUtils.close(statement);
            statement = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=?");
            statement.setInt(1, getObjectId());
            statement.setInt(2, newclass);
            statement.executeUpdate();
            DbUtils.close(statement);
            statement = con.prepareStatement("UPDATE character_skills_save SET class_index=? WHERE char_obj_id=? AND class_index=?");
            statement.setInt(1, newclass);
            statement.setInt(2, getObjectId());
            statement.setInt(3, oldclass);
            statement.executeUpdate();
            DbUtils.close(statement);
        } catch (SQLException e) {
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void storeCharSubClasses() {
        final SubClass main = getActiveClass();
        if (main != null) {
            main.setCp(getCurrentCp());
            main.setHp(getCurrentHp());
            main.setMp(getCurrentMp());
            main.setActive(true);
            getSubClasses().put(getActiveClassId(), main);
        } else {
            LOGGER.warn("Could not store char sub data, main class " + getActiveClassId() + " not found for " + this);
        }
        Connection con = null;
        Statement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.createStatement();
            for (final SubClass subClass : getSubClasses().values()) {
                String sb = "UPDATE character_subclasses SET " + "exp=" + subClass.getExp() + "," +
                        "sp=" + subClass.getSp() + "," +
                        "curHp=" + subClass.getHp() + "," +
                        "curMp=" + subClass.getMp() + "," +
                        "curCp=" + subClass.getCp() + "," +
                        "level=" + subClass.getLevel() + "," +
                        "active=" + (subClass.isActive() ? 1 : 0) + "," +
                        "isBase=" + (subClass.isBase() ? 1 : 0) + "," +
                        "death_penalty=" + subClass.getDeathPenalty(this).getLevelOnSaveDB() +
                        " WHERE char_obj_id=" + getObjectId() + " AND class_id=" + subClass.getClassId() + " LIMIT 1";
                statement.executeUpdate(sb);
            }
            String sb = "UPDATE character_subclasses SET " + "maxHp=" + getMaxHp() + "," +
                    "maxMp=" + getMaxMp() + "," +
                    "maxCp=" + getMaxCp() +
                    " WHERE char_obj_id=" + getObjectId() + " AND active=1 LIMIT 1";
            statement.executeUpdate(sb);
        } catch (Exception e) {
            LOGGER.warn("Could not store char sub data: " + e);
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public boolean addSubClass(final int classId, final boolean storeOld) {
        if (_classlist.size() >= Config.ALT_GAME_BASE_SUB) {
            return false;
        }
        final ClassId newId = ClassId.VALUES[classId];
        final SubClass newClass = new SubClass();
        newClass.setBase(false);
        if (newId.getRace() == null) {
            return false;
        }
        newClass.setClassId(classId);
        _classlist.put(classId, newClass);
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("INSERT INTO  `character_subclasses`  (\t`char_obj_id`,   `class_id`,   `exp`,   `sp`,   `curHp`,   `curMp`,   `curCp`,   `maxHp`,   `maxMp`,   `maxCp`,   `level`,   `active`,   `isBase`,   `death_penalty`)VALUES  (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            statement.setInt(1, getObjectId());
            statement.setInt(2, newClass.getClassId());
            statement.setLong(3, newClass.getExp());
            statement.setInt(4, 0);
            statement.setDouble(5, getCurrentHp());
            statement.setDouble(6, getCurrentMp());
            statement.setDouble(7, getCurrentCp());
            statement.setDouble(8, getCurrentHp());
            statement.setDouble(9, getCurrentMp());
            statement.setDouble(10, getCurrentCp());
            statement.setInt(11, newClass.getLevel());
            statement.setInt(12, 0);
            statement.setInt(13, 0);
            statement.setInt(14, 0);
            statement.execute();
        } catch (Exception e) {
            LOGGER.warn("Could not add character sub-class: " + e, e);
            return false;
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
        setActiveSubClass(classId, storeOld);
        boolean countUnlearnable = true;
        int unLearnable = 0;
        for (Collection<SkillLearn> skills = SkillAcquireHolder.getInstance().getAvailableSkills(this, AcquireType.NORMAL); skills.size() > unLearnable; skills = SkillAcquireHolder.getInstance().getAvailableSkills(this, AcquireType.NORMAL)) {
            for (final SkillLearn s : skills) {
                final Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
                if (sk == null || !sk.getCanLearn(newId)) {
                    if (!countUnlearnable) {
                        continue;
                    }
                    ++unLearnable;
                } else {
                    addSkill(sk, true);
                }
            }
            countUnlearnable = false;
        }
        sendPacket(new SkillList(this));
        setCurrentHpMp(getMaxHp(), getMaxMp(), true);
        setCurrentCp(getMaxCp());
        return true;
    }

    public boolean modifySubClass(final int oldClassId, final int newClassId) {
        final SubClass originalClass = _classlist.get(oldClassId);
        if (originalClass == null || originalClass.isBase()) {
            return false;
        }
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("DELETE FROM character_subclasses WHERE char_obj_id=? AND class_id=? AND isBase = 0");
            statement.setInt(1, getObjectId());
            statement.setInt(2, oldClassId);
            statement.execute();
            DbUtils.close(statement);
            statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=? AND class_index=? ");
            statement.setInt(1, getObjectId());
            statement.setInt(2, oldClassId);
            statement.execute();
            DbUtils.close(statement);
            statement = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=? ");
            statement.setInt(1, getObjectId());
            statement.setInt(2, oldClassId);
            statement.execute();
            DbUtils.close(statement);
            statement = con.prepareStatement("DELETE FROM character_effects_save WHERE object_id=? AND id=? ");
            statement.setInt(1, getObjectId());
            statement.setInt(2, oldClassId);
            statement.execute();
            DbUtils.close(statement);
            statement = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=? ");
            statement.setInt(1, getObjectId());
            statement.setInt(2, oldClassId);
            statement.execute();
            DbUtils.close(statement);
            statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE object_id=? AND class_index=? ");
            statement.setInt(1, getObjectId());
            statement.setInt(2, oldClassId);
            statement.execute();
            DbUtils.close(statement);
        } catch (Exception e) {
            LOGGER.warn("Could not delete char sub-class: " + e);
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
        _classlist.remove(oldClassId);
        getListeners().onPlayerClassChange(this, oldClassId, newClassId);
        return newClassId <= 0 || addSubClass(newClassId, false);
    }

    public void setActiveSubClass(final int subId, final boolean store) {
        final SubClass sub = getSubClasses().get(subId);
        if (sub == null) {
            return;
        }
        try {
            if (getActiveClass() != null) {
                EffectsDAO.getInstance().insert(this);
                storeDisableSkills();
                if (QuestManager.getQuest(422) != null) {
                    final String qn = QuestManager.getQuest(422).getName();
                    if (qn != null) {
                        final QuestState qs = getQuestState(qn);
                        if (qs != null) {
                            qs.exitCurrentQuest(true);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.warn("", ex);
        }
        final SubClass oldsub = getActiveClass();
        if (oldsub != null) {
            oldsub.setActive(false);
            if (store) {
                oldsub.setCp(getCurrentCp());
                oldsub.setHp(getCurrentHp());
                oldsub.setMp(getCurrentMp());
                getSubClasses().put(getActiveClassId(), oldsub);
            }
        }
        sub.setActive(true);
        setActiveClass(sub);
        getSubClasses().put(getActiveClassId(), sub);
        setClassId(subId, false, false);
        removeAllSkills();
        getEffectList().stopAllEffects();
        if (getPet() != null && (getPet().isSummon() || (Config.ALT_IMPROVED_PETS_LIMITED_USE && ((getPet().getNpcId() == 16035 && !isMageClass()) || (getPet().getNpcId() == 16034 && isMageClass()))))) {
            getPet().unSummon();
        }

        restoreSkills();
        if (Config.ALT_SUBLASS_SKILL_TRANSFER && getBaseClassId() == subId) {
            for (final SubClass ssc : getSubClasses().values()) {
                if (ssc.getClassId() != subId) {
                    restoreSkills(ssc.getClassId());
                }
            }
        }
        rewardSkills(false);
        checkSkills();
        refreshExpertisePenalty();
        sendPacket(new SkillList(this));
        getInventory().refreshEquip();
        getInventory().validateItems();
        for (int i = 0; i < 3; ++i) {
            _henna[i] = null;
        }
        restoreHenna();
        sendPacket(new HennaInfo(this));
        EffectsDAO.getInstance().restoreEffects(this);
        restoreDisableSkills();
        setCurrentHpMp(sub.getHp(), sub.getMp());
        setCurrentCp(sub.getCp());
        _shortCuts.restore();
        sendPacket(new ShortCutInit(this));
        getAutoSoulShot().stream().mapToInt(shotId -> shotId).mapToObj(shotId -> new ExAutoSoulShot(shotId, true)).forEach(this::sendPacket);
        sendPacket(new SkillCoolTime(this));
        broadcastPacket(new SocialAction(getObjectId(), 15));
        getDeathPenalty().restore(this);
        setIncreasedForce(0);
        startHourlyTask();
        broadcastCharInfo();
        updateEffectIcons();
        updateStats();
        getListeners().onPlayerClassChange(this, _activeClass.getClassId(), subId);
    }

    public void startKickTask(final long delayMillis) {
        stopKickTask();
        _kickTask = ThreadPoolManager.getInstance().schedule(new KickTask(this), delayMillis);
    }

    public void stopKickTask() {
        if (_kickTask != null) {
            _kickTask.cancel(false);
            _kickTask = null;
        }
    }

    public void startBonusTask() {
        if (Config.SERVICES_RATE_ENABLED) {
            AccountBonusDAO.getInstance().load(getAccountName(), getBonus());
            final long bonusExpireTime = getBonus().getBonusExpire();
            if (bonusExpireTime > System.currentTimeMillis() / 1000L) {
                if (_bonusExpiration == null) {
                    _bonusExpiration = LazyPrecisionTaskManager.getInstance().startBonusExpirationTask(this);
                }
            } else if (bonusExpireTime > 0L) {
                AccountBonusDAO.getInstance().delete(getAccountName());
            }
        }
    }

    public void stopBonusTask() {
        if (_bonusExpiration != null) {
            _bonusExpiration.cancel(false);
            _bonusExpiration = null;
        }
    }

    public void deleteBonusPrem(){
            AccountBonusDAO.getInstance().delete(getAccountName());
    }

    @Override
    public int getInventoryLimit() {
        return (int) calcStat(Stats.INVENTORY_LIMIT, 0.0, null, null);
    }

    public int getWarehouseLimit() {
        return (int) calcStat(Stats.STORAGE_LIMIT, 0.0, null, null);
    }

    public int getTradeLimit() {
        return (int) calcStat(Stats.TRADE_LIMIT, 0.0, null, null);
    }

    public int getDwarvenRecipeLimit() {
        return (int) calcStat(Stats.DWARVEN_RECIPE_LIMIT, 50.0, null, null) + Config.ALT_ADD_RECIPES;
    }

    public int getCommonRecipeLimit() {
        return (int) calcStat(Stats.COMMON_RECIPE_LIMIT, 50.0, null, null) + Config.ALT_ADD_RECIPES;
    }

    public Element getAttackElement() {
        return Formulas.getAttackElement(this, null);
    }

    public int getAttack(final Element element) {
        if (element == Element.NONE) {
            return 0;
        }
        return (int) calcStat(element.getAttack(), 0.0, null, null);
    }

    public int getDefence(final Element element) {
        if (element == Element.NONE) {
            return 0;
        }
        return (int) calcStat(element.getDefence(), 0.0, null, null);
    }

    public boolean getAndSetLastItemAuctionRequest() {
        if (_lastItemAuctionInfoRequest + 2000L < System.currentTimeMillis()) {
            _lastItemAuctionInfoRequest = System.currentTimeMillis();
            return true;
        }
        _lastItemAuctionInfoRequest = System.currentTimeMillis();
        return false;
    }

    @Override
    public int getNpcId() {
        return -2;
    }

    public GameObject getVisibleObject(final int id) {
        if (getObjectId() == id) {
            return this;
        }
        GameObject target = null;
        if (getTargetId() == id) {
            target = getTarget();
        }
        if (target == null && _party != null) {
            for (final Player p : _party.getPartyMembers()) {
                if (p != null && p.getObjectId() == id) {
                    target = p;
                    break;
                }
            }
        }
        if (target == null) {
            target = World.getAroundObjectById(this, id);
        }
        return (target == null || target.isInvisible()) ? null : target;
    }

    @Override
    public int getPAtk(final Creature target) {
        final double init = (getActiveWeaponInstance() == null) ? (isMageClass() ? 3 : 4) : 0.0;
        return (int) calcStat(Stats.POWER_ATTACK, init, target, null);
    }

    @Override
    public int getPDef(final Creature target) {
        double init = 4.; // empty cloak and underwear slots

        final ItemInstance chest = _inventory.getPaperdollItem(Inventory.PAPERDOLL_CHEST);
        if (chest == null) {
            init += isMageClass() ? ArmorTemplate.EMPTY_BODY_MYSTIC : ArmorTemplate.EMPTY_BODY_FIGHTER;
        }
        if (_inventory.getPaperdollItem(Inventory.PAPERDOLL_LEGS) == null && (chest == null || chest.getBodyPart() != ItemTemplate.SLOT_FULL_ARMOR)) {
            init += isMageClass() ? ArmorTemplate.EMPTY_LEGS_MYSTIC : ArmorTemplate.EMPTY_LEGS_FIGHTER;
        }

        if (_inventory.getPaperdollItem(Inventory.PAPERDOLL_HEAD) == null) {
            init += ArmorTemplate.EMPTY_HELMET;
        }
        if (_inventory.getPaperdollItem(Inventory.PAPERDOLL_GLOVES) == null) {
            init += ArmorTemplate.EMPTY_GLOVES;
        }
        if (_inventory.getPaperdollItem(Inventory.PAPERDOLL_FEET) == null) {
            init += ArmorTemplate.EMPTY_BOOTS;
        }

        return (int) calcStat(Stats.POWER_DEFENCE, init, target, null);
    }

    @Override
    public int getMDef(final Creature target, final Skill skill) {
        double init = 0.;

        if (_inventory.getPaperdollItem(Inventory.PAPERDOLL_LEAR) == null) {
            init += ArmorTemplate.EMPTY_EARRING;
        }
        if (_inventory.getPaperdollItem(Inventory.PAPERDOLL_REAR) == null) {
            init += ArmorTemplate.EMPTY_EARRING;
        }
        if (_inventory.getPaperdollItem(Inventory.PAPERDOLL_NECK) == null) {
            init += ArmorTemplate.EMPTY_NECKLACE;
        }
        if (_inventory.getPaperdollItem(Inventory.PAPERDOLL_LFINGER) == null) {
            init += ArmorTemplate.EMPTY_RING;
        }
        if (_inventory.getPaperdollItem(Inventory.PAPERDOLL_RFINGER) == null) {
            init += ArmorTemplate.EMPTY_RING;
        }

        return (int) calcStat(Stats.MAGIC_DEFENCE, init, target, skill);
    }

    public boolean isSubClassActive() {
        return getBaseClassId() != getActiveClassId();
    }

    @Override
    public String getTitle() {
        return super.getTitle();
    }

    public int getTitleColor() {
        return _titlecolor;
    }

    public void setTitleColor(final int titlecolor) {
        if (titlecolor != DEFAULT_TITLE_COLOR) {
            setVar("titlecolor", Integer.toHexString(titlecolor), -1L);
        } else {
            unsetVar("titlecolor");
        }
        _titlecolor = titlecolor;
    }

    public String getDisconnectedTitle() {
        return _disconnectedTitle;
    }

    public void setDisconnectedTitle(final String disconnectedTitle) {
        _disconnectedTitle = disconnectedTitle;
    }

    public int getDisconnectedTitleColor() {
        return _disconnectedTitleColor;
    }

    public void setDisconnectedTitleColor(final int disconnectedTitleColor) {
        _disconnectedTitleColor = disconnectedTitleColor;
    }

    @Override
    public boolean isCursedWeaponEquipped() {
        return _cursedWeaponEquippedId != 0;
    }

    public int getCursedWeaponEquippedId() {
        return _cursedWeaponEquippedId;
    }

    public void setCursedWeaponEquippedId(final int value) {
        _cursedWeaponEquippedId = value;
    }

    @Override
    public boolean isImmobilized() {
        return super.isImmobilized() || isOverloaded() || isSitting() || isFishing();
    }

    @Override
    public boolean isBlocked() {
        return super.isBlocked() || isInMovie() || isInObserverMode() || isTeleporting() || isLogoutStarted();
    }

    @Override
    public boolean isInvul() {
        return super.isInvul() || isInMovie() || getAfterTeleportPortectionTime() > System.currentTimeMillis();
    }

    public boolean isResurectProhibited() {
        return _resurect_prohibited;
    }

    public void setResurectProhibited(final boolean prohibited) {
        _resurect_prohibited = prohibited;
    }

    public boolean isOverloaded() {
        return _overloaded;
    }

    public void setOverloaded(final boolean overloaded) {
        _overloaded = overloaded;
    }

    public boolean isFishing() {
        return _isFishing;
    }

    public Fishing getFishing() {
        return _fishing;
    }

    public void setFishing(final boolean value) {
        _isFishing = value;
    }

    public void startFishing(final FishTemplate fish, final int lureId) {
        _fishing.setFish(fish);
        _fishing.setLureId(lureId);
        _fishing.startFishing();
    }

    public void stopFishing() {
        _fishing.stopFishing();
    }

    public Location getFishLoc() {
        return _fishing.getFishLoc();
    }

    public Bonus getBonus() {
        return _bonus;
    }

    public boolean hasBonus() {
        return _bonus.getBonusExpire() > System.currentTimeMillis() / 1000L;
    }

    @Override
    public double getRateAdena() {
        return calcStat(Stats.ADENA_REWARD_MULTIPLIER, (_party == null) ? ((double) _bonus.getDropAdena()) : _party.getRateAdena());
    }

    @Override
    public double getRateItems() {
        return calcStat(Stats.ITEM_REWARD_MULTIPLIER, (_party == null) ? ((double) _bonus.getDropItems()) : _party.getRateDrop());
    }

    @Override
    public double getRateExp() {
        return calcStat(Stats.EXP, (_party == null) ? ((double) _bonus.getRateXp()) : _party.getRateExp(), null, null);
    }

    @Override
    public double getRateSp() {
        return calcStat(Stats.SP, (_party == null) ? ((double) _bonus.getRateSp()) : _party.getRateSp(), null, null);
    }

    @Override
    public double getRateSpoil() {
        return calcStat(Stats.SPOIL_REWARD_MULTIPLIER, (_party == null) ? ((double) _bonus.getDropSpoil()) : _party.getRateSpoil());
    }

    public boolean isMaried() {
        return _maried;
    }

    public void setMaried(final boolean state) {
        _maried = state;
    }

    public boolean isMaryRequest() {
        return _maryrequest;
    }

    public void setMaryRequest(final boolean state) {
        _maryrequest = state;
    }

    public boolean isMaryAccepted() {
        return _maryaccepted;
    }

    public void setMaryAccepted(final boolean state) {
        _maryaccepted = state;
    }

    public int getPartnerId() {
        return _partnerId;
    }

    public void setPartnerId(final int partnerid) {
        _partnerId = partnerid;
    }

    public int getCoupleId() {
        return _coupleId;
    }

    public void setCoupleId(final int coupleId) {
        _coupleId = coupleId;
    }

    public boolean isUndying() {
        return _isUndying;
    }

    public void setUndying(final boolean val) {
        if (!isGM()) {
            return;
        }
        _isUndying = val;
    }

    public void resetReuse() {
        _skillReuses.clear();
        _sharedGroupReuses.clear();
    }

    public DeathPenalty getDeathPenalty() {
        return (_activeClass == null) ? null : _activeClass.getDeathPenalty(this);
    }

    public boolean isCharmOfCourage() {
        return _charmOfCourage;
    }

    public void setCharmOfCourage(final boolean val) {
        if (!(_charmOfCourage = val)) {
            getEffectList().stopEffect(5041);
        }
        sendEtcStatusUpdate();
    }

    @Override
    public int getIncreasedForce() {
        return _increasedForce;
    }

    @Override
    public void setIncreasedForce(int i) {
        if (_increasedForce == i) {
            return;
        }
        i = Math.min(i, 7);
        i = Math.max(i, 0);
        if (i != 0 && i > _increasedForce) {
            _increasedForceLastUpdateTimeStamp = System.currentTimeMillis();
            if (_increasedForceCleanupTask == null) {
                _increasedForceCleanupTask = ThreadPoolManager.getInstance().schedule(new ForceCleanupTask(), 600000L);
            }
            sendPacket(new SystemMessage(323).addNumber(i));
        }
        _increasedForce = i;
        sendEtcStatusUpdate();
    }

    public boolean isFalling() {
        return System.currentTimeMillis() - _lastFalling < 5000L;
    }

    public void falling(final int height) {
        if (!Config.DAMAGE_FROM_FALLING || isDead() || isFlying() || isInWater() || isInBoat()) {
            return;
        }
        _lastFalling = System.currentTimeMillis();
        final int damage = (int) calcStat(Stats.FALL, getMaxHp() / 2000.0 * height, null, null);
        if (damage > 0) {
            final int curHp = (int) getCurrentHp();
            if (curHp - damage < 1) {
                setCurrentHp(1.0, false);
            } else {
                setCurrentHp(curHp - damage, false);
            }
            sendPacket(new SystemMessage(296).addNumber(damage));
        }
    }

    @Override
    public void checkHpMessages(final double curHp, final double newHp) {
        final int[] _hp = {30, 30};
        final int[] skills = {290, 291};
        final double percent = getMaxHp() / 100;
        final double _curHpPercent = curHp / percent;
        final double _newHpPercent = newHp / percent;
        boolean needsUpdate = false;
        for (int i = 0; i < skills.length; ++i) {
            final int level = getSkillLevel(skills[i]);
            if (level > 0) {
                if (_curHpPercent > _hp[i] && _newHpPercent <= _hp[i]) {
                    sendPacket(new SystemMessage(1133).addSkillName(skills[i], level));
                    needsUpdate = true;
                } else if (_curHpPercent <= _hp[i] && _newHpPercent > _hp[i]) {
                    sendPacket(new SystemMessage(1134).addSkillName(skills[i], level));
                    needsUpdate = true;
                }
            }
        }
        if (needsUpdate) {
            sendChanges();
        }
    }

    public void checkDayNightMessages() {
        final int level = getSkillLevel(294);
        if (level > 0) {
            if (GameTimeController.getInstance().isNowNight()) {
                sendPacket(new SystemMessage(1131).addSkillName(294, level));
            } else {
                sendPacket(new SystemMessage(1132).addSkillName(294, level));
            }
        }
        sendChanges();
    }

    public int getZoneMask() {
        return _zoneMask;
    }


    //TODO [G1ta0] переработать в лисенер?
    @Override
    protected void onUpdateZones(final List<Zone> leaving, final List<Zone> entering) {
        super.onUpdateZones(leaving, entering);

        final boolean lastInCombatZone = (_zoneMask & 0x4000) == 0x4000;
        final boolean lastInDangerArea = (_zoneMask & 0x100) == 0x100;
        final boolean lastOnSiegeField = (_zoneMask & 0x800) == 0x800;
        final boolean lastInPeaceZone = (_zoneMask & 0x1000) == 0x1000;
        final boolean isInCombatZone = isInCombatZone();
        final boolean isInDangerArea = isInDangerArea();
        final boolean isInFunZone = isInZone(ZoneType.fun);
        final boolean isOnSiegeField = isOnSiegeField() || isInFunZone;
        final boolean isInPeaceZone = isInPeaceZone();
        final boolean isInSSQZone = isInSSQZone();
        final int lastZoneMask = _zoneMask;
        _zoneMask = 0;
        if (isInCombatZone) {
            _zoneMask |= 0x4000;
        }
        if (isInDangerArea) {
            _zoneMask |= 0x100;
        }
        if (isOnSiegeField) {
            _zoneMask |= 0x800;
        }
        if (isInPeaceZone) {
            _zoneMask |= 0x1000;
        }
        if (isInSSQZone) {
            _zoneMask |= 0x2000;
        }
        if (lastZoneMask != _zoneMask) {
            sendPacket(new ExSetCompassZoneCode(this));
        }
        if (lastInCombatZone != isInCombatZone) {
            broadcastRelationChanged();
        }
        if (lastInDangerArea != isInDangerArea) {
            sendPacket(new EtcStatusUpdate(this));
        }
        if (lastOnSiegeField != isOnSiegeField) {
            broadcastRelationChanged();
            if (isOnSiegeField) {
                sendPacket(Msg.YOU_HAVE_ENTERED_A_COMBAT_ZONE);
                if (Config.FUN_ZONE_FLAG_ON_ENTER && isInFunZone && !isTeleporting() && getPvpFlag() == 0) {
                    startPvPFlag(null);
                }
            } else {
                sendPacket(Msg.YOU_HAVE_LEFT_A_COMBAT_ZONE);
                if (!isTeleporting() && getPvpFlag() == 0) {
                    startPvPFlag(null);
                }
            }
        }
        if (isInPeaceZone && !lastInPeaceZone) {
            final FlagItemAttachment attachment = getActiveWeaponFlagAttachment();
            if (attachment != null) {
                attachment.onEnterPeace(this);
            }
        }
        if (isInWater()) {
            startWaterTask();
        } else {
            stopWaterTask();
        }
    }

    public void startAutoSaveTask() {
        if (!Config.AUTOSAVE) {
            return;
        }
        if (_autoSaveTask == null) {
            _autoSaveTask = AutoSaveManager.getInstance().addAutoSaveTask(this);
        }
    }

    public void stopAutoSaveTask() {
        if (_autoSaveTask != null) {
            _autoSaveTask.cancel(false);
        }
        _autoSaveTask = null;
    }

    public void startPcBangPointsTask() {
        if (!Config.ALT_PCBANG_POINTS_ENABLED || Config.ALT_PCBANG_POINTS_DELAY <= 0) {
            return;
        }
        if (_pcCafePointsTask == null) {
            _pcCafePointsTask = LazyPrecisionTaskManager.getInstance().addPCCafePointsTask(this);
        }
    }

    public void stopPcBangPointsTask() {
        if (_pcCafePointsTask != null) {
            _pcCafePointsTask.cancel(false);
        }
        _pcCafePointsTask = null;
    }

    public void startUnjailTask(final Player player, final int time) {
        if (_unjailTask != null) {
            _unjailTask.cancel(false);
        }
        _unjailTask = ThreadPoolManager.getInstance().schedule(new UnJailTask(player), time * 60000);
    }

    public void stopUnjailTask() {
        if (_unjailTask != null) {
            _unjailTask.cancel(false);
        }
        _unjailTask = null;
    }

    @Override
    public void sendMessage(final String message) {
        sendPacket(new SystemMessage(message));
    }

    public void sendAdminMessage(final String message) {
        sendPacket(new Say2(0, ChatType.ALL, "SYS", message));
    }

    public void sendHTMLMessage(final String message) {
        sendPacket(new Say2(0, ChatType.ALL, "HTML", message));
    }

    public void sendDebugMessage(final String message) {
        sendPacket(new Say2(0, ChatType.ALL, "BUG", message));
    }

    public Location getLastClientPosition() {
        return _lastClientPosition;
    }

    public void setLastClientPosition(final Location position) {
        _lastClientPosition = position;
    }

    public Location getLastServerPosition() {
        return _lastServerPosition;
    }

    public void setLastServerPosition(final Location position) {
        _lastServerPosition = position;
    }

    public int getUseSeed() {
        return _useSeed;
    }

    public void setUseSeed(final int id) {
        _useSeed = id;
    }

    public int getRelation(final Player target) {
        int result = 0;
        if (getClan() != null) {
            result |= 0x40;
        }
        if (isClanLeader()) {
            result |= 0x80;
        }
        final Party party = getParty();
        if (party != null && party == target.getParty()) {
            result |= 0x20;
            switch (party.getPartyMembers().indexOf(this)) {
                case 0: {
                    result |= 0x10;
                    break;
                }
                case 1: {
                    result |= 0x8;
                    break;
                }
                case 2: {
                    result |= 0x7;
                    break;
                }
                case 3: {
                    result |= 0x6;
                    break;
                }
                case 4: {
                    result |= 0x5;
                    break;
                }
                case 5: {
                    result |= 0x4;
                    break;
                }
                case 6: {
                    result |= 0x3;
                    break;
                }
                case 7: {
                    result |= 0x2;
                    break;
                }
                case 8: {
                    result |= 0x1;
                    break;
                }
            }
        }
        final Clan clan1 = getClan();
        final Clan clan2 = target.getClan();
        if (clan1 != null && clan2 != null && target.getPledgeType() != -1 && getPledgeType() != -1 && clan2.isAtWarWith(clan1.getClanId())) {
            result |= 0x10000;
            if (clan1.isAtWarWith(clan2.getClanId())) {
                result |= 0x8000;
            }
        }
        for (final GlobalEvent e : getEvents()) {
            result = e.getRelation(this, target, result);
        }
        return result;
    }

    public long getlastPvpAttack() {
        return _lastPvpAttack;
    }

    @Override
    public void startPvPFlag(final Creature target) {
        if (_karma > 0) {
            return;
        }
        long startTime = System.currentTimeMillis();
        if (target != null && target.getPvpFlag() != 0) {
            startTime -= Math.max(0, Config.PVP_TIME - Config.PVP_FLAG_ON_UN_FLAG_TIME);
        }
        if (_pvpFlag != 0 && _lastPvpAttack > startTime) {
            return;
        }
        _lastPvpAttack = startTime;
        updatePvPFlag(1);
        if (_PvPRegTask == null) {
            _PvPRegTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new PvPFlagTask(this), 1000L, 1000L);
        }
    }

    public void stopPvPFlag() {
        if (_PvPRegTask != null) {
            _PvPRegTask.cancel(false);
            _PvPRegTask = null;
        }
        updatePvPFlag(0);
    }

    public void updatePvPFlag(final int value) {
        if (_pvpFlag == value) {
            return;
        }
        setPvpFlag(value);
        sendStatusUpdate(true, true, 26);
        broadcastRelationChanged();
    }

    @Override
    public int getPvpFlag() {
        return _pvpFlag;
    }

    public void setPvpFlag(final int pvpFlag) {
        _pvpFlag = pvpFlag;
    }

    public boolean isInDuel() {
        return getEvent(DuelEvent.class) != null;
    }

    public TamedBeastInstance getTrainedBeast() {
        return _tamedBeast;
    }

    public void setTrainedBeast(final TamedBeastInstance tamedBeast) {
        _tamedBeast = tamedBeast;
    }

    public AgathionInstance getAgathion()
    {
        return _agathion;
    }

    public void setAgathion(final AgathionInstance agathion) {
        _agathion = agathion;
    }

    public long getLastAttackPacket() {
        return _lastAttackPacket;
    }

    public void setLastAttackPacket() {
        _lastAttackPacket = System.currentTimeMillis();
    }

    public byte[] getKeyBindings() {
        return _keyBindings;
    }

    public void setKeyBindings(byte[] keyBindings) {
        if (keyBindings == null) {
            keyBindings = ArrayUtils.EMPTY_BYTE_ARRAY;
        }
        _keyBindings = keyBindings;
    }

    private void preparateToTransform(final Skill transSkill) {
        if (transSkill == null || !transSkill.isBaseTransformation()) {
            getEffectList().getAllEffects().stream().filter(effect -> effect != null && effect.getSkill().isToggle()).forEach(Effect::exit);
        }
    }

    public boolean isInFlyingTransform() {
        return _transformationId == 8 || _transformationId == 9 || _transformationId == 260;
    }

    public boolean isInMountTransform() {
        return _transformationId == 106 || _transformationId == 109 || _transformationId == 110 || _transformationId == 20001;
    }

    public int getTransformation() {
        return _transformationId;
    }

    public void setTransformation(final int transformationId) {
        if (transformationId == _transformationId || (_transformationId != 0 && transformationId != 0)) {
            return;
        }
        if (transformationId == 0) {
            for (final Effect effect : getEffectList().getAllEffects()) {
                if (effect != null && effect.getEffectType() == EffectType.Transformation) {
                    if (effect.calc() == 0.0) {
                        continue;
                    }
                    effect.exit();
                    preparateToTransform(effect.getSkill());
                    break;
                }
            }
            if (!_transformationSkills.isEmpty()) {
                _transformationSkills.values().stream().filter(s -> !s.isCommon() && !SkillAcquireHolder.getInstance().isSkillPossible(this, s) && !s.isHeroic()).forEach(super::removeSkill);
                _transformationSkills.clear();
            }
        } else {
            if (!isCursedWeaponEquipped()) {
                for (final Effect effect : getEffectList().getAllEffects()) {
                    if (effect != null && effect.getEffectType() == EffectType.Transformation) {
                        if (effect.getSkill() instanceof Transformation && ((Transformation) effect.getSkill()).isDisguise) {
                            getAllSkills().stream().filter(s2 -> s2 != null && (s2.isActive() || s2.isToggle())).forEach(s2 -> _transformationSkills.put(s2.getId(), s2));
                        } else {
                            for (final AddedSkill s3 : effect.getSkill().getAddedSkills()) {
                                switch (s3.level) {
                                    case 0:
                                        final int s4 = getSkillLevel(s3.id);
                                        if (s4 > 0) {
                                            _transformationSkills.put(s3.id, SkillTable.getInstance().getInfo(s3.id, s4));
                                        }
                                        break;
                                    case -2:
                                        final int learnLevel = Math.max(effect.getSkill().getMagicLevel(), 40);
                                        final int maxLevel = SkillTable.getInstance().getBaseLevel(s3.id);
                                        int curSkillLevel = 1;
                                        if (maxLevel > 3) {
                                            curSkillLevel += getLevel() - learnLevel;
                                        } else {
                                            curSkillLevel += (getLevel() - learnLevel) / ((76 - learnLevel) / maxLevel);
                                        }
                                        curSkillLevel = Math.min(Math.max(curSkillLevel, 1), maxLevel);
                                        _transformationSkills.put(s3.id, SkillTable.getInstance().getInfo(s3.id, curSkillLevel));
                                        break;
                                    default:
                                        _transformationSkills.put(s3.id, s3.getSkill());
                                        break;
                                }
                            }
                        }
                        preparateToTransform(effect.getSkill());
                        break;
                    }
                }
            } else {
                preparateToTransform(null);
            }
            if (!isOlyParticipant() && isCursedWeaponEquipped() && _hero && getBaseClassId() == getActiveClassId()) {
                _transformationSkills.put(395, SkillTable.getInstance().getInfo(395, 1));
                _transformationSkills.put(396, SkillTable.getInstance().getInfo(396, 1));
                _transformationSkills.put(1374, SkillTable.getInstance().getInfo(1374, 1));
                _transformationSkills.put(1375, SkillTable.getInstance().getInfo(1375, 1));
                _transformationSkills.put(1376, SkillTable.getInstance().getInfo(1376, 1));
            }
            _transformationSkills.values().forEach(s -> addSkill(s, false));
        }
        _transformationId = transformationId;
        sendPacket(new ExBasicActionList(this));
        sendPacket(new SkillList(this));
        sendPacket(new ShortCutInit(this));
        getAutoSoulShot().stream().mapToInt(shotId -> shotId).mapToObj(shotId -> new ExAutoSoulShot(shotId, true)).forEach(this::sendPacket);
        broadcastUserInfo(true);
    }

    public String getTransformationName() {
        return _transformationName;
    }

    public void setTransformationName(final String name) {
        _transformationName = name;
    }

    public String getTransformationTitle() {
        return _transformationTitle;
    }

    public void setTransformationTitle(final String transformationTitle) {
        _transformationTitle = transformationTitle;
    }

    public int getTransformationTemplate() {
        return _transformationTemplate;
    }

    public void setTransformationTemplate(final int template) {
        _transformationTemplate = template;
    }

    @Override
    public final Collection<Skill> getAllSkills() {
        if (_transformationId == 0) {
            return super.getAllSkills();
        }
        final Map<Integer, Skill> tempSkills = super.getAllSkills().stream().filter(s -> s != null && !s.isActive() && !s.isToggle()).collect(Collectors.toMap(Skill::getId, s -> s, (a, b) -> b));
        tempSkills.putAll(_transformationSkills);
        return tempSkills.values();
    }

    public int getPcBangPoints() {
        return _pcBangPoints;
    }

    public void setPcBangPoints(final int val) {
        _pcBangPoints = val;
    }

    public void addPcBangPoints(int count, final boolean doublePoints) {
        if (doublePoints) {
            count *= 2;
        }
        _pcBangPoints += count;
        sendPacket(new SystemMessage(doublePoints ? 1708 : 1707).addNumber(count));
        sendPacket(new ExPCCafePointInfo(this, count, 1, 2, 12));
    }

    public boolean reducePcBangPoints(final int count) {
        if (_pcBangPoints < count) {
            return false;
        }
        _pcBangPoints -= count;
        sendPacket(new SystemMessage(1709).addNumber(count));
        sendPacket(new ExPCCafePointInfo(this, 0, 1, 2, 12));
        return true;
    }

    public Location getGroundSkillLoc() {
        return _groundSkillLoc;
    }

    public void setGroundSkillLoc(final Location location) {
        _groundSkillLoc = location;
    }

    public boolean isLogoutStarted() {
        return _isLogout.get();
    }

    public void setOfflineMode(final boolean val) {
        if (!val) {
            unsetVar("offline");
        }
        _offline = val;
    }

    public boolean isInOfflineMode() {
        return _offline;
    }

    public void saveTradeList() {
        StringBuilder val = new StringBuilder();
        if (_sellList == null || _sellList.isEmpty()) {
            unsetVar("selllist");
        } else {
            for (final TradeItem i : _sellList) {
                val.append(i.getObjectId()).append(";").append(i.getCount()).append(";").append(i.getOwnersPrice()).append(":");
            }
            setVar("selllist", val.toString(), -1L);
            val = new StringBuilder();
            if (_tradeList != null && getSellStoreName() != null) {
                setVar("sellstorename", getSellStoreName(), -1L);
            }
        }
        if (_packageSellList == null || _packageSellList.isEmpty()) {
            unsetVar("packageselllist");
        } else {
            for (final TradeItem i : _packageSellList) {
                val.append(i.getObjectId()).append(";").append(i.getCount()).append(";").append(i.getOwnersPrice()).append(":");
            }
            setVar("packageselllist", val.toString(), -1L);
            val = new StringBuilder();
            if (_tradeList != null && getSellStoreName() != null) {
                setVar("sellstorename", getSellStoreName(), -1L);
            }
        }
        if (_buyList == null || _buyList.isEmpty()) {
            unsetVar("buylist");
        } else {
            for (final TradeItem i : _buyList) {
                val.append(i.getItemId()).append(";").append(i.getCount()).append(";").append(i.getOwnersPrice()).append(":");
            }
            setVar("buylist", val.toString(), -1L);
            val = new StringBuilder();
            if (_tradeList != null && getBuyStoreName() != null) {
                setVar("buystorename", getBuyStoreName(), -1L);
            }
        }
        if (_createList == null || _createList.isEmpty()) {
            unsetVar("createlist");
        } else {
            for (final ManufactureItem j : _createList) {
                val.append(j.getRecipeId()).append(";").append(j.getCost()).append(":");
            }
            setVar("createlist", val.toString(), -1L);
            if (getManufactureName() != null) {
                setVar("manufacturename", getManufactureName(), -1L);
            }
        }
    }

    public void restoreTradeList() {
        String var = getVar("selllist");
        if (var != null) {
            _sellList = new CopyOnWriteArrayList<>();
            for (final String item : var.split(":")) {
                if (!"".equals(item)) {
                    final String[] values = item.split(";");
                    if (values.length >= 3) {
                        final int oId = Integer.parseInt(values[0]);
                        long count = Long.parseLong(values[1]);
                        final long price = Long.parseLong(values[2]);
                        final ItemInstance itemToSell = getInventory().getItemByObjectId(oId);
                        if (count >= 1L) {
                            if (itemToSell != null) {
                                if (count > itemToSell.getCount()) {
                                    count = itemToSell.getCount();
                                }
                                final TradeItem i = new TradeItem(itemToSell);
                                i.setCount(count);
                                i.setOwnersPrice(price);
                                _sellList.add(i);
                            }
                        }
                    }
                }
            }
            var = getVar("sellstorename");
            if (var != null) {
                setSellStoreName(var);
            }
        }
        var = getVar("packageselllist");
        if (var != null) {
            _packageSellList = new CopyOnWriteArrayList<>();
            for (final String item : var.split(":")) {
                if (!"".equals(item)) {
                    final String[] values = item.split(";");
                    if (values.length >= 3) {
                        final int oId = Integer.parseInt(values[0]);
                        long count = Long.parseLong(values[1]);
                        final long price = Long.parseLong(values[2]);
                        final ItemInstance itemToSell = getInventory().getItemByObjectId(oId);
                        if (count >= 1L) {
                            if (itemToSell != null) {
                                if (count > itemToSell.getCount()) {
                                    count = itemToSell.getCount();
                                }
                                final TradeItem i = new TradeItem(itemToSell);
                                i.setCount(count);
                                i.setOwnersPrice(price);
                                _packageSellList.add(i);
                            }
                        }
                    }
                }
            }
            var = getVar("sellstorename");
            if (var != null) {
                setSellStoreName(var);
            }
        }
        var = getVar("buylist");
        if (var != null) {
            _buyList = new CopyOnWriteArrayList<>();
            for (final String item : var.split(":")) {
                if (!"".equals(item)) {
                    final String[] values = item.split(";");
                    if (values.length >= 3) {
                        final TradeItem j = new TradeItem();
                        j.setItemId(Integer.parseInt(values[0]));
                        j.setCount(Long.parseLong(values[1]));
                        j.setOwnersPrice(Long.parseLong(values[2]));
                        _buyList.add(j);
                    }
                }
            }
            var = getVar("buystorename");
            if (var != null) {
                setBuyStoreName(var);
            }
        }
        var = getVar("createlist");
        if (var != null) {
            _createList = new CopyOnWriteArrayList<>();
            for (final String item : var.split(":")) {
                if (!"".equals(item)) {
                    final String[] values = item.split(";");
                    if (values.length >= 2) {
                        final int recId = Integer.parseInt(values[0]);
                        final long price2 = Long.parseLong(values[1]);
                        if (findRecipe(recId)) {
                            _createList.add(new ManufactureItem(recId, price2));
                        }
                    }
                }
            }
            var = getVar("manufacturename");
            if (var != null) {
                setManufactureName(var);
            }
        }
    }

    public void restoreRecipeBook() {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT id FROM character_recipebook WHERE char_id=?");
            statement.setInt(1, getObjectId());
            rset = statement.executeQuery();
            while (rset.next()) {
                final int recipeId = rset.getInt("id");
                final Recipe recipe = RecipeHolder.getInstance().getRecipeById(recipeId);
                registerRecipe(recipe, false);
            }
        } catch (Exception e) {
            LOGGER.warn("count not recipe skills:" + e);
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
    }

    public int getMountType() {
        switch (getMountNpcId()) {
            case 12526:
            case 12527:
            case 12528:
            case 16038:
            case 16039:
            case 16040:
            case 16068: {
                return 1;
            }
            case 12621: {
                return 2;
            }
            case 16037:
            case 16041:
            case 16042: {
                return 3;
            }
            default: {
                return 0;
            }
        }
    }

    @Override
    public double getColRadius() {
        if (getTransformation() != 0) {
            final int template = getTransformationTemplate();
            if (template != 0) {
                final NpcTemplate npcTemplate = NpcTemplateHolder.getInstance().getTemplate(template);
                if (npcTemplate != null) {
                    return npcTemplate.getCollisionRadius();
                }
            }
        } else if (isMounted()) {
            final int mountTemplate = getMountNpcId();
            if (mountTemplate != 0) {
                final NpcTemplate mountNpcTemplate = NpcTemplateHolder.getInstance().getTemplate(mountTemplate);
                if (mountNpcTemplate != null) {
                    return mountNpcTemplate.getCollisionRadius();
                }
            }
        }
        return getBaseTemplate().getCollisionRadius();
    }

    @Override
    public double getColHeight() {
        if (getTransformation() != 0) {
            final int template = getTransformationTemplate();
            if (template != 0) {
                final NpcTemplate npcTemplate = NpcTemplateHolder.getInstance().getTemplate(template);
                if (npcTemplate != null) {
                    return npcTemplate.getCollisionHeight();
                }
            }
        } else if (isMounted()) {
            final int mountTemplate = getMountNpcId();
            if (mountTemplate != 0) {
                final NpcTemplate mountNpcTemplate = NpcTemplateHolder.getInstance().getTemplate(mountTemplate);
                if (mountNpcTemplate != null) {
                    return mountNpcTemplate.getCollisionHeight();
                }
            }
        }
        return getBaseTemplate().getCollisionHeight();
    }

    @Override
    public void setReflection(final Reflection reflection) {
        if (getReflection() == reflection) {
            return;
        }
        super.setReflection(reflection);
        if (_summon != null && !_summon.isDead()) {
            _summon.setReflection(reflection);
        }
        if (reflection != ReflectionManager.DEFAULT) {
            final String var = getVar("reflection");
            if (!String.valueOf(reflection.getId()).equals(var)) {
                setVar("reflection", String.valueOf(reflection.getId()), -1L);
            }
        } else {
            unsetVar("reflection");
        }
        if (getActiveClass() != null) {
            getInventory().validateItems();
            if (getPet() != null && (getPet().getNpcId() == 14916 || getPet().getNpcId() == 14917)) {
                getPet().unSummon();
            }
        }
    }

    public int getBuyListId() {
        return _buyListId;
    }

    public void setBuyListId(final int listId) {
        _buyListId = listId;
    }

    public int getIncorrectValidateCount() {
        return _incorrectValidateCount;
    }

    public int setIncorrectValidateCount(final int count) {
        return _incorrectValidateCount = count;
    }

    public int getExpandInventory() {
        return _expandInventory;
    }

    public void setExpandInventory(final int inventory) {
        _expandInventory = inventory;
    }

    public int getExpandWarehouse() {
        return _expandWarehouse;
    }

    public void setExpandWarehouse(final int warehouse) {
        _expandWarehouse = warehouse;
    }

    public void enterMovieMode() {
        if (isInMovie()) {
            return;
        }
        setTarget(null);
        stopMove();
        setIsInMovie(true);
        sendPacket(new CameraMode(1));
    }

    public void leaveMovieMode() {
        setIsInMovie(false);
        sendPacket(new CameraMode(0));
        broadcastCharInfo();
    }

    public void specialCamera(final GameObject target, final int dist, final int yaw, final int pitch, final int time, final int duration) {
        sendPacket(new SpecialCamera(target.getObjectId(), dist, yaw, pitch, time, duration));
    }

    public void specialCamera(final GameObject target, final int dist, final int yaw, final int pitch, final int time, final int duration, final int turn, final int rise, final int widescreen, final int unk) {
        sendPacket(new SpecialCamera(target.getObjectId(), dist, yaw, pitch, time, duration, turn, rise, widescreen, unk));
    }

    public int getMovieId() {
        return _movieId;
    }

    public void setMovieId(final int id) {
        _movieId = id;
    }

    public boolean isInMovie() {
        return _isInMovie;
    }

    public void setIsInMovie(final boolean state) {
        _isInMovie = state;
    }

    public void showQuestMovie(final SceneMovie movie) {
        if (isInMovie()) {
            return;
        }
        sendActionFailed();
        setTarget(null);
        stopMove();
        setMovieId(movie.getId());
        setIsInMovie(true);
        sendPacket(movie.packet(this));
    }

    public void showQuestMovie(final int movieId) {
        if (isInMovie()) {
            return;
        }
        sendActionFailed();
        setTarget(null);
        stopMove();
        setMovieId(movieId);
        setIsInMovie(true);
        sendPacket(new ExStartScenePlayer(movieId));
    }

    public void setAutoLoot(final boolean enable) {
        if (Config.AUTO_LOOT_INDIVIDUAL) {
            _autoLoot = enable;
            setVar("AutoLoot", String.valueOf(enable), -1L);
        }
    }

    public void setAutoLootHerbs(final boolean enable) {
        if (Config.AUTO_LOOT_INDIVIDUAL) {
            AutoLootHerbs = enable;
            setVar("AutoLootHerbs", String.valueOf(enable), -1L);
        }
    }

    public void setAutoLootAdena(final boolean enable) {
        if (Config.AUTO_LOOT_INDIVIDUAL) {
            AutoLootAdena = enable;
            setVar("AutoLootAdend", String.valueOf(enable), -1L);
        }
    }

    public boolean isAutoLootEnabled() {
        return _autoLoot;
    }

    public boolean isAutoLootHerbsEnabled() {
        return AutoLootHerbs;
    }

    public boolean isAutoLootAdenaEnabled() {
        return AutoLootAdena;
    }

    public final void reName(final String name, final boolean saveToDB) {
        setName(name);
        if (saveToDB) {
            saveNameToDB();
        }
        if (isNoble()) {
            NoblessManager.getInstance().renameNoble(getObjectId(), name);
        }
        broadcastCharInfo();
    }

    public final void reName(final String name) {
        reName(name, false);
    }

    public final void saveNameToDB() {
        Connection con = null;
        PreparedStatement st = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            st = con.prepareStatement("UPDATE characters SET char_name = ? WHERE obj_Id = ?");
            st.setString(1, getName());
            st.setInt(2, getObjectId());
            st.executeUpdate();
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, st);
        }
    }

    @Override
    public Player getPlayer() {
        return this;
    }

    public int getTalismanCount() {
        return (int) calcStat(Stats.TALISMANS_LIMIT, 0.0, null, null);
    }

    public final void disableDrop(final int time) {
        _dropDisabled = System.currentTimeMillis() + time;
    }

    public final boolean isDropDisabled() {
        return _dropDisabled > System.currentTimeMillis();
    }

    public void setPetControlItem(final int itemObjId) {
        setPetControlItem(getInventory().getItemByObjectId(itemObjId));
    }

    public ItemInstance getPetControlItem() {
        return _petControlItem;
    }

    public void setPetControlItem(final ItemInstance item) {
        _petControlItem = item;
    }

    public boolean isActive() {
        return isActive.get();
    }

    public void setActive() {
        setNonAggroTime(0L);
        if (isActive.getAndSet(true)) {
            return;
        }
        onActive();
    }

    private void onActive() {
        setNonAggroTime(System.currentTimeMillis() + Config.NONAGGRO_TIME_ONLOGIN);
        if (getPetControlItem() != null) {
            ThreadPoolManager.getInstance().execute(new RunnableImpl() {
                @Override
                public void runImpl() {
                    if (getPetControlItem() != null) {
                        summonPet();
                    }
                }
            });
        }
    }

    public void summonPet() {
        if (getPet() != null) {
            return;
        }
        final ItemInstance controlItem = getPetControlItem();
        if (controlItem == null) {
            return;
        }
        final int npcId = PetDataTable.getSummonId(controlItem);
        if (npcId == 0) {
            return;
        }
        final NpcTemplate petTemplate = NpcTemplateHolder.getInstance().getTemplate(npcId);
        if (petTemplate == null) {
            return;
        }
        final PetInstance pet = PetInstance.restore(controlItem, petTemplate, this);
        if (pet == null) {
            return;
        }
        setPet(pet);
        pet.setTitle(getName());
        if (!pet.isRespawned()) {
            pet.setCurrentHp(pet.getMaxHp(), false);
            pet.setCurrentMp(pet.getMaxMp());
            pet.setCurrentFed(pet.getMaxFed());
            pet.updateControlItem();
            pet.store();
        }
        pet.getInventory().restore();
        pet.setNonAggroTime(System.currentTimeMillis() + Config.NONAGGRO_TIME_ONTELEPORT);
        pet.setReflection(getReflection());
        pet.spawnMe(Location.findPointToStay(this, 50, 70));
        pet.setRunning();
        pet.setFollowMode(true);
        pet.getInventory().validateItems();
        if (pet instanceof PetBabyInstance) {
            ((PetBabyInstance) pet).startBuffTask();
        }
    }

    public Collection<TrapInstance> getTraps() {
        if (_traps == null) {
            return null;
        }
        final Collection<TrapInstance> result = new ArrayList<>(getTrapsCount());
        for (final int trapId : _traps.values()) {
            final TrapInstance trap;
            if ((trap = (TrapInstance) GameObjectsStorage.getNpc(trapId)) != null) {
                result.add(trap);
            } else {
                _traps.remove(trapId);
            }
        }
        return result;
    }

    public int getTrapsCount() {
        return (_traps == null) ? 0 : _traps.size();
    }

    public void addTrap(final TrapInstance trap) {
        if (_traps == null) {
            _traps = new HashMap<>();
        }
        _traps.put(trap.getObjectId(), trap.getObjectId());
    }

    public void removeTrap(final TrapInstance trap) {
        final Map<Integer, Integer> traps = _traps;
        if (traps == null || traps.isEmpty()) {
            return;
        }
        traps.remove(trap.getObjectId());
    }

    public void destroyFirstTrap() {
        final Map<Integer, Integer> traps = _traps;
        if (traps == null || traps.isEmpty()) {
            return;
        }
        final Iterator<Integer> iterator = traps.keySet().iterator();
        if (!iterator.hasNext()) {
            return;
        }
        final int trapId = iterator.next();
        final TrapInstance trap;
        if ((trap = (TrapInstance) GameObjectsStorage.getNpc(traps.get(trapId))) != null) {
            trap.deleteMe();
        }
    }

    public void destroyAllTraps() {
        final Map<Integer, Integer> traps = _traps;
        if (traps == null || traps.isEmpty()) {
            return;
        }
        final List<TrapInstance> toRemove = traps.values().stream().mapToInt(trapId -> trapId).mapToObj(trapId -> (TrapInstance) GameObjectsStorage.getNpc(trapId)).collect(Collectors.toList());
        toRemove.stream().filter(Objects::nonNull).forEach(GameObject::deleteMe);
    }

    @Override
    public PlayerListenerList getListeners() {
        if (listeners == null) {
            synchronized (this) {
                if (listeners == null) {
                    listeners = new PlayerListenerList(this);
                }
            }
        }
        return (PlayerListenerList) listeners;
    }

    @Override
    public PlayerStatsChangeRecorder getStatsRecorder() {
        if (_statsRecorder == null) {
            synchronized (this) {
                if (_statsRecorder == null) {
                    _statsRecorder = new PlayerStatsChangeRecorder(this);
                }
            }
        }
        return (PlayerStatsChangeRecorder) _statsRecorder;
    }

    public int getHoursInGame() {
        return ++_hoursInGame;
    }

    public void startHourlyTask() {
        _hourlyTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new HourlyTask(this), 3600000L, 3600000L);
    }

    public void stopHourlyTask() {
        if (_hourlyTask != null) {
            _hourlyTask.cancel(false);
            _hourlyTask = null;
        }
    }

    public long getPremiumPoints() {
        if (Config.GAME_POINT_ITEM_ID > 0) {
            return ItemFunctions.getItemCount(this, Config.GAME_POINT_ITEM_ID);
        }
        return 0L;
    }

    public void reducePremiumPoints(final int val) {
        if (Config.GAME_POINT_ITEM_ID > 0) {
            ItemFunctions.removeItem(this, Config.GAME_POINT_ITEM_ID, val, true);
        }
    }

    public String getSessionVar(final String key) {
        if (_userSession == null) {
            return null;
        }
        return _userSession.get(key);
    }

    public void setSessionVar(final String key, final String val) {
        if (_userSession == null) {
            _userSession = new ConcurrentHashMap<>();
        }
        if (val == null || val.isEmpty()) {
            _userSession.remove(key);
        } else {
            _userSession.put(key, val);
        }
    }

    public FriendList getFriendList() {
        return _friendList;
    }

    public boolean isNotShowTraders() {
        return _notShowTraders;
    }

    public void setNotShowTraders(final boolean notShowTraders) {
        _notShowTraders = notShowTraders;
    }

    public boolean isDebug() {
        return _debug;
    }

    public void setDebug(final boolean b) {
        _debug = b;
    }

    public void sendItemList(final boolean show) {
        final List<ItemInstance> items = getInventory().getItems();
        final LockType lockType = getInventory().getLockType();
        final int[] lockItems = getInventory().getLockItems();
        sendPacket(new ItemList(items.size(), items, show, lockType, lockItems));
    }

    public void sendSkillList() {
        sendPacket(new SkillList(this));
    }

    @Override
    public boolean isPlayer() {
        return true;
    }

    @Override
    public void startAttackStanceTask() {
        startAttackStanceTask0();
        final Summon summon = getPet();
        if (summon != null) {
            summon.startAttackStanceTask0();
        }
    }

    @Override
    public void displayGiveDamageMessage(final Creature target, final int damage, final boolean crit, final boolean miss, final boolean shld, final boolean magic) {
        super.displayGiveDamageMessage(target, damage, crit, miss, shld, magic);
        if (crit) {
            if (magic) {
                sendPacket(new SystemMessage(1280));
            } else {
                sendPacket(new SystemMessage(44));
            }
        }
        if (miss) {
            sendPacket(new SystemMessage(43));
        } else if (!target.isDamageBlocked()) {
            sendPacket(new SystemMessage(35).addNumber(damage));
        }
        if (target.isPlayer()) {
            if (shld && damage > 1) {
                target.sendPacket(SystemMsg.YOUR_SHIELD_DEFENSE_HAS_SUCCEEDED);
            } else if (shld && damage == 1) {
                target.sendPacket(SystemMsg.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
            }
        }
    }

    @Override
    public void displayReceiveDamageMessage(final Creature attacker, final int damage) {
        if (attacker != this) {
            sendPacket(new SystemMessage(36).addName(attacker).addNumber((long) damage));
        }
    }

    public Map<Integer, String> getPostFriends() {
        return _postFriends;
    }

    public boolean isSharedGroupDisabled(final int groupId) {
        final TimeStamp sts = _sharedGroupReuses.get(groupId);
        if (sts == null) {
            return false;
        }
        if (sts.hasNotPassed()) {
            return true;
        }
        _sharedGroupReuses.remove(groupId);
        return false;
    }

    public TimeStamp getSharedGroupReuse(final int groupId) {
        return _sharedGroupReuses.get(groupId);
    }

    public void addSharedGroupReuse(final int group, final TimeStamp stamp) {
        _sharedGroupReuses.put(group, stamp);
    }

    public Collection<Map.Entry<Integer, TimeStamp>> getSharedGroupReuses() {
        return _sharedGroupReuses.entrySet();
    }

    public void sendReuseMessage(final ItemInstance item) {
        final TimeStamp sts = getSharedGroupReuse(item.getTemplate().getReuseGroup());
        if (sts == null || !sts.hasNotPassed()) {
            sendPacket(new SystemMessage(48).addItemName(item.getTemplate().getItemId()));
        }
    }

    public void ask(final ConfirmDlg dlg, final OnAnswerListener listener) {
        if (_askDialog != null) {
            return;
        }
        final int rnd = Rnd.nextInt();
        _askDialog = new ImmutablePair<>(rnd, listener);
        dlg.setRequestId(rnd);
        sendPacket(dlg);
    }

    public Pair<Integer, OnAnswerListener> getAskListener(final boolean clear) {
        if (!clear) {
            return _askDialog;
        }
        final Pair<Integer, OnAnswerListener> ask = _askDialog;
        _askDialog = null;
        return ask;
    }

    @Override
    public boolean isDead() {
        return (isOlyParticipant() || isInDuel()) ? (getCurrentHp() <= 1.0) : super.isDead();
    }

    public boolean hasPrivilege(final Privilege privilege) {
        return _clan != null && (getClanPrivileges() & privilege.mask()) == privilege.mask();
    }

    public MatchingRoom getMatchingRoom() {
        return _matchingRoom;
    }

    public void setMatchingRoom(final MatchingRoom matchingRoom) {
        _matchingRoom = matchingRoom;
    }

    public void dispelBuffs() {
        getEffectList().getAllEffects().stream().filter(e -> !e.getSkill().isOffensive() && !e.getSkill().isNewbie() && e.isCancelable() && !e.getSkill().isPreservedOnDeath()).forEach(e -> {
            sendPacket(new SystemMessage(749).addSkillName(e.getSkill().getId(), e.getSkill().getLevel()));
            e.exit();
        });
        if (getPet() != null) {
            getPet().getEffectList().getAllEffects().stream().filter(e -> !e.getSkill().isOffensive() && !e.getSkill().isNewbie() && e.isCancelable() && !e.getSkill().isPreservedOnDeath()).forEach(Effect::exit);
        }
    }

    public void setInstanceReuse(final int id, final long time) {
        final CustomMessage msg = new CustomMessage("INSTANT_ZONE_FROM_HERE__S1_S_ENTRY_HAS_BEEN_RESTRICTED_YOU_CAN_CHECK_THE_NEXT_ENTRY_POSSIBLE", this).addString(getName());
        sendMessage(msg);
        _instancesReuses.put(id, time);
        mysql.set("REPLACE INTO character_instances (obj_id, id, reuse) VALUES (?,?,?)", getObjectId(), id, time);
    }

    public void removeInstanceReuse(final int id) {
        if (_instancesReuses.remove(id) != null) {
            mysql.set("DELETE FROM `character_instances` WHERE `obj_id`=? AND `id`=? LIMIT 1", getObjectId(), id);
        }
    }

    public void removeAllInstanceReuses() {
        _instancesReuses.clear();
        mysql.set("DELETE FROM `character_instances` WHERE `obj_id`=?", getObjectId());
    }

    public void removeInstanceReusesByGroupId(final int groupId) {
        for (final int i : InstantZoneHolder.getInstance().getSharedReuseInstanceIdsByGroup(groupId)) {
            if (getInstanceReuse(i) != null) {
                removeInstanceReuse(i);
            }
        }
    }

    public Long getInstanceReuse(final int id) {
        return _instancesReuses.get(id);
    }

    public Map<Integer, Long> getInstanceReuses() {
        return _instancesReuses;
    }

    private void loadInstanceReuses() {
        Connection con = null;
        PreparedStatement offline = null;
        ResultSet rs = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            offline = con.prepareStatement("SELECT * FROM character_instances WHERE obj_id = ?");
            offline.setInt(1, getObjectId());
            rs = offline.executeQuery();
            while (rs.next()) {
                final int id = rs.getInt("id");
                final long reuse = rs.getLong("reuse");
                _instancesReuses.put(id, reuse);
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, offline, rs);
        }
    }

    public Reflection getActiveReflection() {
        for (final Reflection r : ReflectionManager.getInstance().getAll()) {
            if (r != null && ArrayUtils.contains(r.getVisitors(), getObjectId())) {
                return r;
            }
        }
        return null;
    }

    public boolean canEnterInstance(final int instancedZoneId) {
        final InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(instancedZoneId);
        if (isDead()) {
            return false;
        }
        if (ReflectionManager.getInstance().size() > Config.MAX_REFLECTIONS_COUNT) {
            sendMessage(new CustomMessage("THE_MAXIMUM_NUMBER_OF_INSTANCE_ZONES_HAS_BEEN_EXCEEDED", this));
            return false;
        }
        if (iz == null) {
            sendPacket(SystemMsg.SYSTEM_ERROR);
            return false;
        }
        if (ReflectionManager.getInstance().getCountByIzId(instancedZoneId) >= iz.getMaxChannels()) {
            sendMessage(new CustomMessage("THE_MAXIMUM_NUMBER_OF_INSTANCE_ZONES_HAS_BEEN_EXCEEDED", this));
            return false;
        }
        return iz.getEntryType().canEnter(this, iz);
    }

    public boolean canReenterInstance(final int instancedZoneId) {
        final InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(instancedZoneId);
        if (getActiveReflection() != null && getActiveReflection().getInstancedZoneId() != instancedZoneId) {
            sendMessage(new CustomMessage("YOU_HAVE_ENTERED_ANOTHER_INSTANCE_ZONE_THEREFORE_YOU_CANNOT_ENTER_CORRESPONDING_DUNGEON", this));
            return false;
        }
        if (iz.isDispelBuffs()) {
            dispelBuffs();
        }
        return iz.getEntryType().canReEnter(this, iz);
    }

    public int getBattlefieldChatId() {
        return _battlefieldChatId;
    }

    public void setBattlefieldChatId(final int battlefieldChatId) {
        _battlefieldChatId = battlefieldChatId;
    }

    @Override
    public void broadCast(final IStaticPacket... packet) {
        sendPacket(packet);
    }

    @Override
    public Iterator<Player> iterator() {
        return Collections.singleton(this).iterator();
    }

    public PlayerGroup getPlayerGroup() {
        if (getParty() == null) {
            return this;
        }
        if (getParty().getCommandChannel() != null) {
            return getParty().getCommandChannel();
        }
        return getParty();
    }

    public boolean isActionBlocked(final String action) {
        return _blockedActions.contains(action);
    }

    public void blockActions(final String... actions) {
        Collections.addAll(_blockedActions, actions);
    }

    public void unblockActions(final String... actions) {
        for (final String action : actions) {
            _blockedActions.remove(action);
        }
    }

    public void addRadar(final int x, final int y, final int z) {
        sendPacket(new RadarControl(0, 1, x, y, z));
    }

    public void addRadarWithMap(final int x, final int y, final int z) {
        sendPacket(new RadarControl(0, 2, x, y, z));
    }

    public long getAfterTeleportPortectionTime() {
        return _afterTeleportPortectionTime;
    }

    public void setAfterTeleportPortectionTime(final long afterTeleportPortectionTime) {
        _afterTeleportPortectionTime = afterTeleportPortectionTime;
    }

    public AntiFlood getAntiFlood() {
        return antiFlood;
    }

    public boolean isPhantom() {
        return false;
    }

    public boolean isInPvPEvent() {
        return isInTvT() || isInCtF() || isInLastHero()|| isInFightClub() || isInTVTArena() || isInDuelEvent() || isInKoreanStyle();
    }

    public boolean isInTvT() {
        return _InTvT.get();
    }

    public boolean isInDeathMatch() {
        return _InDeathMatch.get();
    }

    public boolean isInCtF() {
        return _inCtF.get();
    }

    public boolean isInLastHero() {
        return _inLastHero.get();
    }

    public boolean isInKoreanStyle() {
        return _inKoreanStyle.get();
    }

    public boolean isInFightClub() {
        return _inFightClub.get();
    }

    public boolean isInTVTArena() {
        return _inTVTArena.get();
    }

    public boolean isInDuelEvent() {
        return _inDuelEvent.get();
    }

    public boolean isNoClanAlyCrest() {
        return _noClanAlyCrest.get();
    }

    public void setNoClanAlyCrest(final boolean param) {
        _noClanAlyCrest.compareAndSet(!param, param);
    }

    public void setIsInTvT(final boolean param) {
        _InTvT.compareAndSet(!param, param);
    }

    public void setIsInDeathMatch(final boolean param) {
        _InDeathMatch.compareAndSet(!param, param);
    }

    public void setIsInCtF(final boolean param) {
        _inCtF.compareAndSet(!param, param);
    }

    public void setIsInKoreanStyle(final boolean param) {
        _inKoreanStyle.compareAndSet(!param, param);
    }

    public void setIsInLastHero(final boolean param) {
        _inLastHero.compareAndSet(!param, param);
    }

    public void setIsInFightClub(final boolean param) {
        _inFightClub.compareAndSet(!param, param);
    }

    public void setIsInTVTArena(final boolean param) {
        _inTVTArena.compareAndSet(!param, param);
    }

    public void setIsInDuelEvent(final boolean param) {
        _inDuelEvent.compareAndSet(!param, param);
    }

    public void setInRegistredEvent(final boolean param) {
        _inRegistredEvent.compareAndSet(!param, param);
    }

    public boolean isRegistredEvent() {
        return _inRegistredEvent.get();
    }

    public boolean isEventFlagEquiped() {
        final ItemInstance weapon = getActiveWeaponInstance();
        return weapon != null && weapon.getTemplate().isCtFFlag();
    }

    public enum EPledgeRank {
        VAGABOND(0),
        VASSAL(1),
        HEIR(2),
        KNIGHT(3),
        WISEMAN(4),
        BARON(5),
        VISCOUNT(6),
        COUNT(7),
        MARQUIS(8);

        public static final EPledgeRank[] VALUES = values();

        private final int _rankId;

        EPledgeRank(final int rankId) {
            _rankId = rankId;
        }

        public static EPledgeRank getPledgeRank(final int pledgeRankId) {
            for (final EPledgeRank pledgeRank : VALUES) {
                if (pledgeRank.getRankId() == pledgeRankId) {
                    return pledgeRank;
                }
            }
            return null;
        }

        public int getRankId() {
            return _rankId;
        }
    }

    private static class MoveToLocationOffloadData {
        private final Location _dest;
        private final int _indent;
        private final boolean _pathfind;

        public MoveToLocationOffloadData(final Location dest, final int indent, final boolean pathfind) {
            _dest = dest;
            _indent = indent;
            _pathfind = pathfind;
        }

        public Location getDest() {
            return _dest;
        }

        public int getIndent() {
            return _indent;
        }

        public boolean isPathfind() {
            return _pathfind;
        }
    }

    private static class MoveToLocationActionForOffload extends MoveToLocationAction {
        public MoveToLocationActionForOffload(final Creature actor, final Location moveFrom, final Location moveTo, final boolean ignoreGeo, final int indent, final boolean pathFind) {
            super(actor, moveFrom, moveTo, ignoreGeo, indent, pathFind);
        }

        private void tryOffloadedMove() {
            final Player player = (Player) getActor();
            MoveToLocationOffloadData mtlOffloadData;
            if (player != null && (mtlOffloadData = player._mtlOffloadData.get()) != null && player._mtlOffloadData.compareAndSet(mtlOffloadData, null)) {
                player.moveToLocation(mtlOffloadData.getDest(), mtlOffloadData.getIndent(), mtlOffloadData.isPathfind());
            }
        }

        @Override
        protected boolean onTick(final double done) {
            boolean result;
            try {
                result = super.onTick(done);
            } finally {
                tryOffloadedMove();
            }
            return result;
        }

        @Override
        protected void onFinish(final boolean finishedWell, final boolean isInterrupted) {
            try {
                super.onFinish(finishedWell, isInterrupted);
            } finally {
                tryOffloadedMove();
            }
        }
    }

    private class UpdateEffectIcons extends RunnableImpl {
        @Override
        public void runImpl() {
            updateEffectIconsImpl();
            _updateEffectIconsTask = null;
        }
    }

    public class BroadcastCharInfoTask extends RunnableImpl {
        @Override
        public void runImpl() {
            broadcastCharInfoImpl();
            _broadcastCharInfoTask = null;
        }
    }

    private class UserInfoTask extends RunnableImpl {
        @Override
        public void runImpl() {
            sendUserInfoImpl();
            _userInfoTask = null;
        }
    }

    private class ForceCleanupTask implements Runnable {
        @Override
        public void run() {
            final long nextDelay = 600000L - (System.currentTimeMillis() - _increasedForceLastUpdateTimeStamp);
            if (nextDelay > 1000L) {
                _increasedForceCleanupTask = ThreadPoolManager.getInstance().schedule(new ForceCleanupTask(), nextDelay);
                return;
            }
            _increasedForce = 0;
            sendEtcStatusUpdate();
            _increasedForceCleanupTask = null;
        }
    }

    private final ConcurrentHashMap<ListenerHookType, CopyOnWriteArraySet<ListenerHook>> listenersHookTypesMap = new ConcurrentHashMap<>();

    public void addListenerHook(ListenerHookType type, ListenerHook hook)
    {
        if(!listenersHookTypesMap.containsKey(type))
        {
            CopyOnWriteArraySet<ListenerHook> hooks = new CopyOnWriteArraySet<>();
            hooks.add(hook);
            listenersHookTypesMap.put(type, hooks);
        }
        else
        {
            CopyOnWriteArraySet<ListenerHook> hooks = listenersHookTypesMap.get(type);
            hooks.add(hook);
        }
    }

    public void removeListenerHookType(ListenerHookType type, ListenerHook hook)
    {
        if(listenersHookTypesMap.containsKey(type))
        {
            Set<ListenerHook> hooks = listenersHookTypesMap.get(type);
            hooks.remove(hook);
        }
    }

    public Set<ListenerHook> getListenerHooks(ListenerHookType type)
    {
        Set<ListenerHook> hooks = listenersHookTypesMap.get(type);
        if(hooks == null)
            hooks = Collections.emptySet();
        return hooks;
    }
}
