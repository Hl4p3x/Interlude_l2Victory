package ru.j2dev.gameserver.model.matching;

import ru.j2dev.gameserver.listener.actor.player.OnPlayerPartyInviteListener;
import ru.j2dev.gameserver.listener.actor.player.OnPlayerPartyLeaveListener;
import ru.j2dev.gameserver.manager.MatchingRoomManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.PlayerGroup;
import ru.j2dev.gameserver.network.lineage2.components.IStaticPacket;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.L2GameServerPacket;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class MatchingRoom implements PlayerGroup {
    public static final int CC_MATCHING = 1;
    public static final int ROOM_MASTER = 1;
    public static final int PARTY_MEMBER = 2;
    public static final int UNION_LEADER = 3;
    public static final int UNION_PARTY = 4;
    public static final int WAIT_PARTY = 5;
    public static final int WAIT_NORMAL = 6;
    public static int PARTY_MATCHING;
    public static int WAIT_PLAYER;
    protected final Player _leader;
    protected final Set<Player> _members;
    private final int _id;
    private final PartyListenerImpl _listener;
    private int _minLevel;
    private int _maxLevel;
    private int _maxMemberSize;
    private int _lootType;
    private String _topic;

    public MatchingRoom(final Player leader, final int minLevel, final int maxLevel, final int maxMemberSize, final int lootType, final String topic) {
        _listener = new PartyListenerImpl();
        _members = new CopyOnWriteArraySet<>();
        _leader = leader;
        _id = MatchingRoomManager.getInstance().addMatchingRoom(this);
        _minLevel = minLevel;
        _maxLevel = maxLevel;
        _maxMemberSize = maxMemberSize;
        _lootType = lootType;
        _topic = topic;
        addMember0(leader, null);
    }

    public boolean addMember(final Player player) {
        if (_members.contains(player)) {
            return true;
        }
        if (player.getLevel() < getMinLevel() || player.getLevel() > getMaxLevel() || getPlayers().size() >= getMaxMembersSize()) {
            player.sendPacket(notValidMessage());
            return false;
        }
        return addMember0(player, new SystemMessage2(enterMessage()).addName(player));
    }

    private boolean addMember0(final Player player, final L2GameServerPacket p) {
        if (!_members.isEmpty()) {
            player.addListener(_listener);
        }
        _members.add(player);
        player.setMatchingRoom(this);
        for (final Player $member : this) {
            if ($member != player) {
                $member.sendPacket(p, addMemberPacket($member, player));
            }
        }
        MatchingRoomManager.getInstance().removeFromWaitingList(player);
        player.sendPacket(infoRoomPacket(), membersPacket(player));
        player.sendChanges();
        return true;
    }

    public void removeMember(final Player member, final boolean oust) {
        if (!_members.remove(member)) {
            return;
        }
        member.removeListener(_listener);
        member.setMatchingRoom(null);
        if (_members.isEmpty()) {
            disband();
        } else {
            final L2GameServerPacket infoPacket = infoRoomPacket();
            final SystemMsg exitMessage0 = exitMessage(true, oust);
            final L2GameServerPacket exitMessage2 = (exitMessage0 != null) ? new SystemMessage2(exitMessage0).addName(member) : null;
            for (final Player player : this) {
                player.sendPacket(infoPacket, removeMemberPacket(player, member), exitMessage2);
            }
        }
        member.sendPacket(closeRoomPacket(), exitMessage(false, oust));
        MatchingRoomManager.getInstance().addToWaitingList(member);
        member.sendChanges();
    }

    public void broadcastPlayerUpdate(final Player player) {
        for (final Player $member : this) {
            $member.sendPacket(updateMemberPacket($member, player));
        }
    }

    public void disband() {
        for (final Player player : this) {
            player.removeListener(_listener);
            player.sendPacket(closeRoomMessage());
            player.sendPacket(closeRoomPacket());
            player.setMatchingRoom(null);
            player.sendChanges();
            MatchingRoomManager.getInstance().addToWaitingList(player);
        }
        _members.clear();
        MatchingRoomManager.getInstance().removeMatchingRoom(this);
    }

    public abstract SystemMsg notValidMessage();

    public abstract SystemMsg enterMessage();

    public abstract SystemMsg exitMessage(final boolean p0, final boolean p1);

    public abstract SystemMsg closeRoomMessage();

    public abstract L2GameServerPacket closeRoomPacket();

    public abstract L2GameServerPacket infoRoomPacket();

    public abstract L2GameServerPacket addMemberPacket(final Player p0, final Player p1);

    public abstract L2GameServerPacket removeMemberPacket(final Player p0, final Player p1);

    public abstract L2GameServerPacket updateMemberPacket(final Player p0, final Player p1);

    public abstract L2GameServerPacket membersPacket(final Player p0);

    public abstract int getType();

    public abstract int getMemberType(final Player p0);

    @Override
    public void broadCast(final IStaticPacket... arg) {
        for (final Player player : this) {
            player.sendPacket(arg);
        }
    }

    public int getId() {
        return _id;
    }

    public int getMinLevel() {
        return _minLevel;
    }

    public void setMinLevel(final int minLevel) {
        _minLevel = minLevel;
    }

    public int getMaxLevel() {
        return _maxLevel;
    }

    public void setMaxLevel(final int maxLevel) {
        _maxLevel = maxLevel;
    }

    public String getTopic() {
        return _topic;
    }

    public void setTopic(final String topic) {
        _topic = topic;
    }

    public int getMaxMembersSize() {
        return _maxMemberSize;
    }

    public int getLocationId() {
        return MatchingRoomManager.getInstance().getLocation(_leader);
    }

    public Player getLeader() {
        return _leader;
    }

    public Collection<Player> getPlayers() {
        return _members;
    }

    public int getLootType() {
        return _lootType;
    }

    public void setLootType(final int lootType) {
        _lootType = lootType;
    }

    @Override
    public Iterator<Player> iterator() {
        return _members.iterator();
    }

    public void setMaxMemberSize(final int maxMemberSize) {
        _maxMemberSize = maxMemberSize;
    }

    private class PartyListenerImpl implements OnPlayerPartyInviteListener, OnPlayerPartyLeaveListener {
        @Override
        public void onPartyInvite(final Player player) {
            broadcastPlayerUpdate(player);
        }

        @Override
        public void onPartyLeave(final Player player) {
            broadcastPlayerUpdate(player);
        }
    }
}
