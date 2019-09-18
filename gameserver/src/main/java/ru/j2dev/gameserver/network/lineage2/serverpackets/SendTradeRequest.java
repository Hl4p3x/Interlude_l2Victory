package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class SendTradeRequest extends L2GameServerPacket {
    private final int _senderId;

    public SendTradeRequest(final int senderId) {
        _senderId = senderId;
    }

    @Override
    protected final void writeImpl() {
        writeC(0x5e);
        writeD(_senderId);
    }
}
