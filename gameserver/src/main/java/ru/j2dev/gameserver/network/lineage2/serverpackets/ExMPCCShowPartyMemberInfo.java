package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Party;

import java.util.ArrayList;
import java.util.List;

public class ExMPCCShowPartyMemberInfo extends L2GameServerPacket {
    private final List<PartyMemberInfo> members;

    public ExMPCCShowPartyMemberInfo(final Party party) {
        members = new ArrayList<>();
        party.getPartyMembers().stream().map(_member -> new PartyMemberInfo(_member.getName(), _member.getObjectId(), _member.getClassId().getId())).forEach(members::add);
    }

    @Override
    protected final void writeImpl() {
        writeEx(0x4a);
        writeD(members.size());
        members.forEach(member -> {
            writeS(member.name);
            writeD(member.object_id);
            writeD(member.class_id);
        });
    }

    static class PartyMemberInfo {
        public final String name;
        public final int object_id;
        public final int class_id;

        public PartyMemberInfo(final String _name, final int _object_id, final int _class_id) {
            name = _name;
            object_id = _object_id;
            class_id = _class_id;
        }
    }
}
