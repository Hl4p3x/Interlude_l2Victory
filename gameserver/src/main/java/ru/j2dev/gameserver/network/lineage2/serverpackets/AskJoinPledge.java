package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class AskJoinPledge extends L2GameServerPacket {
    private final int _requestorId;
    private final String _pledgeName;

    public AskJoinPledge(final int requestorId, final String pledgeName) {
        _requestorId = requestorId;
        _pledgeName = pledgeName;
    }

    @Override
    protected final void writeImpl() {
        writeC(0x32);
        writeD(_requestorId);
        writeS(_pledgeName);
    }
}
