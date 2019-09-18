package ai.residences.clanhall;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ai.CtrlIntention;
import ru.j2dev.gameserver.ai.Fighter;
import ru.j2dev.gameserver.geodata.GeoEngine;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.instances.residences.clanhall.CTBBossInstance;
import ru.j2dev.gameserver.utils.Location;

import java.util.List;

public abstract class MatchFighter extends Fighter {
    public MatchFighter(final NpcInstance actor) {
        super(actor);
    }

    @Override
    protected boolean thinkActive() {
        final NpcInstance actor = getActor();
        if (actor.isActionsDisabled()) {
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
            final List<Creature> chars = World.getAroundCharacters(actor);
            chars.sort(_nearestTargetComparator);
            for (final Creature target : chars) {
                if (checkAggression(target)) {
                    actor.getAggroList().addDamageHate(target, 0, 2);
                    if (target.isSummon()) {
                        actor.getAggroList().addDamageHate(target.getPlayer(), 0, 1);
                    }
                    startRunningTask(AI_TASK_ATTACK_DELAY);
                    setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
                    return true;
                }
            }
        }
        return randomWalk();
    }

    @Override
    protected boolean checkAggression(final Creature target) {
        final CTBBossInstance actor = getActor();
        if (getIntention() != CtrlIntention.AI_INTENTION_ACTIVE) {
            return false;
        }
        if (target.isAlikeDead() || target.isInvul()) {
            return false;
        }
        if (!actor.isAttackable(target)) {
            return false;
        }
        if (!GeoEngine.canSeeTarget(actor, target, false)) {
            return false;
        }
        actor.getAggroList().addDamageHate(target, 0, 2);
        if (target.isSummon() || target.isPet()) {
            actor.getAggroList().addDamageHate(target.getPlayer(), 0, 1);
        }
        startRunningTask(AI_TASK_ATTACK_DELAY);
        setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
        return true;
    }

    @Override
    protected boolean canAttackCharacter(final Creature target) {
        final NpcInstance actor = getActor();
        return actor.isAttackable(target);
    }

    @Override
    public void onEvtSpawn() {
        super.onEvtSpawn();
        final CTBBossInstance actor = getActor();
        final int x = (int) (actor.getX() + 800.0 * Math.cos(actor.headingToRadians(actor.getHeading() - 32768)));
        final int y = (int) (actor.getY() + 800.0 * Math.sin(actor.headingToRadians(actor.getHeading() - 32768)));
        actor.setSpawnedLoc(new Location(x, y, actor.getZ()));
        addTaskMove(actor.getSpawnedLoc(), true);
        doTask();
    }

    @Override
    public boolean isGlobalAI() {
        return true;
    }

    @Override
    public CTBBossInstance getActor() {
        return (CTBBossInstance) super.getActor();
    }
}
