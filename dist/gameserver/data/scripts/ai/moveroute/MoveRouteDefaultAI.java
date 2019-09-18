package ai.moveroute;

import ru.j2dev.gameserver.data.xml.holder.MoveRouteHolder;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ai.CtrlIntention;
import ru.j2dev.gameserver.ai.DefaultAI;
import ru.j2dev.gameserver.geodata.GeoEngine;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.MinionList;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.instances.MinionInstance;
import ru.j2dev.gameserver.model.instances.MonsterInstance;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SocialAction;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.templates.moveroute.MoveNode;
import ru.j2dev.gameserver.templates.moveroute.MoveRoute;
import ru.j2dev.gameserver.utils.Location;

import java.util.List;

public class MoveRouteDefaultAI extends DefaultAI {
    private final MoveRoute _moveRoute;
    private MoveNode _targetMoveNode;
    private boolean _goBackCircle;
    private boolean _nodeDelay;

    public MoveRouteDefaultAI(final NpcInstance actor) {
        super(actor);
        final String moveRoute = actor.getParameter("moveroute", null);
        _moveRoute = ((moveRoute == null) ? null : MoveRouteHolder.getInstance().getRoute(moveRoute));
    }

    @Override
    protected void onEvtSpawn() {
        super.onEvtSpawn();
        if (_moveRoute == null) {
            return;
        }
        _goBackCircle = false;
        if (_moveRoute.isRunning()) {
            getActor().setRunning();
        } else {
            getActor().setWalking();
        }
    }

    @Override
    protected Location getPursueBaseLoc() {
        return (_targetMoveNode != null) ? _targetMoveNode : super.getPursueBaseLoc();
    }

    @Override
    protected boolean thinkActive() {
        if (_moveRoute == null) {
            return super.thinkActive();
        }
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
                final List<Creature> chars = World.getAroundCharacters(actor);
                chars.sort(_nearestTargetComparator);
                for (final Creature target : chars) {
                    if ((aggressive || actor.getAggroList().get(target) != null) && checkAggression(target)) {
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
        if (randomAnimation()) {
            return true;
        }
        if (randomWalk()) {
            return true;
        }
        if (!actor.isMoving() && !actor.isFollowing()) {
            final Location currLoc = actor.getLoc();
            final int nearestMoveNodeIdx = getNearestMoveNodeIdx();
            final MoveNode nearestMoveNode = _moveRoute.getNodes().get(nearestMoveNodeIdx);
            final double nearestToCurrDist = nearestMoveNode.distance3D(currLoc);
            if (nearestToCurrDist > Math.max(64.0, 4.0 * actor.getCollisionRadius())) {
                _targetMoveNode = nearestMoveNode;
                returnHome(true, !GeoEngine.canMoveToCoord(currLoc.getX(), currLoc.getY(), currLoc.getZ(), nearestMoveNode.getX(), nearestMoveNode.getY(), nearestMoveNode.getZ(), actor.getGeoIndex()));
            } else {
                if (nearestMoveNode.getSocialId() > 0) {
                    actor.broadcastPacketToOthers(new SocialAction(actor.getObjectId(), nearestMoveNode.getSocialId()));
                }
                if (nearestMoveNode.getNpcMsgAddress() != null) {
                    Functions.npcSayCustomMessage(actor, nearestMoveNode.getChatType(), nearestMoveNode.getNpcMsgAddress());
                }
                if (!_nodeDelay && nearestMoveNode.getDelay() > 0L) {
                    _nodeDelay = true;
                    setIsInRandomAnimation(nearestMoveNode.getDelay());
                    return true;
                }
                _nodeDelay = false;
                if (_moveRoute.isRunning()) {
                    actor.setRunning();
                } else {
                    actor.setWalking();
                }
                final int nextNodeIdx = getNextMoveNodeIdx(nearestMoveNodeIdx);
                final MoveNode nextNode = _moveRoute.getNodes().get(nextNodeIdx);
                addTaskMove(_targetMoveNode = nextNode, true);
                if (actor.hasMinions()) {
                    attendMinions();
                }
                return doTask();
            }
        }
        return false;
    }

    private int getNextMoveNodeIdx(final int currNodeIdx) {
        switch (_moveRoute.getType()) {
            case LOOP: {
                return (currNodeIdx + 1 < _moveRoute.getNodes().size()) ? (currNodeIdx + 1) : 0;
            }
            case CIRCLE: {
                if (!_goBackCircle) {
                    if (currNodeIdx + 1 < _moveRoute.getNodes().size()) {
                        return currNodeIdx + 1;
                    }
                    _goBackCircle = true;
                    return currNodeIdx - 1;
                } else {
                    if (currNodeIdx - 1 > 0) {
                        return currNodeIdx - 1;
                    }
                    _goBackCircle = false;
                    return currNodeIdx + 1;
                }
            }
            default: {
                return -1;
            }
        }
    }

    private void attendMinions() {
        final NpcInstance actor = getActor();
        final MinionList minionList = actor.getMinionList();
        if (minionList.hasAliveMinions()) {
            for (final NpcInstance minion : minionList.getAliveMinions()) {
                if (!minion.isInCombat() && !minion.isAfraid()) {
                    if (_moveRoute.isRunning()) {
                        minion.setRunning();
                    } else {
                        minion.setWalking();
                    }
                    minion.moveToRelative(getActor(), 400, 500, true);
                }
            }
        }
    }

    private int getNearestMoveNodeIdx() {
        if (_moveRoute == null) {
            return -1;
        }
        final NpcInstance actor = getActor();
        final List<MoveNode> nodes = _moveRoute.getNodes();
        int result = -1;
        long nearestNodeDistSq = Long.MAX_VALUE;
        for (int nodeIdx = 0; nodeIdx < nodes.size(); ++nodeIdx) {
            final MoveNode moveNode = nodes.get(nodeIdx);
            final long moveNodeDistSq = actor.getXYZDeltaSq(moveNode);
            if (moveNodeDistSq < nearestNodeDistSq) {
                nearestNodeDistSq = moveNodeDistSq;
                result = nodeIdx;
            }
        }
        return result;
    }

    @Override
    public boolean isGlobalAI() {
        return false;
    }
}
