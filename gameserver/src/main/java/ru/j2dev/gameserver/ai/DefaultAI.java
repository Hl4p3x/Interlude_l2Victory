package ru.j2dev.gameserver.ai;

import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.math.random.RndSelector;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.geodata.GeoEngine;
import ru.j2dev.gameserver.model.AggroList.AggroInfo;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.model.Skill.SkillTargetType;
import ru.j2dev.gameserver.model.Zone.ZoneType;
import ru.j2dev.gameserver.model.entity.SevenSigns;
import ru.j2dev.gameserver.model.instances.MinionInstance;
import ru.j2dev.gameserver.model.instances.MonsterInstance;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.QuestEventType;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.serverpackets.MagicSkillUse;
import ru.j2dev.gameserver.stats.Stats;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.taskmanager.AiTaskManager;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.NpcUtils;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

public class DefaultAI extends CharacterAI {
    public static final int TaskDefaultWeight = 10000;
    public static final int TELEPORT_TIMEOUT = 10000;
    public static final int MAX_ATTACK_TIMEOUT = 15000;
    public static final int MAX_PATHFIND_FAILS = 3;

    protected final NavigableSet<Task> _tasks = new ConcurrentSkipListSet<>(TaskComparator.getInstance());
    protected final Skill[] _damSkills;
    protected final Skill[] _dotSkills;
    protected final Skill[] _debuffSkills;
    protected final Skill[] _healSkills;
    protected final Skill[] _buffSkills;
    protected final Skill[] _stunSkills;
    protected final Comparator<Creature> _nearestTargetComparator;
    protected final long _minFactionNotifyInterval;
    protected long AI_TASK_ATTACK_DELAY = Config.AI_TASK_ATTACK_DELAY;
    protected long AI_TASK_ACTIVE_DELAY = Config.AI_TASK_ACTIVE_DELAY;
    protected long AI_TASK_DELAY_CURRENT = AI_TASK_ACTIVE_DELAY;
    protected int MAX_PURSUE_RANGE;
    protected ScheduledFuture<?> _aiTask;
    protected ScheduledFuture<?> _runningTask;
    protected ScheduledFuture<?> _madnessTask;
    protected boolean _def_think;
    protected long _globalAggro;
    protected long _randomAnimationEnd;
    protected int _pathfindFails;
    protected long _lastActiveCheck;
    protected long _checkAggroTimestamp;
    protected long _attackTimeout;
    protected long _lastFactionNotifyTime;
    private boolean _thinking;

    public DefaultAI(final NpcInstance actor) {
        super(actor);
        setAttackTimeout(Long.MAX_VALUE);
        _damSkills = actor.getTemplate().getDamageSkills();
        _dotSkills = actor.getTemplate().getDotSkills();
        _debuffSkills = actor.getTemplate().getDebuffSkills();
        _buffSkills = actor.getTemplate().getBuffSkills();
        _stunSkills = actor.getTemplate().getStunSkills();
        _healSkills = actor.getTemplate().getHealSkills();
        _nearestTargetComparator = new NearestTargetComparator(actor);
        MAX_PURSUE_RANGE = actor.getParameter("MaxPursueRange", actor.isRaid() ? Config.MAX_PURSUE_RANGE_RAID : (actor.isUnderground() ? Config.MAX_PURSUE_UNDERGROUND_RANGE : Config.MAX_PURSUE_RANGE));
        _minFactionNotifyInterval = actor.getParameter("FactionNotifyInterval", 3000);
    }

    protected static Skill selectTopSkillByDamage(final Creature actor, final Creature target, final double distance, final Skill[] skills) {
        if (skills == null || skills.length == 0) {
            return null;
        }
        if (skills.length == 1) {
            return skills[0];
        }
        final RndSelector<Skill> rnd = new RndSelector<>(skills.length);
        for (final Skill skill : skills) {
            double weight = skill.getSimpleDamage(actor, target) * skill.getAOECastRange() / distance;
            if (weight < 1.0) {
                weight = 1.0;
            }
            rnd.add(skill, (int) weight);
        }
        return rnd.select();
    }

    protected static Skill selectTopSkillByDebuff(final Creature actor, final Creature target, final double distance, final Skill[] skills) {
        if (skills == null || skills.length == 0) {
            return null;
        }
        if (skills.length == 1) {
            return skills[0];
        }
        final RndSelector<Skill> rnd = new RndSelector<>(skills.length);
        for (final Skill skill : skills) {
            if (skill.getSameByStackType(target) == null) {
                double weight;
                if ((weight = 100.0 * skill.getAOECastRange() / distance) <= 0.0) {
                    weight = 1.0;
                }
                rnd.add(skill, (int) weight);
            }
        }
        return rnd.select();
    }

    protected static Skill selectTopSkillByBuff(final Creature target, final Skill[] skills) {
        if (skills == null || skills.length == 0) {
            return null;
        }
        if (skills.length == 1) {
            return skills[0];
        }
        final RndSelector<Skill> rnd = new RndSelector<>(skills.length);
        for (final Skill skill : skills) {
            if (skill.getSameByStackType(target) == null) {
                double weight;
                if ((weight = skill.getPower()) <= 0.0) {
                    weight = 1.0;
                }
                rnd.add(skill, (int) weight);
            }
        }
        return rnd.select();
    }

    protected static Skill selectTopSkillByHeal(final Creature target, final Skill[] skills) {
        if (skills == null || skills.length == 0) {
            return null;
        }
        final double hpReduced = target.getMaxHp() - target.getCurrentHp();
        if (hpReduced < 1.0) {
            return null;
        }
        if (skills.length == 1) {
            return skills[0];
        }
        final RndSelector<Skill> rnd = new RndSelector<>(skills.length);
        for (final Skill skill : skills) {
            double weight;
            if ((weight = Math.abs(skill.getPower() - hpReduced)) <= 0.0) {
                weight = 1.0;
            }
            rnd.add(skill, (int) weight);
        }
        return rnd.select();
    }

    public void addTaskCast(final Creature target, final Skill skill) {
        final Task task = new Task();
        task.type = TaskType.CAST;
        task.target = target.getRef();
        task.skill = skill;
        _tasks.add(task);
        _def_think = true;
    }

    public void addTaskBuff(final Creature target, final Skill skill) {
        final Task task = new Task();
        task.type = TaskType.BUFF;
        task.target = target.getRef();
        task.skill = skill;
        _tasks.add(task);
        _def_think = true;
    }

    public void addTaskAttack(final Creature target) {
        final Task task = new Task();
        task.type = TaskType.ATTACK;
        task.target = target.getRef();
        _tasks.add(task);
        _def_think = true;
    }

    public void addTaskAttack(final Creature target, final Skill skill, final int weight) {
        final Task task = new Task();
        task.type = (skill.isOffensive() ? TaskType.CAST : TaskType.BUFF);
        task.target = target.getRef();
        task.skill = skill;
        task.weight = weight;
        _tasks.add(task);
        _def_think = true;
    }

    public void addTaskMove(final Location loc, final boolean pathfind) {
        final Task task = new Task();
        task.type = TaskType.MOVE;
        task.loc = loc;
        task.pathfind = pathfind;
        _tasks.add(task);
        _def_think = true;
    }

    protected void addTaskMove(final int locX, final int locY, final int locZ, final boolean pathfind) {
        addTaskMove(new Location(locX, locY, locZ), pathfind);
    }

    @Override
    public void runImpl() {
        if (_aiTask == null) {
            return;
        }
        if (!isGlobalAI() && System.currentTimeMillis() - _lastActiveCheck > 60000L) {
            _lastActiveCheck = System.currentTimeMillis();
            final NpcInstance actor = getActor();
            final WorldRegion region = (actor == null) ? null : actor.getCurrentRegion();
            if (region == null || !region.isActive()) {
                stopAITask();
                return;
            }
        }
        onEvtThink();
    }

    @Override
    public synchronized void startAITask() {
        if (_aiTask == null) {
            AI_TASK_DELAY_CURRENT = AI_TASK_ACTIVE_DELAY;
            _aiTask = AiTaskManager.getInstance().scheduleAtFixedRate(this, 0L, AI_TASK_DELAY_CURRENT);
        }
    }

    protected synchronized void switchAITask(final long NEW_DELAY) {
        if (_aiTask == null) {
            return;
        }
        if (AI_TASK_DELAY_CURRENT != NEW_DELAY) {
            _aiTask.cancel(false);
            AI_TASK_DELAY_CURRENT = NEW_DELAY;
            _aiTask = AiTaskManager.getInstance().scheduleAtFixedRate(this, 0L, AI_TASK_DELAY_CURRENT);
        }
    }

    @Override
    public final synchronized void stopAITask() {
        if (_aiTask != null) {
            _aiTask.cancel(false);
            _aiTask = null;
        }
    }

    protected boolean canSeeInSilentMove(final Playable target) {
        return getActor().getParameter("canSeeInSilentMove", false) || !target.isSilentMoving();
    }

    protected boolean canSeeInHide(final Playable target) {
        return getActor().getParameter("canSeeInHide", false) || !target.isInvisible();
    }

    protected boolean checkAggression(final Creature target) {
        final NpcInstance actor = getActor();
        if (getIntention() != CtrlIntention.AI_INTENTION_ACTIVE || !isGlobalAggro()) {
            return false;
        }
        if (target.isAlikeDead()) {
            return false;
        }
        if (target.isNpc() && target.isInvul()) {
            return false;
        }
        if (target.isPlayable()) {
            if (!canSeeInSilentMove((Playable) target)) {
                return false;
            }
            if (!canSeeInHide((Playable) target)) {
                return false;
            }
            if (actor.getFaction().getName().equalsIgnoreCase("varka_silenos_clan") && target.getPlayer().getVarka() > 0) {
                return false;
            }
            if (actor.getFaction().getName().equalsIgnoreCase("ketra_orc_clan") && target.getPlayer().getKetra() > 0) {
                return false;
            }
            if (target.isPlayer() && ((Player) target).isGM() && target.isInvisible()) {
                return false;
            }
            if (((Playable) target).getNonAggroTime() > System.currentTimeMillis()) {
                return false;
            }
            if (target.isPlayer() && !target.getPlayer().isActive()) {
                return false;
            }
            if (actor.isMonster() && target.isInZonePeace()) {
                return false;
            }
        }
        return isInAggroRange(target) && canAttackCharacter(target) && GeoEngine.canSeeTarget(actor, target, false);
    }

    protected Location getPursueBaseLoc() {
        final NpcInstance actor = getActor();
        final Location spawnLoc = actor.getSpawnedLoc();
        return (spawnLoc != null) ? spawnLoc : actor.getLoc();
    }

    protected boolean isInAggroRange(final Creature target) {
        final NpcInstance actor = getActor();
        final AggroInfo ai = actor.getAggroList().get(target);
        if (ai != null && ai.hate > 0) {
            return target.isInRangeZ(getPursueBaseLoc(), MAX_PURSUE_RANGE);
        } else return isAggressive() && target.isInRangeZ(getPursueBaseLoc(), actor.getAggroRange());
    }

    protected void setIsInRandomAnimation(final long time) {
        _randomAnimationEnd = System.currentTimeMillis() + time;
    }

    protected boolean randomAnimation() {
        final NpcInstance actor = getActor();
        if (actor.getParameter("noRandomAnimation", false)) {
            return false;
        }
        if (actor.hasRandomAnimation() && !actor.isActionsDisabled() && !actor.isMoving() && !actor.isInCombat() && Rnd.chance(Config.RND_ANIMATION_RATE)) {
            setIsInRandomAnimation(3000L);
            actor.onRandomAnimation();
            return true;
        }
        return false;
    }

    protected Creature getNearestTarget(final List<Creature> targets) {
        final NpcInstance actor = getActor();
        return targets.stream().min(Comparator.comparingDouble(p -> p.getDistance3D(actor))).get();
    }

    protected boolean randomWalk() {
        final NpcInstance actor = getActor();
        return !actor.getParameter("noRandomWalk", false) && !actor.isMoving() && maybeMoveToHome();
    }

    protected boolean thinkActive() {
        final NpcInstance actor = getActor();
        if (actor.isActionsDisabled()) {
            return true;
        }
        if (_randomAnimationEnd > System.currentTimeMillis()) {
            return true;
        }
        if (_def_think) {
            if (doTask()) {
                clearTasks();
            }
            return true;
        }
        final long now = System.currentTimeMillis();
        if (now - _checkAggroTimestamp > Config.AGGRO_CHECK_INTERVAL) {
            _checkAggroTimestamp = now;
            final boolean aggressive = Rnd.chance(actor.getParameter("SelfAggressive", actor.isAggressive() ? 100 : 0));
            if (!actor.getAggroList().isEmpty() || aggressive) {
                final List<Creature> targets = World.getAroundCharacters(actor, Math.max(getActor().getAggroRange(), 1000), 250);
                while (!targets.isEmpty()) {
                    final Creature target = getNearestTarget(targets);
                    if (target == null) {
                        break;
                    }

                    if (aggressive || actor.getAggroList().get(target) != null) {
                        if (checkAggression(target)) {
                            actor.getAggroList().addDamageHate(target, 0, 2);
                            //notifyFriends(target, 2);

                            if (target.isSummon()) {
                                actor.getAggroList().addDamageHate(target.getPlayer(), 0, 1);
                            }

                            startRunningTask(AI_TASK_ATTACK_DELAY);
                            setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);

                            return true;
                        }
                    }

                    targets.remove(target);
                }
            }
        }
        if (actor.isMinion()) {
            final MonsterInstance leader = ((MinionInstance) actor).getLeader();
            if (leader != null) {
                final double distance = actor.getDistance(leader.getX(), leader.getY());
                if (distance > 1000.0) {
                    actor.teleToLocation(leader.getMinionPosition());
                } else if (distance > 200.0) {
                    addTaskMove(leader.getMinionPosition(), false);
                }
                return true;
            }
        }
        return randomAnimation() || randomWalk();
    }

    @Override
    protected void onIntentionIdle() {
        final NpcInstance actor = getActor();
        clearTasks();
        actor.stopMove();
        actor.getAggroList().clear(true);
        setAttackTimeout(Long.MAX_VALUE);
        setAttackTarget(null);
        changeIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
    }

    @Override
    protected void onIntentionActive() {
        final NpcInstance actor = getActor();
        actor.stopMove();
        setAttackTimeout(Long.MAX_VALUE);
        if (getIntention() != CtrlIntention.AI_INTENTION_ACTIVE) {
            switchAITask(AI_TASK_ACTIVE_DELAY);
            changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
        }
        onEvtThink();
    }

    @Override
    protected void onIntentionAttack(final Creature target) {
        final NpcInstance actor = getActor();
        clearTasks();
        actor.stopMove();
        setAttackTarget(target);
        setAttackTimeout(getMaxAttackTimeout() + System.currentTimeMillis());
        setGlobalAggro(0L);
        if (getIntention() != CtrlIntention.AI_INTENTION_ATTACK) {
            changeIntention(CtrlIntention.AI_INTENTION_ATTACK, target, null);
            switchAITask(AI_TASK_ATTACK_DELAY);
        }
        onEvtThink();
    }

    protected boolean canAttackCharacter(final Creature target) {
        return target.isPlayable();
    }

    protected boolean isAggressive() {
        return getActor().isAggressive();
    }

    protected boolean checkTarget(final Creature target, final int range) {
        final NpcInstance actor = getActor();
        if (target == null || target.isAlikeDead() || !actor.isInRangeZ(target, range)) {
            return false;
        }
        final boolean hided = target.isPlayable() && !canSeeInHide((Playable) target);
        if (!hided && actor.isConfused()) {
            return true;
        }
        if (getIntention() != CtrlIntention.AI_INTENTION_ATTACK) {
            return canAttackCharacter(target);
        }
        final AggroInfo ai = actor.getAggroList().get(target);
        if (ai == null) {
            return false;
        }
        if (hided) {
            ai.hate = 0;
            return false;
        }
        return ai.hate > 0;
    }

    protected long getAttackTimeout() {
        return _attackTimeout;
    }

    public void setAttackTimeout(final long time) {
        _attackTimeout = time;
    }

    protected void thinkAttack() {
        final NpcInstance actor = getActor();
        if (actor.isDead()) {
            return;
        }
        final Location loc = getPursueBaseLoc();
        if (!actor.isInRange(loc, MAX_PURSUE_RANGE)) {
            teleportHome();
            return;
        }
        if (doTask() && !actor.isAttackingNow() && !actor.isCastingNow() && !createNewTask() && System.currentTimeMillis() > getAttackTimeout()) {
            returnHome();
        }
    }

    @Override
    protected void onEvtSpawn() {
        setGlobalAggro(System.currentTimeMillis() + getActor().getParameter("globalAggro", 10000L));
        setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
    }

    @Override
    protected void onEvtReadyToAct() {
        onEvtThink();
    }

    @Override
    protected void onEvtArrivedTarget() {
        onEvtThink();
    }

    @Override
    protected void onEvtArrived() {
        onEvtThink();
    }

    protected boolean tryMoveToTarget(final Creature target) {
        return tryMoveToTarget(target, CharacterAI.getIndentRange(target.getActingRange()), target.getActingRange());
    }

    protected boolean tryMoveToTarget(final Creature target, final int indent, final int range) {
        final NpcInstance actor = getActor();
        if (range > 16) {
            if (!actor.moveToRelative(target, indent, range)) {
                ++_pathfindFails;
            }
        } else if (!actor.moveToLocation(target.getLoc(), indent, true)) {
            ++_pathfindFails;
        }
        if (_pathfindFails >= getMaxPathfindFails() && System.currentTimeMillis() > getAttackTimeout() - getMaxAttackTimeout() + getTeleportTimeout() && actor.isInRange(target, MAX_PURSUE_RANGE)) {
            _pathfindFails = 0;
            if (target.isPlayable()) {
                final AggroInfo hate = actor.getAggroList().get(target);
                if (hate == null || hate.hate < 100 || (actor.isRaid() && Math.abs(target.getZ() - actor.getZ()) > 900)) {
                    return false;
                }
            }
            Location loc = GeoEngine.moveCheckForAI(target.getLoc(), actor.getLoc(), actor.getGeoIndex());
            if (!GeoEngine.canMoveToCoord(actor.getX(), actor.getY(), actor.getZ(), loc.x, loc.y, loc.z, actor.getGeoIndex())) {
                loc = target.getLoc();
            }
            actor.teleToLocation(loc);
        }
        return true;
    }

    protected boolean maybeNextTask(final Task currentTask) {
        _tasks.remove(currentTask);
        return _tasks.size() == 0;
    }

    protected boolean doTask() {
        final NpcInstance actor = getActor();
        if (!_def_think) {
            return true;
        }
        final Task currentTask = _tasks.pollFirst();
        if (currentTask == null) {
            clearTasks();
            return true;
        }
        if (actor.isDead() || actor.isAttackingNow() || actor.isCastingNow()) {
            return false;
        }
        switch (currentTask.type) {
            case MOVE: {
                if (actor.isMovementDisabled() || !getIsMobile()) {
                    return true;
                }
                if (actor.isInRange(currentTask.loc, 100L)) {
                    return maybeNextTask(currentTask);
                }
                if (actor.isMoving()) {
                    return false;
                }
                if (!actor.moveToLocation(currentTask.loc, 0, currentTask.pathfind)) {
                    clientStopMoving();
                    _pathfindFails = 0;
                    actor.teleToLocation(currentTask.loc);
                    return maybeNextTask(currentTask);
                }
                break;
            }
            case ATTACK: {
                final Creature target = currentTask.target.get();
                if (!checkTarget(target, MAX_PURSUE_RANGE)) {
                    return true;
                }
                setAttackTarget(target);
                if (actor.isMoving()) {
                    return Rnd.chance(25);
                }
                final int pAtkRng = actor.getPhysicalAttackRange();
                final int collisions = (int) (actor.getCollisionRadius() + target.getColRadius());
                final boolean incZ = actor.isFlying() || actor.isInWater() || target.isFlying() || target.isInWater();
                final int dist = (int) (incZ ? actor.getDistance3D(target) : actor.getDistance(target)) - collisions;
                if (dist <= pAtkRng + 16 && GeoEngine.canSeeTarget(actor, target, incZ)) {
                    clientStopMoving();
                    _pathfindFails = 0;
                    setAttackTimeout(getMaxAttackTimeout() + System.currentTimeMillis());
                    actor.doAttack(target);
                    return maybeNextTask(currentTask);
                }
                return actor.isMovementDisabled() || !getIsMobile() || !tryMoveToTarget(target, collisions + CharacterAI.getIndentRange(pAtkRng), collisions + pAtkRng);
            }
            case CAST: {
                final Creature target = currentTask.target.get();
                if (actor.isMuted(currentTask.skill) || actor.isSkillDisabled(currentTask.skill) || actor.isUnActiveSkill(currentTask.skill.getId())) {
                    return true;
                }
                final boolean isAoE = currentTask.skill.getTargetType() == SkillTargetType.TARGET_AURA;
                final int castRange = currentTask.skill.getAOECastRange();
                if (!checkTarget(target, MAX_PURSUE_RANGE + castRange)) {
                    return true;
                }
                setAttackTarget(target);
                final int collisions2 = (int) (actor.getCollisionRadius() + target.getColRadius());
                final boolean incZ2 = actor.isFlying() || actor.isInWater() || target.isFlying() || target.isInWater();
                final int dist2 = (int) (incZ2 ? actor.getDistance3D(target) : actor.getDistance(target)) - collisions2;
                if (dist2 <= castRange && GeoEngine.canSeeTarget(actor, target, incZ2)) {
                    clientStopMoving();
                    _pathfindFails = 0;
                    setAttackTimeout(getMaxAttackTimeout() + System.currentTimeMillis());
                    actor.doCast(currentTask.skill, isAoE ? actor : target, !target.isPlayable());
                    return maybeNextTask(currentTask);
                }
                if (actor.isMoving()) {
                    return Rnd.chance(10);
                }
                return actor.isMovementDisabled() || !getIsMobile() || !tryMoveToTarget(target, collisions2 + CharacterAI.getIndentRange(castRange), collisions2 + castRange);
            }
            case BUFF: {
                final Creature target = currentTask.target.get();
                if (actor.isMuted(currentTask.skill) || actor.isSkillDisabled(currentTask.skill) || actor.isUnActiveSkill(currentTask.skill.getId())) {
                    return true;
                }
                if (target == null || target.isAlikeDead() || !actor.isInRange(target, 2000L)) {
                    return true;
                }
                final boolean isAoE = currentTask.skill.getTargetType() == SkillTargetType.TARGET_AURA;
                final int castRange = currentTask.skill.getAOECastRange();
                if (actor.isMoving()) {
                    return Rnd.chance(10);
                }
                final int collisions2 = (int) (actor.getCollisionRadius() + target.getColRadius());
                final boolean incZ2 = actor.isFlying() || actor.isInWater() || target.isFlying() || target.isInWater();
                final int dist2 = (int) (incZ2 ? actor.getDistance3D(target) : actor.getDistance(target)) - collisions2;
                if (dist2 <= castRange && GeoEngine.canSeeTarget(actor, target, incZ2)) {
                    clientStopMoving();
                    _pathfindFails = 0;
                    actor.doCast(currentTask.skill, isAoE ? actor : target, !target.isPlayable());
                    return maybeNextTask(currentTask);
                }
                return actor.isMovementDisabled() || !getIsMobile() || !tryMoveToTarget(target, collisions2 + CharacterAI.getIndentRange(castRange), collisions2 + castRange);
            }
        }
        return false;
    }

    protected boolean createNewTask() {
        return false;
    }

    protected boolean defaultNewTask() {
        clearTasks();
        final NpcInstance actor = getActor();
        final Creature target;
        if (actor == null || (target = prepareTarget()) == null) {
            return false;
        }
        final double distance = actor.getDistance(target);
        return chooseTaskAndTargets(null, target, distance);
    }

    @Override
    protected void onEvtThink() {
        final NpcInstance actor = getActor();
        if (_thinking || actor == null || actor.isActionsDisabled() || actor.isAfraid()) {
            return;
        }
        if (_randomAnimationEnd > System.currentTimeMillis()) {
            return;
        }
        if (Config.RAID_TELE_TO_HOME_FROM_PVP_ZONES && actor.isRaid() && (actor.isInZoneBattle() || actor.isInZone(ZoneType.SIEGE) || actor.isInZone(ZoneType.fun))) {
            teleportHome();
            return;
        }
        if (Config.RAID_TELE_TO_HOME_FROM_TOWN_ZONES && actor.isRaid() && actor.isInZonePeace()) {
            teleportHome();
            return;
        }
        _thinking = true;
        try {
            if (!Config.BLOCK_ACTIVE_TASKS && getIntention() == CtrlIntention.AI_INTENTION_ACTIVE) {
                thinkActive();
            } else if (getIntention() == CtrlIntention.AI_INTENTION_ATTACK) {
                thinkAttack();
            }
        } finally {
            _thinking = false;
        }
    }

    @Override
    protected void onEvtDead(final Creature killer) {
        final NpcInstance actor = getActor();
        final int transformer = actor.getParameter("transformOnDead", 0);
        final int amount = actor.getParameter("transformSpawnAmount", 1);
        final int rndRadius = actor.getParameter("transformSpawnRndRadius", 0);
        final int chance = actor.getParameter("transformChance", 100);
        if (transformer > 0 && Rnd.chance(chance)) {
            for (int cnt = 0; cnt < amount; ++cnt) {
                Location loc = actor.getLoc();
                if (rndRadius > 0) {
                    loc = Location.findPointToStay(loc, rndRadius, killer.getGeoIndex());
                }
                final NpcInstance npc = NpcUtils.spawnSingle(transformer, loc, actor.getReflection());
                if (killer != null && killer.isPlayable()) {
                    npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, 100);
                    killer.sendPacket(npc.makeStatusUpdate(9, 10));
                }
            }
        }
        super.onEvtDead(killer);
    }

    @Override
    protected void onEvtClanAttacked(final Creature attacked, final Creature attacker, final int damage) {
        if (getIntention() != CtrlIntention.AI_INTENTION_ACTIVE || !isGlobalAggro()) {
            return;
        }
        notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 2);
    }

    @Override
    protected void onEvtAttacked(final Creature attacker, final int damage) {
        final NpcInstance actor = getActor();
        if (attacker == null || actor.isDead()) {
            return;
        }
        final int transformer = actor.getParameter("transformOnUnderAttack", 0);
        if (transformer > 0) {
            final int chance = actor.getParameter("transformChance", 5);
            if (chance == 100 || (((MonsterInstance) actor).getChampion() == 0 && actor.getCurrentHpPercents() > 50.0 && Rnd.chance(chance))) {
                final MonsterInstance npc = (MonsterInstance) NpcTemplateHolder.getInstance().getTemplate(transformer).getNewInstance();
                npc.setSpawnedLoc(actor.getLoc());
                npc.setReflection(actor.getReflection());
                npc.setChampion(((MonsterInstance) actor).getChampion());
                npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp(), true);
                npc.spawnMe(npc.getSpawnedLoc());
                npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 100);
                actor.decayOrDelete();
                attacker.setTarget(npc);
                attacker.sendPacket(npc.makeStatusUpdate(9, 10));
                return;
            }
        }
        final Player player = attacker.getPlayer();
        if (player != null) {
            if (Config.ALT_TELEPORT_FROM_SEVEN_SING_MONSTER && (SevenSigns.getInstance().isSealValidationPeriod() || SevenSigns.getInstance().isCompResultsPeriod()) && actor.isSevenSignsMonster()) {
                final int pcabal = SevenSigns.getInstance().getPlayerCabal(player);
                final int wcabal = SevenSigns.getInstance().getCabalHighestScore();
                if (pcabal != wcabal && wcabal != 0) {
                    player.sendMessage("You have been teleported to the nearest town because you not signed for winning cabal.");
                    player.teleToClosestTown();
                    return;
                }
            }
            final List<QuestState> quests = player.getQuestsForEvent(actor, QuestEventType.ATTACKED_WITH_QUEST);
            if (quests != null) {
                for (final QuestState qs : quests) {
                    qs.getQuest().notifyAttack(actor, qs);
                }
            }
        }
        actor.getAggroList().addDamageHate(attacker, 0, damage);
        if (damage > 0 && (attacker.isSummon() || attacker.isPet())) {
            actor.getAggroList().addDamageHate(attacker.getPlayer(), 0, actor.getParameter("searchingMaster", false) ? damage : 1);
        }
        if (getIntention() != CtrlIntention.AI_INTENTION_ATTACK) {
            if (!actor.isRunning()) {
                startRunningTask(AI_TASK_ATTACK_DELAY);
            }
            setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
        }
        notifyFriends(attacker, damage);
    }

    @Override
    protected void onEvtAggression(final Creature attacker, final int aggro) {
        final NpcInstance actor = getActor();
        if (attacker == null || actor.isDead()) {
            return;
        }
        actor.getAggroList().addDamageHate(attacker, 0, aggro);
        if (aggro > 0 && (attacker.isSummon() || attacker.isPet())) {
            actor.getAggroList().addDamageHate(attacker.getPlayer(), 0, actor.getParameter("searchingMaster", false) ? aggro : 1);
        }
        if (getIntention() != CtrlIntention.AI_INTENTION_ATTACK) {
            if (!actor.isRunning()) {
                startRunningTask(AI_TASK_ATTACK_DELAY);
            }
            chargeShots(actor);
            setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
        }
        notifyMinions(attacker, aggro);
    }

    public void chargeShots(final NpcInstance actor) {
        // Показываем анимацию зарядки шотов, если есть таковые.
        switch (actor.getTemplate().shots)
        {
            case SOUL:
                actor.unChargeShots(false);
                break;
            case SPIRIT:
            case BSPIRIT:
                actor.unChargeShots(true);
                break;
            case SOUL_SPIRIT:
            case SOUL_BSPIRIT:
                actor.unChargeShots(false);
                actor.unChargeShots(true);
                break;
            default:
                break;
        }
    }

    protected boolean maybeMoveToHome() {
        final NpcInstance actor = getActor();
        if (actor.isDead()) {
            return false;
        }
        final boolean randomWalk = actor.hasRandomWalk();
        final Location sloc = getPursueBaseLoc();
        final int maxDriftRange = actor.getParameter("MaxDriftRange", 0);
        if (randomWalk && (!Config.RND_WALK || !Rnd.chance(Config.RND_WALK_RATE))) {
            return false;
        }
        final int driftRange = maxDriftRange > 0 ? maxDriftRange : Config.MAX_DRIFT_RANGE;
        final boolean isInRange = actor.isInRangeZ(sloc, driftRange);
        if (!randomWalk && isInRange) {
            return false;
        }

        int x = actor.getSpawnedLoc().getX() + Rnd.get(-driftRange, driftRange);
        int y = actor.getSpawnedLoc().getY() + Rnd.get(-driftRange, driftRange);
        int z = actor.isFlying() ? Rnd.get(actor.getZ() - driftRange / 2, actor.getZ() + driftRange / 2) : GeoEngine.getHeight(x, y, actor.getSpawnedLoc().getZ(), actor.getReflectionId());

        if (actor.getSpawnedLoc().getZ() - z > 200) {
            return false;
        }

        final Location pos = new Location(x, y, z);
        //final Location pos = Location.findPointToStay(actor, sloc, 0, driftRange);
        actor.setWalking();
        if (!actor.moveToLocation(pos.x, pos.y, pos.z, 0, true) && !isInRange) {
            teleportHome();
        }
        return true;
    }

    protected void returnHome() {
        returnHome(true, Config.ALWAYS_TELEPORT_HOME);
    }

    protected void teleportHome() {
        returnHome(true, true);
    }

    protected void returnHome(final boolean clearAggro, final boolean teleport) {
        final NpcInstance actor = getActor();
        final Location sloc = getPursueBaseLoc();
        clearTasks();
        actor.stopMove();
        if (clearAggro) {
            actor.getAggroList().clear(true);
        }
        setAttackTimeout(Long.MAX_VALUE);
        setAttackTarget(null);
        if (Config.RESTORE_HP_MP_ON_TELEPORT_HOME) {
            actor.setCurrentHpMp(actor.getMaxHp(), actor.getMaxMp(), true);
        }
        changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
        if (teleport) {
            actor.broadcastPacketToOthers(new MagicSkillUse(actor, actor, 2036, 1, 500, 0L));
            actor.teleToLocation(sloc.x, sloc.y, GeoEngine.getHeight(sloc, actor.getGeoIndex()));
        } else {
            if (!clearAggro) {
                actor.setRunning();
            } else {
                actor.setWalking();
            }
            addTaskMove(sloc, false);
        }
    }

    protected Creature prepareTarget() {
        final NpcInstance actor = getActor();
        if (actor.isConfused()) {
            return getAttackTarget();
        }
        if (Rnd.chance(actor.getParameter("isMadness", 0))) {
            final Creature randomHated = actor.getAggroList().getRandomHated();
            if (randomHated != null) {
                setAttackTarget(randomHated);
                if (_madnessTask == null && !actor.isConfused()) {
                    actor.startConfused();
                    _madnessTask = ThreadPoolManager.getInstance().schedule(new MadnessTask(), 10000L);
                }
                return randomHated;
            }
        }
        final List<Creature> hateList = actor.getAggroList().getHateList(MAX_PURSUE_RANGE);
        Creature hated = null;
        for (final Creature cha : hateList) {
            if (checkTarget(cha, MAX_PURSUE_RANGE)) {
                hated = cha;
                break;
            }
            actor.getAggroList().remove(cha, true);
        }
        if (hated != null) {
            setAttackTarget(hated);
            return hated;
        }
        return null;
    }

    protected boolean canUseSkill(final Skill skill, final Creature target, final double distance) {
        final NpcInstance actor = getActor();
        if (skill == null || skill.isNotUsedByAI()) {
            return false;
        }
        if (skill.getTargetType() == SkillTargetType.TARGET_SELF && target != actor) {
            return false;
        }
        final int castRange = skill.getAOECastRange();
        if (castRange <= 200 && distance > 200.0) {
            return false;
        }
        if (actor.isSkillDisabled(skill) || actor.isMuted(skill) || actor.isUnActiveSkill(skill.getId())) {
            return false;
        }
        double mpConsume2 = skill.getMpConsume2();
        if (skill.isMagic()) {
            mpConsume2 = actor.calcStat(Stats.MP_MAGIC_SKILL_CONSUME, mpConsume2, target, skill);
        } else {
            mpConsume2 = actor.calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, mpConsume2, target, skill);
        }
        return actor.getCurrentMp() >= mpConsume2 && target.getEffectList().getEffectsCountForSkill(skill.getId()) == 0;
    }

    protected boolean canUseSkill(final Skill sk, final Creature target) {
        return canUseSkill(sk, target, 0.0);
    }

    protected Skill[] selectUsableSkills(final Creature target, final double distance, final Skill[] skills) {
        if (skills == null || skills.length == 0 || target == null) {
            return null;
        }
        Skill[] ret = null;
        int usable = 0;
        for (final Skill skill : skills) {
            if (canUseSkill(skill, target, distance)) {
                if (ret == null) {
                    ret = new Skill[skills.length];
                }
                ret[usable++] = skill;
            }
        }
        if (ret == null || usable == skills.length) {
            return ret;
        }
        if (usable == 0) {
            return null;
        }
        ret = Arrays.copyOf(ret, usable);
        return ret;
    }

    protected void addDesiredSkill(final Map<Skill, Integer> skillMap, final Creature target, final double distance, final Skill[] skills) {
        if (skills == null || skills.length == 0 || target == null) {
            return;
        }
        Arrays.stream(skills).forEach(sk -> addDesiredSkill(skillMap, target, distance, sk));
    }

    protected void addDesiredSkill(final Map<Skill, Integer> skillMap, final Creature target, final double distance, final Skill skill) {
        if (skill == null || target == null || !canUseSkill(skill, target)) {
            return;
        }
        int weight = (int) (-Math.abs(skill.getAOECastRange() - distance));
        if (skill.getAOECastRange() >= distance) {
            weight += 1000000;
        } else if (skill.isNotTargetAoE() && skill.getTargets(getActor(), target, false).size() == 0) {
            return;
        }
        skillMap.put(skill, weight);
    }

    protected void addDesiredHeal(final Map<Skill, Integer> skillMap, final Skill[] skills) {
        if (skills == null || skills.length == 0) {
            return;
        }
        final NpcInstance actor = getActor();
        final double hpReduced = actor.getMaxHp() - actor.getCurrentHp();
        final double hpPercent = actor.getCurrentHpPercents();
        if (hpReduced < 1.0) {
            return;
        }
        for (final Skill sk : skills) {
            if (canUseSkill(sk, actor) && sk.getPower() <= hpReduced) {
                int weight = (int) sk.getPower();
                if (hpPercent < 50.0) {
                    weight += 1000000;
                }
                skillMap.put(sk, weight);
            }
        }
    }

    protected void addDesiredBuff(final Map<Skill, Integer> skillMap, final Skill[] skills) {
        if (skills == null || skills.length == 0) {
            return;
        }
        final NpcInstance actor = getActor();
        Arrays.stream(skills).filter(sk -> canUseSkill(sk, actor)).forEach(sk -> skillMap.put(sk, 1000000));
    }

    protected Skill selectTopSkill(final Map<Skill, Integer> skillMap) {
        if (skillMap == null || skillMap.isEmpty()) {
            return null;
        }
        int topWeight = Integer.MIN_VALUE;
        for (final int next : skillMap.values()) {
            final int nWeight;
            if ((nWeight = next) > topWeight) {
                topWeight = nWeight;
            }
        }
        if (topWeight == Integer.MIN_VALUE) {
            return null;
        }
        final Skill[] skills = new Skill[skillMap.size()];
        int nWeight = 0;
        for (final Entry<Skill, Integer> e : skillMap.entrySet()) {
            if (e.getValue() < topWeight) {
                continue;
            }
            skills[nWeight++] = e.getKey();
        }
        return skills[Rnd.get(nWeight)];
    }

    protected boolean chooseTaskAndTargets(final Skill skill, Creature target, final double distance) {
        final NpcInstance actor = getActor();
        if (skill != null) {
            if (actor.isMovementDisabled() && distance > skill.getAOECastRange() + 60) {
                target = null;
                if (skill.isOffensive()) {
                    final List<Creature> targets = new ArrayList<>();
                    actor.getAggroList().getHateList(MAX_PURSUE_RANGE).stream().filter(cha -> checkTarget(cha, skill.getAOECastRange() + 60)).filter(cha -> canUseSkill(skill, cha)).forEach(targets::add);
                    if (!targets.isEmpty()) {
                        target = targets.get(Rnd.get(targets.size()));
                    }
                    targets.clear();
                }
            }
            if (target == null) {
                return false;
            }
            if (skill.isOffensive()) {
                addTaskCast(target, skill);
            } else {
                addTaskBuff(target, skill);
            }
            return true;
        } else {
            if (actor.isMovementDisabled() && distance > actor.getPhysicalAttackRange() + 40) {
                target = null;
                final List<Creature> targets = new ArrayList<>();
                actor.getAggroList().getHateList(MAX_PURSUE_RANGE).stream().filter(cha -> checkTarget(cha, actor.getPhysicalAttackRange() + 40)).forEach(targets::add);
                if (!targets.isEmpty()) {
                    target = targets.get(Rnd.get(targets.size()));
                }
                targets.clear();
            }
            if (target == null) {
                return false;
            }
            addTaskAttack(target);
            return true;
        }
    }

    protected boolean chooseTaskAndTargetsMagic(final Skill skill, Creature target, final double distance) {
        final NpcInstance actor = getActor();
        if (skill != null) {
            if (actor.isMovementDisabled() && distance > skill.getAOECastRange() + 60) {
                target = null;
                if (skill.isOffensive()) {
                    final List<Creature> targets = new ArrayList<>();
                    actor.getAggroList().getHateList(MAX_PURSUE_RANGE).stream().filter(cha -> checkTarget(cha, skill.getAOECastRange() + 60)).filter(cha -> canUseSkill(skill, cha)).forEach(targets::add);
                    if (!targets.isEmpty()) {
                        target = targets.get(Rnd.get(targets.size()));
                    }
                    targets.clear();
                }
            }
            if (target == null) {
                return false;
            }
            if (skill.isOffensive()) {
                addTaskCast(target, skill);
            }
            return true;
        }
        return true;
    }

    @Override
    public boolean isActive() {
        return _aiTask != null;
    }

    protected void clearTasks() {
        _def_think = false;
        _tasks.clear();
    }

    protected void startRunningTask(final long interval) {
        final NpcInstance actor = getActor();
        if (actor != null && _runningTask == null && !actor.isRunning()) {
            _runningTask = ThreadPoolManager.getInstance().schedule(new RunningTask(), interval);
        }
    }

    protected boolean isGlobalAggro() {
        if (_globalAggro == 0L) {
            return true;
        }
        if (_globalAggro <= System.currentTimeMillis()) {
            _globalAggro = 0L;
            return true;
        }
        return false;
    }

    public void setGlobalAggro(final long value) {
        _globalAggro = value;
    }

    @Override
    public NpcInstance getActor() {
        return (NpcInstance) super.getActor();
    }

    protected boolean defaultThinkBuff(final int rateSelf) {
        return defaultThinkBuff(rateSelf, 0);
    }

    protected void notifyFriends(final Creature attacker, final int damage) {
        final NpcInstance actor = getActor();
        if (System.currentTimeMillis() - _lastFactionNotifyTime > _minFactionNotifyInterval) {
            _lastFactionNotifyTime = System.currentTimeMillis();
            if (actor.isMinion()) {
                final MonsterInstance master = ((MinionInstance) actor).getLeader();
                if (master != null) {
                    if (!master.isDead() && master.isVisible()) {
                        master.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, damage);
                    }
                    final MinionList minionList = master.getMinionList();
                    if (minionList != null) {
                        minionList.getAliveMinions().stream().filter(minion -> minion != actor).forEach(minion -> minion.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, damage));
                    }
                }
            }
            final MinionList minionList2 = actor.getMinionList();
            if (minionList2 != null && minionList2.hasAliveMinions()) {
                minionList2.getAliveMinions().forEach(minion2 -> minion2.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, damage));
            }
            activeFactionTargets().forEach(npc -> npc.getAI().notifyEvent(CtrlEvent.EVT_CLAN_ATTACKED, new Object[]{actor, attacker, damage}));
        }
    }

    protected void notifyMinions(final Creature attacker, final int damage) {
        final NpcInstance actor = getActor();
        final MinionList minionList = actor.getMinionList();
        if (minionList != null && minionList.hasAliveMinions()) {
            minionList.getAliveMinions().forEach(minion -> minion.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, damage));
        }
    }

    protected List<NpcInstance> activeFactionTargets() {
        final NpcInstance actor = getActor();
        if (actor.getFaction().isNone()) {
            return Collections.emptyList();
        }
        return World.getAroundNpc(actor).stream().filter(npc -> !npc.isDead() && npc.isInFaction(actor) && npc.isInRangeZ(actor, npc.getFaction().getRange()) && GeoEngine.canSeeTarget(npc, actor, false)).collect(Collectors.toList());
    }

    protected boolean defaultThinkBuff(final int rateSelf, final int rateFriends) {
        final NpcInstance actor = getActor();
        if (actor.isDead()) {
            return true;
        }
        if (!Rnd.chance(rateSelf)) {
            if (Rnd.chance(rateFriends)) {
                for (final NpcInstance npc : activeFactionTargets()) {
                    final double targetHp = npc.getCurrentHpPercents();
                    final Skill[] skills = (targetHp < 50.0) ? selectUsableSkills(actor, 0.0, _healSkills) : selectUsableSkills(actor, 0.0, _buffSkills);
                    if (skills != null) {
                        if (skills.length == 0) {
                            continue;
                        }
                        final Skill skill = skills[Rnd.get(skills.length)];
                        addTaskBuff(actor, skill);
                        return true;
                    }
                }
            }
            return false;
        }
        final double actorHp = actor.getCurrentHpPercents();
        final Skill[] skills2 = (actorHp < 50.0) ? selectUsableSkills(actor, 0.0, _healSkills) : selectUsableSkills(actor, 0.0, _buffSkills);
        if (skills2 == null || skills2.length == 0) {
            return false;
        }
        final Skill skill2 = skills2[Rnd.get(skills2.length)];
        addTaskBuff(actor, skill2);
        return true;
    }

    protected boolean defaultFightTask() {
        clearTasks();
        final NpcInstance actor = getActor();
        if (actor.isDead() || actor.isAMuted()) {
            return false;
        }
        final Creature target;
        if ((target = prepareTarget()) == null) {
            return false;
        }
        final double distance = actor.getDistance(target);
        final double targetHp = target.getCurrentHpPercents();
        final double actorHp = actor.getCurrentHpPercents();
        final Skill[] dam = Rnd.chance(getRateDAM()) ? selectUsableSkills(target, distance, _damSkills) : null;
        final Skill[] dot = Rnd.chance(getRateDOT()) ? selectUsableSkills(target, distance, _dotSkills) : null;
        final Skill[] debuff = (targetHp > 10.0) ? (Rnd.chance(getRateDEBUFF()) ? selectUsableSkills(target, distance, _debuffSkills) : null) : null;
        final Skill[] stun = Rnd.chance(getRateSTUN()) ? selectUsableSkills(target, distance, _stunSkills) : null;
        final Skill[] heal = (actorHp < 50.0) ? (Rnd.chance(getRateHEAL()) ? selectUsableSkills(actor, 0.0, _healSkills) : null) : null;
        final Skill[] buff = Rnd.chance(getRateBUFF()) ? selectUsableSkills(actor, 0.0, _buffSkills) : null;
        final RndSelector<Skill[]> rnd = new RndSelector<>();
        if (!actor.isAMuted()) {
            rnd.add(null, getRatePHYS());
        }
        rnd.add(dam, getRateDAM());
        rnd.add(dot, getRateDOT());
        rnd.add(debuff, getRateDEBUFF());
        rnd.add(heal, getRateHEAL());
        rnd.add(buff, getRateBUFF());
        rnd.add(stun, getRateSTUN());
        final Skill[] selected = rnd.select();
        if (selected != null) {
            if (selected == dam || selected == dot) {
                return chooseTaskAndTargets(selectTopSkillByDamage(actor, target, distance, selected), target, distance);
            }
            if (selected == debuff || selected == stun) {
                return chooseTaskAndTargets(selectTopSkillByDebuff(actor, target, distance, selected), target, distance);
            }
            if (selected == buff) {
                return chooseTaskAndTargets(selectTopSkillByBuff(actor, selected), actor, distance);
            }
            if (selected == heal) {
                return chooseTaskAndTargets(selectTopSkillByHeal(actor, selected), actor, distance);
            }
        }
        return chooseTaskAndTargets(null, target, distance);
    }

    protected boolean defaultMagicTask() {
        clearTasks();
        final NpcInstance actor = getActor();
        if (actor.isDead()) {
            return false;
        }
        final Creature target;
        if ((target = prepareTarget()) == null) {
            return false;
        }
        final double distance = actor.getDistance(target);
        final Skill[] dam = Rnd.chance(getRateDAM()) ? selectUsableSkills(target, distance, _damSkills) : null;
        final RndSelector<Skill[]> rnd = new RndSelector<>();
        rnd.add(dam, getRateDAM());
        final Skill[] selected = rnd.select();
        return chooseTaskAndTargetsMagic(selectTopSkillByDamage(actor, target, distance, selected), target, distance);
    }

    public int getRatePHYS() {
        return 100;
    }

    public int getRateDOT() {
        return 0;
    }

    public int getRateDEBUFF() {
        return 0;
    }

    public int getRateDAM() {
        return 0;
    }

    public int getRateSTUN() {
        return 0;
    }

    public int getRateBUFF() {
        return 0;
    }

    public int getRateHEAL() {
        return 0;
    }

    public boolean getIsMobile() {
        return !getActor().getParameter("isImmobilized", false);
    }

    public int getMaxPathfindFails() {
        return MAX_PATHFIND_FAILS;
    }

    public int getMaxAttackTimeout() {
        return MAX_ATTACK_TIMEOUT;
    }

    public int getTeleportTimeout() {
        return TELEPORT_TIMEOUT;
    }

    protected Skill getSkillInfo(final int id, final int level) {
        return SkillTable.getInstance().getInfo(id, level);
    }

    public enum TaskType {
        MOVE,
        ATTACK,
        CAST,
        BUFF
    }

    public static class Task {
        public TaskType type;
        public Skill skill;
        public HardReference<? extends Creature> target;
        public Location loc;
        public boolean pathfind;
        public int weight;

        public Task() {
            weight = TaskDefaultWeight;
        }
    }

    private static class TaskComparator implements Comparator<Task> {
        private static final Comparator<Task> instance = new TaskComparator();

        public static Comparator<Task> getInstance() {
            return instance;
        }

        @Override
        public int compare(final Task o1, final Task o2) {
            if (o1 == null || o2 == null) {
                return 0;
            }
            return o2.weight - o1.weight;
        }
    }

    protected class Teleport extends RunnableImpl {
        final Location _destination;

        public Teleport(final Location destination) {
            _destination = destination;
        }

        @Override
        public void runImpl() {
            final NpcInstance actor = getActor();
            if (actor != null) {
                actor.teleToLocation(_destination);
            }
        }
    }

    protected class RunningTask extends RunnableImpl {
        @Override
        public void runImpl() {
            final NpcInstance actor = getActor();
            if (actor != null) {
                actor.setRunning();
            }
            _runningTask = null;
        }
    }

    public class MadnessTask extends RunnableImpl {
        @Override
        public void runImpl() {
            final NpcInstance actor = getActor();
            if (actor != null) {
                actor.stopConfused();
            }
            _madnessTask = null;
        }
    }

    protected class NearestTargetComparator implements Comparator<Creature> {
        private final Creature actor;

        public NearestTargetComparator(final Creature actor) {
            this.actor = actor;
        }

        @Override
        public int compare(final Creature o1, final Creature o2) {
            final double diff = actor.getDistance3D(o1) - actor.getDistance3D(o2);
            if (diff < 0.0) {
                return -1;
            }
            return (diff > 0.0) ? 1 : 0;
        }
    }
}
