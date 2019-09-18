package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExChangeClientEffectInfo extends L2GameServerPacket {
    private final int _state;

    public ExChangeClientEffectInfo(final int state) {
        _state = state;
    }

    @Override
    protected void writeImpl() {
        writeEx(0xc1);
        writeD(0);
        writeD(_state);
    }
}
