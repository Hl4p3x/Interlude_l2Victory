package ru.j2dev.gameserver.ai;

import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.lang.reference.HardReferences;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.network.lineage2.GameClient;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.PositionUtils;

import java.util.concurrent.ScheduledFuture;

public class SummonAI extends PlayableAI {
    private HardReference<Playable> _runAwayTargetRef = HardReferences.emptyRef();

    public SummonAI(final Summon actor) {
        super(actor);
    }

    @Override
    protected void thinkActive() {
        final Summon actor = getActor();
        clearNextAction();
        if (actor.isDepressed()) {
            setAttackTarget(actor.getPlayer());
            changeIntention(CtrlIntention.AI_INTENTION_ATTACK, actor.getPlayer(), null);
            thinkAttack(true);
        } else if (actor.isFollowMode()) {
            changeIntention(CtrlIntention.AI_INTENTION_FOLLOW, actor.getPlayer(), null);
            thinkFollow();
        }
        super.thinkActive();
    }

    @Override
    protected void thinkAttack(final boolean checkRange) {
        final Summon actor = getActor();
        if (actor.isDepressed()) {
            setAttackTarget(actor.getPlayer());
        }
        super.thinkAttack(checkRange);
    }

    private void tryRunAway() {
        final Summon actor = getActor();
        if (!actor.isDead() && !actor.isDepressed()) {
            final Player owner = actor.getPlayer();
            final Playable runAwayTarget = _runAwayTargetRef.get();
            if (owner != null && runAwayTarget != null && !owner.isDead() && !owner.isOutOfControl()) {
                if (runAwayTarget.isInCombat() && actor.getDistance(runAwayTarget) < actor.getActingRange()) {
                    final int radius = CharacterAI.getIndentRange(actor.getActingRange());
                    final Location ownerLoc = owner.getLoc();
                    final Location targetLoc = runAwayTarget.getLoc();
                    final double radian = PositionUtils.convertHeadingToRadian((0x8000 + PositionUtils.getHeadingTo(ownerLoc, targetLoc)) % 0xffff);
                    final Location ne = new Location((int) (0.5 + ownerLoc.getX() + radius * Math.sin(radian)), (int) (0.5 + ownerLoc.getY() + radius * Math.cos(radian)), ownerLoc.getZ()).correctGeoZ();
                    actor.moveToLocation(ne, 0, true);
                    return;
                }
                _runAwayTargetRef = HardReferences.emptyRef();
            } else {
                _runAwayTargetRef = HardReferences.emptyRef();
            }
        }
    }

    @Override
    protected void onEvtArrived() {
        if (!setNextIntention()) {
            if (getIntention() == CtrlIntention.AI_INTENTION_INTERACT || getIntention() == CtrlIntention.AI_INTENTION_PICK_UP || getIntention() == CtrlIntention.AI_INTENTION_FOLLOW) {
                onEvtThink();
            } else {
                changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onEvtAttacked(final Creature attacker, final int damage) {
        final Summon actor = getActor();
        if (attacker != null && actor.getPlayer().isDead() && !actor.isDepressed()) {
            Attack(attacker, false, false);
        }
        if (attacker != null && attacker.isPlayable()) {
            _runAwayTargetRef = (HardReference<Playable>) attacker.getRef();
        }
        super.onEvtAttacked(attacker, damage);
    }

    @Override
    public Summon getActor() {
        return (Summon) super.getActor();
    }

    @Override
    protected ScheduledFuture<?> scheduleThinkFollowTask() {
        return ThreadPoolManager.getInstance().schedule(new ThinkFollowForSummon(getActor()), Config.MOVE_TASK_QUANTUM_NPC);
    }

    protected static class ThinkFollowForSummon extends RunnableImpl {
        private final HardReference<? extends Playable> _actorRef;

        public ThinkFollowForSummon(final Summon actor) {
            _actorRef = actor.getRef();
        }

        @Override
        public void runImpl() {
            final Summon actor = (Summon) _actorRef.get();
            if (actor == null) {
                return;
            }
            final SummonAI actorAI = actor.getAI();
            if (actorAI.getIntention() != CtrlIntention.AI_INTENTION_FOLLOW) {
                if (actorAI.getIntention() == CtrlIntention.AI_INTENTION_ACTIVE) {
                    actor.setFollowMode(false);
                }
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
            final int followRange = actor.getActingRange();
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
            if (dist > followRange) {
                if (!actor.isFollowing() || actor.getFollowTarget() != target) {
                    actor.moveToRelative(target, CharacterAI.getIndentRange(followIndent), followRange);
                }
            } else {
                actorAI.tryRunAway();
            }
            actorAI._followTask = actorAI.scheduleThinkFollowTask();
        }
    }
}
