package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.manager.MatchingRoomManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.matching.MatchingRoom;

public class ExManageMpccRoomMember extends L2GameServerPacket {
    public static int ADD_MEMBER;
    public static int UPDATE_MEMBER = 1;
    public static int REMOVE_MEMBER = 2;

    private int _type;
    private MpccRoomMemberInfo _memberInfo;

    public ExManageMpccRoomMember(final int type, final MatchingRoom room, final Player target) {
        _type = type;
        _memberInfo = new MpccRoomMemberInfo(target, room.getMemberType(target));
    }

    @Override
    protected void writeImpl() {
        writeEx(0x10);
        writeD(_type);
        writeD(_memberInfo.objectId);
        writeS(_memberInfo.name);
        writeD(_memberInfo.classId);
        writeD(_memberInfo.level);
        writeD(_memberInfo.location);
        writeD(_memberInfo.memberType);
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
