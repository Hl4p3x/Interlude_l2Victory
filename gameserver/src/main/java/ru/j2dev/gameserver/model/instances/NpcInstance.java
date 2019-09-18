package ru.j2dev.gameserver.model.instances;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.collections.MultiValueSet;
import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.ai.CharacterAI;
import ru.j2dev.gameserver.ai.CtrlEvent;
import ru.j2dev.gameserver.ai.CtrlIntention;
import ru.j2dev.gameserver.cache.HtmCache;
import ru.j2dev.gameserver.data.xml.holder.*;
import ru.j2dev.gameserver.geodata.GeoEngine;
import ru.j2dev.gameserver.handler.items.RefineryHandler;
import ru.j2dev.gameserver.idfactory.IdFactory;
import ru.j2dev.gameserver.listener.NpcListener;
import ru.j2dev.gameserver.manager.DimensionalRiftManager;
import ru.j2dev.gameserver.manager.QuestManager;
import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.model.Zone.ZoneType;
import ru.j2dev.gameserver.model.actor.listener.NpcListenerList;
import ru.j2dev.gameserver.model.actor.recorder.NpcStatsChangeRecorder;
import ru.j2dev.gameserver.model.base.AcquireType;
import ru.j2dev.gameserver.model.base.ClassId;
import ru.j2dev.gameserver.model.entity.DimensionalRift;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.model.entity.SevenSigns;
import ru.j2dev.gameserver.model.entity.events.GlobalEvent;
import ru.j2dev.gameserver.model.entity.residence.Castle;
import ru.j2dev.gameserver.model.entity.residence.ClanHall;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.npcmaker.DefaultMaker;
import ru.j2dev.gameserver.model.npcmaker.SpawnDefine;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestEventType;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.*;
import ru.j2dev.gameserver.scripts.Events;
import ru.j2dev.gameserver.stats.Stats;
import ru.j2dev.gameserver.tables.ClanTable;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.taskmanager.DecayTaskManager;
import ru.j2dev.gameserver.taskmanager.LazyPrecisionTaskManager;
import ru.j2dev.gameserver.taskmanager.tasks.objecttasks.NotifyAITask;
import ru.j2dev.gameserver.templates.StatsSet;
import ru.j2dev.gameserver.templates.item.ItemTemplate;
import ru.j2dev.gameserver.templates.item.WeaponTemplate;
import ru.j2dev.gameserver.templates.npc.Faction;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.templates.npc.polymorphed.PolymorphedData;
import ru.j2dev.gameserver.templates.npc.polymorphed.PolymorphedInventory;
import ru.j2dev.gameserver.templates.spawn.SpawnRange;
import ru.j2dev.gameserver.utils.*;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

public class NpcInstance extends Creature {
    public static final String NO_CHAT_WINDOW = "noChatWindow";
    public static final String NO_RANDOM_WALK = "noRandomWalk";
    public static final String IGNORE_DROP_DIFF = "ignoreDropLevelDiff";
    public static final String NO_RANDOM_ANIMATION = "noRandomAnimation";
    public static final String TARGETABLE = "TargetEnabled";
    public static final String SHOW_NAME = "showName";
    private static final Logger LOGGER = LoggerFactory.getLogger(NpcInstance.class);

    protected int _spawnAnimation;
    protected boolean _hasRandomAnimation;
    protected boolean _hasRandomWalk;
    protected boolean _hasChatWindow;
    protected boolean _ignoreDropDiffPenalty;
    protected boolean _unAggred;
    protected long _lastSocialAction;
    private int _personalAggroRange;
    private int _level;
    private long _dieTime;
    private int _currentLHandId;
    private int _currentRHandId;
    private double _currentCollisionRadius;
    private double _currentCollisionHeight;
    private int npcState;
    private Future<?> _decayTask;
    private Future<?> _animationTask;
    private AggroList _aggroList;
    private boolean _isTargetable;
    private boolean _showName;
    private Castle _nearestCastle;
    private ClanHall _nearestClanHall;
    private Spawner _spawn;
    private SpawnDefine spawnDefine;
    private Location _spawnedLoc;
    private SpawnRange _spawnRange;
    private MultiValueSet<String> _parameters;
    private int _displayId;
    private ScheduledFuture<?> _broadcastCharInfoTask;
    private boolean _isBusy;
    private String _busyMessage;
    private boolean _isUnderground;
    private PolymorphedData polymorphedData;
    //param
    private int _param1;
    private int _param2;
    private int _param3;
    private Creature _param4;

    public NpcInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
        _personalAggroRange = -1;
        _level = 0;
        _dieTime = 0L;
        _spawnAnimation = 2;
        npcState = 0;
        _spawnedLoc = new Location();
        _parameters = StatsSet.EMPTY;
        _unAggred = false;
        _displayId = 0;
        _busyMessage = "";
        _isUnderground = false;
        if (template == null) {
            throw new NullPointerException("No template for Npc. Please check your datapack is setup correctly.");
        }
        setParameters(template.getAIParams());
        _hasRandomAnimation = (!getParameter(NO_RANDOM_ANIMATION, false) && Config.MAX_NPC_ANIMATION > 0);
        _hasRandomWalk = !getParameter(NO_RANDOM_WALK, false);
        _ignoreDropDiffPenalty = getParameter(IGNORE_DROP_DIFF, false);
        setHasChatWindow(!getParameter(NO_CHAT_WINDOW, false));
        setTargetable(getParameter(TARGETABLE, true));
        setShowName(getParameter(SHOW_NAME, true));
        if (template.getSkills().size() > 0) {
            template.getSkills().valueCollection().forEach(this::addSkill);
        }
        setName(template.name);
        setTitle(template.title);
        setLHandId(getTemplate().lhand);
        setRHandId(getTemplate().rhand);
        setCollisionHeight(getTemplate().getCollisionHeight());
        setCollisionRadius(getTemplate().getCollisionRadius());
        _aggroList = new AggroList(this);
        setFlying(getParameter("isFlying", false));
        polymorphedData = PolymorphedHolder.getInstance().getPolymorphedData(getNpcId());
        if (getPolymorphedData() != null) {
            getPolymorphedData().setInventory(new PolymorphedInventory(this));
        }
        if (isPolymorphedNpc() && !getPolymorphedData().getItems().isEmpty()) {
            getPolymorphedData().getItems().stream().mapToInt(item_id -> item_id).mapToObj(ItemFunctions::createItem).forEach(item -> {
                item.setOwnerId(getObjectId());
                item.getEquipSlot();
                item.setLocation(ItemInstance.ItemLocation.INVENTORY);
                item.setEnchantLevel(getPolymorphedData().getWeaponEnchant());
                getPolymorphedData().getInventory().equipItem(item);
            });
        }
    }

    public static boolean canBypassCheck(final Player player, final NpcInstance npc) {
        if (npc == null || player.isActionsDisabled() || (!Config.ALLOW_TALK_WHILE_SITTING && player.isSitting()) || !npc.isInActingRange(player) || player.isInDuel()) {
            player.sendActionFailed();
            return false;
        }
        return true;
    }

    public static void showFishingSkillList(final Player player) {
        showAcquireList(AcquireType.FISHING, player);
    }

    public static void showClanSkillList(final Player player) {
        if (player.getClan() == null || !player.isClanLeader()) {
            player.sendPacket(SystemMsg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
            player.sendActionFailed();
            return;
        }
        showAcquireList(AcquireType.CLAN, player);
    }

    public static void showAcquireList(final AcquireType t, final Player player) {
        final Collection<SkillLearn> skills = SkillAcquireHolder.getInstance().getAvailableSkills(player, t);
        final AcquireSkillList asl = new AcquireSkillList(t, skills.size());
        skills.forEach(s -> asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), s.getCost(), 0));
        if (skills.size() == 0) {
            player.sendPacket(AcquireSkillDone.STATIC);
            player.sendPacket(SystemMsg.THERE_ARE_NO_OTHER_SKILLS_TO_LEARN);
        } else {
            player.unsetVar("AcquireSkillClassId");
            player.sendPacket(asl);
        }
        player.sendActionFailed();
    }

    public int getParam1() {
        return _param1;
    }

    public void setParam1(int _param1) {
        this._param1 = _param1;
    }

    public int getParam2() {
        return _param2;
    }

    public void setParam2(int _param2) {
        this._param2 = _param2;
    }

    public int getParam3() {
        return _param3;
    }

    public void setParam3(int _param3) {
        this._param3 = _param3;
    }

    public Creature getParam4() {
        return _param4;
    }

    public void setParam4(Creature _param4) {
        this._param4 = _param4;
    }

    public PolymorphedData getPolymorphedData() {
        return polymorphedData;
    }

    public void setPolymorphedData(PolymorphedData polymorphedData) {
        this.polymorphedData = polymorphedData;
    }

    @SuppressWarnings("unchecked")
    @Override
    public HardReference<NpcInstance> getRef() {
        return (HardReference<NpcInstance>) super.getRef();
    }

    @Override
    public CharacterAI getAI() {
        if (_ai == null) {
            synchronized (this) {
                if (_ai == null) {
                    _ai = getTemplate().getNewAI(this);
                }
            }
        }
        return _ai;
    }

    public Location getSpawnedLoc() {
        return _spawnedLoc;
    }

    public void setSpawnedLoc(final Location loc) {
        _spawnedLoc = loc;
    }

    public int getRightHandItem() {
        return _currentRHandId;
    }

    public int getLeftHandItem() {
        return _currentLHandId;
    }

    public void setLHandId(final int newWeaponId) {
        _currentLHandId = newWeaponId;
    }

    public void setRHandId(final int newWeaponId) {
        _currentRHandId = newWeaponId;
    }

    public double getCollisionHeight() {
        return _currentCollisionHeight;
    }

    public void setCollisionHeight(final double offset) {
        _currentCollisionHeight = offset;
    }

    public double getCollisionRadius() {
        return _currentCollisionRadius;
    }

    public void setCollisionRadius(final double collisionRadius) {
        _currentCollisionRadius = collisionRadius;
    }

    @Override
    protected void onReduceCurrentHp(final double damage, final Creature attacker, final Skill skill, final boolean awake, final boolean standUp, final boolean directHp) {
        if (attacker.isPlayable()) {
            getAggroList().addDamageHate(attacker, (int) damage, 0);
        }
        super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
    }

    @Override
    protected void onDeath(final Creature killer) {
        _dieTime = System.currentTimeMillis();
        if (isMonster() && (((MonsterInstance) this).isSeeded() || ((MonsterInstance) this).isSpoiled())) {
            startDecay(20000L);
        } else {
            startDecay(getTemplate().getCorpseTime());
        }
        setLHandId(getTemplate().lhand);
        setRHandId(getTemplate().rhand);
        setCollisionHeight(getTemplate().getCollisionHeight());
        setCollisionRadius(getTemplate().getCollisionRadius());
        getAI().stopAITask();
        stopRandomAnimation();
        super.onDeath(killer);
    }

    public long getDeadTime() {
        if (_dieTime <= 0L) {
            return 0L;
        }
        return System.currentTimeMillis() - _dieTime;
    }

    public AggroList getAggroList() {
        return _aggroList;
    }

    public MinionList getMinionList() {
        return null;
    }

    public Location getRndMinionPosition() {
        return Location.findPointToStay(this, (int) getColRadius() + 30, (int) getColRadius() + 50);
    }

    public boolean hasMinions() {
        return false;
    }

    public void dropItem(final Player lastAttacker, final int itemId, final long itemCount) {
        if (itemCount == 0L || lastAttacker == null) {
            return;
        }
        for (long i = 0L; i < itemCount; ++i) {
            final ItemInstance item = ItemFunctions.createItem(itemId);
            for (final GlobalEvent e : getEvents()) {
                item.addEvent(e);
            }
            if (item.isStackable()) {
                i = itemCount;
                item.setCount(itemCount);
            }
            if (isRaid() || this instanceof ReflectionBossInstance) {
                SystemMessage2 sm;
                if (itemId == 57) {
                    sm = new SystemMessage2(SystemMsg.C1_HAS_DIED_AND_DROPPED_S2_ADENA);
                    sm.addName(this);
                    sm.addLong(item.getCount());
                } else {
                    sm = new SystemMessage2(SystemMsg.C1_DIED_AND_DROPPED_S3_S2);
                    sm.addName(this);
                    sm.addItemName(itemId);
                    sm.addLong(item.getCount());
                }
                broadcastPacket(sm);
            }
            lastAttacker.doAutoLootOrDrop(item, this);
        }
    }

    public void mobBuffs (Player killer, final int skillId, final int skillLvl, long time, final int animId, final int animSek) {
        if (skillLvl == 0L || killer == null) {
            return;
        }
        long timeSec = time * 1000L;
        killer.getEffectList().stopEffect(skillId);
        SkillTable.getInstance().getInfo(skillId, skillLvl).getEffects(killer, killer, false, false, timeSec, 1.0, false);
        killer.broadcastPacket(new MagicSkillUse(killer, killer, animId, 1, animSek, 0));
    }

    public void dropItem(final Player lastAttacker, final ItemInstance item) {
        if (item.getCount() == 0L) {
            return;
        }
        if (isRaid() || this instanceof ReflectionBossInstance) {
            SystemMessage2 sm;
            if (item.getItemId() == 57) {
                sm = new SystemMessage2(SystemMsg.C1_HAS_DIED_AND_DROPPED_S2_ADENA);
                sm.addName(this);
                sm.addLong(item.getCount());
            } else {
                sm = new SystemMessage2(SystemMsg.C1_DIED_AND_DROPPED_S3_S2);
                sm.addName(this);
                sm.addItemName(item.getItemId());
                sm.addLong(item.getCount());
            }
            broadcastPacket(sm);
        }
        lastAttacker.doAutoLootOrDrop(item, this);
    }

    @Override
    public boolean isAttackable(final Creature attacker) {
        return true;
    }

    @Override
    public boolean isAutoAttackable(final Creature attacker) {
        return false;
    }

    @Override
    protected void onSpawn() {
        super.onSpawn();
        _dieTime = 0L;
        _spawnAnimation = 0;
        if (getAI().isGlobalAI() || (getCurrentRegion() != null && getCurrentRegion().isActive())) {
            getAI().startAITask();
            startRandomAnimation();
        }

        if (spawnDefine != null) {
            spawnDefine.getMaker().onNpcCreated(this);
        }

        ThreadPoolManager.getInstance().execute(new NotifyAITask(this, CtrlEvent.EVT_SPAWN));
        getListeners().onSpawn();
    }

    @Override
    protected void onDespawn() {
        getAggroList().clear();
        getAI().onEvtDeSpawn();
        getAI().stopAITask();
        getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
        stopRandomAnimation();
        super.onDespawn();
    }

    @Override
    public NpcTemplate getTemplate() {
        return (NpcTemplate) _template;
    }

    @Override
    public int getNpcId() {
        return getTemplate().npcId;
    }

    public void setUnAggred(final boolean state) {
        _unAggred = state;
    }

    public boolean isAggressive() {
        return getAggroRange() > 0;
    }

    public int getAggroRange() {
        if (_unAggred) {
            return 0;
        }
        if (_personalAggroRange >= 0) {
            return _personalAggroRange;
        }
        return getTemplate().aggroRange;
    }

    public void setAggroRange(final int aggroRange) {
        _personalAggroRange = aggroRange;
    }

    public Faction getFaction() {
        return getTemplate().getFaction();
    }

    public boolean isInFaction(final NpcInstance npc) {
        return getFaction().equals(npc.getFaction()) && !getFaction().isIgnoreNpcId(npc.getNpcId());
    }

    @Override
    public int getMAtk(final Creature target, final Skill skill) {
        return (int) (super.getMAtk(target, skill) * Config.ALT_NPC_MATK_MODIFIER);
    }

    @Override
    public int getPAtk(final Creature target) {
        return (int) (super.getPAtk(target) * Config.ALT_NPC_PATK_MODIFIER);
    }

    @Override
    public int getMaxHp() {
        return (int) (super.getMaxHp() * Config.ALT_NPC_MAXHP_MODIFIER);
    }

    @Override
    public int getMaxMp() {
        return (int) (super.getMaxMp() * Config.ALT_NPC_MAXMP_MODIFIER);
    }

    public long getExpReward() {
        return (long) calcStat(Stats.EXP, getTemplate().rewardExp, null, null);
    }

    public long getSpReward() {
        return (long) calcStat(Stats.SP, getTemplate().rewardSp, null, null);
    }

    @Override
    protected void onDelete() {
        stopDecay();
        if (_spawn != null) {
            _spawn.stopRespawn();
        }
        setSpawn(null);
        super.onDelete();
    }

    public Spawner getSpawn() {
        return _spawn;
    }

    public void setSpawn(final Spawner spawn) {
        _spawn = spawn;
    }

    public SpawnDefine getSpawnDefine() {
        return spawnDefine;
    }

    public void setSpawnDefine(SpawnDefine sd) {
        spawnDefine = sd;
    }

    public DefaultMaker getMyMaker() {
        if (spawnDefine != null) {
            return spawnDefine.getMaker();
        }
        return null;
    }

    @Override
    protected void onDecay() {
        super.onDecay();
        _spawnAnimation = 2;
        if (_spawn != null) {
            _spawn.decreaseCount(this);
        } else if (!isMinion()) {
            deleteMe();
        }
    }

    public final void decayOrDelete() {
        onDecay();
    }

    protected void startDecay(final long delay) {
        stopDecay();
        _decayTask = DecayTaskManager.getInstance().addDecayTask(this, delay);
    }

    public void stopDecay() {
        if (_decayTask != null) {
            _decayTask.cancel(false);
            _decayTask = null;
        }
    }

    public void endDecayTask() {
        if (_decayTask != null) {
            _decayTask.cancel(false);
            _decayTask = null;
        }
        doDecay();
    }

    @Override
    public boolean isUndead() {
        return getTemplate().isUndead();
    }

    @Override
    public int getLevel() {
        return (_level == 0) ? getTemplate().level : _level;
    }

    public void setLevel(final int level) {
        _level = level;
    }

    public int getDisplayId() {
        return (_displayId > 0) ? _displayId : getTemplate().displayId;
    }

    public void setDisplayId(final int displayId) {
        _displayId = displayId;
    }

    @Override
    public ItemInstance getActiveWeaponInstance() {
        return null;
    }

    @Override
    public int getPhysicalAttackRange() {
        return (int) calcStat(Stats.POWER_ATTACK_RANGE, getTemplate().getBaseAtkRange(), null, null);
    }

    @Override
    public WeaponTemplate getActiveWeaponItem() {
        final int weaponId = getTemplate().rhand;
        if (weaponId < 1) {
            return null;
        }
        final ItemTemplate item = ItemTemplateHolder.getInstance().getTemplate(getTemplate().rhand);
        if (!(item instanceof WeaponTemplate)) {
            return null;
        }
        return (WeaponTemplate) item;
    }

    @Override
    public ItemInstance getSecondaryWeaponInstance() {
        return null;
    }

    @Override
    public WeaponTemplate getSecondaryWeaponItem() {
        final int weaponId = getTemplate().lhand;
        if (weaponId < 1) {
            return null;
        }
        final ItemTemplate item = ItemTemplateHolder.getInstance().getTemplate(getTemplate().lhand);
        if (!(item instanceof WeaponTemplate)) {
            return null;
        }
        return (WeaponTemplate) item;
    }

    @Override
    public void sendChanges() {
        if (isFlying()) {
            return;
        }
        super.sendChanges();
    }

    @Override
    public void broadcastCharInfo() {
        if (!isVisible()) {
            return;
        }
        if (_broadcastCharInfoTask != null) {
            return;
        }
        _broadcastCharInfoTask = ThreadPoolManager.getInstance().schedule(new BroadcastCharInfoTask(), Config.BROADCAST_CHAR_INFO_INTERVAL);
    }

    private void broadcastCharInfoImpl() {
        World.getAroundPlayers(this).forEach(player -> {
            if (isPolymorphedNpc()) {
                player.sendPacket(new PolyMorphedNpcInfo(this));
            } else {
                player.sendPacket(new NpcInfo(this, player).update());
            }
        });
    }

    public void onRandomAnimation() {
        if (System.currentTimeMillis() - _lastSocialAction > 10000L) {
            broadcastPacket(new SocialAction(getObjectId(), 2));
            _lastSocialAction = System.currentTimeMillis();
        }
    }

    public void startRandomAnimation() {
        if (!hasRandomAnimation()) {
            return;
        }
        _animationTask = LazyPrecisionTaskManager.getInstance().addNpcAnimationTask(this);
    }

    public void stopRandomAnimation() {
        if (_animationTask != null) {
            _animationTask.cancel(false);
            _animationTask = null;
        }
    }

    public boolean hasRandomAnimation() {
        return _hasRandomAnimation;
    }

    public boolean hasRandomWalk() {
        return _hasRandomWalk;
    }

    public Castle getCastle() {
        if (getReflection() == ReflectionManager.GIRAN_HARBOR && Config.SERVICES_GIRAN_HARBOR_NOTAX) {
            return null;
        }
        if (Config.SERVICES_OFFSHORE_NO_CASTLE_TAX && getReflection() == ReflectionManager.GIRAN_HARBOR) {
            return null;
        }
        if (Config.SERVICES_OFFSHORE_NO_CASTLE_TAX && isInZone(ZoneType.offshore)) {
            return null;
        }
        if (_nearestCastle == null) {
            _nearestCastle = ResidenceHolder.getInstance().getResidence(getTemplate().getCastleId());
        }
        return _nearestCastle;
    }

    public Castle getCastle(final Player player) {
        return getCastle();
    }

    public ClanHall getClanHall() {
        if (_nearestClanHall == null) {
            _nearestClanHall = ResidenceHolder.getInstance().findNearestResidence(ClanHall.class, getX(), getY(), getZ(), getReflection(), 32768);
        }
        return _nearestClanHall;
    }

    @Override
    public void onAction(final Player player, final boolean shift) {
        if (!isTargetable()) {
            player.sendActionFailed();
            return;
        }
        if (player.getTarget() != this) {
            player.setTarget(this);
            if (player.getTarget() == this) {
                player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()), makeStatusUpdate(9, 10));
            }
            player.sendPacket(new ValidateLocation(this), ActionFail.STATIC);
            return;
        }
        if (Events.onAction(player, this, shift)) {
            player.sendActionFailed();
            return;
        }
        if (isAutoAttackable(player)) {
            player.getAI().Attack(this, false, shift);
            return;
        }
        if (!isInActingRange(player)) {
            if (!player.getAI().isIntendingInteract(this)) {
                player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
            }
            return;
        }
        if (player.getKarma() > 0 && !canInteractWithKarmaPlayer() && !player.isGM()) {
            player.sendActionFailed();
            return;
        }
        if (player.isFlying()) {
            player.sendActionFailed();
            return;
        }
        if ((!Config.ALLOW_TALK_WHILE_SITTING && player.isSitting()) || player.isAlikeDead()) {
            return;
        }
        if (hasRandomAnimation()) {
            onRandomAnimation();
        }
        player.sendActionFailed();
        if (player.isMoving()) {
            player.stopMove();
        }

        if (!isMoving() && !isRunning()) {
            player.sendPacket(new MoveToPawn(player, this, getInteractDistance(player)));
        }
        getListeners().onShowChat();
        getListeners().onShowChatEvent(player);
        player.setLastNpcInteractionTime();
        if (_isBusy) {
            showBusyWindow(player);
        } else if (isHasChatWindow()) {
            boolean flag = false;
            final Quest[] qlst = getTemplate().getEventQuests(QuestEventType.NPC_FIRST_TALK);
            if (qlst != null && qlst.length > 0) {
                for (final Quest element : qlst) {
                    final QuestState qs = player.getQuestState(element.getName());
                    if ((qs == null || !qs.isCompleted()) && element.notifyFirstTalk(this, player)) {
                        flag = true;
                    }
                }
            }
            if (!flag) {
                showChatWindow(player, 0);
            }
        }
    }

    protected boolean canInteractWithKarmaPlayer() {
        return Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP || this instanceof WarehouseInstance;
    }

    public void showQuestWindow(final Player player, final String questId) {
        if (!player.isQuestContinuationPossible(true)) {
            return;
        }
        int count = (int) Arrays.stream(player.getAllQuestsStates()).filter(quest -> quest != null && quest.getQuest().isVisible() && quest.isStarted() && quest.getCond() > 0).count();
        if (count > 40) {
            showChatWindow(player, "quest-limit.htm");
            return;
        }
        try {
            QuestState qs = player.getQuestState(questId);
            if (qs != null) {
                if (qs.isCompleted()) {
                    showChatWindow(player, "completed-quest.htm");
                    return;
                }
                if (qs.getQuest().notifyTalk(this, qs)) {
                    return;
                }
            } else {
                final Quest q = QuestManager.getQuest(questId);
                if (q != null) {
                    final Quest[] qlst = getTemplate().getEventQuests(QuestEventType.QUEST_START);
                    if (qlst != null && qlst.length > 0) {
                        final int length2 = qlst.length;
                        int j = 0;
                        while (j < length2) {
                            final Quest element = qlst[j];
                            if (element == q) {
                                qs = q.newQuestState(player, 1);
                                if (qs.getQuest().notifyTalk(this, qs)) {
                                    return;
                                }
                                break;
                            } else {
                                ++j;
                            }
                        }
                    }
                }
            }
            showChatWindow(player, "no-quest.htm");
        } catch (Exception e) {
            LOGGER.warn("problem with npc text(questId: " + questId + ") " + e);
            LOGGER.error("", e);
        }
        player.sendActionFailed();
    }

    public void onBypassFeedback(final Player player, final String command) {
        if (!canBypassCheck(player, this)) {
            return;
        }
        try {
            if ("TerritoryStatus".equalsIgnoreCase(command)) {
                final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
                html.setFile("merchant/territorystatus.htm");
                html.replace("%npcname%", getName());
                final Castle castle = getCastle(player);
                if (castle != null && castle.getId() > 0) {
                    html.replace("%castlename%", HtmlUtils.htmlResidenceName(castle.getId()));
                    html.replace("%taxpercent%", String.valueOf(castle.getTaxPercent()));
                    if (castle.getOwnerId() > 0) {
                        final Clan clan = ClanTable.getInstance().getClan(castle.getOwnerId());
                        if (clan != null) {
                            html.replace("%clanname%", clan.getName());
                            html.replace("%clanleadername%", clan.getLeaderName());
                        } else {
                            html.replace("%clanname%", "unexistant clan");
                            html.replace("%clanleadername%", "None");
                        }
                    } else {
                        html.replace("%clanname%", "NPC");
                        html.replace("%clanleadername%", "None");
                    }
                } else {
                    html.replace("%castlename%", "Open");
                    html.replace("%taxpercent%", "0");
                    html.replace("%clanname%", "No");
                    html.replace("%clanleadername%", getName());
                }
                player.sendPacket(html);
            } else if (command.startsWith("Quest")) {
                final String quest = command.substring(5).trim();
                if (quest.length() == 0) {
                    showQuestWindow(player);
                } else {
                    showQuestWindow(player, quest);
                }
            } else if (command.startsWith("Chat")) {
                try {
                    final int val = Integer.parseInt(command.substring(5));
                    showChatWindow(player, val);
                } catch (NumberFormatException nfe) {
                    final String filename = command.substring(5).trim();
                    if (filename.length() == 0) {
                        showChatWindow(player, "npcdefault.htm");
                    } else {
                        showChatWindow(player, filename);
                    }
                }
            } else if (command.startsWith("AttributeCancel")) {
                player.sendPacket(new ExShowBaseAttributeCancelWindow(player));
            } else if (command.startsWith("NpcLocationInfo")) {
                final int val = Integer.parseInt(command.substring(16));
                final NpcInstance npc = GameObjectsStorage.getByNpcId(val);
                if (npc != null) {
                    player.sendPacket(new RadarControl(2, 2, npc.getLoc()));
                    player.sendPacket(new RadarControl(0, 1, npc.getLoc()));
                }
            } else if (command.startsWith("Multisell") || command.startsWith("multisell")) {
                final String listId = command.substring(9).trim();
                final Castle castle = getCastle(player);
                MultiSellHolder.getInstance().SeparateAndSend(Integer.parseInt(listId), player, (castle != null) ? castle.getTaxRate() : 0.0);
            } else if (command.startsWith("EnterRift")) {
                final StringTokenizer st = new StringTokenizer(command);
                st.nextToken();
                final Integer b1 = Integer.parseInt(st.nextToken());
                DimensionalRiftManager.getInstance().start(player, b1, this);
            } else if (command.startsWith("ChangeRiftRoom")) {
                if (player.isInParty() && player.getParty().isInReflection() && player.getParty().getReflection() instanceof DimensionalRift) {
                    ((DimensionalRift) player.getParty().getReflection()).manualTeleport(player, this);
                } else {
                    DimensionalRiftManager.getInstance().teleportToWaitingRoom(player);
                }
            } else if (command.startsWith("ExitRift")) {
                if (player.isInParty() && player.getParty().isInReflection() && player.getParty().getReflection() instanceof DimensionalRift) {
                    ((DimensionalRift) player.getParty().getReflection()).manualExitRift(player, this);
                } else {
                    DimensionalRiftManager.getInstance().teleportToWaitingRoom(player);
                }
            } else if ("SkillList".equalsIgnoreCase(command)) {
                showSkillList(player);
            } else if (command.startsWith("AltSkillList")) {
                final int altClsId = Integer.parseInt(command.substring(13).trim());
                showSkillList(player, ClassId.VALUES[altClsId]);
            } else if ("SkillEnchantList".equalsIgnoreCase(command)) {
                showSkillEnchantList(player);
            } else if ("ClanSkillList".equalsIgnoreCase(command)) {
                showClanSkillList(player);
            } else if (command.startsWith("Augment")) {
                final int cmdChoice = Integer.parseInt(command.substring(8, 9).trim());
                if (cmdChoice == 1) {
                    player.setRefineryHandler(RefineryHandler.getInstance());
                    RefineryHandler.getInstance().onInitRefinery(player);
                } else if (cmdChoice == 2) {
                    player.setRefineryHandler(RefineryHandler.getInstance());
                    RefineryHandler.getInstance().onInitRefineryCancel(player);
                }
            } else if (command.startsWith("Link")) {
                showChatWindow(player, command.substring(5));
            } else if (command.startsWith("Teleport")) {
                final int cmdChoice = Integer.parseInt(command.substring(9).trim());
                final TeleportLocation[] list = getTemplate().getTeleportList(cmdChoice);
                if (list != null) {
                    showTeleportList(player, list);
                } else {
                    player.sendMessage("\u0421\u0441\u044b\u043b\u043a\u0430 \u043d\u0435\u0438\u0441\u043f\u0440\u0430\u0432\u043d\u0430, \u0441\u043e\u043e\u0431\u0449\u0438\u0442\u0435 \u0430\u0434\u043c\u0438\u043d\u0438\u0441\u0442\u0440\u0430\u0442\u043e\u0440\u0443.");
                }
            } else if (command.startsWith("Tele20Lvl")) {
                final int cmdChoice = Integer.parseInt(command.substring(10, 11).trim());
                final TeleportLocation[] list = getTemplate().getTeleportList(cmdChoice);
                if (player.getLevel() > 20) {
                    showChatWindow(player, "teleporter/" + getNpcId() + "-no.htm");
                } else if (list != null) {
                    showTeleportList(player, list);
                } else {
                    player.sendMessage("\u0421\u0441\u044b\u043b\u043a\u0430 \u043d\u0435\u0438\u0441\u043f\u0440\u0430\u0432\u043d\u0430, \u0441\u043e\u043e\u0431\u0449\u0438\u0442\u0435 \u0430\u0434\u043c\u0438\u043d\u0438\u0441\u0442\u0440\u0430\u0442\u043e\u0440\u0443.");
                }
            } else if (command.startsWith("open_gate")) {
                final int val = Integer.parseInt(command.substring(10));
                ReflectionUtils.getDoor(val).openMe();
                player.sendActionFailed();
            } else if (command.startsWith("ExitFromQuestInstance")) {
                final Reflection r = player.getReflection();
                r.startCollapseTimer(60000L);
                player.teleToLocation(r.getReturnLoc(), 0);
                if (command.length() > 22) {
                    try {
                        final int val2 = Integer.parseInt(command.substring(22));
                        showChatWindow(player, val2);
                    } catch (NumberFormatException nfe2) {
                        final String filename2 = command.substring(22).trim();
                        if (filename2.length() > 0) {
                            showChatWindow(player, filename2);
                        }
                    }
                }
            }
        } catch (StringIndexOutOfBoundsException sioobe) {
            LOGGER.info("Incorrect htm bypass! npcId=" + getTemplate().npcId + " command=[" + command + "]");
        } catch (NumberFormatException nfe) {
            LOGGER.info("Invalid bypass to Server command parameter! npcId=" + getTemplate().npcId + " command=[" + command + "]");
        }
    }

    public void showTeleportList(final Player player, final TeleportLocation[] list) {
        final StringBuilder sb = new StringBuilder();
        sb.append("&$556;").append("<br><br>");
        if (list != null && player.getPlayerAccess().UseTeleport) {
            for (final TeleportLocation tl : list) {
                if (tl.getItem().getItemId() == 57) {
                    double pricemod = (player.getLevel() <= Config.GATEKEEPER_FREE) ? 0.0 : Config.GATEKEEPER_MODIFIER;
                    if (tl.getPrice() > 0L && pricemod > 0.0) {
                        final Calendar calendar = Calendar.getInstance();
                        final int day = calendar.get(Calendar.DAY_OF_WEEK);
                        final int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                        if ((day == 1 || day == 7) && hour >= 20 && hour <= 12) {
                            pricemod /= 2.0;
                        }
                    }
                    sb.append("[scripts_Util:Gatekeeper ").append(tl.getX()).append(" ").append(tl.getY()).append(" ").append(tl.getZ());
                    if (tl.getCastleId() != 0) {
                        sb.append(" ").append(tl.getCastleId());
                    }
                    final String name = new CustomMessage(tl.getName(), player, new Object[0]).toString();
                    sb.append(" ").append((long) (tl.getPrice() * pricemod)).append(" @811;F;").append(name).append("|").append(name);
                    if (tl.getPrice() * pricemod > 0.0) {
                        sb.append(" - ").append((long) (tl.getPrice() * pricemod)).append(" ").append(HtmlUtils.htmlItemName(57));
                    }
                    if (tl.getMinLevel() > 0) {
                        sb.append(" - ").append(new CustomMessage("ru.j2dev.gameserver.model.instances.NpcInstance.TeleportListMinLevel", player, tl.getMinLevel()));
                    }
                    if (tl.getMaxLevel() > 0) {
                        sb.append(" - ").append(new CustomMessage("ru.j2dev.gameserver.model.instances.NpcInstance.TeleportListMaxLevel", player, tl.getMaxLevel()));
                    }
                    sb.append("]<br1>\n");
                } else {
                    final String name2 = new CustomMessage(tl.getName(), player, new Object[0]).toString();
                    sb.append("[scripts_Util:QuestGatekeeper ").append(tl.getX()).append(" ").append(tl.getY()).append(" ").append(tl.getZ()).append(" ").append(tl.getPrice()).append(" ").append(tl.getItem().getItemId()).append(" @811;F;").append(name2).append("|").append(name2).append(" - ").append(tl.getPrice()).append(" ").append(HtmlUtils.htmlItemName(tl.getItem().getItemId()));
                    if (tl.getMinLevel() > 0) {
                        sb.append(" - ").append(new CustomMessage("ru.j2dev.gameserver.model.instances.NpcInstance.TeleportListMinLevel", player, tl.getMinLevel()));
                    }
                    if (tl.getMaxLevel() > 0) {
                        sb.append(" - ").append(new CustomMessage("ru.j2dev.gameserver.model.instances.NpcInstance.TeleportListMaxLevel", player, tl.getMaxLevel()));
                    }
                    sb.append("]<br1>\n");
                }
            }
        } else {
            sb.append("No teleports available for you.");
        }
        final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
        html.setHtml(HtmlUtils.bbParse(sb.toString()));
        player.sendPacket(html);
    }

    public void showQuestWindow(final Player player) {
        final List<Quest> options = new ArrayList<>();
        final List<QuestState> awaits = player.getQuestsForEvent(this, QuestEventType.QUEST_TALK);
        final Quest[] starts = getTemplate().getEventQuests(QuestEventType.QUEST_START);
        if (awaits != null) {
            awaits.stream().filter(x -> !options.contains(x.getQuest()) && x.getQuest().getQuestIntId() > 0).forEach(x -> options.add(x.getQuest()));
        }
        if (starts != null) {
            Arrays.stream(starts).filter(x2 -> !options.contains(x2) && x2.getQuestIntId() > 0).forEach(options::add);
        }
        if (options.size() > 1) {
            showQuestChooseWindow(player, options.toArray(new Quest[0]));
        } else if (options.size() == 1) {
            showQuestWindow(player, options.get(0).getName());
        } else {
            showQuestWindow(player, "");
        }
    }

    public void showQuestChooseWindow(final Player player, final Quest[] quests) {
        final StringBuilder sb = new StringBuilder();
        sb.append("<html><body><title>Talk about:</title><br>");
        for (final Quest q : quests) {
            if (q.isVisible()) {
                sb.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Quest ").append(q.getName()).append("\">[").append(q.getDescr(player)).append("]</a><br>");
            }
        }
        sb.append("</body></html>");
        final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
        html.setHtml(sb.toString());
        player.sendPacket(html);
    }

    public void showChatWindow(final Player player, final int val, final Object... replace) {
        String filename = "seven_signs/";
        final int npcId = getNpcId();
        switch (npcId) {
            case 31111: {
                final int sealAvariceOwner = SevenSigns.getInstance().getSealOwner(1);
                final int playerCabal = SevenSigns.getInstance().getPlayerCabal(player);
                final int compWinner = SevenSigns.getInstance().getCabalHighestScore();
                if (playerCabal == sealAvariceOwner && playerCabal == compWinner) {
                    switch (sealAvariceOwner) {
                        case 2: {
                            filename += "spirit_dawn.htm";
                            break;
                        }
                        case 1: {
                            filename += "spirit_dusk.htm";
                            break;
                        }
                        case 0: {
                            filename += "spirit_null.htm";
                            break;
                        }
                    }
                    break;
                }
                filename += "spirit_null.htm";
                break;
            }
            case 31112: {
                filename += "spirit_exit.htm";
                break;
            }
            case 30298: {
                if (player.getPledgeType() == -1) {
                    filename = getHtmlPath(npcId, 1, player);
                    break;
                }
                filename = getHtmlPath(npcId, 0, player);
                break;
            }
            default: {
                if ((npcId >= 31093 && npcId <= 31094) || (npcId >= 31172 && npcId <= 31201) || (npcId >= 31239 && npcId <= 31254)) {
                    return;
                }
                filename = getHtmlPath(npcId, val, player);
                break;
            }
        }
        final NpcHtmlMessage packet = new NpcHtmlMessage(player, this, filename, val);
        if (replace.length % 2 == 0) {
            for (int i = 0; i < replace.length; i += 2) {
                packet.replace(String.valueOf(replace[i]), String.valueOf(replace[i + 1]));
            }
        }
        player.sendPacket(packet);
    }

    public void showChatWindow(final Player player, final String filename, final Object... replace) {
        final NpcHtmlMessage packet = new NpcHtmlMessage(player, this, filename, 0);
        if (replace.length % 2 == 0) {
            for (int i = 0; i < replace.length; i += 2) {
                packet.replace(String.valueOf(replace[i]), String.valueOf(replace[i + 1]));
            }
        }
        player.sendPacket(packet);
    }

    public String getHtmlPath(final int npcId, final int val, final Player player) {
        String pom;
        if (val == 0) {
            pom = "" + npcId;
        } else {
            pom = npcId + "-" + val;
        }
        if (getTemplate().getHtmRoot() != null) {
            return getTemplate().getHtmRoot() + pom + ".htm";
        }
        String temp = "default/" + pom + ".htm";
        if (HtmCache.getInstance().getNullable(temp, player) != null) {
            return temp;
        }
        temp = "trainer/" + pom + ".htm";
        if (HtmCache.getInstance().getNullable(temp, player) != null) {
            return temp;
        }
        return "npcdefault.htm";
    }

    public final boolean isBusy() {
        return _isBusy;
    }

    public void setBusy(final boolean isBusy) {
        _isBusy = isBusy;
    }

    public final String getBusyMessage() {
        return _busyMessage;
    }

    public void setBusyMessage(final String message) {
        _busyMessage = message;
    }

    public void showBusyWindow(final Player player) {
        final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
        html.setFile("npcbusy.htm");
        html.replace("%npcname%", getName());
        html.replace("%playername%", player.getName());
        html.replace("%busymessage%", _busyMessage);
        player.sendPacket(html);
    }

    public void showSkillEnchantList(final Player player) {
        final ClassId classId = player.getClassId();
        if (player.getClassId().getLevel() < 4) {
            final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
            final StringBuilder sb = new StringBuilder();
            sb.append("<html><head><body>");
            if (player.isLangRus()) {
                sb.append("\u041c\u0430\u0441\u0442\u0435\u0440:<br>");
                sb.append("\u0412\u044b \u0434\u043e\u043b\u0436\u043d\u044b \u0432\u044b\u043f\u043e\u043b\u043d\u0438\u0442\u044c \u043a\u0432\u0435\u0441\u0442 \u043d\u0430 \u043f\u043e\u043b\u0443\u0447\u0435\u043d\u0438\u0435 \u0442\u0440\u0435\u0442\u044c\u0435\u0439 \u043f\u0440\u043e\u0444\u0435\u0441\u0441\u0438\u0438.");
            } else {
                sb.append("Trainer:<br>");
                sb.append("You must have 3rd class change quest completed.");
            }
            sb.append("</body></html>");
            html.setHtml(sb.toString());
            player.sendPacket(html);
            return;
        }
        if (player.getLevel() < 76) {
            player.sendPacket(new SystemMessage(1438));
            return;
        }
        if (!getTemplate().canTeach(classId) && !getTemplate().canTeach(classId.getParent(player.getSex())) && !Config.ALT_ALLOW_ALLCLASS_SKILLENCHANT) {
            if (this instanceof TrainerInstance) {
                showChatWindow(player, "trainer/" + getNpcId() + "-noteach.htm");
            } else {
                final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
                String sb = "<html><head><body>" +
                        new CustomMessage("l2p.gameserver.model.instances.L2NpcInstance.WrongTeacherClass", player) +
                        "</body></html>";
                html.setHtml(sb);
                player.sendPacket(html);
            }
            return;
        }
        player.sendPacket(ExEnchantSkillList.packetFor(player));
    }

    public void showSkillList(final Player player) {
        showSkillList(player, player.getClassId());
    }

    public void showSkillList(final Player player, final ClassId classId) {
        if (classId == null) {
            return;
        }
        final int npcId = getTemplate().npcId;
        if (getTemplate().getTeachInfo().isEmpty()) {
            final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
            final StringBuilder sb = new StringBuilder();
            sb.append("<html><head><body>");
            if (!player.isLangRus()) {
                sb.append("I cannot teach you. My class list is empty.<br> Ask admin to fix it. <br>NpcId:").append(npcId).append(", Your classId:").append(classId.name()).append("<br>");
            } else {
                sb.append("Я не могу обучить тебя. Для твоего класса мой список пуст.<br> Свяжись с админом для фикса этого. <br>NpcId:").append(npcId).append(", твой classId:").append(classId.name()).append("<br>");
            }
            sb.append("</body></html>");
            html.setHtml(sb.toString());
            player.sendPacket(html);
            return;
        }
        if (!getTemplate().canTeach(classId) && !getTemplate().canTeach(classId.getParent(player.getSex())) && !Config.ALT_ALLOW_ALLCLASS_SKILLENCHANT) {
            if (this instanceof WarehouseInstance) {
                showChatWindow(player, "warehouse/" + getNpcId() + "-noteach.htm");
            } else if (this instanceof TrainerInstance) {
                showChatWindow(player, "trainer/" + getNpcId() + "-noteach.htm");
            } else {
                final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
                String sb = "<html><head><body>" +
                        new CustomMessage("l2p.gameserver.model.instances.L2NpcInstance.WrongTeacherClass", player) +
                        "</body></html>";
                html.setHtml(sb);
                player.sendPacket(html);
            }
            return;
        }
        final Collection<SkillLearn> skills = SkillAcquireHolder.getInstance().getAvailableSkills(player, classId, AcquireType.NORMAL, null);
        final AcquireSkillList asl = new AcquireSkillList(AcquireType.NORMAL, skills.size());
        int counts = 0;
        for (final SkillLearn s : skills) {
            if (s.isClicked()) {
                continue;
            }
            final Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
            if (sk == null) {
                continue;
            }
            if (!Config.ALT_WEAK_SKILL_LEARN) {
                if (!sk.getCanLearn(player.getClassId())) {
                    continue;
                }
                if (!sk.canTeachBy(npcId)) {
                    continue;
                }
            }
            counts++;
            asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), s.getCost(), 0);
        }
        if (counts == 0) {
            final int minlevel = SkillAcquireHolder.getInstance().getMinLevelForNewSkill(classId, player.getLevel(), AcquireType.NORMAL);
            if (minlevel > 0) {
                final SystemMessage2 sm = new SystemMessage2(SystemMsg.YOU_DO_NOT_HAVE_ANY_FURTHER_SKILLS_TO_LEARN__COME_BACK_WHEN_YOU_HAVE_REACHED_LEVEL_S1);
                sm.addInteger(minlevel);
                player.sendPacket(sm);
            } else {
                player.sendPacket(SystemMsg.THERE_ARE_NO_OTHER_SKILLS_TO_LEARN);
            }
            player.sendPacket(AcquireSkillDone.STATIC);
        } else {
            player.setVar("AcquireSkillClassId", classId.getId(), -1L);
            player.sendPacket(asl);
        }
        player.sendActionFailed();
    }

    public int getSpawnAnimation() {
        return _spawnAnimation;
    }

    @Override
    public boolean getChargedSoulShot() {
        switch (getTemplate().shots) {
            case SOUL:
            case SOUL_SPIRIT:
            case SOUL_BSPIRIT:
                return true;
            default:
                return false;
        }
    }

    @Override
    public int getChargedSpiritShot() {
        switch (getTemplate().shots) {
            case SPIRIT:
            case SOUL_SPIRIT:
                return 1;
            case BSPIRIT:
            case SOUL_BSPIRIT:
                return 2;
            default:
                return 0;
        }
    }

    @Override
    public boolean unChargeShots(final boolean spirit) {
        broadcastPacket(new MagicSkillUse(this, spirit ? 2061 : 2039, 1, 0, 0));
        return true;
    }

    @Override
    public double getColRadius() {
        return getCollisionRadius();
    }

    @Override
    public double getColHeight() {
        return getCollisionHeight();
    }

    public int calculateLevelDiffForDrop(final int charLevel) {
        if (!Config.DEEPBLUE_DROP_RULES || _ignoreDropDiffPenalty) {
            return 0;
        }
        final int mobLevel = getLevel();
        final int deepblue_maxdiff = (this instanceof RaidBossInstance) ? Config.DEEPBLUE_DROP_RAID_MAXDIFF : Config.DEEPBLUE_DROP_MAXDIFF;
        return Math.max(charLevel - mobLevel - deepblue_maxdiff, 0);
    }

    public boolean isSevenSignsMonster() {
        return "c_dungeon_clan".equalsIgnoreCase(getFaction().getName());
    }

    @Override
    public String toString() {
        return getNpcId() + " " + getName();
    }

    public void refreshID() {
        GameObjectsStorage.remove(this);
        objectId = IdFactory.getInstance().getNextId();
        GameObjectsStorage.put(this);
    }

    public boolean isUnderground() {
        return _isUnderground;
    }

    public void setUnderground(final boolean b) {
        _isUnderground = b;
    }

    public boolean isTargetable() {
        return _isTargetable;
    }

    public void setTargetable(final boolean value) {
        _isTargetable = value;
    }

    public boolean isShowName() {
        return _showName;
    }

    public void setShowName(final boolean value) {
        _showName = value;
    }

    @Override
    public NpcListenerList getListeners() {
        if (listeners == null) {
            synchronized (this) {
                if (listeners == null) {
                    listeners = new NpcListenerList(this);
                }
            }
        }
        return (NpcListenerList) listeners;
    }

    public <T extends NpcListener> boolean addListener(final T listener) {
        return getListeners().add(listener);
    }

    public <T extends NpcListener> boolean removeListener(final T listener) {
        return getListeners().remove(listener);
    }

    @Override
    public NpcStatsChangeRecorder getStatsRecorder() {
        if (_statsRecorder == null) {
            synchronized (this) {
                if (_statsRecorder == null) {
                    _statsRecorder = new NpcStatsChangeRecorder(this);
                }
            }
        }
        return (NpcStatsChangeRecorder) _statsRecorder;
    }

    public int getNpcState() {
        return npcState;
    }

    public void setNpcState(final int stateId) {
        broadcastPacket(new ExChangeNpcState(getObjectId(), stateId));
        npcState = stateId;
    }

    @Override
    public List<L2GameServerPacket> addPacketList(final Player forPlayer, final Creature dropper) {
        final List<L2GameServerPacket> list = new ArrayList<>(3);
        if (isPolymorphedNpc()) {
            list.add(new PolyMorphedNpcInfo(this));
        } else if (!isPolymorphedNpc()) {
            list.add(new NpcInfo(this, forPlayer));
        }
        if (isInCombat()) {
            list.add(new AutoAttackStart(getObjectId()));
        }
        if (isMoving() || isFollowing()) {
            list.add(movePacket());
        }
        return list;
    }

    @Override
    public Clan getClan() {
        final Castle castle = getCastle();
        return (castle != null) ? castle.getOwner() : null;
    }

    @Override
    public boolean isNpc() {
        return true;
    }

    @Override
    public int getGeoZ(final Location loc) {
        if (isFlying() || isInWater() || isInBoat() || isBoat() || isDoor()) {
            return loc.z;
        }
        if (!isNpc()) {
            return super.getGeoZ(loc);
        }
        if (_spawnRange instanceof Territory) {
            return GeoEngine.getHeight(loc, getGeoIndex());
        }
        return loc.z;
    }

    public boolean isMerchantNpc() {
        return false;
    }

    public SpawnRange getSpawnRange() {
        return _spawnRange;
    }

    public void setSpawnRange(final SpawnRange spawnRange) {
        _spawnRange = spawnRange;
    }

    public void setParameter(final String str, final Object val) {
        if (_parameters == StatsSet.EMPTY) {
            _parameters = new StatsSet();
        }
        _parameters.set(str, val);
    }

    public int getParameter(final String str, final int val) {
        return _parameters.getInteger(str, val);
    }

    public long getParameter(final String str, final long val) {
        return _parameters.getLong(str, val);
    }

    public boolean getParameter(final String str, final boolean val) {
        return _parameters.getBool(str, val);
    }

    public String getParameter(final String str, final String val) {
        return _parameters.getString(str, val);
    }

    public MultiValueSet<String> getParameters() {
        return _parameters;
    }

    public void setParameters(final MultiValueSet<String> set) {
        if (set.isEmpty()) {
            return;
        }
        if (_parameters == StatsSet.EMPTY) {
            _parameters = new MultiValueSet<>(set.size());
        }
        _parameters.putAll(set);
    }

    @Override
    public boolean isInvul() {
        return true;
    }

    public boolean isHasChatWindow() {
        return _hasChatWindow;
    }

    public void setHasChatWindow(final boolean hasChatWindow) {
        _hasChatWindow = hasChatWindow;
    }

    @Override
    public boolean isPolymorphedNpc() {
        return polymorphedData != null;
    }

    /**
     * - Кричит в обычный чат текст, видят все игроки в зоне видимости
     */
    public void MakeFString(final String text) {
        World.getAroundPlayers(this, 1500, Math.max(1500 / 2, 200)).
                stream().
                filter(player -> getReflection() == player.getReflection()).
                forEach(player -> player.sendPacket(new NpcSay(this, ChatType.ALL, text)));
    }

    public void MakeFString(final int id) {
        MakeFString(id, "");
    }

    public void MakeFString(final int id, final String... arg) {
        MakeFString(id, ChatType.NPC_NORMAL, arg);
    }

    public void MakeFString(final int id, final ChatType type) {
        MakeFString(id, type, "");
    }

    public void MakeFString(final int id, ChatType type, final String... params) {
        World.getAroundPlayers(this, 1500, Math.max(1500 / 2, 200)).
                stream().
                filter(player -> getReflection() == player.getReflection()).
                forEach(player -> player.sendPacket(new NpcSay(this, type, PtsUtils.MakeFString(player, id, params))));
    }

    public class BroadcastCharInfoTask extends RunnableImpl {
        @Override
        public void runImpl() {
            broadcastCharInfoImpl();
            _broadcastCharInfoTask = null;
        }
    }
}
