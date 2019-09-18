package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.manager.MatchingRoomManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.matching.MatchingRoom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExMpccRoomMember extends L2GameServerPacket {
    private final int _type;
    private List<MpccRoomMemberInfo> _members;

    public ExMpccRoomMember(final MatchingRoom room, final Player player) {
        _members = Collections.emptyList();
        _type = room.getMemberType(player);
        _members = new ArrayList<>(room.getPlayers().size());
        room.getPlayers().forEach(member -> _members.add(new MpccRoomMemberInfo(member, room.getMemberType(member))));
    }

    @Override
    public void writeImpl() {
        writeEx(0xe);
        writeD(_type);
        writeD(_members.size());
        _members.forEach(member -> {
            writeD(member.objectId);
            writeS(member.name);
            writeD(member.level);
            writeD(member.classId);
            writeD(member.location);
            writeD(member.memberType);
        });
    }

    static class MpccRoomMemberInfo {
        public final int objectId;
        public final int classId;
        public final int level;
        public final int location;
        public final int memberType;
        public final String name;

        public MpccRoomMemberInfo(final Player member, final int type) {
            objectId = member.getObjectId();
            name = member.getName();
            classId = member.getClassId().ordinal();
            level = member.getLevel();
            location = MatchingRoomManager.getInstance().getLocation(member);
            memberType = type;
        }
    }
}
