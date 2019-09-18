package ru.j2dev.gameserver.ai;

import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.geodata.GeoEngine;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.GameClient;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.MyTargetSelected;
import ru.j2dev.gameserver.utils.Location;

import java.util.concurrent.ScheduledFuture;

public class PlayableAI extends CharacterAI {
    protected Object _intention_arg0;
    protected Object _intention_arg1;
    protected Skill _skill;
    protected boolean _forceUse;
    protected ScheduledFuture<?> _followTask;
    private volatile int thinking;
    private NextAction _nextAction;
    private Object _nextAction_arg0;
    private Object _nextAction_arg1;
    private boolean _nextAction_arg2;
    private boolean _nextAction_arg3;
    private boolean _dontMove;

    public PlayableAI(final Playable actor) {
        super(actor);
        thinking = 0;
        _intention_arg0 = null;
        _intention_arg1 = null;
    }

    protected static boolean isThinkImplyZ(final Playable actor, final GameObject target) {
        if (actor.isFlying() || actor.isInWater()) {
            return true;
        }
        if (target != null) {
            if (target.isDoor()) {
                return false;
            }
            if (target.isCreature()) {
                final Creature creature = (Creature) target;
                return creature.isInWater() || creature.isFlying();
            }
        }
        return false;
    }

    @Override
    public void changeIntention(final CtrlIntention intention, final Object arg0, final Object arg1) {
        super.changeIntention(intention, arg0, arg1);
        _intention_arg0 = arg0;
        _intention_arg1 = arg1;
    }

    @Override
    public void setIntention(final CtrlIntention intention, final Object arg0, final Object arg1) {
        _intention_arg0 = null;
        _intention_arg1 = null;
        super.setIntention(intention, arg0, arg1);
    }

    @Override
    protected void onIntentionCast(final Skill skill, final Creature target) {
        super.onIntentionCast(_skill = skill, target);
    }

    public NextAction getNextAction() {
        return _nextAction;
    }

    public boolean isIntendingInteract(final GameObject withTarget) {
        return getIntention() == CtrlIntention.AI_INTENTION_INTERACT && _intention_arg0 == withTarget;
    }

    @Override
    public void setNextAction(final NextAction action, final Object arg0, final Object arg1, final boolean arg2, final boolean arg3) {
        _nextAction = action;
        _nextAction_arg0 = arg0;
        _nextAction_arg1 = arg1;
        _nextAction_arg2 = arg2;
        _nextAction_arg3 = arg3;
    }

    public boolean setNextIntention() {
        final NextAction nextAction = _nextAction;
        final Object nextAction_arg0 = _nextAction_arg0;
        final Object nextAction_arg2 = _nextAction_arg1;
        final boolean nextAction_arg3 = _nextAction_arg2;
        final boolean nextAction_arg4 = _nextAction_arg3;
        final Playable actor = getActor();
        if (nextAction == null || actor.isActionsDisabled()) {
            return false;
        }
        switch (nextAction) {
            case ATTACK: {
                if (nextAction_arg0 == null) {
                    return false;
                }
                final Creature target = (Creature) nextAction_arg0;
                _forceUse = nextAction_arg3;
                _dontMove = nextAction_arg4;
                clearNextAction();
                setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
                break;
            }
            case CAST: {
                if (nextAction_arg0 == null || nextAction_arg2 == null) {
                    return false;
                }
                final Skill skill = (Skill) nextAction_arg0;
                final Creature target = (Creature) nextAction_arg2;
                _forceUse = nextAction_arg3;
                _dontMove = nextAction_arg4;
                clearNextAction();
                if (skill.checkCondition(actor, target, _forceUse, _dontMove, true)) {
                    setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
                    break;
                }
                if (skill.getSkillNextAction() == Skill.SkillNextAction.ATTACK && !actor.equals(target) && !_forceUse) {
                    setNextAction(NextAction.ATTACK, target, null, false, false);
                    return setNextIntention();
                }
                return false;
            }
            case MOVE: {
                if (nextAction_arg0 == null || nextAction_arg2 == null) {
                    return false;
                }
                final Location loc = (Location) nextAction_arg0;
                final Integer offset = (Integer) nextAction_arg2;
                clearNextAction();
                actor.moveToLocation(loc, offset, nextAction_arg3);
                break;
            }
            case REST: {
                actor.sitDown(null);
                break;
            }
            case INTERACT: {
                if (nextAction_arg0 == null) {
                    return false;
                }
                final GameObject object = (GameObject) nextAction_arg0;
                clearNextAction();
                onIntentionInteract(object);
                break;
            }
            case EQUIP: {
                if (nextAction_arg0 == null) {
                    return false;
                }
                final ItemInstance item = (ItemInstance) nextAction_arg0;
                item.getTemplate().getHandler().useItem(getActor(), item, nextAction_arg3);
                clearNextAction();
                break;
            }
            case PICKUP: {
                if (nextAction_arg0 == null) {
                    return false;
                }
                final GameObject object = (GameObject) nextAction_arg0;
                clearNextAction();
                onIntentionPickUp(object);
                break;
            }
            default: {
                return false;
            }
        }
        return true;
    }

    @Override
    public void clearNextAction() {
        _nextAction = null;
        _nextAction_arg0 = null;
        _nextAction_arg1 = null;
        _nextAction_arg2 = false;
        _nextAction_arg3 = false;
    }

    @Override
    protected void onEvtFinishCasting() {
        if (!setNextIntention()) {
            setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
        }
    }

    @Override
    protected void onEvtReadyToAct() {
        if (!setNextIntention()) {
            onEvtThink();
        }
    }

    @Override
    protected void onEvtArrived() {
        if (!setNextIntention()) {
            if (getIntention() == CtrlIntention.AI_INTENTION_INTERACT || getIntention() == CtrlIntention.AI_INTENTION_PICK_UP) {
                onEvtThink();
            } else {
                changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
            }
        }
    }

    @Override
    protected void onEvtArrivedTarget() {
        switch (getIntention()) {
            case AI_INTENTION_ATTACK: {
                thinkAttack(false);
                break;
            }
            case AI_INTENTION_CAST: {
                thinkCast(false);
                break;
            }
            case AI_INTENTION_FOLLOW: {
                thinkFollow();
                break;
            }
            default: {
                onEvtThink();
                break;
            }
        }
    }

    @Override
    protected void onEvtThink() {
        final Playable actor = getActor();
        if (actor.isActionsDisabled()) {
            return;
        }
        try {
            if (thinking++ > 1) {
                return;
            }
            switch (getIntention()) {
                case AI_INTENTION_ACTIVE: {
                    thinkActive();
                    break;
                }
                case AI_INTENTION_ATTACK: {
                    thinkAttack(true);
                    break;
                }
                case AI_INTENTION_CAST: {
                    thinkCast(true);
                    break;
                }
                case AI_INTENTION_PICK_UP: {
                    thinkPickUp();
                    break;
                }
                case AI_INTENTION_INTERACT: {
                    thinkInteract();
                    break;
                }
                case AI_INTENTION_FOLLOW: {
                    thinkFollow();
                    break;
                }
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            --thinking;
        }
    }

    protected void thinkActive() {
    }

    protected void thinkAttack(final boolean checkRange) {
        final Playable actor = getActor();
        final Player player = actor.getPlayer();
        if (player == null) {
            setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
            return;
        }
        if (actor.isActionsDisabled() || actor.isAttackingDisabled()) {
            actor.sendActionFailed();
            return;
        }
        final boolean isPosessed = actor instanceof Summon && ((Summon) actor).isDepressed();
        final Creature attack_target = getAttackTarget();
        if (attack_target != null && !attack_target.isDead()) {
            if (!isPosessed) {
                if (_forceUse) {
                    if (!attack_target.isAttackable(actor)) {
                        return;
                    }
                } else if (!attack_target.isAutoAttackable(actor)) {
                    return;
                }
            }
            if (!checkRange) {
                clientStopMoving();
                actor.doAttack(attack_target);
                return;
            }
            final int clientClipRange = (player.getNetConnection() != null) ? player.getNetConnection().getPawnClippingRange() : GameClient.DEFAULT_PAWN_CLIPPING_RANGE;
            final int collisions = (int) (actor.getColRadius() + attack_target.getColRadius());
            final boolean incZ = isThinkImplyZ(actor, attack_target);
            final int dist = (int) (incZ ? actor.getDistance3D(attack_target) : actor.getDistance(attack_target)) - collisions;
            final boolean useActAsAtkRange = attack_target.isDoor();
            final int atkRange = useActAsAtkRange ? attack_target.getActingRange() : actor.getPhysicalAttackRange();
            final boolean canSee = dist < clientClipRange && GeoEngine.canSeeTarget(actor, attack_target, incZ);
            if (!canSee && (atkRange > 256 || Math.abs(actor.getZ() - attack_target.getZ()) > 256)) {
                actor.sendPacket(SystemMsg.CANNOT_SEE_TARGET);
                setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
                actor.sendActionFailed();
                return;
            }
            if (dist <= atkRange) {
                if (!canSee) {
                    actor.sendPacket(SystemMsg.CANNOT_SEE_TARGET);
                    setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
                    actor.sendActionFailed();
                    return;
                }
                clientStopMoving(false);
                actor.doAttack(attack_target);
            } else if (!_dontMove) {
                final int moveIndent = CharacterAI.getIndentRange(atkRange) + (useActAsAtkRange ? 0 : collisions);
                final int moveRange = Math.max(moveIndent, atkRange + (useActAsAtkRange ? 0 : collisions));
                ThreadPoolManager.getInstance().execute(new RunnableImpl() {
                    @Override
                    public void runImpl() {
                        actor.moveToRelative(attack_target, moveIndent, moveRange);
                    }
                });
            } else {
                actor.sendActionFailed();
            }
            return;
        }
        setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
        actor.sendActionFailed();
    }

    protected void thinkCast(final boolean checkRange) {
        final Playable actor = getActor();
        final Creature target = getAttackTarget();
        if (_skill.getSkillType() == Skill.SkillType.CRAFT || _skill.isToggle()) {
            if (_skill.checkCondition(actor, target, _forceUse, _dontMove, true)) {
                actor.doCast(_skill, target, _forceUse);
            }
            return;
        }
        if (target == null || (target.isDead() != _skill.getCorpse() && !_skill.isNotTargetAoE())) {
            setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
            actor.sendActionFailed();
            return;
        }
        if (!checkRange) {
            if (_skill.getSkillNextAction() == Skill.SkillNextAction.ATTACK && !actor.equals(target) && !_forceUse) {
                setNextAction(NextAction.ATTACK, target, null, false, false);
            } else {
                clearNextAction();
            }
            clientStopMoving();
            if (_skill.checkCondition(actor, target, _forceUse, _dontMove, true)) {
                actor.doCast(_skill, target, _forceUse);
            } else {
                setNextIntention();
                if (getIntention() == CtrlIntention.AI_INTENTION_ATTACK) {
                    thinkAttack(true);
                }
            }
            return;
        }
        final int collisions = (int) (actor.getColRadius() + target.getColRadius());
        final boolean incZ = isThinkImplyZ(actor, target);
        final int dist = (int) (incZ ? actor.getDistance3D(target) : actor.getDistance(target)) - collisions;
        final boolean useActAsCastRange = target.isDoor();
        final int castRange = Math.max(16, actor.getMagicalAttackRange(_skill));
        boolean canSee = false;
        switch (_skill.getSkillType()) {
            case TAKECASTLE: {
                canSee = true;
                break;
            }
            default: {
                canSee = GeoEngine.canSeeTarget(actor, target, actor.isFlying());
                break;
            }
        }
        final boolean noRangeSkill = _skill.getCastRange() == 32767;
        if (!noRangeSkill && !canSee && (castRange > 256 || Math.abs(actor.getZ() - target.getZ()) > 256)) {
            actor.sendPacket(SystemMsg.CANNOT_SEE_TARGET);
            setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
            actor.sendActionFailed();
            return;
        }
        if (noRangeSkill || dist <= castRange) {
            if (!noRangeSkill && !canSee) {
                actor.sendPacket(SystemMsg.CANNOT_SEE_TARGET);
                setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
                actor.sendActionFailed();
                return;
            }
            if (_skill.getSkillNextAction() == Skill.SkillNextAction.ATTACK && !actor.equals(target) && !_forceUse) {
                setNextAction(NextAction.ATTACK, target, null, false, false);
            } else {
                clearNextAction();
            }
            if (_skill.checkCondition(actor, target, _forceUse, _dontMove, true)) {
                clientStopMoving(false);
                actor.doCast(_skill, target, _forceUse);
            } else {
                setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
                actor.sendActionFailed();
            }
        } else if (!_dontMove) {
            final int moveIndent = CharacterAI.getIndentRange(castRange) + (useActAsCastRange ? 0 : collisions);
            final int moveRange = Math.max(moveIndent, castRange + (useActAsCastRange ? 0 : collisions));
            ThreadPoolManager.getInstance().execute(new RunnableImpl() {
                @Override
                public void runImpl() {
                    actor.moveToRelative(target, moveIndent, moveRange);
                }
            });
        } else {
            actor.sendPacket(Msg.YOUR_TARGET_IS_OUT_OF_RANGE);
            setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
            actor.sendActionFailed();
        }
    }

    protected void thinkPickUp() {
        final Playable actor = getActor();
        final GameObject target = (GameObject) _intention_arg0;
        if (target == null) {
            setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
            return;
        }
        if (actor.isInRange(target, target.getActingRange() + 16) && Math.abs(actor.getZ() - target.getZ()) < 64) {
            if (actor.isPlayer() || actor.isPet()) {
                actor.doPickupItem(target);
            }
            setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
        } else {
            final Location moveToLoc = new Location(target.getX() & 0xFFFFFFF8, target.getY() & 0xFFFFFFF8, target.getZ());
            ThreadPoolManager.getInstance().execute(new RunnableImpl() {
                @Override
                public void runImpl() {
                    actor.moveToLocation(moveToLoc, 0, true);
                    setNextAction(NextAction.PICKUP, target, null, false, false);
                }
            });
        }
    }

    protected void thinkInteract() {
        final Playable actor = getActor();
        final GameObject target = (GameObject) _intention_arg0;
        if (target == null) {
            setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
            return;
        }
        final boolean incZ = isThinkImplyZ(actor, target);
        final int dist = (int) (incZ ? actor.getDistance3D(target) : actor.getDistance(target));
        final int actRange = target.getActingRange();
        if (dist <= actRange) {
            if (actor.isPlayer()) {
                ((Player) actor).doInteract(target);
            }
            setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
        } else {
            final int moveIndent = CharacterAI.getIndentRange(actRange);
            ThreadPoolManager.getInstance().execute(new RunnableImpl() {
                @Override
                public void runImpl() {
                    actor.moveToRelative(target, moveIndent, actRange);
                }
            });
            setNextAction(NextAction.INTERACT, target, null, false, false);
        }
    }

    protected void thinkFollow() {
        final Playable actor = getActor();
        final Creature target = (Creature) _intention_arg0;
        if (target == null || target.isAlikeDead()) {
            clientActionFailed();
            return;
        }
        if (actor.isFollowing() && actor.getFollowTarget() == target) {
            clientActionFailed();
            return;
        }
        final int clientClipRange = (actor.getPlayer() != null && actor.getPlayer().getNetConnection() != null) ? actor.getPlayer().getNetConnection().getPawnClippingRange() : GameClient.DEFAULT_PAWN_CLIPPING_RANGE;
        final int collisions = (int) (actor.getColRadius() + target.getColRadius());
        final boolean incZ = isThinkImplyZ(actor, target);
        final int dist = (int) (incZ ? actor.getDistance3D(target) : actor.getDistance(target)) - collisions;
        final int followRange = Config.FOLLOW_ARRIVE_DISTANCE;
        if (dist > clientClipRange) {
            setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
            clientActionFailed();
            clientStopMoving();
            return;
        }
        if (dist <= followRange + 16 || actor.isMovementDisabled()) {
            clientActionFailed();
        }
        if (_followTask != null) {
            _followTask.cancel(false);
            _followTask = null;
        }
        _followTask = scheduleThinkFollowTask();
    }

    protected ScheduledFuture<?> scheduleThinkFollowTask() {
        return ThreadPoolManager.getInstance().schedule(new ThinkFollow(getActor()), Config.MOVE_TASK_QUANTUM_PC);
    }

    @Override
    protected void onIntentionInteract(final GameObject target) {
        final Playable actor = getActor();
        if (actor.isActionsDisabled()) {
            setNextAction(NextAction.INTERACT, target, null, false, false);
            clientActionFailed();
            return;
        }
        clearNextAction();
        changeIntention(CtrlIntention.AI_INTENTION_INTERACT, target, null);
        onEvtThink();
    }

    @Override
    protected void onIntentionPickUp(final GameObject object) {
        final Playable actor = getActor();
        if (actor.isActionsDisabled()) {
            setNextAction(NextAction.PICKUP, object, null, false, false);
            clientActionFailed();
            return;
        }
        clearNextAction();
        changeIntention(CtrlIntention.AI_INTENTION_PICK_UP, object, null);
        onEvtThink();
    }

    @Override
    protected void onEvtDead(final Creature killer) {
        clearNextAction();
        super.onEvtDead(killer);
    }

    @Override
    protected void onEvtFakeDeath() {
        clearNextAction();
        super.onEvtFakeDeath();
    }

    public void lockTarget(final Creature target) {
        final Playable actor = getActor();
        if (target == null || target.isDead()) {
            actor.setAggressionTarget(null);
        } else if (actor.getAggressionTarget() == null) {
            final GameObject actorStoredTarget = actor.getTarget();
            actor.setAggressionTarget(target);
            actor.setTarget(target);
            clearNextAction();
            if (actorStoredTarget != target) {
                actor.sendPacket(new MyTargetSelected(target.getObjectId(), 0));
            }
        }
    }

    @Override
    public void Attack(final GameObject target, final boolean forceUse, final boolean dontMove) {
        final Playable actor = getActor();
        if (target.isCreature() && (actor.isActionsDisabled() || actor.isAttackingDisabled())) {
            setNextAction(NextAction.ATTACK, target, null, forceUse, false);
            actor.sendActionFailed();
            return;
        }
        _dontMove = dontMove;
        _forceUse = forceUse;
        clearNextAction();
        setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
    }

    @Override
    public void Cast(final Skill skill, final Creature target, final boolean forceUse, final boolean dontMove) {
        final Playable actor = getActor();
        if (skill.altUse() || skill.isToggle()) {
            if ((skill.isToggle() || skill.isHandler()) && (actor.isOutOfControl() || actor.isStunned() || actor.isSleeping() || actor.isParalyzed() || actor.isAlikeDead())) {
                clientActionFailed();
            } else {
                actor.altUseSkill(skill, target);
            }
            return;
        }
        if (actor.isActionsDisabled()) {
            setNextAction(NextAction.CAST, skill, target, forceUse, dontMove);
            clientActionFailed();
            return;
        }
        _forceUse = forceUse;
        _dontMove = dontMove;
        clearNextAction();
        setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
    }

    @Override
    public Playable getActor() {
        return (Playable) super.getActor();
    }

    @Override
    protected void onEvtForgetObject(final GameObject object) {
        super.onEvtForgetObject(object);
        if (getIntention() == CtrlIntention.AI_INTENTION_FOLLOW && _intention_arg0 == object) {
            setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
        }
    }

    protected static class ThinkFollow extends RunnableImpl {
        private final HardReference<? extends Playable> _actorRef;

        public ThinkFollow(final Playable actor) {
            _actorRef = actor.getRef();
        }

        @Override
        public void runImpl() {
            final Playable actor = _actorRef.get();
            if (actor == null) {
                return;
            }
            final PlayableAI actorAI = (PlayableAI) actor.getAI();
            if (actorAI.getIntention() != CtrlIntention.AI_INTENTION_FOLLOW) {
                return;
            }
            final Creature target = (Creature) actorAI._intention_arg0;
            if (target == null || target.isAlikeDead()) {
                actorAI.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
                return;
            }
            final int clientClipRange = (actor.getPlayer() != null && actor.getPlayer().getNetConnection() != null) ? actor.getPlayer().getNetConnection().getPawnClippingRange() : GameClient.DEFAULT_PAWN_CLIPPING_RANGE;
            final int collisions = (int) (actor.getColRadius() + target.getColRadius());
            final boolean incZ = PlayableAI.isThinkImplyZ(actor, target);
            final int dist = (int) (incZ ? actor.getDistance3D(target) : actor.getDistance(target)) - collisions;
            final int followIndent = Math.min(clientClipRange, target.getActingRange());
            final int followRange = Config.FOLLOW_ARRIVE_DISTANCE;
            if (dist > clientClipRange || dist > 2 << World.SHIFT_BY) {
                actorAI.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
                actorAI.clientStopMoving();
                return;
            }
            final Player player = actor.getPlayer();
            if (player == null || player.isLogoutStarted() || ((actor.isPet() || actor.isSummon()) && player.getPet() != actor)) {
                actorAI.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
                actorAI.clientStopMoving();
                return;
            }
            if (dist > followRange && (!actor.isFollowing() || actor.getFollowTarget() != target)) {
                actor.moveToRelative(target, followIndent + collisions, followRange + collisions);
            }
            actorAI._followTask = actorAI.scheduleThinkFollowTask();
        }
    }
}
