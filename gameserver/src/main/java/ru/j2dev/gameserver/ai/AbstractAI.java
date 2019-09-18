package ru.j2dev.gameserver.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.lang.reference.HardReferences;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.utils.Location;

public abstract class AbstractAI extends RunnableImpl {
    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractAI.class);

    protected final Creature _actor;
    private HardReference<? extends Creature> _attackTarget;
    private CtrlIntention _intention;

    protected AbstractAI(final Creature actor) {
        _attackTarget = HardReferences.emptyRef();
        _intention = CtrlIntention.AI_INTENTION_IDLE;
        _actor = actor;
    }

    @Override
    public void runImpl() {
    }

    public void changeIntention(final CtrlIntention intention, final Object arg0, final Object arg1) {
        _intention = intention;
        if (intention != CtrlIntention.AI_INTENTION_CAST && intention != CtrlIntention.AI_INTENTION_ATTACK) {
            setAttackTarget(null);
        }
    }

    public final void setIntention(final CtrlIntention intention, final Object arg0) {
        setIntention(intention, arg0, null);
    }

    public void setIntention(CtrlIntention intention, final Object arg0, final Object arg1) {
        if (intention != CtrlIntention.AI_INTENTION_CAST && intention != CtrlIntention.AI_INTENTION_ATTACK) {
            setAttackTarget(null);
        }
        final Creature actor = getActor();
        if (!actor.isVisible()) {
            if (_intention == CtrlIntention.AI_INTENTION_IDLE) {
                return;
            }
            intention = CtrlIntention.AI_INTENTION_IDLE;
        }
        actor.getListeners().onAiIntention(intention, arg0, arg1);
        switch (intention) {
            case AI_INTENTION_IDLE: {
                onIntentionIdle();
                break;
            }
            case AI_INTENTION_ACTIVE: {
                onIntentionActive();
                break;
            }
            case AI_INTENTION_REST: {
                onIntentionRest();
                break;
            }
            case AI_INTENTION_ATTACK: {
                onIntentionAttack((Creature) arg0);
                break;
            }
            case AI_INTENTION_CAST: {
                onIntentionCast((Skill) arg0, (Creature) arg1);
                break;
            }
            case AI_INTENTION_PICK_UP: {
                onIntentionPickUp((GameObject) arg0);
                break;
            }
            case AI_INTENTION_INTERACT: {
                onIntentionInteract((GameObject) arg0);
                break;
            }
            case AI_INTENTION_FOLLOW: {
                onIntentionFollow((Creature) arg0);
                break;
            }
        }
    }

    public final void notifyEvent(final CtrlEvent evt) {
        notifyEvent(evt, new Object[0]);
    }

    public final void notifyEvent(final CtrlEvent evt, final Object arg0) {
        notifyEvent(evt, new Object[] { arg0 });
    }

    public final void notifyEvent(final CtrlEvent evt, final Object arg0, final Object arg1) {
        notifyEvent(evt, new Object[] { arg0, arg1 });
    }

    public final void notifyEvent(final CtrlEvent evt, final Object arg0, final Object arg1, final Object arg2) {
        notifyEvent(evt, new Object[] { arg0, arg1, arg2 });
    }

    public void notifyEvent(final CtrlEvent evt, final Object[] args) {
        final Creature actor = getActor();
        if (actor == null || !actor.isVisible()) {
            return;
        }

        actor.getListeners().onAiEvent(evt, args);

        switch (evt) {
            case EVT_THINK:
                onEvtThink();
                break;
            case EVT_ATTACKED:
                onEvtAttacked((Creature) args[0], ((Number) args[1]).intValue());
                break;
            case EVT_CLAN_ATTACKED:
                onEvtClanAttacked((Creature) args[0], (Creature) args[1], ((Number) args[2]).intValue());
                break;
            case EVT_AGGRESSION:
                onEvtAggression((Creature) args[0], ((Number) args[1]).intValue());
                break;
            case EVT_MANIPULATION:
                onEvtManipulation((Creature) args[0], ((Number) args[1]).intValue());
                break;
            case EVT_READY_TO_ACT:
                onEvtReadyToAct();
                break;
            case EVT_ARRIVED:
                onEvtArrived();
                break;
            case EVT_ARRIVED_TARGET:
                onEvtArrivedTarget();
                break;
            case EVT_ARRIVED_BLOCKED:
                onEvtArrivedBlocked((Location) args[0]);
                break;
            case EVT_FORGET_OBJECT:
                onEvtForgetObject((GameObject) args[0]);
                break;
            case EVT_DEAD:
                onEvtDead((Creature) args[0]);
                break;
            case EVT_FAKE_DEATH:
                onEvtFakeDeath();
                break;
            case EVT_FINISH_CASTING:
                onEvtFinishCasting(); //(Skill) args[0]
                break;
            case EVT_SEE_SPELL:
                onEvtSeeSpell((Skill) args[0], (Creature) args[1]);
                break;
            case EVT_SPAWN:
                onEvtSpawn();
                break;
            case EVT_DESPAWN:
                onEvtDeSpawn();
                break;
            case EVT_TIMER:
                onEvtTimer(((Number) args[0]).intValue(), args[1], args[2]);
                break;
            case EVT_OUT_OF_MY_TERRITORY:
                onEvtOutOfMyTerritory();
                break;
            case EVT_SCRIPT_EVENT:
                onEvtScriptEvent(((Number) args[0]).intValue(), args[1], args[2]);
                break;
            case EVT_SPELLED:
                onEvtSpelled((Skill) args[0], (Creature) args[1]);
                break;
            case EVT_SEE_CREATURE:
                onEvtSeeCreature((Creature) args[0]);
                break;
            case EVT_CREATURE_LOST:
                onEvtCreatureLost(args[0] instanceof Creature ? (Creature) args[0] : null, (Integer) args[1]);
                break;
            case EVT_ABNORMAL_STATUS_CHANGED:
                onEvtAbnormalStatusChanged((Creature) args[0], (Effect) args[1], (Boolean) args[2]);
                break;
            case EVT_PARTY_DIED:
                onEvtPartyDead((NpcInstance) args[0]);
                break;
            case EVT_CLAN_DIED:
                onEvtClanDead((NpcInstance) args[0]);
                break;
            case EVT_DIE_SET:
                onEvtDieSet((Creature) args[0]);
                break;
            case EVT_PARTY_ATTACKED:
                onEvtPartyAttacked((Creature) args[0], (Creature) args[1], (Integer) args[2]);
                break;
            case EVT_NO_DESIRE:
                onEvtNoDesire();
                break;
            case EVT_TRAP_STEP_IN:
                onEvtTrapStepIn((Creature) args[0]);
                break;
            case EVT_TRAP_STEP_OUT:
                onEvtTrapStepOut((Creature) args[0]);
                break;
            case EVT_TRAP_ACTIVATED:
                onEvtTrapActivated();
                break;
            case EVT_TRAP_DETECTED:
                onEvtTrapDetected((Creature) args[0]);
                break;
            case EVT_TRAP_DEFUSED:
                onEvtTrapDefused((Creature) args[0]);
                break;
            case EVT_MENU_SELECTED:
                onEvtMenuSelected((Player) args[0], ((Number) args[1]).intValue(), ((Number) args[2]).intValue());
                break;
            case EVT_TIMER_FIRED_EX:
                onEvtTimerFiredEx(((Number) args[0]).intValue(), args[1], args[2]);
                break;
        }
    }

    protected void clientActionFailed() {
        final Creature actor = getActor();
        if (actor != null && actor.isPlayer()) {
            actor.sendActionFailed();
        }
    }

    public void clientStopMoving(final boolean validate) {
        final Creature actor = getActor();
        actor.stopMove(validate);
    }

    public void clientStopMoving() {
        final Creature actor = getActor();
        actor.stopMove();
    }

    public Creature getActor() {
        return _actor;
    }

    public CtrlIntention getIntention() {
        return _intention;
    }

    public final void setIntention(final CtrlIntention intention) {
        setIntention(intention, null, null);
    }

    public Creature getAttackTarget() {
        return _attackTarget.get();
    }

    public void setAttackTarget(final Creature target) {
        _attackTarget = ((target == null) ? HardReferences.emptyRef() : target.getRef());
    }

    public boolean isGlobalAI() {
        return false;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " for " + getActor();
    }

    protected abstract void onIntentionIdle();

    protected abstract void onIntentionActive();

    protected abstract void onIntentionRest();

    protected abstract void onIntentionAttack(final Creature p0);

    protected abstract void onIntentionCast(final Skill p0, final Creature p1);

    protected abstract void onIntentionPickUp(final GameObject p0);

    protected abstract void onIntentionInteract(final GameObject p0);

    protected abstract void onEvtThink();

    protected abstract void onEvtAttacked(final Creature p0, final int p1);

    protected abstract void onEvtClanAttacked(final Creature p0, final Creature p1, final int p2);

    protected abstract void onEvtAggression(final Creature p0, final int p1);

    protected abstract void onEvtReadyToAct();

    protected abstract void onEvtArrived();

    protected abstract void onEvtArrivedTarget();

    protected abstract void onEvtArrivedBlocked(final Location p0);

    protected abstract void onEvtForgetObject(final GameObject p0);

    protected abstract void onEvtDead(final Creature p0);

    protected abstract void onEvtFakeDeath();

    protected abstract void onEvtFinishCasting();

    protected abstract void onEvtSeeSpell(final Skill p0, final Creature p1);

    protected abstract void onEvtSpawn();

    public abstract void onEvtDeSpawn();

    protected abstract void onIntentionFollow(final Creature p0);

    protected abstract void onEvtTimerFiredEx(final int p0, final Object p1, final Object p2);



    protected abstract void onEvtManipulation(Creature target, int aggro);

    protected abstract void onEvtTimer(int timerId, Object arg1, Object arg2);

    protected abstract void onEvtOutOfMyTerritory();

    protected abstract void onEvtScriptEvent(int eventId, Object arg1, Object arg2);

    protected abstract void onEvtSpelled(Skill skill, Creature caster);

    protected abstract void onEvtSeeCreature(Creature creature);

    protected abstract void onEvtCreatureLost(Creature creature, int objectId);

    protected abstract void onEvtAbnormalStatusChanged(Creature speller, Effect effect, boolean added);

    protected abstract void onEvtPartyDead(NpcInstance partyPrivate);

    protected abstract void onEvtDieSet(Creature talker);

    protected abstract void onEvtClanDead(NpcInstance clanPrivate);

    protected abstract void onEvtPartyAttacked(Creature attacker, Creature victim, int damage);

    protected abstract void onEvtNoDesire();

    protected abstract void onEvtTrapStepIn(Creature cha);

    protected abstract void onEvtTrapStepOut(Creature cha);

    protected abstract void onEvtTrapActivated();

    protected abstract void onEvtTrapDetected(Creature cha);

    protected abstract void onEvtTrapDefused(Creature cha);

    protected abstract void onEvtMenuSelected(Player player, int ask, int reply);

    protected abstract void onEvtScriptEvent(ScriptEvent event, Object arg1, Object arg2);
}
