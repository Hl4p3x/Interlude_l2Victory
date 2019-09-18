package ru.j2dev.gameserver.model.entity.events.impl;

import com.google.common.collect.Iterators;
import ru.j2dev.commons.collections.MultiValueSet;
import ru.j2dev.gameserver.listener.actor.player.OnPlayerExitListener;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.Zone.ZoneType;
import ru.j2dev.gameserver.model.base.TeamType;
import ru.j2dev.gameserver.model.entity.events.GlobalEvent;
import ru.j2dev.gameserver.model.entity.events.objects.DuelSnapshotObject;
import ru.j2dev.gameserver.network.lineage2.components.IStaticPacket;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.*;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public abstract class DuelEvent extends GlobalEvent implements Iterable<DuelSnapshotObject> {
    public static final String RED_TEAM = TeamType.RED.name();
    public static final String BLUE_TEAM = TeamType.BLUE.name();

    protected final AtomicReference<DuelState> _duelState;
    protected final OnPlayerExitListener _playerExitListener;
    protected TeamType _winner;
    protected boolean _aborted;

    public DuelEvent(final MultiValueSet<String> set) {
        super(set);
        _playerExitListener = new OnPlayerExitListenerImpl();
        _winner = TeamType.NONE;
        _duelState = new AtomicReference<>(DuelState.EPrepare);
    }

    protected DuelEvent(final int id, final String name) {
        super(id, name);
        _playerExitListener = new OnPlayerExitListenerImpl();
        _winner = TeamType.NONE;
        _duelState = new AtomicReference<>(DuelState.EPrepare);
    }

    @Override
    public void initEvent() {
    }

    public abstract boolean canDuel(final Player p0, final Player p1, final boolean p2);

    public abstract void askDuel(final Player p0, final Player p1);

    public abstract void createDuel(final Player p0, final Player p1);

    public abstract void playerExit(final Player p0);

    public abstract void packetSurrender(final Player p0);

    public abstract void onDie(final Player p0);

    public abstract int getDuelType();

    private boolean canStart() {
        if (_duelState.get() != DuelState.EPrepare) {
            return false;
        }
        for (final DuelSnapshotObject dso : this) {
            final Player player = dso.getPlayer();
            if (player == null) {
                return false;
            }
            final IStaticPacket pkt = checkPlayer(player);
            if (pkt != null) {
                sendPacket(pkt);
                abortDuel(player);
                return false;
            }
        }
        return true;
    }

    @Override
    public void action(final String name, final boolean start) {
        if ("event".equalsIgnoreCase(name)) {
            if (start) {
                if (canStart()) {
                    startEvent();
                }
            } else {
                stopEvent();
            }
        }
    }

    @Override
    public void startEvent() {
        if (_duelState.compareAndSet(DuelState.EPrepare, DuelState.EInProgress)) {
            updatePlayers(true, false);
            sendPackets(new ExDuelStart(this), PlaySound.B04_S01, SystemMsg.LET_THE_DUEL_BEGIN);
            for (final DuelSnapshotObject $snapshot : this) {
                sendPacket(new ExDuelUpdateUserInfo($snapshot.getPlayer()), $snapshot.getTeam().revert().name());
            }
        }
    }

    public void sendPacket(final IStaticPacket packet, final String... ar) {
        for (final String a : ar) {
            final List<DuelSnapshotObject> objs = getObjects(a);
            objs.forEach(obj -> obj.getPlayer().sendPacket(packet));
        }
    }

    public void sendPacket(final IStaticPacket packet) {
        sendPackets(packet);
    }

    public void sendPackets(final IStaticPacket... packet) {
        for (final DuelSnapshotObject d : this) {
            d.getPlayer().sendPacket(packet);
        }
    }

    public void abortDuel(final Player player) {
        _aborted = true;
        _winner = TeamType.NONE;
        stopEvent();
    }

    protected IStaticPacket checkPlayer(final Player player) {
        IStaticPacket packet = null;
        if (player.isInCombat()) {
            packet = ((SysMsgContainer) new SystemMessage2(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_ENGAGED_IN_BATTLE)).addName(player);
        } else if (player.isDead() || player.isAlikeDead() || player.getCurrentHpPercents() < 50.0 || player.getCurrentMpPercents() < 50.0 || player.getCurrentCpPercents() < 50.0) {
            packet = ((SysMsgContainer) new SystemMessage2(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1S_HP_OR_MP_IS_BELOW_50)).addName(player);
        } else if (player.getEvent(DuelEvent.class) != null) {
            packet = ((SysMsgContainer) new SystemMessage2(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_ALREADY_ENGAGED_IN_A_DUEL)).addName(player);
        } else if (player.getEvent(ClanHallSiegeEvent.class) != null || player.getEvent(ClanHallNpcSiegeEvent.class) != null) {
            packet = ((SysMsgContainer) new SystemMessage2(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_PARTICIPATING_IN_A_CLAN_HALL_WAR)).addName(player);
        } else if (player.getEvent(SiegeEvent.class) != null) {
            packet = ((SysMsgContainer) new SystemMessage2(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_PARTICIPATING_IN_A_SIEGE_WAR)).addName(player);
        } else if (player.isOlyParticipant()) {
            packet = ((SysMsgContainer) new SystemMessage2(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_PARTICIPATING_IN_THE_OLYMPIAD)).addName(player);
        } else if (player.isCursedWeaponEquipped() || player.getKarma() > 0 || player.getPvpFlag() > 0) {
            packet = ((SysMsgContainer) new SystemMessage2(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_IN_A_CHAOTIC_STATE)).addName(player);
        } else if (player.isInStoreMode()) {
            packet = ((SysMsgContainer) new SystemMessage2(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_ENGAGED_IN_A_PRIVATE_STORE_OR_MANUFACTURE)).addName(player);
        } else if (player.isMounted() || player.isInBoat()) {
            packet = ((SysMsgContainer) new SystemMessage2(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_RIDING_A_BOAT_STEED_OR_STRIDER)).addName(player);
        } else if (player.isFishing()) {
            packet = ((SysMsgContainer) new SystemMessage2(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_FISHING)).addName(player);
        } else if (player.isInCombatZone() || player.isInPeaceZone() || player.isInWater() || player.isInZone(ZoneType.no_restart)) {
            packet = ((SysMsgContainer) new SystemMessage2(SystemMsg.C1_CANNOT_MAKE_A_CHALLENGE_TO_A_DUEL_BECAUSE_C1_IS_CURRENTLY_IN_A_DUELPROHIBITED_AREA_PEACEFUL_ZONE__SEVEN_SIGNS_ZONE__NEAR_WATER__RESTART_PROHIBITED_AREA)).addName(player);
        } else if (player.getTransformation() != 0) {
            packet = ((SysMsgContainer) new SystemMessage2(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_POLYMORPHED)).addName(player);
        }
        return packet;
    }

    protected IStaticPacket canDuel0(final Player requester, final Player target) {
        IStaticPacket packet = checkPlayer(target);
        if (packet == null && !requester.isInRangeZ(target, 1200L)) {
            packet = ((SysMsgContainer) new SystemMessage2(SystemMsg.C1_CANNOT_RECEIVE_A_DUEL_CHALLENGE_BECAUSE_C1_IS_TOO_FAR_AWAY)).addName(target);
        }
        return packet;
    }

    protected void updatePlayers(final boolean start, final boolean teleport) {
        for (final DuelSnapshotObject dso : this) {
            final Player player = dso.getPlayer();
            if (player == null) {
                continue;
            }
            if (teleport) {
                dso.teleport();
            } else if (start) {
                player.addEvent(this);
                player.setTeam(dso.getTeam());
            } else {
                player.removeEvent(this);
                dso.restore(_aborted);
                player.setTeam(TeamType.NONE);
            }
        }
    }

    @Override
    public SystemMsg checkForAttack(final Creature target, final Creature attacker, final Skill skill, final boolean force) {
        if (target.getTeam() == TeamType.NONE || attacker.getTeam() == TeamType.NONE || target.getTeam() == attacker.getTeam()) {
            return SystemMsg.INVALID_TARGET;
        }
        final DuelEvent duelEvent = target.getEvent(DuelEvent.class);
        if (duelEvent == null || duelEvent != this) {
            return SystemMsg.INVALID_TARGET;
        }
        return null;
    }

    @Override
    public boolean canAttack(final Creature target, final Creature attacker, final Skill skill, final boolean force) {
        if (target.getTeam() == TeamType.NONE || attacker.getTeam() == TeamType.NONE || target.getTeam() == attacker.getTeam()) {
            return false;
        }
        final DuelEvent duelEvent = target.getEvent(DuelEvent.class);
        return duelEvent != null && duelEvent == this;
    }

    @Override
    public void onAddEvent(final GameObject o) {
        if (o.isPlayer()) {
            o.getPlayer().addListener(_playerExitListener);
        }
    }

    @Override
    public void onRemoveEvent(final GameObject o) {
        if (o.isPlayer()) {
            o.getPlayer().removeListener(_playerExitListener);
        }
    }

    @Override
    public Iterator<DuelSnapshotObject> iterator() {
        final List<DuelSnapshotObject> blue = getObjects(BLUE_TEAM);
        final List<DuelSnapshotObject> red = getObjects(RED_TEAM);
        return Iterators.concat(blue.iterator(), red.iterator());
    }

    @Override
    public void reCalcNextTime(final boolean onInit) {
        registerActions();
    }

    @Override
    public void announce(final int i) {
        sendPacket(new SystemMessage2(SystemMsg.THE_DUEL_WILL_BEGIN_IN_S1_SECONDS).addInteger(i));
    }

    protected enum DuelState {
        EPrepare,
        EInProgress,
        EEnd
    }

    private class OnPlayerExitListenerImpl implements OnPlayerExitListener {
        @Override
        public void onPlayerExit(final Player player) {
            playerExit(player);
        }
    }
}
