package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ShowCalc extends L2GameServerPacket {
    private final int _calculatorId;

    public ShowCalc(final int calculatorId) {
        _calculatorId = calculatorId;
    }

    @Override
    protected final void writeImpl() {
        writeC(0xdc);
        writeD(_calculatorId);
    }
}
