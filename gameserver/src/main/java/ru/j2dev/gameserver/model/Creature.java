package ru.j2dev.gameserver.model;

import gnu.trove.set.hash.TIntHashSet;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.lang.reference.HardReferences;
import ru.j2dev.commons.listener.Listener;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.commons.util.concurrent.atomic.AtomicState;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.ai.CharacterAI;
import ru.j2dev.gameserver.ai.CtrlEvent;
import ru.j2dev.gameserver.ai.CtrlIntention;
import ru.j2dev.gameserver.ai.NextAction;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.geodata.GeoEngine;
import ru.j2dev.gameserver.geodata.GeoMove;
import ru.j2dev.gameserver.manager.DimensionalRiftManager;
import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.model.Skill.SkillTargetType;
import ru.j2dev.gameserver.model.Skill.SkillType;
import ru.j2dev.gameserver.model.Zone.ZoneType;
import ru.j2dev.gameserver.model.actor.listener.CharListenerList;
import ru.j2dev.gameserver.model.actor.recorder.CharStatsChangeRecorder;
import ru.j2dev.gameserver.model.base.InvisibleType;
import ru.j2dev.gameserver.model.base.TeamType;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.model.instances.MinionInstance;
import ru.j2dev.gameserver.model.instances.MonsterInstance;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.instances.StaticObjectInstance;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.model.quest.QuestEventType;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.model.reference.L2Reference;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.components.IStaticPacket;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.*;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ChangeMoveType.GroundPosition;
import ru.j2dev.gameserver.skills.AbnormalEffect;
import ru.j2dev.gameserver.skills.EffectType;
import ru.j2dev.gameserver.skills.TimeStamp;
import ru.j2dev.gameserver.stats.*;
import ru.j2dev.gameserver.stats.Formulas.AttackInfo;
import ru.j2dev.gameserver.stats.funcs.Func;
import ru.j2dev.gameserver.stats.triggers.TriggerInfo;
import ru.j2dev.gameserver.stats.triggers.TriggerType;
import ru.j2dev.gameserver.taskmanager.LazyPrecisionTaskManager;
import ru.j2dev.gameserver.taskmanager.RegenTaskManager;
import ru.j2dev.gameserver.taskmanager.tasks.objecttasks.*;
import ru.j2dev.gameserver.templates.CharTemplate;
import ru.j2dev.gameserver.templates.item.WeaponTemplate;
import ru.j2dev.gameserver.templates.item.WeaponTemplate.WeaponType;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.PositionUtils;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class Creature extends GameObject {
    public static final double HEADINGS_IN_PI = 10430.378350470453;
    public static final int CLIENT_BAR_SIZE = 352;
    private static final Logger LOGGER = LoggerFactory.getLogger(Creature.class);
    private static final double[] POLE_VAMPIRIC_MOD = {1.0, 0.9, 0.0, 7.0, 0.2, 0.01};
    protected final Map<Integer, Skill> _skills = new ConcurrentSkipListMap<>();
    protected final ConcurrentMap<Integer, TimeStamp> _skillReuses = new ConcurrentHashMap<>();
    protected final AtomicBoolean isDead = new AtomicBoolean();
    protected final AtomicBoolean isTeleporting = new AtomicBoolean();
    protected final CharTemplate _baseTemplate;
    protected final HardReference<? extends Creature> reference = new L2Reference<>(this);
    private final Lock moveLock = new ReentrantLock();
    private final Calculator[] _calculators = new Calculator[Stats.NUM_STATS];
    private final Lock regenLock = new ReentrantLock();
    private final Lock statusListenersLock = new ReentrantLock();
    private final AtomicState _afraid = new AtomicState();
    private final AtomicState _muted = new AtomicState();
    private final AtomicState _pmuted = new AtomicState();
    private final AtomicState _amuted = new AtomicState();
    private final AtomicState _paralyzed = new AtomicState();
    private final AtomicState _rooted = new AtomicState();
    private final AtomicState _sleeping = new AtomicState();
    private final AtomicState _stunned = new AtomicState();
    private final AtomicState _immobilized = new AtomicState();
    private final AtomicState _confused = new AtomicState();
    private final AtomicState _frozen = new AtomicState();
    private final AtomicState _healBlocked = new AtomicState();
    private final AtomicState _damageBlocked = new AtomicState();
    private final AtomicState _buffImmunity = new AtomicState();
    private final AtomicState _debuffImmunity = new AtomicState();
    private final AtomicState _effectImmunity = new AtomicState();
    private final AtomicState _weaponEquipBlocked = new AtomicState();
    private final List<Zone> _zones = new ArrayList<>();
    /**
     * Блокировка для чтения/записи объектов из региона
     */
    private final ReadWriteLock zonesLock = new ReentrantReadWriteLock();
    private final Lock zonesRead = zonesLock.readLock();
    private final Lock zonesWrite = zonesLock.writeLock();
    private final TIntHashSet _unActiveSkills = new TIntHashSet();
    public int _scheduledCastInterval;
    public Future<?> _skillTask;
    public Future<?> _skillLaunchedTask;
    protected double _currentCp;
    protected double _currentHp = 1.0;
    protected double _currentMp = 1.0;
    protected boolean _isAttackAborted;
    protected long _attackEndTime;
    protected long _attackReuseEndTime;
    protected Map<TriggerType, Set<TriggerInfo>> _triggers;
    protected volatile EffectList _effectList;
    protected volatile CharStatsChangeRecorder<? extends Creature> _statsRecorder;
    protected boolean _isInvul;
    protected MoveActionBase moveAction;
    protected CharTemplate _template;
    protected volatile CharacterAI _ai;
    protected String _name;
    protected String _title;
    protected TeamType _team = TeamType.NONE;
    protected boolean _checksForTeam;
    protected volatile CharListenerList listeners;
    private Skill _castingSkill;
    private long _castInterruptTime;
    private long _animationEndTime;
    private Future<?> _stanceTask;
    private Runnable _stanceTaskRunnable;
    private long _stanceEndTime;
    private int _lastCpBarUpdate = -1;
    private int _lastHpBarUpdate = -1;
    private int _lastMpBarUpdate = -1;
    private int _poleAttackCount;
    private List<Stats> _blockedStats;
    private int _abnormalEffects;
    private int _abnormalEffects2;
    private int _abnormalEffects3;
    private Map<Integer, Integer> _skillMastery;
    private boolean _isBlessedByNoblesse;
    private boolean _isSalvation;
    private boolean _meditated;
    private boolean _lockedTarget;
    private boolean _blocked;
    private boolean _flying;
    private boolean _running;
    private Future<?> _moveTask;
    private Runnable _moveTaskRunnable;
    private volatile HardReference<? extends GameObject> target = HardReferences.emptyRef();
    private volatile HardReference<? extends Creature> castingTarget = HardReferences.emptyRef();
    private volatile HardReference<? extends Creature> _aggressionTarget = HardReferences.emptyRef();
    private int _heading;
    private boolean _isRegenerating;
    private Future<?> _regenTask;
    private Runnable _regenTaskRunnable;
    private List<Player> _statusListeners;
    private Location _flyLoc;

    public Creature(final int objectId, final CharTemplate template) {
        super(objectId);
        _template = template;
        _baseTemplate = template;
        StatFunctions.addPredefinedFuncs(this);
        GameObjectsStorage.put(this);
    }

    @Override
    public int getActingRange() {
        return 150;
    }

    @Override
    public HardReference<? extends Creature> getRef() {
        return reference;
    }

    public boolean isAttackAborted() {
        return _isAttackAborted;
    }

    public final void abortAttack(final boolean force, final boolean message) {
        if (isAttackingNow()) {
            _attackEndTime = 0L;
            if (force) {
                _isAttackAborted = true;
            }
            getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
            if (isPlayer() && message) {
                sendActionFailed();
                sendPacket(new SystemMessage(158));
            }
        }
    }

    public final void abortCast(final boolean force, final boolean message) {
        if (isCastingNow() && (force || canAbortCast())) {
            final Skill castingSkill = _castingSkill;
            final Future<?> skillTask = _skillTask;
            final Future<?> skillLaunchedTask = _skillLaunchedTask;
            finishFly();
            clearCastVars();
            if (skillTask != null) {
                skillTask.cancel(false);
            }
            if (skillLaunchedTask != null) {
                skillLaunchedTask.cancel(false);
            }
            if (castingSkill != null) {
                if (castingSkill.isUsingWhileCasting()) {
                    final Creature target = getAI().getAttackTarget();
                    if (target != null) {
                        target.getEffectList().stopEffect(castingSkill.getId());
                    }
                }
                removeSkillMastery(castingSkill.getId());
            }
            broadcastPacket(new MagicSkillCanceled(this));
            getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
            if (isPlayer() && message) {
                sendPacket(Msg.CASTING_HAS_BEEN_INTERRUPTED);
            }
        }
    }

    public final boolean canAbortCast() {
        return _castInterruptTime >= System.currentTimeMillis();
    }

    public boolean absorbAndReflect(final Creature target, final Skill skill, double damage, final boolean sendMessage) {
        if (target.isDead()) {
            return false;
        }
        final boolean bow = getActiveWeaponItem() != null && getActiveWeaponItem().getItemType() == WeaponType.BOW;
        double value = 0.0;
        if (skill != null && skill.isMagic()) {
            value = target.calcStat(Stats.REFLECT_AND_BLOCK_MSKILL_DAMAGE_CHANCE, 0.0, this, skill);
        } else if (skill != null && skill.getCastRange() <= 200) {
            value = target.calcStat(Stats.REFLECT_AND_BLOCK_PSKILL_DAMAGE_CHANCE, 0.0, this, skill);
        } else if (skill == null && !bow) {
            value = target.calcStat(Stats.REFLECT_AND_BLOCK_DAMAGE_CHANCE, 0.0, this, null);
        }
        if (value > 0.0 && Rnd.chance(value)) {
            reduceCurrentHp(damage, target, null, true, true, false, false, false, false, true);
            return true;
        }
        if (skill != null && skill.isMagic()) {
            value = target.calcStat(Stats.REFLECT_MSKILL_DAMAGE_PERCENT, 0.0, this, skill);
        } else if (skill != null && skill.getCastRange() <= 200) {
            value = target.calcStat(Stats.REFLECT_PSKILL_DAMAGE_PERCENT, 0.0, this, skill);
        } else if (skill == null && !bow) {
            value = target.calcStat(Stats.REFLECT_DAMAGE_PERCENT, 0.0, this, null);
        }
        if (value > 0.0 && target.getCurrentHp() + target.getCurrentCp() > damage) {
            final double dam = value / 100.0 * damage;
            reduceCurrentHp(dam, target, null, true, true, false, false, false, false, sendMessage);
            if (sendMessage && target.isPlayable()) {
                target.sendPacket(new SystemMessage(35).addNumber((int) dam));
            }
        }
        if (skill != null || bow) {
            return false;
        }
        damage = (int) (damage - target.getCurrentCp());
        if (damage <= 0.0) {
            return false;
        }
        final double poleMod = (_poleAttackCount < POLE_VAMPIRIC_MOD.length) ? POLE_VAMPIRIC_MOD[_poleAttackCount] : 0.0;
        final double absorb = poleMod * calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0.0, target, null);
        if (absorb > 0.0 && !target.isDamageBlocked()) {
            final double limit = calcStat(Stats.HP_LIMIT, null, null) * getMaxHp() / 100.0;
            if (getCurrentHp() < limit) {
                setCurrentHp(Math.min(_currentHp + damage * absorb * Config.ALT_ABSORB_DAMAGE_MODIFIER / 100.0, limit), false);
            }
        }
        return false;
    }

    public double absorbToEffector(final Creature attacker, double damage) {
        final double transferToEffectorDam = calcStat(Stats.TRANSFER_TO_EFFECTOR_DAMAGE_PERCENT, 0.0);
        if (transferToEffectorDam > 0.0) {
            final Effect effect = getEffectList().getEffectByType(EffectType.AbsorbDamageToEffector);
            if (effect == null) {
                return damage;
            }
            final Creature effector = effect.getEffector();
            if (effector == this || effector.isDead() || !isInRange(effector, 1200L)) {
                return damage;
            }
            final Player thisPlayer = getPlayer();
            final Player effectorPlayer = effector.getPlayer();
            if (thisPlayer == null || effectorPlayer == null) {
                return damage;
            }
            if (thisPlayer != effectorPlayer && (!thisPlayer.isOnline() || !thisPlayer.isInParty() || thisPlayer.getParty() != effectorPlayer.getParty())) {
                return damage;
            }
            final double transferDamage = damage * transferToEffectorDam * 0.01;
            damage -= transferDamage;
            effector.reduceCurrentHp(transferDamage, effector, null, false, false, !attacker.isPlayable(), false, true, false, true);
        }
        return damage;
    }

    public double absorbToSummon(final Creature attacker, double damage) {
        final double transferToSummonDam = calcStat(Stats.TRANSFER_TO_SUMMON_DAMAGE_PERCENT, 0.0);
        if (transferToSummonDam > 0.0) {
            final Summon summon = getPet();
            final double transferDamage = damage * transferToSummonDam * 0.01;
            if (summon == null || summon.isDead() || summon.getCurrentHp() < transferDamage) {
                getEffectList().stopEffects(EffectType.AbsorbDamageToSummon);
            } else if (summon.isSummon() && summon.isInRangeZ(this, 1200L)) {
                damage -= transferDamage;
                summon.reduceCurrentHp(transferDamage, summon, null, false, false, false, false, true, false, true);
            }
        }
        return damage;
    }

    public void addBlockStats(final List<Stats> stats) {
        if (_blockedStats == null) {
            _blockedStats = new ArrayList<>();
        }
        _blockedStats.addAll(stats);
    }

    public Skill addSkill(final Skill newSkill) {
        if (newSkill == null) {
            return null;
        }
        final Skill oldSkill = _skills.get(newSkill.getId());
        if (oldSkill != null && oldSkill.getLevel() == newSkill.getLevel()) {
            return newSkill;
        }
        _skills.put(newSkill.getId(), newSkill);
        if (oldSkill != null) {
            removeStatsOwner(oldSkill);
            removeTriggers(oldSkill);
        }
        addTriggers(newSkill);
        addStatFuncs(newSkill.getStatFuncs());
        return oldSkill;
    }

    public Calculator[] getCalculators() {
        return _calculators;
    }

    public final void addStatFunc(final Func f) {
        if (f == null) {
            return;
        }
        final int stat = f.getStat().ordinal();
        synchronized (_calculators) {
            if (_calculators[stat] == null) {
                _calculators[stat] = new Calculator(f.getStat(), this);
            }
            _calculators[stat].addFunc(f);
        }
    }

    public final void addStatFuncs(final Func[] funcs) {
        for (final Func f : funcs) {
            addStatFunc(f);
        }
    }

    public final void removeStatFunc(final Func f) {
        if (f == null) {
            return;
        }
        final int stat = f.getStat().ordinal();
        synchronized (_calculators) {
            if (_calculators[stat] != null) {
                _calculators[stat].removeFunc(f);
            }
        }
    }

    public final void removeStatFuncs(final Func[] funcs) {
        Arrays.stream(funcs).forEach(this::removeStatFunc);
    }

    public final void removeStatsOwner(final Object owner) {
        synchronized (_calculators) {
            Arrays.stream(_calculators).filter(Objects::nonNull).forEach(_calculator -> _calculator.removeOwner(owner));
        }
    }

    public void altOnMagicUseTimer(final Creature aimingTarget, final Skill skill) {
        if (isAlikeDead()) {
            return;
        }
        final List<Creature> targets = skill.getTargets(this, aimingTarget, true);
        final double mpConsume2 = skill.getMpConsume2();
        if (mpConsume2 > 0.0) {
            if (_currentMp < mpConsume2) {
                sendPacket(Msg.NOT_ENOUGH_MP);
                return;
            }
            if (skill.isMagic()) {
                reduceCurrentMp(calcStat(Stats.MP_MAGIC_SKILL_CONSUME, mpConsume2, aimingTarget, skill), null);
            } else {
                reduceCurrentMp(calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, mpConsume2, aimingTarget, skill), null);
            }
        }
        callSkill(skill, targets, false);
        broadcastPacket(new MagicSkillLaunched(this, skill, targets.stream().mapToInt(GameObject::getObjectId).toArray()));
    }

    public void altUseSkill(final Skill skill, Creature target) {
        if (skill == null) {
            return;
        }
        final int magicId = skill.getId();
        if (isUnActiveSkill(magicId)) {
            return;
        }
        if (isSkillDisabled(skill)) {
            sendReuseMessage(skill);
            return;
        }
        if (target == null) {
            target = skill.getAimingTarget(this, getTarget());
            if (target == null) {
                return;
            }
        }
        getListeners().onMagicUse(skill, target, true);
        final int[] itemConsume = skill.getItemConsume();
        if (itemConsume[0] > 0) {
            for (int i = 0; i < itemConsume.length; ++i) {
                if (!consumeItem(skill.getItemConsumeId()[i], itemConsume[i])) {
                    sendPacket(skill.isHandler() ? SystemMsg.INCORRECT_ITEM_COUNT : new SystemMessage(113).addSkillName(skill.getDisplayId(), skill.getDisplayLevel()));
                    return;
                }
            }
        }
        if (skill.getReferenceItemId() > 0 && !consumeItemMp(skill.getReferenceItemId(), skill.getReferenceItemMpConsume())) {
            return;
        }
        if (skill.getEnergyConsume() > getAgathionEnergy()) {
            sendPacket(SystemMsg.THE_SKILL_HAS_BEEN_CANCELED_BECAUSE_YOU_HAVE_INSUFFICIENT_ENERGY);
            return;
        }
        if (skill.getEnergyConsume() > 0) {
            setAgathionEnergy(getAgathionEnergy() - skill.getEnergyConsume());
        }
        final int level = Math.max(1, getSkillDisplayLevel(magicId));
        Formulas.calcSkillMastery(skill, this);
        final long reuseDelay = Formulas.calcSkillReuseDelay(this, skill);
        if (!skill.isToggle()) {
            broadcastPacket(new MagicSkillUse(this, target, skill.getDisplayId(), level, skill.getHitTime(), reuseDelay));
        }
        if (!skill.isHideUseMessage()) {
            if (skill.getSkillType() == SkillType.PET_SUMMON) {
                sendPacket(new SystemMessage(547));
            } else if (!skill.isHandler()) {
                sendPacket(new SystemMessage(46).addSkillName(magicId, level));
            } else {
                sendPacket(new SystemMessage(46).addItemName(skill.getItemConsumeId()[0]));
            }
        }
        if (!skill.isHandler()) {
            disableSkill(skill, reuseDelay);
        }
        ThreadPoolManager.getInstance().schedule(new AltMagicUseTask(this, target, skill), skill.getHitTime());
    }

    public void sendReuseMessage(final Skill skill) {
    }

    public void broadcastPacket(final L2GameServerPacket... packets) {
        sendPacket(packets);
        broadcastPacketToOthers(packets);
    }

    public void broadcastPacket(final List<L2GameServerPacket> packets) {
        sendPacket(packets);
        broadcastPacketToOthers(packets);
    }

    public void broadcastPacketToOthers(final L2GameServerPacket... packets) {
        if (!isVisible() || packets.length == 0) {
            return;
        }
        final List<Player> players = World.getAroundPlayers(this);
        players.forEach(target -> target.sendPacket((IStaticPacket[]) packets));
    }

    public void broadcastPacketToOthers(final List<L2GameServerPacket> packets) {
        if (!isVisible() || packets.isEmpty()) {
            return;
        }
        final List<Player> players = World.getAroundPlayers(this);
        players.forEach(target -> target.sendPacket(packets));
    }

    public void broadcastToStatusListeners(final L2GameServerPacket... packets) {
        if (!isVisible() || packets.length == 0) {
            return;
        }
        statusListenersLock.lock();
        try {
            if (_statusListeners == null || _statusListeners.isEmpty()) {
                return;
            }
            _statusListeners.forEach(player -> player.sendPacket((packets)));
        } finally {
            statusListenersLock.unlock();
        }
    }

    public void addStatusListener(final Player cha) {
        if (cha == this) {
            return;
        }
        statusListenersLock.lock();
        try {
            if (_statusListeners == null) {
                _statusListeners = new ArrayList<>();
            }
            if (!_statusListeners.contains(cha)) {
                _statusListeners.add(cha);
            }
        } finally {
            statusListenersLock.unlock();
        }
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    public void removeStatusListener(final Creature cha) {
        statusListenersLock.lock();
        try {
            if (_statusListeners == null) {
                return;
            }
            _statusListeners.remove(cha);
        } finally {
            statusListenersLock.unlock();
        }
    }

    public void clearStatusListeners() {
        statusListenersLock.lock();
        try {
            if (_statusListeners == null) {
                return;
            }
            _statusListeners.clear();
        } finally {
            statusListenersLock.unlock();
        }
    }

    public StatusUpdate makeStatusUpdate(final int... fields) {
        final StatusUpdate su = new StatusUpdate(getObjectId());
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
                case 26: {
                    su.addAttribute(field, getPvpFlag());
                    break;
                }
            }
        }
        return su;
    }

    public void broadcastStatusUpdate() {
        if (!needStatusUpdate()) {
            return;
        }
        final StatusUpdate su = makeStatusUpdate(10, 12, 9, 11);
        broadcastToStatusListeners(su);
    }

    public int calcHeading(final int x_dest, final int y_dest) {
        return (int) (Math.atan2(getY() - y_dest, getX() - x_dest) * 10430.378350470453) + 32768;
    }

    public final double calcStat(final Stats stat, final double init) {
        return calcStat(stat, init, null, null);
    }

    public final double calcStat(final Stats stat, final double init, final Creature target, final Skill skill) {
        final int id = stat.ordinal();
        final Calculator c = _calculators[id];
        if (c == null) {
            return init;
        }
        final Env env = new Env();
        env.character = this;
        env.target = target;
        env.skill = skill;
        env.value = init;
        c.calc(env);
        return env.value;
    }

    public final double calcStat(final Stats stat, final Creature target, final Skill skill) {
        final Env env = new Env(this, target, skill);
        env.value = stat.getInit();
        final int id = stat.ordinal();
        final Calculator c = _calculators[id];
        if (c != null) {
            c.calc(env);
        }
        return env.value;
    }

    public int calculateAttackDelay() {
        return Formulas.calcPAtkSpd(getPAtkSpd());
    }

    public void callSkill(final Skill skill, final List<Creature> targets, final boolean useActionSkills) {
        try {
            if (useActionSkills && !skill.isUsingWhileCasting() && _triggers != null) {
                if (skill.isOffensive()) {
                    if (skill.isMagic()) {
                        useTriggers(getTarget(), TriggerType.OFFENSIVE_MAGICAL_SKILL_USE, null, skill, 0.0);
                    } else {
                        useTriggers(getTarget(), TriggerType.OFFENSIVE_PHYSICAL_SKILL_USE, null, skill, 0.0);
                    }
                } else if (Config.BUFF_STICK_FOR_ALL || skill.isMagic()) {
                    final boolean targetSelf = skill.isAoE() || skill.isNotTargetAoE() || skill.getTargetType() == SkillTargetType.TARGET_SELF;
                    useTriggers(targetSelf ? this : getTarget(), TriggerType.SUPPORT_MAGICAL_SKILL_USE, null, skill, 0.0);
                }
            }

            List<Creature> filteredTargets = targets.stream().filter(input -> ((Predicate<Creature>) creature -> {
                if (skill.isOffensive() && input.isInvul() && (!skill.isIgnoreInvul() || input.getPlayer() != null && input.getPlayer().isGM()) && !input.isArtefact())
                    return false;
                final Effect ie = input.getEffectList().getEffectByType(EffectType.IgnoreSkill);
                return !(ie != null && ArrayUtils.contains(ie.getTemplate().getParam().getIntegerArray("skillId"), skill.getId()));
            }).test(input)).collect(Collectors.toList());


            final Player pl = getPlayer();
            filteredTargets.forEach(target -> {
                target.getListeners().onMagicHit(skill, this);
                if (pl != null && target.isNpc()) {
                    final NpcInstance npc = (NpcInstance) target;
                    final List<QuestState> ql = pl.getQuestsForEvent(npc, QuestEventType.MOB_TARGETED_BY_SKILL);
                    if (ql != null) {
                        ql.forEach(qs -> qs.getQuest().notifySkillUse(npc, skill, qs));
                    }
                }
                if (skill.getNegateSkill() > 0) {
                    target.getEffectList().getAllEffects().forEach(e -> {
                        final Skill efs = e.getSkill();
                        if (efs.getId() == skill.getNegateSkill() && e.isCancelable() && (skill.getNegatePower() <= 0 || efs.getPower() <= skill.getNegatePower())) {
                            e.exit();
                        }
                    });
                }
                if (skill.getCancelTarget() <= 0 || !Rnd.chance(skill.getCancelTarget()) || (target.getCastingSkill() != null && target.getCastingSkill().getSkillType() == SkillType.TAKECASTLE) || target.isRaid()) {
                    return;
                }
                target.abortAttack(true, true);
                target.abortCast(true, true);
                target.setTarget(null);
            });
            if (skill.isOffensive()) {
                startAttackStanceTask();
            }
            if (!skill.isNotTargetAoE() || !skill.isOffensive() || filteredTargets.size() != 0) {
                skill.getEffects(this, this, false, true);
            }
            skill.useSkill(this, filteredTargets);
        } catch (Exception e2) {
            LOGGER.error("", e2);
        }
    }

    public void useTriggers(final GameObject target, final TriggerType type, final Skill ex, final Skill owner, final double damage) {
        if (_triggers == null) {
            return;
        }
        final Set<TriggerInfo> triggerInfos = _triggers.get(type);
        if (triggerInfos != null) {
            triggerInfos.stream().filter(t -> t.getSkill() != ex).forEach(t -> useTriggerSkill((target == null) ? getTarget() : target, null, t, owner, damage));
        }
    }

    public void useTriggerSkill(final GameObject target, List<Creature> targets, final TriggerInfo trigger, final Skill owner, final double damage) {
        final Skill skill = trigger.getSkill();
        if (skill.getReuseDelay() > 0L && isSkillDisabled(skill)) {
            return;
        }
        final Creature aimTarget = skill.getAimingTarget(this, target);
        final Creature realTarget = (target != null && target.isCreature()) ? ((Creature) target) : null;
        if (Rnd.chance(trigger.getChance()) && trigger.checkCondition(this, realTarget, aimTarget, owner, damage) && skill.checkCondition(this, aimTarget, false, true, true)) {
            if (targets == null) {
                targets = skill.getTargets(this, aimTarget, false);
            }
            int displayId = 0;
            int displayLevel = 0;
            if (skill.hasEffects()) {
                displayId = skill.getEffectTemplates()[0]._displayId;
                displayLevel = skill.getEffectTemplates()[0]._displayLevel;
            }
            if (displayId == 0) {
                displayId = skill.getDisplayId();
            }
            if (displayLevel == 0) {
                displayLevel = skill.getDisplayLevel();
            }
            if (trigger.getType() != TriggerType.SUPPORT_MAGICAL_SKILL_USE) {
                for (final Creature cha : targets) {
                    broadcastPacket(new MagicSkillUse(this, cha, displayId, displayLevel, 0, 0L));
                }
            }
            callSkill(skill, targets, false);
            disableSkill(skill, skill.getReuseDelay());
        }
    }

    public boolean checkBlockedStat(final Stats stat) {
        return _blockedStats != null && _blockedStats.contains(stat);
    }

    public boolean checkReflectSkill(final Creature attacker, final Skill skill) {
        if (!skill.isReflectable()) {
            return false;
        }
        if (isInvul() || attacker.isInvul() || !skill.isOffensive()) {
            return false;
        }
        if (skill.isMagic() && skill.getSkillType() != SkillType.MDAM) {
            return false;
        }
        if (Rnd.chance(calcStat(skill.isMagic() ? Stats.REFLECT_MAGIC_SKILL : Stats.REFLECT_PHYSIC_SKILL, 0.0, attacker, skill))) {
            sendPacket(new SystemMessage(1998).addName(attacker));
            attacker.sendPacket(new SystemMessage(1999).addName(this));
            return true;
        }
        return false;
    }

    public void doCounterAttack(final Skill skill, final Creature attacker, final boolean blow) {
        if (isDead()) {
            return;
        }
        if (isDamageBlocked() || attacker.isDamageBlocked()) {
            return;
        }
        if (skill == null || skill.hasEffects() || skill.isMagic() || !skill.isOffensive() || skill.getCastRange() > 200) {
            return;
        }
        if (Rnd.chance(calcStat(Stats.COUNTER_ATTACK, 0.0, attacker, skill))) {
            final double damage = 1189 * getPAtk(attacker) / Math.max(attacker.getPDef(this), 1);
            attacker.sendPacket(new SystemMessage(1997).addName(this));
            if (blow) {
                sendPacket(new SystemMessage(1997).addName(this));
                sendPacket(new SystemMessage(35).addNumber((long) damage));
                attacker.reduceCurrentHp(damage, this, skill, true, true, false, false, false, false, true);
            } else {
                sendPacket(new SystemMessage(1997).addName(this));
            }
            sendPacket(new SystemMessage(35).addNumber((long) damage));
            attacker.reduceCurrentHp(damage, this, skill, true, true, false, false, false, false, true);
        }
    }

    public void disableSkill(final Skill skill, final long delay) {
        _skillReuses.put(skill.hashCode(), new TimeStamp(skill, delay));
    }

    public abstract boolean isAutoAttackable(final Creature p0);

    public void doAttack(final Creature target) {
        if (target == null || isAMuted() || isAttackingNow() || isAlikeDead() || target.isAlikeDead() || !isInRange(target, 2048L) || (isPlayer() && getPlayer().isInMountTransform())) {
            return;
        }
        getListeners().onAttack(target);
        if (Config.ALT_TELEPORT_PROTECTION && isPlayer()) {
            final Player player = getPlayer();
            if (player.getAfterTeleportPortectionTime() > System.currentTimeMillis()) {
                player.setAfterTeleportPortectionTime(0L);
                player.sendMessage(new CustomMessage("alt.teleport_protect_gonna", player));
            }
        }
        final int sAtk = Math.max(calculateAttackDelay(), Config.MIN_ATK_DELAY);
        int ssGrade = 0;
        int reuse = sAtk;
        _attackEndTime = sAtk + System.currentTimeMillis() - Config.ATTACK_END_DELAY;
        _isAttackAborted = false;
        final WeaponTemplate weaponItem = getActiveWeaponItem();
        if (weaponItem != null) {
            reuse = sAtk + (int) (weaponItem.getAttackReuseDelay() / (getPAtkSpd() / 333.0f));
            if (isPlayer() && weaponItem.getAttackReuseDelay() > 0 && reuse > 0) {
                sendPacket(new SetupGauge(this, 1, reuse));
                _attackReuseEndTime = reuse + System.currentTimeMillis() - Config.ATTACK_END_DELAY;
            }
            ssGrade = weaponItem.getCrystalType().gradeOrd();
        }
        final Attack attack = new Attack(this, target, getChargedSoulShot(), ssGrade);
        setHeading(PositionUtils.calculateHeadingFrom(this, target));
        final int hitDelay = reuse / 2;
        if (weaponItem == null) {
            doAttackHitSimple(attack, target, 1.0, !isPlayer(), hitDelay, true);
        } else {
            switch (weaponItem.getItemType()) {
                case BOW: {
                    doAttackHitByBow(attack, target, hitDelay);
                    break;
                }
                case POLE: {
                    doAttackHitByPole(attack, target, hitDelay);
                    break;
                }
                case DUAL:
                case DUALFIST: {
                    doAttackHitByDual(attack, target, hitDelay);
                    break;
                }
                default: {
                    doAttackHitSimple(attack, target, 1.0, true, hitDelay, true);
                    break;
                }
            }
        }
        if (attack.hasHits()) {
            broadcastPacket(attack);
        }
    }

    private void doAttackHitSimple(final Attack attack, final Creature target, final double multiplier, final boolean unchargeSS, final long hitDelay, final boolean notify) {
        int damage1 = 0;
        boolean shld1 = false;
        boolean crit1 = false;
        final boolean miss1 = Formulas.calcHitMiss(this, target);
        if (!miss1) {
            final AttackInfo info = Formulas.calcPhysDam(this, target, null, false, false, attack._soulshot, false);
            damage1 = (int) (info.damage * multiplier);
            shld1 = info.shld;
            crit1 = info.crit;
        }
        ThreadPoolManager.getInstance().schedule(new HitTask(this, target, damage1, crit1, miss1, attack._soulshot, shld1, unchargeSS, notify, hitDelay), hitDelay);
        attack.addHit(target, damage1, miss1, crit1, shld1);
    }

    private void doAttackHitByBow(final Attack attack, final Creature target, final long hitDelay) {
        final WeaponTemplate activeWeapon = getActiveWeaponItem();
        if (activeWeapon == null) {
            return;
        }
        int damage1 = 0;
        boolean shld1 = false;
        boolean crit1 = false;
        final boolean miss1 = Formulas.calcHitMiss(this, target);
        if (Config.ALT_CONSUME_ARROWS) {
            reduceArrowCount();
        }
        if (!miss1) {
            final AttackInfo info = Formulas.calcPhysDam(this, target, null, false, false, attack._soulshot, false);
            damage1 = (int) info.damage;
            shld1 = info.shld;
            crit1 = info.crit;
        }
        ThreadPoolManager.getInstance().schedule(new HitTask(this, target, damage1, crit1, miss1, attack._soulshot, shld1, true, true, hitDelay), hitDelay);
        attack.addHit(target, damage1, miss1, crit1, shld1);
    }

    private void doAttackHitByDual(final Attack attack, final Creature target, final long hitDelay) {
        int damage1 = 0;
        int damage2 = 0;
        boolean shld1 = false;
        boolean shld2 = false;
        boolean crit1 = false;
        boolean crit2 = false;
        final boolean miss1 = Formulas.calcHitMiss(this, target);
        final boolean miss2 = Formulas.calcHitMiss(this, target);
        if (!miss1) {
            final AttackInfo info = Formulas.calcPhysDam(this, target, null, true, false, attack._soulshot, false);
            damage1 = (int) info.damage;
            shld1 = info.shld;
            crit1 = info.crit;
        }
        if (!miss2) {
            final AttackInfo info = Formulas.calcPhysDam(this, target, null, true, false, attack._soulshot, false);
            damage2 = (int) info.damage;
            shld2 = info.shld;
            crit2 = info.crit;
        }
        ThreadPoolManager.getInstance().schedule(new HitTask(this, target, damage1, crit1, miss1, attack._soulshot, shld1, true, false), hitDelay / 2L);
        ThreadPoolManager.getInstance().schedule(new HitTask(this, target, damage2, crit2, miss2, attack._soulshot, shld2, false, true, hitDelay), hitDelay);
        attack.addHit(target, damage1, miss1, crit1, shld1);
        attack.addHit(target, damage2, miss2, crit2, shld2);
    }

    private int getBaseAttackAngele() {
        WeaponTemplate weaponTemplate = getActiveWeaponItem();
        if (weaponTemplate != null) {
            //todo baseDamRange for weapons
        }
        return getTemplate().getBaseDamageRange()[3];
    }

    private void doAttackHitByPole(final Attack attack, final Creature target, final long hitDelay) {
        final int angle = (int) calcStat(Stats.POLE_ATTACK_ANGLE, getBaseAttackAngele(), target, null);
        final int range = (int) calcStat(Stats.POWER_ATTACK_RANGE, getTemplate().getBaseAtkRange(), target, null);
        int attackcountmax = (int) Math.round(calcStat(Stats.POLE_TARGET_COUNT, 0.0, target, null));
        if (isBoss()) {
            attackcountmax += 27;
        } else if (isRaid()) {
            attackcountmax += 12;
        } else if (isMonster() && getLevel() > 0) {
            attackcountmax += (int) (getLevel() / 7.5);
        }
        double mult = 1.0;
        _poleAttackCount = 1;
        if (!isInZonePeace()) {
            for (final Creature t : getAroundCharacters(range, 200)) {
                if (_poleAttackCount > attackcountmax) {
                    break;
                }
                if (Math.abs(target.getZ() - getZ()) > 650) {
                    continue;
                }
                if (t == target || t.isDead()) {
                    continue;
                }
                if (!PositionUtils.isFacing(this, t, angle)) {
                    continue;
                }
                if (!t.isAutoAttackable(this)) {
                    continue;
                }
                doAttackHitSimple(attack, t, mult, false, hitDelay, false);
                mult *= Config.ALT_POLE_DAMAGE_MODIFIER;
                _poleAttackCount++;
            }
        }
        _poleAttackCount = 0;
        doAttackHitSimple(attack, target, 1.0, true, hitDelay, true);
    }

    public long getAnimationEndTime() {
        return _animationEndTime;
    }

    public void doCast(final Skill skill, Creature target, final boolean forceUse) {
        if (skill == null) {
            return;
        }
        final int[] itemConsume = skill.getItemConsume();
        if (itemConsume[0] > 0) {
            for (int i = 0; i < itemConsume.length; ++i) {
                if (!consumeItem(skill.getItemConsumeId()[i], itemConsume[i])) {
                    sendPacket(skill.isHandler() ? SystemMsg.INCORRECT_ITEM_COUNT : new SystemMessage(113).addSkillName(skill.getId(), skill.getLevel()));
                    return;
                }
            }
        }
        if (skill.getReferenceItemId() > 0 && !consumeItemMp(skill.getReferenceItemId(), skill.getReferenceItemMpConsume())) {
            return;
        }
        final int magicId = skill.getId();
        if (target == null) {
            target = skill.getAimingTarget(this, getTarget());
        }
        if (target == null) {
            return;
        }
        getListeners().onMagicUse(skill, target, false);
        if (this != target) {
            setHeading(PositionUtils.calculateHeadingFrom(this, target));
        }
        final int level = Math.max(1, getSkillDisplayLevel(magicId));
        int skillTime = skill.isSkillTimePermanent() ? skill.getHitTime() : Formulas.calcMAtkSpd(this, skill, skill.getHitTime());
        int skillInterruptTime = skill.getSkillInterruptTime();
        final int minCastTime = Math.min(Config.SKILLS_CAST_TIME_MIN, skill.getHitTime());
        if (skillTime < minCastTime) {
            skillTime = minCastTime;
            skillInterruptTime = 0;
        }
        _animationEndTime = System.currentTimeMillis() + skillTime;
        if (skill.isMagic() && !skill.isSkillTimePermanent() && getChargedSpiritShot() > 0) {
            skillTime = (int) (0.70 * skillTime);
            skillInterruptTime = (int) (0.70 * skillInterruptTime);
        }
        Formulas.calcSkillMastery(skill, this);
        final long reuseDelay = Math.max(0L, Formulas.calcSkillReuseDelay(this, skill));
        broadcastPacket(new MagicSkillUse(this, target, skill, skillTime, reuseDelay));
        if (!skill.isHandler()) {
            disableSkill(skill, reuseDelay);
        }
        if (isPlayer()) {
            if (skill.getSkillType() == SkillType.PET_SUMMON) {
                sendPacket(Msg.SUMMON_A_PET);
            } else if (!skill.isHandler()) {
                sendPacket(new SystemMessage(46).addSkillName(magicId, level));
            } else {
                sendPacket(new SystemMessage(46).addItemName(skill.getItemConsumeId()[0]));
            }
        }
        if (skill.getTargetType() == SkillTargetType.TARGET_HOLY) {
            target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, this, 1);
        }
        final double mpConsume1 = skill.isUsingWhileCasting() ? skill.getMpConsume() : skill.getMpConsume1();
        if (mpConsume1 > 0.0) {
            if (_currentMp < mpConsume1) {
                sendPacket(Msg.NOT_ENOUGH_MP);
                onCastEndTime();
                return;
            }
            reduceCurrentMp(mpConsume1, null);
        }
        _flyLoc = null;
        switch (skill.getFlyType()) {
            case DUMMY:
            case CHARGE: {
                final Location flyLoc = getFlyLocation(target, skill);
                if (flyLoc != null) {
                    _flyLoc = flyLoc;
                    broadcastPacket(new FlyToLocation(this, flyLoc, skill.getFlyType()));
                    break;
                }
                _animationEndTime = 0L;
                sendPacket(SystemMsg.CANNOT_SEE_TARGET);
                return;
            }
        }
        _castingSkill = skill;
        final int skillLaunchTime = (skillInterruptTime > 0) ? Math.max(0, skillTime - skillInterruptTime) : 0;
        _castInterruptTime = System.currentTimeMillis() + skillLaunchTime;
        setCastingTarget(target);
        if (skill.isUsingWhileCasting()) {
            callSkill(skill, skill.getTargets(this, target, forceUse), true);
        }
        _scheduledCastInterval = skillTime;
        if (skillTime > 333 && isPlayer()) {
            sendPacket(new SetupGauge(this, 0, skillTime));
        }
        scheduleSkillLaunchedTask(forceUse, skillLaunchTime);
        scheduleSkillUseTask(forceUse, skillTime);
    }

    private void scheduleSkillLaunchedTask(final boolean forceUse, final int skillLaunchTime) {
        _skillLaunchedTask = ThreadPoolManager.getInstance().schedule(new MagicLaunchedTask(this, forceUse), skillLaunchTime);
    }

    private void scheduleSkillUseTask(final boolean forceUse, final int skillTime) {
        _skillTask = ThreadPoolManager.getInstance().schedule(new MagicUseTask(this, forceUse), skillTime);
    }

    public void clearCastVars() {
        _animationEndTime = 0L;
        _castInterruptTime = 0L;
        _castingSkill = null;
        _skillTask = null;
        _skillLaunchedTask = null;
        _flyLoc = null;
    }

    private Location getFlyLocation(final GameObject target, final Skill skill) {
        if (target != null && target != this) {
            Location loc;
            if (skill.isFlyToBack()) {
                final double radian = PositionUtils.convertHeadingToRadian(target.getHeading());
                loc = new Location(target.getX() + (int) (Math.sin(radian) * 40.0), target.getY() - (int) (Math.cos(radian) * 40.0), target.getZ());
            } else {
                final double alpha = Math.atan2(getY() - target.getY(), getX() - target.getX());
                loc = new Location(target.getX() + (int) Math.round(Math.cos(alpha) * 40.0), target.getY() + (int) Math.round(Math.sin(alpha) * 40.0), target.getZ());
            }
            if (isFlying()) {
                if (isPlayer() && ((Player) this).isInFlyingTransform() && (loc.z <= 0 || loc.z >= 6000)) {
                    return null;
                }
                if (GeoEngine.moveCheckInAir(getX(), getY(), getZ(), loc.x, loc.y, loc.z, getColRadius(), getGeoIndex()) == null) {
                    return null;
                }
            } else {
                loc.correctGeoZ();
                if (!GeoEngine.canMoveToCoord(getX(), getY(), getZ(), loc.x, loc.y, loc.z, getGeoIndex())) {
                    loc = target.getLoc();
                    if (!GeoEngine.canMoveToCoord(getX(), getY(), getZ(), loc.x, loc.y, loc.z, getGeoIndex())) {
                        return null;
                    }
                }
            }
            return loc;
        }
        final double radian2 = PositionUtils.convertHeadingToRadian(getHeading());
        final int x1 = -(int) (Math.sin(radian2) * skill.getFlyRadius());
        final int y1 = (int) (Math.cos(radian2) * skill.getFlyRadius());
        if (isFlying()) {
            return GeoEngine.moveCheckInAir(getX(), getY(), getZ(), getX() + x1, getY() + y1, getZ(), getColRadius(), getGeoIndex());
        }
        return GeoEngine.moveCheck(getX(), getY(), getZ(), getX() + x1, getY() + y1, getGeoIndex());
    }

    public final void doDie(final Creature killer) {
        if (!isDead.compareAndSet(false, true)) {
            return;
        }
        onDeath(killer);
    }

    protected void onDeath(final Creature killer) {
        if (killer != null) {
            final Player killerPlayer = killer.getPlayer();
            if (killerPlayer != null) {
                killerPlayer.getListeners().onKillIgnorePetOrSummon(this);
            }
            killer.getListeners().onKill(this);
            if (isPlayer() && killer.isPlayable()) {
                _currentCp = 0.0;
            }
        }
        setTarget(null);
        stopMove();
        stopAttackStanceTask();
        stopRegeneration();
        _currentHp = 0.0;
        if (isBlessedByNoblesse() || isSalvation()) {
            if (isSalvation() && isPlayer() && !getPlayer().isOlyParticipant() && !getPlayer().isResurectProhibited()) {
                getPlayer().reviveRequest(getPlayer(), 100.0, false);
            }
            for (final Effect e : getEffectList().getAllEffects()) {
                if (e.getEffectType() == EffectType.BlessNoblesse || e.getSkill().getId() == 1325 || e.getSkill().getId() == 2168) {
                    e.exit();
                }
            }
        } else if (Config.ALT_PASSIVE_NOBLESS_ID == 0 || getKnownSkill(Config.ALT_PASSIVE_NOBLESS_ID) == null) {
            for (final Effect e : getEffectList().getAllEffects()) {
                if (e.getEffectType() != EffectType.Transformation && !e.getSkill().isPreservedOnDeath()) {
                    e.exit();
                }
            }
        }
        ThreadPoolManager.getInstance().execute(new NotifyAITask(this, CtrlEvent.EVT_DEAD, killer));
        getListeners().onDeath(killer);
        updateEffectIcons();
        updateStats();
        broadcastStatusUpdate();
    }

    protected void onRevive() {
    }

    public void enableSkill(final Skill skill) {
        _skillReuses.remove(skill.hashCode());
    }

    public int getAbnormalEffect() {
        return _abnormalEffects;
    }

    public int getAbnormalEffect2() {
        return _abnormalEffects2;
    }

    public int getAbnormalEffect3() {
        return _abnormalEffects3;
    }

    public int getAccuracy() {
        return (int) calcStat(Stats.ACCURACY_COMBAT, 0.0, null, null);
    }

    public Collection<Skill> getAllSkills() {
        return _skills.values();
    }

    public final Skill[] getAllSkillsArray() {
        final Collection<Skill> vals = _skills.values();
        return vals.toArray(new Skill[0]);
    }

    public final double getAttackSpeedMultiplier() {
        return getTemplate().getHitTimeFactor() * getPAtkSpd() / getTemplate().getBasePAtkSpd();
    }

    public int getBuffLimit() {
        return (int) calcStat(Stats.BUFF_LIMIT, Config.ALT_BUFF_LIMIT, null, null);
    }

    public Skill getCastingSkill() {
        return _castingSkill;
    }

    public int getCON() {
        return (int) calcStat(Stats.STAT_CON, _template.getBaseCON(), null, null);
    }

    public int getCriticalHit(final Creature target, final Skill skill) {
        return (int) calcStat(Stats.CRITICAL_BASE, _template.getBaseCritRate(), target, skill);
    }

    public double getMagicCriticalRate(final Creature target, final Skill skill) {
        return calcStat(Stats.MCRITICAL_RATE, target, skill);
    }

    public final double getCurrentCp() {
        return _currentCp;
    }

    public final void setCurrentCp(final double newCp) {
        setCurrentCp(newCp, true);
    }

    public final double getCurrentCpRatio() {
        return getCurrentCp() / getMaxCp();
    }

    public final double getCurrentCpPercents() {
        return getCurrentCpRatio() * 100.0;
    }

    public final boolean isCurrentCpFull() {
        return getCurrentCp() >= getMaxCp();
    }

    public final boolean isCurrentCpZero() {
        return getCurrentCp() < 1.0;
    }

    public final double getCurrentHp() {
        return _currentHp;
    }

    public final double getCurrentHpRatio() {
        return getCurrentHp() / getMaxHp();
    }

    public final double getCurrentHpPercents() {
        return getCurrentHpRatio() * 100.0;
    }

    public final boolean isCurrentHpFull() {
        return getCurrentHp() >= getMaxHp();
    }

    public final boolean isCurrentHpZero() {
        return getCurrentHp() < 1.0;
    }

    public final double getCurrentMp() {
        return _currentMp;
    }

    public final void setCurrentMp(final double newMp) {
        setCurrentMp(newMp, true);
    }

    public final double getCurrentMpRatio() {
        return getCurrentMp() / getMaxMp();
    }

    public final double getCurrentMpPercents() {
        return getCurrentMpRatio() * 100.0;
    }

    public final boolean isCurrentMpFull() {
        return getCurrentMp() >= getMaxMp();
    }

    public final boolean isCurrentMpZero() {
        return getCurrentMp() < 1.0;
    }

    public int getDEX() {
        return (int) calcStat(Stats.STAT_DEX, _template.getBaseDEX(), null, null);
    }

    public int getEvasionRate(final Creature target) {
        return (int) calcStat(Stats.EVASION_RATE, 0.0, target, null);
    }

    public int getINT() {
        return (int) calcStat(Stats.STAT_INT, _template.getBaseINT(), null, null);
    }

    public List<Creature> getAroundCharacters(final int radius, final int height) {
        if (!isVisible()) {
            return Collections.emptyList();
        }
        return World.getAroundCharacters(this, radius, height);
    }

    public List<MonsterInstance> getArountMonsters(final int radius, final int height) {
        if (!isVisible()) {
            return Collections.emptyList();
        }
        return World.getAroundMonsters(this, radius, height);
    }

    public List<NpcInstance> getAroundNpc(final int range, final int height) {
        if (!isVisible()) {
            return Collections.emptyList();
        }
        return World.getAroundNpc(this, range, height);
    }

    public boolean knowsObject(final GameObject obj) {
        return World.getAroundObjectById(this, obj.getObjectId()) != null;
    }

    public final Skill getKnownSkill(final int skillId) {
        return _skills.get(skillId);
    }

    public final int getMagicalAttackRange(final Skill skill) {
        if (skill != null) {
            return (int) calcStat(Stats.MAGIC_ATTACK_RANGE, skill.getCastRange(), null, skill);
        }
        return getTemplate().getBaseAtkRange();
    }

    public final int getMagicalAttackRange(final double base, final Skill skill) {
        if (skill != null) {
            return (int) calcStat(Stats.MAGIC_ATTACK_RANGE, base, null, skill);
        }
        return getTemplate().getBaseAtkRange();
    }

    public int getMAtk(final Creature target, final Skill skill) {
        if (skill != null && skill.getMatak() > 0) {
            return skill.getMatak();
        }
        return (int) calcStat(Stats.MAGIC_ATTACK, _template.getBaseMAtk(), target, skill);
    }

    public int getMAtkSpd() {
        return (int) calcStat(Stats.MAGIC_ATTACK_SPEED, _template.getBaseMAtkSpd(), null, null);
    }

    public final int getMaxCp() {
        return (int) calcStat(Stats.MAX_CP, _template.getBaseCpMax(), null, null);
    }

    public int getMaxHp() {
        return (int) calcStat(Stats.MAX_HP, _template.getBaseHpMax(), null, null);
    }

    public int getMaxMp() {
        return (int) calcStat(Stats.MAX_MP, _template.getBaseMpMax(), null, null);
    }

    public int getMDef(final Creature target, final Skill skill) {
        return Math.max((int) calcStat(Stats.MAGIC_DEFENCE, _template.getBaseMDef(), target, skill), 1);
    }

    public int getMEN() {
        return (int) calcStat(Stats.STAT_MEN, _template.getBaseMEN(), null, null);
    }

    public double getMinDistance(final GameObject obj) {
        double distance = getTemplate().getCollisionRadius();
        if (obj != null && obj.isCreature()) {
            distance += ((Creature) obj).getTemplate().getCollisionRadius();
        }
        return distance;
    }

    public double getMovementSpeedMultiplier() {
        return getRunSpeed() * 1.0 / _template.getBaseRunSpd();
    }

    @Override
    public int getMoveSpeed() {
        if (isRunning()) {
            return getRunSpeed();
        }
        return getWalkSpeed();
    }

    @Override
    public String getName() {
        return StringUtils.defaultString(_name);
    }

    public final void setName(final String name) {
        _name = name;
    }

    public int getPAtk(final Creature target) {
        return (int) calcStat(Stats.POWER_ATTACK, _template.getBasePAtk(), target, null);
    }

    public int getPAtkSpd() {
        return (int) calcStat(Stats.POWER_ATTACK_SPEED, _template.getBasePAtkSpd(), null, null);
    }

    public int getPDef(final Creature target) {
        return (int) calcStat(Stats.POWER_DEFENCE, _template.getBasePDef(), target, null);
    }

    public int getPhysicalAttackRange() {
        final WeaponTemplate weaponItem = getActiveWeaponItem();
        if (weaponItem == null) {
            return (int) calcStat(Stats.POWER_ATTACK_RANGE, getTemplate().getBaseAtkRange(), null, null);
        }
        return (int) calcStat(Stats.POWER_ATTACK_RANGE, weaponItem.getAttackRange(), null, null);
    }

    @Deprecated
    public final int getRandomDamage() {
        final WeaponTemplate weaponItem = getActiveWeaponItem();
        if (weaponItem == null) {
            return getTemplate().getBaseRndDam();
        }
        return weaponItem.getRandomDamage();
    }

    public double getReuseModifier(final Creature target) {
        return calcStat(Stats.ATK_REUSE, 1.0, target, null);
    }

    public int getRunSpeed() {
        return getSpeed(_template.getBaseRunSpd());
    }

    public final int getShldDef() {
        if (isPlayer()) {
            return (int) calcStat(Stats.SHIELD_DEFENCE, 0.0, null, null);
        }
        return (int) calcStat(Stats.SHIELD_DEFENCE, _template.getBaseShldDef(), null, null);
    }

    public final int getSkillDisplayLevel(final Integer skillId) {
        final Skill skill = _skills.get(skillId);
        if (skill == null) {
            return -1;
        }
        return skill.getDisplayLevel();
    }

    public final int getSkillLevel(final Integer skillId) {
        return getSkillLevel(skillId, -1);
    }

    public final int getSkillLevel(final Integer skillId, final int def) {
        final Skill skill = _skills.get(skillId);
        if (skill == null) {
            return def;
        }
        return skill.getLevel();
    }

    public int getSkillMastery(final Integer skillId) {
        if (_skillMastery == null) {
            return 0;
        }
        final Integer val = _skillMastery.get(skillId);
        return (val == null) ? 0 : val;
    }

    public void removeSkillMastery(final Integer skillId) {
        if (_skillMastery != null) {
            _skillMastery.remove(skillId);
        }
    }

    public int getSpeed(final int baseSpeed) {
        if (isInWater()) {
            return getSwimSpeed();
        }
        return (int) calcStat(Stats.RUN_SPEED, baseSpeed, null, null);
    }

    public int getSTR() {
        return (int) calcStat(Stats.STAT_STR, _template.getBaseSTR(), null, null);
    }

    public int getSwimSpeed() {
        return (int) calcStat(Stats.RUN_SPEED, Config.SWIMING_SPEED, null, null);
    }

    public GameObject getTarget() {
        return target.get();
    }

    public void setTarget(GameObject object) {
        if (object != null && !object.isVisible()) {
            object = null;
        }
        if (object == null) {
            target = HardReferences.emptyRef();
        } else {
            target = object.getRef();
        }
    }

    public final int getTargetId() {
        final GameObject target = getTarget();
        return (target == null) ? -1 : target.getObjectId();
    }

    public CharTemplate getTemplate() {
        return _template;
    }

    public CharTemplate getBaseTemplate() {
        return _baseTemplate;
    }

    public String getTitle() {
        return StringUtils.defaultString(_title);
    }

    public void setTitle(final String title) {
        _title = title;
    }

    public final int getWalkSpeed() {
        if (isInWater()) {
            return getSwimSpeed();
        }
        return getSpeed(_template.getBaseWalkSpd());
    }

    public int getWIT() {
        return (int) calcStat(Stats.STAT_WIT, _template.getBaseWIT(), null, null);
    }

    public double headingToRadians(final int heading) {
        return (heading - 32768) / 10430.378350470453;
    }

    public boolean isAlikeDead() {
        return isDead();
    }

    public final boolean isAttackingNow() {
        return _attackEndTime > System.currentTimeMillis();
    }

    public final boolean isBlessedByNoblesse() {
        return _isBlessedByNoblesse;
    }

    public final boolean isSalvation() {
        return _isSalvation;
    }

    public boolean isEffectImmune() {
        return _effectImmunity.get();
    }

    public boolean isBuffImmune() {
        return _buffImmunity.get();
    }

    public boolean isDebuffImmune() {
        return _debuffImmunity.get();
    }

    public boolean isDead() {
        return _currentHp < 0.5 || isDead.get();
    }

    @Override
    public final boolean isFlying() {
        return _flying;
    }

    public final void setFlying(boolean mode) {
        _flying = mode;
        if (mode) {
            broadcastPacket(new ChangeMoveType(this, GroundPosition.air));
        } else {
            broadcastPacket(new ChangeMoveType(this, GroundPosition.earth));
        }
    }

    public final boolean isInCombat() {
        return System.currentTimeMillis() < _stanceEndTime;
    }

    public boolean isInvul() {
        return _isInvul;
    }

    public boolean isMageClass() {
        return getTemplate().getBaseMAtk() > 3;
    }

    public final boolean isRunning() {
        return _running;
    }

    public boolean isSkillDisabled(final Skill skill) {
        final TimeStamp sts = _skillReuses.get(skill.hashCode());
        if (sts == null) {
            return false;
        }
        if (sts.hasNotPassed()) {
            return true;
        }
        _skillReuses.remove(skill.hashCode());
        return false;
    }

    public final boolean isTeleporting() {
        return isTeleporting.get();
    }

    public Location getDestination() {
        if (moveAction instanceof MoveToLocationAction) {
            return moveAction.moveTo().clone();
        }
        return null;
    }

    public boolean isMoving() {
        final MoveActionBase theMoveActionBase = moveAction;
        return theMoveActionBase != null && !theMoveActionBase.isFinished();
    }

    public boolean isFollowing() {
        final MoveActionBase theMoveActionBase = moveAction;
        return theMoveActionBase != null && theMoveActionBase instanceof MoveToRelativeAction && !theMoveActionBase.isFinished();
    }

    public int maxZDiff() {
        final MoveActionBase theMoveActionBase = moveAction;
        if (theMoveActionBase != null) {
            final Location moveFrom = theMoveActionBase.moveFrom();
            final Location moveTo = theMoveActionBase.moveTo();
            if (moveFrom.getZ() > moveTo.getZ()) {
                return moveFrom.getZ() - moveTo.getZ();
            }
        }
        return Config.MAX_Z_DIFF;
    }

    public Creature getFollowTarget() {
        final MoveActionBase moveAction = this.moveAction;
        if (moveAction instanceof MoveToRelativeAction && !moveAction.isFinished()) {
            final MoveToRelativeAction mtra = (MoveToRelativeAction) moveAction;
            final GameObject target = mtra.getTarget();
            if (target instanceof Creature) {
                return (Creature) target;
            }
        }
        return null;
    }

    protected MoveActionBase createMoveToRelative(final GameObject pawn, final int indent, final int range, final boolean pathfinding) {
        return new MoveToRelativeAction(this, pawn, !Config.ALLOW_GEODATA, indent, range, pathfinding);
    }

    protected MoveActionBase createMoveToLocation(final Location dest, final int indent, final boolean pathFind) {
        return new MoveToLocationAction(this, getLoc(), dest, isInBoat() || isBoat() || !Config.ALLOW_GEODATA, indent, pathFind);
    }

    public boolean moveToLocation(final Location loc, final int offset, final boolean pathfinding) {
        return moveToLocation(loc.x, loc.y, loc.z, offset, pathfinding);
    }

    public boolean moveToLocation(final int toX, final int toY, final int toZ, int indent, final boolean pathfinding) {
        moveLock.lock();
        try {
            indent = Math.max(indent, 0);
            final Location worldTo = new Location(toX, toY, toZ);
            final MoveActionBase prevMoveAction = moveAction;
            if (prevMoveAction instanceof MoveToLocationAction && ((MoveToLocationAction) prevMoveAction).isSameDest(worldTo)) {
                sendActionFailed();
                return false;
            }
            if (isMovementDisabled()) {
                getAI().setNextAction(NextAction.MOVE, new Location(toX, toY, toZ), indent, pathfinding, false);
                sendActionFailed();
                return false;
            }
            getAI().clearNextAction();
            if (isPlayer()) {
                final Player player = getPlayer();
                getAI().changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
                if (Config.ALT_TELEPORT_PROTECTION && isPlayer() && player.getAfterTeleportPortectionTime() > System.currentTimeMillis()) {
                    player.setAfterTeleportPortectionTime(0L);
                    player.sendMessage(new CustomMessage("alt.teleport_protect_gonna", player));
                }
            }
            stopMove(false, false);
            final MoveActionBase mtla = createMoveToLocation(worldTo, indent, pathfinding);
            moveAction = mtla;
            if (!mtla.start()) {
                moveAction = null;
                sendActionFailed();
                return false;
            }
            mtla.scheduleNextTick();
            return true;
        } finally {
            moveLock.unlock();
        }
    }

    public boolean moveToRelative(final GameObject pawn, final int indent, final int range) {
        return moveToRelative(pawn, indent, range, Config.ALLOW_PAWN_PATHFIND);
    }

    public boolean moveToRelative(final GameObject pawn, int indent, int range, final boolean pathfinding) {
        moveLock.lock();
        try {
            if (isMovementDisabled() || pawn == null || isInBoat()) {
                return false;
            }
            final MoveActionBase prevMoveAction = moveAction;
            if (prevMoveAction instanceof MoveToRelativeAction && !prevMoveAction.isFinished() && ((MoveToRelativeAction) prevMoveAction).isSameTarget(pawn)) {
                sendActionFailed();
                return false;
            }
            range = Math.max(range, 10);
            indent = Math.min(indent, range);
            getAI().clearNextAction();
            if (isPlayer()) {
                final Player player = getPlayer();
                if (Config.ALT_TELEPORT_PROTECTION && isPlayer() && player.getAfterTeleportPortectionTime() > System.currentTimeMillis()) {
                    player.setAfterTeleportPortectionTime(0L);
                    player.sendMessage(new CustomMessage("alt.teleport_protect_gonna", player));
                }
            }
            stopMove(false, false);
            final MoveActionBase mtra = createMoveToRelative(pawn, indent, range, pathfinding);
            moveAction = mtra;
            if (!mtra.start()) {
                moveAction = null;
                sendActionFailed();
                return false;
            }
            mtra.scheduleNextTick();
            return true;
        } finally {
            moveLock.unlock();
        }
    }

    private void broadcastMove() {
        validateLocation(isPlayer() ? 2 : 1);
        broadcastPacket(movePacket());
    }

    public void stopMove() {
        stopMove(true, true);
    }

    public void stopMove(final boolean validate) {
        stopMove(true, validate);
    }

    public void stopMove(final boolean stop, final boolean validate) {
        stopMove(stop, validate, true);
    }

    public void stopMove(final boolean stop, final boolean validate, final boolean action) {
        if (!isMoving()) {
            return;
        }
        moveLock.lock();
        try {
            if (!isMoving()) {
                return;
            }
            if (action && moveAction != null && !moveAction.isFinished()) {
                moveAction.interrupt();
                moveAction = null;
            }
            if (_moveTask != null) {
                _moveTask.cancel(false);
                _moveTask = null;
            }
            if (validate) {
                validateLocation(isPlayer() ? 2 : 1);
            }
            if (stop) {
                broadcastPacket(stopMovePacket());
            }
        } finally {
            moveLock.unlock();
        }
    }

    /**
     * Возвращает координаты поверхности воды, если мы находимся в ней, или над ней.
     */
    public int getWaterZ() {
        if (!isInWater()) {
            broadcastPacket(new ChangeMoveType(this, GroundPosition.earth));
            return Integer.MIN_VALUE;
        }

        int waterZ = Integer.MIN_VALUE;
        zonesRead.lock();
        try {
            Zone zone;
            for (Zone _zone : _zones) {
                zone = _zone;
                if (zone.getType() == ZoneType.water) {
                    if (waterZ == Integer.MIN_VALUE || waterZ < zone.getTerritory().getZmax()) {
                        waterZ = zone.getTerritory().getZmax();
                    }
                }
            }
        } finally {
            zonesRead.unlock();
            broadcastPacket(new ChangeMoveType(this, GroundPosition.underwater));
        }

        return waterZ;
    }

    protected L2GameServerPacket stopMovePacket() {
        return new StopMove(this);
    }

    public L2GameServerPacket movePacket() {
        final MoveActionBase moveAction = this.moveAction;
        if (moveAction != null) {
            return moveAction.movePacket();
        }
        return new CharMoveToLocation(this);
    }

    public void updateZones() {
        Zone[] zones = isVisible() ? getCurrentRegion().getZones() : Zone.EMPTY_L2ZONE_ARRAY;

        List<Zone> entering = null;
        List<Zone> leaving = null;

        zonesWrite.lock();
        try {
            if (!_zones.isEmpty()) {
                leaving = new ArrayList<>(_zones.size());
                for (Zone _zone : _zones) {
                    // зоны больше нет в регионе, либо вышли за территорию зоны
                    if (!ArrayUtils.contains(zones, _zone) || !_zone.checkIfInZone(getX(), getY(), getZ(), getReflection())) {
                        leaving.add(_zone);
                    }
                }

                //Покинули зоны, убираем из списка зон персонажа
                if (!leaving.isEmpty()) {
                    leaving.forEach(_zones::remove);
                }
            }

            if (zones.length > 0) {
                entering = new ArrayList<>(zones.length);
                for (Zone zone1 : zones) {
                    // в зону еще не заходили и зашли на территорию зоны
                    if (!_zones.contains(zone1) && zone1.checkIfInZone(getX(), getY(), getZ(), getReflection())) {
                        entering.add(zone1);
                    }
                }

                //Вошли в зоны, добавим в список зон персонажа
                if (!entering.isEmpty()) {
                    _zones.addAll(new ArrayList<>(entering));
                }
            }
        } finally {
            zonesWrite.unlock();
        }

        onUpdateZones(leaving, entering);

        if (leaving != null) {
            leaving.clear();
        }

        if (entering != null) {
            entering.clear();
        }

    }

    protected void onUpdateZones(List<Zone> leaving, List<Zone> entering) {
        Zone zone;

        if (leaving != null && !leaving.isEmpty()) {
            for (Zone aLeaving : leaving) {
                zone = aLeaving;
                zone.doLeave(this);
            }
        }

        if (entering != null && !entering.isEmpty()) {
            for (Zone anEntering : entering) {
                zone = anEntering;
                zone.doEnter(this);
            }
        }
    }

    public boolean isInZonePeace() {
        return isInZone(ZoneType.peace_zone) && !isInZoneBattle();
    }

    public boolean isInZoneBattle() {
        return isInZone(ZoneType.battle_zone);
    }

    public boolean isInWater() {
        return isInZone(ZoneType.water) && !isInBoat() && !isBoat() && !isFlying();
    }


    public boolean isInZone(ZoneType type) {
        zonesRead.lock();
        try {
            Zone zone;
            for (Zone _zone : _zones) {
                zone = _zone;
                if (zone.getType() == type) {
                    return true;
                }
            }
        } finally {
            zonesRead.unlock();
        }

        return false;
    }

    public boolean isInZone(String name) {
        zonesRead.lock();
        try {
            Zone zone;
            for (Zone _zone : _zones) {
                zone = _zone;
                if (zone.getName().equals(name)) {
                    return true;
                }
            }
        } finally {
            zonesRead.unlock();
        }

        return false;
    }

    public boolean isInZone(Zone zone) {
        zonesRead.lock();
        try {
            return _zones.contains(zone);
        } finally {
            zonesRead.unlock();
        }
    }

    public Zone getZone(ZoneType type) {
        zonesRead.lock();
        try {
            Zone zone;
            for (Zone _zone : _zones) {
                zone = _zone;
                if (zone.getType() == type) {
                    return zone;
                }
            }
        } finally {
            zonesRead.unlock();
        }
        return null;
    }

    public String getZonesNames() {
            String[] zone_name = new String[_zones.size()];
            for (Zone _zone : _zones) {
                if(_zone.getName() != null && !_zone.getName().isEmpty()) {
                    zone_name = ArrayUtils.add(zone_name, _zone.getName());
                }
            }
            return Arrays.toString(zone_name);
    }

    public Location getRestartPoint() {
        zonesRead.lock();
        try {
            Zone zone;
            for (Zone _zone : _zones) {
                zone = _zone;
                if (zone.getRestartPoints() != null) {
                    ZoneType type = zone.getType();
                    if (type == ZoneType.battle_zone || type == ZoneType.peace_zone || type == ZoneType.offshore || type == ZoneType.dummy) {
                        return zone.getSpawn();
                    }
                }
            }
        } finally {
            zonesRead.unlock();
        }

        return null;
    }

    public Location getPKRestartPoint() {
        zonesRead.lock();
        try {
            Zone zone;
            for (Zone _zone : _zones) {
                zone = _zone;
                if (zone.getRestartPoints() != null) {
                    ZoneType type = zone.getType();
                    if (type == ZoneType.battle_zone || type == ZoneType.peace_zone || type == ZoneType.offshore || type == ZoneType.dummy) {
                        return zone.getPKSpawn();
                    }
                }
            }
        } finally {
            zonesRead.unlock();
        }

        return null;
    }

    @Override
    public int getGeoZ(final Location loc) {
        if (isFlying() || isInWater() || isInBoat() || isBoat() || isDoor()) {
            return loc.z;
        }
        return super.getGeoZ(loc);
    }

    protected boolean needStatusUpdate() {
        if (!isVisible()) {
            return false;
        }
        boolean result = false;
        int bar = (int) (getCurrentHp() * 352.0 / getMaxHp());
        if (bar == 0 || bar != _lastHpBarUpdate) {
            _lastHpBarUpdate = bar;
            result = true;
        }
        bar = (int) (getCurrentMp() * 352.0 / getMaxMp());
        if (bar == 0 || bar != _lastMpBarUpdate) {
            _lastMpBarUpdate = bar;
            result = true;
        }
        if (isPlayer()) {
            bar = (int) (getCurrentCp() * 352.0 / getMaxCp());
            if (bar == 0 || bar != _lastCpBarUpdate) {
                _lastCpBarUpdate = bar;
                result = true;
            }
        }
        return result;
    }

    @Override
    public void onForcedAttack(final Player player, final boolean shift) {
        player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
        if (!isAttackable(player) || player.isConfused() || player.isBlocked()) {
            player.sendActionFailed();
            return;
        }
        player.getAI().Attack(this, true, shift);
    }

    public void onHitTimer(final Creature target, final int damage, final boolean crit, final boolean miss, final boolean soulshot, final boolean shld, final boolean unchargeSS) {
        if (isAlikeDead()) {
            sendActionFailed();
            return;
        }
        if (target.isDead() || !isInRange(target, 2000L)) {
            sendActionFailed();
            return;
        }
        if (isPlayable() && target.isPlayable() && isInZoneBattle() != target.isInZoneBattle()) {
            final Player player = getPlayer();
            if (player != null) {
                player.sendPacket(Msg.INVALID_TARGET);
                player.sendActionFailed();
            }
            return;
        }
        target.getListeners().onCreatureAttack(this);
        if (!miss && target.isPlayer() && (isCursedWeaponEquipped() || (getActiveWeaponInstance() != null && getActiveWeaponInstance().isHeroWeapon() && target.isCursedWeaponEquipped()))) {
            target.setCurrentCp(0.0);
        }
        if (target.isStunned() && Formulas.calcStunBreak(this, target, crit)) {
            target.getEffectList().stopEffects(EffectType.Stun);
        }
        displayGiveDamageMessage(target, damage, crit, miss, shld, false);
        ThreadPoolManager.getInstance().execute(new NotifyAITask(target, CtrlEvent.EVT_ATTACKED, this, damage));
        final boolean checkPvP = checkPvP(target, null);
        if (!miss && damage > 0) {
            target.getListeners().onCreatureAttacked(this);
            target.reduceCurrentHp(damage, this, null, true, true, false, true, false, false, true);
            if (!target.isDead()) {
                if (crit) {
                    useTriggers(target, TriggerType.CRIT, null, null, damage);
                }
                useTriggers(target, TriggerType.ATTACK, null, null, damage);
                if (Formulas.calcCastBreak(this, target, damage)) {
                    target.abortCast(false, true);
                }
            }
            if (soulshot && unchargeSS) {
                unChargeShots(false);
            }
        }
        if (miss) {
            target.useTriggers(this, TriggerType.UNDER_MISSED_ATTACK, null, null, damage);
        }
        startAttackStanceTask();
        if (checkPvP) {
            startPvPFlag(target);
        }
    }

    public void onMagicUseTimer(final Creature aimingTarget, final Skill skill, boolean forceUse) {
        _castInterruptTime = 0L;
        if (skill.isUsingWhileCasting()) {
            aimingTarget.getEffectList().stopEffect(skill.getId());
            onCastEndTime();
            return;
        }
        if (!skill.isOffensive() && getAggressionTarget() != null) {
            forceUse = true;
        }
        if (!skill.checkCondition(this, aimingTarget, forceUse, false, false)) {
            if (skill.getSkillType() == SkillType.PET_SUMMON && isPlayer()) {
                getPlayer().setPetControlItem(null);
            }
            onCastEndTime();
            return;
        }
        final List<Creature> targets = skill.getTargets(this, aimingTarget, forceUse);
        final int hpConsume = skill.getHpConsume();
        if (hpConsume > 0) {
            setCurrentHp(Math.max(0.0, _currentHp - hpConsume), false);
        }
        double mpConsume2 = skill.getMpConsume2();
        if (mpConsume2 > 0.0) {
            if (skill.isMusic()) {
                mpConsume2 += getEffectList().getActiveMusicCount(skill.getId()) * mpConsume2 / 2.0;
                mpConsume2 = calcStat(Stats.MP_DANCE_SKILL_CONSUME, mpConsume2, aimingTarget, skill);
            } else if (skill.isMagic()) {
                mpConsume2 = calcStat(Stats.MP_MAGIC_SKILL_CONSUME, mpConsume2, aimingTarget, skill);
            } else {
                mpConsume2 = calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, mpConsume2, aimingTarget, skill);
            }
            if (_currentMp < mpConsume2 && isPlayable()) {
                sendPacket(Msg.NOT_ENOUGH_MP);
                onCastEndTime();
                return;
            }
            reduceCurrentMp(mpConsume2, null);
        }
        callSkill(skill, targets, true);
        if (skill.getNumCharges() > 0) {
            setIncreasedForce(getIncreasedForce() - skill.getNumCharges());
        }
        switch (skill.getFlyType()) {
            case THROW_UP:
            case THROW_HORIZONTAL: {
                for (final Creature target : targets) {
                    final Location flyLoc = getFlyLocation(null, skill);
                    target.setLoc(flyLoc);
                    broadcastPacket(new FlyToLocation(target, flyLoc, skill.getFlyType()));
                }
                break;
            }
        }
        final int skillCoolTime = Formulas.calcMAtkSpd(this, skill, skill.getCoolTime());
        if (skillCoolTime > 0) {
            ThreadPoolManager.getInstance().schedule(new CastEndTimeTask(this), skillCoolTime);
        } else {
            onCastEndTime();
        }
    }

    public void onCastEndTime() {
        finishFly();
        clearCastVars();
        getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING, null, null);
    }

    private void finishFly() {
        final Location flyLoc = _flyLoc;
        _flyLoc = null;
        if (flyLoc != null) {
            setLoc(flyLoc);
            validateLocation(1);
        }
    }

    public void reduceCurrentHp(double damage, final Creature attacker, final Skill skill, final boolean awake, final boolean standUp, final boolean directHp, final boolean canReflect, final boolean transferDamage, final boolean isDot, final boolean sendMessage) {
        if (attacker == null || isDead() || (attacker.isDead() && !isDot)) {
            return;
        }
        if (isDamageBlocked() && transferDamage) {
            return;
        }
        if (isDamageBlocked() && attacker != this) {
            if (sendMessage) {
                attacker.sendPacket(Msg.THE_ATTACK_HAS_BEEN_BLOCKED);
            }
            return;
        }
        if (canReflect) {
            if (attacker.absorbAndReflect(this, skill, damage, sendMessage)) {
                return;
            }
            damage = absorbToEffector(attacker, damage);
            damage = absorbToSummon(attacker, damage);
        }
        getListeners().onCurrentHpDamage(damage, attacker, skill);
        if (attacker != this) {
            if (sendMessage) {
                displayReceiveDamageMessage(attacker, (int) damage);
            }
            if (!isDot) {
                useTriggers(attacker, TriggerType.RECEIVE_DAMAGE, null, null, damage);
            }
        }
        onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
    }

    protected void onReduceCurrentHp(final double damage, final Creature attacker, final Skill skill, final boolean awake, final boolean standUp, final boolean directHp) {
        if (awake && isSleeping()) {
            getEffectList().stopEffects(EffectType.Sleep);
        }
        if (attacker != this || (skill != null && skill.isOffensive())) {
            if (isMeditated()) {
                final Effect effect = getEffectList().getEffectByType(EffectType.Meditation);
                if (effect != null) {
                    getEffectList().stopEffect(effect.getSkill());
                }
            }
            startAttackStanceTask();
            checkAndRemoveInvisible();
            if (getCurrentHp() - damage < 0.5) {
                useTriggers(attacker, TriggerType.DIE, null, null, damage);
            }
        }
        if (isPlayer() && getPlayer().isGM() && getPlayer().isUndying() && damage + 0.5 >= getCurrentHp()) {
            return;
        }
        setCurrentHp(Math.max(getCurrentHp() - damage, 0.0), false);
        if (getCurrentHp() < 0.5) {
            doDie(attacker);
        }
    }

    public void reduceCurrentMp(final double i, final Creature attacker) {
        reduceCurrentMp(i, attacker, false);
    }

    public void reduceCurrentMp(double i, final Creature attacker, final boolean sendMessage) {
        if (attacker != null && attacker != this) {
            if (isSleeping()) {
                getEffectList().stopEffects(EffectType.Sleep);
            }
            if (isMeditated()) {
                final Effect effect = getEffectList().getEffectByType(EffectType.Meditation);
                if (effect != null) {
                    getEffectList().stopEffect(effect.getSkill());
                }
            }
        }
        if (isDamageBlocked() && attacker != null && attacker != this) {
            attacker.sendPacket(Msg.THE_ATTACK_HAS_BEEN_BLOCKED);
            return;
        }
        if (attacker != null && attacker.isPlayer() && Math.abs(attacker.getLevel() - getLevel()) > 10) {
            if (attacker.getKarma() > 0 && getEffectList().getEffectsBySkillId(5182) != null && !isInZone(ZoneType.SIEGE)) {
                return;
            }
            if (getKarma() > 0 && attacker.getEffectList().getEffectsBySkillId(5182) != null && !attacker.isInZone(ZoneType.SIEGE)) {
                return;
            }
        }
        getListeners().onCurrentMpReduce(i, attacker);
        if (sendMessage) {
            final int msgMp = (int) Math.min(_currentMp, i);
            sendPacket(new SystemMessage(1866).addNumber(msgMp));
            if (attacker != null && attacker.isPlayer()) {
                attacker.sendPacket(new SystemMessage(1867).addNumber(msgMp));
            }
        }
        i = Math.max(0.0, _currentMp - i);
        setCurrentMp(i);
        if (attacker != null && attacker != this) {
            startAttackStanceTask();
        }
    }

    public double relativeSpeed(final GameObject target) {
        return getMoveSpeed() - target.getMoveSpeed() * Math.cos(headingToRadians(getHeading()) - headingToRadians(target.getHeading()));
    }

    public void removeAllSkills() {
        for (final Skill s : getAllSkillsArray()) {
            removeSkill(s);
        }
    }

    public void removeBlockStats(final List<Stats> stats) {
        if (_blockedStats != null) {
            _blockedStats.removeAll(stats);
            if (_blockedStats.isEmpty()) {
                _blockedStats = null;
            }
        }
    }

    public Skill removeSkill(final Skill skill) {
        if (skill == null) {
            return null;
        }
        return removeSkillById(skill.getId());
    }

    public Skill removeSkillById(final int id) {
        final Skill oldSkill = _skills.remove(id);
        if (oldSkill != null) {
            removeTriggers(oldSkill);
            removeStatsOwner(oldSkill);
            if (Config.ALT_DELETE_SA_BUFFS && (oldSkill.isItemSkill() || oldSkill.isHandler())) {
                List<Effect> effects = getEffectList().getEffectsBySkill(oldSkill);
                if (effects != null) {
                    effects.forEach(Effect::exit);
                }
                final Summon pet = getPet();
                if (pet != null) {
                    effects = pet.getEffectList().getEffectsBySkill(oldSkill);
                    if (effects != null) {
                        effects.forEach(Effect::exit);
                    }
                }
            }
        }
        return oldSkill;
    }

    public void addTriggers(final StatTemplate f) {
        if (f.getTriggerList().isEmpty()) {
            return;
        }
        for (final TriggerInfo t : f.getTriggerList()) {
            addTrigger(t);
        }
    }

    public void addTrigger(final TriggerInfo t) {
        if (_triggers == null) {
            _triggers = new ConcurrentHashMap<>();
        }
        Set<TriggerInfo> hs = _triggers.computeIfAbsent(t.getType(), k -> new CopyOnWriteArraySet<>());
        hs.add(t);
        if (t.getType() == TriggerType.ADD) {
            useTriggerSkill(this, null, t, null, 0.0);
        }
    }

    public void removeTriggers(final StatTemplate f) {
        if (_triggers == null || f.getTriggerList().isEmpty()) {
            return;
        }
        for (final TriggerInfo t : f.getTriggerList()) {
            removeTrigger(t);
        }
    }

    public void removeTrigger(final TriggerInfo t) {
        if (_triggers == null) {
            return;
        }
        final Set<TriggerInfo> hs = _triggers.get(t.getType());
        if (hs == null) {
            return;
        }
        hs.remove(t);
    }

    public void sendActionFailed() {
        sendPacket(ActionFail.STATIC);
    }

    public boolean hasAI() {
        return _ai != null;
    }

    public CharacterAI getAI() {
        if (_ai == null) {
            synchronized (this) {
                if (_ai == null) {
                    _ai = new CharacterAI(this);
                }
            }
        }
        return _ai;
    }

    public void setAI(final CharacterAI newAI) {
        if (newAI == null) {
            return;
        }
        final CharacterAI oldAI = _ai;
        synchronized (this) {
            _ai = newAI;
        }
        if (oldAI != null && oldAI.isActive()) {
            oldAI.stopAITask();
            newAI.startAITask();
            newAI.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
        }
    }

    public final void setCurrentHp(double newHp, final boolean canRessurect, final boolean sendInfo) {
        final int maxHp = getMaxHp();
        newHp = Math.min(maxHp, Math.max(0.0, newHp));
        if (_currentHp == newHp) {
            return;
        }
        if (newHp >= 0.5 && isDead() && !canRessurect) {
            return;
        }
        final double hpStart = _currentHp;
        _currentHp = newHp;
        if (isDead.compareAndSet(true, false)) {
            onRevive();
        }
        checkHpMessages(hpStart, _currentHp);
        if (sendInfo) {
            broadcastStatusUpdate();
            sendChanges();
        }
        if (_currentHp < maxHp) {
            startRegeneration();
        }
    }

    public final void setCurrentHp(final double newHp, final boolean canRessurect) {
        setCurrentHp(newHp, canRessurect, true);
    }

    public final void setCurrentMp(double newMp, final boolean sendInfo) {
        final int maxMp = getMaxMp();
        newMp = Math.min(maxMp, Math.max(0.0, newMp));
        if (_currentMp == newMp) {
            return;
        }
        if (newMp >= 0.5 && isDead()) {
            return;
        }
        _currentMp = newMp;
        if (sendInfo) {
            broadcastStatusUpdate();
            sendChanges();
        }
        if (_currentMp < maxMp) {
            startRegeneration();
        }
    }

    public final void setCurrentCp(double newCp, final boolean sendInfo) {
        if (!isPlayer()) {
            return;
        }
        final int maxCp = getMaxCp();
        newCp = Math.min(maxCp, Math.max(0.0, newCp));
        if (_currentCp == newCp) {
            return;
        }
        if (newCp >= 0.5 && isDead()) {
            return;
        }
        _currentCp = newCp;
        if (sendInfo) {
            broadcastStatusUpdate();
            sendChanges();
        }
        if (_currentCp < maxCp) {
            startRegeneration();
        }
    }

    public void setCurrentHpMp(double newHp, double newMp, final boolean canRessurect) {
        final int maxHp = getMaxHp();
        final int maxMp = getMaxMp();
        newHp = Math.min(maxHp, Math.max(0.0, newHp));
        newMp = Math.min(maxMp, Math.max(0.0, newMp));
        if (_currentHp == newHp && _currentMp == newMp) {
            return;
        }
        if (newHp >= 0.5 && isDead() && !canRessurect) {
            return;
        }
        final double hpStart = _currentHp;
        _currentHp = newHp;
        _currentMp = newMp;
        if (isDead.compareAndSet(true, false)) {
            onRevive();
        }
        checkHpMessages(hpStart, _currentHp);
        broadcastStatusUpdate();
        sendChanges();
        if (_currentHp < maxHp || _currentMp < maxMp) {
            startRegeneration();
        }
    }

    public void setCurrentHpMp(final double newHp, final double newMp) {
        setCurrentHpMp(newHp, newMp, false);
    }

    @Override
    public final int getHeading() {
        return _heading;
    }

    public void setHeading(final int heading) {
        _heading = heading;
    }

    public final void setIsTeleporting(final boolean value) {
        isTeleporting.compareAndSet(!value, value);
    }

    public Creature getCastingTarget() {
        return castingTarget.get();
    }

    public void setCastingTarget(final Creature target) {
        if (target == null) {
            castingTarget = HardReferences.emptyRef();
        } else {
            castingTarget = target.getRef();
        }
    }

    public final void setRunning() {
        if (!_running) {
            _running = true;
            broadcastPacket(new ChangeMoveType(this, GroundPosition.earth));
        }
    }

    public final void setIsRunning(boolean value) {

        _running = value;
        if (getMoveSpeed() != 0) {
            broadcastPacket(new ChangeMoveType(this, GroundPosition.earth));
        }

        if (isPlayer()) {
            getPlayer().broadcastUserInfo(true);
        } else if (isSummon()) {
            broadcastStatusUpdate();
        } else if (isNpc()) {
            World.getAroundPlayers(this).forEach(player -> {
                if (isPolymorphedNpc()) {
                    player.sendPacket(new PolyMorphedNpcInfo((NpcInstance) this));
                } else if (getMoveSpeed() == 0) {
                    player.sendPacket(new ServerObjectInfo((NpcInstance) this, player));
                } else {
                    player.sendPacket(new NpcInfo((NpcInstance) this, player));
                }
            });
        }
    }

    public void setSkillMastery(final Integer skill, final int mastery) {
        if (_skillMastery == null) {
            _skillMastery = new HashMap<>();
        }
        _skillMastery.put(skill, mastery);
    }

    public Creature getAggressionTarget() {
        return _aggressionTarget.get();
    }

    public void setAggressionTarget(final Creature target) {
        if (target == null) {
            _aggressionTarget = HardReferences.emptyRef();
        } else {
            _aggressionTarget = target.getRef();
        }
    }

    public void setWalking() {
        if (_running) {
            _running = false;
            broadcastPacket(new ChangeMoveType(this, GroundPosition.earth));
        }
    }

    public void startAbnormalEffect(final AbnormalEffect ae) {
        if (ae == AbnormalEffect.NULL) {
            _abnormalEffects = AbnormalEffect.NULL.getMask();
            _abnormalEffects2 = AbnormalEffect.NULL.getMask();
            _abnormalEffects3 = AbnormalEffect.NULL.getMask();
        } else if (ae.isSpecial()) {
            _abnormalEffects2 |= ae.getMask();
        } else if (ae.isEvent()) {
            _abnormalEffects3 |= ae.getMask();
        } else {
            _abnormalEffects |= ae.getMask();
        }
        sendChanges();
    }

    public void startAttackStanceTask() {
        startAttackStanceTask0();
    }

    protected void startAttackStanceTask0() {
        if (isInCombat()) {
            _stanceEndTime = System.currentTimeMillis() + 15000L;
            return;
        }
        _stanceEndTime = System.currentTimeMillis() + 15000L;
        broadcastPacket(new AutoAttackStart(getObjectId()));
        final Future<?> task = _stanceTask;
        if (task != null) {
            task.cancel(false);
        }
        _stanceTask = LazyPrecisionTaskManager.getInstance().scheduleAtFixedRate((_stanceTaskRunnable == null) ? (_stanceTaskRunnable = new AttackStanceTask()) : _stanceTaskRunnable, 1000L, 1000L);
    }

    public void stopAttackStanceTask() {
        _stanceEndTime = 0L;
        final Future<?> task = _stanceTask;
        if (task != null) {
            task.cancel(false);
            _stanceTask = null;
            broadcastPacket(new AutoAttackStop(getObjectId()));
        }
    }

    protected void stopRegeneration() {
        regenLock.lock();
        try {
            if (_isRegenerating) {
                _isRegenerating = false;
                if (_regenTask != null) {
                    _regenTask.cancel(false);
                    _regenTask = null;
                }
            }
        } finally {
            regenLock.unlock();
        }
    }

    protected void startRegeneration() {
        if (!isVisible() || isDead() || getRegenTick() == 0L) {
            return;
        }
        if (_isRegenerating) {
            return;
        }
        regenLock.lock();
        try {
            if (!_isRegenerating) {
                _isRegenerating = true;
                _regenTask = RegenTaskManager.getInstance().scheduleAtFixedRate((_regenTaskRunnable == null) ? (_regenTaskRunnable = new RegenTask()) : _regenTaskRunnable, 0L, getRegenTick());
            }
        } finally {
            regenLock.unlock();
        }
    }

    public long getRegenTick() {
        return 3000L;
    }

    public void stopAbnormalEffect(final AbnormalEffect ae) {
        if (ae.isSpecial()) {
            _abnormalEffects2 &= ~ae.getMask();
        }
        if (ae.isEvent()) {
            _abnormalEffects3 &= ~ae.getMask();
        } else {
            _abnormalEffects &= ~ae.getMask();
        }
        sendChanges();
    }

    public void block() {
        _blocked = true;
    }

    public void unblock() {
        _blocked = false;
    }

    public boolean startConfused() {
        return _confused.getAndSet(true);
    }

    public boolean stopConfused() {
        return _confused.setAndGet(false);
    }

    public boolean startFear() {
        return _afraid.getAndSet(true);
    }

    public boolean stopFear() {
        return _afraid.setAndGet(false);
    }

    public boolean startMuted() {
        return _muted.getAndSet(true);
    }

    public boolean stopMuted() {
        return _muted.setAndGet(false);
    }

    public boolean startPMuted() {
        return _pmuted.getAndSet(true);
    }

    public boolean stopPMuted() {
        return _pmuted.setAndGet(false);
    }

    public boolean startAMuted() {
        return _amuted.getAndSet(true);
    }

    public boolean stopAMuted() {
        return _amuted.setAndGet(false);
    }

    public boolean startRooted() {
        return _rooted.getAndSet(true);
    }

    public boolean stopRooted() {
        return _rooted.setAndGet(false);
    }

    public boolean startSleeping() {
        return _sleeping.getAndSet(true);
    }

    public boolean stopSleeping() {
        return _sleeping.setAndGet(false);
    }

    public boolean startStunning() {
        return _stunned.getAndSet(true);
    }

    public boolean stopStunning() {
        return _stunned.setAndGet(false);
    }

    public boolean startParalyzed() {
        return _paralyzed.getAndSet(true);
    }

    public boolean stopParalyzed() {
        return _paralyzed.setAndGet(false);
    }

    public boolean startImmobilized() {
        return _immobilized.getAndSet(true);
    }

    public boolean stopImmobilized() {
        return _immobilized.setAndGet(false);
    }

    public boolean startHealBlocked() {
        return _healBlocked.getAndSet(true);
    }

    public boolean stopHealBlocked() {
        return _healBlocked.setAndGet(false);
    }

    public boolean startDamageBlocked() {
        return _damageBlocked.getAndSet(true);
    }

    public boolean stopDamageBlocked() {
        return _damageBlocked.setAndGet(false);
    }

    public boolean startBuffImmunity() {
        return _buffImmunity.getAndSet(true);
    }

    public boolean stopBuffImmunity() {
        return _buffImmunity.setAndGet(false);
    }

    public boolean startDebuffImmunity() {
        return _debuffImmunity.getAndSet(true);
    }

    public boolean stopDebuffImmunity() {
        return _debuffImmunity.setAndGet(false);
    }

    public boolean startEffectImmunity() {
        return _effectImmunity.getAndSet(true);
    }

    public boolean stopEffectImmunity() {
        return _effectImmunity.setAndGet(false);
    }

    public boolean startWeaponEquipBlocked() {
        return _weaponEquipBlocked.getAndSet(true);
    }

    public boolean stopWeaponEquipBlocked() {
        return _weaponEquipBlocked.getAndSet(false);
    }

    public boolean startFrozen() {
        return _frozen.getAndSet(true);
    }

    public boolean stopFrozen() {
        return _frozen.setAndGet(false);
    }

    public final void setIsBlessedByNoblesse(final boolean value) {
        _isBlessedByNoblesse = value;
    }

    public final void setIsSalvation(final boolean value) {
        _isSalvation = value;
    }

    public void setIsInvul(final boolean value) {
        _isInvul = value;
    }

    public boolean isConfused() {
        return _confused.get();
    }

    public boolean isAfraid() {
        return _afraid.get();
    }

    public boolean isBlocked() {
        return _blocked;
    }

    public boolean isMuted(final Skill skill) {
        return skill != null && !skill.isNotAffectedByMute() && ((isMMuted() && skill.isMagic()) || (isPMuted() && !skill.isMagic()));
    }

    public boolean isPMuted() {
        return _pmuted.get();
    }

    public boolean isMMuted() {
        return _muted.get();
    }

    public boolean isAMuted() {
        return _amuted.get();
    }

    public boolean isRooted() {
        return _rooted.get();
    }

    public boolean isSleeping() {
        return _sleeping.get();
    }

    public boolean isStunned() {
        return _stunned.get();
    }

    public boolean isMeditated() {
        return _meditated;
    }

    public void setMeditated(final boolean value) {
        _meditated = value;
    }

    public boolean isWeaponEquipBlocked() {
        return _weaponEquipBlocked.get();
    }

    public boolean isParalyzed() {
        return _paralyzed.get();
    }

    public boolean isFrozen() {
        return _frozen.get();
    }

    public boolean isImmobilized() {
        return _immobilized.get() || getRunSpeed() < 1;
    }

    public boolean isHealBlocked() {
        return isAlikeDead() || _healBlocked.get();
    }

    public boolean isDamageBlocked() {
        return isInvul() || _damageBlocked.get();
    }

    public boolean isCastingNow() {
        return _skillTask != null;
    }

    public boolean isLockedTarget() {
        return _lockedTarget;
    }

    public void setLockedTarget(final boolean value) {
        _lockedTarget = value;
    }

    public boolean isMovementDisabled() {
        return isBlocked() || isRooted() || isImmobilized() || isAlikeDead() || isStunned() || isSleeping() || isParalyzed() || isAttackingNow() || isCastingNow() || isFrozen();
    }

    public boolean isActionsDisabled() {
        return isBlocked() || isAlikeDead() || isStunned() || isSleeping() || isParalyzed() || isAttackingNow() || isCastingNow() || isFrozen();
    }

    public boolean isPotionsDisabled() {
        return isActionsDisabled() || isStunned() || isSleeping() || isParalyzed() || isAlikeDead() || isAfraid();
    }

    public final boolean isAttackingDisabled() {
        return _attackReuseEndTime > System.currentTimeMillis();
    }

    public boolean isOutOfControl() {
        return isBlocked() || isConfused() || isAfraid() || isFrozen();
    }

    public void teleToLocation(final Location loc) {
        teleToLocation(loc.x, loc.y, loc.z, getReflection());
    }

    public void teleToLocation(final Location loc, final int refId) {
        teleToLocation(loc.x, loc.y, loc.z, refId);
    }

    public void teleToLocation(final Location loc, final Reflection r) {
        teleToLocation(loc.x, loc.y, loc.z, r);
    }

    public void teleToLocation(final int x, final int y, final int z) {
        teleToLocation(x, y, z, getReflection());
    }

    public void checkAndRemoveInvisible() {
        final InvisibleType invisibleType = getInvisibleType();
        if (invisibleType == InvisibleType.EFFECT) {
            getEffectList().stopEffects(EffectType.Invisible);
        }
    }

    public void teleToLocation(final int x, final int y, final int z, final int refId) {
        final Reflection r = ReflectionManager.getInstance().get(refId);
        if (r == null) {
            return;
        }
        teleToLocation(x, y, z, r);
    }

    public void teleToLocation(int x, int y, int z, final Reflection r) {
        if (!isTeleporting.compareAndSet(false, true)) {
            return;
        }
        abortCast(true, false);
        if (!isLockedTarget()) {
            setTarget(null);
        }
        stopMove(true, true, false);
        if (!isBoat() && !isFlying() && !World.isWater(new Location(x, y, z), r)) {
            z = GeoEngine.getHeight(x, y, z, r.getGeoIndex());
        }
        if (isPlayer() && DimensionalRiftManager.getInstance().checkIfInRiftZone(getLoc(), true)) {
            final Player player = (Player) this;
            if (player.isInParty() && player.getParty().isInDimensionalRift()) {
                final Location newCoords = DimensionalRiftManager.getInstance().getRoom(0, 0).getTeleportCoords();
                x = newCoords.x;
                y = newCoords.y;
                z = newCoords.z;
                player.getParty().getDimensionalRift().usedTeleport(player);
            }
        }
        if (isPlayer()) {
            final Player player = (Player) this;
            player.getListeners().onTeleport(x, y, z, r);
            decayMe();
            setXYZ(x, y, z);
            setReflection(r);
            player.setLastClientPosition(null);
            player.setLastServerPosition(null);
            player.sendPacket(new TeleportToLocation(player, x, y, z));
        } else {
            setXYZ(x, y, z);
            setReflection(r);
            broadcastPacket(new TeleportToLocation(this, x, y, z));
            onTeleported();
        }
    }

    public boolean onTeleported() {
        return isTeleporting.compareAndSet(true, false);
    }

    public void sendMessage(final CustomMessage message) {
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + getObjectId() + "]";
    }

    @Override
    public double getColRadius() {
        return getTemplate().getCollisionRadius();
    }

    @Override
    public double getColHeight() {
        return getTemplate().getCollisionHeight();
    }

    public EffectList getEffectList() {
        if (_effectList == null) {
            synchronized (this) {
                if (_effectList == null) {
                    _effectList = new EffectList(this);
                }
            }
        }
        return _effectList;
    }

    public boolean paralizeOnAttack(final Creature attacker) {
        int max_attacker_level = 65535;
        final MonsterInstance leader;
        if (isRaid() || (isMinion() && (leader = ((MinionInstance) this).getLeader()) != null && leader.isRaid())) {
            max_attacker_level = getLevel() + Config.RAID_MAX_LEVEL_DIFF;
        } else if (isNpc()) {
            final int max_level_diff = ((NpcInstance) this).getParameter("ParalizeOnAttack", -1000);
            if (max_level_diff != -1000) {
                max_attacker_level = getLevel() + max_level_diff;
            }
        }
        return attacker.getLevel() > max_attacker_level;
    }

    @Override
    protected void onDelete() {
        GameObjectsStorage.remove(this);
        getEffectList().stopAllEffects();
        super.onDelete();
    }

    public void addExpAndSp(final long exp, final long sp) {
    }

    public void broadcastCharInfo() {
    }

    public void checkHpMessages(final double currentHp, final double newHp) {
    }

    public boolean checkPvP(final Creature target, final Skill skill) {
        return false;
    }

    public boolean consumeItem(final int itemConsumeId, final long itemCount) {
        return true;
    }

    public boolean consumeItemMp(final int itemId, final int mp) {
        return true;
    }

    public boolean isFearImmune() {
        return false;
    }

    public boolean isLethalImmune() {
        return getMaxHp() >= 50000;
    }

    public boolean getChargedSoulShot() {
        return false;
    }

    public int getChargedSpiritShot() {
        return 0;
    }

    public int getIncreasedForce() {
        return 0;
    }

    public void setIncreasedForce(final int i) {
    }

    public int getAgathionEnergy() {
        return 0;
    }

    public void setAgathionEnergy(final int val) {
    }

    public int getKarma() {
        return 0;
    }

    public double getLevelMod() {
        return 1.0;
    }

    public int getNpcId() {
        return 0;
    }

    public Summon getPet() {
        return null;
    }

    public int getPvpFlag() {
        return 0;
    }

    public TeamType getTeam() {
        return _team;
    }

    public void setTeam(final TeamType t) {
        _team = t;
        sendChanges();
    }

    public void setTeam(final TeamType t, final boolean checksForTeam) {
        _checksForTeam = checksForTeam;
        if (_team == t) {
            return;
        }
        _team = t;
        sendChanges();
    }

    public boolean isChecksForTeam() {
        return _checksForTeam;
    }

    public boolean isUndead() {
        return false;
    }

    public boolean isParalyzeImmune() {
        return false;
    }

    public void reduceArrowCount() {
    }

    public void sendChanges() {
        getStatsRecorder().sendChanges();
    }

    public void sendMessage(final String message) {
    }

    public void sendPacket(final IStaticPacket mov) {
    }

    public void sendPacket(final IStaticPacket... mov) {
    }

    public void sendPacket(final List<? extends IStaticPacket> mov) {
    }

    public void setConsumedSouls(final int i, final NpcInstance monster) {
    }

    public void startPvPFlag(final Creature target) {
    }

    public boolean unChargeShots(final boolean spirit) {
        return false;
    }

    public void updateEffectIcons() {
    }

    protected void refreshHpMpCp() {
        final int maxHp = getMaxHp();
        final int maxMp = getMaxMp();
        final int maxCp = isPlayer() ? getMaxCp() : 0;
        if (_currentHp > maxHp) {
            setCurrentHp(maxHp, false);
        }
        if (_currentMp > maxMp) {
            setCurrentMp(maxMp, false);
        }
        if (_currentCp > maxCp) {
            setCurrentCp(maxCp, false);
        }
        if (_currentHp < maxHp || _currentMp < maxMp || _currentCp < maxCp) {
            startRegeneration();
        }
    }

    public void updateStats() {
        refreshHpMpCp();
        sendChanges();
    }

    public void setOverhitAttacker(final Creature attacker) {
    }

    public void setOverhitDamage(final double damage) {
    }

    public boolean isCursedWeaponEquipped() {
        return false;
    }

    public boolean isHero() {
        return false;
    }

    public int getAccessLevel() {
        return 0;
    }

    public Clan getClan() {
        return null;
    }

    public double getRateAdena() {
        return 1.0;
    }

    public double getRateItems() {
        return 1.0;
    }

    public double getRateExp() {
        return 1.0;
    }

    public double getRateSp() {
        return 1.0;
    }

    public double getRateSpoil() {
        return 1.0;
    }

    public int getFormId() {
        return 0;
    }

    public boolean isNameAbove() {
        return true;
    }

    @Override
    public void setLoc(final Location loc) {
        setXYZ(loc.x, loc.y, loc.z);
    }

    public void setLoc(final Location loc, final boolean MoveTask) {
        setXYZ(loc.x, loc.y, loc.z, MoveTask);
    }

    @Override
    public void setXYZ(final int x, final int y, final int z) {
        setXYZ(x, y, z, false);
    }

    public void setXYZ(final int x, final int y, final int z, final boolean MoveTask) {
        if (!MoveTask) {
            stopMove();
        }
        moveLock.lock();
        try {
            super.setXYZ(x, y, z);
        } finally {
            moveLock.unlock();
        }
        updateZones();
    }

    @Override
    protected void onSpawn() {
        super.onSpawn();
        updateStats();
        updateZones();
    }

    @Override
    public void spawnMe(final Location loc) {
        if (loc.h > 0) {
            setHeading(loc.h);
        }
        try {
            super.spawnMe(loc);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onDespawn() {
        if (!isLockedTarget()) {
            setTarget(null);
        }
        stopMove();
        stopAttackStanceTask();
        stopRegeneration();
        updateZones();
        clearStatusListeners();
        super.onDespawn();
    }

    public final void doDecay() {
        if (!isDead()) {
            return;
        }
        onDecay();
    }

    protected void onDecay() {
        decayMe();
    }

    public void validateLocation(final int broadcast) {
        final L2GameServerPacket sp = new ValidateLocation(this);
        switch (broadcast) {
            case 0:
                sendPacket(sp);
                break;
            case 1:
                broadcastPacket(sp);
                break;
            default:
                broadcastPacketToOthers(sp);
                break;
        }
    }

    public void addUnActiveSkill(final Skill skill) {
        if (skill == null || isUnActiveSkill(skill.getId())) {
            return;
        }
        removeStatsOwner(skill);
        removeTriggers(skill);
        _unActiveSkills.add(skill.getId());
    }

    public void removeUnActiveSkill(final Skill skill) {
        if (skill == null || !isUnActiveSkill(skill.getId())) {
            return;
        }
        addStatFuncs(skill.getStatFuncs());
        addTriggers(skill);
        _unActiveSkills.remove(skill.getId());
    }

    public boolean isUnActiveSkill(final int id) {
        return _unActiveSkills.contains(id);
    }

    public abstract int getLevel();

    public abstract ItemInstance getActiveWeaponInstance();

    public abstract WeaponTemplate getActiveWeaponItem();

    public abstract ItemInstance getSecondaryWeaponInstance();

    public abstract WeaponTemplate getSecondaryWeaponItem();

    public CharListenerList getListeners() {
        if (listeners == null) {
            synchronized (this) {
                if (listeners == null) {
                    listeners = new CharListenerList(this);
                }
            }
        }
        return listeners;
    }

    public <T extends Listener<Creature>> boolean addListener(final T listener) {
        return getListeners().add(listener);
    }

    public <T extends Listener<Creature>> boolean removeListener(final T listener) {
        return getListeners().remove(listener);
    }

    public CharStatsChangeRecorder<? extends Creature> getStatsRecorder() {
        if (_statsRecorder == null) {
            synchronized (this) {
                if (_statsRecorder == null) {
                    _statsRecorder = new CharStatsChangeRecorder<>(this);
                }
            }
        }
        return _statsRecorder;
    }

    @Override
    public boolean isCreature() {
        return true;
    }

    public void displayGiveDamageMessage(final Creature target, final int damage, final boolean crit, final boolean miss, final boolean shld, final boolean magic) {
        if (miss && target.isPlayer() && !target.isDamageBlocked()) {
            target.sendPacket(new SystemMessage(42).addName(this));
        }
    }

    public void displayReceiveDamageMessage(final Creature attacker, final int damage) {
    }

    public Collection<TimeStamp> getSkillReuses() {
        return _skillReuses.values();
    }

    public TimeStamp getSkillReuse(final Skill skill) {
        return _skillReuses.get(skill.hashCode());
    }

    public boolean isPolymorphedNpc() {
        return false;
    }

    public int getInteractDistance(final GameObject target) {
        return getActingRange() + (int) getMinDistance(target);
    }

    protected static class CreatureMoveActionTask extends RunnableImpl {
        private final HardReference<? extends Creature> _creatureRef;

        public CreatureMoveActionTask(final Creature creature) {
            _creatureRef = creature.getRef();
        }

        @Override
        public void runImpl() {
            final Creature actor = _creatureRef.get();
            if (actor == null) {
                return;
            }
            actor.moveLock.lock();
            try {
                final MoveActionBase moveActionBase = actor.moveAction;
                if (actor._moveTaskRunnable == this && moveActionBase != null && !moveActionBase.isFinished() && moveActionBase.tickImpl(actor) && actor._moveTaskRunnable == this) {
                    moveActionBase.scheduleNextTick();
                }
            } finally {
                actor.moveLock.unlock();
            }
        }
    }

    protected abstract static class MoveActionBase {
        private final HardReference<? extends Creature> actorRef;
        private final boolean isForPlayable;
        protected volatile boolean isFinished;
        private long prevTick;
        private int prevSpeed;
        private double passDist;

        public MoveActionBase(final Creature actor) {
            isFinished = false;
            actorRef = actor.getRef();
            isForPlayable = actor.isPlayable();
            prevTick = 0L;
            prevSpeed = 0;
            passDist = 0.0;
            isFinished = false;
        }

        protected boolean isForPlayable() {
            return isForPlayable;
        }

        protected Creature getActor() {
            return actorRef.get();
        }

        protected void setIsFinished(final boolean isFinished) {
            this.isFinished = isFinished;
        }

        public boolean isFinished() {
            return isFinished;
        }

        protected long getPrevTick() {
            return prevTick;
        }

        protected void setPrevTick(final long prevTick) {
            this.prevTick = prevTick;
        }

        protected int getPrevSpeed() {
            return prevSpeed;
        }

        protected void setPrevSpeed(final int prevSpeed) {
            this.prevSpeed = prevSpeed;
        }

        protected double getPassDist() {
            return passDist;
        }

        protected void setPassDist(final double passDist) {
            this.passDist = passDist;
        }

        public boolean start() {
            final Creature actor = getActor();
            if (actor == null) {
                return false;
            }
            setPrevTick(System.currentTimeMillis());
            setPrevSpeed(actor.getMoveSpeed());
            setPassDist(0.0);
            setIsFinished(false);
            return weightCheck(actor);
        }

        public abstract Location moveFrom();

        public abstract Location moveTo();

        protected double getMoveLen() {
            return PositionUtils.calculateDistance(moveFrom(), moveTo(), includeMoveZ());
        }

        protected boolean includeMoveZ() {
            final Creature actor = getActor();
            return actor == null || actor.isInWater() || actor.isFlying() || actor.isBoat() || actor.isInBoat();
        }

        public int getNextTickInterval() {
            if (!isForPlayable()) {
                return Math.min(Config.MOVE_TASK_QUANTUM_NPC, (int) (1000.0 * (getMoveLen() - getPassDist()) / Math.max(getPrevSpeed(), 1)));
            }
            return Math.min(Config.MOVE_TASK_QUANTUM_PC, (int) (1000.0 * (getMoveLen() - getPassDist()) / Math.max(getPrevSpeed(), 1)));
        }

        protected boolean onEnd() {
            return true;
        }

        protected void onFinish(final boolean finishedWell, final boolean isInterrupted) {
            setIsFinished(true);
        }

        public void interrupt() {
            tick();
            onFinish(false, true);
        }

        protected boolean onTick(final double done) {
            final Creature actor = getActor();
            if (actor == null) {
                onFinish(false, true);
                return false;
            }
            return true;
        }

        public boolean scheduleNextTick() {
            final Creature actor = getActor();
            if (actor == null) {
                return false;
            }
            Runnable r;
            actor._moveTaskRunnable = (r = new CreatureMoveActionTask(actor));
            actor._moveTask = ThreadPoolManager.getInstance().schedule(r, getNextTickInterval());
            return true;
        }

        public boolean tick() {
            final Creature actor = getActor();
            if (actor == null) {
                return false;
            }
            actor.moveLock.lock();
            try {
                return tickImpl(actor);
            } finally {
                actor.moveLock.unlock();
            }
        }

        private boolean tickImpl(final Creature actor) {
            if (isFinished()) {
                return false;
            }
            if (actor.moveAction != this) {
                setIsFinished(true);
                return false;
            }
            if (actor.isMovementDisabled()) {
                onFinish(false, false);
                return false;
            }
            final int currSpeed = actor.getMoveSpeed();
            if (currSpeed <= 0) {
                onFinish(false, false);
                return false;
            }
            final long now = System.currentTimeMillis();
            final float delta = (now - getPrevTick()) / 1000.0f;
            final boolean includeMoveZ = includeMoveZ();
            double passLen = getPassDist();
            passLen += delta * (Math.max(getPrevSpeed() + currSpeed, 2) / 2.0);
            setPrevTick(now);
            setPrevSpeed(currSpeed);
            setPassDist(passLen);
            final double len = getMoveLen();
            final double done = Math.max(0.0, Math.min(passLen / Math.max(len, 1.0), 1.0));
            final Location currLoc = actor.getLoc();
            final Location newLoc = currLoc.clone();
            if (!calcMidDest(actor, newLoc, includeMoveZ, done, passLen, len)) {
                onFinish(false, false);
                return false;
            }
            if (!includeMoveZ) {
            }
            actor.setLoc(newLoc, true);
            if (done == 1.0) {
                return !onEnd();
            }
            if (!onTick(done)) {
                setIsFinished(true);
                return false;
            }
            return true;
        }

        protected boolean weightCheck(final Creature creature) {
            if (!creature.isPlayer()) {
                return true;
            }
            if (creature.getPlayer().getCurrentLoad() >= 2 * creature.getPlayer().getMaxLoad()) {
                creature.sendPacket(new SystemMessage(555));
                return false;
            }
            return true;
        }

        protected boolean calcMidDest(final Creature creature, final Location result, final boolean includeZ, final double done, final double pass, final double len) {
            result.set(moveTo().clone().indent(moveFrom(), (int) Math.round(len - pass), creature.isFlying() || creature.isInWater())).correctGeoZ();
            return true;
        }

        public abstract L2GameServerPacket movePacket();
    }

    public abstract static class MoveToAction extends MoveActionBase {
        protected final int indent;
        protected final boolean pathFind;
        protected final boolean ignoreGeo;
        protected final Queue<List<Location>> geoPathLines;
        protected List<Location> currentGeoPathLine;
        protected Location moveFrom;
        protected Location moveTo;
        protected double prevMoveLen;
        protected boolean prevIncZ;

        protected MoveToAction(final Creature actor, final boolean ignoreGeo, final int indent, final boolean pathFind) {
            super(actor);
            this.indent = indent;
            this.pathFind = pathFind;
            this.ignoreGeo = ignoreGeo;
            geoPathLines = new LinkedList<>();
            currentGeoPathLine = Collections.emptyList();
            moveFrom = actor.getLoc();
            moveTo = actor.getLoc();
            prevMoveLen = 0.0;
            prevIncZ = false;
        }

        protected boolean buildPathLines(final Location pathFrom, final Location pathTo) {
            final Creature actor = getActor();
            if (actor == null) {
                return false;
            }
            final LinkedList<List<Location>> geoPathLines = new LinkedList<>();
            if (!GeoMove.buildGeoPath(geoPathLines, pathFrom.clone().world2geo(), pathTo.clone().world2geo(), actor.getGeoIndex(), (int) actor.getColRadius(), (int) actor.getColHeight(), indent, pathFind && !ignoreGeo && !isRelativeMove(), isForPlayable(), actor.isFlying(), actor.isInWater(), actor.getWaterZ(), ignoreGeo)) {
                return false;
            }
            this.geoPathLines.clear();
            this.geoPathLines.addAll(geoPathLines);
            return true;
        }

        protected boolean pollPathLine() {
            final List<Location> currentGeoPathLine = geoPathLines.poll();
            this.currentGeoPathLine = currentGeoPathLine;
            if (currentGeoPathLine != null) {
                final Creature actor = getActor();
                moveFrom = this.currentGeoPathLine.get(0).clone().geo2world();
                moveTo = this.currentGeoPathLine.get(this.currentGeoPathLine.size() - 1).clone().geo2world();
                prevIncZ = includeMoveZ();
                prevMoveLen = PositionUtils.calculateDistance(moveFrom, moveTo, prevIncZ);
                setPassDist(0.0);
                setPrevTick(System.currentTimeMillis());
                if (prevMoveLen > 16.0) {
                    actor.setHeading(PositionUtils.calculateHeadingFrom(moveFrom.getX(), moveFrom.getY(), moveTo.getX(), moveTo.getY()));
                }
                return true;
            }
            return false;
        }

        protected int remainingLinesCount() {
            return geoPathLines.size();
        }

        protected abstract boolean isRelativeMove();

        @Override
        protected boolean calcMidDest(final Creature creature, final Location result, final boolean includeZ, final double done, final double pass, final double len) {
            if (currentGeoPathLine == null) {
                return false;
            }
            final Location currLoc = creature.getLoc();
            if (len < 16.0 || done == 0.0 || pass == 0.0 || currentGeoPathLine.isEmpty()) {
                result.set(currLoc);
                return true;
            }
            final int lastIdx = currentGeoPathLine.size() - 1;
            result.set(moveFrom).indent(moveTo, (int) (pass + 0.5), includeZ).setZ(currentGeoPathLine.get(Math.min(lastIdx, (int) (lastIdx * done + 0.5))).getZ());
            return result.equalsGeo(currLoc) || ignoreGeo || !Config.ALLOW_GEODATA || includeZ || GeoEngine.canMoveToCoord(currLoc.getX(), currLoc.getY(), currLoc.getZ(), result.getX(), result.getY(), result.getZ(), creature.getGeoIndex());
        }

        @Override
        public Location moveFrom() {
            return moveFrom;
        }

        @Override
        public Location moveTo() {
            return moveTo;
        }

        @Override
        protected double getMoveLen() {
            final boolean incZ = includeMoveZ();
            if (incZ != prevIncZ) {
                prevMoveLen = PositionUtils.calculateDistance(moveFrom, moveTo, incZ);
                prevIncZ = incZ;
            }
            return prevMoveLen;
        }
    }

    public static class MoveToLocationAction extends MoveToAction {
        private final Location dst;
        private final Location src;

        public MoveToLocationAction(final Creature actor, final Location moveFrom, final Location moveTo, final boolean ignoreGeo, final int indent, final boolean pathFind) {
            super(actor, ignoreGeo, indent, pathFind);
            src = moveFrom.clone();
            dst = moveTo.clone();
        }

        public MoveToLocationAction(final Creature actor, final Location dest, final int indent, final boolean pathFind) {
            this(actor, actor.getLoc(), dest, actor.isBoat() || actor.isInBoat(), indent, pathFind);
        }

        public boolean isSameDest(final Location to) {
            return dst.equalsGeo(to);
        }

        @Override
        public boolean start() {
            return super.start() && buildPathLines(src, dst) && !onEnd();
        }

        @Override
        protected boolean onEnd() {
            final Creature actor = getActor();
            if (actor == null) {
                return true;
            }
            if (!pollPathLine()) {
                onFinish(true, false);
                return true;
            }
            actor.broadcastMove();
            return false;
        }

        @Override
        protected void onFinish(final boolean finishedWell, final boolean isInterrupted) {
            final Creature actor = getActor();
            if (isFinished() || actor == null) {
                return;
            }
            if (isInterrupted) {
                setIsFinished(true);
                return;
            }
            if (finishedWell) {
                ThreadPoolManager.getInstance().execute(new NotifyAITask(actor, CtrlEvent.EVT_ARRIVED));
            } else {
                actor.stopMove(true, true, false);
                ThreadPoolManager.getInstance().execute(new NotifyAITask(actor, CtrlEvent.EVT_ARRIVED_BLOCKED, actor.getLoc()));
            }
            super.onFinish(finishedWell, isInterrupted);
        }

        @Override
        public L2GameServerPacket movePacket() {
            final Creature actor = getActor();
            return (actor != null) ? new CharMoveToLocation(actor, actor.getLoc(), moveTo.clone()) : null;
        }

        @Override
        protected boolean isRelativeMove() {
            return false;
        }
    }

    public static class MoveToRelativeAction extends MoveToAction {
        private final HardReference<? extends GameObject> targetRef;
        private final int range;
        private Location prevTargetLoc;
        private boolean isRelativeMoveEnabled;

        protected MoveToRelativeAction(final Creature actor, final GameObject target, final boolean ignoreGeo, final int indent, final int range, final boolean pathFind) {
            super(actor, ignoreGeo, indent, pathFind);
            targetRef = target.getRef();
            prevTargetLoc = target.getLoc().clone();
            this.range = Math.max(range, indent + 16);
            isRelativeMoveEnabled = false;
        }

        private GameObject getTarget() {
            return targetRef.get();
        }

        public boolean isSameTarget(final GameObject target) {
            return getTarget() == target;
        }

        @Override
        public boolean start() {
            if (!super.start()) {
                return false;
            }
            final Creature actor = getActor();
            final GameObject target = getTarget();
            if (actor == null || target == null) {
                return false;
            }
            final Location actorLoc = actor.getLoc();
            final Location pawnLoc = target.getLoc().clone();
            if (!buildPathLines(actorLoc, pawnLoc)) {
                return false;
            }
            prevTargetLoc = pawnLoc.clone();
            return !onEnd();
        }

        protected boolean isPathRebuildRequired() {
            final Creature actor = getActor();
            final GameObject target = getTarget();
            if (actor == null || target == null) {
                return true;
            }
            final Location targetLoc = target.getLoc();
            return isRelativeMoveEnabled && !prevTargetLoc.equalsGeo(targetLoc);
        }

        @Override
        protected boolean onEnd() {
            final Creature actor = getActor();
            final GameObject target = getTarget();
            if (actor == null || target == null) {
                return true;
            }
            final int remainingLinesCount = remainingLinesCount();
            if (remainingLinesCount > 1) {
                if (!pollPathLine()) {
                    onFinish(false, false);
                    return true;
                }
            } else {
                if (remainingLinesCount != 1) {
                    onFinish(true, false);
                    return true;
                }
                if (!(actor instanceof Summon)) {
                    isRelativeMoveEnabled = true;
                }
                if (isPathRebuildRequired()) {
                    if (isArrived()) {
                        onFinish(true, false);
                        return true;
                    }
                    final Location actorLoc = actor.getLoc();
                    final Location targetLoc = getImpliedTargetLoc();
                    if (!buildPathLines(actorLoc, targetLoc)) {
                        onFinish(false, false);
                        return true;
                    }
                    if (!pollPathLine()) {
                        onFinish(false, false);
                        return true;
                    }
                    prevTargetLoc = targetLoc.clone();
                } else if (!pollPathLine()) {
                    onFinish(false, false);
                    return true;
                }
            }
            actor.broadcastMove();
            return false;
        }

        protected boolean isArrived() {
            final Creature actor = getActor();
            final GameObject target = getTarget();
            if (actor == null || target == null) {
                return false;
            }
            if (target.isCreature() && ((Creature) target).isMoving()) {
                final int threshold = indent + 16;
                if (includeMoveZ()) {
                    return target.isInRangeZ(actor, threshold);
                }
                return target.isInRange(actor, threshold);
            } else {
                if (includeMoveZ()) {
                    return target.isInRangeZ(actor, indent + 16);
                }
                return target.isInRange(actor, indent + 16);
            }
        }

        private Location getImpliedTargetLoc() {
            final Creature actor = getActor();
            final GameObject targetObj = getTarget();
            if (actor == null || targetObj == null) {
                return null;
            }
            if (!targetObj.isCreature()) {
                return targetObj.getLoc();
            }
            final Creature target = (Creature) targetObj;
            final Location loc = targetObj.getLoc();
            if (!target.isMoving()) {
                return loc;
            }
            return GeoMove.getIntersectPoint(actor.getLoc(), loc, target.getMoveSpeed(), Math.max(128, Config.MOVE_TASK_QUANTUM_PC / 2));
        }

        @Override
        protected boolean onTick(final double done) {
            if (!super.onTick(done)) {
                return false;
            }
            final Creature actor = getActor();
            final GameObject target = getTarget();
            if (actor == null || target == null) {
                return false;
            }
            if (done < 1.0) {
                if (isPathRebuildRequired()) {
                    final Location actorLoc = actor.getLoc();
                    final Location pawnLoc = getImpliedTargetLoc();
                    if (actor.isPlayer() && actor.getPlayer().getNetConnection() != null) {
                        final int pawnClippingRange = actor.getPlayer().getNetConnection().getPawnClippingRange();
                        if (actorLoc.distance3D(pawnLoc) > pawnClippingRange) {
                            onFinish(false, false);
                            return false;
                        }
                    }
                    if (!buildPathLines(actorLoc, pawnLoc)) {
                        onFinish(false, false);
                        return false;
                    }
                    if (!pollPathLine()) {
                        onFinish(false, false);
                        return false;
                    }
                    prevTargetLoc = pawnLoc.clone();
                } else if (isRelativeMoveEnabled && isArrived()) {
                    onFinish(true, false);
                    return false;
                }
            }
            return true;
        }

        @Override
        protected void onFinish(final boolean finishedWell, final boolean isInterrupted) {
            final Creature actor = getActor();
            final GameObject target = getTarget();
            if (isFinished() || actor == null || target == null) {
                return;
            }
            if (isInterrupted) {
                setIsFinished(true);
                return;
            }
            actor.stopMove(!(target instanceof StaticObjectInstance) && !target.isDoor(), false, false);
            boolean succeed = false;
            if (finishedWell) {
                succeed = ((includeMoveZ() ? actor.getRealDistance3D(target) : actor.getRealDistance(target)) <= range + 16);
            }
            setIsFinished(true);
            if (succeed) {
                ThreadPoolManager.getInstance().execute(new NotifyAITask(actor, CtrlEvent.EVT_ARRIVED_TARGET));
            } else {
                ThreadPoolManager.getInstance().execute(new NotifyAITask(actor, CtrlEvent.EVT_ARRIVED_BLOCKED, actor.getLoc()));
            }
        }

        @Override
        protected boolean isRelativeMove() {
            return isRelativeMoveEnabled;
        }

        @Override
        public L2GameServerPacket movePacket() {
            final Creature actor = getActor();
            if (actor == null) {
                return null;
            }
            final GameObject target = getTarget();
            if (!isRelativeMove()) {
                return new CharMoveToLocation(actor, actor.getLoc(), moveTo.clone());
            }
            if (target == null) {
                return null;
            }
            return new MoveToPawn(actor, target, indent);
        }
    }

    private class AttackStanceTask extends RunnableImpl {
        @Override
        public void runImpl() {
            if (!isInCombat()) {
                stopAttackStanceTask();
            }
        }
    }

    private class RegenTask implements Runnable {
        @Override
        public void run() {
            if (isAlikeDead() || getRegenTick() == 0L) {
                return;
            }
            final double hpStart = _currentHp;
            final int maxHp = getMaxHp();
            final int maxMp = getMaxMp();
            final int maxCp = isPlayer() ? getMaxCp() : 0;
            double addHp = 0.0;
            double addMp = 0.0;
            regenLock.lock();
            try {
                if (_currentHp < maxHp) {
                    addHp += Formulas.calcHpRegen(Creature.this);
                }
                if (_currentMp < maxMp) {
                    addMp += Formulas.calcMpRegen(Creature.this);
                }
                if (isPlayer() && Config.REGEN_SIT_WAIT) {
                    final Player pl = (Player) Creature.this;
                    if (pl.isSitting()) {
                        pl.updateWaitSitTime();
                        if (pl.getWaitSitTime() > 5) {
                            addHp += pl.getWaitSitTime();
                            addMp += pl.getWaitSitTime();
                        }
                    }
                } else if (isRaid()) {
                    addHp *= Config.RATE_RAID_REGEN;
                    addMp *= Config.RATE_RAID_REGEN;
                }
                final Creature creature = Creature.this;
                creature._currentHp += Math.max(0.0, Math.min(addHp, calcStat(Stats.HP_LIMIT, null, null) * maxHp / 100.0 - _currentHp));
                final Creature creature1 = Creature.this;
                creature1._currentMp += Math.max(0.0, Math.min(addMp, calcStat(Stats.MP_LIMIT, null, null) * maxMp / 100.0 - _currentMp));
                _currentHp = Math.min(maxHp, _currentHp);
                _currentMp = Math.min(maxMp, _currentMp);
                if (isPlayer()) {
                    final Creature creature2 = Creature.this;
                    creature2._currentCp += Math.max(0.0, Math.min(Formulas.calcCpRegen(Creature.this), calcStat(Stats.CP_LIMIT, null, null) * maxCp / 100.0 - _currentCp));
                    _currentCp = Math.min(maxCp, _currentCp);
                }
                if (_currentHp == maxHp && _currentMp == maxMp && _currentCp == maxCp) {
                    stopRegeneration();
                }
            } finally {
                regenLock.unlock();
            }
            broadcastStatusUpdate();
            sendChanges();
            checkHpMessages(hpStart, _currentHp);
        }
    }
}
