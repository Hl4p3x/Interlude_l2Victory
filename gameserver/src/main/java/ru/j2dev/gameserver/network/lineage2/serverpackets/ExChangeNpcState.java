package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExChangeNpcState extends L2GameServerPacket {
    private final int _objId;
    private final int _state;

    public ExChangeNpcState(final int objId, final int state) {
        _objId = objId;
        _state = state;
    }

    @Override
    protected void writeImpl() {
        writeEx(0xbe);
        writeD(_objId);
        writeD(_state);
    }
}
