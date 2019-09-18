package ru.j2dev.gameserver.templates.npc;

import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.util.TroveUtils;
import ru.j2dev.gameserver.ai.CharacterAI;
import ru.j2dev.gameserver.idfactory.IdFactory;
import ru.j2dev.gameserver.listener.hooks.ListenerHook;
import ru.j2dev.gameserver.listener.hooks.ListenerHookType;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.Skill.SkillTargetType;
import ru.j2dev.gameserver.model.Skill.SkillType;
import ru.j2dev.gameserver.model.TeleportLocation;
import ru.j2dev.gameserver.model.base.ClassId;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.instances.RaidBossInstance;
import ru.j2dev.gameserver.model.instances.ReflectionBossInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestEventType;
import ru.j2dev.gameserver.model.reward.RewardList;
import ru.j2dev.gameserver.model.reward.RewardType;
import ru.j2dev.gameserver.scripts.Scripts;
import ru.j2dev.gameserver.skills.effects.EffectTemplate;
import ru.j2dev.gameserver.templates.CharTemplate;
import ru.j2dev.gameserver.templates.StatsSet;

import java.lang.reflect.Constructor;
import java.util.*;

public final class NpcTemplate extends CharTemplate {
    @SuppressWarnings("unchecked")
    public static final Constructor<NpcInstance> DEFAULT_TYPE_CONSTRUCTOR = (Constructor<NpcInstance>) NpcInstance.class.getConstructors()[0];
    @SuppressWarnings("unchecked")
    public static final Constructor<CharacterAI> DEFAULT_AI_CONSTRUCTOR = (Constructor<CharacterAI>) CharacterAI.class.getConstructors()[0];
    private static final Logger LOGGER = LoggerFactory.getLogger(NpcTemplate.class);
    public final int npcId;
    public final String name;
    public final String title;
    public final int level;
    public final long rewardExp;
    public final int rewardSp;
    public final int rewardRp;
    public final int aggroRange;
    public final int rhand;
    public final int lhand;
    public final double rateHp;
    public final String jClass;
    public final int displayId;
    public final ShotsType shots;
    private final StatsSet _AIParams;
    private final int _castleId;
    private final String _htmRoot;
    public boolean isRaid;
    private Faction faction = Faction.NONE;
    private int race;
    private Map<RewardType, RewardList> _rewards = Collections.emptyMap();
    private Map<Integer, TeleportLocation[]> _teleportList = Collections.emptyMap();
    private List<MinionData> _minions = Collections.emptyList();
    private List<AbsorbInfo> _absorbInfo = Collections.emptyList();
    private List<ClassId> _teachInfo = Collections.emptyList();
    private Map<QuestEventType, Quest[]> _questEvents = Collections.emptyMap();
    private TIntObjectHashMap<Skill> _skills = TroveUtils.emptyIntObjectMap();
    private Skill[] _damageSkills = Skill.EMPTY_ARRAY;
    private Skill[] _dotSkills = Skill.EMPTY_ARRAY;
    private Skill[] _debuffSkills = Skill.EMPTY_ARRAY;
    private Skill[] _buffSkills = Skill.EMPTY_ARRAY;
    private Skill[] _stunSkills = Skill.EMPTY_ARRAY;
    private Skill[] _healSkills = Skill.EMPTY_ARRAY;
    private Class<NpcInstance> _classType = NpcInstance.class;
    private Constructor<NpcInstance> _constructorType = DEFAULT_TYPE_CONSTRUCTOR;
    private Class<CharacterAI> _classAI = CharacterAI.class;
    private Constructor<CharacterAI> _constructorAI = DEFAULT_AI_CONSTRUCTOR;

    private long corpse_time;
    private int noSleepMode;
    private final Map<ListenerHookType, Set<ListenerHook>> _listenerHooks = new HashMap<>();

    public NpcTemplate(final StatsSet set) {
        super(set);
        npcId = set.getInteger("npcId");
        displayId = set.getInteger("displayId");
        name = set.getString("name");
        title = set.getString("title");
        level = set.getInteger("level");
        rewardExp = set.getLong("rewardExp");
        rewardSp = set.getInteger("rewardSp");
        rewardRp = set.getInteger("rewardRp");
        aggroRange = set.getInteger("aggroRange");
        rhand = set.getInteger("rhand", 0);
        lhand = set.getInteger("lhand", 0);
        rateHp = set.getDouble("baseHpRate");
        jClass = set.getString("texture", null);
        _htmRoot = set.getString("htm_root", null);
        shots = set.getEnum("shots", ShotsType.class, ShotsType.NONE);
        _castleId = set.getInteger("castle_id", 0);
        _AIParams = (StatsSet) set.getObject("aiParams", StatsSet.EMPTY);
        setType(set.getString("type", null));
        setAI(set.getString("ai_type", null));
        setCorpseTime(set.getLong("corpse_time", 7) * 1000);
        noSleepMode = set.getInteger("noSleepMode", 0);
    }

    public Class<? extends NpcInstance> getInstanceClass() {
        return _classType;
    }

    public Constructor<? extends NpcInstance> getInstanceConstructor() {
        return _constructorType;
    }

    public boolean isInstanceOf(final Class<?> _class) {
        return _class.isAssignableFrom(_classType);
    }

    public NpcInstance getNewInstance() {
        try {
            return _constructorType.newInstance(IdFactory.getInstance().getNextId(), this);
        } catch (Exception e) {
            LOGGER.error("Unable to create instance of NPC " + npcId, e);
            return null;
        }
    }

    public CharacterAI getNewAI(final NpcInstance npc) {
        try {
            return _constructorAI.newInstance(npc);
        } catch (Exception e) {
            LOGGER.error("Unable to create ai of NPC " + npcId, e);
            return new CharacterAI(npc);
        }
    }

    @SuppressWarnings("unchecked")
    private void setType(final String type) {
        Class<NpcInstance> classType;
        try {
            classType = (Class<NpcInstance>) Class.forName("ru.j2dev.gameserver.model.instances." + type + "Instance");
        }
        catch(ClassNotFoundException e) {
            classType = (Class<NpcInstance>) Scripts.getInstance().getClasses().get("npc.model." + type + "Instance");
        }

        if(classType == null) {
            LOGGER.error("Not found type class for type: " + type + ". NpcId: " + npcId);
        } else if(npcId == 0) { //temp
            try {
                classType = (Class<NpcInstance>) Class.forName("ru.j2dev.gameserver.model.instances.NpcInstance");
            } catch(ClassNotFoundException e) {
                e.printStackTrace();
            }
            _classType = classType;
            _constructorType = (Constructor<NpcInstance>) _classType.getConstructors()[0];
        } else {
            _classType = classType;
            _constructorType = (Constructor<NpcInstance>) _classType.getConstructors()[0];
        }

        if(_classType.isAnnotationPresent(Deprecated.class)) {
            LOGGER.error("Npc type: " + type + ", is deprecated. NpcId: " + npcId);
        }
        //TODO [G1ta0] сделать поле в соотвествующих классах
        isRaid = (isInstanceOf(RaidBossInstance.class) && !isInstanceOf(ReflectionBossInstance.class));
    }

    @SuppressWarnings("unchecked")
    private void setAI(final String ai) {
        Class<CharacterAI> classAI;
        try {
            classAI = (Class<CharacterAI>) Class.forName("ru.j2dev.gameserver.ai." + ai);
        } catch (ClassNotFoundException e) {
            classAI = (Class<CharacterAI>) Scripts.getInstance().getClasses().get("ai." + ai);
        }
        if (classAI == null) {
            LOGGER.error("Not found ai class for ai: " + ai + ". NpcId: " + npcId);
        } else {
            _classAI = classAI;
            _constructorAI = (Constructor<CharacterAI>) _classAI.getConstructors()[0];
        }
        if (_classAI.isAnnotationPresent(Deprecated.class)) {
            LOGGER.error("Ai type: " + ai + ", is deprecated. NpcId: " + npcId);
        }
    }

    public void addTeachInfo(final ClassId classId) {
        if (_teachInfo.isEmpty()) {
            _teachInfo = new ArrayList<>(1);
        }
        _teachInfo.add(classId);
    }

    public List<ClassId> getTeachInfo() {
        return _teachInfo;
    }

    public boolean canTeach(final ClassId classId) {
        return _teachInfo.contains(classId);
    }

    public void addTeleportList(final int id, final TeleportLocation[] list) {
        if (_teleportList.isEmpty()) {
            _teleportList = new HashMap<>(1);
        }
        _teleportList.put(id, list);
    }

    public TeleportLocation[] getTeleportList(final int id) {
        return _teleportList.get(id);
    }

    public Map<Integer, TeleportLocation[]> getTeleportList() {
        return _teleportList;
    }

    public void putRewardList(final RewardType rewardType, final RewardList list) {
        if (_rewards.isEmpty()) {
            _rewards = new HashMap<>(RewardType.values().length);
        }
        _rewards.put(rewardType, list);
    }

    public RewardList getRewardList(final RewardType t) {
        return _rewards.get(t);
    }

    public Map<RewardType, RewardList> getRewards() {
        return _rewards;
    }

    public void addAbsorbInfo(final AbsorbInfo absorbInfo) {
        if (_absorbInfo.isEmpty()) {
            _absorbInfo = new ArrayList<>(1);
        }
        _absorbInfo.add(absorbInfo);
    }

    public void addMinion(final MinionData minion) {
        if (_minions.isEmpty()) {
            _minions = new ArrayList<>(1);
        }
        _minions.add(minion);
    }

    public Faction getFaction() {
        return faction;
    }

    public void setFaction(final Faction faction) {
        this.faction = faction;
    }

    public void addSkill(final Skill skill) {
        if (_skills.isEmpty()) {
            _skills = new TIntObjectHashMap<>();
        }
        _skills.put(skill.getId(), skill);
        if (skill.isNotUsedByAI() || skill.getTargetType() == SkillTargetType.TARGET_NONE || skill.getSkillType() == SkillType.NOTDONE || !skill.isActive()) {
            return;
        }
        switch (skill.getSkillType()) {
            case PDAM:
            case MANADAM:
            case MDAM:
            case DRAIN:
            case DRAIN_SOUL: {
                boolean added = false;
                if (skill.hasEffects()) {
                    for (final EffectTemplate eff : skill.getEffectTemplates()) {
                        switch (eff.getEffectType()) {
                            case Stun: {
                                _stunSkills = ArrayUtils.add(_stunSkills, skill);
                                added = true;
                                break;
                            }
                            case DamOverTime:
                            case DamOverTimeLethal:
                            case ManaDamOverTime:
                            case LDManaDamOverTime: {
                                _dotSkills = ArrayUtils.add(_dotSkills, skill);
                                added = true;
                                break;
                            }
                        }
                    }
                }
                if (!added) {
                    _damageSkills = ArrayUtils.add(_damageSkills, skill);
                    break;
                }
                break;
            }
            case DOT:
            case MDOT:
            case POISON:
            case BLEED: {
                _dotSkills = ArrayUtils.add(_dotSkills, skill);
                break;
            }
            case DEBUFF:
            case SLEEP:
            case ROOT:
            case PARALYZE:
            case MUTE:
            case TELEPORT_NPC:
            case AGGRESSION: {
                _debuffSkills = ArrayUtils.add(_debuffSkills, skill);
                break;
            }
            case BUFF: {
                _buffSkills = ArrayUtils.add(_buffSkills, skill);
                break;
            }
            case STUN: {
                _stunSkills = ArrayUtils.add(_stunSkills, skill);
                break;
            }
            case HEAL:
            case HEAL_PERCENT:
            case HOT: {
                _healSkills = ArrayUtils.add(_healSkills, skill);
                break;
            }
        }
    }

    public Skill[] getDamageSkills() {
        return _damageSkills;
    }

    public Skill[] getDotSkills() {
        return _dotSkills;
    }

    public Skill[] getDebuffSkills() {
        return _debuffSkills;
    }

    public Skill[] getBuffSkills() {
        return _buffSkills;
    }

    public Skill[] getStunSkills() {
        return _stunSkills;
    }

    public Skill[] getHealSkills() {
        return _healSkills;
    }

    public List<MinionData> getMinionData() {
        return _minions;
    }

    public TIntObjectHashMap<Skill> getSkills() {
        return _skills;
    }

    public void addQuestEvent(final QuestEventType EventType, final Quest q) {
        if (_questEvents.isEmpty()) {
            _questEvents = new HashMap<>();
        }
        if (_questEvents.get(EventType) == null) {
            _questEvents.put(EventType, new Quest[]{q});
        } else {
            final Quest[] _quests = _questEvents.get(EventType);
            final int len = _quests.length;
            final Quest[] tmp = new Quest[len + 1];
            for (int i = 0; i < len; ++i) {
                if (_quests[i].getName().equals(q.getName())) {
                    _quests[i] = q;
                    return;
                }
                tmp[i] = _quests[i];
            }
            tmp[len] = q;
            _questEvents.put(EventType, tmp);
        }
    }

    public Quest[] getEventQuests(final QuestEventType EventType) {
        return _questEvents.get(EventType);
    }

    public int getRace() {
        return race;
    }

    public void setRace(final int newrace) {
        race = newrace;
    }

    public boolean isUndead() {
        return race == 1;
    }

    @Override
    public String toString() {
        return "Npc template " + name + "[" + npcId + "]";
    }

    @Override
    public int getNpcId() {
        return npcId;
    }

    public String getName() {
        return name;
    }

    public final String getJClass() {
        return jClass;
    }

    public final StatsSet getAIParams() {
        return _AIParams;
    }

    public List<AbsorbInfo> getAbsorbInfo() {
        return _absorbInfo;
    }

    public int getCastleId() {
        return _castleId;
    }

    public Map<QuestEventType, Quest[]> getQuestEvents() {
        return _questEvents;
    }

    public String getHtmRoot() {
        return _htmRoot;
    }

    public int getNoSleepMode() {
        return noSleepMode;
    }

    /**
     * Возвращает время которое моб лежит на земле после смерти (милисекунды)
     *
     * @return Возвращает время которое моб лежит на земле после смерти (милисекунды)
     */
    public long getCorpseTime() {
        return corpse_time;
    }

    /**
     * Указывает время которое моб лежит на земле после смерти (указывать в милисекундах)
     *
     * @param _corpse_time - время которое моб еще лежит на земле после смерти указываеться в милисекундах;
     */
    public void setCorpseTime(final long _corpse_time) {
        corpse_time = _corpse_time;
    }

    public enum ShotsType {
        NONE,
        SOUL,
        SPIRIT,
        BSPIRIT,
        SOUL_SPIRIT,
        SOUL_BSPIRIT
    }

    public void addListenerHook(ListenerHookType type, ListenerHook hook)
    {
        Set<ListenerHook> hooks = _listenerHooks.computeIfAbsent(type, k -> new HashSet<>());
        hooks.add(hook);
    }

    public Set<ListenerHook> getListenerHooks(ListenerHookType type)
    {
        Set<ListenerHook> hooks = _listenerHooks.get(type);
        if(hooks == null)
            return Collections.emptySet();
        return hooks;
    }
}
