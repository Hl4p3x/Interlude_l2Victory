package ru.j2dev.gameserver.model;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.geometry.Polygon;
import ru.j2dev.commons.lang.ArrayUtils;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.geodata.GeoEngine;
import ru.j2dev.gameserver.model.Zone.ZoneType;
import ru.j2dev.gameserver.model.base.*;
import ru.j2dev.gameserver.model.entity.events.GlobalEvent;
import ru.j2dev.gameserver.model.entity.olympiad.OlympiadGameType;
import ru.j2dev.gameserver.model.instances.ChestInstance;
import ru.j2dev.gameserver.model.instances.FeedableBeastInstance;
import ru.j2dev.gameserver.model.items.Inventory;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.FlyToLocation.FlyType;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;
import ru.j2dev.gameserver.skills.effects.EffectTemplate;
import ru.j2dev.gameserver.skills.skillclasses.*;
import ru.j2dev.gameserver.skills.skillclasses.DeathPenalty;
import ru.j2dev.gameserver.stats.Env;
import ru.j2dev.gameserver.stats.Formulas;
import ru.j2dev.gameserver.stats.StatTemplate;
import ru.j2dev.gameserver.stats.Stats;
import ru.j2dev.gameserver.stats.conditions.Condition;
import ru.j2dev.gameserver.stats.funcs.Func;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.templates.StatsSet;
import ru.j2dev.gameserver.utils.PositionUtils;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class Skill extends StatTemplate implements Cloneable {
    public static final Skill[] EMPTY_ARRAY = new Skill[0];
    public static final int SKILL_CRAFTING = 172;
    public static final int SKILL_POLEARM_MASTERY = 216;
    public static final int SKILL_CRYSTALLIZE = 248;
    public static final int SKILL_WEAPON_MAGIC_MASTERY1 = 249;
    public static final int SKILL_WEAPON_MAGIC_MASTERY2 = 250;
    public static final int SKILL_BLINDING_BLOW = 321;
    public static final int SKILL_STRIDER_ASSAULT = 325;
    public static final int SKILL_WYVERN_AEGIS = 327;
    public static final int SKILL_BLUFF = 358;
    public static final int SKILL_HEROIC_MIRACLE = 395;
    public static final int SKILL_HEROIC_BERSERKER = 396;
    public static final int SKILL_SOUL_MASTERY = 467;
    public static final int SKILL_TRANSFORM_DISPEL = 619;
    public static final int SKILL_FINAL_FLYING_FORM = 840;
    public static final int SKILL_AURA_BIRD_FALCON = 841;
    public static final int SKILL_AURA_BIRD_OWL = 842;
    public static final int SKILL_RECHARGE = 1013;
    public static final int SKILL_TRANSFER_PAIN = 1262;
    public static final int SKILL_FISHING_MASTERY = 1315;
    public static final int SKILL_NOBLESSE_BLESSING = 1323;
    public static final int SKILL_SUMMON_CP_POTION = 1324;
    public static final int SKILL_FORTUNE_OF_NOBLESSE = 1325;
    public static final int SKILL_HARMONY_OF_NOBLESSE = 1326;
    public static final int SKILL_SYMPHONY_OF_NOBLESSE = 1327;
    public static final int SKILL_HEROIC_VALOR = 1374;
    public static final int SKILL_HEROIC_GRANDEUR = 1375;
    public static final int SKILL_HEROIC_DREAD = 1376;
    public static final int SKILL_MYSTIC_IMMUNITY = 1411;
    public static final int SKILL_RAID_BLESSING = 2168;
    public static final int SKILL_HINDER_STRIDER = 4258;
    public static final int SKILL_WYVERN_BREATH = 4289;
    public static final int SKILL_RAID_CURSE = 4515;
    public static final int SKILL_CHARM_OF_COURAGE = 5041;
    protected static final Logger LOGGER = LoggerFactory.getLogger(Skill.class);

    protected final int[] _itemConsume;
    protected final int[] _itemConsumeId;
    protected final int _referenceItemId;
    protected final int _referenceItemMpConsume;
    protected final int[] _affectLimit = new int[2];
    private final int hashCode;
    public boolean _isStandart;
    protected EffectTemplate[] _effectTemplates;
    protected List<Integer> _teachers;
    protected List<ClassId> _canLearn;
    protected List<AddedSkill> _addedSkills;
    protected boolean _isAltUse;
    protected boolean _isBehind;
    protected boolean _isCancelable;
    protected boolean _isCorpse;
    protected boolean _isCommon;
    protected boolean _isItemHandler;
    protected boolean _isOffensive;
    protected boolean _isPvpSkill;
    protected boolean _isNotUsedByAI;
    protected boolean _isFishingSkill;
    protected boolean _isPvm;
    protected boolean _isForceUse;
    protected boolean _isNewbie;
    protected boolean _isPreservedOnDeath;
    protected boolean _isHeroic;
    protected boolean _isSaveable;
    protected boolean _isSkillTimePermanent;
    protected boolean _isReuseDelayPermanent;
    protected boolean _isReflectable;
    protected boolean _isSuicideAttack;
    protected boolean _isShieldignore;
    protected boolean _isUndeadOnly;
    protected Ternary _isUseSS;
    protected boolean _isOverhit;
    protected boolean _isBlow;
    protected boolean _isChargeBoost;
    protected boolean _isUsingWhileCasting;
    protected boolean _isIgnoreResists;
    protected boolean _isIgnoreInvul;
    protected boolean _isTrigger;
    protected boolean _isNotAffectedByMute;
    protected boolean _basedOnTargetDebuff;
    protected boolean _deathlink;
    protected boolean _hideStartMessage;
    protected boolean _hideUseMessage;
    protected boolean _skillInterrupt;
    protected boolean _flyingTransformUsage;
    protected boolean _canUseTeleport;
    protected boolean _isProvoke;
    protected boolean _isCubicSkill;
    protected boolean _isSelfDispellable;
    protected boolean _isSlotNone;
    protected boolean _isSharedClassReuse;
    protected SkillType _skillType;
    protected SkillOpType _operateType;
    protected SkillTargetType _targetType;
    protected SkillMagicType _magicType;
    protected SkillTrait _traitType;
    protected BaseStats _saveVs;
    protected SkillNextAction _skillNextAction;
    protected Element _element;
    protected FlyType _flyType;
    protected boolean _flyToBack;
    protected Condition[] _preCondition;
    protected int _id;
    protected int _level;
    protected int _baseLevel;
    protected int _displayId;
    protected int _displayLevel;
    protected int _activateRate;
    protected int _castRange;
    protected int _cancelTarget;
    protected int _coolTime;
    protected int _delayedEffect;
    protected int _effectPoint;
    protected int _energyConsume;
    protected int _elementPower;
    protected int _flyRadius;
    protected int _hitTime;
    protected int _hpConsume;
    protected int _levelModifier;
    protected int _magicLevel;
    protected int _matak;
    protected int _minPledgeClass;
    protected int _minRank;
    protected int _negatePower;
    protected int _negateSkill;
    protected int _npcId;
    protected int _numCharges;
    protected int _skillInterruptTime;
    protected int _skillRadius;
    protected int _effectiveRange;
    protected int _symbolId;
    protected int _weaponsAllowed;
    protected int _enchantLevelCount;
    protected int _criticalRate;
    protected int _secondSkill;
    protected long _reuseDelay;
    protected double _power;
    protected double _powerPvP;
    protected double _powerPvE;
    protected double _mpConsume1;
    protected double _mpConsume2;
    protected double _lethal1;
    protected double _lethal2;
    protected double _absorbPart;
    protected String _name;
    protected String _baseValues;
    protected String _icon;
    protected int _baseBlowRate;

    protected Skill(final StatsSet set) {
        _effectTemplates = EffectTemplate.EMPTY_ARRAY;
        _addedSkills = Collections.emptyList();
        _isCubicSkill = false;
        _preCondition = Condition.EMPTY_ARRAY;
        _isStandart = false;
        _id = set.getInteger("skill_id");
        _level = set.getInteger("level");
        _displayId = set.getInteger("displayId", _id);
        _displayLevel = set.getInteger("displayLevel", _level);
        _baseLevel = set.getInteger("base_level");
        _name = set.getString("name");
        _operateType = set.getEnum("operateType", SkillOpType.class);
        _isNewbie = set.getBool("isNewbie", false);
        _isSelfDispellable = set.getBool("isSelfDispellable", true);
        _isPreservedOnDeath = set.getBool("isPreservedOnDeath", false);
        _isHeroic = set.getBool("isHeroic", false);
        _isAltUse = set.getBool("altUse", false);
        _mpConsume1 = set.getInteger("mpConsume1", 0);
        _mpConsume2 = set.getInteger("mpConsume2", 0);
        _energyConsume = set.getInteger("energyConsume", 0);
        _hpConsume = set.getInteger("hpConsume", 0);
        _isChargeBoost = set.getBool("chargeBoost", false);
        _isProvoke = set.getBool("provoke", false);
        _isUsingWhileCasting = set.getBool("isUsingWhileCasting", false);
        _matak = set.getInteger("mAtk", 0);
        _isUseSS = Ternary.valueOf(set.getString("useSS", Ternary.DEFAULT.toString()).toUpperCase());
        _magicLevel = set.getInteger("magicLevel", 0);
        _castRange = set.getInteger("castRange", 40);
        _effectiveRange = set.getInteger("effectiveRange", _castRange + ((_castRange < 200) ? 400 : 500));
        _baseValues = set.getString("baseValues", null);
        _isBlow = set.getBool("blow", false);
        _baseBlowRate = set.getInteger("baseBlowRate", _isBlow ? 200 : 0);
        final String s1 = set.getString("itemConsumeCount", "");
        final String s2 = set.getString("itemConsumeId", "");
        if (s1.length() == 0) {
            _itemConsume = new int[]{0};
        } else {
            final String[] s3 = s1.split(" ");
            _itemConsume = new int[s3.length];
            for (int i = 0; i < s3.length; ++i) {
                _itemConsume[i] = Integer.parseInt(s3[i]);
            }
        }
        if (s2.length() == 0) {
            _itemConsumeId = new int[]{0};
        } else {
            final String[] s3 = s2.split(" ");
            _itemConsumeId = new int[s3.length];
            for (int i = 0; i < s3.length; ++i) {
                _itemConsumeId[i] = Integer.parseInt(s3[i]);
            }
        }
        _referenceItemId = set.getInteger("referenceItemId", 0);
        _referenceItemMpConsume = set.getInteger("referenceItemMpConsume", 0);
        _isItemHandler = set.getBool("isHandler", false);
        _isCommon = set.getBool("isCommon", false);
        _isSaveable = set.getBool("isSaveable", true);
        _coolTime = set.getInteger("coolTime", 0);
        _skillInterruptTime = set.getInteger("hitCancelTime", 0);
        _reuseDelay = set.getLong("reuseDelay", 0L);
        _hitTime = set.getInteger("hitTime", 0);
        _skillRadius = set.getInteger("skillRadius", 80);
        _targetType = set.getEnum("target", SkillTargetType.class);
        _magicType = set.getEnum("magicType", SkillMagicType.class, SkillMagicType.PHYSIC);
        _traitType = set.getEnum("trait", SkillTrait.class, null);
        _saveVs = set.getEnum("saveVs", BaseStats.class, null);
        _hideStartMessage = set.getBool("isHideStartMessage", false);
        _hideUseMessage = set.getBool("isHideUseMessage", false);
        _isUndeadOnly = set.getBool("undeadOnly", false);
        _isCorpse = set.getBool("corpse", false);
        _power = set.getDouble("power", 0.0);
        _powerPvP = set.getDouble("powerPvP", 0.0);
        _powerPvE = set.getDouble("powerPvE", 0.0);
        _effectPoint = set.getInteger("effectPoint", 0);
        _skillNextAction = SkillNextAction.valueOf(set.getString("nextAction", "DEFAULT").toUpperCase());
        _skillType = set.getEnum("skillType", SkillType.class);
        _isSuicideAttack = set.getBool("isSuicideAttack", false);
        _isSkillTimePermanent = set.getBool("isSkillTimePermanent", false);
        _isReuseDelayPermanent = set.getBool("isReuseDelayPermanent", false);
        _deathlink = set.getBool("deathlink", false);
        _basedOnTargetDebuff = set.getBool("basedOnTargetDebuff", false);
        _isNotUsedByAI = set.getBool("isNotUsedByAI", false);
        _isIgnoreResists = set.getBool("isIgnoreResists", false);
        _isIgnoreInvul = set.getBool("isIgnoreInvul", false);
        _isSharedClassReuse = set.getBool("isSharedClassReuse", false);
        _isTrigger = set.getBool("isTrigger", false);
        _isNotAffectedByMute = set.getBool("isNotAffectedByMute", false);
        _flyingTransformUsage = set.getBool("flyingTransformUsage", false);
        _canUseTeleport = set.getBool("canUseTeleport", true);
        if (NumberUtils.isNumber(set.getString("element", "NONE"))) {
            _element = Element.getElementById(set.getInteger("element", -1));
        } else {
            _element = Element.getElementByName(set.getString("element", "none").toUpperCase());
        }
        _elementPower = set.getInteger("elementPower", 0);
        if (_element != Element.NONE && _elementPower == 0) {
            _elementPower = 20;
        }
        _activateRate = set.getInteger("activateRate", -1);
        _levelModifier = set.getInteger("levelModifier", 1);
        _isCancelable = set.getBool("cancelable", true);
        _isReflectable = set.getBool("reflectable", true);
        _isShieldignore = set.getBool("shieldignore", false);
        _criticalRate = set.getInteger("criticalRate", 0);
        _isOverhit = set.getBool("overHit", false);
        _weaponsAllowed = set.getInteger("weaponsAllowed", 0);
        _minPledgeClass = set.getInteger("minPledgeClass", 0);
        _minRank = set.getInteger("minRank", 0);
        _isOffensive = set.getBool("isOffensive", _skillType.isOffensive());
        _isPvpSkill = set.getBool("isPvpSkill", _skillType.isPvpSkill());
        _isFishingSkill = set.getBool("isFishingSkill", false);
        _isPvm = set.getBool("isPvm", _skillType.isPvM());
        _isForceUse = set.getBool("isForceUse", false);
        _isBehind = set.getBool("behind", false);
        _symbolId = set.getInteger("symbolId", 0);
        _npcId = set.getInteger("npcId", 0);
        _flyType = FlyType.valueOf(set.getString("flyType", "NONE").toUpperCase());
        _flyToBack = set.getBool("flyToBack", false);
        _flyRadius = set.getInteger("flyRadius", 200);
        _negateSkill = set.getInteger("negateSkill", 0);
        _negatePower = set.getInteger("negatePower", Integer.MAX_VALUE);
        _numCharges = set.getInteger("num_charges", 0);
        _delayedEffect = set.getInteger("delayedEffect", 0);
        _cancelTarget = set.getInteger("cancelTarget", 0);
        _skillInterrupt = set.getBool("skillInterrupt", false);
        _lethal1 = set.getDouble("lethal1", 0.0);
        _lethal2 = set.getDouble("lethal2", 0.0);
        _absorbPart = set.getDouble("absorbPart", 0.0);
        _icon = set.getString("icon", "");
        _secondSkill = set.getInteger("secondSkill", 0);
        _isSlotNone = set.getBool("isIgnorBuffLimit", false);
        final String affectLimit = set.getString("affect_limit", null);
        if (affectLimit != null) {
            try {
                final String[] valuesSplit = affectLimit.split(";");
                _affectLimit[0] = Integer.parseInt(valuesSplit[0]);
                _affectLimit[1] = Integer.parseInt(valuesSplit[1]);
            } catch (final Exception e) {
                throw new IllegalArgumentException("SkillId: " + _id + " invalid affectLimit value: " + affectLimit + ", \"percent;percent\" required");
            }
        }
        StringTokenizer st = new StringTokenizer(set.getString("addSkills", ""), ";");
        while (st.hasMoreTokens()) {
            final int id = Integer.parseInt(st.nextToken());
            int level = Integer.parseInt(st.nextToken());
            if (level == -1) {
                level = _level;
            }
            if(_addedSkills.isEmpty()) {
                _addedSkills = new ArrayList<>();
            }
            _addedSkills.add(new AddedSkill(id, level));
        }
        if (_skillNextAction == SkillNextAction.DEFAULT) {
            switch (_skillType) {
                case SOWING:
                case LETHAL_SHOT:
                case PDAM:
                case CPDAM:
                case SPOIL:
                case STUN: {
                    _skillNextAction = SkillNextAction.ATTACK;
                    break;
                }
                default: {
                    _skillNextAction = SkillNextAction.NONE;
                    break;
                }
            }
        }
        final String canLearn = set.getString("canLearn", null);
        if (canLearn == null) {
            _canLearn = null;
        } else {
            _canLearn = new ArrayList<>();
            st = new StringTokenizer(canLearn, " \r\n\t,;");
            while (st.hasMoreTokens()) {
                final String cls = st.nextToken();
                _canLearn.add(ClassId.valueOf(cls));
            }
        }
        final String teachers = set.getString("teachers", null);
        if (teachers == null) {
            _teachers = null;
        } else {
            _teachers = new ArrayList<>();
            st = new StringTokenizer(teachers, " \r\n\t,;");
            while (st.hasMoreTokens()) {
                final String npcid = st.nextToken();
                _teachers.add(Integer.parseInt(npcid));
            }
        }
        hashCode = _id * 1023 + _level;
    }

    public int getBaseBlowRate() {
        return !isBlowSkill() && _baseBlowRate > 0 ? 0 : _baseBlowRate;
    }

    public final boolean getWeaponDependancy(final Creature activeChar) {
        if (_weaponsAllowed == 0) {
            return true;
        }
        if (activeChar.getActiveWeaponInstance() != null && activeChar.getActiveWeaponItem() != null && (activeChar.getActiveWeaponItem().getItemType().mask() & _weaponsAllowed) != 0x0L) {
            return true;
        }
        if (activeChar.getSecondaryWeaponInstance() != null && activeChar.getSecondaryWeaponItem() != null && (activeChar.getSecondaryWeaponItem().getItemType().mask() & _weaponsAllowed) != 0x0L) {
            return true;
        }
        activeChar.sendPacket(new SystemMessage(113).addSkillName(_displayId, _displayLevel));
        return false;
    }

    public boolean checkCondition(final Creature activeChar, final Creature target, final boolean forceUse, final boolean dontMove, final boolean first) {
        final Player player = activeChar.getPlayer();
        if (activeChar.isDead()) {
            return false;
        }
        if (target != null && activeChar.getReflection() != target.getReflection()) {
            activeChar.sendPacket(SystemMsg.CANNOT_SEE_TARGET);
            return false;
        }
        if (!getWeaponDependancy(activeChar)) {
            return false;
        }
        if (activeChar.isUnActiveSkill(_id)) {
            return false;
        }
        if (first && activeChar.isSkillDisabled(this)) {
            activeChar.sendReuseMessage(this);
            return false;
        }
        if (first) {
            double mpConsume2 = _mpConsume2;
            if (isMusic()) {
                mpConsume2 += activeChar.getEffectList().getActiveMusicCount(getId()) * mpConsume2 / 2.0;
                mpConsume2 = activeChar.calcStat(Stats.MP_DANCE_SKILL_CONSUME, mpConsume2, target, this);
            } else if (isMagic()) {
                mpConsume2 = activeChar.calcStat(Stats.MP_MAGIC_SKILL_CONSUME, mpConsume2, target, this);
            } else {
                mpConsume2 = activeChar.calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, mpConsume2, target, this);
            }
            if (activeChar.getCurrentMp() < _mpConsume1 + mpConsume2) {
                activeChar.sendPacket(Msg.NOT_ENOUGH_MP);
                return false;
            }
        }
        if (activeChar.getCurrentHp() < _hpConsume + 1) {
            activeChar.sendPacket(Msg.NOT_ENOUGH_HP);
            return false;
        }
        if (!_isItemHandler && !_isAltUse && activeChar.isMuted(this)) {
            return false;
        }
        if (player != null) {
            if (player.isInFlyingTransform() && _isItemHandler && !flyingTransformUsage()) {
                player.sendPacket(new SystemMessage(113).addItemName(getItemConsumeId()[0]));
                return false;
            }
            if (player.isInBoat() && player.getBoat().isVehicle() && !(this instanceof FishingSkill) && !(this instanceof ReelingPumping)) {
                return false;
            }
            if (player.isInObserverMode()) {
                activeChar.sendPacket(Msg.OBSERVERS_CANNOT_PARTICIPATE);
                return false;
            }
            if (first && _itemConsume[0] > 0) {
                for (int i = 0; i < _itemConsume.length; i++) {
                    Inventory inv;
                    if (activeChar instanceof Summon) {
                        inv = player.getInventory();
                    } else {
                        inv = ((Playable) activeChar).getInventory();
                    }
                    final ItemInstance requiredItems = inv.getItemByItemId(_itemConsumeId[i]);
                    if (requiredItems == null || requiredItems.getCount() < _itemConsume[i]) {
                        if (activeChar == player) {
                            player.sendPacket(isHandler() ? SystemMsg.INCORRECT_ITEM_COUNT : new SystemMessage(113).addSkillName(getDisplayId(), getDisplayLevel()));
                        }
                        return false;
                    }
                }
            }
            if (player.isFishing() && !isFishingSkill() && !altUse() && !activeChar.isSummon() && !activeChar.isPet()) {
                if (activeChar == player) {
                    player.sendPacket(Msg.ONLY_FISHING_SKILLS_ARE_AVAILABLE);
                }
                return false;
            }
            if (player.isOlyParticipant() && isOffensive() && !player.isOlyCompetitionStarted() && getId() != 347) {
                return false;
            }
        }
        if (getFlyType() != FlyType.NONE && getId() != 628 && getId() != 821 && (activeChar.isImmobilized() || activeChar.isRooted())) {
            activeChar.getPlayer().sendPacket(Msg.YOUR_TARGET_IS_OUT_OF_RANGE);
            return false;
        }
        if (first && target != null && getFlyType() == FlyType.CHARGE && activeChar.isInRange(target.getLoc(), Math.min(150, getFlyRadius()))) {
            activeChar.getPlayer().sendPacket(Msg.THERE_IS_NOT_ENOUGH_SPACE_TO_MOVE_THE_SKILL_CANNOT_BE_USED);
            return false;
        }
        final SystemMsg msg = checkTarget(activeChar, target, target, forceUse, first);
        if (msg != null && activeChar.getPlayer() != null) {
            activeChar.getPlayer().sendPacket(msg);
            return false;
        }
        if (_preCondition.length == 0) {
            return true;
        }
        final Env env = new Env();
        env.character = activeChar;
        env.skill = this;
        env.target = target;
        if (first) {
            for (final Condition cond : _preCondition) {
                if (!cond.test(env)) {
                    final SystemMsg cond_msg = cond.getSystemMsg();
                    if (cond_msg != null) {
                        if (cond_msg.size() > 0) {
                            activeChar.sendPacket((new SystemMessage2(cond_msg)).addSkillName(this));
                        } else {
                            activeChar.sendPacket(cond_msg);
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    public int getSecondSkill() {
        return _secondSkill;
    }

    public SystemMsg checkTarget(final Creature activeChar, final Creature target, final Creature aimingTarget, final boolean forceUse, final boolean first) {
        if ((target == activeChar && isNotTargetAoE()) || (target == activeChar.getPet() && _targetType == SkillTargetType.TARGET_PET_AURA)) {
            return null;
        }
        if (target == null || (isOffensive() && target == activeChar)) {
            return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
        }
        if (activeChar.getReflection() != target.getReflection()) {
            return SystemMsg.CANNOT_SEE_TARGET;
        }
        if (target != activeChar && target == aimingTarget && getCastRange() > 0 && getCastRange() < 32767) {
            if (!GeoEngine.canSeeTarget(activeChar, target, activeChar.isFlying())) {
                return SystemMsg.CANNOT_SEE_TARGET;
            }
            if (!first) {
                final int minRange = (int) (Math.max(0, getEffectiveRange()) + activeChar.getMinDistance(target) + 16.0);
                if (!activeChar.isInRange(target.getLoc(), minRange)) {
                    return SystemMsg.THE_DISTANCE_IS_TOO_FAR_AND_SO_THE_CASTING_HAS_BEEN_STOPPED;
                }
            }
        }
        if (_skillType == SkillType.TAKECASTLE) {
            return null;
        }
        Label_0241:
        {
            if (!first && target != activeChar && (_targetType == SkillTargetType.TARGET_MULTIFACE || _targetType == SkillTargetType.TARGET_MULTIFACE_AURA || _targetType == SkillTargetType.TARGET_TUNNEL)) {
                if (_isBehind) {
                    if (!PositionUtils.isFacing(activeChar, target, 120)) {
                        break Label_0241;
                    }
                } else if (PositionUtils.isFacing(activeChar, target, 60)) {
                    break Label_0241;
                }
                return SystemMsg.YOUR_TARGET_IS_OUT_OF_RANGE;
            }
        }
        if ((target.isDead() != _isCorpse && _targetType != SkillTargetType.TARGET_AREA_AIM_CORPSE) || (_isUndeadOnly && !target.isUndead())) {
            return SystemMsg.INVALID_TARGET;
        }
        if (_isAltUse || _targetType == SkillTargetType.TARGET_FEEDABLE_BEAST || _targetType == SkillTargetType.TARGET_UNLOCKABLE || _targetType == SkillTargetType.TARGET_CHEST) {
            return null;
        }
        final Player player = activeChar.getPlayer();
        if (player != null) {
            final Player pcTarget = target.getPlayer();
            if (pcTarget != null) {
                if (isPvM()) {
                    return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
                }
                if (player.isInZone(ZoneType.epic) != pcTarget.isInZone(ZoneType.epic)) {
                    return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
                }
                if ((!player.isOlyParticipant() && pcTarget.isOlyParticipant()) || (player.isOlyParticipant() && !pcTarget.isOlyParticipant()) || (player.isOlyParticipant() && pcTarget.isOlyParticipant() && player.getOlyParticipant().getCompetition() != pcTarget.getOlyParticipant().getCompetition())) {
                    return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
                }
                if (isOffensive()) {
                    if (player.isOlyParticipant() && pcTarget.isOlyParticipant() && player.getOlyParticipant().getCompetition() != pcTarget.getOlyParticipant().getCompetition()) {
                        return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
                    }
                    if (player.isOlyParticipant() && !player.isOlyCompetitionStarted()) {
                        return SystemMsg.INVALID_TARGET;
                    }
                    if (player.isOlyParticipant() && player.getOlyParticipant() == pcTarget.getOlyParticipant()) {
                        return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
                    }
                    if (pcTarget.isOlyParticipant() && pcTarget.isLooseOlyCompetition()) {
                        return SystemMsg.INVALID_TARGET;
                    }
                    if (player.getTeam() != TeamType.NONE && player.getTeam() == pcTarget.getTeam()) {
                        return SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
                    }
                    if (isAoE() && getCastRange() < 32767 && !GeoEngine.canSeeTarget(activeChar, target, activeChar.isFlying())) {
                        return SystemMsg.CANNOT_SEE_TARGET;
                    }
                    if (activeChar.isInZoneBattle() != target.isInZoneBattle() && !player.getPlayerAccess().PeaceAttack) {
                        return SystemMsg.YOU_MAY_NOT_ATTACK_THIS_TARGET_IN_A_PEACEFUL_ZONE;
                    }
                    if ((activeChar.isInZonePeace() || target.isInZonePeace()) && !player.getPlayerAccess().PeaceAttack) {
                        return SystemMsg.YOU_MAY_NOT_ATTACK_THIS_TARGET_IN_A_PEACEFUL_ZONE;
                    }
                    if (isAoE() && player.getParty() != null && player.getParty() == pcTarget.getParty()) {
                        return SystemMsg.INVALID_TARGET;
                    }
                    if (activeChar.isInZoneBattle()) {
                        if (!forceUse && !isForceUse() && player.getParty() != null && player.getParty() == pcTarget.getParty()) {
                            return SystemMsg.INVALID_TARGET;
                        }
                        return null;
                    } else {
                        SystemMsg msg;
                        for (final GlobalEvent e : player.getEvents()) {
                            if ((msg = e.checkForAttack(target, activeChar, this, forceUse)) != null) {
                                return msg;
                            }
                        }
                        for (final GlobalEvent e : player.getEvents()) {
                            if (e.canAttack(target, activeChar, this, forceUse)) {
                                return null;
                            }
                        }
                        if (isProvoke()) {
                            if (!forceUse && player.getParty() != null && player.getParty() == pcTarget.getParty()) {
                                return SystemMsg.INVALID_TARGET;
                            }
                            return null;
                        } else {
                            if (isPvpSkill() || !forceUse || isAoE()) {
                                if (player == pcTarget) {
                                    return SystemMsg.INVALID_TARGET;
                                }
                                if (player.getParty() != null && player.getParty() == pcTarget.getParty()) {
                                    return SystemMsg.INVALID_TARGET;
                                }
                                if (player.getClan() != null && player.getClan() == pcTarget.getClan()) {
                                    return SystemMsg.INVALID_TARGET;
                                }
                                if (Config.ALLY_ALLOW_BUFF_DEBUFFS && player.getAlliance() != null && player.getAlliance() == pcTarget.getAlliance()) {
                                    return SystemMsg.INVALID_TARGET;
                                }
                            }
                            if (activeChar.isInZone(ZoneType.SIEGE) && target.isInZone(ZoneType.SIEGE)) {
                                return null;
                            }
                            if (activeChar.isInZone(ZoneType.fun) && target.isInZone(ZoneType.fun)) {
                                return null;
                            }
                            if (player.atMutualWarWith(pcTarget)) {
                                return null;
                            }
                            if (isForceUse()) {
                                return null;
                            }
                            if (pcTarget.getPvpFlag() != 0) {
                                return null;
                            }
                            if (pcTarget.getKarma() > 0) {
                                return null;
                            }
                            if (forceUse && !isPvpSkill() && (!isAoE() || aimingTarget == target)) {
                                return null;
                            }
                            return SystemMsg.INVALID_TARGET;
                        }
                    }
                } else {
                    if (pcTarget == player) {
                        return null;
                    }
                    if (player.isOlyParticipant() && player.getOlyParticipant().getCompetition() == pcTarget.getOlyParticipant().getCompetition() && player.getOlyParticipant() != pcTarget.getOlyParticipant()) {
                        if (player.getOlyParticipant().getCompetition().getType() == OlympiadGameType.TEAM_CLASS_FREE) {
                            return SystemMsg.INVALID_TARGET;
                        }
                        if (!forceUse) {
                            return SystemMsg.INVALID_TARGET;
                        }
                    }
                    if (!activeChar.isInZoneBattle() && target.isInZoneBattle()) {
                        return SystemMsg.INVALID_TARGET;
                    }
                    if (forceUse || isForceUse()) {
                        return null;
                    }
                    if (player.getParty() != null && player.getParty() == pcTarget.getParty()) {
                        return null;
                    }
                    if (player.getClan() != null && player.getClan() == pcTarget.getClan()) {
                        return null;
                    }
                    if (Config.ALLY_ALLOW_BUFF_DEBUFFS && player.getAlliance() != null && player.getAlliance() == pcTarget.getAlliance()) {
                        return null;
                    }
                    if (player.atMutualWarWith(pcTarget)) {
                        return SystemMsg.INVALID_TARGET;
                    }
                    if (pcTarget.getPvpFlag() != 0) {
                        return SystemMsg.INVALID_TARGET;
                    }
                    if (pcTarget.getKarma() > 0) {
                        return SystemMsg.INVALID_TARGET;
                    }
                    return null;
                }
            }
        }
        if (isAoE() && isOffensive() && getCastRange() < 32767 && !GeoEngine.canSeeTarget(activeChar, target, activeChar.isFlying())) {
            return SystemMsg.CANNOT_SEE_TARGET;
        }
        if (!forceUse && !isForceUse() && !isOffensive() && target.isAutoAttackable(activeChar)) {
            return SystemMsg.INVALID_TARGET;
        }
        if (!forceUse && !isForceUse() && isOffensive() && !target.isAutoAttackable(activeChar)) {
            return SystemMsg.INVALID_TARGET;
        }
        if (!target.isAttackable(activeChar)) {
            return SystemMsg.INVALID_TARGET;
        }
        return null;
    }

    public final Creature getAimingTarget(final Creature activeChar, final GameObject obj) {
        Creature target = (obj == null || !obj.isCreature()) ? null : ((Creature) obj);
        switch (_targetType) {
            case TARGET_ALLY:
            case TARGET_CLAN:
            case TARGET_PARTY:
            case TARGET_CLAN_ONLY:
            case TARGET_SELF: {
                return activeChar;
            }
            case TARGET_AURA:
            case TARGET_COMMCHANNEL:
            case TARGET_MULTIFACE_AURA: {
                return activeChar;
            }
            case TARGET_HOLY: {
                return (target != null && activeChar.isPlayer() && target.isArtefact()) ? target : null;
            }
            case TARGET_FLAGPOLE: {
                return activeChar;
            }
            case TARGET_UNLOCKABLE: {
                return ((target != null && target.isDoor()) || target instanceof ChestInstance) ? target : null;
            }
            case TARGET_CHEST: {
                return (target instanceof ChestInstance) ? target : null;
            }
            case TARGET_FEEDABLE_BEAST: {
                return (target instanceof FeedableBeastInstance) ? target : null;
            }
            case TARGET_PET:
            case TARGET_PET_AURA: {
                target = activeChar.getPet();
                return (target != null && target.isDead() == _isCorpse) ? target : null;
            }
            case TARGET_OWNER: {
                if (activeChar.isSummon() || activeChar.isPet()) {
                    target = activeChar.getPlayer();
                    return (target != null && target.isDead() == _isCorpse) ? target : null;
                }
                return null;
            }
            case TARGET_ENEMY_PET: {
                if (target == null || target == activeChar.getPet() || !target.isPet()) {
                    return null;
                }
                return target;
            }
            case TARGET_ENEMY_SUMMON: {
                if (target == null || target == activeChar.getPet() || !target.isSummon()) {
                    return null;
                }
                return target;
            }
            case TARGET_ENEMY_SERVITOR: {
                if (target == null || target == activeChar.getPet() || !(target instanceof Summon)) {
                    return null;
                }
                return target;
            }
            case TARGET_ONE: {
                return (target != null && target.isDead() == _isCorpse && (target != activeChar || !isOffensive()) && (!_isUndeadOnly || target.isUndead())) ? target : null;
            }
            case TARGET_OTHER: {
                return (target != null && target != activeChar && target.isDead() == _isCorpse && (!_isUndeadOnly || target.isUndead())) ? target : null;
            }
            case TARGET_AREA:
            case TARGET_MULTIFACE:
            case TARGET_TUNNEL: {
                return (target != null && target.isDead() == _isCorpse && (target != activeChar || !isOffensive()) && (!_isUndeadOnly || target.isUndead())) ? target : null;
            }
            case TARGET_AREA_AIM_CORPSE: {
                return (target != null && target.isDead()) ? target : null;
            }
            case TARGET_CORPSE: {
                if (target == null || !target.isDead()) {
                    return null;
                }
                if (target.isSummon() && target != activeChar.getPet()) {
                    return target;
                }
                return target.isNpc() ? target : null;
            }
            case TARGET_CORPSE_PLAYER: {
                return (target != null && target.isPlayable() && target.isDead()) ? target : null;
            }
            case TARGET_SIEGE: {
                return (target != null && !target.isDead() && target.isDoor()) ? target : null;
            }
            default: {
                activeChar.sendMessage("Target type of skill is not currently handled");
                return null;
            }
        }
    }

    public List<Creature> getTargets(final Creature activeChar, final Creature aimingTarget, final boolean forceUse) {
        if (oneTarget()) {
            return Collections.singletonList(aimingTarget);
        }
        final List<Creature> targets = new CopyOnWriteArrayList<>();
        switch (_targetType) {
            case TARGET_AREA:
            case TARGET_MULTIFACE:
            case TARGET_TUNNEL:
            case TARGET_AREA_AIM_CORPSE: {
                if (aimingTarget.isDead() == _isCorpse && (!_isUndeadOnly || aimingTarget.isUndead())) {
                    targets.add(aimingTarget);
                }
                addTargetsToList(targets, aimingTarget, activeChar, forceUse);
                break;
            }
            case TARGET_AURA:
            case TARGET_MULTIFACE_AURA: {
                addTargetsToList(targets, activeChar, activeChar, forceUse);
                break;
            }
            case TARGET_COMMCHANNEL: {
                if (activeChar.getPlayer() == null) {
                    break;
                }
                if (!activeChar.getPlayer().isInParty()) {
                    targets.add(activeChar);
                    addTargetAndPetToList(targets, activeChar.getPlayer(), activeChar.getPlayer());
                    break;
                }
                if (activeChar.getPlayer().getParty().isInCommandChannel()) {
                    for (final Player p : activeChar.getPlayer().getParty().getCommandChannel()) {
                        if (!p.isDead() && p.isInRange(activeChar, (_skillRadius == 0) ? 600L : ((long) _skillRadius))) {
                            targets.add(p);
                        }
                    }
                    addTargetAndPetToList(targets, activeChar.getPlayer(), activeChar.getPlayer());
                    break;
                }
                for (final Player p : activeChar.getPlayer().getParty().getPartyMembers()) {
                    if (!p.isDead() && p.isInRange(activeChar, (_skillRadius == 0) ? 600L : ((long) _skillRadius))) {
                        targets.add(p);
                    }
                }
                addTargetAndPetToList(targets, activeChar.getPlayer(), activeChar.getPlayer());
                break;
            }
            case TARGET_PET_AURA: {
                if (activeChar.getPet() == null) {
                    break;
                }
                addTargetsToList(targets, activeChar.getPet(), activeChar, forceUse);
                break;
            }
            case TARGET_ALLY:
            case TARGET_CLAN:
            case TARGET_PARTY:
            case TARGET_CLAN_ONLY: {
                if (activeChar.isMonster() || activeChar.isSiegeGuard()) {
                    targets.add(activeChar);
                    for (final Creature c : World.getAroundCharacters(activeChar, _skillRadius, 600)) {
                        if (!c.isDead() && (c.isMonster() || c.isSiegeGuard())) {
                            targets.add(c);
                        }
                    }
                    break;
                }
                final Player player = activeChar.getPlayer();
                if (player == null) {
                    break;
                }
                for (final Player target : World.getAroundPlayers(player, _skillRadius, 600)) {
                    boolean check = false;
                    switch (_targetType) {
                        case TARGET_PARTY: {
                            check = (player.getParty() != null && player.getParty() == target.getParty());
                            break;
                        }
                        case TARGET_CLAN: {
                            check = ((player.getClanId() != 0 && target.getClanId() == player.getClanId()) || (player.getParty() != null && target.getParty() == player.getParty()));
                            break;
                        }
                        case TARGET_CLAN_ONLY: {
                            check = (player.getClanId() != 0 && target.getClanId() == player.getClanId());
                            break;
                        }
                        case TARGET_ALLY: {
                            check = ((player.getClanId() != 0 && target.getClanId() == player.getClanId()) || (player.getAllyId() != 0 && target.getAllyId() == player.getAllyId()));
                            break;
                        }
                    }
                    if (!check) {
                        continue;
                    }
                    if (player.isOlyParticipant() && target.isOlyParticipant() && player.getOlyParticipant() != target.getOlyParticipant()) {
                        continue;
                    }
                    if (checkTarget(player, target, aimingTarget, forceUse, false) != null) {
                        continue;
                    }
                    addTargetAndPetToList(targets, player, target);
                }
                addTargetAndPetToList(targets, player, player);
                break;
            }
        }
        return targets;
    }

    private void addTargetAndPetToList(final List<Creature> targets, final Player actor, final Player target) {
        if ((actor == target || actor.isInRange(target, _skillRadius)) && target.isDead() == _isCorpse) {
            targets.add(target);
        }
        final Summon pet = target.getPet();
        if (actor.isInRange(pet, _skillRadius) && pet.isDead() == _isCorpse) {
            targets.add(pet);
        }
        applyAffectLimit(targets);
    }

    private void addTargetsToList(final List<Creature> targets, final Creature aimingTarget, final Creature activeChar, final boolean forceUse) {
        int count = 0;
        Polygon terr = null;
        if (_targetType == SkillTargetType.TARGET_TUNNEL) {
            final int radius = 100;
            final int zmin1 = activeChar.getZ() - 200;
            final int zmax1 = activeChar.getZ() + 200;
            final int zmin2 = aimingTarget.getZ() - 200;
            final int zmax2 = aimingTarget.getZ() + 200;
            final double angle = PositionUtils.convertHeadingToDegree(activeChar.getHeading());
            final double radian1 = Math.toRadians(angle - 90.0);
            final double radian2 = Math.toRadians(angle + 90.0);
            terr = new Polygon();
            terr.add(activeChar.getX() + (int) (Math.cos(radian1) * radius), activeChar.getY() + (int) (Math.sin(radian1) * radius));
            terr.add(activeChar.getX() + (int) (Math.cos(radian2) * radius), activeChar.getY() + (int) (Math.sin(radian2) * radius));
            terr.add(aimingTarget.getX() + (int) (Math.cos(radian2) * radius), aimingTarget.getY() + (int) (Math.sin(radian2) * radius));
            terr.add(aimingTarget.getX() + (int) (Math.cos(radian1) * radius), aimingTarget.getY() + (int) (Math.sin(radian1) * radius));
            terr.setZmin(Math.min(zmin1, zmin2)).setZmax(Math.max(zmax1, zmax2));
        }
        for (final Creature target : aimingTarget.getAroundCharacters(_skillRadius, 300)) {
            if (terr != null && !terr.isInside(target.getX(), target.getY(), target.getZ())) {
                continue;
            }
            if (target == null || activeChar == target) {
                continue;
            }
            if (activeChar.getPlayer() != null && activeChar.getPlayer() == target.getPlayer()) {
                continue;
            }
            if (checkTarget(activeChar, target, aimingTarget, forceUse, false) != null) {
                continue;
            }
            if (activeChar.isNpc() && target.isNpc()) {
                continue;
            }
            targets.add(target);
            count++;
            if (isOffensive() && count >= 20 && !activeChar.isRaid()) {
                break;
            }
        }
        applyAffectLimit(targets);
    }

    public final void getEffects(final Creature effector, final Creature effected, final boolean calcChance, final boolean applyOnCaster) {
        getEffects(effector, effected, calcChance, applyOnCaster, false);
    }

    public final void getEffects(final Creature effector, final Creature effected, final boolean calcChance, final boolean applyOnCaster, final boolean skillReflected) {
        double timeMult = 1.0;
        if (isMusic()) {
            timeMult = Config.SONGDANCETIME_MODIFIER;
        } else if (getId() >= 4342 && getId() <= 4360) {
            timeMult = Config.CLANHALL_BUFFTIME_MODIFIER;
        }
        getEffects(effector, effected, calcChance, applyOnCaster, 0L, timeMult, skillReflected);
    }

    public final void getEffects(final Creature effector, final Creature effected, final boolean calcChance, final boolean applyOnCaster, final long timeConst, final double timeMult, final boolean skillReflected) {
        if (isPassive() || !hasEffects() || effector == null || effected == null) {
            return;
        }
        boolean isChg = false;
        if (getId() == 345 || getId() == 346 || getId() == 321 || getId() == 369 || getId() == 1231) {
            isChg = (effected == effector);
        }
        if (!isChg && (effected.isEffectImmune() || (effected.isInvul() && isOffensive()))) {
            if (effector.isPlayer()) {
                effector.sendPacket(new SystemMessage(139).addName(effected).addSkillName(_displayId, _displayLevel));
            }
            return;
        }
        if (effected.isDoor() || (effected.isAlikeDead() && !isPreservedOnDeath())) {
            return;
        }
        ThreadPoolManager.getInstance().execute(new RunnableImpl() {
            @Override
            public void runImpl() {
                boolean success = false;
                boolean skillMastery = false;
                final int sps = effector.getChargedSpiritShot();
                if (effector.getSkillMastery(getId()) == 2) {
                    skillMastery = true;
                    effector.removeSkillMastery(getId());
                }
                for (final EffectTemplate et : getEffectTemplates()) {
                    if (applyOnCaster == et._applyOnCaster) {
                        if (et._count != 0) {
                            final Creature character = (et._applyOnCaster || (et._isReflectable && skillReflected)) ? effector : effected;
                            final List<Creature> targets = new ArrayList<>(1);
                            targets.add(character);
                            if (et._applyOnSummon && character.isPlayer()) {
                                final Summon summon = character.getPlayer().getPet();
                                if (summon != null && summon.isSummon() && !isOffensive() && !isToggle() && !isCubicSkill()) {
                                    targets.add(summon);
                                }
                            }
                            Label_0250:
                            for (Creature target : targets) {
                                if (target.isAlikeDead() && !isPreservedOnDeath()) {
                                    continue;
                                }
                                if (target.isRaid() && et.getEffectType().isRaidImmune()) {
                                    continue;
                                }
                                if (et.getPeriod() > 0L && ((effected.isBuffImmune() && !isOffensive() && effector != effected && !Config.BLOCK_BUFF_EXCLUDE.contains(getId())) || (effected.isDebuffImmune() && isOffensive()))) {
                                    if (!effector.isPlayer()) {
                                        continue;
                                    }
                                    effector.sendPacket(new SystemMessage(139).addName(effected).addSkillName(_displayId, _displayLevel));
                                } else {
                                    if (isBlockedByChar(target, et)) {
                                        continue;
                                    }
                                    if (et._stackOrder == -1) {
                                        if (!"none".equals(et._stackType)) {
                                            for (final Effect e : target.getEffectList().getAllEffects()) {
                                                if (e.getStackType().equalsIgnoreCase(et._stackType)) {
                                                    continue Label_0250;
                                                }
                                            }
                                        } else if (target.getEffectList().getEffectsBySkillId(getId()) != null) {
                                            continue;
                                        }
                                    }
                                    final Env env = new Env(effector, target, Skill.this);
                                    final int chance = et.chance(getActivateRate());
                                    if ((calcChance || chance >= 0) && !et._applyOnCaster) {
                                        env.value = chance;
                                        if (!Formulas.calcSkillSuccess(env, et, sps)) {
                                            continue;
                                        }
                                    }
                                    if (_isReflectable && et._isReflectable && isOffensive() && target != effector && !effector.isTrap() && Rnd.chance(target.calcStat(isMagic() ? Stats.REFLECT_MAGIC_DEBUFF : Stats.REFLECT_PHYSIC_DEBUFF, 0.0, effector, Skill.this))) {
                                        target.sendPacket(new SystemMessage(1998).addName(effector));
                                        effector.sendPacket(new SystemMessage(1999).addName(target));
                                        target = effector;
                                        env.target = target;
                                    }
                                    if (success) {
                                        env.value = 2.147483647E9;
                                    }
                                    final Effect e2 = et.getEffect(env);
                                    if (e2 == null) {
                                        continue;
                                    }
                                    if (chance > 0) {
                                        success = true;
                                    }
                                    if (e2.isOneTime()) {
                                        if (!e2.checkCondition()) {
                                            continue;
                                        }
                                        e2.onStart();
                                        e2.onActionTime();
                                        e2.onExit();
                                    } else {
                                        int count = et.getCount();
                                        long period = et.getPeriod();
                                        if (skillMastery) {
                                            if (count > 1) {
                                                count *= 2;
                                            } else {
                                                period *= 2L;
                                            }
                                        }
                                        if (Config.CALC_EFFECT_TIME_YIELD_AND_RESIST && !et._applyOnCaster && isOffensive() && !isIgnoreResists() && !effector.isRaid()) {
                                            double res = 0.0;
                                            final Pair<Stats, Stats> resistAndPowerType = et.getEffectType().getResistAndPowerType();
                                            if (resistAndPowerType != null) {
                                                final Stats resistType = resistAndPowerType.getLeft();
                                                final Stats powerType = resistAndPowerType.getRight();
                                                if (resistType != null) {
                                                    res += effected.calcStat(resistType, effector, Skill.this);
                                                }
                                                if (powerType != null) {
                                                    res -= effector.calcStat(powerType, effected, Skill.this);
                                                }
                                            }
                                            res += effected.calcStat(Stats.DEBUFF_RESIST, effector, Skill.this);
                                            if (res != 0.0) {
                                                double mod = 1.0 + Math.abs(0.01 * res);
                                                if (res > 0.0) {
                                                    mod = 1.0 / mod;
                                                }
                                                if (count > 1) {
                                                    count = (int) Math.floor(Math.max(count * mod, 1.0));
                                                } else {
                                                    period = (long) Math.floor(Math.max(period * mod, 1.0));
                                                }
                                            }
                                        }
                                        if (timeConst > 0L) {
                                            if (count > 1) {
                                                period = timeConst / count;
                                            } else {
                                                period = timeConst;
                                            }
                                        } else if (timeMult > 1.0) {
                                            if (count > 1) {
                                                count *= timeMult;
                                            } else {
                                                period *= timeMult;
                                            }
                                        }
                                        final Skill s = e2.getSkill();
                                        if (s != null && Config.SKILL_DURATION_MOD.containsKey(s.getId())) {
                                            final int mtime = Config.SKILL_DURATION_MOD.get(s.getId());
                                            if (s.getLevel() >= 100 && s.getLevel() < 140) {
                                                if (count > 1) {
                                                    count = mtime;
                                                } else {
                                                    period = mtime;
                                                }
                                            } else if (count > 1) {
                                                count = mtime;
                                            } else {
                                                period = mtime;
                                            }
                                        }
                                        e2.setCount(count);
                                        e2.setPeriod(period);
                                        e2.schedule();
                                    }
                                }
                            }
                        }
                    }
                }
                if (calcChance) {
                    if (success) {
                        effector.sendPacket(new SystemMessage(1595).addSkillName(_displayId, _displayLevel));
                    } else {
                        effector.sendPacket(new SystemMessage(1597).addSkillName(_displayId, _displayLevel));
                    }
                }
            }
        });
    }

    public final void attach(final EffectTemplate effect) {
        _effectTemplates = ArrayUtils.add(_effectTemplates, effect);
    }

    public EffectTemplate[] getEffectTemplates() {
        return _effectTemplates;
    }

    public boolean hasEffects() {
        return _effectTemplates.length > 0;
    }

    public final Func[] getStatFuncs() {
        return getStatFuncs(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || (obj != null && getClass() == obj.getClass() && hashCode() == obj.hashCode());
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    public final void attach(final Condition c) {
        _preCondition = ArrayUtils.add(_preCondition, c);
    }

    public final boolean altUse() {
        return _isAltUse;
    }

    public final boolean canTeachBy(final int npcId) {
        return _teachers == null || _teachers.contains(npcId);
    }

    public final int getActivateRate() {
        return _activateRate;
    }

    public AddedSkill[] getAddedSkills() {
        return _addedSkills.toArray(AddedSkill.EMPTY_ARRAY);
    }

    public final boolean getCanLearn(final ClassId cls) {
        return _canLearn == null || _canLearn.contains(cls);
    }

    public final int getCastRange() {
        return _castRange;
    }

    public void setCastRange(final int castRange) {
        _castRange = castRange;
    }

    public int getEffectiveRange() {
        return _effectiveRange;
    }

    public final int getAOECastRange() {
        return Math.max(_castRange, _skillRadius);
    }

    public final int getCoolTime() {
        return _coolTime;
    }

    public boolean getCorpse() {
        return _isCorpse;
    }

    public int getDelayedEffect() {
        return _delayedEffect;
    }

    public final int getDisplayId() {
        return _displayId;
    }

    public int getDisplayLevel() {
        return _displayLevel;
    }

    public void setDisplayLevel(final int lvl) {
        _displayLevel = lvl;
    }

    public int getEffectPoint() {
        return _effectPoint;
    }

    public Effect getSameByStackType(final List<Effect> list) {
        for (final EffectTemplate et : getEffectTemplates()) {
            final Effect ret;
            if (et != null && (ret = et.getSameByStackType(list)) != null) {
                return ret;
            }
        }
        return null;
    }

    public Effect getSameByStackType(final EffectList list) {
        return getSameByStackType(list.getAllEffects());
    }

    public Effect getSameByStackType(final Creature actor) {
        return getSameByStackType(actor.getEffectList().getAllEffects());
    }

    public final Element getElement() {
        return _element;
    }

    public final int getElementPower() {
        return _elementPower;
    }

    public Skill getFirstAddedSkill() {
        if (_addedSkills.size() == 0) {
            return null;
        }
        return _addedSkills.get(0).getSkill();
    }

    public int getFlyRadius() {
        return _flyRadius;
    }

    public FlyType getFlyType() {
        return _flyType;
    }

    public boolean isFlyToBack() {
        return _flyToBack;
    }

    public final int getHitTime() {
        return _hitTime;
    }

    public void setHitTime(final int hitTime) {
        _hitTime = hitTime;
    }

    public final int getHpConsume() {
        return _hpConsume;
    }

    public void setHpConsume(final int hpConsume) {
        _hpConsume = hpConsume;
    }

    public int getId() {
        return _id;
    }

    public void setId(final int id) {
        _id = id;
    }

    public final int[] getItemConsume() {
        return _itemConsume;
    }

    public final int[] getItemConsumeId() {
        return _itemConsumeId;
    }

    public final int getReferenceItemId() {
        return _referenceItemId;
    }

    public final int getReferenceItemMpConsume() {
        return _referenceItemMpConsume;
    }

    public final int getLevel() {
        return _level;
    }

    public final int getBaseLevel() {
        return _baseLevel;
    }

    public final void setBaseLevel(final int baseLevel) {
        _baseLevel = baseLevel;
    }

    public final int getLevelModifier() {
        return _levelModifier;
    }

    public final int getMagicLevel() {
        return _magicLevel;
    }

    public final void setMagicLevel(final int newlevel) {
        _magicLevel = newlevel;
    }

    public int getMatak() {
        return _matak;
    }

    public int getMinPledgeClass() {
        return _minPledgeClass;
    }

    public int getMinRank() {
        return _minRank;
    }

    public final double getMpConsume() {
        return _mpConsume1 + _mpConsume2;
    }

    public final double getMpConsume1() {
        return _mpConsume1;
    }

    public void setMpConsume1(final double mpConsume1) {
        _mpConsume1 = mpConsume1;
    }

    public final double getMpConsume2() {
        return _mpConsume2;
    }

    public void setMpConsume2(final double mpConsume2) {
        _mpConsume2 = mpConsume2;
    }

    public final String getName() {
        return _name;
    }

    public void setName(final String name) {
        _name = name;
    }

    public int getNegatePower() {
        return _negatePower;
    }

    public int getNegateSkill() {
        return _negateSkill;
    }

    public SkillNextAction getSkillNextAction() {
        return _skillNextAction;
    }

    public int getNpcId() {
        return _npcId;
    }

    public int getNumCharges() {
        return _numCharges;
    }

    public final double getPower(final Creature target) {
        if (target != null) {
            if (target.isPlayable()) {
                return getPowerPvP();
            }
            if (target.isMonster()) {
                return getPowerPvE();
            }
        }
        return getPower();
    }

    public final double getPower() {
        return _power;
    }

    public final void setPower(final double power) {
        _power = power;
    }

    public final double getPowerPvP() {
        return (_powerPvP != 0.0) ? _powerPvP : _power;
    }

    public final double getPowerPvE() {
        return (_powerPvE != 0.0) ? _powerPvE : _power;
    }

    public final long getReuseDelay() {
        return _reuseDelay;
    }

    public final void setReuseDelay(final long newReuseDelay) {
        _reuseDelay = newReuseDelay;
    }

    public final boolean getShieldIgnore() {
        return _isShieldignore;
    }

    public final boolean isReflectable() {
        return _isReflectable;
    }

    public final int getSkillInterruptTime() {
        return _skillInterruptTime;
    }

    public void setSkillInterruptTime(final int skillInterruptTime) {
        _skillInterruptTime = skillInterruptTime;
    }

    public final int getSkillRadius() {
        return _skillRadius;
    }

    public final SkillType getSkillType() {
        return _skillType;
    }

    public int getSymbolId() {
        return _symbolId;
    }

    public final SkillTargetType getTargetType() {
        return _targetType;
    }

    public final SkillTrait getTraitType() {
        return _traitType;
    }

    public final BaseStats getSaveVs() {
        return _saveVs;
    }

    public final int getWeaponsAllowed() {
        return _weaponsAllowed;
    }

    public double getLethal1() {
        return _lethal1;
    }

    public double getLethal2() {
        return _lethal2;
    }

    public String getBaseValues() {
        return _baseValues;
    }

    public boolean isBlockedByChar(final Creature effected, final EffectTemplate et) {
        if (et.getAttachedFuncs() == null) {
            return false;
        }
        return Arrays.stream(et.getAttachedFuncs()).anyMatch(func -> func != null && effected.checkBlockedStat(func.getStat()));
    }

    public final boolean isCancelable() {
        return _isCancelable && getSkillType() != SkillType.TRANSFORMATION && !isToggle();
    }

    public final boolean isCommon() {
        return _isCommon;
    }

    public final int getCriticalRate() {
        return _criticalRate;
    }

    public final boolean isHandler() {
        return _isItemHandler;
    }

    public final boolean isMagic() {
        return _magicType == SkillMagicType.MAGIC;
    }

    public final SkillMagicType getMagicType() {
        return _magicType;
    }

    public void setMagicType(final SkillMagicType type) {
        _magicType = type;
    }

    public final boolean isNewbie() {
        return _isNewbie;
    }

    public final boolean isPreservedOnDeath() {
        return _isPreservedOnDeath;
    }

    public final boolean isHeroic() {
        return _isHeroic;
    }

    public final boolean isSelfDispellable() {
        return _isSelfDispellable;
    }

    public void setOperateType(final SkillOpType type) {
        _operateType = type;
    }

    public final boolean isOverhit() {
        return _isOverhit;
    }

    public void setOverhit(final boolean isOverhit) {
        _isOverhit = isOverhit;
    }

    public final boolean isActive() {
        return _operateType == SkillOpType.OP_ACTIVE;
    }

    public final boolean isPassive() {
        return _operateType == SkillOpType.OP_PASSIVE;
    }

    public boolean isSaveable() {
        return (Config.ALT_SAVE_UNSAVEABLE || (!isMusic() && !_name.startsWith("Herb of"))) && _isSaveable;
    }

    public final boolean isSkillTimePermanent() {
        return _isSkillTimePermanent || _isItemHandler || _name.contains("Talisman");
    }

    public final boolean isReuseDelayPermanent() {
        return _isReuseDelayPermanent || _isItemHandler;
    }

    public boolean isDeathlink() {
        return _deathlink;
    }

    public boolean isBasedOnTargetDebuff() {
        return _basedOnTargetDebuff;
    }

    public boolean isChargeBoost() {
        return _isChargeBoost;
    }

    public boolean isUsingWhileCasting() {
        return _isUsingWhileCasting;
    }

    public boolean isBehind() {
        return _isBehind;
    }

    public boolean isHideStartMessage() {
        return _hideStartMessage;
    }

    public boolean isHideUseMessage() {
        return _hideUseMessage;
    }

    public boolean isSSPossible() {
        return _isUseSS == Ternary.TRUE || (_isUseSS == Ternary.DEFAULT && !_isItemHandler && !isMusic() && isActive() && (getTargetType() != SkillTargetType.TARGET_SELF || isMagic()));
    }

    public final boolean isSuicideAttack() {
        return _isSuicideAttack;
    }

    public final boolean isToggle() {
        return _operateType == SkillOpType.OP_TOGGLE;
    }

    public boolean isItemSkill() {
        return _name.contains("Item Skill") || _name.contains("Talisman");
    }

    @Override
    public String toString() {
        return _name + "[id=" + _id + ",lvl=" + _level + "]";
    }

    public abstract void useSkill(final Creature p0, final List<Creature> p1);

    public boolean isAoE() {
        switch (_targetType) {
            case TARGET_AURA:
            case TARGET_MULTIFACE_AURA:
            case TARGET_PET_AURA:
            case TARGET_AREA:
            case TARGET_MULTIFACE:
            case TARGET_TUNNEL:
            case TARGET_AREA_AIM_CORPSE: {
                return true;
            }
            default: {
                return false;
            }
        }
    }

    public boolean isNotTargetAoE() {
        switch (_targetType) {
            case TARGET_ALLY:
            case TARGET_CLAN:
            case TARGET_PARTY:
            case TARGET_CLAN_ONLY:
            case TARGET_AURA:
            case TARGET_MULTIFACE_AURA: {
                return true;
            }
            default: {
                return false;
            }
        }
    }

    public boolean isAllowedForTeamate() {
        switch (_skillType) {
            case BUFF:
            case BUFF_CHARGER:
            case HEAL:
            case HEAL_PERCENT:
            case MANAHEAL:
            case MANAHEAL_PERCENT:
            case COMBATPOINTHEAL:
            case BALANCE:
                return true;
            default:
                return false;
        }
    }

    public boolean isOffensive() {
        return _isOffensive;
    }

    public final boolean isForceUse() {
        return _isForceUse;
    }

    public boolean isAI() {
        return _skillType.isAI();
    }

    public boolean isPvM() {
        return _isPvm;
    }

    public final boolean isPvpSkill() {
        return _isPvpSkill;
    }

    public final boolean isFishingSkill() {
        return _isFishingSkill;
    }

    public boolean isMusic() {
        return _magicType == SkillMagicType.MUSIC;
    }

    public boolean isTrigger() {
        return _isTrigger;
    }

    public boolean isSlotNone() {
        return _isSlotNone;
    }

    public boolean oneTarget() {
        switch (_targetType) {
            case TARGET_SELF:
            case TARGET_HOLY:
            case TARGET_FLAGPOLE:
            case TARGET_UNLOCKABLE:
            case TARGET_CHEST:
            case TARGET_FEEDABLE_BEAST:
            case TARGET_PET:
            case TARGET_OWNER:
            case TARGET_ENEMY_PET:
            case TARGET_ENEMY_SUMMON:
            case TARGET_ENEMY_SERVITOR:
            case TARGET_ONE:
            case TARGET_OTHER:
            case TARGET_CORPSE:
            case TARGET_CORPSE_PLAYER:
            case TARGET_SIEGE:
            case TARGET_ITEM:
            case TARGET_NONE: {
                return true;
            }
            default: {
                return false;
            }
        }
    }

    public int getCancelTarget() {
        return _cancelTarget;
    }

    public boolean isSkillInterrupt() {
        return _skillInterrupt;
    }

    public boolean isNotUsedByAI() {
        return _isNotUsedByAI;
    }

    public boolean isIgnoreResists() {
        return _isIgnoreResists;
    }

    public boolean isIgnoreInvul() {
        return _isIgnoreInvul;
    }

    public boolean isSharedClassReuse() {
        return _isSharedClassReuse;
    }

    public boolean isNotAffectedByMute() {
        return _isNotAffectedByMute;
    }

    public boolean flyingTransformUsage() {
        return _flyingTransformUsage;
    }

    public boolean canUseTeleport() {
        return _canUseTeleport;
    }

    public int getEnchantLevelCount() {
        return _enchantLevelCount;
    }

    public void setEnchantLevelCount(final int count) {
        _enchantLevelCount = count;
    }

    public boolean isClanSkill() {
        return (_id >= 370 && _id <= 391) || (_id >= 611 && _id <= 616);
    }

    public boolean isBaseTransformation() {
        return (_id >= 810 && _id <= 813) || (_id >= 1520 && _id <= 1522) || _id == 538;
    }

    public boolean isSummonerTransformation() {
        return _id >= 929 && _id <= 931;
    }

    public double getSimpleDamage(final Creature attacker, final Creature target) {
        if (isMagic()) {
            final double mAtk = attacker.getMAtk(target, this);
            final double mdef = target.getMDef(null, this);
            final double power = getPower();
            final int sps = (attacker.getChargedSpiritShot() > 0 && isSSPossible()) ? (attacker.getChargedSpiritShot() * 2) : 1;
            return 91.0 * power * Math.sqrt(sps * mAtk) / mdef;
        }
        final double pAtk = attacker.getPAtk(target);
        final double pdef = target.getPDef(attacker);
        final double power = getPower();
        final int ss = (attacker.getChargedSoulShot() && isSSPossible()) ? 2 : 1;
        return ss * (pAtk + power) * 70.0 / pdef;
    }

    public long getReuseForMonsters() {
        long min = 1000L;
        switch (_skillType) {
            case DEBUFF:
            case PARALYZE:
            case NEGATE_STATS:
            case NEGATE_EFFECTS:
            case STEAL_BUFF: {
                min = 10000L;
                break;
            }
            case MUTE:
            case ROOT:
            case SLEEP:
            case STUN: {
                min = 5000L;
                break;
            }
        }
        return Math.max(Math.max(_hitTime + _coolTime, _reuseDelay), min);
    }

    private void applyAffectLimit(List<Creature> targets) {
        int limit = getAffectLimit();
        if (limit < 1) {
            return;
        }

        if (targets.size() < limit) {
            return;
        }

        while (targets.size() > limit) {
            targets.remove(Rnd.get(targets.size()));
        }
    }

    public int getAffectLimit() {
        if (_affectLimit[0] > 0 || _affectLimit[1] > 0) {
            return (_affectLimit[0] + Rnd.get(_affectLimit[1]));
        }
        return 0;
    }

    public double getAbsorbPart() {
        return _absorbPart;
    }

    public boolean isProvoke() {
        return _isProvoke;
    }

    public String getIcon() {
        return _icon;
    }

    public int getEnergyConsume() {
        return _energyConsume;
    }

    public boolean isCubicSkill() {
        return _isCubicSkill;
    }

    public void setCubicSkill(final boolean value) {
        _isCubicSkill = value;
    }

    public boolean isBlowSkill() {
        return _isBlow;
    }

    public enum SkillNextAction {
        ATTACK,
        CAST,
        DEFAULT,
        MOVE,
        NONE
    }

    public enum SkillOpType {
        OP_ACTIVE,
        OP_PASSIVE,
        OP_TOGGLE
    }

    public enum Ternary {
        TRUE,
        FALSE,
        DEFAULT
    }

    public enum SkillMagicType {
        PHYSIC,
        MAGIC,
        SPECIAL,
        MUSIC
    }

    public enum SkillTargetType {
        TARGET_ALLY,
        TARGET_AREA,
        TARGET_AREA_AIM_CORPSE,
        TARGET_AURA,
        TARGET_PET_AURA,
        TARGET_CHEST,
        TARGET_FEEDABLE_BEAST,
        TARGET_CLAN,
        TARGET_CLAN_ONLY,
        TARGET_CORPSE,
        TARGET_CORPSE_PLAYER,
        TARGET_ENEMY_PET,
        TARGET_ENEMY_SUMMON,
        TARGET_ENEMY_SERVITOR,
        TARGET_FLAGPOLE,
        TARGET_COMMCHANNEL,
        TARGET_HOLY,
        TARGET_ITEM,
        TARGET_MULTIFACE,
        TARGET_MULTIFACE_AURA,
        TARGET_TUNNEL,
        TARGET_NONE,
        TARGET_ONE,
        TARGET_OTHER,
        TARGET_OWNER,
        TARGET_PARTY,
        TARGET_PET,
        TARGET_SELF,
        TARGET_SIEGE,
        TARGET_UNLOCKABLE
    }

    public enum SkillType {
        AGATHION(Agathion.class),
        AGGRESSION(Aggression.class),
        AIEFFECTS(AIeffects.class),
        BALANCE(Balance.class),
        BEAST_FEED(BeastFeed.class),
        BLEED(Continuous.class),
        BUFF(Continuous.class),
        BUFF_CHARGER(BuffCharger.class),
        CALL(Call.class),
        CLAN_GATE(ClanGate.class),
        COMBATPOINTHEAL(CombatPointHeal.class),
        CONT(Toggle.class),
        CPDAM(CPDam.class),
        CPHOT(Continuous.class),
        CRAFT(Craft.class),
        DEATH_PENALTY(DeathPenalty.class),
        DEBUFF(Continuous.class),
        DELETE_HATE(DeleteHate.class),
        DELETE_HATE_OF_ME(DeleteHateOfMe.class),
        DESTROY_SUMMON(DestroySummon.class),
        DEFUSE_TRAP(DefuseTrap.class),
        DETECT_TRAP(DetectTrap.class),
        DISCORD(Continuous.class),
        DOT(Continuous.class),
        DRAIN(Drain.class),
        DRAIN_SOUL(DrainSoul.class),
        EFFECT(ru.j2dev.gameserver.skills.skillclasses.Effect.class),
        EFFECTS_FROM_SKILLS(EffectsFromSkills.class),
        ENCHANT_ARMOR,
        ENCHANT_WEAPON,
        FEED_PET,
        FISHING(FishingSkill.class),
        HARDCODED(ru.j2dev.gameserver.skills.skillclasses.Effect.class),
        HARVESTING(Harvesting.class),
        HEAL(Heal.class),
        HEAL_PERCENT(HealPercent.class),
        HOT(Continuous.class),
        LETHAL_SHOT(LethalShot.class),
        LUCK,
        MANADAM(ManaDam.class),
        MANAHEAL(ManaHeal.class),
        MANAHEAL_PERCENT(ManaHealPercent.class),
        MDAM(MDam.class),
        MDOT(Continuous.class),
        MPHOT(Continuous.class),
        MUTE(Disablers.class),
        NEGATE_EFFECTS(NegateEffects.class),
        NEGATE_STATS(NegateStats.class),
        ADD_PC_BANG(PcBangPointsAdd.class),
        NOTDONE,
        NOTUSED,
        PARALYZE(Disablers.class),
        PASSIVE,
        PDAM(PDam.class),
        PET_SUMMON(PetSummon.class),
        POISON(Continuous.class),
        PUMPING(ReelingPumping.class),
        RECALL(Recall.class),
        REELING(ReelingPumping.class),
        RESURRECT(Resurrect.class),
        RIDE(Ride.class),
        ROOT(Disablers.class),
        SHIFT_AGGRESSION(ShiftAggression.class),
        SSEED(SkillSeed.class),
        SLEEP(Disablers.class),
        SOULSHOT,
        SOWING(Sowing.class),
        SPHEAL(SPHeal.class),
        SPIRITSHOT,
        SPOIL(Spoil.class),
        STEAL_BUFF(StealBuff.class),
        STUN(Disablers.class),
        SUMMON(ru.j2dev.gameserver.skills.skillclasses.Summon.class),
        SUMMON_FLAG(SummonSiegeFlag.class),
        SUMMON_ITEM(SummonItem.class),
        SWEEP(Sweep.class),
        TAKECASTLE(TakeCastle.class),
        TAMECONTROL(TameControl.class),
        TELEPORT_NPC(TeleportNpc.class),
        TELEPORT_TO_LOC(TeleportToLoc.class),
        TRANSFORMATION(Transformation.class),
        UNLOCK(Unlock.class),
        WATCHER_GAZE(Continuous.class);

        private final Class<? extends Skill> clazz;

        SkillType() {
            clazz = Default.class;
        }

        SkillType(final Class<? extends Skill> clazz) {
            this.clazz = clazz;
        }

        public Skill makeSkill(final StatsSet set) {
            try {
                final Constructor<? extends Skill> c = clazz.getConstructor(StatsSet.class);
                return c.newInstance(set);
            } catch (Exception e) {
                LOGGER.error("", e);
                throw new RuntimeException(e);
            }
        }

        public final boolean isPvM() {
            switch (this) {
                case DISCORD: {
                    return true;
                }
                default: {
                    return false;
                }
            }
        }

        public boolean isAI() {
            switch (this) {
                case AGGRESSION:
                case AIEFFECTS:
                case SOWING:
                case DELETE_HATE:
                case DELETE_HATE_OF_ME: {
                    return true;
                }
                default: {
                    return false;
                }
            }
        }

        public final boolean isPvpSkill() {
            switch (this) {
                case AGGRESSION:
                case DELETE_HATE:
                case DELETE_HATE_OF_ME:
                case BLEED:
                case DEBUFF:
                case DOT:
                case MDOT:
                case MUTE:
                case PARALYZE:
                case POISON:
                case ROOT:
                case SLEEP:
                case MANADAM:
                case DESTROY_SUMMON:
                case NEGATE_STATS:
                case NEGATE_EFFECTS:
                case STEAL_BUFF: {
                    return true;
                }
                default: {
                    return false;
                }
            }
        }

        public boolean isOffensive() {
            switch (this) {
                case DISCORD:
                case AGGRESSION:
                case AIEFFECTS:
                case SOWING:
                case DELETE_HATE:
                case DELETE_HATE_OF_ME:
                case BLEED:
                case DEBUFF:
                case DOT:
                case MDOT:
                case MUTE:
                case PARALYZE:
                case POISON:
                case ROOT:
                case SLEEP:
                case MANADAM:
                case DESTROY_SUMMON:
                case STEAL_BUFF:
                case DRAIN:
                case DRAIN_SOUL:
                case LETHAL_SHOT:
                case MDAM:
                case PDAM:
                case CPDAM:
                case SOULSHOT:
                case SPIRITSHOT:
                case SPOIL:
                case STUN:
                case SWEEP:
                case HARVESTING:
                case TELEPORT_NPC: {
                    return true;
                }
                default: {
                    return false;
                }
            }
        }
    }

    public static class AddedSkill {
        public static final AddedSkill[] EMPTY_ARRAY = new AddedSkill[0];

        public int id;
        public int level;
        private Skill _skill;

        public AddedSkill(final int id, final int level) {
            this.id = id;
            this.level = level;
        }

        public Skill getSkill() {
            if (_skill == null) {
                _skill = SkillTable.getInstance().getInfo(id, level);
            }
            return _skill;
        }
    }
}
