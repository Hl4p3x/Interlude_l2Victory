package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class SendTradeDone extends L2GameServerPacket {
    public static final L2GameServerPacket SUCCESS = new SendTradeDone(1);
    public static final L2GameServerPacket FAIL = new SendTradeDone(0);

    private int _response;

    private SendTradeDone(final int num) {
        _response = num;
    }

    @Override
    protected final void writeImpl() {
        writeC(0x22);
        writeD(_response);
    }
}
