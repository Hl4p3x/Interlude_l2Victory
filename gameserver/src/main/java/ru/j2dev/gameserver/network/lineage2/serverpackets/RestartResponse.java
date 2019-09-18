package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class RestartResponse extends L2GameServerPacket {
    public static final RestartResponse OK = new RestartResponse(1);
    public static final RestartResponse FAIL = new RestartResponse(0);

    private String _message;
    private int _param;

    public RestartResponse(final int param) {
        _message = "bye";
        _param = param;
    }

    @Override
    protected final void writeImpl() {
        writeC(0x5f);
        writeD(_param);
        writeS(_message);
    }
}
