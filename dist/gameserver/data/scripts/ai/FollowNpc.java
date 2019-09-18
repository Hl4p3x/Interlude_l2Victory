package ai;

import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.ai.CtrlIntention;
import ru.j2dev.gameserver.ai.DefaultAI;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.instances.MonsterInstance;
import ru.j2dev.gameserver.model.instances.NpcInstance;

import java.util.concurrent.ScheduledFuture;

public class FollowNpc extends DefaultAI {
    private boolean _thinking;
    private ScheduledFuture<?> _followTask;

    public FollowNpc(final NpcInstance actor) {
        super(actor);
        _thinking = false;
        AI_TASK_DELAY_CURRENT = 1000L;
        AI_TASK_ACTIVE_DELAY = 1000L;
    }

    @Override
    protected boolean randomWalk() {
        return getActor() instanceof MonsterInstance;
    }

    @Override
    protected void onEvtThink() {
        final NpcInstance actor = getActor();
        if (_thinking || actor.isActionsDisabled() || actor.isAfraid() || actor.isDead() || actor.isMovementDisabled()) {
            return;
        }
        _thinking = true;
        try {
            if (!Config.BLOCK_ACTIVE_TASKS && (getIntention() == CtrlIntention.AI_INTENTION_ACTIVE || getIntention() == CtrlIntention.AI_INTENTION_IDLE)) {
                thinkActive();
            } else if (getIntention() == CtrlIntention.AI_INTENTION_FOLLOW) {
                thinkFollow();
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            _thinking = false;
        }
    }

    protected void thinkFollow() {
        final NpcInstance actor = getActor();
        Creature target = actor.getFollowTarget();
        if (target == null) {
            target = actor.getPlayer();
        }
        if (target == null || target.isAlikeDead() || actor.getDistance(target) > 4000.0 || actor.isMovementDisabled()) {
            clientActionFailed();
            return;
        }
        if (actor.isFollowing() && actor.getFollowTarget() == target) {
            clientActionFailed();
            return;
        }
        if (actor.isInRange(target, 100L)) {
            clientActionFailed();
        }
        if (_followTask != null) {
            _followTask.cancel(false);
            _followTask = null;
        }
        _followTask = ThreadPoolManager.getInstance().schedule(new ThinkFollow(), 500L);
    }

    protected class ThinkFollow extends RunnableImpl {
        public NpcInstance getActor() {
            return FollowNpc.this.getActor();
        }

        @Override
        public void runImpl() {
            final NpcInstance actor = getActor();
            if (actor == null) {
                return;
            }
            Creature target = actor.getFollowTarget();
            if (target == null) {
                target = actor.getPlayer();
            }
            if (target == null || target.isAlikeDead() || actor.getDistance(target) > 4000.0) {
                setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
                return;
            }
            if (!actor.isInRange(target, 150L) && (!actor.isFollowing() || actor.getFollowTarget() != target)) {
                actor.moveToRelative(target, 100, 150, false);
            }
            _followTask = ThreadPoolManager.getInstance().schedule(this, 500L);
        }
    }
}
