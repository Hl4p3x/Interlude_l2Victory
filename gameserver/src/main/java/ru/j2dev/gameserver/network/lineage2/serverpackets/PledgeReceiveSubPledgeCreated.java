package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.pledge.SubUnit;

public class PledgeReceiveSubPledgeCreated extends L2GameServerPacket {
    private final int type;
    private final String _name;
    private final String leader_name;

    public PledgeReceiveSubPledgeCreated(final SubUnit subPledge) {
        type = subPledge.getType();
        _name = subPledge.getName();
        leader_name = subPledge.getLeaderName();
    }

    @Override
    protected final void writeImpl() {
        writeEx(0x3f);
        writeD(1);
        writeD(type);
        writeS(_name);
        writeS(leader_name);
    }
}
