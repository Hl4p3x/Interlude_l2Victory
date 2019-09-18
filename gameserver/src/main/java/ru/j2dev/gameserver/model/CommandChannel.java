package ru.j2dev.gameserver.model;

import com.google.common.collect.Iterators;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.model.instances.NpcFriendInstance;
import ru.j2dev.gameserver.model.matching.MatchingRoom;
import ru.j2dev.gameserver.network.lineage2.components.IStaticPacket;
import ru.j2dev.gameserver.network.lineage2.serverpackets.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class CommandChannel implements PlayerGroup {
    public static final int STRATEGY_GUIDE_ID = 8871;
    public static final int CLAN_IMPERIUM_ID = 391;
    private final List<Party> _commandChannelParties;
    private Player _commandChannelLeader;
    private int _commandChannelLvl;
    private Reflection _reflection;
    private MatchingRoom _matchingRoom;

    public CommandChannel(final Player leader) {
        _commandChannelParties = new CopyOnWriteArrayList<>();
        _commandChannelLeader = leader;
        _commandChannelParties.add(leader.getParty());
        _commandChannelLvl = leader.getParty().getLevel();
        leader.getParty().setCommandChannel(this);
        broadCast(ExMPCCOpen.STATIC);
    }

    public static boolean checkAuthority(final Player creator) {
        if (creator.getClan() == null || !creator.isInParty() || !creator.getParty().isLeader(creator) || (Config.CHECK_CLAN_RANK_ON_COMMAND_CHANNEL_CREATE && creator.getPledgeClass() < 4)) {
            creator.sendPacket(Msg.YOU_DO_NOT_HAVE_AUTHORITY_TO_USE_THE_COMMAND_CHANNEL);
            return false;
        }
        final boolean haveSkill = creator.getSkillLevel(CLAN_IMPERIUM_ID) > 0;
        final boolean haveItem = creator.getInventory().getItemByItemId(STRATEGY_GUIDE_ID) != null;
        if (!haveSkill && !haveItem) {
            creator.sendPacket(Msg.YOU_DO_NOT_HAVE_AUTHORITY_TO_USE_THE_COMMAND_CHANNEL);
            return false;
        }
        return true;
    }

    public void addParty(final Party party) {
        broadCast(new ExMPCCPartyInfoUpdate(party, 1));
        _commandChannelParties.add(party);
        refreshLevel();
        party.setCommandChannel(this);
        for (final Player partyMember : party) {
            partyMember.sendPacket(ExMPCCOpen.STATIC);
            if (_matchingRoom != null) {
                _matchingRoom.broadcastPlayerUpdate(partyMember);
            }
        }
    }

    public void removeParty(final Party party) {
        _commandChannelParties.remove(party);
        refreshLevel();
        party.setCommandChannel(null);
        party.broadCast(ExMPCCClose.STATIC);
        final Reflection reflection = getReflection();
        if (reflection != null) {
            party.getPartyMembers().forEach(player -> player.teleToLocation(reflection.getReturnLoc(), 0));
        }
        if (_commandChannelParties.size() < 2) {
            disbandChannel();
        } else {
            for (final Player partyMember : party) {
                partyMember.sendPacket(new ExMPCCPartyInfoUpdate(party, 0));
                if (_matchingRoom != null) {
                    _matchingRoom.broadcastPlayerUpdate(partyMember);
                }
            }
        }
    }

    public void disbandChannel() {
        broadCast(Msg.THE_COMMAND_CHANNEL_HAS_BEEN_DISBANDED);
        for (final Party party : _commandChannelParties) {
            party.setCommandChannel(null);
            party.broadCast(ExMPCCClose.STATIC);
            if (isInReflection()) {
                party.broadCast(new SystemMessage(2106).addNumber(1));
            }
        }
        final Reflection reflection = getReflection();
        if (reflection != null) {
            reflection.startCollapseTimer(60000L);
            setReflection(null);
        }
        if (_matchingRoom != null) {
            _matchingRoom.disband();
        }
        _commandChannelParties.clear();
        _commandChannelLeader = null;
    }

    public int getMemberCount() {
        return _commandChannelParties.stream().mapToInt(Party::getMemberCount).sum();
    }

    @Override
    public void broadCast(final IStaticPacket... gsp) {
        for (final Party party : _commandChannelParties) {
            party.broadCast(gsp);
        }
    }

    public void broadcastToChannelPartyLeaders(final L2GameServerPacket gsp) {
        _commandChannelParties.stream().map(Party::getPartyLeader).filter(Objects::nonNull).forEach(leader -> leader.sendPacket(gsp));
    }

    public List<Party> getParties() {
        return _commandChannelParties;
    }

    @Deprecated
    public List<Player> getMembers() {
        final List<Player> members = new ArrayList<>(_commandChannelParties.size());
        getParties().stream().map(Party::getPartyMembers).forEach(members::addAll);
        return members;
    }

    @Override
    public Iterator<Player> iterator() {
        final List<Player> players = new ArrayList<>(_commandChannelParties.size());

        getParties().stream().map(Party::getPartyMembers).forEach(players::addAll);

        return Iterators.unmodifiableIterator(players.iterator());
    }

    public int getLevel() {
        return _commandChannelLvl;
    }

    public Player getChannelLeader() {
        return _commandChannelLeader;
    }

    public void setChannelLeader(final Player newLeader) {
        _commandChannelLeader = newLeader;
        broadCast(new SystemMessage(1589).addString(newLeader.getName()));
    }

    public boolean meetRaidWarCondition(final NpcFriendInstance npc) {
        if (!npc.isRaid()) {
            return false;
        }
        final int npcId = npc.getNpcId();
        switch (npcId) {
            case 29001:
            case 29006:
            case 29014:
            case 29022: {
                return getMemberCount() > 36;
            }
            case 29020: {
                return getMemberCount() > 56;
            }
            case 29019: {
                return getMemberCount() > 225;
            }
            case 29028: {
                return getMemberCount() > 99;
            }
            default: {
                return getMemberCount() > 18;
            }
        }
    }

    private void refreshLevel() {
        _commandChannelLvl = 0;
        _commandChannelParties.stream().filter(pty -> pty.getLevel() > _commandChannelLvl).forEach(pty -> _commandChannelLvl = pty.getLevel());
    }

    public boolean isInReflection() {
        return _reflection != null;
    }

    public Reflection getReflection() {
        return _reflection;
    }

    public void setReflection(final Reflection reflection) {
        _reflection = reflection;
    }

    public MatchingRoom getMatchingRoom() {
        return _matchingRoom;
    }

    public void setMatchingRoom(final MatchingRoom matchingRoom) {
        _matchingRoom = matchingRoom;
    }
}
