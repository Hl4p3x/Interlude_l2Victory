package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.pledge.SubUnit;
import ru.j2dev.gameserver.model.pledge.UnitMember;

public class PledgeShowMemberListUpdate extends L2GameServerPacket {
    private final String _name;
    private final int _lvl;
    private final int _classId;
    private final int _sex;
    private final int _isOnline;
    private final int _objectId;
    private final int _pledgeType;
    private int _isApprentice;

    public PledgeShowMemberListUpdate(final Player player) {
        _name = player.getName();
        _lvl = player.getLevel();
        _classId = player.getClassId().getId();
        _sex = player.getSex();
        _objectId = player.getObjectId();
        _isOnline = (player.isOnline() ? 1 : 0);
        _pledgeType = player.getPledgeType();
        final SubUnit subUnit = player.getSubUnit();
        final UnitMember member = (subUnit == null) ? null : subUnit.getUnitMember(_objectId);
        if (member != null) {
            _isApprentice = (member.hasSponsor() ? 1 : 0);
        }
    }

    public PledgeShowMemberListUpdate(final UnitMember cm) {
        _name = cm.getName();
        _lvl = cm.getLevel();
        _classId = cm.getClassId();
        _sex = cm.getSex();
        _objectId = cm.getObjectId();
        _isOnline = (cm.isOnline() ? 1 : 0);
        _pledgeType = cm.getPledgeType();
        _isApprentice = (cm.hasSponsor() ? 1 : 0);
    }

    @Override
    protected final void writeImpl() {
        writeC(0x54);
        writeS(_name);
        writeD(_lvl);
        writeD(_classId);
        writeD(_sex);
        writeD(_objectId);
        writeD(_isOnline);
        writeD(_pledgeType);
        writeD(_isApprentice);
    }
}
