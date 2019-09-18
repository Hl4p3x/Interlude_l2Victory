package ru.j2dev.gameserver.model.entity.events.impl;

import com.google.common.collect.Iterators;
import ru.j2dev.commons.collections.MultiValueSet;
import ru.j2dev.gameserver.ai.CtrlEvent;
import ru.j2dev.gameserver.data.xml.holder.InstantZoneHolder;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.model.Request.L2RequestType;
import ru.j2dev.gameserver.model.base.TeamType;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.model.entity.events.objects.DuelSnapshotObject;
import ru.j2dev.gameserver.network.lineage2.components.IStaticPacket;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.*;
import ru.j2dev.gameserver.templates.InstantZone;

import java.util.Iterator;
import java.util.List;

public class PartyVsPartyDuelEvent extends DuelEvent {
    public PartyVsPartyDuelEvent(final MultiValueSet<String> set) {
        super(set);
    }

    protected PartyVsPartyDuelEvent(final int id, final String name) {
        super(id, name);
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
                    DuelSnapshotObject winner = null;
                    try {
                        winner = winners.get(0);
                    } catch (Exception ignored) {
                    }
                    if (winner != null) {
                        sendPacket(((SysMsgContainer) new SystemMessage2(SystemMsg.C1S_PARTY_HAS_WON_THE_DUEL)).addName(winners.get(0).getPlayer()));
                        for (final DuelSnapshotObject d2 : lossers) {
                            d2.getPlayer().broadcastPacket(new SocialAction(d2.getPlayer().getObjectId(), 7));
                        }
                        break;
                    }
                    sendPacket(SystemMsg.THE_DUEL_HAS_ENDED_IN_A_TIE);
                    break;
                }
            }
            updatePlayers(false, true);
        }
        removeObjects(PartyVsPartyDuelEvent.RED_TEAM);
        removeObjects(PartyVsPartyDuelEvent.BLUE_TEAM);
    }

    @Override
    public void teleportPlayers(final String name) {
        final InstantZone instantZone = InstantZoneHolder.getInstance().getInstantZone(1);
        final Reflection reflection = new Reflection();
        reflection.init(instantZone);
        List<DuelSnapshotObject> team = getObjects(PartyVsPartyDuelEvent.BLUE_TEAM);
        for (int i = 0; i < team.size(); ++i) {
            final DuelSnapshotObject $member = team.get(i);
            $member.getPlayer()._stablePoint = $member.getLoc();
            $member.getPlayer().teleToLocation(instantZone.getTeleportCoords().get(i), reflection);
        }
        team = getObjects(PartyVsPartyDuelEvent.RED_TEAM);
        for (int i = 0; i < team.size(); ++i) {
            final DuelSnapshotObject $member = team.get(i);
            $member.getPlayer()._stablePoint = $member.getLoc();
            $member.getPlayer().teleToLocation(instantZone.getTeleportCoords().get(9 + i), reflection);
        }
    }

    @Override
    public boolean canDuel(final Player player, final Player target, final boolean first) {
        if (player.getParty() == null) {
            player.sendPacket(SystemMsg.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME);
            return false;
        }
        if (target.getParty() == null) {
            player.sendPacket(SystemMsg.SINCE_THE_PERSON_YOU_CHALLENGED_IS_NOT_CURRENTLY_IN_A_PARTY_THEY_CANNOT_DUEL_AGAINST_YOUR_PARTY);
            return false;
        }
        final Party party1 = player.getParty();
        final Party party2 = target.getParty();
        if (player != party1.getPartyLeader() || target != party2.getPartyLeader()) {
            player.sendPacket(SystemMsg.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME);
            return false;
        }
        final Iterator<Player> iterator = Iterators.concat(party1.iterator(), party2.iterator());
        while (iterator.hasNext()) {
            final Player $member = iterator.next();
            IStaticPacket packet;
            if ((packet = canDuel0(player, $member)) != null) {
                player.sendPacket(packet);
                target.sendPacket(packet);
                return false;
            }
        }
        return true;
    }

    @Override
    public void askDuel(final Player player, final Player target) {
        final Request request = new Request(L2RequestType.DUEL, player, target).setTimeout(10000L);
        request.set("duelType", 1);
        player.setRequest(request);
        target.setRequest(request);
        player.sendPacket(((SysMsgContainer) new SystemMessage2(SystemMsg.C1S_PARTY_HAS_BEEN_CHALLENGED_TO_A_DUEL)).addName(target));
        target.sendPacket(((SysMsgContainer) new SystemMessage2(SystemMsg.C1S_PARTY_HAS_CHALLENGED_YOUR_PARTY_TO_A_DUEL)).addName(player), new ExDuelAskStart(player.getName(), 1));
    }

    @Override
    public void createDuel(final Player player, final Player target) {
        final PartyVsPartyDuelEvent duelEvent = new PartyVsPartyDuelEvent(getDuelType(), player.getObjectId() + "_" + target.getObjectId() + "_duel");
        cloneTo(duelEvent);
        for (final Player $member : player.getParty()) {
            duelEvent.addObject(PartyVsPartyDuelEvent.BLUE_TEAM, new DuelSnapshotObject($member, TeamType.BLUE));
        }
        for (final Player $member : target.getParty()) {
            duelEvent.addObject(PartyVsPartyDuelEvent.RED_TEAM, new DuelSnapshotObject($member, TeamType.RED));
        }
        duelEvent.sendPacket(new ExDuelReady(this));
        duelEvent.reCalcNextTime(false);
    }

    @Override
    public void playerExit(final Player player) {
        for (final DuelSnapshotObject $snapshot : this) {
            if ($snapshot.getPlayer() == player) {
                removeObject($snapshot.getTeam().name(), $snapshot);
            }
            final List<DuelSnapshotObject> objects = getObjects($snapshot.getTeam().name());
            if (objects.isEmpty()) {
                _winner = $snapshot.getTeam().revert();
                stopEvent();
            }
        }
    }

    @Override
    public void packetSurrender(final Player player) {
    }

    @Override
    public void onDie(final Player player) {
        final TeamType team = player.getTeam();
        if (team == TeamType.NONE || _aborted) {
            return;
        }
        sendPacket(SystemMsg.THE_OTHER_PARTY_IS_FROZEN, team.revert().name());
        player.stopAttackStanceTask();
        player.startFrozen();
        player.setTeam(TeamType.NONE);
        for (final Player $player : World.getAroundPlayers(player)) {
            $player.getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, player);
            if (player.getPet() != null) {
                $player.getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, player.getPet());
            }
        }
        player.sendChanges();
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
        return 1;
    }

    @Override
    protected long startTimeMillis() {
        return System.currentTimeMillis() + 30000L;
    }
}
