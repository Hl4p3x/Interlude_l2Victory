package ru.j2dev.gameserver.ai;

import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.Die;
import ru.j2dev.gameserver.taskmanager.tasks.objecttasks.NotifyAITask;
import ru.j2dev.gameserver.utils.Location;

public class CharacterAI extends AbstractAI {
    public CharacterAI(final Creature actor) {
        super(actor);
    }

    protected static int getIndentRange(final int range) {
        return (range < 300) ? (range / 3 * 2) : (range - 100);
    }

    @Override
    protected void onIntentionIdle() {
        clientStopMoving();
        changeIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
    }

    @Override
    protected void onIntentionActive() {
        clientStopMoving();
        changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
        onEvtThink();
    }

    @Override
    protected void onIntentionAttack(final Creature target) {
        setAttackTarget(target);
        clientStopMoving();
        changeIntention(CtrlIntention.AI_INTENTION_ATTACK, target, null);
        onEvtThink();
    }

    @Override
    protected void onIntentionCast(final Skill skill, final Creature target) {
        setAttackTarget(target);
        changeIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
        onEvtThink();
    }

    @Override
    protected void onIntentionFollow(final Creature target) {
        changeIntention(CtrlIntention.AI_INTENTION_FOLLOW, target, null);
        onEvtThink();
    }

    @Override
    protected void onIntentionInteract(final GameObject object) {
    }

    @Override
    protected void onIntentionPickUp(final GameObject item) {
    }

    @Override
    protected void onIntentionRest() {
    }

    @Override
    protected void onEvtArrivedBlocked(final Location blocked_at_pos) {
        final Creature actor = getActor();
        if (actor.isPlayer()) {
            final Location loc = ((Player) actor).getLastServerPosition();
            if (loc != null) {
                actor.setLoc(loc, true);
            }
            if (actor.isMoving()) {
                actor.stopMove();
            }
        }
        onEvtThink();
    }

    @Override
    protected void onEvtForgetObject(final GameObject object) {
        if (object == null) {
            return;
        }
        final Creature actor = getActor();
        if (actor.isAttackingNow() && getAttackTarget() == object) {
            actor.abortAttack(true, true);
        }
        if (actor.isCastingNow() && getAttackTarget() == object) {
            actor.abortCast(true, true);
        }
        if (getAttackTarget() == object) {
            setAttackTarget(null);
        }
        if (actor.getTargetId() == object.getObjectId()) {
            actor.setTarget(null);
        }
        if (actor.getFollowTarget() == object) {
            actor.stopMove();
        }
        if (actor.getPet() != null) {
            actor.getPet().getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, object);
        }
    }

    @Override
    protected void onEvtDead(final Creature killer) {
        final Creature actor = getActor();
        actor.abortAttack(true, true);
        actor.abortCast(true, true);
        actor.stopMove();
        actor.broadcastPacket(new Die(actor));
        setIntention(CtrlIntention.AI_INTENTION_IDLE);
    }

    @Override
    protected void onEvtFakeDeath() {
        clientStopMoving();
        setIntention(CtrlIntention.AI_INTENTION_IDLE);
    }

    @Override
    protected void onEvtAttacked(final Creature attacker, final int damage) {
    }

    @Override
    protected void onEvtClanAttacked(final Creature attacked_member, final Creature attacker, final int damage) {
    }

    public void Attack(final GameObject target, final boolean forceUse, final boolean dontMove) {
        setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
    }

    public void Cast(final Skill skill, final Creature target) {
        Cast(skill, target, false, false);
    }

    public void Cast(final Skill skill, final Creature target, final boolean forceUse, final boolean dontMove) {
        setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
    }

    @Override
    protected void onEvtThink() {
    }

    @Override
    protected void onEvtAggression(final Creature target, final int aggro) {
    }

    @Override
    protected void onEvtFinishCasting() {
    }

    @Override
    protected void onEvtReadyToAct() {
    }

    @Override
    protected void onEvtArrived() {
    }

    @Override
    protected void onEvtArrivedTarget() {
    }

    @Override
    protected void onEvtSeeSpell(final Skill skill, final Creature caster) {
    }

    @Override
    protected void onEvtSpawn() {
    }

    @Override
    public void onEvtDeSpawn() {
    }

    public void stopAITask() {
    }

    public void startAITask() {
    }

    public void setNextAction(final NextAction action, final Object arg0, final Object arg1, final boolean arg2, final boolean arg3) {
    }

    public void clearNextAction() {
    }

    public boolean isActive() {
        return true;
    }

    @Override
    protected void onEvtTimerFiredEx(final int timerId, final Object arg1, final Object arg2) {
    }

    @Override
    protected void onEvtManipulation(Creature target, int aggro) {
        onEvtAggression(target, aggro);
    }

    @Override
    protected void onEvtTimer(int timerId, Object arg1, Object arg2) {

    }

    @Override
    protected void onEvtOutOfMyTerritory() {

    }

    @Override
    protected void onEvtScriptEvent(int eventId, Object arg1, Object arg2) {

    }

    @Override
    protected void onEvtSpelled(Skill skill, Creature caster) {

    }

    @Override
    protected void onEvtSeeCreature(Creature creature) {

    }

    @Override
    protected void onEvtCreatureLost(Creature creature, int objectId) {

    }

    @Override
    protected void onEvtAbnormalStatusChanged(Creature speller, Effect effect, boolean added) {

    }

    @Override
    protected void onEvtPartyDead(NpcInstance partyPrivate) {

    }

    @Override
    protected void onEvtDieSet(Creature talker) {

    }

    @Override
    protected void onEvtClanDead(NpcInstance clanPrivate) {

    }

    @Override
    protected void onEvtPartyAttacked(Creature attacker, Creature victim, int damage) {

    }

    @Override
    protected void onEvtNoDesire() {

    }

    @Override
    protected void onEvtTrapStepIn(Creature cha) {

    }

    @Override
    protected void onEvtTrapStepOut(Creature cha) {

    }

    @Override
    protected void onEvtTrapActivated() {

    }

    @Override
    protected void onEvtTrapDetected(Creature cha) {

    }

    @Override
    protected void onEvtTrapDefused(Creature cha) {

    }

    @Override
    protected void onEvtMenuSelected(Player player, int ask, int reply) {

    }

    @Override
    protected void onEvtScriptEvent(ScriptEvent event, Object arg1, Object arg2) {

    }

    public void AddTimerEx(final int timerId, final long delay) {
        AddTimerEx(timerId, null, null, delay);
    }

    public void AddTimerEx(final int timerId, final Object arg1, final long delay) {
        AddTimerEx(timerId, arg1, null, delay);
    }

    public void AddTimerEx(final int timerId, final Object arg1, final Object arg2, final long delay) {
        ThreadPoolManager.getInstance().schedule(new TimerEx(timerId, arg1, arg2), delay);
    }

    protected class TimerEx extends RunnableImpl {
        private final int _timerId;
        private final Object _arg1;
        private final Object _arg2;

        public TimerEx(final int timerId, final Object arg1, final Object arg2) {
            _timerId = timerId;
            _arg1 = arg1;
            _arg2 = arg2;
        }

        @Override
        public void runImpl() {
            notifyEvent(CtrlEvent.EVT_TIMER_FIRED_EX, _timerId, _arg1, _arg2);
        }
    }

    public void sendScriptEvent(Creature target, int eventId, java.lang.Object arg1, java.lang.Object arg2) {
        if (target == null)
            return;

        ThreadPoolManager.getInstance().schedule(new NotifyAITask(target, CtrlEvent.EVT_SCRIPT_EVENT, eventId, arg1, arg2), 2000);
    }
}
