package ru.j2dev.gameserver.model.entity.events.impl;

import ru.j2dev.commons.collections.MultiValueSet;
import ru.j2dev.gameserver.ai.CtrlEvent;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Request;
import ru.j2dev.gameserver.model.Request.L2RequestType;
import ru.j2dev.gameserver.model.base.TeamType;
import ru.j2dev.gameserver.model.entity.events.objects.DuelSnapshotObject;
import ru.j2dev.gameserver.network.lineage2.components.IStaticPacket;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.*;

import java.util.List;

public class PlayerVsPlayerDuelEvent extends DuelEvent {
    public PlayerVsPlayerDuelEvent(final MultiValueSet<String> set) {
        super(set);
    }

    protected PlayerVsPlayerDuelEvent(final int id, final String name) {
        super(id, name);
    }

    @Override
    public boolean canDuel(final Player player, final Player target, final boolean first) {
        IStaticPacket sm = canDuel0(player, target);
        if (sm != null) {
            player.sendPacket(sm);
            return false;
        }
        sm = canDuel0(target, player);
        if (sm != null) {
            player.sendPacket(SystemMsg.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME);
            return false;
        }
        return true;
    }

    @Override
    public void askDuel(final Player player, final Player target) {
        final Request request = new Request(L2RequestType.DUEL, player, target).setTimeout(10000L);
        request.set("duelType", 0);
        player.setRequest(request);
        target.setRequest(request);
        player.sendPacket(((SysMsgContainer) new SystemMessage2(SystemMsg.C1_HAS_BEEN_CHALLENGED_TO_A_DUEL)).addName(target));
        target.sendPacket(((SysMsgContainer) new SystemMessage2(SystemMsg.C1_HAS_CHALLENGED_YOU_TO_A_DUEL)).addName(player), new ExDuelAskStart(player.getName(), 0));
    }

    @Override
    public void createDuel(final Player player, final Player target) {
        final PlayerVsPlayerDuelEvent duelEvent = new PlayerVsPlayerDuelEvent(getDuelType(), player.getObjectId() + "_" + target.getObjectId() + "_duel");
        cloneTo(duelEvent);
        duelEvent.addObject(PlayerVsPlayerDuelEvent.BLUE_TEAM, new DuelSnapshotObject(player, TeamType.BLUE));
        duelEvent.addObject(PlayerVsPlayerDuelEvent.RED_TEAM, new DuelSnapshotObject(target, TeamType.RED));
        duelEvent.sendPacket(new ExDuelReady(this));
        duelEvent.reCalcNextTime(false);
    }

    @Override
    public void stopEvent() {
        clearActions();
        if (_duelState.compareAndSet(DuelState.EInProgress, DuelState.EEnd)) {
            updatePlayers(false, false);
            for (final DuelSnapshotObject d : this) {
                d.getPlayer().sendPacket(new ExDuelEnd(this));
                final GameObject target = d.getPlayer().getTarget();
                if (target != null) {
                    d.getPlayer().getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, target);
                }
            }
            switch (_winner) {
                case NONE: {
                    sendPacket(SystemMsg.THE_DUEL_HAS_ENDED_IN_A_TIE);
                    break;
                }
                case RED:
                case BLUE: {
                    final List<DuelSnapshotObject> winners = getObjects(_winner.name());
                    final List<DuelSnapshotObject> lossers = getObjects(_winner.revert().name());
                    sendPacket(((SysMsgContainer) new SystemMessage2(SystemMsg.C1_HAS_WON_THE_DUEL)).addName(winners.get(0).getPlayer()));
                    for (final DuelSnapshotObject d2 : lossers) {
                        final Player player = d2.getPlayer();
                        if (player == null) {
                            continue;
                        }
                        player.broadcastPacket(new SocialAction(d2.getPlayer().getObjectId(), 7));
                    }
                    break;
                }
            }
        }
        removeObjects(PlayerVsPlayerDuelEvent.RED_TEAM);
        removeObjects(PlayerVsPlayerDuelEvent.BLUE_TEAM);
    }

    @Override
    public void onDie(final Player player) {
        final TeamType team = player.getTeam();
        if (team == TeamType.NONE || _aborted) {
            return;
        }
        player.stopAttackStanceTask();
        boolean allDead = true;
        final List<DuelSnapshotObject> objs = getObjects(team.name());
        for (final DuelSnapshotObject obj : objs) {
            if (obj.getPlayer() == player) {
                obj.setDead();
            }
            if (!obj.isDead()) {
                allDead = false;
            }
        }
        if (allDead) {
            _winner = team.revert();
            stopEvent();
        }
    }

    @Override
    public int getDuelType() {
        return 0;
    }

    @Override
    public void playerExit(final Player player) {
        if (_winner != TeamType.NONE || _aborted) {
            return;
        }
        _winner = player.getTeam().revert();
        _aborted = false;
        stopEvent();
    }

    @Override
    public void packetSurrender(final Player player) {
        playerExit(player);
    }

    @Override
    protected long startTimeMillis() {
        return System.currentTimeMillis() + 5000L;
    }
}
