package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.commons.lang.ArrayUtils;
import ru.j2dev.gameserver.manager.MatchingRoomManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.matching.MatchingRoom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExPartyRoomMember extends L2GameServerPacket {
    private final int _type;
    private List<PartyRoomMemberInfo> _members;

    public ExPartyRoomMember(final MatchingRoom room, final Player activeChar) {
        _members = Collections.emptyList();
        _type = room.getMemberType(activeChar);
        _members = new ArrayList<>(room.getPlayers().size());
        room.getPlayers().forEach($member -> _members.add(new PartyRoomMemberInfo($member, room.getMemberType($member))));
    }

    @Override
    protected final void writeImpl() {
        writeEx(0xe);
        writeD(_type);
        writeD(_members.size());
        _members.forEach(member_info -> {
            writeD(member_info.objectId);
            writeS(member_info.name);
            writeD(member_info.classId);
            writeD(member_info.level);
            writeD(member_info.location);
            writeD(member_info.memberType);
        });
    }

    static class PartyRoomMemberInfo {
        public final int objectId;
        public final int classId;
        public final int level;
        public final int location;
        public final int memberType;
        public final String name;
        public final int[] instanceReuses;

        public PartyRoomMemberInfo(final Player member, final int type) {
            objectId = member.getObjectId();
            name = member.getName();
            classId = member.getClassId().ordinal();
            level = member.getLevel();
            location = MatchingRoomManager.getInstance().getLocation(member);
            memberType = type;
            instanceReuses = ArrayUtils.toArray(member.getInstanceReuses().keySet());
        }
    }
}
