package ru.j2dev.gameserver.model.matching;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExClosePartyRoom;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExPartyRoomMember;
import ru.j2dev.gameserver.network.lineage2.serverpackets.L2GameServerPacket;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PartyRoomInfo;

public class PartyMatchingRoom extends MatchingRoom {
    public PartyMatchingRoom(final Player leader, final int minLevel, final int maxLevel, final int maxMemberSize, final int lootType, final String topic) {
        super(leader, minLevel, maxLevel, maxMemberSize, lootType, topic);
        leader.broadcastCharInfo();
    }

    @Override
    public SystemMsg notValidMessage() {
        return SystemMsg.YOU_DO_NOT_MEET_THE_REQUIREMENTS_TO_ENTER_THAT_PARTY_ROOM;
    }

    @Override
    public SystemMsg enterMessage() {
        return SystemMsg.C1_HAS_ENTERED_THE_PARTY_ROOM;
    }

    @Override
    public SystemMsg exitMessage(final boolean toOthers, final boolean kick) {
        if (toOthers) {
            return kick ? SystemMsg.C1_HAS_BEEN_KICKED_FROM_THE_PARTY_ROOM : SystemMsg.C1_HAS_LEFT_THE_PARTY_ROOM;
        }
        return kick ? SystemMsg.YOU_HAVE_BEEN_OUSTED_FROM_THE_PARTY_ROOM : SystemMsg.YOU_HAVE_EXITED_THE_PARTY_ROOM;
    }

    @Override
    public SystemMsg closeRoomMessage() {
        return SystemMsg.THE_PARTY_ROOM_HAS_BEEN_DISBANDED;
    }

    @Override
    public L2GameServerPacket closeRoomPacket() {
        return ExClosePartyRoom.STATIC;
    }

    @Override
    public L2GameServerPacket infoRoomPacket() {
        return new PartyRoomInfo(this);
    }

    @Override
    public L2GameServerPacket addMemberPacket(final Player $member, final Player active) {
        return membersPacket($member);
    }

    @Override
    public L2GameServerPacket removeMemberPacket(final Player $member, final Player active) {
        return membersPacket($member);
    }

    @Override
    public L2GameServerPacket updateMemberPacket(final Player $member, final Player active) {
        return membersPacket($member);
    }

    @Override
    public L2GameServerPacket membersPacket(final Player active) {
        return new ExPartyRoomMember(this, active);
    }

    @Override
    public int getType() {
        return PartyMatchingRoom.PARTY_MATCHING;
    }

    @Override
    public int getMemberType(final Player member) {
        return member.equals(_leader) ? PartyMatchingRoom.ROOM_MASTER : ((member.getParty() != null && _leader.getParty() == member.getParty()) ? PartyMatchingRoom.PARTY_MEMBER : PartyMatchingRoom.WAIT_PLAYER);
    }
}
