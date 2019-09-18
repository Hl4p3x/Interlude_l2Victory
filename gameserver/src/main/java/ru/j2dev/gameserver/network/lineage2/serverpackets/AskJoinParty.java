package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class AskJoinParty extends L2GameServerPacket {
    private final String _requestorName;
    private final int _itemDistribution;

    public AskJoinParty(final String requestorName, final int itemDistribution) {
        _requestorName = requestorName;
        _itemDistribution = itemDistribution;
    }

    @Override
    protected final void writeImpl() {
        writeC(0x39);
        writeS(_requestorName);
        writeD(_itemDistribution);
    }
}
