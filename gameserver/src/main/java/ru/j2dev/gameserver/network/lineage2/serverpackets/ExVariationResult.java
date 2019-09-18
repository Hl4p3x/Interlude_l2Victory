package ru.j2dev.gameserver.network.lineage2.serverpackets;

public class ExVariationResult extends L2GameServerPacket {
    public static final ExVariationResult FAIL_PACKET = new ExVariationResult(0, 0, 0);

    private int _stat1;
    private int _stat2;
    private int _result;

    public ExVariationResult(final int unk1, final int unk2, final int unk3) {
        _stat1 = unk1;
        _stat2 = unk2;
        _result = unk3;
    }

    @Override
    protected void writeImpl() {
        writeEx(0x55);
        writeD(_stat1);
        writeD(_stat2);
        writeD(_result);
    }
}
