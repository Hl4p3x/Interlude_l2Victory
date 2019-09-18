package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Party;
import ru.j2dev.gameserver.model.Player;

public class ExMPCCPartyInfoUpdate extends L2GameServerPacket {
    final Player _leader;
    private final Party _party;
    private final int _mode;
    private final int _count;

    public ExMPCCPartyInfoUpdate(final Party party, final int mode) {
        _party = party;
        _mode = mode;
        _count = _party.getMemberCount();
        _leader = _party.getPartyLeader();
    }

    @Override
    protected void writeImpl() {
        writeEx(0x5a);
        writeS(_leader.getName());
        writeD(_leader.getObjectId());
        writeD(_count);
        writeD(_mode);
    }
}
