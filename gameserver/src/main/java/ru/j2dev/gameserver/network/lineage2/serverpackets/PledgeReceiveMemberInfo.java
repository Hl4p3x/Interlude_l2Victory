package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.pledge.UnitMember;

public class PledgeReceiveMemberInfo extends L2GameServerPacket {
    private final UnitMember _member;

    public PledgeReceiveMemberInfo(final UnitMember member) {
        _member = member;
    }

    @Override
    protected final void writeImpl() {
        writeEx(0x3d);
        writeD(_member.getPledgeType());
        writeS(_member.getName());
        writeS(_member.getTitle());
        writeD(_member.getPowerGrade());
        writeS(_member.getSubUnit().getName());
        writeS(_member.getRelatedName());
    }
}
