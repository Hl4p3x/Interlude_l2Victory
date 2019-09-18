package ru.j2dev.gameserver.model.matching;

import ru.j2dev.gameserver.model.CommandChannel;
import ru.j2dev.gameserver.model.Party;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.*;

public class CCMatchingRoom extends MatchingRoom {
    public CCMatchingRoom(final Player leader, final int minLevel, final int maxLevel, final int maxMemberSize, final int lootType, final String topic) {
        super(leader, minLevel, maxLevel, maxMemberSize, lootType, topic);
        leader.sendPacket(SystemMsg.THE_COMMAND_CHANNEL_MATCHING_ROOM_WAS_CREATED);
    }

    @Override
    public SystemMsg notValidMessage() {
        return SystemMsg.YOU_CANNOT_ENTER_THE_COMMAND_CHANNEL_MATCHING_ROOM_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS;
    }

    @Override
    public SystemMsg enterMessage() {
        return SystemMsg.C1_ENTERED_THE_COMMAND_CHANNEL_MATCHING_ROOM;
    }

    @Override
    public SystemMsg exitMessage(final boolean toOthers, final boolean kick) {
        if (!toOthers) {
            return kick ? SystemMsg.YOU_WERE_EXPELLED_FROM_THE_COMMAND_CHANNEL_MATCHING_ROOM : SystemMsg.YOU_EXITED_FROM_THE_COMMAND_CHANNEL_MATCHING_ROOM;
        }
        return null;
    }

    @Override
    public SystemMsg closeRoomMessage() {
        return SystemMsg.THE_COMMAND_CHANNEL_MATCHING_ROOM_WAS_CANCELLED;
    }

    @Override
    public L2GameServerPacket closeRoomPacket() {
        return ExDissmissMpccRoom.STATIC;
    }

    @Override
    public L2GameServerPacket infoRoomPacket() {
        return new PartyRoomInfo(this);
    }

    @Override
    public L2GameServerPacket addMemberPacket(final Player $member, final Player active) {
        return new ExManageMpccRoomMember(ExManageMpccRoomMember.ADD_MEMBER, this, active);
    }

    @Override
    public L2GameServerPacket removeMemberPacket(final Player $member, final Player active) {
        return new ExManageMpccRoomMember(ExManageMpccRoomMember.REMOVE_MEMBER, this, active);
    }

    @Override
    public L2GameServerPacket updateMemberPacket(final Player $member, final Player active) {
        return new ExManageMpccRoomMember(ExManageMpccRoomMember.UPDATE_MEMBER, this, active);
    }

    @Override
    public L2GameServerPacket membersPacket(final Player active) {
        return new ExMpccRoomMember(this, active);
    }

    @Override
    public int getType() {
        return CCMatchingRoom.CC_MATCHING;
    }

    @Override
    public void disband() {
        final Party party = _leader.getParty();
        if (party != null) {
            final CommandChannel commandChannel = party.getCommandChannel();
            if (commandChannel != null) {
                commandChannel.setMatchingRoom(null);
            }
        }
        super.disband();
    }

    @Override
    public int getMemberType(final Player member) {
        final Party party = _leader.getParty();
        final CommandChannel commandChannel = party.getCommandChannel();
        if (member == _leader) {
            return MatchingRoom.UNION_LEADER;
        }
        if (member.getParty() == null) {
            return MatchingRoom.WAIT_NORMAL;
        }
        if (member.getParty() == party || commandChannel.getParties().contains(member.getParty())) {
            return MatchingRoom.UNION_PARTY;
        }
        if (member.getParty() != null) {
            return MatchingRoom.WAIT_PARTY;
        }
        return MatchingRoom.WAIT_NORMAL;
    }
}
