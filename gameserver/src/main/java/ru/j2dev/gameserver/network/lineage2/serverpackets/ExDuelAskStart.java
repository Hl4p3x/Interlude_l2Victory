package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExDuelAskStart extends L2GameServerPacket {
    final String _requestor;
    final int _isPartyDuel;

    public ExDuelAskStart(final String requestor, final int isPartyDuel) {
        _requestor = requestor;
        _isPartyDuel = isPartyDuel;
    }

    @Override
    protected final void writeImpl() {
        writeEx(0x4b);
        writeS(_requestor);
        writeD(_isPartyDuel);
    }
}
