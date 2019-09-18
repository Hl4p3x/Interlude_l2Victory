package ai.residences;

import npc.model.residences.SiegeGuardInstance;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ai.CtrlIntention;
import ru.j2dev.gameserver.ai.DefaultAI;
import ru.j2dev.gameserver.geodata.GeoEngine;
import ru.j2dev.gameserver.model.AggroList.AggroInfo;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.utils.Location;

import java.util.List;

public abstract class SiegeGuard extends DefaultAI {
    public SiegeGuard(final NpcInstance actor) {
        super(actor);
        MAX_PURSUE_RANGE = 1000;
    }

    @Override
    public SiegeGuardInstance getActor() {
        return (SiegeGuardInstance) super.getActor();
    }

    @Override
    public int getMaxPathfindFails() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getMaxAttackTimeout() {
        return 0;
    }

    @Override
    protected boolean randomWalk() {
        return false;
    }

    @Override
    protected boolean randomAnimation() {
        return false;
    }

    @Override
    public boolean canSeeInSilentMove(final Playable target) {
        return !target.isSilentMoving() || Rnd.chance(10);
    }

    @Override
    protected boolean checkAggression(final Creature target) {
        final NpcInstance actor = getActor();
        if (getIntention() != CtrlIntention.AI_INTENTION_ACTIVE || !isGlobalAggro()) {
            return false;
        }
        if (target.isAlikeDead() || target.isInvul()) {
            return false;
        }
        if (target.isPlayable()) {
            if (!canSeeInSilentMove((Playable) target)) {
                return false;
            }
            if (!canSeeInHide((Playable) target)) {
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
        final AggroInfo ai = actor.getAggroList().get(target);
        if (ai != null && ai.hate > 0) {
            if (!target.isInRangeZ(actor.getSpawnedLoc(), (long) MAX_PURSUE_RANGE)) {
                return false;
            }
        } else if (!target.isInRangeZ(actor.getSpawnedLoc(), 600L)) {
            return false;
        }
        if (!canAttackCharacter(target)) {
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
    protected boolean isGlobalAggro() {
        return true;
    }

    @Override
    protected void onEvtAggression(final Creature target, final int aggro) {
        final SiegeGuardInstance actor = getActor();
        if (actor.isDead()) {
            return;
        }
        if (target == null || !actor.isAutoAttackable(target)) {
            return;
        }
        super.onEvtAggression(target, aggro);
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
        final Location sloc = actor.getSpawnedLoc();
        if (!actor.isInRange(sloc, 250L)) {
            teleportHome();
            return true;
        }
        return false;
    }

    @Override
    protected Creature prepareTarget() {
        final SiegeGuardInstance actor = getActor();
        if (actor.isDead()) {
            return null;
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

    @Override
    protected boolean canAttackCharacter(final Creature target) {
        return getActor().isAutoAttackable(target);
    }
}
